package com.example.module_android_demo;

import com.function.commfun;
import com.uhf.api.cls.R2000_calibration;
import com.uhf.api.cls.Reader;
import com.uhf.api.cls.Reader.Mtr_Param;
import com.uhf.api.cls.Reader.READER_ERR;
import com.uhf.api.cls.Reader.TagFilter_ST;
import com.uhf.api.cls.SpecialSensorTag;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

public class SubTagTempLedActivity extends Activity {
	MyApplication myapp;
	TagFilter_ST g2tf = null;
	Button button_readtemp, button_readled,button_sst,button_ledx1;
	Spinner spinner_opbank, spinner_bankr;
	private ArrayAdapter arradp_bank, arradp_fbank;
	RadioGroup gr_opant, gr_match, gr_enablefil, gr_wdatatype;
	CheckBox cb_pwd,cbmf_readcount, cbmf_rssi, cbmf_ant, cbmf_fre, cbmf_time,
            cbmf_rfu, cbmf_pro, cbmf_dl,cb_ledctt;
	EditText et_optimeout, et_selwait, et_readwait, et_startadr, et_blockcnt,
			et_invtvl,et_data,et_sst,et_ctt;
    EditText ET_ledfildata;
	private Handler handler = new Handler();
	R2000_calibration.META_DATA rmeta;
	int readtimeout;
    int ledctt;
	int sleep;
    int YLtemperture=45;
	private void SetOpant() {
		myapp.Rparams.opant = commfun.SortGroup(gr_opant) + 1;
	}
	private void SetFiler() {
		if (commfun.SortGroup(gr_enablefil) == 1) {
			int ln = ET_ledfildata.getText().toString().length();
			if (ln == 1 || ln % 2 == 1)
				ln++;
			byte[] fdata = new byte[ln / 2];
			myapp.Mreader.Str2Hex(ET_ledfildata.getText().toString(), ET_ledfildata.getText()
					.toString().length(), fdata);

			g2tf = myapp.Mreader.new TagFilter_ST();
			g2tf.fdata = fdata;
			g2tf.flen = ET_ledfildata.getText().toString().length() * 4;
			int ma = commfun.SortGroup(gr_match);
			if (ma == 1)
				g2tf.isInvert = 1;
			else
				g2tf.isInvert = 0;

			g2tf.bank = spinner_opbank.getSelectedItemPosition() + 1;

			EditText etadr = (EditText) findViewById(R.id.editText_opfilsadr_ttl);
			g2tf.startaddr = Integer.valueOf(etadr.getText().toString());

			READER_ERR er = myapp.Mreader.ParamSet(
					Mtr_Param.MTR_PARAM_TAG_FILTER, g2tf);
			if (er == READER_ERR.MT_OK_ERR)
				Toast.makeText(SubTagTempLedActivity.this,
						MyApplication.Constr_ok + er.toString(),
						Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(SubTagTempLedActivity.this,
						MyApplication.Constr_failed + er.toString(),
						Toast.LENGTH_SHORT).show();
		} else {
			g2tf = null;
			myapp.Mreader.ParamSet(Mtr_Param.MTR_PARAM_TAG_FILTER, g2tf);
		}
	}

	/**
	 * 设置密码
	 */
	private void SetPassword() {
		myapp.Rparams.password = "";
		if (cb_pwd.isChecked()) {
			EditText et = (EditText) findViewById(R.id.editText_password_ttl);
			myapp.Rparams.password = et.getText().toString();
		}
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tagtempled);
		Application app = getApplication();
		myapp = (MyApplication) app;
        ET_ledfildata= (EditText) findViewById(R.id.editText_opfilterdata_ttl);
        ET_ledfildata.setText(myapp.Curepc);
		cbmf_readcount = (CheckBox) this.findViewById(R.id.checkBox_readcount_ttl);
		cbmf_rssi = (CheckBox) this.findViewById(R.id.checkBox_rssi_ttl);
		cbmf_ant = (CheckBox) this.findViewById(R.id.checkBox_ant_ttl);
		cbmf_fre = (CheckBox) this.findViewById(R.id.checkBox_frequency_ttl);
		cbmf_time = (CheckBox) this.findViewById(R.id.checkBox_time_ttl);
		cbmf_rfu = (CheckBox) this.findViewById(R.id.checkBox_rfu_ttl);
		cbmf_pro = (CheckBox) this.findViewById(R.id.checkBox_pro_ttl);
		cbmf_dl = (CheckBox) this.findViewById(R.id.checkBox_tagdatalen_ttl);
        cb_ledctt=(CheckBox) this.findViewById(R.id.checkBox_ylledctt);
		
		arradp_bank = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, MyApplication.spibank);
		arradp_bank
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		arradp_fbank = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, MyApplication.spifbank);
		arradp_fbank
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_opbank = (Spinner) findViewById(R.id.spinner_opfbank_ttl);
		spinner_opbank.setAdapter(arradp_fbank);
		spinner_bankr = (Spinner) findViewById(R.id.spinner_bankr_ttl);
		spinner_bankr.setAdapter(arradp_bank);
		spinner_bankr.setSelection(3);
		gr_opant = (RadioGroup) findViewById(R.id.radioGroup_opant_ttl);
		gr_match = (RadioGroup) findViewById(R.id.radioGroup_opmatch_ttl);
		gr_enablefil = (RadioGroup) findViewById(R.id.radioGroup_enableopfil_ttl);
		cb_pwd = (CheckBox) findViewById(R.id.checkBox_opacepwd_ttl);

		et_optimeout = (EditText) findViewById(R.id.editText_invtime_ttl);
		et_selwait = (EditText) findViewById(R.id.editText_waitsel_ttl);
		et_readwait = (EditText) findViewById(R.id.editText_waitread_ttl);
		et_startadr = (EditText) findViewById(R.id.editText_startaddr_ttl);
		et_blockcnt = (EditText) findViewById(R.id.editText_opcountr_ttl);
		et_invtvl = (EditText) findViewById(R.id.editText_ledttl);
		et_data = (EditText) findViewById(R.id.editText_datar_ttl);
		et_sst=(EditText) findViewById(R.id.editText_sst);
        et_ctt=(EditText) findViewById(R.id.editText_ylctt);
		button_readtemp = (Button) findViewById(R.id.button_read_ttl);
		button_readled = (Button) findViewById(R.id.button_led_ttl);
        button_sst = (Button) findViewById(R.id.button_sst);
        button_ledx1= (Button) findViewById(R.id.button_lightledx1);
		button_readtemp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {

					SetOpant();
					SetPassword();
					SetFiler();
					
					byte[] rpaswd = null;
					if (!myapp.Rparams.password.equals("")) {
						 rpaswd = new byte[4];
						myapp.Mreader.Str2Hex(myapp.Rparams.password,
								myapp.Rparams.password.length(), rpaswd);
					}

					READER_ERR er = READER_ERR.MT_OK_ERR;
					R2000_calibration.META_DATA rmeta=new R2000_calibration().new META_DATA();
					if (cbmf_readcount.isChecked())
						rmeta.IsReadCnt=true;
					if (cbmf_rssi.isChecked()) 
						rmeta.IsRSSI=true;
					if (cbmf_ant.isChecked()) 
						rmeta.IsAntennaID=true;
					if (cbmf_fre.isChecked()) 
						rmeta.IsFrequency=true;
					if (cbmf_time.isChecked())
						rmeta.IsTimestamp=true;
					if (cbmf_rfu.isChecked())
						rmeta.IsRFU=true;
					if (cbmf_pro.isChecked()) 
						rmeta.IsPro=true;
					if (cbmf_dl.isChecked()) 
						rmeta.IsEmdData=true;
					
					int adrhex=Integer.parseInt(et_startadr.getText().toString(),16);
					int blkcnt=Integer.parseInt(et_blockcnt.getText().toString());
					int selwait=Integer.parseInt(et_selwait.getText().toString());
					int readwait=Integer.parseInt(et_readwait.getText().toString());
					int readtimeout=Integer.parseInt(et_optimeout.getText().toString());
					
					R2000_calibration.Tagtemperture_DATA tagtemp=new R2000_calibration().new Tagtemperture_DATA();
					 er=myapp.Mreader.ReadTagTemperature(myapp.Rparams.opant, 
							 (char) spinner_bankr.getSelectedItemPosition(), adrhex, blkcnt,
							 readtimeout,  selwait, readwait, rmeta.getMetaflag(), rpaswd, tagtemp);
				
					 if(er==READER_ERR.MT_OK_ERR)
					 { 
						 String tempet = "";
	                       if ((tagtemp.Data()[0] & 0x80) == 0)
	                       {
	                          tempet=(tagtemp.Data()[0]&0xff)-YLtemperture + "." + (tagtemp.Data()[1]&0xff) * 100 / 255;
	                       }
	                       else
	                       {
	                    	   tagtemp.Data()[0] = (byte)(~tagtemp.Data()[0]);
	                    	   tagtemp.Data()[1] = (byte)(~tagtemp.Data()[1] + 1);
	                           tempet = "-"+((tagtemp.Data()[0]&0xff)-YLtemperture) + "." + (tagtemp.Data()[1]&0xff) * 100 / 255;
	                       }
	                       
	                    tempet+="℃	  EPC:"+Reader.bytes_Hexstr(tagtemp.TagEpc())+"	";
	                    if(rmeta.IsReadCnt)
	                    tempet+="次数:"+tagtemp.ReadCount()+"	";
	                    if(rmeta.IsAntennaID)
	                    	  tempet+="天线:"+tagtemp.Antenna()+"	";
	                    if(rmeta.IsRSSI)
	                    	  tempet+="RSSI:"+tagtemp.Lqi()+"	";
	                    if(rmeta.IsFrequency)
	                    	  tempet+="频率:"+tagtemp.Frequency()+"	";
	                    et_data.setText(tempet);
						Toast.makeText(SubTagTempLedActivity.this,
								MyApplication.Constr_ok + er.toString(),
								Toast.LENGTH_SHORT).show();
					 }
					 else
						 et_data.setText("failed:"+er.toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Toast.makeText(SubTagTempLedActivity.this,
							MyApplication.Constr_excep + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		
		button_readled.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				 
				String txt = button_readled.getText().toString().trim();
				try {
				if (txt.equals("读LED")) {
					
					SetOpant();
					SetFiler();
					
					sleep=Integer.parseInt(et_invtvl.getText().toString());
					readtimeout=Integer.parseInt(et_optimeout.getText().toString());
                    ledctt=Integer.parseInt(et_ctt.getText().toString());
					rmeta=new R2000_calibration().new META_DATA();
					if (cbmf_readcount.isChecked())
						rmeta.IsReadCnt=true;
					if (cbmf_rssi.isChecked()) 
						rmeta.IsRSSI=true;
					if (cbmf_ant.isChecked()) 
						rmeta.IsAntennaID=true;
					if (cbmf_fre.isChecked()) 
						rmeta.IsFrequency=true;
					if (cbmf_time.isChecked())
						rmeta.IsTimestamp=true;
					if (cbmf_rfu.isChecked())
						rmeta.IsRFU=true;
					if (cbmf_pro.isChecked()) 
						rmeta.IsPro=true;
					if (cbmf_dl.isChecked()) 
						rmeta.IsEmdData=true;
					
					handler.post(runnable_led);
					button_readled.setText("停止");
				}
				else
				{
					handler.removeCallbacks(runnable_led);
					button_readled.setText("读LED");
				}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Toast.makeText(SubTagTempLedActivity.this,
							MyApplication.Constr_excep + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}
				
			}
		});

        button_sst.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {

                    SetOpant();
                    SetPassword();
                    SetFiler();

                    byte[] rpaswd = null;
                    if (!myapp.Rparams.password.equals("")) {
                        rpaswd = new byte[4];
                        myapp.Mreader.Str2Hex(myapp.Rparams.password,
                                myapp.Rparams.password.length(), rpaswd);
                    }

                    SpecialSensorTag.Tag_SCR409 tagscr=new SpecialSensorTag().new Tag_SCR409();
                    READER_ERR er=myapp.Mreader.SpecialSensorTagOp(1,1000, tagscr);
                    if(er==READER_ERR.MT_OK_ERR)
                    {
                        et_sst.setText("RSSI:"+tagscr.RSSI+"  Frequency:"+tagscr.Frequency+
                                "   pvtPhase:"+tagscr.Phase+"  EPC:"+myapp.Mreader.bytes_Hexstr(tagscr.EpcId)+
                                "   温度:"+tagscr.temperature);
                    }
                    else
                        et_sst.setText("failed:" + er.toString());
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(SubTagTempLedActivity.this,
                            MyApplication.Constr_excep + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        button_ledx1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
              try{
                  SetOpant();
                  SetPassword();
                  SetFiler();

                  byte[] rpaswd = null;
                  if (!myapp.Rparams.password.equals("")) {
                      rpaswd = new byte[4];
                      myapp.Mreader.Str2Hex(myapp.Rparams.password,
                              myapp.Rparams.password.length(), rpaswd);
                  }
                  byte[] led_on=new byte[]{0x00,0x01};
                  byte[] led_off=new byte[]{0x00,0x00};

                  EditText et=(EditText)findViewById(R.id.editText_ledx1time);
                  int ledtime=Integer.parseInt(et.getText().toString());
                  READER_ERR er = myapp.Mreader.WriteTagData(myapp.Rparams.opant, (char)3, 128,
                          led_on, 2, rpaswd, (short) myapp.Rparams.optime);

                  if (er == READER_ERR.MT_OK_ERR) {

                  } else
                      Toast.makeText(SubTagTempLedActivity.this,
                              MyApplication.Constr_failed + er.toString(),
                              Toast.LENGTH_SHORT).show();

                  Thread.sleep(ledtime);
                  er = myapp.Mreader.WriteTagData(myapp.Rparams.opant, (char)3, 128,
                          led_off, 2, rpaswd, (short) myapp.Rparams.optime);

                  if (er == READER_ERR.MT_OK_ERR) {

                  } else
                      Toast.makeText(SubTagTempLedActivity.this,
                              MyApplication.Constr_failed + er.toString(),
                              Toast.LENGTH_SHORT).show();
              }catch (Exception e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(SubTagTempLedActivity.this,
                            MyApplication.Constr_excep + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
	}
	private Runnable runnable_led = new Runnable() {
		public void run() {
			READER_ERR er = READER_ERR.MT_OK_ERR;
			
			R2000_calibration.TagLED_DATA tagled=new R2000_calibration().new TagLED_DATA();
			for(int i=0;i<3;i++)
			{ 
				//操作天线，超时时间，返回标签属性项，返回的标签对象
				//若需要控制led亮灯时间，则 超时时间=((控制led时间毫秒/100)<<8)|(超时时间/100)，并且把metaflag |= 0x8000;
                if(!cb_ledctt.isChecked())
				er=myapp.Mreader.ReadTagLED(myapp.Rparams.opant, (short)readtimeout,rmeta.getMetaflag(),tagled);
                else {
                    int alltimeout = (ledctt/ 100) << 8|(readtimeout/ 100);
                    er = myapp.Mreader.ReadTagLED(myapp.Rparams.opant, (short) alltimeout,
                            (short)(rmeta.getMetaflag()|0x8000), tagled);
                }
                try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}

			if(er==READER_ERR.MT_OK_ERR)
				{
				 String tempet = "";
                
              tempet+=" EPC:"+Reader.bytes_Hexstr(tagled.TagEpc())+"	";
              if(rmeta.IsReadCnt)
              tempet+="次数:"+tagled.ReadCount()+"	";
              if(rmeta.IsAntennaID)
              	  tempet+="天线:"+tagled.Antenna()+"	";
              if(rmeta.IsRSSI)
              	  tempet+="RSSI:"+tagled.Lqi()+"	";
              if(rmeta.IsFrequency)
              	  tempet+="频率:"+tagled.Frequency()+"	";
               et_data.setText(tempet);
               handler.postDelayed(this, myapp.Rparams.sleep);
				}
			else
			{	 et_data.setText("failed:"+er.toString());
			
			handler.postDelayed(this, 0);
			}
		}
	};
}
