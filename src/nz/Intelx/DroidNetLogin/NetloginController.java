package nz.Intelx.DroidNetLogin;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.math.BigDecimal;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import nz.ac.auckland.cs.des.Key_schedule;
import nz.ac.auckland.cs.des.desDataInputStream;
import nz.ac.auckland.cs.des.desDataOutputStream;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class NetloginController extends Service{
	static String TAG;
	
	boolean Wifi_Check = false; //TODO make a preference option to turn on and off wifi check
	
	//For Ping
	PingSender PingSender;
	PingRespHandler PingRespHandler;
	Context context;
	
	
	//For authentication
	String Username;
	String Password;
	
	//For updating preferences
	public static final String NET_PREF = "NetloginPref";
	SharedPreferences preferences;
	SharedPreferences.Editor preferenceEditor;
	PreferenceChangeListener preference_listener;
	
	
	//For receiving intents
	Intent intent_PRH;
	Intent intent_control;
	
	private BroadcastReceiver mWifiReceiver;
	
	
	int action;
	boolean Debug_Option;
	boolean Proxy_Option;
	static final String ACTION_CONTROL = "nz.Intelx.AndNetlogin.ACTION";
	static final int CHECK_STATUS = 0;
	static final int NETLOGIN_CONNECT = 1;
	static final int NETLOGIN_DISCONNECT = 2;
	static final int PROXY_START = 3;
	static final int PROXY_STOP = 4;
	static final int AUTO_START = 5;
	static final int AUTO_STOP = 6;
	
	
	//Status variables
	int IP_Usage = 0;
	int Status = 5;
	boolean Proxy = false;
	BigDecimal Usage;
	boolean loading; //
	int next = 6; //used in update() to generate new number to force call onPreferenceChangeListener
	boolean Manual_Flag = false; //to flag an action as manual control by the user
	
	//Notification related
	NotificationManager nManager;
	Intent notificationIntent;
	Notification notification;
	NotificationCompat.Builder nBuilder;
	PendingIntent contentIntent;
	int ID = 1080;
	RemoteViews contentView;
	CharSequence Ticker = "NetLogin service running";
	


	//For passing on instructions
	Intent intent_toNetlogin;
	Proxy proxy;
	String basedir;


	
	@Override
	public void onCreate(){
		preferences = getSharedPreferences(NET_PREF,0);
		preferenceEditor = preferences.edit();
		preference_listener = new PreferenceChangeListener();
		preferences.registerOnSharedPreferenceChangeListener(preference_listener);
		
		//notification stuff
		Resources res = this.getResources();
		notificationIntent = new Intent(this, DroidNetLoginActivity.class);
    	contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nBuilder = new NotificationCompat.Builder(this);
		nBuilder.setContentIntent(contentIntent)
				.setSmallIcon(R.drawable.ic_stat_notify)
				.setWhen(System.currentTimeMillis())
				.setAutoCancel(true)
				.setContentTitle("DroidNetLogin")
				.setContentText("0 MB used")
				.setOngoing(true);
		notification = nBuilder.getNotification();

    	
    	 IntentFilter filter = new IntentFilter();
    	  //filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
    	  //filter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE");//only when wifi is turned off
    	  filter.addAction("android.net.wifi.STATE_CHANGE");

    	  mWifiReceiver = new BroadcastReceiver() {
    	    @Override
    	    public void onReceive(Context context, Intent intent) {
    	    	if(!Manual_Flag && !WifiCheck()){
    	    		alert("Wifi disconnected");
    				stopSelf();
    	    	}
    	    }
    	  };
    	registerReceiver(mWifiReceiver, filter);
   
	}
	
	@Override
	public void onDestroy(){
		preferences.unregisterOnSharedPreferenceChangeListener(preference_listener);
		unregisterReceiver(mWifiReceiver);
		if (PingSender != null){
			PingSender.stopPinging();
		}
		if (PingRespHandler != null){
			PingRespHandler.end();
		}
		if(Proxy){
			Proxy(false);
		}
		nManager.cancel(ID);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startID){
		Proxy = preferences.getBoolean("Proxy", false);
		Status = preferences.getInt("Status",5);
		intent_control = intent;
		action = intent_control.getIntExtra("action", 0);
		Debug_Option = intent_control.getBooleanExtra("debug_option",false);
		Proxy_Option = intent_control.getBooleanExtra("proxy_option", true);
		switch (action){
		case CHECK_STATUS:
			if(PingSender==null||!PingSender.isAlive())
			preferenceEditor.putInt("Status", 2);
			preferenceEditor.commit();
		case NETLOGIN_CONNECT:
			if (WifiCheck()){
				Netlogin(true);
			}
			break;
		case NETLOGIN_DISCONNECT:
			Netlogin(false);
			break;
		case PROXY_START:
			Manual_Flag = true;
			Proxy(true);
			break;
			
		case PROXY_STOP:
			Manual_Flag=true;
			Proxy(false);
			break;
		
		case AUTO_START:
			Manual_Flag=false;
			Netlogin(true);						
			break;
		case AUTO_STOP:
				stopSelf();
			break;
		}
		return START_NOT_STICKY;
	}
	
	

	private boolean Netlogin(boolean action){
		Username = preferences.getString("username", "");
		Password = preferences.getString("password", "");
		Netlogin_Auth Auth = new Netlogin_Auth(Username, Password);
		
		if (action){
			if(!WifiCheck()){
				preferenceEditor.putInt("Status", 4);
				preferenceEditor.commit();
				update();
				stopSelf();				
			}else if (Username.equals("")||Password.equals("")){
				alert("Please enter your ID and Password");
				preferenceEditor.putInt("Status", 4);
				preferenceEditor.commit();
				update();
				stopSelf();
				}else{
				
					try {
						Auth.authenticate();
						PingSender = new PingSender(Auth.SERVER, Auth.PINGD_PORT, Auth.schedule, Auth.Auth_Ref, Auth.random2 + 2, Auth.Sequence_Number);
						PingRespHandler = new PingRespHandler(PingSender, PingSender.getSocket(), Auth.random1 + 3, Auth.Sequence_Number, Auth.schedule);
						PingRespHandler.start();
						PingSender.start();
						if(Proxy_Option){
							Proxy(true);
						}
						
						update();
						return true;
					}catch (UnknownHostException e) {
						Log.e(TAG,"Cannot resolve hostname");
						e.printStackTrace();
						alert(e.getMessage());
						preferenceEditor.putInt("Status", 4);
						preferenceEditor.commit();
						update();
						return false;
					}catch (IOException e1) {
						Log.e(TAG,"Cannot authenticate");
						preferenceEditor.putInt("Status", 4);
						preferenceEditor.commit();
						update();
						
							try {
								alert(e1.getMessage());
								Auth.NetGuardian_stream.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
				}
		

		}else{
			if (PingSender != null){
				PingSender.stopPinging();
			}
			if (PingRespHandler != null){
				PingRespHandler.end();
			}
			if (Status == 2)
			{
				preferenceEditor.putInt("Status",3);
				preferenceEditor.commit();
			}
			update();
		}
		
		update();
		return true;

	}
	
	private void alert(String message) {
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, (CharSequence)message, duration);
		toast.show();
	}

	
	private void Proxy(boolean action){
			
			try {
				basedir = this.getFilesDir().getAbsolutePath();
			} catch (Exception e) {
				Log.w(TAG,"cannot get directory path");
				alert("Proxy failed to start: cannot get directory path");
			}
			proxy = new Proxy(basedir);
			
			if (action){
				proxy.stop();
				if(proxy.start()){
					preferenceEditor.putBoolean("Proxy", true);	
					preferenceEditor.commit();					
				}else{
					preferenceEditor.putBoolean("Proxy", false);
					preferenceEditor.commit();
				}
			}else{
				proxy.stop();
				preferenceEditor.putBoolean("Proxy", false);
				preferenceEditor.commit();
				}

	}
	public class PreferenceChangeListener implements OnSharedPreferenceChangeListener{

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			Status = preferences.getInt("Status", 5);
			Proxy = preferences.getBoolean("Proxy", false);
			IP_Usage = preferences.getInt("IP_Usage", -1);
			update_notification(key);
		}
	}
	
	
	private void update(){ //to update the toggle button
		next++;
		preferenceEditor.putInt("force", next);
		preferenceEditor.putInt("Status", Status);
		preferenceEditor.commit();
		
	}
	
    private void update_notification(String key){
	
	    	//update class variables
	    	Status = preferences.getInt("Status", -1);
	    	IP_Usage = preferences.getInt("IP_Usage", -1);
    		Proxy = preferences.getBoolean("Proxy", false);
    		Usage = new BigDecimal(IP_Usage);
    		BigDecimal Usage_Rounded = Usage.divide(new BigDecimal(1000));
	    	if(key == "Status"){
	    		switch (Status){
			    	case 2: 
			    			if(!Ticker.toString().startsWith("NetLogin: Connected")){
				    			Ticker = "NetLogin: Connected.  "+ Usage_Rounded+ " MB";
				    			notification.tickerText = Ticker;
				    			notification.setLatestEventInfo(this, "NetLogin Connected", Usage_Rounded+" MB used", contentIntent);
				    			nManager.notify(ID,notification);
				    			startForeground(ID, notification);
				    			alert("Connected to Netlogin!");		    	
			    			}
			    			break;
			    	case 3: Ticker = "NetLogin Disconnected";
			    			notification.tickerText = Ticker;
			    			nManager.notify(ID,notification);
			    			alert("Netlogin disconnected");
			    		  	break;
	    		}
	    	}	
	    		if(key=="IP_Usage"){		
	    			notification.setLatestEventInfo(this, "NetLogin Connected", Usage_Rounded+" MB used", contentIntent);
	    			nManager.notify(ID, notification);
	    			IP_Usage = 0;
	    		}
	    	if(key == "Proxy"){
	    		if (Proxy){
	    			if (Manual_Flag){
		    			alert("Proxy started"); //will only show when proxy is started manually
		    			notification.tickerText= "Proxy Started";
		    			notification.setLatestEventInfo(this, "Proxy ON", "For testing only", contentIntent);
	    			}
	    			nManager.notify(ID,notification);
	    			startForeground(ID, notification);
	    		} else {
	    			Ticker = "Proxy: Stopped";
	    			if(Manual_Flag){
	    				notification.tickerText= "Proxy Stopped";
	    				notification.setLatestEventInfo(this, "Proxy OFF", "For testing only", contentIntent);
	    				alert("Proxy stopped"); //will only show when proxy is started manually
	    			}
	    			nManager.notify(ID,notification);
	    			
	    		}
	    	}
	  	
	    	if(!Manual_Flag&&Status!=2){
	    		notification.tickerText = "NetLogin Disconnected" ;
	    		nManager.notify(ID,notification);
	    		stopSelf();
	    	}
	    	
	    	if(!Proxy&&Status!=2){
	    		notification.tickerText = "NetLogin Disconnected" ;
	    		nManager.notify(ID,notification);
	    		stopSelf();
	    	}

	    	
    }

    private boolean WifiCheck(){
    	
    	WifiManager wifiM = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiI = wifiM.getConnectionInfo();
		if (!Debug_Option){
			if (wifiI.getSSID() != null && wifiI.getSSID().contains("UoA")) {
				return true;			
			}else {
				Toast t = Toast.makeText(getApplicationContext(), "Not connected to UOA network", 2);
				t.show();
			return false;
				}
			}else{
				ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			    NetworkInfo netInfo = cm.getActiveNetworkInfo();
			    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			        return true;
			    }
			    alert("Not connected to the internet");
			    return false;
			}
    }
	
  //thread to read and validate ping responses from server

    class PingRespHandler extends Thread {
    	   	
    	
    	//Response ping packet Commands
    	private final int ACK		= 1;	
    	private final int NACK		= 0; 	//will cause a shutdown.
    	private int inToken			= 0;
    	private DatagramSocket s	= null;
    	private Key_schedule schedule		= null; //set up encryption key to the users passwd
    	private volatile boolean loop		= true;
        private int Next_Sequence_Number_Expected = 0;
    	private PingSender Pinger;


    	/*
    	 * Use supplied socket instead of making one with a dynamically
    	 * allocated port, when using this send a response port of 0, and 
    	 * Robs new ping daemon will just send ping responses to the same
    	 * port it receives them on.
    	 */
    	
    	public PingRespHandler(PingSender pinger, DatagramSocket socket, int randomIn, int sequenceNum, Key_schedule sched){
       		s = socket; //Use same socket we use for sending pings
    		inToken = randomIn;
    		Next_Sequence_Number_Expected = sequenceNum;	
    		schedule = sched;
    		Pinger = pinger;
    		try {
    			s.setSoTimeout( 500 );
    		} catch (SocketException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}	// set timeout to half a second
    	}
    							
    	public int getLocalPort(){
    		return s.getLocalPort();
    	}

    	public void end(){
    		loop = false;
    		Update(3,-1);
    		interrupt();
    	}
    	
    	private void Update(int Status, int IP_Usage){
    		if(IP_Usage!= -1){
    			preferenceEditor.putInt("IP_Usage", IP_Usage);	
    		}
    		preferenceEditor.putInt("Status", Status);
    		preferenceEditor.commit();
    	}


    	public synchronized void run( ){
    		
    		byte recvBytes[] = new byte[ 8192 ];
    		DatagramPacket incomingPacket = new DatagramPacket( recvBytes, recvBytes.length );
    		desDataInputStream des_in;
    		byte message[];
    		int random_returned;
    		int Seq_number_returned;
    		int packet_length;
    		int IP_usage;
    		int Command;
    		int message_length;
    		int bad = 0;
    		int OnPlan = 0;

    		
    		setPriority( Thread.MAX_PRIORITY / 4 );
    				
    		while( loop && bad < 10 && Pinger.getOutstandingPings() < 5 ){
    			
    			incomingPacket = new DatagramPacket( recvBytes, recvBytes.length );
    			try{
    				s.receive( incomingPacket );
    			} catch( InterruptedIOException e ){
    				// end() interrupted is and wants us to stop or socket timeout
    				continue;
    			} catch( IOException e ) {
    				Log.w("AndNetLogin", "Error receiving: " + e );
    				bad++;
    				continue;
    			}
    			
    			packet_length = incomingPacket.getLength();
    			if( packet_length < 4 * 6) {
    				Log.w("AndNetLogin", "Short packet"  );
    				bad++;
    				continue;
    			}
    			
    			des_in = new desDataInputStream( incomingPacket.getData() , 0, packet_length, schedule);

    		    try {
    				random_returned = des_in.readInt();
    				if( inToken != random_returned ) {	// Other end doesn't agree on the current passwd
    					Log.w("AndNetLogin", "Other end doesn't agree on the current passwd" );
    					bad++;
    					continue; //packet could have been mashed.
    				}
    				
    				Seq_number_returned = des_in.readInt();
    				
    				if( Next_Sequence_Number_Expected > Seq_number_returned ) {	// Probably a delayed packet
    					Log.w("AndNetLogin", "Ping responce sequence numbers out" );
    					continue;
    				}
    				
    				Next_Sequence_Number_Expected  = Seq_number_returned + 1;  //Catch up.
    				
    				
    				/* from client version 3, These will be replaced by display user Internet Plan and monthly usage*/
    			
    				//IP_balance = des_in.readInt();
    				IP_usage = des_in.readInt();
    				
    				Command	 = des_in.readInt();
    				
    				//OnPeak = des_in.readInt();
    				OnPlan=des_in.readInt();
    				
    				message_length = des_in.readInt();
    				message = new byte[ message_length ];
    				des_in.read( message );
    				
    				//netLogin.update( IP_balance, ( OnPeak & 0x01 ) == 0x01, true, new String( message ) );
    				//update status
    				Update(2, IP_usage);
    				bad = 0; //start error trapping again.
    				Pinger.zeroOutstandingPings();
    				
    				if( Command == NACK ) {
    					end(); //kill own thread
    				}
    			} catch( Exception e ) {
    				Log.w("AndNetLogin", "ping recv: Exception:"+e );
    				bad++;
    		    }
    		}
    		if( Pinger.getOutstandingPings() < 5 ){
    			Log.w("AndNetLogin", "Max outstanding pings reached, disconnecting" );
    			end();
    		} 
    		
    	}
    	
    }
    
  //thread to periodically ping the server to maintain connection
    class PingSender extends Thread {
    	private DatagramSocket s = null;
    	private int Auth_Ref = -1;
    	private int outtoken = 0;
    	private String Host;
    	private InetAddress Host_IPAddr;
    	private int Port;
    	private Key_schedule schedule = null;  //set up encryption key to the users old passwd
    	private int Sequence_Number = 0;
    	private volatile boolean stop = false;
    	private volatile int outstandingPings = 0;

    	
    	
    	public PingSender(String The_Host, int The_Port, Key_schedule schedule, int Auth_Ref, int outtoken, int Sequence_Number){
    		this.schedule = schedule;
    		this.Auth_Ref = Auth_Ref;
    		this.outtoken = outtoken;
    		this.Sequence_Number = Sequence_Number;try {
    		Host = The_Host;
    		Port = The_Port;	
    		s = new DatagramSocket(); 	// Allocate a datgram socket
    		}
    		catch ( Exception e ) {
    			e.printStackTrace();
    		}
    	
    		try {
    			Host_IPAddr = InetAddress.getByName( Host );
    		} catch (UnknownHostException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}

    	public DatagramSocket getSocket() {
    		return s;
    	}

    	public void stopPinging() {
    		DatagramPacket sendPacket = null;
    		desDataOutputStream packit = new desDataOutputStream( 128 );
    		desDataOutputStream des_out = new desDataOutputStream( 128 );
    		byte EncryptedOutBuffer[];
    		byte messageBytes[];

    		try{
    			// Tell gate we're disconnecting
    			des_out.writeInt( outtoken ); 
    			des_out.writeInt( Sequence_Number + 10000 );

    			EncryptedOutBuffer = des_out.des_encrypt( schedule );

    			packit.writeInt( Auth_Ref );
    			packit.write( EncryptedOutBuffer, 0, EncryptedOutBuffer.length );
    			messageBytes = packit.toByteArray();
    			sendPacket = new DatagramPacket( messageBytes, messageBytes.length, Host_IPAddr , Port );

    			s.send( sendPacket );
    			s.close();
    		} catch( Exception e){ }

    		stop = true;
    		interrupt();
    	}

    	public int getOutstandingPings() {
    		return outstandingPings;
    	}

    	public void zeroOutstandingPings() {
    		outstandingPings = 0;
    	}

    	public void sendMessage( String user, String message ){
    		DatagramPacket sendPacket = null;
    		desDataOutputStream packit = new desDataOutputStream( 8192 );
    		byte messageBytes[];
    	

    		try {
    			packit.writeInt( -1 );					// -1 tells Pingd this is a message
    			packit.writeBytes( "senduser " + user + " " + message + " " );
    			messageBytes = packit.toByteArray();
    			sendPacket = new DatagramPacket( messageBytes, messageBytes.length, Host_IPAddr, Port );
    			s.send( sendPacket );
    			Log.d("AndNetLogin", "Sent ping");
    		} catch ( IOException e ) {
    			Log.w("AndNetLogin", "PingSender: Error sending message" );
    		}
    	}

    	public synchronized void run( ) {
    		DatagramPacket sendPacket = null;
    		desDataOutputStream packit = new desDataOutputStream( 128 );
    		desDataOutputStream des_out = new desDataOutputStream( 128 );
    		byte EncryptedOutBuffer[];
    		byte messageBytes[];
    		int bad = 0;

    		setPriority( Thread.MAX_PRIORITY / 4 );
    		while ( !stop && outstandingPings < 5 && bad < 10 ) {
    			try {
    				des_out.writeInt( outtoken );   		//Can throw IOException
    				des_out.writeInt( Sequence_Number );   //Can throw IOException

    				EncryptedOutBuffer = des_out.des_encrypt( schedule );  	//encrypt buffer

    				//These can throw IOException
    				packit.writeInt( Auth_Ref );
    				packit.write( EncryptedOutBuffer, 0, EncryptedOutBuffer.length );
    				messageBytes = packit.toByteArray();
    				sendPacket = new DatagramPacket( messageBytes, messageBytes.length, Host_IPAddr , Port );

    				s.send( sendPacket );
    				Log.d("AndNetLogin", "Sent packet");

    				Sequence_Number++;
    				outstandingPings++;
    				bad = 0;
    	
    			} catch ( IOException e ) {
    				Log.w("AndNetLogin", "PingSender: Error sending ping packet" );
    				bad++; 		// Ignore it at least 10 times in a row
    			}

    			des_out.reset();  //zero the buffers so we can reuse them.
    			packit.reset();
    			EncryptedOutBuffer = null;  //free the memory for these.
    			messageBytes = null;
    			sendPacket = null;

    			// Sleep for 10 seconds
    			try {
    				sleep( 10000 );  //time to sleep in 1/1000 of a second
    			} catch ( InterruptedException e ) {
    				// stopPinging wants us to stop.
    			}
    			
    		} //end of while
    		
    		try {
    			s.close();
    		} catch ( Exception e ) {
    			Log.w("AndNetLogin", "Error closing socket: " + e );
    		}
    		
    	}

    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	
}


	
