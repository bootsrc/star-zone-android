package com.appjishu.starzone.util;

public class MyFileUtil {

    /**判断字符类型是否是路径
     * @param path
     * @return
     */
    public static boolean isFilePath(String path) {
        if (isEmpty(path, true)) {
            return false;
        }

        if (! path.contains(".") || path.endsWith(".")) {
            return false;
        }

        return true;
    }

    /**判断字符是否为空
     * @param s
     * @param trim
     * @return
     */
    public static boolean isEmpty(String s, boolean trim) {
        //		Log.i(TAG, "isEmpty   s = " + s);
        if (s == null) {
            return true;
        }
        if (trim) {
            s = s.trim();
        }
        if (s.length() <= 0) {
            return true;
        }

        return false;
    }
}
