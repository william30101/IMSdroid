package org.doubango.imsdroid.Screens;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;


public class ScreenUIJoyStick{
	public static final int STICK_NONE = 0;
	public static final int STICK_UP = 1;
	public static final int STICK_UPRIGHT = 2;
	public static final int STICK_RIGHT = 3;
	public static final int STICK_DOWNRIGHT = 4;
	public static final int STICK_DOWN = 5;
	public static final int STICK_DOWNLEFT = 6;
	public static final int STICK_LEFT = 7;
	public static final int STICK_UPLEFT = 8;
	
	private int STICK_ALPHA = 200;
	private int LAYOUT_ALPHA = 200;
	private int OFFSET = 0;
	
	private Context mContext;
	private ViewGroup mLayout;
	private LayoutParams params;
	private int stick_width, stick_height;
	private int stick_center_x, stick_center_y;
	
	private int position_x = 0, position_y = 0, min_distance = 0;
	private float distance = 0, angle = 0;
	
	private DrawCanvas draw;
	private Paint paint;
	private Bitmap stick;

	private boolean touch_state = false;

	private float ScreenX;
	private float ScreenY;
	
	public ScreenUIJoyStick(Context context, ViewGroup layout, int stick_res_id){
		mContext = context;
		
		stick = BitmapFactory.decodeResource(mContext.getResources(), stick_res_id);
		
		stick_width = stick.getWidth();
		stick_height = stick.getHeight();
		
		draw = new DrawCanvas(mContext);
		paint = new Paint();
		
		mLayout = layout;
		params = mLayout.getLayoutParams();
	}
	
	/* DrawCanvas Class */
	private class DrawCanvas extends View{
		float x, y;

		public DrawCanvas(Context mContext) {
			super(mContext);
			// TODO Auto-generated constructor stub
		}
		
		public void onDraw(Canvas canvas){
			canvas.drawBitmap(stick, x, y, paint);
		}
		
		private void position(float pos_x, float pos_y){
			x = pos_x - (stick_width / 2);
			y = pos_y - (stick_height / 2);
		}

	}	
	
	private void draw(){
		try{
			mLayout.removeView(draw);
		} catch(Exception e){}
		mLayout.addView(draw);
	}
	
	

	/* Set Sitck size & get Stick size */
	public void setStickSize(int width, int height){
		stick = Bitmap.createScaledBitmap(stick, width, height, false);
		stick_width  = stick.getWidth();
		stick_height = stick.getHeight();
	}
	
	public void setStickWidth(int width) {
        stick = Bitmap.createScaledBitmap(stick, width, stick_height, false);
        stick_width = stick.getWidth();
	}
	
	public void setStickHeight(int height) {
        stick = Bitmap.createScaledBitmap(stick, stick_width, height, false);
        stick_height = stick.getHeight();
	}
	
	public int getStickWidth() {
		return stick_width;
	}
	
	public int getStickHeight() {
		return stick_height;
	}
	
	private void setStickCenter(int width, int height){
		stick_center_x = width / 2;
		stick_center_y = height / 2;
	}
	
	/* Set Layout size & get layout size */
	public void setLayoutSize(int width, int height){
		params.width = width;
		params.height = height;	
		
		/* Call setStickCenter to set center */
		setStickCenter(width, height);
	}
	
	public int getLayoutWidth(){
		return params.width;
	}
	
	public int getLayoutHeight(){
		return params.height;
	}
	
	
	/* Set Alpha for Stick or Layout */
	public void setStickAlpha(int alpha){
		STICK_ALPHA = alpha;
		paint.setAlpha(alpha);
	}
	
	public int getStickAlpha(){
		return STICK_ALPHA;
	}
	
	public void setLayoutAlpha(int alpha){
		LAYOUT_ALPHA = alpha;
		mLayout.getBackground().setAlpha(alpha);
	}
	
	public int getLayoutAlpha(){
		return 	LAYOUT_ALPHA;
	}
	
	/* set Offset function */ 
	public void setoffset(int offset){
		OFFSET = offset;
	}
	
	public int getoffset(){
		return OFFSET;
	}

	/* Set Distance */
	public void setMinimumDistance(int minDistance){
		min_distance = minDistance;
	}
	
	public int getMinimunDistance(){
		return min_distance;
	}

	/* Implement Method */
	public void drawStickDefault(){
		draw.position(stick_center_x, stick_center_y);
		draw();
	}
	
	public void drawStick(MotionEvent arg1){
		/* Test */
		ScreenX = arg1.getX();
		ScreenY = arg1.getY();
		
		
		/* Method */
		position_x = (int) (arg1.getX() - (params.width / 2));
		position_y = (int) (arg1.getY() - (params.height / 2));
		distance = (float) Math.sqrt(Math.pow(position_x, 2) + Math.pow(position_y, 2));
		angle = (float) cal_angle(position_x, position_y);
		
		if(arg1.getAction() == MotionEvent.ACTION_DOWN){
			if( distance <= (params.width / 2) - OFFSET){
				draw.position(arg1.getX(), arg1.getY());
				draw();
				touch_state = true;
			}
		}
		else if(arg1.getAction() == MotionEvent.ACTION_MOVE && touch_state){
			if(distance <= (params.width / 2) - OFFSET){
				draw.position(arg1.getX(), arg1.getY());
				draw();
			}
			else if(distance > (params.width / 2) - OFFSET){
                float x = (float) (Math.cos(Math.toRadians(cal_angle(position_x, position_y))) * ((params.width / 2) - OFFSET));
                float y = (float) (Math.sin(Math.toRadians(cal_angle(position_x, position_y))) * ((params.height / 2) - OFFSET));
                x += (params.width / 2);
                y += (params.height / 2);
                draw.position(x, y);
                draw();
			}
			else {
				mLayout.removeView(draw);
			}
		}
		else if(arg1.getAction() == MotionEvent.ACTION_UP){
			mLayout.removeView(draw);
			drawStickDefault();
			touch_state = false;
		}
		
	}
	
	private double cal_angle(float x, float y){
		if(x >= 0 && y >= 0)
			return Math.toDegrees(Math.atan(y / x));
		else if (x < 0 && y >= 0)
			return Math.toDegrees(Math.atan(y / x)) + 180;
		else if (x < 0 && y < 0 )
			return Math.toDegrees(Math.atan(y / x)) + 180;
		else if (x >= 0 && y < 0)
			return Math.toDegrees(Math.atan(y / x)) + 360;					
		return 0;
	}
	
	
	public int get8Direction(){
		if(distance > min_distance && touch_state){
		     if(angle >= 247.5 && angle < 292.5 ) {
	                return STICK_UP;
	            } else if(angle >= 292.5 && angle < 337.5 ) {
	                return STICK_UPRIGHT;
	            } else if(angle >= 337.5 || angle < 22.5 ) {
	                return STICK_RIGHT;
	            } else if(angle >= 22.5 && angle < 67.5 ) {
	                return STICK_DOWNRIGHT;
	            } else if(angle >= 67.5 && angle < 112.5 ) {
	                return STICK_DOWN;
	            } else if(angle >= 112.5 && angle < 157.5 ) {
	                return STICK_DOWNLEFT;
	            } else if(angle >= 157.5 && angle < 202.5 ) {
	                return STICK_LEFT;
	            } else if(angle >= 202.5 && angle < 247.5 ) {
	                return STICK_UPLEFT;
	            }   
		}
		else if(distance <= min_distance && touch_state){
			return STICK_NONE;
		}
		return 0;
	}

	public int getX() {
		// TODO Auto-generated method stub
		if(distance > min_distance && touch_state){
			return position_x;
		}
		return 0;
	}
	public int getY() {
		// TODO Auto-generated method stub
		if(distance > min_distance && touch_state){
			return position_y;
		}
		return 0;
	}
	
	public float getAngle() {
		// TODO Auto-generated method stub
		if(distance > min_distance && touch_state){
			return angle;
		}
		return 0;
	}

	public float getDistance() {
		// TODO Auto-generated method stub
		if(distance > min_distance && touch_state){
			return distance;
		}
		return 0;
	}

	public float getScreenX() {
		if(distance > min_distance && touch_state){
			return ScreenX;
		}
		return 0;
	}
	
	public float getScreenY(){
		if(distance > min_distance && touch_state){
			return ScreenY;
		}
		return 0;	
	}


}
