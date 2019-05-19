package razerdp.github.com.lib.network.ssl;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

public class HttpsModel {
    private SSLSocketFactory sslSocketFactory;
    private X509TrustManager x509TrustManager;

    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    public X509TrustManager getX509TrustManager() {
        return x509TrustManager;
    }

    public void setX509TrustManager(X509TrustManager x509TrustManager) {
        this.x509TrustManager = x509TrustManager;
    }
}
