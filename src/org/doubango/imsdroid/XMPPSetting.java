package org.doubango.imsdroid;

import java.io.IOException;

import org.doubango.imsdroid.map.Game;
import org.doubango.imsdroid.map.GameView;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import android.util.Log;

public class XMPPSetting {

	
	private String TAG = "william";

	private static XMPPConnection connection;
	private UartCmd UCmd = UartCmd.getInstance();
	private boolean LogSuc = false;
	
	public Game game = new Game();

	//public XMPPSetting(ScreenAV xmppClient)
	public XMPPSetting()
	{
		//this.xmppClient = xmppClient;
	}

	public boolean XMPPStart(String Name , String Pass)
	{
		// Hardcode here , we could modify later.
		 String host = "61.222.245.149";
	     String port = "5222";
	     String username = Name;
	     String password = Pass;
	     LogSuc = false;
	     // Create a connection

	     ConnectionConfiguration connConfig =
	             new ConnectionConfiguration(host, Integer.parseInt(port));
	     connection = new XMPPConnection(connConfig);
	     Log.i(TAG, "Name= " + username + " Pass = " + Pass);

	     try {
	         connection.connect();
	         Log.i(TAG, "[SettingsDialog] Connected to " + connection.getHost());
	     } catch (XMPPException ex1) {
	         Log.e(TAG, "[SettingsDialog] Failed to connect to " + connection.getHost());
	         Log.e(TAG, ex1.toString());
	        // xmppClient.setConnection(null);
	         setConnection(null);
	     }
	     
	     try {
	         connection.login(username, password);
	         Log.i(TAG, "Logged in as " + connection.getUser());
	
	         // Set the status to available
	         Presence presence = new Presence(Presence.Type.available);
	         connection.sendPacket(presence);
	        // xmppClient.setConnection(connection);
	         setConnection(connection);
	         LogSuc = true;
	     } catch (XMPPException ex) {
	         Log.e(TAG, "[SettingsDialog] Failed to log in as " + username);
	         Log.e(TAG, ex.toString());
	             //xmppClient.setConnection(null);
	         setConnection(null);
	     }
	     return LogSuc;
	}
	
	
	public void setConnection(XMPPConnection connection) {
		if (connection != null) {
		    // Add a packet listener to get messages sent to us
		    PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
		    connection.addPacketListener(new PacketListener() {
		        public void processPacket(Packet packet) {
		            Message message = (Message) packet;
		            if (message.getBody() != null) {
		                String fromName = StringUtils.parseBareAddress(message.getFrom());
		                
		                
		                String[] inM = message.getBody().split("\\s+");
		                
		                if (inM[0] == "start")
		                {
		                	Log.i(TAG, "Got text [" + message.getBody() + "] from [" + fromName + "]" );
		                	game.source[0] = Integer.parseInt(inM[1]);
		                	game.source[1] = Integer.parseInt(inM[2]);
		                }
		                
						try {
							byte[] cmdByte = UCmd.GetAllByte(inM);
							Log.i(TAG, "Got text [" + message.getBody() + "] from [" + fromName + "]" + " Func num = " + cmdByte[1] + " Direc = " + cmdByte[2]);
							//Do JNI here , We got correct data format here.
							//String decoded = new String(cmdByte, "ISO-8859-1");
							UCmd.SendMsgUart(1,cmdByte);

							
							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		                
		                //We receive message here.
		                
		            }
		        }
		    }, filter);
		}
    }

	public void XMPPSendText(String to,String istr)
    {
		//Server name , can't be removed here.
		String Reci = to+"@james-pc/Smack";
        String text = istr;

        Log.i(TAG, "Sending text [" + text + "] to [" + Reci + "]");
        Message msg = new Message(Reci, Message.Type.chat);
        msg.setBody(text);
        connection.sendPacket(msg);

    }
	
	public XMPPConnection GetConnection()
	{
		return this.connection;
	}

}
