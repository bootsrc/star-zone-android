package razerdp.github.com.lib.network.ssl;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class NetConfig {
    private static final String MY_TAG = "NetConfig";

    // 证书数据
    private static List<byte[]> CERTIFICATES_DATA = new ArrayList<>();

    /**
     * 添加https证书
     *
     * @param inputStream inputStream
     */
    public synchronized static void addCertificate(InputStream inputStream) {
        Log.i(MY_TAG, "#addCertificate inputStream = " + inputStream);

        if (inputStream != null) {
            try {
                int ava = 0;// 数据当次可读长度
                int len = 0;// 数据总长度
                ArrayList<byte[]> data = new ArrayList<>();
                while ((ava = inputStream.available()) > 0) {
                    byte[] buffer = new byte[ava];
                    inputStream.read(buffer);
                    data.add(buffer);
                    len += ava;
                }

                byte[] buff = new byte[len];
                int dstPos = 0;
                for (byte[] bytes:data) {
                    int length = bytes.length;
                    System.arraycopy(bytes, 0, buff, dstPos, length);
                    dstPos += length;
                }

                CERTIFICATES_DATA.add(buff);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * https证书
     * @return
     */
    public static List<byte[]> getCertificatesData() {
        return CERTIFICATES_DATA;
    }
}
