package razerdp.github.com.publish;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.razerdp.github.com.common.constant.NetConstant;
import com.razerdp.github.com.common.entity.photo.PhotoBrowserInfo;
import com.razerdp.github.com.common.manager.LocalHostManager;
import com.razerdp.github.com.common.request.AddMomentsRequest;
import com.razerdp.github.com.common.router.RouterList;
import com.socks.library.KLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

//import cn.bmob.v3.datatype.BmobFile;
//import cn.bmob.v3.exception.BmobException;
//import cn.bmob.v3.listener.UploadBatchListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import razerdp.github.com.lib.base.AppjsException;
import razerdp.github.com.lib.common.entity.ImageInfo;
import razerdp.github.com.lib.helper.AppSetting;
import razerdp.github.com.lib.interfaces.adapter.TextWatcherAdapter;
import razerdp.github.com.lib.manager.compress.CompressManager;
import razerdp.github.com.lib.manager.compress.OnCompressListener;
import razerdp.github.com.lib.network.base.OnResponseListener;
import razerdp.github.com.lib.network.ssl.MySslUtil;
import razerdp.github.com.lib.utils.StringUtil;
import razerdp.github.com.lib.utils.ToolUtil;
import razerdp.github.com.ui.base.BaseTitleBarActivity;
import razerdp.github.com.ui.helper.PhotoHelper;
import razerdp.github.com.ui.imageloader.ImageLoadMnanger;
import razerdp.github.com.ui.util.SwitchActivityTransitionUtil;
import razerdp.github.com.ui.util.UIHelper;
import razerdp.github.com.ui.util.ViewUtil;
import razerdp.github.com.ui.widget.common.TitleBar;
import razerdp.github.com.ui.widget.imageview.PreviewImageView;
import razerdp.github.com.ui.widget.popup.PopupProgress;
import razerdp.github.com.ui.widget.popup.SelectPhotoMenuPopup;


/**
 * Created by liushaoming on 2017/3/1.
 * <p>
 * 发布朋友圈页面
 */

@Route(path = RouterList.PublishActivity.path)
public class PublishActivity extends BaseTitleBarActivity {
    @Autowired(name = RouterList.PublishActivity.key_mode)
    int mode = -1;

    private boolean canTitleRightClick = false;
    private List<ImageInfo> selectedPhotos;

    private EditText mInputContent;
    private PreviewImageView<ImageInfo> mPreviewImageView;

    private SelectPhotoMenuPopup mSelectPhotoMenuPopup;
    private PopupProgress mPopupProgress;

    private long userId;
    private String token;
    private OkHttpClient httpClient;
    private Gson gson = new Gson();
    private int uploadedCount = 0;

    @Override
    public void onHandleIntent(Intent intent) {
        mode = intent.getIntExtra(RouterList.PublishActivity.key_mode, -1);
        selectedPhotos = intent.getParcelableArrayListExtra(RouterList.PublishActivity.key_photoList);
        userId = intent.getLongExtra(RouterList.PublishActivity.key_userId, 0);
        token = intent.getStringExtra(RouterList.PublishActivity.key_token);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        if (mode == -1) {
            finish();
            return;
        } else if (mode == RouterList.PublishActivity.MODE_MULTI && selectedPhotos == null) {
            finish();
            return;
        }
        initData();
        initView();
    }

    public void initData() {//必须调用
//        httpClient = new OkHttpClient.Builder()
//                .connectTimeout(5, TimeUnit.SECONDS)
//                .writeTimeout(10, TimeUnit.SECONDS)
//                .readTimeout(30, TimeUnit.SECONDS)
//                .build();
        httpClient = MySslUtil.newOkHttpClient();
    }

    private void initView() {
        initTitle();
        mInputContent = findView(R.id.publish_input);
        mPreviewImageView = findView(R.id.preview_image_content);
        ViewUtil.setViewsVisible(mode == RouterList.PublishActivity.MODE_TEXT ? View.GONE : View.VISIBLE, mPreviewImageView);
        mInputContent.setHint(mode == RouterList.PublishActivity.MODE_MULTI ? "这一刻的想法..." : null);
        mInputContent.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                refreshTitleRightClickable();
            }
        });

        if (mode == RouterList.PublishActivity.MODE_TEXT) {
            UIHelper.showInputMethod(mInputContent, 300);
        }

        if (mode == RouterList.PublishActivity.MODE_MULTI) {
            initPreviewImageView();
            loadImage();
        }
        refreshTitleRightClickable();
    }

    private void initPreviewImageView() {
        mPreviewImageView.setOnPhotoClickListener(new PreviewImageView.OnPhotoClickListener<ImageInfo>() {
            @Override
            public void onPhotoClickListener(int pos, ImageInfo data, @NonNull ImageView imageView) {
                PhotoBrowserInfo info = PhotoBrowserInfo.create(pos, null, selectedPhotos);
                ARouter.getInstance()
                        .build(RouterList.PhotoMultiBrowserActivity.path)
                        .withParcelable(RouterList.PhotoMultiBrowserActivity.key_browserinfo, info)
                        .withInt(RouterList.PhotoMultiBrowserActivity.key_maxSelectCount, selectedPhotos.size())
                        .navigation(PublishActivity.this, RouterList.PhotoMultiBrowserActivity.requestCode);
            }
        });
        mPreviewImageView.setOnAddPhotoClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectPhotoPopup();
            }
        });
    }

    private void loadImage() {
        mPreviewImageView.setDatas(selectedPhotos, new PreviewImageView.OnLoadPhotoListener<ImageInfo>() {
            @Override
            public void onPhotoLoading(int pos, ImageInfo data, @NonNull ImageView imageView) {
                KLog.i(data.getImagePath());
                ImageLoadMnanger.INSTANCE.loadImage(imageView, data.getImagePath());
            }
        });
    }

    private void showSelectPhotoPopup() {
        if (mSelectPhotoMenuPopup == null) {
            mSelectPhotoMenuPopup = new SelectPhotoMenuPopup(this);
            mSelectPhotoMenuPopup.setOnSelectPhotoMenuClickListener(new SelectPhotoMenuPopup.OnSelectPhotoMenuClickListener() {
                @Override
                public void onShootClick() {
                    PhotoHelper.fromCamera(PublishActivity.this, false);
                }

                @Override
                public void onAlbumClick() {
                    ARouter.getInstance()
                            .build(RouterList.PhotoSelectActivity.path)
                            .withInt(RouterList.PhotoSelectActivity.key_maxSelectCount, mPreviewImageView.getRestPhotoCount())
                            .navigation(PublishActivity.this, RouterList.PhotoSelectActivity.requestCode);
                }
            });
        }
        mSelectPhotoMenuPopup.showPopupWindow();
    }

    //title init
    private void initTitle() {
        setTitle(mode == RouterList.PublishActivity.MODE_TEXT ? "发表文字" : null);
        setTitleRightTextColor(mode != RouterList.PublishActivity.MODE_TEXT);
        setTitleMode(TitleBar.MODE_BOTH);
        setTitleLeftText("取消");
        setTitleLeftIcon(0);
        setTitleRightText("发送");
        setTitleRightIcon(0);
    }

    private void setTitleRightTextColor(boolean canClick) {
        setRightTextColor(canClick ? UIHelper.getResourceColor(R.color.wechat_green_bg) : UIHelper.getResourceColor(R.color.wechat_green_transparent));
        canTitleRightClick = canClick;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PhotoHelper.handleActivityResult(this, requestCode, resultCode, data, new PhotoHelper.PhotoCallback() {
            @Override
            public void onFinish(String filePath) {
                mPreviewImageView.addData(new ImageInfo(filePath, null, null, 0, 0));
                refreshTitleRightClickable();
            }

            @Override
            public void onError(String msg) {
                UIHelper.ToastMessage(msg);
            }
        });
        if (requestCode == RouterList.PhotoSelectActivity.requestCode && resultCode == RESULT_OK) {
            List<ImageInfo> result = data.getParcelableArrayListExtra(RouterList.PhotoSelectActivity.key_result);
            if (result != null) {
                mPreviewImageView.addData(result);
            }
            refreshTitleRightClickable();
        }
    }

    @Override
    public void onTitleRightClick() {
        if (!canTitleRightClick) return;

        // 发布按钮只能点击一次
        canTitleRightClick = false;
        publish();
    }

    private void refreshTitleRightClickable() {
        String inputContent = mInputContent.getText().toString();
        switch (mode) {
            case RouterList.PublishActivity.MODE_MULTI:
                setTitleRightTextColor(!ToolUtil.isListEmpty(mPreviewImageView.getDatas()) && StringUtil.noEmpty(inputContent));
                break;
            case RouterList.PublishActivity.MODE_TEXT:
                setTitleRightTextColor(StringUtil.noEmpty(inputContent));
                break;
        }

    }

    @Override
    public void finish() {
        super.finish();
        if (mPopupProgress != null) {
            mPopupProgress.dismiss();
        }
        SwitchActivityTransitionUtil.transitionVerticalOnFinish(this);
    }

    private void publish() {
        UIHelper.hideInputMethod(mInputContent);
        List<ImageInfo> datas = mPreviewImageView.getDatas();
        final boolean hasImage = !ToolUtil.isListEmpty(datas);
        final String inputContent = mInputContent.getText().toString();
        if (mPopupProgress == null) {
            mPopupProgress = new PopupProgress(this);
        }

        final String[] uploadTaskPaths;
        if (hasImage) {
            uploadTaskPaths = new String[datas.size()];
            for (int i = 0; i < datas.size(); i++) {
                uploadTaskPaths[i] = datas.get(i).getImagePath();
            }
            doCompress(uploadTaskPaths, new OnCompressListener.OnCompressListenerAdapter() {
                @Override
                public void onSuccess(List<String> imagePath) {
                    if (!ToolUtil.isListEmpty(imagePath)) {
                        for (int i = 0; i < imagePath.size(); i++) {
                            uploadTaskPaths[i] = imagePath.get(i);
                        }
                        doUpload(uploadTaskPaths, inputContent);
                    } else {
                        publishInternal(inputContent, null);
                    }
                }
            });
        } else {
            publishInternal(inputContent, null);
        }
    }

    private void doCompress(String[] uploadPaths, final OnCompressListener.OnCompressListenerAdapter listener) {
        CompressManager compressManager = CompressManager.create(this);
        for (String uploadPath : uploadPaths) {
            compressManager.addTask().setOriginalImagePath(uploadPath);
        }
        mPopupProgress.showPopupWindow();
        compressManager.start(new OnCompressListener.OnCompressListenerAdapter() {
            @Override
            public void onSuccess(List<String> imagePath) {
                if (listener != null) {
                    listener.onSuccess(imagePath);
                }
                mPopupProgress.dismiss();
            }

            @Override
            public void onCompress(long current, long target) {
                float progress = (float) current / target;
                mPopupProgress.setProgressTips("正在压缩第" + current + "/" + target + "张图片");
                mPopupProgress.setProgress((int) (progress * 100));
            }

            @Override
            public void onError(String tag) {
                mPopupProgress.dismiss();
                UIHelper.ToastMessage(tag);
            }
        });
    }

    private void doUpload(final String[] uploadTaskPaths, final String inputContent) {
        uploadedCount = 0;
        String uploadUrl = NetConstant.URL_BASE_HTTPS + NetConstant.RESOURCE_UPLOAD;
        final List<String> uploadedList = new ArrayList<String>();
        if (uploadTaskPaths != null && uploadTaskPaths.length > 0) {
            for (int i = 0; i < uploadTaskPaths.length; i++) {
                //MultipartBody多功能的请求实体对象,,,formBody只能传表单形式的数据
                MultipartBody.Builder builder = new MultipartBody.Builder();
                builder.setType(MultipartBody.FORM);
                builder.addFormDataPart("creater", userId + "");

                File currentFile = new File(uploadTaskPaths[i]);
                builder.addFormDataPart("upfile", currentFile.getName(),
                        RequestBody.create(MediaType.parse("application/octet-stream"), currentFile));

                //构建
                MultipartBody multipartBody = builder.build();

                //创建Request
                Request request = new Request.Builder().url(uploadUrl).post(multipartBody).build();

                //得到Call
                Call call = httpClient.newCall(request);
                //执行请求
                Callback uploadCallback = new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == 200) {
                            String uploadResponse = response.body().string();
                            JsonObject jsonObject = gson.fromJson(uploadResponse, JsonObject.class);
                            JsonElement jsonElement = jsonObject.get("url");
                            String img = jsonElement.getAsString();

                            if (!TextUtils.isEmpty(img)) {
                                uploadedList.add(img);
                                uploadedCount++;

                                //1、curIndex--表示当前第几个文件正在上传
                                //2、curPercent--表示当前上传文件的进度值（百分比）
                                //3、total--表示总的上传文件数
                                //4、totalPercent--表示总的上传进度（百分比）

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mPopupProgress.setProgressTips("正在上传第"
                                                + uploadedCount + "/" + uploadTaskPaths.length + "张图片");

                                        int totalPercent = uploadedCount * 100 / uploadTaskPaths.length;
                                        mPopupProgress.setProgress(totalPercent);
                                        if (!mPopupProgress.isShowing()) {
                                            mPopupProgress.showPopupWindow();
                                        }

                                        if (!ToolUtil.isListEmpty(uploadedList) && uploadedList.size() == uploadTaskPaths.length) {
                                            publishInternal(inputContent, uploadedList);
                                        }
                                    }
                                });

                            }
                        } else if (response.code()==413 && "Request Entity Too Large".equals(response.message())) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(PublishActivity.this, "图片太大了，您可以选择别的图片"
                                            , Toast.LENGTH_SHORT)
                                    .show();
                                }
                            });
                        }
                    }
                };
                call.enqueue(uploadCallback);
            }
        }
    }

    private void publishInternal(String input, List<String> uploadPicPaths) {
        mPopupProgress.setProgressTips("正在发布");
        if (!mPopupProgress.isShowing()) {
            mPopupProgress.showPopupWindow();
        }


        AddMomentsRequest addMomentsRequest = new AddMomentsRequest();
        addMomentsRequest.setAuthId(LocalHostManager.INSTANCE.getUserid())
                .setToken(token)
                .addText(input);
        if (!ToolUtil.isListEmpty(uploadPicPaths)) {
            for (String uploadPicPath : uploadPicPaths) {
                addMomentsRequest.addPicture(uploadPicPath);
            }
        }
        addMomentsRequest.setOnResponseListener(new OnResponseListener.SimpleResponseListener<String>() {
            @Override
            public void onSuccess(String response, int requestType) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPopupProgress.dismiss();
                        UIHelper.ToastMessage("发布成功");
                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }

            @Override
            public void onError(AppjsException e, int requestType) {
                UIHelper.ToastMessage(e.toString());
            }
        });
        addMomentsRequest.execute();
    }
}
