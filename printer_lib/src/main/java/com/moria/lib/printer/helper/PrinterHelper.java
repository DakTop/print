package com.moria.lib.printer.helper;

import java.io.IOException;

/**
 * 打印帮助类
 *
 * @author moria
 * @date 2018/11/27
 */
public class PrinterHelper {

    /**
     * 获取str中长度为len的字节，并返回为字符串
     *
     * @param str
     * @param len
     * @return
     * @throws IOException
     */
    public static String getStrByByte(String str, int len) throws IOException {
        byte[] btf = str.getBytes("gbk");
        int count = 0;
        for (int j = len - 1; j >= 0; j--) {
            if (btf[j] < 0) {
                count++;
            } else {
                break;
            }
        }
        if (count % 2 == 0) {
            return new String(btf, 0, len, "gbk");
        } else {
            return new String(btf, 0, len - 1, "gbk");
        }
    }
}
