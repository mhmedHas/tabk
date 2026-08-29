package com.idata.serialport;

import java.io.FileDescriptor;

public class SerialPortControl {

    static {
        try {
            System.loadLibrary("iDataSerialPortJni");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SerialPortControl getInstance() {
        return MySingleton.singleton;
    }

    private static class MySingleton {
        final static SerialPortControl singleton = new SerialPortControl();
    }

    private SerialPortControl() {
    }

    //鎸囦护鎺ュ彛锛岀敤浜庢帶鍒舵ā鍧椾笂涓嬪崟
    public static native boolean ioControl(String powerName, int io);

    public native FileDescriptor openUart(String uartName, int nSpeed, int nBits, char nEvent, int nStop);

    //鍏抽棴涓插彛
    public native boolean closeUart();

    public FileDescriptor mFd;


    /**
     * 鎵撳紑涓插彛锛岃幏鍙朏ileDescriptor鐢ㄤ簬璇诲啓娴佹搷浣?
     *
     * @param uartName 涓插彛鑺傜偣鍚?
     * @param nSpeed   娉㈢壒鐜囷紙nSpeed锛夋敮鎸? 1200锛?400锛?600锛?9200锛?15200
     * @param nBits    鏁版嵁浣嶅浐瀹氫负8
     * @param nEvent   鏍￠獙浣嶅浐瀹氫负瀛楃n
     * @param nStop    鍋滄浣嶅浐瀹氫负1
     * @return 杩斿洖鐢ㄤ簬IO鎿嶄綔鐨勬枃浠舵弿杩扮
     */
    public FileDescriptor getMFD(String uartName, int nSpeed, int nBits, char nEvent, int nStop) {
        return mFd = openUart(uartName, nSpeed, nBits, nEvent, nStop);
    }
}
