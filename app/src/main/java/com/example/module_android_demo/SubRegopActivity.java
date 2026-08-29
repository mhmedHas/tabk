package com.example.module_android_demo;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.Sheet;
import jxl.Workbook;

import com.function.CallbackBundle;
import com.tools.dlog;
import com.uhf.api.cls.R2000_calibration;
import com.uhf.api.cls.R2000_calibration.OEM_DATA;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class SubRegopActivity extends Activity {
	MyApplication myapp;
	Spinner spinner_regtype;
	ArrayAdapter<String> arradp_regtype;
	Button button_regr, button_regw,button_selectfile;
	ListView elist;
	EditText et_regadr,et_regc,et_regwadr,et_regwv;
	R2000_calibration r2000pcmd = new R2000_calibration();
	 static private int openfileDialogId = 0; 
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
	
	public void readExcelFile(String readfile) { 
		
		 Workbook book=null;
        try {
        	   int val=0;
	            short addr = 0;
	            long val2;
	            int regtype=0;
	            
             book = Workbook.getWorkbook(new File(readfile)); 
            dlog.toDlog(">>>>>>number of sheet "+book.getNumberOfSheets()); 
            // 获得第一个工作表对象            
            Sheet sheet = book.getSheet(0); 
            int Rows = sheet.getRows(); 
            int Cols = sheet.getColumns(); 
            dlog.toDlog("当前工作表的名字:" + sheet.getName()); 
            dlog.toDlog("总行数:" + Rows); 
            dlog.toDlog("总列数:" + Cols); 
            for (int i = 0; i < 1; ++i) { 
                for (int j = 0; j < 1; ++j) { 
                    if((sheet.getCell(j, i)).getContents().toLowerCase().contains("bypass"))
                    	regtype=2;
                    else if((sheet.getCell(j, i)).getContents().toLowerCase().contains("oem"))
                    	regtype=0;
                    else
                    	regtype=1;
                } 
                dlog.toDlog("\n");
            } 
            
            
            for (int i = 1; i < Rows; ++i) { 
            	
                for (int j = 0; j < Cols; ++j) { 
                    // getCell(Col,Row)获得单元格的值 
                	 dlog.toDlog((sheet.getCell(j, i)).getContents() + "\t"); 
                  
                	 if(j==0)
                		 addr=Short.parseShort((sheet.getCell(j, i)).getContents(),16);
                	 
                	 if(j==1)
                	 { val2=Short.parseShort((sheet.getCell(j, i)).getContents(),16);
                	   val=((int)val2)&0xffffffff;
                	 }
                	 
                } 
                
                R2000_calibration.R2000cmd rcmdo = null;
	            R2000_calibration.OEM_DATA r2000oem = null;
	            R2000_calibration.MAC_DATA r2000mac = null;

	            if (regtype == 0)
	            {
	                rcmdo = R2000_calibration.R2000cmd.OEMwrite;
	                r2000oem =r2000pcmd.new OEM_DATA(addr, val);

	            }
	            else if(regtype==1)
	            {  rcmdo = R2000_calibration.R2000cmd.writeMAC; 
	               r2000mac =r2000pcmd.new MAC_DATA(addr, val);
	            }
	            
	            if(regtype==2)
	            {
	                try
	                {
	                    
	                    byte[] data = new byte[5];
	                    data[0] = 0x07;
	                    data[1] = (byte)((addr & 0xff00) >> 8);
	                    data[2] = (byte)(addr & 0x00ff);
	                    data[3] = (byte)((val & 0xff00) >> 8);
	                    data[4] = (byte)(val & 0x00ff);
	                    byte[] senddata = r2000pcmd.GetSendCmd(R2000_calibration.R2000cmd.Regop, data);
	                    
	                    myapp.Mreader.DataTransportSend(senddata, senddata.length, 1000);
	                    
	    				byte[] part1 = new byte[3];
	    				byte[] part2=null;
	    				byte[] revdata=null;
	    				int re = myapp.Mreader.DataTransportRecv(part1, part1.length,
	    						1000);

	    				if (re == 0 && part1[2] != 0x00) {
	    					int l = (part1[1] & 0xff) + 4;
	    					part2 = new byte[l];
	    					revdata= new byte[l+3];
	    					System.arraycopy(part1, 0, revdata, 0, 3);
	    					 
	    					re = myapp.Mreader.DataTransportRecv(part2, part2.length,
	    							1000);
	    					if (re == 0)
	    						System.arraycopy(part2, 0, revdata, 3, part2.length);

	    				}
	    				else
	    					throw new Exception("收发指令失败");
	                }
	                catch (Exception ex)
	                {
	                	 throw ex;
	                }
	            }
	            else
	            {
	                try
	                {
	                    byte[] senddata = null;

	                    if (regtype == 0)
	                        senddata = r2000pcmd.GetSendCmd(rcmdo, r2000oem.ToByteData());
	                    else
	                        senddata = r2000pcmd.GetSendCmd(rcmdo, r2000mac.ToByteData());

	                    myapp.Mreader.DataTransportSend(senddata, senddata.length, 1000);
	                    
	    				byte[] part1 = new byte[3];
	    				byte[] part2=null;
	    				byte[] revdata=null;
	    				int re = myapp.Mreader.DataTransportRecv(part1, part1.length,
	    						1000);

	    				if (re == 0 && part1[2] != 0x00) {
	    					int l = (part1[1] & 0xff) + 4;
	    					part2 = new byte[l];
	    					revdata= new byte[l+3];
	    					System.arraycopy(part1, 0, revdata, 0, 3);
	    					 
	    					re = myapp.Mreader.DataTransportRecv(part2, part2.length,
	    							1000);
	    					if (re == 0)
	    						System.arraycopy(part2, 0, revdata, 3, part2.length);

	    				}
	    				else
	    					throw new Exception("收发指令失败");
	                }
	                catch (Exception ex)
	                {
	                	 throw ex;
	                }

	            }

                dlog.toDlog("\n");
            } 
           

        } catch (Exception e) { 
        	dlog.toDlog(e.getMessage());
        }
        
        if(book!=null)
        	 book.close();
        Toast.makeText(SubRegopActivity.this, "操作成功", Toast.LENGTH_SHORT)
		.show();
    }
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registerop);
		Application app = getApplication();
		myapp = (MyApplication) app;
		
		spinner_regtype = (Spinner) findViewById(R.id.spinner_regtype);
		arradp_regtype = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, MyApplication.regtype);
		spinner_regtype.setAdapter(arradp_regtype);
		
		button_regr = (Button) findViewById(R.id.button_regread);
		button_regw = (Button) findViewById(R.id.button_regwrite);
		button_selectfile = (Button) findViewById(R.id.button_fileread);
		
		elist = (ListView) this.findViewById(R.id.listView_reglist);
		
		et_regadr=(EditText)findViewById(R.id.editText_regadr);
		et_regc=(EditText)findViewById(R.id.editText_regrc);
		et_regwadr=(EditText)findViewById(R.id.editText_regwadr);
		et_regwv=(EditText)findViewById(R.id.editText_regwv);
		 
		button_selectfile.setOnClickListener(new View.OnClickListener(){

			@SuppressWarnings("deprecation")
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				showDialog(openfileDialogId);  
				
			}
 	 
		});
		
		button_regr.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (spinner_regtype.getSelectedItemPosition() == -1)
	            {
	                Toast.makeText(SubRegopActivity.this, "请选择类型", Toast.LENGTH_SHORT)
    				.show();
	                return;
	            }

	            if (et_regadr.getText().toString().isEmpty())
	            {
	                Toast.makeText(SubRegopActivity.this, "请输入读地址", Toast.LENGTH_SHORT)
    				.show();
	                return;
	            }

	            int count;
	            short addr;
	            

	            try
	            {
	                //Convert.ToInt32(hex, 16);
	                //addr == Convert.ToUInt16(raddresstextBox.Text);
	                if (et_regadr.getText().toString().length() > 4)
	                    throw new Exception("地址溢出");

	                addr = Short.parseShort(et_regadr.getText().toString(),16);
	                		 
	                count = Integer.parseInt(et_regc.getText().toString());
	                
	            }
	            catch (Exception ex)
	            {
	                Toast.makeText(SubRegopActivity.this, "请正确读次数和读地址:" + ex.getMessage(), Toast.LENGTH_SHORT)
    				.show();
	                return;
	            }
 
	            R2000_calibration.R2000cmd rcmdo=null;

	            R2000_calibration.OEM_DATA r2000oem = null;
	            R2000_calibration.MAC_DATA r2000mac = null;

	            if (spinner_regtype.getSelectedItemPosition() == 0)
	            {
	                rcmdo = R2000_calibration.R2000cmd.OEMread;
	                r2000oem = r2000pcmd.new OEM_DATA(addr);
	                for (int i = 1; i < count; i++)
	                {
	                    r2000oem.AddTo(++addr, 0);
	                }
	            }
	            else if (spinner_regtype.getSelectedItemPosition() == 1)
	            {
	                rcmdo = R2000_calibration.R2000cmd.readMAC; r2000mac =r2000pcmd.new MAC_DATA(addr);
	                for (int i = 1; i < count; i++)
	                {
	                    r2000mac.AddTo(++addr, 0);
	                }
	            }
	            
	            
	            List<Map<String, Object>> list = new  ArrayList<Map<String, Object>>();
	            Map<String, Object> map1 = new HashMap<String, Object>();
				map1.put("number","number");
				map1.put("address", "address");
				map1.put("value",  "value");
				list.add(map1);
	            if (spinner_regtype.getSelectedItemPosition() != 2)
	            {

	                try
	                {
	                    byte[] senddata = null;

	                    if (spinner_regtype.getSelectedItemPosition() == 0)
	                        senddata = r2000pcmd.GetSendCmd(rcmdo, r2000oem.ToByteData());
	                    else if (spinner_regtype.getSelectedItemPosition() == 1)
	                        senddata = r2000pcmd.GetSendCmd(rcmdo, r2000mac.ToByteData());
 
	                    myapp.Mreader.DataTransportSend(senddata, senddata.length, 1000);
	                    
	    				byte[] part1 = new byte[3];
	    				byte[] part2=null;
	    				byte[] revdata=null;
	    				int re = myapp.Mreader.DataTransportRecv(part1, part1.length,
	    						1000);

	    				if (re == 0 && part1[2] != 0x00) {
	    					
	    					int l = (part1[1] & 0xff) + 4;
	    					part2 = new byte[l];
	    					revdata= new byte[l+3];
	    					System.arraycopy(part1, 0, revdata, 0, 3);
	    					 
	    					re = myapp.Mreader.DataTransportRecv(part2, part2.length,
	    							1000);
	    					if (re == 0&&((part2[0]+part2[1])==0))
	    						System.arraycopy(part2, 0, revdata, 3, part2.length);
	    					else
	    					{
	    						 Toast.makeText(SubRegopActivity.this, "收发指令失败"+
	    								 String.format("%02x", part2[0])+String.format("%02x", part2[1]),
	    								 Toast.LENGTH_SHORT)
		    	    				.show();
		    		                return;
	    					}
	    				}
	    				else
	    				 {
	    		                Toast.makeText(SubRegopActivity.this, "收发指令失败", Toast.LENGTH_SHORT)
	    	    				.show();
	    		                return;
	    		            }
	    			 
	                    byte[] data = new byte[revdata.length - 19];
	                    System.arraycopy(revdata, 17, data, 0, data.length);
	                    R2000_calibration.OEM_DATA r2000data = r2000pcmd.new OEM_DATA(data);

	                    R2000_calibration.OEM_DATA.Adpair[] adp = r2000data.GetAddr();
	                    for (int i = 0; i < adp.length; i++)
	                    {
	                        Map<String, Object> map = new HashMap<String, Object>();
	        				map.put("number",String.valueOf(i+1));
	        				map.put("address", String.format("%04x", adp[i].addr));
	        				map.put("value",  String.format("%04x", adp[i].val));
	        				list.add(map);
	                    }
	                     
	                }
	                catch (Exception ex)
	                {
	                	  Toast.makeText(SubRegopActivity.this, "操作失败:" + ex.getMessage(), Toast.LENGTH_SHORT)
		    				.show();
	                    
	                    return;
	                }
	            }
	            else
	            {
	                try
	                {
	                    for (int i = 0; i < count;i++ )
	                    {
	                        short val = (short)(addr+i);
	                        byte[] data = new byte[3];
	                        data[0] = 0x07;
	                        data[1] = (byte)((val & 0xff00) >> 8);
	                        data[2] = (byte)(val & 0x00ff);
	                        byte[] senddata = r2000pcmd.GetSendCmd(R2000_calibration.R2000cmd.Regop, data);
	                        
	                        myapp.Mreader.DataTransportSend(senddata, senddata.length, 1000);
		                    
		    				byte[] part1 = new byte[3];
		    				byte[] part2=null;
		    				byte[] revdata=null;
		    				int re = myapp.Mreader.DataTransportRecv(part1, part1.length,
		    						1000);

		    				if (re == 0 && part1[2] != 0x00) {
		    					int l = (part1[1] & 0xff) + 4;
		    					part2 = new byte[l];
		    					revdata= new byte[l+3];
		    					System.arraycopy(part1, 0, revdata, 0, 3);
		    					
		    					re = myapp.Mreader.DataTransportRecv(part2, part2.length,
		    							1000);
		    					 
		    					if (re == 0&&((part2[0]+part2[1])==0))
		    						System.arraycopy(part2, 0, revdata, 3, part2.length);
		    					else
		    					{
		    						 Toast.makeText(SubRegopActivity.this, "收发指令失败"+
		    								 String.format("%02x", part2[0])+String.format("%02x", part2[1]),
		    								 Toast.LENGTH_SHORT)
			    	    				.show();
			    		                return;
		    					}
		    					
		    					 Map<String, Object> map = new HashMap<String, Object>();
			        				map.put("number",String.valueOf(i+1));
			        				map.put("address", String.format("%04x", addr+i));
			        				map.put("value",  String.format("%02x", revdata[revdata.length - 4])+String.format("%02x", revdata[revdata.length - 3]));
			        				list.add(map);
		    				}
		    				else
		    				 {
		    		                Toast.makeText(SubRegopActivity.this, "收发指令失败", Toast.LENGTH_SHORT)
		    	    				.show();
		    		                return;
		    		          }
	                    }
	                }
	                catch (Exception ex)
	                {
	                    Toast.makeText(SubRegopActivity.this, "操作失败:" + ex.getMessage(), Toast.LENGTH_SHORT)
	    				.show();
	                    return;
	                }
	 
	            }

	            Toast.makeText(SubRegopActivity.this, "操作成功", Toast.LENGTH_SHORT)
				.show();
	          
				SimpleAdapter adapter = new SimpleAdapter(SubRegopActivity.this,list,
						R.layout.listitemview_reg,
						new String[] { "number", "address", "value" }, new int[] {
								R.id.textView_regnumber, R.id.textView_regaddress,
								R.id.textView_regval });
				elist.setAdapter(adapter);
			}
			
		});
		
		button_regw.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if (spinner_regtype.getSelectedItemPosition() == -1)
	            {
	                Toast.makeText(SubRegopActivity.this, "请选择类型", Toast.LENGTH_SHORT)
    				.show();
	                return;
	            }

	            if (et_regwadr.getText().toString().isEmpty())
	            {
	                Toast.makeText(SubRegopActivity.this, "请输入写地址", Toast.LENGTH_SHORT)
    				.show();
	                return;
	            }


		            int val=0;
		            short addr;
		            long val2;
				try {
					if (et_regwadr.getText().toString().length() > 4)
						throw new Exception("地址溢出");

					addr = Short
							.parseShort(et_regwadr.getText().toString(), 16);

					val2 = (Long.parseLong(et_regwv.getText().toString(), 16));
					val=((int)val2)&0xffffffff;

				} catch (Exception ex) {
					Toast.makeText(SubRegopActivity.this,
							"请正确写数据和地址:" + ex.getMessage(), Toast.LENGTH_SHORT)
							.show();
					return;

				}

				R2000_calibration.R2000cmd rcmdo = null;

		            R2000_calibration.OEM_DATA r2000oem = null;
		            R2000_calibration.MAC_DATA r2000mac = null;

		            if (spinner_regtype.getSelectedItemPosition() == 0)
		            {
		                rcmdo = R2000_calibration.R2000cmd.OEMwrite;
		                r2000oem =r2000pcmd.new OEM_DATA(addr, val);

		            }
		            else if(spinner_regtype.getSelectedItemPosition()==1)
		            {
		                rcmdo = R2000_calibration.R2000cmd.writeMAC; r2000mac =r2000pcmd.new MAC_DATA(addr, val);

		            }

		            if(spinner_regtype.getSelectedItemPosition()==2)
		            {
		                try
		                {
		                    
		                    byte[] data = new byte[5];
		                    data[0] = 0x07;
		                    data[1] = (byte)((addr & 0xff00) >> 8);
		                    data[2] = (byte)(addr & 0x00ff);
		                    data[3] = (byte)((val & 0xff00) >> 8);
		                    data[4] = (byte)(val & 0x00ff);
		                    byte[] senddata = r2000pcmd.GetSendCmd(R2000_calibration.R2000cmd.Regop, data);
		                    
		                    myapp.Mreader.DataTransportSend(senddata, senddata.length, 1000);
		                    
		    				byte[] part1 = new byte[3];
		    				byte[] part2=null;
		    				byte[] revdata=null;
		    				int re = myapp.Mreader.DataTransportRecv(part1, part1.length,
		    						1000);

		    				if (re == 0 && part1[2] != 0x00) {
		    					int l = (part1[1] & 0xff) + 4;
		    					part2 = new byte[l];
		    					revdata= new byte[l+3];
		    					System.arraycopy(part1, 0, revdata, 0, 3);
		    					 
		    					re = myapp.Mreader.DataTransportRecv(part2, part2.length,
		    							1000);
		    					if (re == 0)
		    						System.arraycopy(part2, 0, revdata, 3, part2.length);

		    				}
		    				else
		    				 {
		    		                Toast.makeText(SubRegopActivity.this, "收发指令失败", Toast.LENGTH_SHORT)
		    	    				.show();
		    		                return;
		    		          }
		                }
		                catch (Exception ex)
		                {
		                	 Toast.makeText(SubRegopActivity.this, "操作失败:" + ex.getMessage(), Toast.LENGTH_SHORT)
			    				.show();
			                    return;
		                }
		            }
		            else
		            {
		                try
		                {
		                    byte[] senddata = null;

		                    if (spinner_regtype.getSelectedItemPosition() == 0)
		                        senddata = r2000pcmd.GetSendCmd(rcmdo, r2000oem.ToByteData());
		                    else
		                        senddata = r2000pcmd.GetSendCmd(rcmdo, r2000mac.ToByteData());

		                    myapp.Mreader.DataTransportSend(senddata, senddata.length, 1000);
		                    
		    				byte[] part1 = new byte[3];
		    				byte[] part2=null;
		    				byte[] revdata=null;
		    				int re = myapp.Mreader.DataTransportRecv(part1, part1.length,
		    						1000);

		    				if (re == 0 && part1[2] != 0x00) {
		    					int l = (part1[1] & 0xff) + 4;
		    					part2 = new byte[l];
		    					revdata= new byte[l+3];
		    					System.arraycopy(part1, 0, revdata, 0, 3);
		    					 
		    					re = myapp.Mreader.DataTransportRecv(part2, part2.length,
		    							1000);
		    					if (re == 0)
		    						System.arraycopy(part2, 0, revdata, 3, part2.length);

		    				}
		    				else
		    				 {
		    		                Toast.makeText(SubRegopActivity.this, "收发指令失败", Toast.LENGTH_SHORT)
		    	    				.show();
		    		                return;
		    		          }
		                }
		                catch (Exception ex)
		                {
		                	 Toast.makeText(SubRegopActivity.this, "操作失败:" + ex.getMessage(), Toast.LENGTH_SHORT)
			    				.show();
			                    return;
		                }

		            }
		            Toast.makeText(SubRegopActivity.this, "操作成功", Toast.LENGTH_SHORT)
					.show();
			}
			
		});
	}
	
	 public int GetOEM(short addr) throws Exception
     {
         try
         {
        	 //R2000_calibration r2000pcmd = new R2000_calibration();
             R2000_calibration.R2000cmd rcmdo =R2000_calibration.R2000cmd.OEMread;

             R2000_calibration.OEM_DATA r2000oem = null;
            
             r2000oem = new R2000_calibration().new OEM_DATA(addr);
             byte[] senddata = null;

             senddata = r2000pcmd.GetSendCmd(rcmdo, r2000oem.ToByteData());
             myapp.Mreader.DataTransportSend(senddata, senddata.length, 1000);
             byte[] part1 = new byte[3];
				byte[] part2=null;
				byte[] revdata=null;
				int re = myapp.Mreader.DataTransportRecv(part1, part1.length,
						1000);

				if (re == 0 && part1[2] != 0x00) {
					int l = (part1[1] & 0xff) + 4;
					part2 = new byte[l];
					revdata= new byte[l+3];
					System.arraycopy(part1, 0, revdata, 0, 3);
					 
					re = myapp.Mreader.DataTransportRecv(part2, part2.length,
							1000);
					if (re == 0)
						System.arraycopy(part2, 0, revdata, 3, part2.length);

				}
				else
		          throw new Exception("no recived part1");

             byte[] data = new byte[revdata.length - 19];
             System.arraycopy(revdata, 17, data, 0, data.length);
             R2000_calibration.OEM_DATA r2000data = new R2000_calibration().new OEM_DATA(data);

             R2000_calibration.OEM_DATA.Adpair[] adp = r2000data.GetAddr();

             return adp[0].val;
         }
         catch (Exception ex)
         {
             throw ex;
         }
     }
     public int GetMAC(short addr) throws Exception
     {
         try
         {
        	 //R2000_calibration r2000pcmd = new R2000_calibration();
             R2000_calibration.R2000cmd rcmdo = R2000_calibration.R2000cmd.readMAC;

             R2000_calibration.MAC_DATA r2000mac = null;
             
             r2000mac = new R2000_calibration().new MAC_DATA(addr);
             byte[] senddata = null;

             senddata = r2000pcmd.GetSendCmd(rcmdo, r2000mac.ToByteData());
             myapp.Mreader.DataTransportSend(senddata, senddata.length, 1000);
             byte[] part1 = new byte[3];
				byte[] part2=null;
				byte[] revdata=null;
				int re = myapp.Mreader.DataTransportRecv(part1, part1.length,
						1000);

				if (re == 0 && part1[2] != 0x00) {
					int l = (part1[1] & 0xff) + 4;
					part2 = new byte[l];
					revdata= new byte[l+3];
					System.arraycopy(part1, 0, revdata, 0, 3);
					 
					re = myapp.Mreader.DataTransportRecv(part2, part2.length,
							1000);
					if (re == 0)
						System.arraycopy(part2, 0, revdata, 3, part2.length);

				}
				else
		          throw new Exception("no recived part1");

             byte[] data = new byte[revdata.length - 19];
             System.arraycopy(revdata, 17, data, 0, data.length);
             R2000_calibration.MAC_DATA r2000data = new R2000_calibration().new MAC_DATA(data);

             R2000_calibration.MAC_DATA.Adpair[] adp = r2000data.GetAddr();

             return adp[0].val;
         }
         catch (Exception ex)
         {
             throw ex;

         }
     }
     public void SetOEM(short addr, int val) throws Exception
     {
         try
         {
        	 //R2000_calibration r2000pcmd = new R2000_calibration();
             R2000_calibration.R2000cmd rcmdo = R2000_calibration.R2000cmd.OEMwrite;

             R2000_calibration.OEM_DATA r2000oem = null;
 
             r2000oem = new R2000_calibration().new OEM_DATA(addr, val);

             byte[] senddata = r2000pcmd.GetSendCmd(rcmdo, r2000oem.ToByteData());


             myapp.Mreader.DataTransportSend(senddata, senddata.length, 1000);
             byte[] part1 = new byte[3];
				byte[] part2=null;
				byte[] revdata=null;
				int re = myapp.Mreader.DataTransportRecv(part1, part1.length,
						1000);

				if (re == 0 && part1[2] != 0x00) {
					int l = (part1[1] & 0xff) + 4;
					part2 = new byte[l];
					revdata= new byte[l+3];
					System.arraycopy(part1, 0, revdata, 0, 3);
					 
					re = myapp.Mreader.DataTransportRecv(part2, part2.length,
							1000);
					if (re == 0)
						System.arraycopy(part2, 0, revdata, 3, part2.length);

				}
				else
		          throw new Exception("no recived part1");

             byte[] data = new byte[revdata.length - 19];
             System.arraycopy(revdata, 17, data, 0, data.length);
             if (revdata[3] + revdata[4] != 0)
             {
            	 byte[] out=new byte[]{revdata[3],revdata[4]};
                 throw new Exception("S1:" +myapp.Mreader.bytes_Hexstr(out));
             }
         }
         catch (Exception ex)
         {
             throw ex;
         }

     }

     public void SetMAC(short addr, int val) throws Exception
     {
         try
         {
             //R2000_calibration r2000pcmd = new R2000_calibration();
             R2000_calibration.R2000cmd rcmdo = R2000_calibration.R2000cmd.writeMAC;

             R2000_calibration.MAC_DATA r2000mac = null;
 
             r2000mac = new R2000_calibration().new MAC_DATA(addr, val);

             byte[] senddata = r2000pcmd.GetSendCmd(rcmdo, r2000mac.ToByteData());
             myapp.Mreader.DataTransportSend(senddata, senddata.length, 1000);
             byte[] part1 = new byte[3];
				byte[] part2=null;
				byte[] revdata=null;
				int re = myapp.Mreader.DataTransportRecv(part1, part1.length,
						1000);

				if (re == 0 && part1[2] != 0x00) {
					int l = (part1[1] & 0xff) + 4;
					part2 = new byte[l];
					revdata= new byte[l+3];
					System.arraycopy(part1, 0, revdata, 0, 3);
					 
					re = myapp.Mreader.DataTransportRecv(part2, part2.length,
							1000);
					if (re == 0)
						System.arraycopy(part2, 0, revdata, 3, part2.length);

				}
				else
		          throw new Exception("no recived part1");

             if (revdata[3] + revdata[4] != 0)
             {
            	 byte[] out=new byte[]{revdata[3],revdata[4]};
                 throw new Exception("S1:" +myapp.Mreader.bytes_Hexstr(out));
             }
         }
         catch (Exception ex)
         {
             throw ex;
         }
     }

     public void SetBypass(short addr, int val) throws Exception
     {
         try
         {
        	 //R2000_calibration r2000pcmd = new R2000_calibration();
             byte[] data = new byte[5];
             data[0] = 0x07;
             data[1] = (byte)((addr & 0xff00) >> 8);
             data[2] = (byte)(addr & 0x00ff);
             data[3] = (byte)((val & 0xff00) >> 8);
             data[4] = (byte)(val & 0x00ff);
             byte[] senddata = r2000pcmd.GetSendCmd(R2000_calibration.R2000cmd.Regop, data);
             myapp.Mreader.DataTransportSend(senddata, senddata.length, 1000);
             byte[] part1 = new byte[3];
				byte[] part2=null;
				byte[] revdata=null;
				int re = myapp.Mreader.DataTransportRecv(part1, part1.length,
						1000);

				if (re == 0 && part1[2] != 0x00) {
					int l = (part1[1] & 0xff) + 4;
					part2 = new byte[l];
					revdata= new byte[l+3];
					System.arraycopy(part1, 0, revdata, 0, 3);
					 
					re = myapp.Mreader.DataTransportRecv(part2, part2.length,
							1000);
					if (re == 0)
						System.arraycopy(part2, 0, revdata, 3, part2.length);

				}
				else
		          throw new Exception("no recived part1");

				  if (revdata[3] + revdata[4] != 0)
		             {
					     byte[] out=new byte[]{revdata[3],revdata[4]};
		                 throw new Exception("S1:" +myapp.Mreader.bytes_Hexstr(out));
		             }
         }
         catch (Exception ex)
         {
             throw ex;
         }
     }
	
	@Override  
	   protected Dialog onCreateDialog(int id) {  
	        if(id==openfileDialogId){  
	            Map<String, Integer> images = new HashMap<String, Integer>();  
	           // 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹   
	           images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);   // 根目录图标   
            images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);    //返回上一层的图标   
	           images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);   //文件夹图标   
	           images.put("csv", R.drawable.filedialog_file);  
	           images.put("xlsx", R.drawable.filedialog_file);
	           images.put("xls", R.drawable.filedialog_file);  
	           images.put("txt", R.drawable.filedialog_file);  
	          //images.put("wav", R.drawable.filedialog_wavfile);   //wav文件图标   //wav文件图标   
	            images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);  
	           Dialog dialog = OpenFileDialog.createDialog(id, this, "打开文件", new CallbackBundle() {  
	                @Override  
	              public void callback(Bundle bundle) {  
	                  String filepath = bundle.getString("path");  
	                  readExcelFile(filepath);
	                }  
             },   
	           null,  
	          images);  
	        return dialog;  
	      }  
	     return null;  
	   }  
}
