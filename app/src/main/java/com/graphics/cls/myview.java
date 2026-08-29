package com.graphics.cls;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.tools.dlog;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义 view Created by Administrator on 2017/11/8.
 */

public class myview extends SurfaceView implements SurfaceHolder.Callback {
	class interdrawThread extends Thread {
		private Canvas mycanvs;
		private boolean mRun = false;
		private SurfaceHolder mSurfaceHolder;
		private final Object mRunLock = new Object();

		public interdrawThread(SurfaceHolder surfaceHolder, Context context,
				Handler handler) {
			// get handles to some important objects
			mSurfaceHolder = surfaceHolder;

		}

		@Override
		public void run() {

			while (mRun) {

				synchronized (this) {
					if (optype == 0) {
						try {
							Thread.sleep(10);
						} catch (Exception ex) {
						}
						continue;
					}
					Canvas c = null;
					try {
						c = mSurfaceHolder.lockCanvas();
						if (mycanvs == null) {
							pbitmap = Bitmap.createBitmap(c.getWidth(),
									c.getHeight(), Bitmap.Config.ARGB_8888);

							mycanvs = new Canvas();
							mycanvs.setBitmap(pbitmap);
						}

						synchronized (mSurfaceHolder) {
							// Critical section. Do not allow mRun to be set
							// false until
							// we are sure all canvas draw operations are
							// complete.

							// If mRun has been toggled false, inhibit canvas
							// operations.
							synchronized (mRunLock) {
								if (mRun) {
									switch (optype) {
									case 1:
										//
										cdi.DrawCordi(mycanvs);

										break;
									case 2:

										cdi.DrawPointnline(mycanvs,
												(Integer) lob.get(0),
												(Integer) lob.get(1));
										break;
									case 3:
										// dlog.toDlog("drawtext 3");
										cdi.DrawText(mycanvs,
												(Integer) lob.get(0),
												(Integer) lob.get(1),
												(Integer) lob.get(2),
												(String) lob.get(3));
										break;
									case 4:

										cdi.DrawLine(mycanvs,
												(Integer) lob.get(0),
												(Integer) lob.get(1),
												(Integer) lob.get(2),
												(Integer) lob.get(3));
										break;
									case 5:
										cdi.DrawColor(mycanvs);

										break;
									}

									// dlog.toDlog("save:"+c.save());
								}
								;

							}
						}
					} finally {
						// do this in a finally so that if an exception is
						// thrown
						// during the above, we don't leave the Surface in an
						// inconsistent state
						if (c != null) {
							c.drawBitmap(pbitmap, 0, 0, null);
							mSurfaceHolder.unlockCanvasAndPost(c);

						}
						optype = 0;

					}
				}
			}
			dlog.toDlog("thread finish");
		}

		public void setRunning(boolean b) {
			// Do not allow mRun to be modified while any canvas operations
			// are potentially in-flight. See doDraw().
			synchronized (mRunLock) {
				mRun = b;
			}
		}

	}

	// int st_x,st_y,ed_x,ed_y;
	Coordinate cdi;
	int optype;
	List<Object> lob;
	Canvas pcanvas;
	Bitmap pbitmap;
	interdrawThread inthread;
	Context pcontext;

	public myview(Context context, AttributeSet attrs) {
		super(context, attrs);
		lob = new ArrayList<Object>();

		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		pcontext = context;
		if (cdi == null)
			cdi = new Coordinate();
	}

	private void waiting(int sleep) {
		while (optype != 0) {
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void drawCodi() {
		waiting(10);
		// dlog.toDlog("optype1");
		optype = 1;
	}

	public void drawClear() {
		waiting(10);
		// dlog.toDlog("optype1");
		optype = 5;
	}

	public void drawPointandline(int x, int y) {
		waiting(10);
		// dlog.toDlog("optype2");
		optype = 2;
		lob.clear();
		lob.add(x);
		lob.add(y);

	}

	public void drawLine(int lx1, int ly1, int lx2, int ly2) {
		waiting(10);
		// dlog.toDlog("optype4");
		optype = 4;
		lob.clear();
		lob.add(lx1);
		lob.add(ly1);
		lob.add(lx2);
		lob.add(ly2);

	}

	public void drawText(int x, int y, int size, String text) {
		waiting(10);
		// dlog.toDlog("optype3");
		optype = 3;
		lob.clear();
		lob.add(x);
		lob.add(y);
		lob.add(size);
		lob.add(text);

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		inthread = new interdrawThread(holder, pcontext, new Handler() {
			@Override
			public void handleMessage(Message m) {

			}
		});
		if (!inthread.mRun) {
			inthread.setRunning(true);

			inthread.start();
		}

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

		inthread.setRunning(false);
		try {
			inthread.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

}
