package com.zguang.api.comm;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class linuxcomm {
	static
	{
		System.loadLibrary("Android_commdkg");/* 加载JNI库 */
	  
	}
	
	public native int OpenComm(String comm,int baudrate);
	private native FileDescriptor GetCommFd();
	public native int CloseComm();
	private native void CloseCommFd();
	public native int SetParity(int databits,int stopbits,int parity);
	public native int SetRTimeout(int readtimeout);
	public native int SetWTimeout(int writetimeout);
	public native int RevDatac(char[] readbuffer,int readcount,int[] hreadcount);
	public native int RevDatab(byte[] readbuffer,int readcount,int[] hreadcount);
	public native int RevData_nonblockc(char[] readbuffer,int[] hreadcount);
	public native int RevData_nonblockb(byte[] readbuffer,int[] hreadcount);
	public native int SendDatac(char[] readbuffer,int writecount);
	public native int SendDatab(byte[] readbuffer,int writecount);
	
	
	private FileDescriptor mFd;
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;
	
	 public void InitStream()
	 {
		 mFd=GetCommFd();
		 mFileInputStream=new FileInputStream(mFd);
		 mFileOutputStream=new FileOutputStream(mFd);
	 }
	 
	 public void CloseStream()
	 {
		 if(mFileInputStream!=null)
			try {
				mFileInputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 if(mFileOutputStream!=null)
			try {
				mFileOutputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 
		 if(mFd!=null)
			 CloseCommFd();
		 
	 }
	
	// Getters and setters
		public InputStream getInputStream() {
			
			return mFileInputStream;
		}

		public OutputStream getOutputStream() {
			
			return mFileOutputStream;
		}
}
