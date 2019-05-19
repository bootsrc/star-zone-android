package com.appjishu.starzone.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.launcher.ARouter;
import com.appjishu.starzone.R;
import com.appjishu.starzone.constant.StarSignConstant;
import com.appjishu.starzone.constant.UserProfileConstant;
import com.appjishu.starzone.model.LoginResult;
import com.appjishu.starzone.model.UserProfile;
import com.appjishu.starzone.singleton.GsonSingleton;
import com.appjishu.starzone.util.MyFileUtil;
import com.appjishu.starzone.util.SharedPreferencesMyUtil;
import com.appjishu.starzone.util.ToastUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.razerdp.github.com.common.constant.NetConstant;
import com.razerdp.github.com.common.constant.PermissionConstant;
import com.razerdp.github.com.common.router.RouterList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import razerdp.github.com.lib.common.entity.ImageInfo;
import razerdp.github.com.lib.manager.compress.CompressManager;
import razerdp.github.com.lib.manager.compress.OnCompressListener;
import razerdp.github.com.lib.network.ssl.MySslUtil;
import razerdp.github.com.lib.utils.ToolUtil;
import razerdp.github.com.ui.base.BaseTitleBarActivity;
import razerdp.github.com.ui.helper.PhotoHelper;
import razerdp.github.com.ui.util.UIHelper;
import razerdp.github.com.ui.widget.common.TitleBar;
import razerdp.github.com.ui.widget.popup.PopupProgress;
import razerdp.github.com.ui.widget.popup.SelectPhotoMenuPopup;

public class AddProfileActivity extends BaseTitleBarActivity implements View.OnClickListener {

    private static final String MY_TAG = "AddProfActivity";

    private long userId;
    private String token;

    private static final Gson gson = new Gson();
    private int sex = 2;
    private int starSign = 0;
    private int age = 18;
    private UserProfile userProfile;

    private RadioGroup sexRedioGroup;
    private RadioButton maleRadioButton;
    private RadioButton femaleRadioButton;
    private Button submitBtn;
    private Spinner starSpinner;
    private EditText ageEditer;
    private EditText nicknameEditer;
    private TextView phoneView;

    private OkHttpClient httpClient;
    private String headImg;

    /**
     * 裁剪完的头像的图片的本地路径
     */
    private String picturePath;
    private boolean needUpdateImageCache = false;
    private static final String MALE = "男";
    private SelectPhotoMenuPopup mSelectPhotoMenuPopup;
    private ProgressWheel mProgressWheel;
    private static final int MSG_WHAT_SHOW_NETWORK_IMAGE = 0x01;
    private MyHandler myHandler;
    private PopupProgress mPopupProgress;

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getStarSign() {
        return starSign;
    }

    public void setStarSign(int starSign) {
        this.starSign = starSign;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile);
        requestWritePermission();

        initView();
        initEvent();
        // 进入界面时隐藏软键盘
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUI();
    }

    @Override
    public void onHandleIntent(Intent intent) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PhotoHelper.handleActivityResult(this, requestCode, resultCode, data, new PhotoHelper.PhotoCallback() {
            @Override
            public void onFinish(String filePath) {
                // filePath是拍照后保存的图片的文件的绝对路径
                if (!TextUtils.isEmpty(filePath)) {
                    picturePath = filePath;
                    showSelectedImg();
                }
            }

            @Override
            public void onError(String msg) {
                UIHelper.ToastMessage(msg);
            }
        });
        if (requestCode == RouterList.PhotoSelectActivity.requestCode && resultCode == RESULT_OK) {
            List<ImageInfo> result = data.getParcelableArrayListExtra(RouterList.PhotoSelectActivity.key_result);
            if (result != null && result.size() > 0) {
                ImageInfo selectedImageInfo = result.get(0);
                picturePath = selectedImageInfo.getImagePath();
                showSelectedImg();
            }
        }
    }

    @Override
    public void onTitleRightClick() {
        finish();
    }

    private ScrollView profileScrollView;
    private ImageView mImageView;

    private void initView() {//必须调用
        httpClient = MySslUtil.newOkHttpClient();
        myHandler = new MyHandler();

        setTitle(getString(R.string.add_profile_activity_title));
        setTitleMode(TitleBar.MODE_BOTH);
        setTitleLeftIcon(R.drawable.ic_arrow_back_white_24dp);
        setTitleRightIcon(R.drawable.ic_close_white_24dp);

        mProgressWheel = findViewById(R.id.progress_wheel_add_profile);
        mProgressWheel.stopSpinning();
        profileScrollView = findViewById(R.id.profile_scroll_view);
        mImageView = findViewById(R.id.profile_my_head_image_view);
        nicknameEditer = findViewById(R.id.nickname_editor);
        phoneView = findViewById(R.id.phone_field);
        initSex();
        initStarSignSpinner();
        ageEditer = findViewById(R.id.age_editor);
        String ageStr = "20";
        ageEditer.setText(ageStr);
        initSubmitBtn();

        if (mPopupProgress == null) {
            mPopupProgress = new PopupProgress(getActivity());
        }
        refreshUI();
    }

    private void initEvent() {//必须调用
        mImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_my_head_image_view:
                showSelectPhotoPopup();
                break;
            default:
        }
    }

    private void showSelectPhotoPopup() {
        if (mSelectPhotoMenuPopup == null) {
            mSelectPhotoMenuPopup = new SelectPhotoMenuPopup(this);
            mSelectPhotoMenuPopup.setOnSelectPhotoMenuClickListener(new SelectPhotoMenuPopup.OnSelectPhotoMenuClickListener() {
                @Override
                public void onShootClick() {
                    PhotoHelper.fromCamera(AddProfileActivity.this, false);
                }

                @Override
                public void onAlbumClick() {
                    ARouter.getInstance()
                            .build(RouterList.PhotoSelectActivity.path)
                            .withInt(RouterList.PhotoSelectActivity.key_maxSelectCount, 1)
                            .navigation(AddProfileActivity.this, RouterList.PhotoSelectActivity.requestCode);
                }
            });
        }
        mSelectPhotoMenuPopup.showPopupWindow();
    }

    private void refreshUI() {
        checkLoginStatus();
        if (userId > 0 && !TextUtils.isEmpty(token)) {
            doGetProfile(userId, token);
        }
    }

    private void checkLoginStatus() {
        LoginResult loginResult = SharedPreferencesMyUtil.queryToken(this);
        if (loginResult == null || loginResult.getUserId() < 1
                || TextUtils.isEmpty(loginResult.getToken())) {
            Intent intentObj = new Intent(this, LoginActivity.class);
            startActivity(intentObj);
        } else {
            userId = loginResult.getUserId();
            token = loginResult.getToken();
        }
    }

    private void initStarSignSpinner() {
        starSpinner = findViewById(R.id.star_spinner);

        List<String> list = new ArrayList<String>();

        for (int i = 0; i < StarSignConstant.arrText.length; i++) {
            list.add(StarSignConstant.arrText[i]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

        starSpinner.setAdapter(adapter);
        starSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // parent： 为控件Spinner view：显示文字的TextView position：下拉选项的位置从0开始
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                starSign = position;
                //获取Spinner控件的适配器
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) parent.getAdapter();
                String selectedStr = adapter.getItem(position);
                String selectLog = "Select: " + position + ", str=" + selectedStr;
//                Toast.makeText(AddProfileActivity.this, selectLog, Toast.LENGTH_SHORT).show();
            }

            //没有选中时的处理
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initSex() {
        sexRedioGroup = findViewById(R.id.profile_sex_radiogroup);
        maleRadioButton = findViewById(R.id.profile_male_rb);
        femaleRadioButton = findViewById(R.id.profile_female_rb);

        sexRedioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup rg, int checkedId) {
                if (checkedId == maleRadioButton.getId()) {
                    sex = UserProfileConstant.SEX_MALE;
                } else if (checkedId == femaleRadioButton.getId()) {
                    sex = UserProfileConstant.SEX_FEMALE;
                } else {
                    sex = UserProfileConstant.SEX_UNSELECTED;
                }
            }
        });
    }

    private void initSubmitBtn() {
        submitBtn = findViewById(R.id.profile_add_btn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                needUpdateImageCache = false;
                attempSubmitProfile();
            }
        });
    }

    private void attempSubmitProfile() {
        String str = "Sex is " + sex + ", starSign=" + starSign;
        LoginResult loginResult = SharedPreferencesMyUtil.queryToken(AddProfileActivity.this);

        long userId = loginResult.getUserId();
        String token = loginResult.getToken();
        String mobile = loginResult.getUsername();
        String ageStrValue = ageEditer.getText().toString();
        int currentAge = Integer.valueOf(ageStrValue);
        AddProfileActivity.this.setAge(currentAge);
        String nicknameStr = nicknameEditer.getText().toString();

        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(userId);
        userProfile.setMobile(mobile);
        userProfile.setAge(currentAge);
        userProfile.setSex(sex);
        userProfile.setStarSign(starSign);
        userProfile.setNickname(nicknameStr);
        userProfile.setHeadImg(AddProfileActivity.this.headImg);
        userProfile.setHeadVersion(0);

        String nicknameFieldValue = nicknameEditer.getText().toString();
        if (TextUtils.isEmpty(nicknameFieldValue)) {
            ToastUtil.showToast(AddProfileActivity.this, "昵称不能为空");
            return;
        }
        if (TextUtils.isEmpty(headImg)) {
            ToastUtil.showToast(AddProfileActivity.this, "需要上传头像");
            return;
        }

        String userProfileStr = gson.toJson(userProfile);
        if (sex == UserProfileConstant.SEX_UNSELECTED) {
            ToastUtil.showToast(AddProfileActivity.this, "请选择性别");
        } else {
            doSubmitProfile(userId, token, userProfileStr);
        }
    }


    private void doGetProfile(long userId, String token) {
        mProgressWheel.spin();

        FormBody formBody = new FormBody
                .Builder()
                .build();
        final String url = NetConstant.URL_BASE_HTTPS + NetConstant.GET_PROFILE;
        final Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .addHeader("userId", userId + "")
                .addHeader("token", token)
                .build();
        Call call = httpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressWheel.stopSpinning();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseStr = response.body().string();
                UserProfile userProfile = gson.fromJson(responseStr, UserProfile.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressWheel.stopSpinning();
                    }
                });

                if (userProfile == null || userProfile.getUserId() == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            nicknameEditer.setText("");
                            maleRadioButton.setChecked(true);
                            femaleRadioButton.setChecked(false);
                            String defaultAge = "18";
                            ageEditer.setText(defaultAge);
                            starSpinner.setSelection(0);
                        }
                    });
                    return;
                }
                AddProfileActivity.this.setUserProfile(userProfile);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//
//                            //// 检查是不是最新的头像----start
//                            long latestVersion = thisUserProfile.getHeadVersion();
//                            String latestHeadImg = thisUserProfile.getHeadImg();
//                            if (!TextUtils.isEmpty(latestHeadImg)) {
//                                // 把服务器上最新的图片显示到界面上
//                                String imgFullUrl = NetConstant.RESOURCES_BASE + latestHeadImg;
//                                Glide.with(AddProfileActivity.this).asBitmap().load(imgFullUrl).into(new SimpleTarget<Bitmap>() {
//                                    @Override
//                                    public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
//                                        mImageView.setImageBitmap(CommonUtil.toRoundCorner(bitmap, bitmap.getWidth() / 2));
//                                    }
//                                });
//                            }
//                            //// 检查是不是最新的头像----end
                        UserProfile thisUserProfile = AddProfileActivity.this.getUserProfile();
                        int resultSex = thisUserProfile.getSex();
                        int resultAge = thisUserProfile.getAge();
                        String resultAgeStr = resultAge + "";
                        int resultStarSign = thisUserProfile.getStarSign();
                        String nicknameResuslt = thisUserProfile.getNickname();
                        nicknameEditer.setText(nicknameResuslt);
                        switch (resultSex) {
                            case UserProfileConstant.SEX_UNSELECTED:
                                break;
                            case UserProfileConstant.SEX_MALE:
                                maleRadioButton.setChecked(true);
                                femaleRadioButton.setChecked(false);
                                break;
                            case UserProfileConstant.SEX_FEMALE:
                                maleRadioButton.setChecked(false);
                                femaleRadioButton.setChecked(true);
                        }
                        sex = resultSex;

                        ageEditer.setText(resultAgeStr);
                        if (resultStarSign > -1) {
                            starSpinner.setSelection(resultStarSign);
                        }
                        phoneView.setText(thisUserProfile.getMobile());
                    }
                });

                headImg = userProfile.getHeadImg();
                long latestVersion = userProfile.getHeadVersion();
                String latestHeadImg = userProfile.getHeadImg();
                if (!TextUtils.isEmpty(latestHeadImg)) {
                    // 把服务器上最新的图片显示到界面上
                    String imgFullUrl = NetConstant.RESOURCES_BASE + latestHeadImg;
                    showNetworkImage(imgFullUrl);
                }
            }
        });
    }

    private void doSubmitProfile(long userId, String token, String userProfile) {
        mProgressWheel.spin();

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(2, TimeUnit.MINUTES)
                .build();

        FormBody formBody = new FormBody
                .Builder()
                .add("userProfile", userProfile)
                .build();
        final String url = NetConstant.URL_BASE_HTTPS + NetConstant.SAVE_PROFILE;
        final Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .addHeader("userId", userId + "")
                .addHeader("token", token)
                .build();
        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressWheel.stopSpinning();
                        ToastUtil.showToast(AddProfileActivity.this, "提交失败");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressWheel.stopSpinning();
                    }
                });
                final String responseStr = response.body().string();
                Log.i("AddProfileHttp", responseStr);

                if (needUpdateImageCache && !TextUtils.isEmpty(AddProfileActivity.this.picturePath)) {
                    if (!TextUtils.isEmpty(responseStr)) {
                        UserProfile resultProfile = GsonSingleton.getInstance().getGson().fromJson(responseStr, UserProfile.class);
                        long resultHeadVersion = resultProfile.getHeadVersion();
                        SharedPreferencesMyUtil.storeHead(AddProfileActivity.this, AddProfileActivity.this.picturePath);
                        if (resultHeadVersion > 0) {
                            SharedPreferencesMyUtil.storeHeadVersion(AddProfileActivity.this, resultHeadVersion);
                        }
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast(AddProfileActivity.this, "更新资料成功");
                        Intent myIntent = new Intent(AddProfileActivity.this, MainActivity.class);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        AddProfileActivity.this.startActivity(myIntent);
                    }
                });
            }
        });
    }

    private void uploadImage(String compressedFilePath) {
        String uploadUrl = NetConstant.URL_BASE_HTTPS + NetConstant.RESOURCE_UPLOAD;
        //MultipartBody多功能的请求实体对象,,,formBody只能传表单形式的数据
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        //参数
        builder.addFormDataPart("creater", userId + "");

        //文件...参数name指的是请求路径中所接受的参数...如果路径接收参数键值是fileeeee,
//此处应该改变
        File currentFile = new File(compressedFilePath);
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
                String uploadResponse = response.body().string();
                JsonObject jsonObject = GsonSingleton.getInstance().getGson().fromJson(
                        uploadResponse, JsonObject.class);

                if (jsonObject != null) {
                    String urlData = jsonObject.get("url").getAsString();
                    if (!TextUtils.isEmpty(urlData)) {
                        headImg = urlData;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddProfileActivity.this, "上传成功", Toast.LENGTH_SHORT)
                                        .show();
                                needUpdateImageCache = true;
                                attempSubmitProfile();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddProfileActivity.this, "上传失败", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
                    }
                }
            }
        };

        call.enqueue(uploadCallback);
    }

    /// 发图片
//    private boolean isHeadImgValid() {
//        if (!TextUtils.isEmpty(headImg)) {
//            if (headImg.startsWith("/star-sign/")
//                    || headImg.startsWith("/star-sign-file/mobile/default-img/")) {
//                return true;
//            }
//        }
//        return false;
//    }

    private void showSelectedImg() {
        if (MyFileUtil.isFilePath(picturePath) == false) {
            Log.e(MY_TAG, "setPicture  StringUtil.isFilePath(path) == false >> showShortToast(找不到图片);return;");
            ToastUtil.showToast(this, "找不到图片");
            return;
        }
        String logContent = "---picturePath=" + this.picturePath;
        Log.i(MY_TAG, logContent);

        compressAndUpload();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && grantResults.length > 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
                boolean b = shouldShowRequestPermissionRationale(permissions[0]);
                if (!b) {
                    // 用户还是想用我的 APP 的
                    // 提示用户去应用设置界面手动开启权限
//                        showDialogTipUserGoToAppSettting();
                    Toast.makeText(this, "请到系统设置里去增加权限,否则无法上传照片", Toast.LENGTH_SHORT).show();
                } else {

                }
            } else {
                Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestWritePermission() {
        if (Build.VERSION.SDK_INT > 22) {
            List<String> needGrantList = new ArrayList<String>();
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                needGrantList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                needGrantList.add(Manifest.permission.CAMERA);
            }
            if (needGrantList != null && needGrantList.size() > 0) {
                String[] needGrant = needGrantList.toArray(new String[0]);
                ActivityCompat.requestPermissions(getActivity(), needGrant, PermissionConstant.OWN_PERMISSION);
            }
        }
    }

    /**
     * 子线程操作,发送消息到Handler
     *
     * @param url
     */
    private void showNetworkImage(String url) {
        Message msg = Message.obtain();
        msg.what = MSG_WHAT_SHOW_NETWORK_IMAGE;
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        msg.setData(bundle);
        myHandler.sendMessage(msg);
    }

    private void compressAndUpload() {
        doCompress(this.picturePath, new OnCompressListener.OnCompressListenerAdapter() {
            @Override
            public void onSuccess(List<String> imagePath) {
                if (!ToolUtil.isListEmpty(imagePath)) {
                    String compressedFilePath = imagePath.get(0);
                    Glide.with(AddProfileActivity.this).load(new File(compressedFilePath)).into(mImageView);
                    uploadImage(compressedFilePath);
                }
            }
        });
    }

    private void doCompress(String uploadPath, final OnCompressListener.OnCompressListenerAdapter listener) {
        CompressManager compressManager = CompressManager.create(this);
        compressManager.addTask(CompressManager.TYPE_HEAD).setOriginalImagePath(uploadPath);
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

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_SHOW_NETWORK_IMAGE:
                    String url = msg.getData().getString("url");
                    RequestOptions requestOptions = RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE)
                            .override(150, 150)
                            .placeholder(R.mipmap.ic_launcher);

                    Glide.with(getActivity()).asBitmap().load(url)
                            .apply(requestOptions)
                            .thumbnail(0.5F)
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
//                            mImageView.setImageBitmap(CommonUtil.toRoundCorner(bitmap, 100));
                                    mImageView.setImageBitmap(bitmap);
                                }
                            });
                    break;
            }
        }
    }

    /**
     * 如何后台的LoginInterceptor验证失败，Android端就重定向到LoginActivity
     *
     * @param responseStr
     * @return
     */
    private boolean confirmPassport(String responseStr) {
        if (TextUtils.isEmpty(responseStr) || "null".equals(responseStr.toLowerCase())) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "请上传头像并完善资料", Toast.LENGTH_SHORT)
                            .show();
                    Intent intentObj = new Intent(getActivity(), AddProfileActivity.class);
                    startActivity(intentObj);
                }
            });
            return false;
        }

        LoginResult loginResultData = GsonSingleton.getInstance().getGson().fromJson(responseStr, LoginResult.class);
        if (loginResultData == null || loginResultData.getCode() == 0) {
            // 当userId+token在后台验证成功的时候，后台返回的是UserProfile的对象的json string
            // 如果一定要把这个string反序列化到LoginResult，就是一个空对象。即除了userId > 0 外,其它的字段都是
            // 默认空值, 这时候code=0,    token==codeDesc==username==""
            return true;
        }
        if (loginResultData != null && loginResultData.getCode() != 0) {
            // 这时候是后台验证时候，需要跳转到LoginActivity
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intentObj = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intentObj);
                }
            });
            return false;
        }
        return true;
    }
}
