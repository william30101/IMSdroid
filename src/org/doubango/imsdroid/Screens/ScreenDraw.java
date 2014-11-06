package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class ScreenDraw {

	private Context contextDraw;
	private ViewGroup viewGroup;
	private LayoutParams params;
	private Bitmap risehead, headup, bowhead, extendhead, shortenhead;
	private int center_x, center_y, bitmapWidth, bitmapHeight;
	
	private DrawCanvas draw;
	Paint paint;
	
	/* test */
	private ScaleGestureDetector mScaleDetector;
	private float mScaleFactor = 1.f;
	


	public ScreenDraw(Context context, ViewGroup viewGroup){
		contextDraw = context;
		
		risehead    = BitmapFactory.decodeResource(contextDraw.getResources(), R.drawable.xyzrisehead);
		headup      = BitmapFactory.decodeResource(contextDraw.getResources(), R.drawable.xyznormal);
		bowhead     = BitmapFactory.decodeResource(contextDraw.getResources(), R.drawable.xyzbowhead);
		extendhead  = BitmapFactory.decodeResource(contextDraw.getResources(), R.drawable.xyzlong);
		shortenhead = BitmapFactory.decodeResource(contextDraw.getResources(), R.drawable.xyzshort);
	
		draw = new DrawCanvas(contextDraw);
		paint = new Paint();
		
		this.viewGroup = viewGroup;
		viewGroup.setBackgroundResource(R.drawable.xyzrobot);
		params = viewGroup.getLayoutParams();
		
	
	}
		
	public void setFrameSize(int width, int height){
		params.width = width;
		params.height = height;	
		center_x = params.width / 2;
		center_y = params.height / 2;
	}
	
	public void setBitmapSize(int width, int height){
		risehead = Bitmap.createScaledBitmap(risehead, width, height, false);
		headup = Bitmap.createScaledBitmap(headup, width, height, false);
		bowhead = Bitmap.createScaledBitmap(bowhead, width, height, false);
		extendhead = Bitmap.createScaledBitmap(extendhead, width, height, false);
		shortenhead = Bitmap.createScaledBitmap(shortenhead, width, height, false);

		bitmapWidth  = risehead.getWidth();
		bitmapHeight = risehead.getHeight();
	}
	
	public void drawBitmap(){
		draw.position(center_x, center_y);
		draw();
	}
	
	public void setBitmapDecode(int draw_id){
		risehead = BitmapFactory.decodeResource(contextDraw.getResources(), draw_id);
	}
	
	private void draw(){
		try{
			viewGroup.removeView(draw);
		} catch(Exception e){}
		viewGroup.addView(draw);
	}
	
	/* DrawCanvas Class */
	private class DrawCanvas extends View{
		float x, y;
		Bitmap onDrawbitmap;

		public DrawCanvas(Context mContext) {
			super(mContext);
			// TODO Auto-generated constructor stub
		}
		
		public void onDraw(Canvas canvas){
			canvas.drawBitmap(risehead, x, y, paint);
			canvas.drawBitmap(headup, x, y, paint);
			canvas.drawBitmap(bowhead, x, y, paint);
			canvas.drawBitmap(extendhead, x, y, paint);
			canvas.drawBitmap(shortenhead, x, y, paint);

		}
		
		private void position(float pos_x, float pos_y){
			x = pos_x  - (bitmapWidth / 2);
			y = pos_y  - (bitmapHeight / 2);
		}
		
	}


}
