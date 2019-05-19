package razerdp.github.com.lib.network.base;

import razerdp.github.com.lib.base.AppjsException;

/**
 * Created by liushaoming on 2016/10/27.
 */

public interface OnResponseListener<T> {
    void onStart(int requestType);

    void onSuccess(T response, int requestType);

    void onError(AppjsException e, int requestType);

    abstract class SimpleResponseListener<T> implements OnResponseListener<T> {

        @Override
        public void onStart(int requestType) {

        }


        @Override
        public void onError(AppjsException e, int requestType) {

        }
    }
}
