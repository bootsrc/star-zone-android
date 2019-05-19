package razerdp.github.com.lib.network.ssl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class MySslUtil {
    public static final boolean SSL_ENABLED = true;

    public static OkHttpClient newOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(3, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES);
        HttpsModel httpsModel = newSSLSocketFactory();

        if (SSL_ENABLED && httpsModel != null && httpsModel.getSslSocketFactory() != null
                && httpsModel.getX509TrustManager() != null) {
            SSLSocketFactory sslSocketFactory = httpsModel.getSslSocketFactory();
            X509TrustManager trustManager = httpsModel.getX509TrustManager();
            builder.sslSocketFactory(sslSocketFactory, trustManager);
        }
        return builder.build();
    }

    private static HttpsModel newSSLSocketFactory() {
        // 添加证书
        List<InputStream> certificates = new ArrayList<>();
        List<byte[]> certs_data = NetConfig.getCertificatesData();
        // 将字节数组转为数组输入流
        if (certs_data != null && !certs_data.isEmpty()) {
            for (byte[] bytes : certs_data) {
                certificates.add(new ByteArrayInputStream(bytes));
            }
        }
        return getSocketFactory(certificates);
    }

    /**
     * 添加证书
     *
     * @param certificates
     */
    private static HttpsModel getSocketFactory(List<InputStream> certificates) {
        HttpsModel httpsModel = new HttpsModel();
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            try {
                for (int i = 0, size = certificates.size(); i < size; ) {
                    InputStream certificate = certificates.get(i);
                    String certificateAlias = Integer.toString(i++);
                    keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
                    if (certificate != null)
                        certificate.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init(null,
                    trustManagerFactory.getTrustManagers(),
                    new SecureRandom()
            );

            X509TrustManager trustManager = (X509TrustManager) trustManagerFactory.getTrustManagers()[0];
            httpsModel.setX509TrustManager(trustManager);
            httpsModel.setSslSocketFactory(sslContext.getSocketFactory());
            return httpsModel;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
