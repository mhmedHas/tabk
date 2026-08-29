package com.idata.rfid;
//other power class
public class SerialPortControl {
            static {
                try {
                    System.loadLibrary("serialportJni2");
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
    }

    public static native boolean controlIO(String powerName, int io);


}
