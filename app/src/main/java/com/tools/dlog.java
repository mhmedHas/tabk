package com.tools;

import android.util.Log;

/**
 * Created by Administrator on 2017/11/7.
 */

public class dlog {
	public static void toDlog(String mess) {
		Log.d("MYINFO", mess);
	}
	
	public static void toDlogAPI(String mess) {
		Log.d("ModuleAPI", mess);
	}
}
