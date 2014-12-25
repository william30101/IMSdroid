package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.XMPPSetting;
import org.doubango.imsdroid.Utils.NetworkStatus;
import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnSipSession.ConnectionState;
import org.doubango.ngn.utils.NgnConfigurationEntry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class ScreenXYZLogin extends BaseScreen {
	
	private static String TAG = ScreenXYZLogin.class.getCanonicalName();
	
	private final INgnConfigurationService mConfigurationService;
	private final INgnSipService mSipService;
	private BroadcastReceiver mSipBroadCastRecv;
	
	private Button enterBtn, cencelBtn;
	
	/* Add new layout declare */
	private EditText editTextUsername, editTextPassword;
	private int width, height;
	
	private NetworkStatus loggin;

	
	private String xmppUsername, xmppPassword; 	//For XMPP thread user name & password
	private XMPPSetting XMPPSet;
	public Thread XMPPThreadv = new XMPPThread(); 

	/* Maybe it can be remove */
	private EditText editTextRealm, editTextImpi,  editTextImpu;
	private CheckBox checkBoxEarlyIMS;
	

	int horizontalscope, verticalscope;	
	
	public ScreenXYZLogin() {
		super(SCREEN_TYPE.HOME_T, TAG);
		mSipService = getEngine().getSipService();
		this.mConfigurationService = getEngine().getConfigurationService();
		
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("shinhua","shinhua oncreate screen login here");
		setContentView(R.layout.xyzloginpage);
		setScreenBackground();
		getScreenSize();

		enterBtn  = (Button)findViewById(R.id.loginenter);
		cencelBtn = (Button)findViewById(R.id.logincencel);

		enterBtn.setOnClickListener(ClickListener);
		cencelBtn.setOnClickListener(ClickListener);

		editTextUsername = (EditText)findViewById(R.id.editText_username);
		
		// this function is not good, it needs fix
//		setComponentPosition(editTextUsername, 2, 1, 1);	// Set screen 
		
		editTextPassword = (EditText)findViewById(R.id.editText_password);
		
		/* Maybe below can be remove*/ 
		editTextRealm = (EditText)findViewById(R.id.editText_realm);
		editTextImpi  = (EditText)findViewById(R.id.editText_impi);
		editTextImpu  = (EditText)findViewById(R.id.editText_impu);
		checkBoxEarlyIMS = (CheckBox)findViewById(R.id.checkBox_earlyIMS);
		/*---------------------------*/
			
		editTextUsername.setText(mConfigurationService.getString(
				NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
				NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME));
		editTextImpu.setText("sip:"+editTextUsername.getText().toString().trim()+"@61.222.245.149");
		editTextImpi.setText(editTextUsername.getText().toString().trim());
		editTextPassword.setText(mConfigurationService.getString(
				NgnConfigurationEntry.IDENTITY_PASSWORD,
				NgnConfigurationEntry.DEFAULT_IDENTITY_PASSWORD));
		editTextRealm.setText("sip:61.222.245.149");
		checkBoxEarlyIMS.setChecked(false);
			
		super.addConfigurationListener(editTextUsername);
        super.addConfigurationListener(editTextImpu);
        super.addConfigurationListener(editTextImpi);
        super.addConfigurationListener(editTextPassword);
        super.addConfigurationListener(editTextRealm);
        super.addConfigurationListener(checkBoxEarlyIMS);
		
        loggin = NetworkStatus.getInstance();
      
        super.SetmName(editTextUsername.getText().toString().trim());
        super.SetmPass(editTextPassword.getText().toString().trim());

		mSipBroadCastRecv = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				final String action = intent.getAction();
				Log.i("william","enter broadcase");
				// Registration Event
				if(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(action)){
					NgnRegistrationEventArgs args = intent.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
					
					if(args == null){
						Log.e(TAG, "Invalid event args");
						return;
					}
					
					switch(args.getEventType()){
						case REGISTRATION_NOK:
						case UNREGISTRATION_OK:
						case REGISTRATION_OK:
						case REGISTRATION_INPROGRESS:
						case UNREGISTRATION_INPROGRESS:
						case UNREGISTRATION_NOK:
						default:
							Log.i(TAG,"Show main view here");
							mScreenService.show(ScreenFuncTest.class, "FuncTest");
							//mScreenService.show(ScreenDirectionJS.class, "ScreenDirectionJS");
							
							break;
					}
				}
			}
			
		};
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
	    registerReceiver(mSipBroadCastRecv, intentFilter);
		
	}
	
	private void setScreenBackground(){
		View v= this.findViewById(R.id.wloginpagelayoutid);
		v.setBackgroundResource(R.drawable.xyzbackground);
	}
	
	private void getScreenSize(){
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		width = size.x;
		height = size.y;		
	}
	
	
	public void setComponentPosition(EditText text, int GAP, int horizontalGAP, int verticalGAP){
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		horizontalscope = width / GAP; 
		verticalscope = height / GAP;		
		layoutParams.setMargins(horizontalscope * horizontalGAP, verticalscope * verticalGAP, 0, 0);
		text.setLayoutParams(layoutParams);
	}
	
	
		
	private OnClickListener ClickListener = new OnClickListener() {
		@Override
		public void onClick(final View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			
			case R.id.loginenter:
				Log.i("shinhua", "shinhua editTextUsername = "
						+ editTextUsername.getText().toString().trim()
						+ " editTextUsername = "
						+ editTextPassword.getText().toString().trim());
				
				editTextUsername.setText(editTextUsername.getText().toString().trim());
				editTextImpu.setText(editTextImpu.getText().toString().trim());
				editTextImpi.setText(editTextImpi.getText().toString().trim());
				editTextPassword.setText(editTextPassword.getText().toString().trim());
				editTextRealm.setText("sip:doubango.org");
				checkBoxEarlyIMS.setChecked(false);
				
				mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, 
						editTextUsername.getText().toString().trim());
				mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU, 
						editTextImpu.getText().toString().trim());
				mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI, 
						editTextImpi.getText().toString().trim());
				mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD, 
						editTextPassword.getText().toString().trim());
				mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM, 
						editTextRealm.getText().toString().trim());
				mConfigurationService.putBoolean(
						NgnConfigurationEntry.NETWORK_USE_EARLY_IMS, 
						checkBoxEarlyIMS.isChecked());
				
				setXmpploggin(editTextUsername, editTextPassword);
				XMPPSet = new XMPPSetting();
				XMPPThreadv = new XMPPThread();
				XMPPThreadv.start();
				
				break;
			case R.id.logincencel:
				mScreenService.show(ScreenXYZsignin.class,
						"ScreenWLoginRevsion");
				break;
			default:
				Log.i(TAG, "Invaild Button function");
				break;
			}
		}
	};
	
	private void setXmpploggin(EditText Username, EditText Password){
		xmppUsername = Username.getText().toString().trim();
		xmppPassword = Password.getText().toString().trim();
	}
	
	class XMPPThread extends Thread {
		 @Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			try {
				Log.i(TAG, "Username = " + xmppUsername + " Password = "
						+ xmppPassword);
							
            	loggin.SetLogStatus(XMPPSet.XMPPStart(xmppUsername,xmppPassword));

				if (loggin.GetLogStatus()) {
					Log.i(TAG, xmppUsername + " Loggin successful");
					Sendmsg("ok");
				} else {
					Log.i(TAG, mName + " Loggin Fail");
					Sendmsg("Loggin Fail");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	        
		public void Sendmsg(String msg) {
			Message messageContent = handler.obtainMessage();
			Bundle bundle = new Bundle();
			bundle.putString("message", msg);
			messageContent.setData(bundle);
			handler.sendMessage(messageContent);
		}
	}
	
	
	// Define the Handler that receives messages from the thread and update the progress
    private final Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            String serverResponse = msg.getData().getString("message");
            
			if (serverResponse == "Loggin Fail") {
				Toast.makeText(getBaseContext(), serverResponse, Toast.LENGTH_SHORT)
						.show();
			} else if (serverResponse == "ok") {

				if (mSipService.getRegistrationState() == ConnectionState.CONNECTING
						|| mSipService.getRegistrationState() == ConnectionState.TERMINATING) {
					mSipService.stopStack();
				} 
				else if (mSipService.isRegistered()) {
					mSipService.unRegister();
				} 
				else {
					mSipService.register(ScreenXYZLogin.this);
				}
			}
		}
    };

	@Override
	protected void onDestroy() {
       if(mSipBroadCastRecv != null){
    	   unregisterReceiver(mSipBroadCastRecv);
    	   mSipBroadCastRecv = null;
       }
      
       super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {

	    Log.i(TAG, "onBackPressed--");
	   
	}
	
	protected void onPause() {
		if(super.mComputeConfiguration){
			mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, 
					editTextUsername.getText().toString().trim());
			mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU, 
					editTextImpu.getText().toString().trim());
			mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI, 
					editTextImpi.getText().toString().trim());
			mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD, 
					editTextPassword.getText().toString().trim());
			mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM, 
					editTextRealm.getText().toString().trim());
			mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_EARLY_IMS, 
					checkBoxEarlyIMS.isChecked());
			
			super.SetmName(editTextUsername.getText().toString().trim());
			super.SetmPass(editTextPassword.getText().toString().trim());
			
			// Compute
			if(!mConfigurationService.commit()){
				Log.e(TAG, "Failed to Commit() configuration");
			}
			
			super.mComputeConfiguration = false;
		}
		super.onPause();
	}
	
	
   
}





