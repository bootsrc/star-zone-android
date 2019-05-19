package com.appjishu.starzone.singleton;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

public class GlideRequestOptionsSingleton {
    private static final GlideRequestOptionsSingleton ourInstance = new GlideRequestOptionsSingleton();

    public static GlideRequestOptionsSingleton getInstance() {
        return ourInstance;
    }

    private GlideRequestOptionsSingleton() {
        requestOptions = RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE);
    }

    private RequestOptions requestOptions;

    public RequestOptions getRequestOptions() {
        return requestOptions;
    }
}
