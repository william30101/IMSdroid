package org.doubango.imsdroid.map;

public class NetworkStatus {

	private boolean logStatus = false;
	private static NetworkStatus instance;
	
	public static NetworkStatus getInstance() {
		 if (instance == null){
	            synchronized(NetworkStatus.class){
	                if(instance == null) {
	                     instance = new NetworkStatus(false);
	                }
	            }
	        }
	        return instance;
	}
	
	private NetworkStatus(boolean status)
	{
		logStatus = status;
	}
	
	public boolean GetLogStatus()
	{
		return logStatus;
	}
	
	public void SetLogStatus(boolean logS)
	{
		logStatus = logS;
	}
	
	
	
}
