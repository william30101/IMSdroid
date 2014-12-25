package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.UartCmd;
import org.doubango.imsdroid.XMPPSetting;
import org.doubango.imsdroid.Screens.BaseScreen.SCREEN_TYPE;
import org.doubango.imsdroid.Screens.ScreenHome.ScreenHomeAdapter;
import org.doubango.imsdroid.Utils.NetworkStatus;
import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnSipSession.ConnectionState;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnStringUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class ScreenWLogin extends BaseScreen {
	
	
	//private static String TAG = ScreenWLogin.class.getCanonicalName();
	private static String TAG = "william";
	
	private final INgnConfigurationService mConfigurationService;
	
	private EditText mEtDisplayName;
	private EditText mEtIMPU;
	private EditText mEtIMPI;
	private EditText mEtPassword;
	private EditText mEtRealm;
	private CheckBox mCbEarlyIMS;
	
	private final INgnSipService mSipService;
	
	private BroadcastReceiver mSipBroadCastRecv;
	
	private Button SignBtn;
	private Button NetworkBtn;
	private Button NattBtn;
	private Button btnTest;
	
	private XMPPSetting XMPPSet;
	
	public Thread XMPPThreadv = new XMPPThread(); 
	
	private String mName;	//For XMPP thread user name
	private String mPass;	//For XMPP thread user password
	
	//private boolean loggin; // For XMPP thread detect user status,
	
	private NetworkStatus loggin;
	
	private UartCmd uartCmd = UartCmd.getInstance();
	
	public ScreenWLogin() {
		super(SCREEN_TYPE.HOME_T, TAG);
		
		mSipService = getEngine().getSipService();
		mConfigurationService = getEngine().getConfigurationService();
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wlogin);
		
		// *************** Get each field from default configure , 
		// *************** if we modify it on view,  will save to default configure on ClickListener function.
		
		Log.i(TAG,"william oncreate screen login here");
		SignBtn = (Button) findViewById(R.id.signinbtn);
		NetworkBtn = (Button) findViewById(R.id.NetworkBtn);
		NattBtn = (Button) findViewById(R.id.NattBtn);
		btnTest = (Button) findViewById(R.id.btnTest);
		
		SignBtn.setOnClickListener(ClickListener);
		NetworkBtn.setOnClickListener(ClickListener);
		NattBtn.setOnClickListener(ClickListener);
		btnTest.setOnClickListener(ClickListener);
		
		mEtDisplayName = (EditText)findViewById(R.id.screen_identity_editText_displayname);
        mEtIMPU = (EditText)findViewById(R.id.screen_identity_editText_impu);
        mEtIMPI = (EditText)findViewById(R.id.screen_identity_editText_impi);
        mEtPassword = (EditText)findViewById(R.id.screen_identity_editText_password);
        mEtRealm = (EditText)findViewById(R.id.screen_identity_editText_realm);
        mCbEarlyIMS = (CheckBox)findViewById(R.id.screen_identity_checkBox_earlyIMS);
        
        mEtDisplayName.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME));
        //mEtIMPU.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPU, NgnConfigurationEntry.DEFAULT_IDENTITY_IMPU));
        mEtIMPU.setText("sip:"+mEtDisplayName.getText().toString().trim()+"@61.222.245.149");
        //mEtIMPI.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPI, NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI));
        mEtIMPI.setText(mEtDisplayName.getText().toString().trim());
        //mEtPassword.setText(mEtPassword.getText().toString().trim());
        mEtPassword.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_PASSWORD, NgnConfigurationEntry.DEFAULT_IDENTITY_PASSWORD));
        mEtRealm.setText("sip:61.222.245.149");
        mCbEarlyIMS.setChecked(false);
        
        super.addConfigurationListener(mEtDisplayName);
        super.addConfigurationListener(mEtIMPU);
        super.addConfigurationListener(mEtIMPI);
        super.addConfigurationListener(mEtPassword);
        super.addConfigurationListener(mEtRealm);
        super.addConfigurationListener(mCbEarlyIMS);
		
        loggin = NetworkStatus.getInstance();
        
		super.SetmName(mEtDisplayName.getText().toString().trim());
		super.SetmPass(mEtPassword.getText().toString().trim());
		
		uartCmd.OpenSetUartPort("ttymxc4");
		uartCmd.OpenSetUartPort("ttymxc2");
		
		mSipBroadCastRecv = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
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
							//((ScreenHomeAdapter)mGridView.getAdapter()).refresh();
							Log.i(TAG,"Show main view here");
							mScreenService.show(ScreenFuncTest.class, "FuncTest");
							
							break;
					}
				}
			}
		};
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
	    registerReceiver(mSipBroadCastRecv, intentFilter);
		
	}
	
	private OnClickListener ClickListener = new OnClickListener() {
	    @Override
	    public void onClick(final View v) {
	    	switch(v.getId()){
	    		case R.id.signinbtn : 
	    			
	    			//Check and save Login user on OpenFire server.
	    			Log.i(TAG,"william mEtDisplayName="+mEtDisplayName.getText().toString().trim()+" mEtPassword="+mEtPassword.getText().toString().trim());
	    			
	    			
	    			//save

	    			
	    			mEtDisplayName.setText(mEtDisplayName.getText().toString().trim());
	    	        mEtIMPU.setText("sip:"+mEtDisplayName.getText().toString().trim()+"@61.222.245.149");
	    	        //mEtIMPI.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPI, NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI));
	    	        mEtIMPI.setText(mEtDisplayName.getText().toString().trim());
	    	        mEtPassword.setText(mEtPassword.getText().toString().trim());
	    	        mEtRealm.setText("sip:61.222.245.149");
	    	        mCbEarlyIMS.setChecked(false);
	    			
    				mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, 
    						mEtDisplayName.getText().toString().trim());
    				mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU, 
    						mEtIMPU.getText().toString().trim());
    				mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI, 
    						mEtIMPI.getText().toString().trim());
    				mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD, 
    						mEtPassword.getText().toString().trim());
    				mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM, 
    						mEtRealm.getText().toString().trim());
    				mConfigurationService.putBoolean(
    						NgnConfigurationEntry.NETWORK_USE_EARLY_IMS, 
    						mCbEarlyIMS.isChecked());
    					
	    			
	    			mName = mEtDisplayName.getText().toString().trim();
	    			mPass = mEtPassword.getText().toString().trim();

	    			
	    			XMPPSet = new XMPPSetting();
	    			XMPPThreadv = new XMPPThread();
	    			XMPPThreadv.start();
	    			
	    		break;
	    		case R.id.NetworkBtn : 
	    			mScreenService.show(ScreenNetwork.class, "NetworkSetting");
	    		break;
	    		case R.id.NattBtn : 
	    			mScreenService.show(ScreenNatt.class, "NattSetting");
	    		break;
	    		case R.id.btnTest :
	    			Log.i(TAG,"enter btnTest");
	    			mScreenService.show(ScreenDirection.class, "BtnTest");
	    		default:
	    			Log.i(TAG,"Invaild Button function");
	    			break;
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
					mEtDisplayName.getText().toString().trim());
			mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU, 
					mEtIMPU.getText().toString().trim());
			mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI, 
					mEtIMPI.getText().toString().trim());
			mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD, 
					mEtPassword.getText().toString().trim());
			mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM, 
					mEtRealm.getText().toString().trim());
			mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_EARLY_IMS, 
					mCbEarlyIMS.isChecked());
			
			
			super.SetmName(mEtDisplayName.getText().toString().trim());
			super.SetmPass(mEtPassword.getText().toString().trim());
			
			// Compute
			if(!mConfigurationService.commit()){
				Log.e(TAG, "Failed to Commit() configuration");
			}
			
			super.mComputeConfiguration = false;
		}
		super.onPause();
	}
	
	class XMPPThread extends Thread {
		 
        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            try {
            	Log.i(TAG,"Name = "+mName + " Pass = "+mPass);
            	loggin.SetLogStatus(XMPPSet.XMPPStart(mName,mPass));
            	
            	
            	if (loggin.GetLogStatus())
            	{
            		Log.i(TAG,mName + " Loggin successful");
            		Sendmsg("ok");
            	}
            	else
            	{
            		Log.i(TAG,mName + " Loggin Fail");
            		Sendmsg("Loggin Fail");
            	}

            } catch (Exception e) {
                 e.printStackTrace();
            }
        }
        
        public void Sendmsg(String msg)
        {
        	Message msgObj = handler.obtainMessage();
            Bundle b = new Bundle();
            b.putString("message", msg);
            msgObj.setData(b);
            handler.sendMessage(msgObj);
        }
    }
	
	
	// Define the Handler that receives messages from the thread and update the progress
    private final Handler handler = new Handler() {

        public void handleMessage(Message msg) {
             
            String aResponse = msg.getData().getString("message");
            if (aResponse == "Loggin Fail")
            {
                    Toast.makeText(
                            getBaseContext(),
                            aResponse,
                            Toast.LENGTH_SHORT).show();
            }
            else if (aResponse == "ok")
            {
            	
            	if(mSipService.getRegistrationState() == ConnectionState.CONNECTING || mSipService.getRegistrationState() == ConnectionState.TERMINATING){
					mSipService.stopStack();
				}
				else if(mSipService.isRegistered()){
					mSipService.unRegister();
				}
				else{
					mSipService.register(ScreenWLogin.this);
					
				}

            }
        }
    };
}
