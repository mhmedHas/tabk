package com.example.module_android_demo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import com.function.SPconfig;
import com.jws.thirdlibrary.CxManager;
import com.other.power.OtherPower;
import com.pow.api.cls.RfidPower;
import com.pow.api.cls.RfidPower.PDATYPE;
import com.uhf.api.cls.LogFile_C;
import com.uhf.api.cls.Reader;
import com.uhf.api.cls.Reader.AntPortsVSWR;
import com.uhf.api.cls.Reader.AntPower;
import com.uhf.api.cls.Reader.AntPowerConf;
import com.uhf.api.cls.Reader.CustomParam_ST;
import com.uhf.api.cls.Reader.EmbededData_ST;
import com.uhf.api.cls.Reader.HardwareDetails;
import com.uhf.api.cls.Reader.HoptableData_ST;
import com.uhf.api.cls.Reader.Inv_Potl;
import com.uhf.api.cls.Reader.Inv_Potls_ST;
import com.uhf.api.cls.Reader.Mtr_Param;
import com.uhf.api.cls.Reader.READER_ERR;
import com.uhf.api.cls.Reader.ReaderVersion;
import com.uhf.api.cls.Reader.Region_Conf;
import com.uhf.api.cls.Reader.SL_TagProtocol;
import com.uhf.api.cls.Reader.TagFilter_ST;
import com.uhf.api.cls.Reader.deviceVersion;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

/**
 * 连接页面类 若demo以及集成了该平台的供电管理。可以从列表中选中相关平台，则自动
 * 会列出其串口地址。并且上电后连接读写器。若无该平台则手动输入地址，ip地址或者串口地址
 * 
 * @author Administrator
 * 
 * @param <OpeListActivity>
 */
public class Sub1TabActivity<OpeListActivity> extends Activity {

	private Button button_connect, button_disconnect,button_menu,button_update;
	TabHost tabHost_connect;

	private Spinner spinner_pdatype, spinner_antports;
	@SuppressWarnings("rawtypes")
	private ArrayAdapter arradp_pdatype, arradp_antports,arradp_baud;;
    String[] str_baud = { "4800","9600", "19200", "38400", "57600", "115200","230400",
            "460800", "921600" };
    public static Spinner spinner_cbaudrate;
    // 已经集成的平台
	String[] pdatypev;

	MyApplication myapp;
	public static EditText et_addr;
	boolean isdefaultmaxpow_V=false;
	//public static boolean isotherpowerup=false;

	// USB bridge (CH340/CH34x reader dongles) - عشان يتغلب على مشكلة MT_IO_ERR
	private BridgeHelper bridgeHelper;
	private static final String ACTION_USB_PERM = "com.example.module_android_demo.USB_PERM";
	private BroadcastReceiver usbPermReceiver;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab1_tablelayout_connect);
        byte[] Res=new byte[]{0x01, (byte) 0xD6};

		Application app = getApplication();
		myapp = (MyApplication) app;
		//myapp.Mreader = new Reader("/lib/myalib/");
		//myapp.Mreader = new Reader("/lib/myalib");

        Reader.isLogfile=false;
		myapp.Mreader = new Reader();

		myapp.spf = new SPconfig(this);

		pdatypev=RfidPower.getPdaPlat();
		pdatypev[0] = MyApplication.No;

		spinner_antports = (Spinner) findViewById(R.id.spinner_antports);
		arradp_antports = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, MyApplication.pdaatpot);
		arradp_antports
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_antports.setAdapter(arradp_antports);
		spinner_pdatype = (Spinner) findViewById(R.id.spinner_pdatype);
		// 将可选内容与ArrayAdapter连接起来
		arradp_pdatype = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, pdatypev);
		// 设置下拉列表的风格
		arradp_pdatype
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		spinner_pdatype.setAdapter(arradp_pdatype);

        spinner_cbaudrate = (Spinner) findViewById(R.id.spinner_ccbaudrate);
        arradp_baud = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, str_baud);
        arradp_baud
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_cbaudrate.setAdapter(arradp_baud);
        spinner_cbaudrate.setSelection(5);

		button_connect = (Button) findViewById(R.id.button_connect);
		button_disconnect = (Button) findViewById(R.id.button_disconnect);
        button_menu=(Button) findViewById(R.id.button_menu);
        button_update=(Button) findViewById(R.id.button_update);

		et_addr = (EditText) findViewById(R.id.editText_adr);

		String pdatypestr = myapp.spf.GetString("PDATYPE");
		String addressstr = myapp.spf.GetString("ADDRESS");
		String antportstr = myapp.spf.GetString("ANTPORT");
        String baudratstr = myapp.spf.GetString("BAUDRATE");
		//if(OtherPower.isotherpowerup)
	//	{ et_addr.setText(OtherPower.OpAddr);OtherPower.Ctt=this.getApplicationContext();
            /*
            OtherPower.cx= CxManager.register(this, new com.jws.thirdlibrary.ICustomCallback() {
                @Override
                public void onCustomCallback(String s) {

                }
            });
             //*/
       // }
	//	else{
			if (!pdatypestr.equals("")) {
				spinner_pdatype.setSelection(Integer.valueOf(pdatypestr));
				if (spinner_pdatype.getSelectedItemPosition() == 0) {
					et_addr.setText(addressstr);
				}
			}
	//	}


		if (!antportstr.equals("")) {
			spinner_antports.setSelection(Integer.valueOf(antportstr));
		}
        if(!baudratstr.equals("")){
           for(int i=0;i<str_baud.length;i++)
               if(baudratstr.equals(str_baud[i]))
               {
                   spinner_cbaudrate.setSelection(i);
               }
        }

        button_update.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                myapp.Address = Sub1TabActivity.et_addr.getText()
                        .toString();
                myapp.Baudrate =Integer.parseInt(Sub1TabActivity.spinner_cbaudrate.getSelectedItem().toString());

                Intent intent = new Intent(Sub1TabActivity.this,
                        UpdateActivity.class);
                startActivityForResult(intent, 0);
            }
        });
		/**
		 * 连接读写器，配置gen2协议（有些读写器必须指定gen2协议）
		 */
		button_connect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				String ip = et_addr.getText().toString();

				if (ip.trim().isEmpty()) {
					Toast.makeText(Sub1TabActivity.this,
							MyApplication.Constr_sub1adrno, Toast.LENGTH_SHORT)
							.show();
					return;
				}

				// --- USB bridge mode: اكتب "usb" أو "USB" في خانة العنوان ---
				if (ip.trim().equalsIgnoreCase("usb")) {
					UsbManager um = (UsbManager) getSystemService(Context.USB_SERVICE);
					UsbDevice dev = BridgeHelper.findDevice(um);
					if (dev == null) {
						Toast.makeText(Sub1TabActivity.this,
								"USB device not found (VID=2E88/1A86)", Toast.LENGTH_LONG).show();
						return;
					}
					if (!um.hasPermission(dev)) {
						PendingIntent pi = PendingIntent.getBroadcast(
								Sub1TabActivity.this, 0,
								new Intent(ACTION_USB_PERM),
								0x02000000 | PendingIntent.FLAG_UPDATE_CURRENT); // FLAG_MUTABLE
						um.requestPermission(dev, pi);
						Toast.makeText(Sub1TabActivity.this,
								"تم طلب إذن الوصول لجهاز USB، اضغط اتصال تاني بعد الموافقة",
								Toast.LENGTH_LONG).show();
						return;
					}
					bridgeHelper = new BridgeHelper();
					ip = bridgeHelper.start(um, dev);
					if (ip == null) {
						Toast.makeText(Sub1TabActivity.this,
								"فشل تشغيل جسر USB (Bridge)", Toast.LENGTH_LONG).show();
						bridgeHelper = null;
						return;
					}
					Toast.makeText(Sub1TabActivity.this,
							"تم تشغيل الجسر على " + ip, Toast.LENGTH_SHORT).show();
				} else {
				if (myapp.Rpower == null) {
					Toast.makeText(Sub1TabActivity.this,
							MyApplication.Constr_sub1pdtsl, Toast.LENGTH_SHORT)
							.show();
				}
				
				boolean blen =false;
				 
				if(myapp.Rpower.GetType()!=RfidPower.PDATYPE.NONE)
				{  
					blen =myapp.Rpower.PowerUp();
					Toast.makeText(Sub1TabActivity.this,
				 	MyApplication.Constr_mainpu + String.valueOf(blen),
						Toast.LENGTH_SHORT).show();

					if (!blen)
						return;
				}
				else
				{
			    	if(OtherPower.isotherpowerup)
						OtherPower.oPowerUp();
				}
				}
				

				int portc = spinner_antports.getSelectedItemPosition();
				if (portc == 5)
					portc = 16;
				else if(portc==4)
					portc=8;
				else
					portc++;
				
				//myapp.dv=new deviceVersion();
				//Reader.GetDeviceVersion(ip, myapp.dv);
                if(!ip.contains(".")&&!ip.contains(":")&&!spinner_cbaudrate.getSelectedItem().toString().equals("115200"))
                {
                    ip+=":";
                    ip+=spinner_cbaudrate.getSelectedItem().toString();
                }
		 
				long const_v = System.currentTimeMillis();
				READER_ERR er = myapp.Mreader.InitReader_Notype(ip, portc);
				Log.d("MYINFO", "connect cost time:"
								+ String.valueOf(System.currentTimeMillis() - const_v));
				 
				if (er == READER_ERR.MT_OK_ERR) {
					myapp.needlisen = true;
					myapp.needreconnect = false;
					myapp.spf.SaveString("PDATYPE", String
							.valueOf(spinner_pdatype.getSelectedItemPosition()));
					myapp.spf.SaveString("ADDRESS", et_addr.getText().toString());
					myapp.spf.SaveString("ANTPORT",
							String.valueOf(spinner_antports
									.getSelectedItemPosition()));
                    myapp.spf.SaveString("BAUDRATE",
                            String.valueOf(spinner_cbaudrate
                                    .getSelectedItem().toString()));

					// Toast.makeText(Sub1TabActivity.this,
					// MyApplication.Constr_connectok, Toast.LENGTH_SHORT)
					// .show();

                    Reader.CustomParam_ST cpara = myapp.Mreader.new CustomParam_ST();
                    cpara.ParamName = "Reader/Savestandby";
                    cpara.ParamVal = new byte[1];
                    cpara.ParamVal[0] = 1;
                    myapp.Mreader.ParamSet(Reader.Mtr_Param.MTR_PARAM_CUSTOM, cpara);
                    /*
                    if(myapp.Rpower.GetType()!=RfidPower.PDATYPE.NONE)
                    {
                        Reader.CustomParam_ST cpara = myapp.Mreader.new CustomParam_ST();
                        cpara.ParamName = "Reader/Savestandby";
                        cpara.ParamVal = new byte[1];
                        cpara.ParamVal[0] = 1;
                        myapp.Mreader.ParamSet(Reader.Mtr_Param.MTR_PARAM_CUSTOM, cpara);
                    }
                    else
                    {
                        if(OtherPower.isotherpowerup)
                        {
                            Reader.CustomParam_ST cpara = myapp.Mreader.new CustomParam_ST();
                            cpara.ParamName = "Reader/Savestandby";
                            cpara.ParamVal = new byte[1];
                            cpara.ParamVal[0] = 1;
                            myapp.Mreader.ParamSet(Reader.Mtr_Param.MTR_PARAM_CUSTOM, cpara);
                        }
                    }
                     */

					Inv_Potls_ST ipst = myapp.Mreader.new Inv_Potls_ST();
					ipst.potlcnt = 1;
					ipst.potls = new Inv_Potl[ipst.potlcnt];
					for (int i = 0; i < ipst.potlcnt; i++) {
						Inv_Potl ipl = myapp.Mreader.new Inv_Potl();
						ipl.weight = 30;
						ipl.potl = SL_TagProtocol.SL_TAG_PROTOCOL_GEN2;
						ipst.potls[i] = ipl;
					}
					er = myapp.Mreader.ParamSet(
							Mtr_Param.MTR_PARAM_TAG_INVPOTL, ipst);
					int[] av = new int[1];
					myapp.Mreader.ParamGet(
							Mtr_Param.MTR_PARAM_READER_AVAILABLE_ANTPORTS, av);
					myapp.antportc = portc;
					
					
					ConnectHandleUI();
					myapp.Address = ip;
					myapp.haveinit=true;

				} else {
                    myapp.Mreader.CloseReader();
					Toast.makeText(
							Sub1TabActivity.this,
							MyApplication.Constr_connectfialed + ":"
									+ er.toString(), Toast.LENGTH_SHORT).show();
				}
				 
			}
		});

		/**
		 * 关闭读写器，释放资源并且掉电
		 */
		button_disconnect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				if (myapp.Mreader != null) {
					myapp.Mreader.CloseReader();
					myapp.needlisen = true;
					myapp.haveinit=false;
				}

                boolean blen =false;

                if(myapp.Rpower.GetType()!=RfidPower.PDATYPE.NONE)
                    blen= myapp.Rpower.PowerDown();
                else
                {
                    if(OtherPower.isotherpowerup)
                      OtherPower.oPowerDown();
                }

				Toast.makeText(
						Sub1TabActivity.this,
						MyApplication.Constr_disconpowdown
								+ String.valueOf(blen), Toast.LENGTH_SHORT)
						.show();
				DisConnectHandleUI();
			}

		});
		button_menu.setOnClickListener(new OnClickListener() {
			 @Override
			  public void onClick(View arg0) {
				 openOptionsMenu();
				
			  }
		});
		/**
		 * 若demo集成了该平台电源管理，则选择平台，构建Rfidpower对象
		 */
		spinner_pdatype.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub

				PDATYPE PT = PDATYPE.valueOf(arg2);
				myapp.Rpower = new RfidPower(PT,getApplicationContext());
				if (PT != PDATYPE.NONE) {
                    et_addr.setText(myapp.Rpower.GetDevPath());
				}

				// *
				String mod = android.os.Build.MODEL;
				mod+=" "+android.os.Build.CPU_ABI;
				Toast.makeText(Sub1TabActivity.this, mod, Toast.LENGTH_SHORT)
						.show();
				/*
				 * if (mod.equals("h901")) myapp.Rpower = new
				 * RfidPower(PDATYPE.HANDEHUOER); else if (mod.equals("SHT37"))
				 * myapp.Rpower = new RfidPower(PDATYPE.XBANG_4G); //
				 */
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		inputMethodManager.hideSoftInputFromWindow(Sub1TabActivity.this
				.getCurrentFocus().getWindowToken(),

		InputMethodManager.HIDE_NOT_ALWAYS);

		// --- USB permission receiver ---
		usbPermReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// الإذن اتاخد -> المستخدم يدوس اتصال تاني
			}
		};
		ContextCompat.registerReceiver(this, usbPermReceiver, new IntentFilter(ACTION_USB_PERM),
				ContextCompat.RECEIVER_NOT_EXPORTED);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (usbPermReceiver != null) {
			try { unregisterReceiver(usbPermReceiver); } catch (Exception ignored) {}
		}
	}

	/**
	 * 连接后读取配置文件配置读写器
	 */
	private void ConnectHandleUI() {
		try {
			READER_ERR er;
			myapp.Rparams = myapp.spf.ReadReaderParams();
            int[] mp = new int[1];
			if(isdefaultmaxpow_V)
			{
			  er = myapp.Mreader.ParamGet(
						Mtr_Param.MTR_PARAM_RF_MAXPOWER, mp);
			}
            else
            {
                AntPowerConf apcf2 = myapp.Mreader.new AntPowerConf();
                er = myapp.Mreader.ParamGet(
                        Mtr_Param.MTR_PARAM_RF_ANTPOWER, apcf2);
                if(er==READER_ERR.MT_OK_ERR)
                    mp[0]=apcf2.Powers[0].readPower;
                else
                    mp[0]=2000;
            }
              myapp.Rparams.setdefaulpwval((short)mp[0]);

			if (myapp.Rparams.invpro.size() < 1)
				myapp.Rparams.invpro.add("GEN2");

			List<SL_TagProtocol> ltp = new ArrayList<SL_TagProtocol>();
			for (int i = 0; i < myapp.Rparams.invpro.size(); i++) {
				if (myapp.Rparams.invpro.get(i).equals("GEN2")) {
					ltp.add(SL_TagProtocol.SL_TAG_PROTOCOL_GEN2);

				} else if (myapp.Rparams.invpro.get(i).equals("6B")) {
					ltp.add(SL_TagProtocol.SL_TAG_PROTOCOL_ISO180006B);

				} else if (myapp.Rparams.invpro.get(i).equals("IPX64")) {
					ltp.add(SL_TagProtocol.SL_TAG_PROTOCOL_IPX64);

				} else if (myapp.Rparams.invpro.get(i).equals("IPX256")) {
					ltp.add(SL_TagProtocol.SL_TAG_PROTOCOL_IPX256);

				}
			}

			Inv_Potls_ST ipst = myapp.Mreader.new Inv_Potls_ST();
			ipst.potlcnt = ltp.size();
			ipst.potls = new Inv_Potl[ipst.potlcnt];
			SL_TagProtocol[] stp = ltp
					.toArray(new SL_TagProtocol[ipst.potlcnt]);
			for (int i = 0; i < ipst.potlcnt; i++) {
				Inv_Potl ipl = myapp.Mreader.new Inv_Potl();
				ipl.weight = 30;
				ipl.potl = stp[i];
				ipst.potls[i] = ipl;
			}

			er = myapp.Mreader.ParamSet(Mtr_Param.MTR_PARAM_TAG_INVPOTL, ipst);
			Log.d("MYINFO", "Connected set pro:" + er.toString());

			er = myapp.Mreader.ParamSet(Mtr_Param.MTR_PARAM_READER_IS_CHK_ANT,
					new int[] { myapp.Rparams.checkant });
			Log.d("MYINFO", "Connected set checkant:" + er.toString());

			AntPowerConf apcf = myapp.Mreader.new AntPowerConf();
			apcf.antcnt = myapp.antportc;
			for (int i = 0; i < apcf.antcnt; i++) {
				AntPower jaap = myapp.Mreader.new AntPower();
				jaap.antid = i + 1;
				jaap.readPower = (short) myapp.Rparams.rpow[i];
				jaap.writePower = (short) myapp.Rparams.wpow[i];
				apcf.Powers[i] = jaap;
			}
            if(myapp.Rparams.rpow[0]!=0) {
                Sub4TabActivity.nowpower = myapp.Rparams.rpow[0];
                myapp.Mreader.ParamSet(Mtr_Param.MTR_PARAM_RF_ANTPOWER, apcf);
            }
			 
			Region_Conf rre;
			switch (myapp.Rparams.region) {
			case 0:
				rre = Region_Conf.RG_PRC;
				break;
			case 1:
				rre = Region_Conf.RG_NA;
				break;
			case 2:
				rre = Region_Conf.RG_NONE;
				break;
			case 3:
				rre = Region_Conf.RG_KR;
				break;
			case 4:
				rre = Region_Conf.RG_EU;
				break;
			case 5:
				rre = Region_Conf.RG_EU2;
				break;
			case 6:
				rre = Region_Conf.RG_EU3;
				break;
			case 9:
				rre=Region_Conf.RG_OPEN;
				break;
			case 10:
				rre=Region_Conf.RG_PRC2;
				break;
			case 7:
			case 8:	 
			default:
				rre = Region_Conf.RG_NONE;
				break;
			}
			if (rre != Region_Conf.RG_NONE) {
				er = myapp.Mreader.ParamSet(
						Mtr_Param.MTR_PARAM_FREQUENCY_REGION, rre);
			}

			if (myapp.Rparams.frelen > 0) {

				HoptableData_ST hdst = myapp.Mreader.new HoptableData_ST();
				hdst.lenhtb = myapp.Rparams.frelen;
				hdst.htb = myapp.Rparams.frecys;
				er = myapp.Mreader.ParamSet(
						Mtr_Param.MTR_PARAM_FREQUENCY_HOPTABLE, hdst);
			}

			er = myapp.Mreader.ParamSet(Mtr_Param.MTR_PARAM_POTL_GEN2_SESSION,
					new int[] { myapp.Rparams.session });
			er = myapp.Mreader.ParamSet(Mtr_Param.MTR_PARAM_POTL_GEN2_Q,
					new int[] { myapp.Rparams.qv });
			er = myapp.Mreader.ParamSet(
					Mtr_Param.MTR_PARAM_POTL_GEN2_WRITEMODE,
					new int[] { myapp.Rparams.wmode });
			er = myapp.Mreader.ParamSet(
					Mtr_Param.MTR_PARAM_POTL_GEN2_MAXEPCLEN,
					new int[] { myapp.Rparams.maxlen });
			er = myapp.Mreader.ParamSet(Mtr_Param.MTR_PARAM_POTL_GEN2_TARGET,
					new int[] { myapp.Rparams.target });

			if (myapp.Rparams.filenable == 1) {
				TagFilter_ST tfst = myapp.Mreader.new TagFilter_ST();
				tfst.bank = myapp.Rparams.filbank;
				int len = myapp.Rparams.fildata.length();
				len = len % 2 == 0 ? len : len + 1;
				tfst.fdata = new byte[len / 2];
				myapp.Mreader.Str2Hex(myapp.Rparams.fildata,
						myapp.Rparams.fildata.length(), tfst.fdata);
				tfst.flen = myapp.Rparams.fildata.length() * 4;
				tfst.startaddr = myapp.Rparams.filadr;
				tfst.isInvert = myapp.Rparams.filisinver;

				myapp.Mreader.ParamSet(Mtr_Param.MTR_PARAM_TAG_FILTER, tfst);
			}

			if (myapp.Rparams.emdenable == 1) {
				EmbededData_ST edst = myapp.Mreader.new EmbededData_ST();

				edst.accesspwd = null;
				edst.bank = myapp.Rparams.emdbank;
				edst.startaddr = myapp.Rparams.emdadr;
				edst.bytecnt = myapp.Rparams.emdbytec;

				er = myapp.Mreader.ParamSet(
						Mtr_Param.MTR_PARAM_TAG_EMBEDEDDATA, edst);
			}

			er = myapp.Mreader.ParamSet(
					Mtr_Param.MTR_PARAM_TAGDATA_UNIQUEBYEMDDATA,
					new int[] { myapp.Rparams.adataq });
			er = myapp.Mreader.ParamSet(
					Mtr_Param.MTR_PARAM_TAGDATA_RECORDHIGHESTRSSI,
					new int[] { myapp.Rparams.rhssi });
			er = myapp.Mreader.ParamSet(Mtr_Param.MTR_PARAM_TAG_SEARCH_MODE,
					new int[] { myapp.Rparams.invw });

	/*		AntPortsVSWR apvr=myapp.Mreader.new AntPortsVSWR();
			apvr.andid=1;
			apvr.power=(short) myapp.Rparams.rpow[0];
			apvr.region=Region_Conf.RG_NA;
			er=myapp.Mreader.ParamGet(Mtr_Param.MTR_PARAM_RF_ANTPORTS_VSWR, apvr);*/
			
			TextView tv_module = (TextView) findViewById(R.id.textView_module);

			HardwareDetails val = myapp.Mreader.new HardwareDetails();
			er = myapp.Mreader.GetHardwareDetails(val);

			if(val.antportnumbers>1) {
				if (myapp.Rparams.uants == null || myapp.Rparams.uants.length == 0) {
					Reader.ConnAnts_ST valc = myapp.Mreader.new ConnAnts_ST();
					er = myapp.Mreader.ParamGet(
							Mtr_Param.MTR_PARAM_READER_CONN_ANTS, valc);
					if (er == READER_ERR.MT_OK_ERR) {
						if (valc.antcnt > 0) {
							myapp.Rparams.uants = new int[valc.antcnt];
							for (int i = 0; i < valc.antcnt; i++) {
								myapp.Rparams.uants[i] = valc.connectedants[i];
							}
						}
					}
				}
			}
			else
			{
				myapp.Rparams.uants=new int[]{1};
			}

			if (er == READER_ERR.MT_OK_ERR) {

				//ReaderVersion rdrver=myapp.Mreader.new ReaderVersion();
				//myapp.Mreader.ParamGet(Mtr_Param.MTR_PARAM_READER_VERSION,rdrver);

				String sn = "";  //序列号
				String bootver = "";  //boot 版本
				String hardver = "";  //硬件版本
				String firmdata = "";  //固件信息
				String firmver = "";  //固件版本
                String devSerino="";
                Reader.DeviceSerialNumber dnr=myapp.Mreader.new DeviceSerialNumber();
                myapp.Mreader.GetSerialNumber(dnr);
                devSerino=dnr.serailNumber;
				CustomParam_ST cpara = myapp.Mreader.new CustomParam_ST();
				cpara.ParamName = "reader/moduleinfo";
				READER_ERR ret = myapp.Mreader.ParamGet(Mtr_Param.MTR_PARAM_CUSTOM, cpara);
				if (ret != READER_ERR.MT_OK_ERR) {
					ReaderVersion rdrver = myapp.Mreader.new ReaderVersion();
					myapp.Mreader.ParamGet(Mtr_Param.MTR_PARAM_READER_VERSION, rdrver);
                    String MT=val.module.toString();
                    if(rdrver.hardwareVer.contains("B100"))
                        MT="SFM2100";
                    else if(rdrver.hardwareVer.contains("B110"))
                        MT="SFM2105";
                    else if(rdrver.hardwareVer.contains("B102"))
                        MT="SFM2200";
                    else if(rdrver.hardwareVer.contains("B103"))
                        MT="SFM2300";
                    else if(rdrver.hardwareVer.contains("B104"))
                        MT="SFM2400";

					tv_module.setText("module:" + MT + "\r\nsoftwareVer:" +
							rdrver.softwareVer + "\r\nhardwareVer:" + rdrver.hardwareVer+"\r\ndevSerino:"+devSerino);
				} else {
					byte[] binhv = new byte[28];
					System.arraycopy(cpara.ParamVal, 16, binhv, 0, 28);
					byte[] temp = new byte[1];
					int p = 0;

					for (int i = 0; i < 12; i++) {
						temp[0] = binhv[p++];
						sn += myapp.Mreader.bytes_Hexstr(temp);
					}

					temp[0] = binhv[p++];
					bootver += myapp.Mreader.bytes_Hexstr(temp) + ".";
					temp[0] = binhv[p++];
					bootver += myapp.Mreader.bytes_Hexstr(temp) + ".";
					temp[0] = binhv[p++];
					bootver += myapp.Mreader.bytes_Hexstr(temp) + ".";
					temp[0] = binhv[p++];
					bootver += myapp.Mreader.bytes_Hexstr(temp);

					temp[0] = binhv[p++];
					hardver += myapp.Mreader.bytes_Hexstr(temp) + ".";
					temp[0] = binhv[p++];
					hardver += myapp.Mreader.bytes_Hexstr(temp) + ".";
					temp[0] = binhv[p++];
					hardver += myapp.Mreader.bytes_Hexstr(temp) + ".";
					temp[0] = binhv[p++];
					hardver += myapp.Mreader.bytes_Hexstr(temp);

					temp[0] = binhv[p++];
					firmdata += myapp.Mreader.bytes_Hexstr(temp) + ".";
					temp[0] = binhv[p++];
					firmdata += myapp.Mreader.bytes_Hexstr(temp) + ".";
					temp[0] = binhv[p++];
					firmdata += myapp.Mreader.bytes_Hexstr(temp) + ".";
					temp[0] = binhv[p++];
					firmdata += myapp.Mreader.bytes_Hexstr(temp);

					temp[0] = binhv[p++];
					firmver += myapp.Mreader.bytes_Hexstr(temp) + ".";
					temp[0] = binhv[p++];
					firmver += myapp.Mreader.bytes_Hexstr(temp) + ".";
					temp[0] = binhv[p++];
					firmver += myapp.Mreader.bytes_Hexstr(temp) + ".";
					temp[0] = binhv[p++];
					firmver += myapp.Mreader.bytes_Hexstr(temp);

					CustomParam_ST cpara2 = myapp.Mreader.new CustomParam_ST();
					cpara2.ParamName = "reader/infacthardwareinfo";
					ret = myapp.Mreader.ParamGet(Mtr_Param.MTR_PARAM_CUSTOM, cpara2);
					if (ret == READER_ERR.MT_OK_ERR) {
						if (cpara2.ParamVal[0] == 1) {
							myapp.Ismulpro=true;
							p = 1;
							hardver = "";
							temp[0] = cpara2.ParamVal[p++];
							hardver += myapp.Mreader.bytes_Hexstr(temp) + ".";
							temp[0] = cpara2.ParamVal[p++];
							hardver += myapp.Mreader.bytes_Hexstr(temp) + ".";
							temp[0] = cpara2.ParamVal[p++];
							hardver += myapp.Mreader.bytes_Hexstr(temp) + ".";
							temp[0] = cpara2.ParamVal[p++];
							hardver += myapp.Mreader.bytes_Hexstr(temp);

						}
						else
							myapp.Ismulpro=false;
					}
                    String MT=val.module.toString();
                    if(hardver.contains("B1.00"))
                        MT="MODOULE_SFM2100";
                    else if(hardver.contains("B1.10"))
                        MT="MODOULE_SFM2105";
                    else if(hardver.contains("B1.02"))
                        MT="MODOULE_SFM2200";
                    else if(hardver.contains("B1.03"))
                        MT="MODOULE_SFM2300";
                    else if(hardver.contains("B1.04"))
                        MT="MODOULE_SFM2400";
					tv_module.setText("module:" + MT + "\r\nbootver:" +
							bootver + "\r\nhardver:" + hardver + "\r\nfirmdat:" + firmdata +
							"\r\nfirmver:" + firmver + "\r\nmodulesn:" + sn+"\r\ndevSerino:"+devSerino);
				}
				myapp.myhd = val;
			}
			/*
			long st=System.currentTimeMillis();
			byte[] restmodule=new byte[]{(byte) 0xff,0x0f,(byte) 0xaa,0x4d,0x6f,0x64,0x75,
					0x6c,0x65,0x74,0x65,0x63,0x68,(byte) 0xaa,0x4b,0x00,(byte) 0xf5,(byte) 0xbb,(byte) 0xf8,0x3a};
			int re=myapp.Mreader.DataTransportSend(restmodule,restmodule.length,500);

			byte[] revd=new byte[19];
			re=myapp.Mreader.DataTransportRecv(revd,19,1000);
			Log.d("MYINFO", "reset cost:"+String.valueOf(System.currentTimeMillis()-st));
            //*/
            if(myapp.Rparams.readtime==50&&myapp.antportc>1)
            myapp.Rparams.readtime=myapp.Rparams.readtime*myapp.antportc;
		} catch (Exception ex) {
			Log.d("MYINFO",
					ex.getMessage() + ex.toString() + ex.getStackTrace());
		}
		this.button_connect.setEnabled(false);
		this.button_disconnect.setEnabled(true);
		TabWidget tw = myapp.tabHost.getTabWidget();
		tw.getChildAt(1).setVisibility(View.VISIBLE);
		tw.getChildAt(2).setVisibility(View.VISIBLE);
		tw.getChildAt(3).setVisibility(View.VISIBLE);
		myapp.tabHost.setCurrentTab(1);
	}

	private void DisConnectHandleUI() {
		button_disconnect.setEnabled(false);
		button_connect.setEnabled(true);
		TabWidget tw = myapp.tabHost.getTabWidget();
		tw.getChildAt(1).setVisibility(View.INVISIBLE);
		tw.getChildAt(2).setVisibility(View.INVISIBLE);
		tw.getChildAt(3).setVisibility(View.INVISIBLE);
		TextView tv_module = (TextView) findViewById(R.id.textView_module);
		tv_module.setText("");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - myapp.exittime) > 2000) {
				Toast.makeText(getApplicationContext(),
						MyApplication.Constr_Putandexit, Toast.LENGTH_SHORT)
						.show();
				myapp.exittime = System.currentTimeMillis();
			} else {
				finish();
				// System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
    public void openOptionsMenu() {
        Configuration config = getResources().getConfiguration();
        //如果是大屏幕
      /*  if((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)> Configuration.SCREENLAYOUT_SIZE_LARGE) {
            int originalScreenLayout = config.screenLayout;
            config.screenLayout = Configuration.SCREENLAYOUT_SIZE_LARGE;
            super.openOptionsMenu();
            config.screenLayout = originalScreenLayout;
        } else*/
        {
            super.openOptionsMenu();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
