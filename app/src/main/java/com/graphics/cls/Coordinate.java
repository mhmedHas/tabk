package com.graphics.cls;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by Administrator on 2017/11/9.
 */

public class Coordinate {

	private int scale_x;// 鍒诲害闂磋窛 鎸囦袱涓埢搴︽爣鐨勮窛
    private int scale_xh;// 鍒诲害鏍囬暱

	private int scale_y;// 鍒诲害闂磋窛 鎸囦袱涓埢搴︽爣鐨勮窛绂?
    private int scale_yh;// 鍒诲害鏍囬暱

	private int axis_x;// 鍧愭爣杞磝闀垮害

	private int axis_y;// 鍧愭爣杞磞闀垮害

	private int cordi_x;// 涓績杞磝
	private int cordi_y;// 涓績杞磞-

	private int psize;
	private int pcolor;

	private int lastp_x;// 涓婁竴涓潗鏍囪酱x
	private int lastp_y;// 涓婁竴涓潗鏍囪酱y
	int cc;

	public Coordinate() {

		scale_xh = 10;
		scale_x = 80;

		scale_y = 40;
		scale_yh = 10;

		axis_x = 1200;
		axis_y = 600;

		cordi_x = 150;
		cordi_y = 950;

		psize = 6;
		pcolor = Color.BLUE;

		lastp_x = -1;
		lastp_y = -1;
		cc = 0;
	}

	public void Set_scale(int x, int xh, int y, int yh) {
		scale_xh = xh;
		scale_x = x;

		scale_y = y;
		scale_yh = yh;
	}

	public void Set_axis(int x, int y) {
		axis_x = x;
		axis_y = y;
	}

	public void Set_cordi(int x, int y) {
		cordi_x = x;
		cordi_y = y;
	}

	public void Set_sizecolor(int size, int color) {
		psize = size;
		pcolor = color;
	}

	public void DrawCordi(Canvas canvas) {

		Paint pt = new Paint();
		pt.setColor(pcolor);
		pt.setStrokeWidth(psize);

		canvas.drawLine(cordi_x, cordi_y, cordi_x, cordi_y - axis_y, pt);
		canvas.drawLine(cordi_x, cordi_y, cordi_x + axis_x, cordi_y, pt);

		pt.setStrokeWidth(psize > 2 ? psize - 2 : psize);
		int yl = axis_y, yc = cordi_y;
		while (yl >= scale_y) {
			canvas.drawLine(cordi_x, yc - scale_y, cordi_x + scale_xh, yc
					- scale_y, pt);
			yl -= scale_y;
			yc -= scale_y;
		}

		int xc = cordi_x;
		while (xc + scale_x <= axis_x + cordi_x) {
			canvas.drawLine(xc + scale_x, cordi_y, xc + scale_x, cordi_y
					- scale_yh, pt);
			xc += scale_x;
		}

	}

	public void DrawText(Canvas canvas, int x, int y, int size, String text) {

		Paint pt2 = new Paint();
		pt2.setColor(Color.WHITE);
		pt2.setTextSize(psize + size);

		canvas.drawText(text, x, y, pt2);
	}

	public void DrawPointnline(Canvas canvas, int x, int y) {
		cc++;
		Paint pt = new Paint();
		pt.setColor(pcolor);
		pt.setStrokeWidth(psize - 2);
		canvas.drawPoint(x, y, pt);

		Paint pt2 = new Paint();
		pt2.setColor(Color.RED);
		pt2.setStrokeWidth(psize + 4);
		// canvas.drawText(String.valueOf(cc),x,y,pt2);
		if (lastp_x != -1 && lastp_y != -1) {
			canvas.drawLine(lastp_x, lastp_y, x, y, pt);

		}

		lastp_y = y;
		lastp_x = x;

	}

	public void DrawLine(Canvas canvas, int x, int y, int x2, int y2) {

		Paint pt = new Paint();
		pt.setColor(pcolor);
		pt.setStrokeWidth(psize - 1);
		canvas.drawLine(x, y, x2, y2, pt);
	}

	public void DrawColor(Canvas mycanvs) {
		// TODO Auto-generated method stub
		mycanvs.drawColor(Color.BLACK);
	}
}
