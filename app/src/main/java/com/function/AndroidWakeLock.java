package com.function;

import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

/**
 * 唤醒作用类
 * 
 * @author Administrator
 * 
 */
public class AndroidWakeLock {
	WakeLock wakelock;
	PowerManager pmr;

	public AndroidWakeLock(PowerManager pm) {

		// PowerManager pm = (PowerManager)
		// getSystemService(Context.POWER_SERVICE);
		pmr = pm;
	}

	public void WakeLock() {
		if (wakelock == null) {
			wakelock = pmr.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, this
					.getClass().getCanonicalName());
		}
		wakelock.acquire();

	}

	public void WakeLock_v1() {
		if (wakelock == null) {
			wakelock = pmr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this
					.getClass().getCanonicalName());
		}
		wakelock.acquire();


	}

	public void ReleaseWakeLock() {
		if (wakelock != null && wakelock.isHeld()) {
			wakelock.release();
			wakelock = null;
		}
	}
}
