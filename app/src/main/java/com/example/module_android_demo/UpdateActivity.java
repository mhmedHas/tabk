package com.example.module_android_demo;

import java.util.HashMap;
import java.util.Map;

import com.function.CallbackBundle;
import com.pow.api.cls.RfidPower;
import com.uhf.api.cls.R2000Command;
import com.uhf.api.cls.Reader;
import com.uhf.api.cls.Reader.READER_ERR;
import com.zguang.api.comm.linuxcomm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Dialog;


public class UpdateActivity extends Activity {
	
	private MyApplication myapp;
	Button button_selectfile,button_selectfile2,button_selectfile3,button_stupdate,button_stupdate2;
	 static private int openfileDialogId = 0; 
	 private final int HandlerorPlatBoard=0;//0手机1平板
	 ProgressBar prb;
	 String filename;
	 String serialportPath;
	 public Message amsg;
	 private Thread runthread;
	 String fileexten="mdfw";
	 Dialog dialog;
	 linuxcomm lreader;
     int up_mode=0;

	 Runnable Run=new Runnable(){
       
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				//serialportPath=myapp.Address;
				String devstr="";
				int baudrate=115200;
				if(myapp.Rpower.GetType()!=RfidPower.PDATYPE.NONE)
				serialportPath=myapp.Rpower.GetDevPath();
				else
					serialportPath=myapp.Address;
                baudrate=myapp.Baudrate;
				if(myapp.Mreader!=null&&myapp.haveinit)
				myapp.Mreader.CloseReader();
				myapp.Rpower.PowerUp();
				READER_ERR err=READER_ERR.MT_OK_ERR;
				String errmsg="";
				if(up_mode==0&&fileexten.equals("mdfw"))
					err=Reader.FirmwareLoadFromSerialPort(serialportPath,filename);
                else if(up_mode==2)
                {
                    err=Reader.ChipLoadFromSerialPort(serialportPath,filename);
                }
				else
				{
					try{
						lreader=new linuxcomm();
						String[] parscom=null;

						if(serialportPath.contains(":")) {
							parscom = serialportPath.split(":");
							devstr=parscom[0].trim();
							baudrate=Integer.parseInt(parscom[1]);
						}
						else
							devstr=serialportPath;
						int re=lreader.OpenComm(devstr,baudrate);

						if(re!=0)
							throw new Exception("openserial err:"+String.valueOf(re)+" "+devstr+" "+String.valueOf(baudrate));
						lreader.InitStream();
						R2000Command.is = lreader.getInputStream();
						R2000Command.os = lreader.getOutputStream();

						final Exception[] threadex = {null};
						final float[] finalVal = new float[1];

						new Thread(new Runnable(){

							@Override
							public void run() {
								try {
									R2000Command.Updatebyserial(filename, finalVal);
									return;
								} catch (Exception e) {
									threadex[0] =e;
								}
							}
						}).start();

						do {
							if (threadex[0] != null)
							{	throw threadex[0];}
							if(finalVal[0]>=1)
								break;

							Message msg = new Message();
							msg.what = 0;
							Bundle bundle = new Bundle();
							bundle.putFloat("ps", (float)finalVal[0]);
							msg.setData(bundle);
							handler.sendMessage(msg);
							Thread.sleep(500);
						}
						while (true);

					}catch (Exception ex)
					{
						err=READER_ERR.MT_OTHER_ERR;

						errmsg="("+devstr+" "+String.valueOf(baudrate)+")"+ ex.getMessage()+com.uhf.api.cls.LogFile_C.StackTrace(R2000Command.class);
					}
					finally
					{
						if(R2000Command.is!=null)
							R2000Command.is.close();
						if(R2000Command.os!=null)
							R2000Command.os.close();
						if(lreader!=null)
							lreader.CloseStream();
					}
				}
				Message msg = new Message(); 
				 Bundle bundle = new Bundle();  
				 msg.what = 2; 
				if(err!=READER_ERR.MT_OK_ERR)
                    bundle.putCharSequence("ps", "失败:"+err.toString()+" "+errmsg);
				else
                    bundle.putCharSequence("ps", "成功:请重新连接读写器:"+err.toString());
				 msg.setData(bundle);  
				handler.sendMessage(msg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				    Message msg = new Message(); 
      		    
	                msg.what = 1;  
	                Bundle bundle = new Bundle();  
	                
	                bundle.putCharSequence("ps", e.toString());
	               
	                msg.setData(bundle);  
				    handler.sendMessage(msg);
			}
		}
		 
	 };
	 
	 public  Handler handler = new Handler()  
	    {  
			
	        public void handleMessage(Message msg)  
	        {  
	            switch (msg.what)  
	            {  
	            case 0:  
	                {  
	                    //取出参数更新控件  
	                	Bundle bd=msg.getData();
	                	float v=bd.getFloat("ps");
	                	float vps=v*100;
	            		TextView et= (TextView)findViewById(R.id.textView_ps);
	            		String vstr=String.valueOf(vps);
	            		vstr=vstr.substring(0,vstr.indexOf(".")+2)+"%";
	            		et.setText(vstr);
	            		et.refreshDrawableState();
	            		if(!vstr.contains("失败"))
		            		prb.setProgress((int)(v*100));
	                }  
	                break;  
	            case 1:  
	            {
	            	Bundle bd=msg.getData();
                	String v=(String) bd.getCharSequence("ps");
                	TextView et= (TextView)findViewById(R.id.textView_ps);
            		 
            		et.setText(v);
            		prb.setProgress(0);
	            }
	            break;
	            case 2:
	            {
	            	Bundle bd=msg.getData();
                	String v=(String) bd.getCharSequence("ps");
                	 TextView et= (TextView)findViewById(R.id.textView_ps);
                     if(!v.contains("失败")) {
                         et.setText("100%-"+v);
                         et.refreshDrawableState();
                         prb.setProgress(100);
                     }
                     else
                    	 et.setText("0%-"+v);
					Toast.makeText(UpdateActivity.this, v,
							Toast.LENGTH_SHORT).show();
	            }
	            default:  
	                break;  
	            }  
	            super.handleMessage(msg);  
	        }  
	          
	    };  
	      
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update);
		button_selectfile=(Button)this.findViewById(R.id.button_selectfile);
		button_selectfile.setOnClickListener(new View.OnClickListener(){

			@SuppressWarnings("deprecation")
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				fileexten="mdfw";
				showDialog(openfileDialogId);
                up_mode=0;
			}
 	 
		});
		
		button_selectfile2=(Button)this.findViewById(R.id.button_selectfile2);
		button_selectfile2.setOnClickListener(new View.OnClickListener(){

			@SuppressWarnings("deprecation")
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				fileexten="bin";
				showDialog(openfileDialogId);
                up_mode=1;
			}
 	 
		});

        button_selectfile3=(Button)this.findViewById(R.id.button_selectfile3);
        button_selectfile3.setOnClickListener(new View.OnClickListener(){

            @SuppressWarnings("deprecation")
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                fileexten="bin";
                showDialog(openfileDialogId);
                up_mode=2;
            }

        });
		
		button_stupdate=(Button)this.findViewById(R.id.button_startupdate);
		button_stupdate.setOnClickListener(new View.OnClickListener() {
		 
			@Override
			public void onClick(View arg0) {
				filename="";
				// TODO Auto-generated method stub
				TextView et=(TextView)findViewById(R.id.textView_path);
				 filename=(String) et.getText();
				if(filename.isEmpty()||filename.endsWith("path"))
				{
					 Toast.makeText(UpdateActivity.this, "选择升级文件/*."+fileexten,
								Toast.LENGTH_SHORT).show();
					 return ;
				}
			 
				try {
					 
					runthread=new Thread(Run);
					runthread.start();
	                 
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Toast.makeText(UpdateActivity.this, "升级失败",
							Toast.LENGTH_SHORT).show();
				 return ;
				}
				 prb.setProgress(0);
				Toast.makeText(UpdateActivity.this, "升级开始",
						Toast.LENGTH_SHORT).show();
			}
		});
		
		prb=(ProgressBar) findViewById(R.id.progressBar1);

		myapp=(MyApplication) getApplication(); 
	 
	}
	
	protected void dialog1() {
		  AlertDialog.Builder builder = new Builder(UpdateActivity.this);
		  builder.setMessage("请确认是否需要执行，执行后请重新上电？");

		  builder.setTitle("提示");

		  builder.setPositiveButton("否", new OnClickListener() {

		   @Override
		   public void onClick(android.content.DialogInterface dialog, int which) {
		    dialog.dismiss();

		    UpdateActivity.this.finish();
		   }
 
		  });

		  builder.setNegativeButton("是", new OnClickListener() {

		   @Override
		   public void onClick(DialogInterface dialog, int which) {
		    dialog.dismiss();
		   }
		  });

		  builder.create().show();
		 }
 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override  
	   protected Dialog onCreateDialog(int id) {  
	        if(id==openfileDialogId){  
	            Map<String, Integer> images = new HashMap<String, Integer>();  
	           // 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹   
	           images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);   // 根目录图标   
               images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);    //返回上一层的图标   
	           images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);   //文件夹图标   
	           images.put(fileexten, R.drawable.filedialog_file);  
	          //images.put("wav", R.drawable.filedialog_wavfile);   //wav文件图标   //wav文件图标   
	            images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);  
	            dialog= OpenFileDialog.createDialog(id, this, "打开文件", new CallbackBundle() {  
	                @Override  
	              public void callback(Bundle bundle) {  
	                  String filepath = bundle.getString("path");  
	                 // setTitle(filepath); // 把文件路径显示在标题上   
	                  TextView et=(TextView)findViewById(R.id.textView_path);
	  				  et.setText(filepath);
	  				 dialog.dismiss();
	  				 removeDialog(openfileDialogId);
	                }  
	               
                },   
	           "."+fileexten+";",  
	          images); 
	            
	            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
             	   @Override
             	   public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
             	   {
             	   if (keyCode == KeyEvent.KEYCODE_BACK)
             	    {
             		    removeDialog(openfileDialogId);
             	      
             	    }
             	    return true;
             	   }
             	  });
	        return dialog;  
	        
	      }  
	     return null;  
	   }  
}
