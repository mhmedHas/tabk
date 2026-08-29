package com.example.module_android_demo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import com.uhf.api.cls.Reader;
import com.uhf.api.cls.Reader.TAGINFO;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * 系统配置
 * 
 * @author Administrator
 * 
 */
public class SubSystemActivity extends Activity {

	private Spinner spinner_threadmode;
	private ArrayAdapter<String> arradp_thmode;

	String[] str_thmode = { "MainActivity_thread", "event_broast" };

	Button button_save, button_default, button_thread, button_path,
	button_sound,button_out,button_menddef;
	static EditText et_path;
	MyApplication myapp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.systemlayout);
		Application app = getApplication();
		myapp = (MyApplication) app;
		button_save = (Button) findViewById(R.id.button_saveparam);
		button_default = (Button) findViewById(R.id.button_paramdefault);
		button_thread = (Button) findViewById(R.id.button_threadset);
		button_path = (Button) findViewById(R.id.button_locate);
		button_out = (Button) findViewById(R.id.button_outfile);
		button_sound=(Button) findViewById(R.id.button_soundset);
		button_menddef=(Button)findViewById(R.id.button_savesptomd);
		((CheckBox)findViewById(R.id.checkBox_sound)).setChecked(myapp.issound);
		spinner_threadmode = (Spinner) findViewById(R.id.spinner_thread);
		arradp_thmode = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, str_thmode);
		arradp_thmode
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_threadmode.setAdapter(arradp_thmode);

		et_path = (EditText) findViewById(R.id.editText_path);

		/**
		 * 保存参数至配置文件
		 */
		button_save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				try {
					myapp.spf.SaveReaderParams(myapp.Rparams);
				} catch (Exception ex) {
					Toast.makeText(SubSystemActivity.this,
							MyApplication.Constr_subsysavefailed,
							Toast.LENGTH_SHORT).show();
					return;
				}
				Toast.makeText(SubSystemActivity.this,
						MyApplication.Constr_subsysexefin, Toast.LENGTH_SHORT)
						.show();
			}
		});

		/**
		 * 默认参数
		 */
		button_default.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				myapp.Rparams.opant = 1;

				myapp.Rparams.invpro = new ArrayList<String>();
				myapp.Rparams.invpro.add("GEN2");

				myapp.Rparams.opro = "GEN2";

				myapp.Rparams.uants = new int[] {  };

				myapp.Rparams.readtime = 50;
				myapp.Rparams.sleep = 0;
				myapp.Rparams.checkant = 1;

				int crpow=myapp.Rparams.crpow;
				// rpow=new
				// int[]{2700,2000,2000,2000,2000,2000,2000,2000,2000,2000,2000,2000,2000,2000,2000,2000};
				myapp.Rparams.rpow = new int[] { crpow, crpow, crpow, crpow, crpow, crpow, crpow,
						crpow, crpow, crpow, crpow, crpow, crpow, crpow, crpow,
						crpow };
				myapp.Rparams.wpow = new int[] { crpow, crpow, crpow, crpow, crpow, crpow, crpow,
						crpow, crpow, crpow, crpow, crpow, crpow, crpow, crpow,
						crpow };
				//myapp.Rparams.rpow = new int[] { 2000, 2000, 2000, 2000 };
				//myapp.Rparams.wpow = new int[] { 2000, 2000, 2000, 2000 };

				myapp.Rparams.region = 1;
				myapp.Rparams.frelen = 0;

				myapp.Rparams.session = 0;
				myapp.Rparams.qv = -1;
				myapp.Rparams.wmode = 0;
				myapp.Rparams.blf = 0;
				myapp.Rparams.maxlen = 0;
				myapp.Rparams.target = 0;
				myapp.Rparams.gen2code = 2;
				myapp.Rparams.gen2tari = 0;

				myapp.Rparams.fildata = "";
				myapp.Rparams.filadr = 32;
				myapp.Rparams.filbank = 1;
				myapp.Rparams.filisinver = 0;
				myapp.Rparams.filenable = 0;

				myapp.Rparams.emdadr = 0;
				myapp.Rparams.emdbytec = 0;
				myapp.Rparams.emdbank = 1;
				myapp.Rparams.emdenable = 0;

				myapp.Rparams.adataq = 0;
				myapp.Rparams.rhssi = 1;
				myapp.Rparams.invw = 0;
				myapp.Rparams.iso6bdeep = 0;
				myapp.Rparams.iso6bdel = 0;
				myapp.Rparams.iso6bblf = 0;
				myapp.spf.SaveReaderParams(myapp.Rparams);

				Reader.Default_Param dp=myapp.Mreader.new Default_Param();
				dp.isdefault=true;
				dp.key= Reader.Mtr_Param.MTR_PARAM_RF_ANTPOWER;
				myapp.Mreader.ParamSet(Reader.Mtr_Param.MTR_PARAM_SAVEINMODULE, dp);

				dp.key= Reader.Mtr_Param.MTR_PARAM_POTL_GEN2_SESSION;
				//dp.isdefault=true; 注意设置默认值同时影响 Q，TARGET，Session
				myapp.Mreader.ParamSet(Reader.Mtr_Param.MTR_PARAM_SAVEINMODULE, dp);

				dp.key= Reader.Mtr_Param.MTR_PARAM_FREQUENCY_REGION;
				dp.val= myapp.Rparams.region;
				myapp.Mreader.ParamSet(Reader.Mtr_Param.MTR_PARAM_SAVEINMODULE, dp);

				Toast.makeText(SubSystemActivity.this,
						MyApplication.Constr_subsysreavaid, Toast.LENGTH_SHORT)
						.show();


			}
		});

		/**
		 * 指定盘点线程为前台线程或者后台线程（事件模式）
		 */
		button_thread.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				myapp.ThreadMODE = spinner_threadmode.getSelectedItemPosition();
				if (myapp.ThreadMODE == 1) {
					EditText et = (EditText) findViewById(R.id.editText_refreshtime);
					// myapp.refreshtime=Integer.valueOf(et.getText().toString());
				}
			}

		});

		/**
		 * 指定导出标签数据文件路径
		 */
		button_path.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(SubSystemActivity.this,
						SubPathActivity.class);
				startActivityForResult(intent, 0);
			}

		});

		/**
		 * 导出标签数据文件
		 */
		button_out.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (!et_path.getText().toString().contains("txt")
						&& !et_path.getText().toString().contains("csv")) {
					Toast.makeText(SubSystemActivity.this,
							MyApplication.Constr_subsysout, Toast.LENGTH_SHORT)
							.show();
				}
				File file = new File(et_path.getText().toString());
				FileOutputStream fs = null;
				try {

					fs = new FileOutputStream(file);
					String wstr = "EPC,Count,Ant,Rssi,Fre,Res,Time,Emd\r\n";
					fs.write(wstr.getBytes());

					Iterator<Entry<String, TAGINFO>> iesb;
					iesb = myapp.TagsMap.entrySet().iterator();

					while (iesb.hasNext()) {
						TAGINFO bd = iesb.next().getValue();
						String linestr = Reader.bytes_Hexstr(bd.EpcId) + ",";
						linestr += String.valueOf(bd.ReadCnt) + ",";
						linestr += String.valueOf(bd.AntennaID) + ",";
						linestr += String.valueOf(bd.RSSI) + ",";
						linestr += String.valueOf(bd.Frequency) + ",";
						linestr += String.valueOf(bd.Res[1]&0x3F) + ",";
						linestr += String.valueOf(bd.TimeStamp) + ",";
						if(bd.EmbededDatalen>0)
							linestr+=Reader.bytes_Hexstr(bd.EmbededData) + ",";
						linestr += "\r\n";
						fs.write(linestr.getBytes());
					}

					Toast.makeText(SubSystemActivity.this,
							MyApplication.Constr_subsysaveok,
							Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						if (fs != null)
							fs.close();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}

		});
		
		button_sound.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				myapp.issound=((CheckBox)findViewById(R.id.checkBox_sound)).isChecked();
			}
			
		});

		button_menddef.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Reader.Default_Param dp=myapp.Mreader.new Default_Param();
				dp.isdefault=false;
				Reader.READER_ERR ret=Reader.READER_ERR.MT_OK_ERR;
					if (myapp.Rparams.rpow != null && myapp.Rparams.rpow.length > 0
					&&myapp.Rparams.wpow != null && myapp.Rparams.wpow.length > 0) {

						dp.key= Reader.Mtr_Param.MTR_PARAM_RF_ANTPOWER;

						Reader.AntPowerConf apcf = myapp.Mreader.new AntPowerConf();
						//apcf.antcnt = AntCount;
						apcf.antcnt = myapp.antportc;
						int st=1;
						for (int i = 0; i < apcf.antcnt; i++) {
							Reader.AntPower jaap = myapp.Mreader.new AntPower();
							//jaap.antid = i + 1;
							jaap.antid = i + st;
							jaap.readPower = (short) (myapp.Rparams.rpow[i]);
							jaap.writePower = (short) (myapp.Rparams.wpow[i]);
							apcf.Powers[i] = jaap;
						}
						dp.val=apcf;

						 ret =myapp.Mreader.ParamSet(Reader.Mtr_Param.MTR_PARAM_SAVEINMODULE, dp);
					}

				dp.key= Reader.Mtr_Param.MTR_PARAM_POTL_GEN2_Q;
				dp.val=myapp.Rparams.qv;
				ret =myapp.Mreader.ParamSet(Reader.Mtr_Param.MTR_PARAM_SAVEINMODULE, dp);

				dp.key= Reader.Mtr_Param.MTR_PARAM_POTL_GEN2_SESSION;
				//dp.isdefault=true; 注意设置默认值同时影响 Q，TARGET，Session
				dp.val=myapp.Rparams.session;
				ret =myapp.Mreader.ParamSet(Reader.Mtr_Param.MTR_PARAM_SAVEINMODULE, dp);

				dp.key= Reader.Mtr_Param.MTR_PARAM_POTL_GEN2_TARGET;
				//dp.isdefault=true;注意设置默认值同时影响 Q，TARGET，Session
				dp.val=myapp.Rparams.target;
				ret =myapp.Mreader.ParamSet(Reader.Mtr_Param.MTR_PARAM_SAVEINMODULE, dp);

				dp.key= Reader.Mtr_Param.MTR_PARAM_FREQUENCY_REGION;
				dp.val= myapp.Rparams.region;
				ret =myapp.Mreader.ParamSet(Reader.Mtr_Param.MTR_PARAM_SAVEINMODULE, dp);
				if(ret!= Reader.READER_ERR.MT_OK_ERR)
				{
					Toast.makeText(SubSystemActivity.this,
							MyApplication.Constr_failed,
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

}
