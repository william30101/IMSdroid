package org.doubango.imsdroid;


import org.doubango.imsdroid.Screens.ScreenAV;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import android.util.Log;

public class XMPPSetting {

	private ScreenAV xmppClient;
	
	public XMPPSetting(ScreenAV xmppClient)
	{
		this.xmppClient = xmppClient;
	}
	
	public void XMPPStart()
	{
		// Hardcode here , we could modify later.
		 String host = "61.222.245.149";
	     String port = "5222";
	     String username = "james1";
	     String password = "1234";
	
	     // Create a connection
	     
	     ConnectionConfiguration connConfig =
	             new ConnectionConfiguration(host, Integer.parseInt(port));
	     
	     XMPPConnection connection = new XMPPConnection(connConfig);
	     
	     try {
	         connection.connect();
	         Log.i("XMPPClient", "[SettingsDialog] Connected to " + connection.getHost());
	     } catch (XMPPException ex1) {
	         Log.e("XMPPClient", "[SettingsDialog] Failed to connect to " + connection.getHost());
	         Log.e("XMPPClient", ex1.toString());
	         xmppClient.setConnection(null);
	     }
	     
	     try {
	         connection.login(username, password);
	         Log.i("XMPPClient", "Logged in as " + connection.getUser());
	
	         // Set the status to available
	         Presence presence = new Presence(Presence.Type.available);
	         connection.sendPacket(presence);
	         xmppClient.setConnection(connection);
	     } catch (XMPPException ex) {
	         Log.e("XMPPClient", "[SettingsDialog] Failed to log in as " + username);
	         Log.e("XMPPClient", ex.toString());
	             xmppClient.setConnection(null);
	     }
	}
	
	
	

}
