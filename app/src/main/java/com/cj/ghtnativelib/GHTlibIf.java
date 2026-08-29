package com.cj.ghtnativelib;

/**
 * android 设备gpio驱动
 */
public class GHTlibIf {
    public static  int LED_RED=44;
    public static  int LED_GREEN=43;
    public static  int LED_BLUE=25;
    public static  int LED_PINK=128;
    public static  int BZ=12;
    public native int InitLib();
    public native int PowerControl(int value);
    public native int GetConnectedAnts(int[] ant,int[] antcnt);
    public native int Switch(int antid);
    public native int GPIOSet(int num,int val);
    public native int GPIOGet(int num);
    public native int GpIOExport(int num);
    static {
        System.loadLibrary("GHTNativeLibJni");
    }
}
