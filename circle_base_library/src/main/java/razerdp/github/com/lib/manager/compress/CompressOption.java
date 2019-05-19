package razerdp.github.com.lib.manager.compress;

import android.text.TextUtils;

import java.io.Serializable;
import java.lang.ref.WeakReference;

import razerdp.github.com.lib.helper.AppFileHelper;
import razerdp.github.com.lib.utils.EncryUtil;

import static razerdp.github.com.lib.manager.compress.CompressManager.BOTH;

/**
 * Created by liushaoming on 2018/1/9.
 * 压缩配置
 */
public class CompressOption implements Serializable {
    public static final int SIZE_TARGET_MOMENT = 80;   //100KB
    public static final int MAX_WIDTH_MOMENT = 270;
    public static final int MAX_HEIGHT_MOMENT = 480;

    public static final int SIZE_TARGET_HEAD = 20;
    public static final int MAX_WIDTH_HEAD = 128;
    public static final int MAX_HEIGHT_HEAD = 128;

    private WeakReference<CompressManager> mManagerWeakReference;
    @CompressManager.CompressType
    int compressType = BOTH;
//    int sizeTarget = 5 * 1024;//默认最大5M
//    int maxWidth = 720;//默认最大720p
//    int maxHeight = 1280;

    int sizeTarget = SIZE_TARGET_MOMENT;//默认最大512KB
    int maxWidth = MAX_WIDTH_MOMENT;//默认最大720p
    int maxHeight = MAX_HEIGHT_MOMENT;

    boolean autoRotate = true;

    @CompressManager.ImageType
    String suffix = CompressManager.JPG;
    String originalImagePath;
    String saveCompressImagePath;

    OnCompressListener mOnCompressListener;

    public CompressOption(CompressManager manager) {
        mManagerWeakReference = new WeakReference<CompressManager>(manager);
    }

    CompressManager getManager() {
        return mManagerWeakReference == null ? null : mManagerWeakReference.get();
    }


    public CompressOption setCompressType(@CompressManager.CompressType int compressType) {
        this.compressType = compressType;
        return this;
    }


    public CompressOption setSizeTarget(int sizeTarget) {
        this.sizeTarget = sizeTarget;
        return this;
    }

    public CompressOption setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public CompressOption setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    public CompressOption setAutoRotate(boolean autoRotate) {
        this.autoRotate = autoRotate;
        return this;
    }


    public CompressOption setOriginalImagePath(String originalImagePath) {
        this.originalImagePath = originalImagePath;
        if (TextUtils.isEmpty(saveCompressImagePath)) {
            saveCompressImagePath = AppFileHelper.getAppTempPath().concat(EncryUtil.MD5(originalImagePath) + suffix);
        }
        return this;
    }

    public CompressOption setSaveCompressImagePath(String saveCompressImagePath) {
        this.saveCompressImagePath = saveCompressImagePath;
        return this;
    }

    public CompressOption setOnCompressListener(OnCompressListener onCompressListener) {
        mOnCompressListener = onCompressListener;
        return this;
    }

    public CompressOption setSuffix(@CompressManager.ImageType String suffix) {
        this.suffix = suffix;
        return this;
    }

    public CompressOption addTask() {
        if (getManager() == null) {
            throw new NullPointerException("CompressManager must not be null");
        }
        return getManager().addTaskInternal(this);
    }

    public void start() {
        start(null);
    }

    public void start(OnCompressListener listener) {
        if (getManager() == null) {
            throw new NullPointerException("CompressManager must not be null");
        }
        getManager().start(listener);
    }

    public static CompressOption newHeadCompressOption(CompressManager manager) {
        CompressOption option =new CompressOption(manager);
        option.setSizeTarget(SIZE_TARGET_HEAD);
        option.setMaxWidth(MAX_WIDTH_HEAD);
        option.setMaxHeight(MAX_HEIGHT_HEAD);
        return option;
    }
}
