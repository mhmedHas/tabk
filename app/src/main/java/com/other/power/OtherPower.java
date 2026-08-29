package com.other.power;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import com.idata.serialport.SerialPortControl;
import com.jws.thirdlibrary.ICustomCallback;
import com.tools.dlog;
import com.znht.iodev2.PowerCtl;
//import android.app.*;
import com.pda.powercntrl.*;
import com.idata.rfid.*;
import  com.idata.serialport.*;
import com.cj.ghtnativelib.*;

import com.ohhmei.rfidpower.*;

import android.hardware.uhf.*;
import com.hc.pda.HcPowerCtrl;
import com.jws.thirdlibrary.CxManager;

public class OtherPower {
   // 上层接口，在接口内实现具体的手持机平台上电，掉电方法
   public static boolean isotherpowerup=false;//由于静态加载所以，一旦设置false，必须把导入so的上电也注释掉，否则可能因为缺少so而导致错误
	public static Context Ctt;

    //各手持机平台电源管理，需定义OpAddr地址
    //商米
    //public static String OpAddr = "/dev/ttyUHF1";
    //世麦 盛本
    //public static String OpAddr="/dev/ttyHSL3";
    //信联
   // public static String OpAddr="/dev/ttysWK0";
    //鸟鸟
    //public static String OpAddr="/dev/ttysWK1";
    //idata 95w 10
    //public static String OpAddr="/dev/ttyS0";
    //攀宁1上电
    // public static String OpAddr="/dev/ttysWK0";
    //slpb001
    //public static String OpAddr = "/dev/ttyS1";
    //攀宁2
    //public static String OpAddr="/dev/ttysWK2";
    //蓝畅
    //public static String OpAddr = "/dev/ttyS3:921600";
    //东极
   // public static String OpAddr="/dev/ttyHS0";
    //盈达 210802 t1u t2u
    public static String OpAddr= "/dev/ttyS1";
    //东方拓宇 IWRIST R11
    //public static String OpAddr="/dev/ttyS1";
    //晶锐
    //public static String OpAddr="/dev/ttyMT1";
    //攀宁3
    //public static String OpAddr="/dev/ttyS1";
    //优博讯 53S 240517
  // public static String OpAddr="/dev/ttyHSL0";
    //思必拓55rt
   // public static String OpAddr= "/dev/ttyS1";
    //世麦  盛本 zoomsmart
   // static IUhfManager uhfManager  =null;

    //触想 cx3568
   //  public static String OpAddr= "/dev/ttyS4";

    //信联
    static HcPowerCtrl ctrl = new HcPowerCtrl();

    //IDATA_T1U
    //setDriveNodeValue(mKeyInfo.getPowerManagerName(),"1");
    //setDriveNodeValue("proc/driver/uhf_uart_enable","1");

    private static void setDriveNodeValue(String path, String value) throws IOException {
        try {
            BufferedWriter bufWriter = new BufferedWriter(new FileWriter(path));
            bufWriter.write(value);
            bufWriter.close();
        } catch (IOException e) {
           throw e;
        }
    }
    public static final String IDATA_UHF_T1U = "proc/driver/uhf_power_enable";
    public static boolean  idata_t1u_powerupdownp(boolean bl)
    {
        try {
            if(bl)
            setDriveNodeValue(IDATA_UHF_T1U,"1");
            else
                setDriveNodeValue(IDATA_UHF_T1U,"0");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean oPowerUp()
	{
		boolean rebl=true;
		//power_up();

		//new OtherPower().panningpowerup();//攀宁

		//power_jingrui(true);//晶锐

		//panning2_up();

		//nn_powerup();

		//return idata_95w_powerup();//idata 95w

		// handleRfidPower(true);

		// rebl=ydpowerup();//盈达t2u

       rebl=idata_t1u_powerupdownp(true);//idata_t1u

       // panling_power_up();//攀宁

        /* 广和通
		if(powerup_ght()==0)
			return true;
		else
			return false;
         //*/

		//FileUtil.openRfidPower();

		//LCpower("1");

       // sunmipowerup();

        /*
        try {
            if(uhfManager==null)
                uhfManager  =IUhfManager.Stub.asInterface(ServiceManager.getService("uhf"));
            if(uhfManager==null)
                return false;
            uhfManager.setUhfPowerOn();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
         */

      //信联供电
     /*
     ctrl.uhfPower(1);
       ctrl.uhfCtrl(1);
       //*/

      //rebl=onEnablePower(true);//Uro

        /*
        try {
            spd_st55rt_powerOn();
        } catch (IOException e) {
           return false;
        }
         //*/

        /*
        if(cx==null) {
           cx = CxManager.register(Ctt, new ICustomCallback() {
                @Override
                public void onCustomCallback(String s) {
                    AndroidSet_3568_Power_ON();
                }
            });
        }
        else
            AndroidSet_3568_Power_ON();
         //*/
        return rebl;
	}

	public static boolean oPowerDown()
	{
		boolean rebl=true;
		//power_down();
		//power_jingrui(false);
		//panning2_down();
		//nn_powerdown();
		//return idata_95w_powerdown();
		// handleRfidPower(false);

	//	rebl=ydpowerdown();// idata_t2u
      rebl=idata_t1u_powerupdownp(true);//idata_t1u
      //  panling_power_down();

		/*
		if(powerdown_ght()==0)
			return true;
		else
			return false;
		//*/

		//FileUtil.closeRfidPower();
		//LCpower("0");


        /*
        try {
            uhfManager.setUhfPowerOn();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
         */

        //*
       ctrl.identityPower(0);
       ctrl.uhfPower(0);
       ctrl.uhfCtrl(0);
         //*/

       //rebl=onEnablePower(false);//Uro

        /*
        try {
            spd_st55rt_powerOff();
        } catch (IOException e) {
            return false;
        }
         //*/

        /*
        if(cx==null) {
            cx = CxManager.register(Ctt, new ICustomCallback() {
                @Override
                public void onCustomCallback(String s) {
                    AndroidSet_3568_Power_Off();
                }
            });
        }
        else
            AndroidSet_3568_Power_Off();
         //*/
	  return rebl;
	}

    /**
     * 优博讯 53S 上下电方法
     * @param enable  true：上电   false ： 下电
     * @return
     */
    public static boolean onEnablePower(boolean enable) {
        String NOde_53S = "/sys/devices/virtual/pogo/pogo_pin/pogo_otg_vbus";
        FileOutputStream node_1 = null;
        try {
            byte[] open_one = new byte[]{0x31};
            byte[] close = new byte[]{0x30};
            node_1 = new FileOutputStream(NOde_53S);
            node_1.write(enable ? open_one : close);
            return true;
        } catch (Throwable var13) {
            var13.printStackTrace();
        } finally {
            try {
                if (node_1 != null) {
                    node_1.close();
                }
            } catch (Exception var12) {
                var12.printStackTrace();
            }

        }
        return false;
    }

    //鸟鸟
	/*public static UHFManager mUHFManager;
	public  static void nn_init()
	{
		mUHFManager = new UHFManager(Ctt.getApplicationContext());
	}
	public  static void nn_powerup()
	{
		mUHFManager.controlUHFPower(true,1);
	}
	public  static  void nn_powerdown()
	{
		mUHFManager.controlUHFPower(false,1);
	}
	*/

	/*
	public  static PowerControl nnpower=new PowerControl();
	public  static void nn_powerup()
	{
		nnpower._5VPowerControl(true);
	}
	public  static  void nn_powerdown()
	{
		nnpower._5VPowerControl(false);
	}
*/

	//idata 95w 10
	public static boolean idata_95w_powerup(){
		boolean bl= com.idata.rfid.SerialPortControl.controlIO("/dev/idata_uhf",0x05);
		try {
			Thread.sleep(1000);
		}catch (Exception ex)
		{}
		return  bl;
	}
	public static boolean idata_95w_powerdown()
	{
		return com.idata.rfid.SerialPortControl.controlIO("/dev/idata_uhf",0x06);
	}

	//肖邦新手持机上电？？？
	private void power_up() {
		   File com = new File("/sys/devices/platform/odm/odm:exp_dev/gps_com_switch");
		   File power = new File("/sys/bus/platform/devices/odm:exp_dev/psam_en");
		   dlog.toDlogAPI("power:" + power);
		  
		   writeFile(com, "1");
		   writeFile(power, "1");
		}
	
	private void power_down() {
		   File com = new File("/sys/devices/platform/odm/odm:exp_dev/gps_com_switch");
		   File power = new File("/sys/bus/platform/devices/odm:exp_dev/psam_en");
		   dlog.toDlogAPI("power:" + power);
		  
		   writeFile(com, "0");
		   writeFile(power, "0");
		}
	private synchronized void writeFile(File file, String value) {

		   try {
		      FileOutputStream outputStream = new FileOutputStream(file);
		      outputStream.write(value.getBytes());
		      outputStream.flush();
		      outputStream.close();

		   } catch (FileNotFoundException e) {
		      e.printStackTrace();
		   } catch (IOException e) {
		      e.printStackTrace();
		   }
		}
	//>>>>>>>>>>>>>>>>>>>>>>>

   //攀宁1上电
	//*
	private PowerCtl powerCtl ;
	public void panningpowerup() {

		try {
			powerCtl = new PowerCtl();
			powerCtl.psam_ctl(1);
			powerCtl.sub_board_power(1);
			powerCtl.identity_ctl(1);
			powerCtl.scan_trig(1);
			powerCtl.scan_wakeup(0);
			powerCtl.scan_power(1);
			powerCtl.identity_uhf_power(1);
			powerCtl.uhf_ctl(1);

		} catch (SecurityException e) {
			//DisplayError(R.string.error_security);
		} catch (InvalidParameterException e) {
			//DisplayError(R.string.error_configuration);
		}
	}
	//*/
	
	//攀宁2
	/*
	 private static PowerCtl powerCtl = new PowerCtl();
	private static void panning2_up()
	{
		powerCtl.identity_uhf_power(1);
		powerCtl.identity_ctl(1);
		powerCtl.uhf_ctl(1);
	}
	private static void panning2_down()
	{

		powerCtl.identity_uhf_power(0);
		powerCtl.identity_ctl(0);
		powerCtl.uhf_ctl(0);
	}
	 */
	
	//晶锐手持机上电
	public static final String KEY_RFIDIG_POWER_ACTION = "android.intent.action.RfidIgPower";
	private static void power_jingrui(boolean enable)
	{
		Intent intent = new Intent(KEY_RFIDIG_POWER_ACTION);
		intent.putExtra(KEY_RFIDIG_POWER_ACTION, enable);
		Ctt.sendBroadcast(intent);
	}

	//东方拓宇 IWRIST R11
	private static void handleRfidPower(boolean powerOn) {
		Intent pintent = new Intent("android.intent.action.SETTINGS_BJ");
		pintent.putExtra( "enable" , powerOn);
		Ctt.sendBroadcast(pintent);
	}

 	//盈达T2U 210802
	private static boolean ydpowerup()
	{
		return com.idata.serialport.SerialPortControl.ioControl("/dev/uhf_dev",1);
	}
	private static boolean ydpowerdown()
	{
		return com.idata.serialport.SerialPortControl.ioControl("/dev/uhf_dev",0);
	}

    //东极
	static GHTlibIf  libIF=null;
	private static int powerup_ght()
	{
		int re;
		if(libIF==null)
			libIF=new GHTlibIf();
		re=libIF.InitLib();
		if(re!=0)
			return re;

		re= libIF.PowerControl(0);
		try {
			Thread.sleep(50);
		}catch(Exception ex){}
		return  re;
	}

	private static int powerdown_ght()
	{
		if(libIF==null)
			libIF=new GHTlibIf();

		int re=  libIF.PowerControl(1);
		try {
			Thread.sleep(1000);
		}catch(Exception ex){}
		return  re;
	}

    //蓝畅
	private static String s1 = "/proc/gpiocontrol/set_id";
	private static String s2 = "/proc/gpiocontrol/set_uhf";

	public static boolean LCpower(String state) {
		try {
			FileWriter localFileWriterOn = new FileWriter(s2);
			localFileWriterOn.write(state);
			localFileWriterOn.close();
			Log.e("PowerUtil", "power=" + state + " Path=" + s2);
			FileWriter RaidPower = new FileWriter(s1);
			RaidPower.write(state);
			RaidPower.close();
			Log.e("PowerUtil", "power=" + state + " Path=" + s1);
			Thread.sleep(200);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

    //商米
    /**关闭RFID服务*/
    static String BROADCAST_RFID_CLOSE = "com.sunmi.rfid.rfid_close";

   /**启用RFID服务*/
    static String BROADCAST_RFID_OPEN = "com.sunmi.rfid.rfid_open";
	final static String WAKE_STATE_STRING="/sys/class/sunmi_uhf/uhf/apint";
	static  boolean writeFileData(String path,String data)
    {
        try{
            FileWriter writer=new FileWriter(path);
            writer.write(data);
            writer.close();
            return true;
        }catch (Exception ex)
        {

        }
            return false;
    }

    public static  boolean  sunmipowerdown()
    {
        //关掉rfid服务
        Intent intent = new Intent(BROADCAST_RFID_OPEN);
        Ctt.sendBroadcast(intent);
        return  true;
    }
	public static  boolean  sunmipowerup()
    {
           //激活通信
        try {
            boolean bl = writeFileData(WAKE_STATE_STRING, "0");
            Log.d("ModuleAPI", " writeFileData "+WAKE_STATE_STRING+" 0");
            if (!bl)
                return false;
            Thread.sleep(10);

            bl = writeFileData(WAKE_STATE_STRING, "1");
            Log.d("ModuleAPI", " writeFileData "+WAKE_STATE_STRING+" 1");
            if (!bl)
                return false;
            Thread.sleep(10);
            bl = writeFileData(WAKE_STATE_STRING, "0");
            Log.d("ModuleAPI", " writeFileData "+WAKE_STATE_STRING+" 0");
            if (!bl)
                return false;
            Thread.sleep(10);

            //关掉rfid服务
            Log.d("ModuleAPI", "sendBroadcast "+BROADCAST_RFID_CLOSE);
             Intent intent = new Intent(BROADCAST_RFID_CLOSE);
            Ctt.sendBroadcast(intent);
            Thread.sleep(1000);
            return  true;
        } catch (InterruptedException e) {
            return  false;
        }
    }

    //攀凌3上电
    private static final String vol33 = "/sys/class/rt5_gpio/gpio_uhf_ext_v33";
    private static final String vol5 = "/sys/class/rt5_gpio/gpio_uhf_ext_v5";
    private static final String power = "/sys/class/rt5_gpio/gpio_uhf_power";

    public static void panling_power_up() {

        Log.d("ModuleAPI","panling_power_up:");

        writeUhfFile(vol33, "1".getBytes());
        writeUhfFile(vol5, "1".getBytes());
        writeUhfFile(power, "1".getBytes());
    }

    public static void panling_power_down() {

        Log.d("ModuleAPI","panling_power_down:");

        writeUhfFile(vol33, "0".getBytes());
        writeUhfFile(vol5, "0".getBytes());
        writeUhfFile(power, "0".getBytes());
    }

    public static void writeUhfFile(String path, byte[] value) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            fos.write(value);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("ModuleAPI","-----writeUhfFile-----e1=" + e.getMessage());
        } catch (IOException e) {
            Log.d("ModuleAPI","-----writeUhfFile-----e2=" + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void WriteFile(String path, String value) throws IOException {
        File DeviceName = new File(path);
        if (DeviceName.exists()) {
            BufferedWriter CtrlFile = new BufferedWriter(new FileWriter(DeviceName, false));
            CtrlFile.write(value);
            CtrlFile.flush();
            CtrlFile.close();
        }
    }
    public static void spd_st55rt_powerOn() throws IOException {
        WriteFile("/sys/devices/platform/pinctrl/mt_gpio", "out 165 1");
        WriteFile("/sys/devices/platform/pinctrl/mt_gpio", "mode 165 0");
        WriteFile("/sys/devices/platform/pinctrl/mt_gpio", "dir 165 1");
    }
    public static void spd_st55rt_powerOff() throws IOException {
        WriteFile("/sys/devices/platform/pinctrl/mt_gpio", "out 165 0");
    }
    //*
    public  static CxManager cx;
    public static  void AndroidSet_3568_Power_ON()
    {
        if(cx!=null)
        {
            AndroidSet_3568_Power_Off();
            int re=cx.jwsSetExtrnalGpioValue(3, false);
            Log.d("MYINFO", "AndroidSet_3568_Power_ON: "+re);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static  void AndroidSet_3568_Power_Off()
    {
        if(cx!=null)
       {
           int re= cx.jwsSetExtrnalGpioValue(3,true);
           Log.d("MYINFO", "AndroidSet_3568_Power_Off: "+re);
           try {
               Thread.sleep(200);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       }
    }
     //*/
}
