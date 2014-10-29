package org.doubango.imsdroid.Screens;

import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class ScreenComponent{

	int Screen_width, Screen_height;
	int horizontalscope, verticalscope;	
	
	
	public void setScreenSize(int Screen_width, int Screen_height){
		this.Screen_width  = Screen_width;
		this.Screen_height = Screen_height;
	}

	
	public void setComponentPosition(ImageButton btn, int GAP, int horizontalGAP, int verticalGAP){
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		horizontalscope = Screen_width / GAP; 
		verticalscope = Screen_height / GAP;		
		layoutParams.setMargins(horizontalscope * horizontalGAP, verticalscope * 6, 0, 0);
		btn.setLayoutParams(layoutParams);
		//Log.i("shinhua","horizontalscope = " + horizontalscope + " verticalscope = " + verticalscope);
	}
	
	

	
	public void setComponentPosition(EditText text, int GAP, int horizontalGAP, int verticalGAP){
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		horizontalscope = Screen_width / GAP; 
		verticalscope = Screen_height / GAP;		
		layoutParams.setMargins(horizontalscope * horizontalGAP, verticalscope * verticalGAP, 0, 0);
		text.setLayoutParams(layoutParams);
	}


	
	
	
	
}
