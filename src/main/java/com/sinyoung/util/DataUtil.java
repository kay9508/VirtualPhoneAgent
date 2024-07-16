package com.sinyoung.util;

import java.util.Date;

public class DataUtil {

    /**
     *
     * @param from
     * @param end
     * @return
     */
    public static long getDiffTime(Date from, Date end) {
        long diff = end.getTime() - from.getTime();
        return diff/1000;
    }

    public static byte[] getIntTo2Byte(int val) {
        byte[] res = new byte[2];
        res[0] = (byte) (val >> 8);
        res[1] = (byte) val;
        return res;
    }

    public static byte[] getIntTo2Byte(int val, boolean isConvert) {
        byte[] res = getIntTo2Byte(val);
        if(isConvert) {
            convertEndian(res);
        }
        return res;
    }

    public static int get2ByteToInt(byte[] val) {
        int res = ((val[0] & 0xff) << 8) + (val[1] & 0xff);
        return res;
    }

    public static void convertEndian(byte[] stream) {
        byte b = 0;
        int j = stream.length / 2;
        int len = stream.length - 1;
        for (int i = 0; i < j; i++) {
            b = stream[i];
            stream[i] = stream[len - i];
            stream[len - i] = b;
        }
    }

    /*public static void arraycopy(IoBuffer src, int spos, byte[] dest, int dpos, int dlen) {
        for (int i = dpos; i < dlen; i++) {
            dest[i] = src.get(spos++);
        }
    }*/
}
