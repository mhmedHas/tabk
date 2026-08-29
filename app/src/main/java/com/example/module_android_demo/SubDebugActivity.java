package com.example.module_android_demo;

import com.other.power.OtherPower;
import com.pow.api.cls.RfidPower;
import com.uhf.api.cls.Reader;
import com.uhf.api.cls.Reader.Default_Param;
import com.uhf.api.cls.Reader.Mtr_Param;
import com.uhf.api.cls.Reader.READER_ERR;
import com.zguang.api.comm.linuxcomm;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import java.util.Date;

public class SubDebugActivity extends Activity {

	@SuppressWarnings("unused")
	private Button button_open, button_close, button_powerup, button_powerdown,
			button_send1, button_send2, button_send3, button_revd,button_setbaud,button_bdclear,
    button_autocheck;

	private Spinner spinner_pdatype_debug, spinner_baudrate;
	private ArrayAdapter<String> arradp_pdatype_debug, arradp_baud;
    EditText et_addr;

	String[] str_baud = { "4800","9600", "19200", "38400", "57600", "115200","230400",
			"460800",
			"921600" };                                                                                                                                                                                                                                     
	MyApplication myapp;
	linuxcomm lreader;
	boolean isnewopen=false;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			Bundle bd = msg.getData();
			switch (msg.what) {
			case 0: {
				String val = bd.get("STRING").toString();
				
				if(val.equals(""))
				 mess="";
				else
				{
			    if(mess.length()>1000)
			    	mess=mess.substring(900);
			    	mess+=val+"\r\n";
				}
				revdDT.setText(mess);
				 
			}
				break;
			}
		}
	};
	EditText revdDT;
	Thread thread_1;
	boolean isrun_1;
	 
	String mess="";
	long nodatast=0;
	long st;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab1_tablelayout_debug);
        String[] str_pdatype =null;
		Application app = getApplication();
		myapp = (MyApplication) app;
		str_pdatype=RfidPower.getPdaPlat();
		
		spinner_pdatype_debug = (Spinner) findViewById(R.id.spinner_pdatype2);
		arradp_pdatype_debug = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, str_pdatype);
		arradp_pdatype_debug
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_pdatype_debug.setAdapter(arradp_pdatype_debug);

		spinner_baudrate = (Spinner) findViewById(R.id.spinner_baudrate);
		arradp_baud = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, str_baud);
		arradp_baud
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_baudrate.setAdapter(arradp_baud);
		spinner_baudrate.setSelection(5);

		button_open = (Button) findViewById(R.id.button_open);
		button_close = (Button) findViewById(R.id.button_close);
		button_powerup = (Button) findViewById(R.id.button_up);
		button_powerdown = (Button) findViewById(R.id.button_down);
		button_send1 = (Button) findViewById(R.id.button_send1);
		button_send2 = (Button) findViewById(R.id.button_send2);
		button_send3 = (Button) findViewById(R.id.button_send3);
		button_revd = (Button) findViewById(R.id.button_revd);
		button_setbaud=(Button) findViewById(R.id.button_pbaudr);
		button_bdclear=(Button) findViewById(R.id.button_debugclear);
        button_autocheck=(Button) findViewById(R.id.button_dback);
		if(myapp.haveinit)
			button_setbaud.setEnabled(true);
		else
			button_setbaud.setEnabled(false);
		revdDT = (EditText) findViewById(R.id.editText_revd);

        if(OtherPower.isotherpowerup)
        {
            et_addr = (EditText) findViewById(R.id.editText_daddr);
            et_addr.setText(OtherPower.OpAddr);
            OtherPower.Ctt=getApplicationContext();}
        else
            et_addr = (EditText) findViewById(R.id.editText_daddr);

		button_open.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				int re=open(et_addr.getText().toString(),Integer.valueOf(spinner_baudrate.getSelectedItem().toString()));
				if(re==0)
					button_open.setEnabled(false);
				
				Toast.makeText(SubDebugActivity.this,
						"open:" + String.valueOf(re),
						Toast.LENGTH_SHORT).show();
			}
			
		});
		button_close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				close();
				button_open.setEnabled(true);
			}
			
		});
		
		button_send1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String strval = ((EditText) findViewById(R.id.editText_send1))
						.getText().toString();
				if(strval.contains(" "))
				{
					Toast.makeText(SubDebugActivity.this,
							"open:" + strval+" no all hex chars",
							Toast.LENGTH_SHORT).show();
					return;
				}
                try {
                    byte[] buf = new byte[strval.length() / 2];
                    myapp.Mreader.Str2Hex(strval, strval.length(), buf);
                    if (isnewopen) {
                        lreader.SendDatab(buf, buf.length);
                    } else {
                        int re = myapp.Mreader.DataTransportSend(buf, buf.length, 200);
                    }
                    st = System.currentTimeMillis();
                }catch (Exception ex)
                {
                    Toast.makeText(SubDebugActivity.this,
                            "data error:"+ex.getMessage()+ex.toString(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
			}
		});
		button_send2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String strval = ((EditText) findViewById(R.id.editText_send2))
						.getText().toString().trim();
				if(strval.contains(" "))
				{
					Toast.makeText(SubDebugActivity.this,
							"open:" + strval+" no all hex chars",
							Toast.LENGTH_SHORT).show();
					return;
				}
                try {
				byte[] buf = new byte[strval.length() / 2];
				myapp.Mreader.Str2Hex(strval, strval.length(), buf);
				//myapp.Mreader.DataTransportSend(buf, buf.length, 1000);
				if(isnewopen)
				{
					lreader.SendDatab(buf, buf.length);
				}
				else{
				int re = myapp.Mreader.DataTransportSend(buf, buf.length, 1000);
				}
				st=System.currentTimeMillis();
                }catch (Exception ex)
                {
                    Toast.makeText(SubDebugActivity.this,
                            "data error:"+ex.getMessage()+ex.toString(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
			}
		});
		button_send3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String strval = ((EditText) findViewById(R.id.editText_send3))
						.getText().toString().trim();
				if(strval.contains(" "))
				{
					Toast.makeText(SubDebugActivity.this,
							"open:" + strval+" no all hex chars",
							Toast.LENGTH_SHORT).show();
					return;
				}
                try {
				byte[] buf = new byte[strval.length() / 2];
				myapp.Mreader.Str2Hex(strval, strval.length(), buf);
				if(isnewopen)
				{
					lreader.SendDatab(buf, buf.length);
				}
				else{
				int re = myapp.Mreader.DataTransportSend(buf, buf.length, 1000);
				}
				st=System.currentTimeMillis();
                }catch (Exception ex)
                {
                    Toast.makeText(SubDebugActivity.this,
                            "data error:"+ex.getMessage()+ex.toString(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
			}
		});
		button_revd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (button_revd.getText().toString()
						.equals(MyApplication.Constr_subdbrev)) {
					thread_1 = new Thread(runnable_1);
					isrun_1 = true;
					thread_1.start();
					 
					// handler.postDelayed(runnable_1, 0);
					button_revd.setText(MyApplication.Constr_stop);
				} else {
					isrun_1 = false;
					if (thread_1 != null)
						try {
							thread_1.join();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					// handler.removeCallbacks(runnable_1);
					button_revd.setText(MyApplication.Constr_subdbrev);
				}
			}
		});

		button_powerup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
                boolean blen =false;
                if(OtherPower.isotherpowerup)
                {
                    blen = OtherPower.oPowerUp();
                }
                else {
                      blen = myapp.Rpower.PowerUp();
                }

                Toast.makeText(SubDebugActivity.this,
                        MyApplication.Constr_mainpu + String.valueOf(blen),
                        Toast.LENGTH_SHORT).show();
			}

		});

		button_powerdown.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
                boolean blen=false;
                if(OtherPower.isotherpowerup)
                {
                    blen = OtherPower.oPowerDown();
                }
                else {
                     blen = myapp.Rpower.PowerDown();
                }
                Toast.makeText(SubDebugActivity.this,
                        MyApplication.action_db_down + String.valueOf(blen),
                        Toast.LENGTH_SHORT).show();
			}

		});
		
		button_setbaud.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(isnewopen)
				{
					Toast.makeText(SubDebugActivity.this,
							"must connect to reader",
							Toast.LENGTH_SHORT).show();
				}
				else{
					  Default_Param dp= myapp.Mreader.new Default_Param();
					    dp.isdefault=false;
					    dp.key=Mtr_Param.MTR_PARAM_SAVEINMODULE_BAUD;
					    dp.val=Integer.valueOf(spinner_baudrate.getSelectedItem().toString());
			 
					    READER_ERR re = myapp.Mreader.ParamSet(Mtr_Param.MTR_PARAM_SAVEINMODULE, dp);
					    if(re==READER_ERR.MT_OK_ERR)
					    {
					    	Toast.makeText(SubDebugActivity.this,
									"ok,power down fo reader,reconnect",
									Toast.LENGTH_SHORT).show();
					    }
					    else
					    {
					    	Toast.makeText(SubDebugActivity.this,
									"oprate failed",
									Toast.LENGTH_SHORT).show();
					    }
				}
			}
			
		});
		
		button_bdclear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				 mess="";
				revdDT.setText("");
			}
		});
        button_autocheck.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                revdDT.setText(new Date().toString());

                String addrs=et_addr.getText().toString();
                String msg="";
                int bdr=Integer.valueOf(spinner_baudrate.getSelectedItem().toString());
                lreader=new linuxcomm();
                int re=lreader.OpenComm(addrs,bdr);
                if(re!=0)
                {
                    msg="打开地址:"+addrs+" 波特率："+bdr+" 失败，返回错误:"+re+",请检查串口地址是否存在，正确并且不受限制可访问\r\n";
                    msg+="/open the:"+addrs+" baudrate："+bdr+" failed: return err:"+re+", please check it is existing ,right and accessible\r\n";
                    revdDT.setText(msg);
                    return;
                }
                lreader.SetRTimeout(1000);
                msg="打开地址:"+addrs+" 波特率："+bdr+" 成功\r\n";
                msg+="/open the:"+addrs+" baudrate："+bdr+" successful\r\n";
                revdDT.setText(msg);

                re=checkcommand(0x03);
                if(re!=0)
                    return;
                re=checkcommand(0x04);
                if(re!=0)
                    return;

                re=checkcommand(0x91);
                if(re!=0)
                    return;

                re=checkcommand(0x22);
                if(re!=0)
                    return;

                re=checkcommand(0x29);
                if(re!=0)
                    return;

                re=checkcommand(0x09);
                lreader.CloseComm();
            }
        });

		spinner_pdatype_debug.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
  
					 if(!OtherPower.isotherpowerup)
					et_addr.setText(myapp.Rpower.GetDevPath());
 
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}

		});
	}

    int checkcommand(int cmd)
    {
        int ret=0;
        byte[] sendbuf=null;
        byte[] revdbuf=null;
        String msgstr=revdDT.getText().toString();
        if(cmd==0x03)
        {
            sendbuf=new byte[]{(byte) 0xff,0x00,0x03,0x1d,0x0c};
            revdbuf=new byte[27];
        }
        else if(cmd==0x04)
        {
            sendbuf=new byte[]{(byte) 0xff,0x00,0x04,0x1d,0x0B};
            revdbuf=new byte[27];
        }
        else if(cmd==0x09)
        {
            sendbuf=new byte[]{(byte) 0xff,0x00,0x09,0x1d,0x06};
            revdbuf=new byte[7];
        }
        else if(cmd==0x0C)
        {
            sendbuf=new byte[]{(byte) 0xff,0x00,0x0C,0x1d,0x03};
            revdbuf=new byte[7];
        }
        else if(cmd==0x91)
        {
            sendbuf=new byte[]{(byte) 0xff,0x08, (byte) 0x91,0x04,0x01,0x01, (byte) 0xF4,
                    0x01, (byte) 0xF4,0x01, (byte) 0xF4, (byte) 0xA3, (byte) 0x88};
            revdbuf=new byte[7];
        }
        else if(cmd==0x22)
        {
            sendbuf=new byte[]{(byte) 0xff,0x05,0x22,0x00,0x00,0x03,0x00,0x32,0x38,(byte) 0xEE};
            revdbuf=new byte[11];
        }
        else if(cmd==0x29)
        {
            sendbuf=new byte[]{(byte) 0xff,0x03,0x29,0x00, (byte) 0xBF,0x00,0x4B,0x22};
            revdbuf=new byte[255];
        }

        ret=lreader.SendDatab(sendbuf, sendbuf.length);
        if(ret!=0)
        {
            msgstr+="发送 "+Integer.toHexString(cmd)+":失败，返回错误:"+ret+"\r\n";
            msgstr+="send "+Integer.toHexString(cmd)+":failed: return err:"+ret+"\r\n";
            lreader.CloseComm();
            revdDT.setText(msgstr);
            return ret;
        }
        msgstr+="发送 "+Integer.toHexString(cmd)+": 成功"+"\r\n";
        msgstr+="send "+Integer.toHexString(cmd)+": ok"+"\r\n";

        try {
            int sl=50;
            if(cmd==0x22)
                sl=100;
            Thread.sleep(sl);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int[] hreadc=new int[1];
        hreadc[0]=0;
        ret=lreader.RevDatab(revdbuf,revdbuf.length,hreadc);
        if(ret!=0)
        {
            msgstr+="接收 "+Integer.toHexString(cmd)+":失败，返回错误:"+ret+"\r\n";
            msgstr+="revd  "+Integer.toHexString(cmd)+": failed: return err:"+ret+"\r\n";
            lreader.CloseComm();
            revdDT.setText(msgstr);
            return ret;
        }

        if(hreadc[0]<7)
        {
            msgstr+="接收 "+Integer.toHexString(cmd)+" 长度不足7"+"\r\n";
            msgstr+="revd "+Integer.toHexString(cmd)+"  length not enough 7"+"\r\n";
            byte[] temp=new byte[hreadc[0]];
            System.arraycopy(revdbuf,0,temp,0,hreadc[0]);
            msgstr+="revdcmd:"+Reader.bytes_Hexstr(temp);
            lreader.CloseComm();
            revdDT.setText(msgstr);
            return ret;
        }

        if((revdbuf[0]&0xff)!=0xFF)
        {
            msgstr+="接收 "+Integer.toHexString(cmd)+" 没收到头帧FF"+"\r\n";
            msgstr+="revd "+Integer.toHexString(cmd)+"  no header FF"+"\r\n";
            byte[] temp=new byte[hreadc[0]];
            System.arraycopy(revdbuf,0,temp,0,hreadc[0]);
            msgstr+="revdcmd:"+Reader.bytes_Hexstr(temp);
            lreader.CloseComm();
            revdDT.setText(msgstr);
            return ret;
        }

        if((revdbuf[1]+7!=hreadc[0]))
        {
            msgstr+="接收 "+Integer.toHexString(cmd)+" 长度不对"+"\r\n";
            msgstr+="revd "+Integer.toHexString(cmd)+"  length error"+"\r\n";
            byte[] temp=new byte[hreadc[0]];
            System.arraycopy(revdbuf,0,temp,0,hreadc[0]);
            msgstr+="revdcmd:"+Reader.bytes_Hexstr(temp);
            lreader.CloseComm();
            revdDT.setText(msgstr);
            return ret;
        }

        if((revdbuf[3]+revdbuf[4]!=0x00))
        {
            msgstr+="接收 "+Integer.toHexString(cmd)+" 状态码不对"+"\r\n";
            msgstr+="revd "+Integer.toHexString(cmd)+"  status error"+"\r\n";
            byte[] temp=new byte[hreadc[0]];
            System.arraycopy(revdbuf,0,temp,0,hreadc[0]);
            msgstr+="revdcmd:"+Reader.bytes_Hexstr(temp);
            lreader.CloseComm();
            revdDT.setText(msgstr);
            return ret;
        }

        msgstr+="接收 "+Integer.toHexString(cmd)+" count:"+hreadc[0]+" : 成功"+"\r\n";
        msgstr+="revd "+Integer.toHexString(cmd)+" count:"+hreadc[0]+" : ok"+"\r\n";
        revdDT.setText(msgstr);
        return ret;
    }
	public int open(String addr,int b)
	{
		 lreader=new linuxcomm();
		 int re=lreader.OpenComm(addr,b);
		 if(re==0)
		 isnewopen=true;
		 else
			 isnewopen=false;
		 return re;
	}
	public void close()
	{
		if(lreader!=null)
			lreader.CloseComm();
		isnewopen=false;
	}
	public String bytes_Hexstr(byte[] bArray, int length) {
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = 0; i < length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}

	private Runnable runnable_1 = new Runnable() {
		public void run() {

			int errc=0;
			while (isrun_1) {
				String temp = "";
				
				if (isnewopen) {
					byte[] readbuffer=new byte[255];
					int[] hreadcount=new int[1];
					
                  int re= lreader.RevData_nonblockb(readbuffer, hreadcount);
					//int re= lreader.RevDatab(readbuffer, 1, hreadcount);
                  if(hreadcount[0]>0)
                  { 
                	  temp += bytes_Hexstr(readbuffer,hreadcount[0]);
                       temp="L:"+temp.length()/2+" "+temp+" T:"+(System.currentTimeMillis()-st);
                  }
                  
                  if(re>0&&errc!=re)
                  {
                	  errc=re;
                	  temp="E:"+String.valueOf(re)+" T:"+(System.currentTimeMillis()-st);
                  }
				} else {
					byte[] part1 = new byte[3];

					int re = myapp.Mreader.DataTransportRecv(part1,
							part1.length, 500);

					if (re == 0 && part1[2] != 0x00) {
						temp += bytes_Hexstr(part1, 3);
						int l = (part1[1] & 0xff) + 4;
						byte[] part2 = new byte[l];
						re = myapp.Mreader.DataTransportRecv(part2,
								part2.length, 1000);
						if (re == 0)
							temp += bytes_Hexstr(part2, part2.length);
						
						temp="L:"+temp.length()/2+" "+temp+" T:"+System.currentTimeMillis();
					}
					
					
				}
				
			
				if (!temp.isEmpty()) {
					Message msg = new Message();
					msg.what = 0;
					Bundle data = new Bundle();
					data.putString("STRING", temp);
					msg.setData(data);
					handler.sendMessage(msg);
					// revdDT.setText(temp);
					nodatast=0;
				}
				
				/*
				else
					{ 
				     //auto clear message
				 
					   if(nodatast==0)
					   nodatast=System.currentTimeMillis();
					   else
					   {
						   if(System.currentTimeMillis()-nodatast>1000)
						   {
							   Message msg = new Message();
								msg.what = 0;
								Bundle data = new Bundle();
								data.putString("STRING", "");
								msg.setData(data);
								handler.sendMessage(msg);
								// revdDT.setText(temp);
								nodatast=0;
						   }
					   }
					   
					}
				//*/
				// handler.postDelayed(this, 0);
			}
		}
	};
}
