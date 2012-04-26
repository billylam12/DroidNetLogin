package nz.Intelx.DroidNetLogin;


import nz.Intelx.DroidNetLogin.PreferenceListFragment.OnPreferenceAttachedListener;
import nz.ac.auckland.cs.des.C_Block;
import nz.ac.auckland.cs.des.Key_schedule;
import nz.ac.auckland.cs.des.des_encrypt;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;



public class DroidNetLoginActivity extends SherlockFragmentActivity implements OnPreferenceAttachedListener, OnPreferenceChangeListener, OnPreferenceClickListener{
    /** Called when the activity is first created. */
	
	static final String NET_PREF = "NetloginPref";
	static final String TAG = "AndNetlogin";
	EditText editUsername;
	EditText editPassword;
	String basedir = null;
	static SharedPreferences preferences;
	PreferenceChangeListener preference_listener;
	static SharedPreferences.Editor preferenceEditor;
	static int next = -1;
	
	//connection codes
	static int LOGGING_IN = 0;
	static int CONNECTED = 1;
	static int DISCONNECTED = 2;
	static int FAILED = 3;
	static int Status = -1;
	static int IP_Usage;
	static boolean Proxy = false; 
	
	//Intents
	
	static Intent intent;
	Intent intent_update;
	Intent intent_proxy;
	
	//UI related
	private static final int NUM_ITEMS = 3;
	private MyAdapter mAdapter;
    private ViewPager mPager;
    public ActionBar mActionBar;
    private EditText usernameEdit;
    private EditText passwordEdit;
    
    //Options
    public static int Login_Option;
    public static boolean Debug_Option;
    public static boolean Proxy_Option;
  
    @Override
	public void onResume(){
		super.onResume();
		preferences.registerOnSharedPreferenceChangeListener(preference_listener);
		if(!Service_Running()&&(preferences.getBoolean("Proxy", false)||preferences.getInt("Status", 5)==2)){
			preferenceEditor.putInt("Status", 3);
			preferenceEditor.commit();
			intent.putExtra("debug_option", Debug_Option);
    		intent.putExtra("action", 4);
			startService(intent);
		}

		updateUI();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    
        
        preference_listener = new PreferenceChangeListener();
        preferences = getSharedPreferences(NET_PREF,0);
        intent = new Intent(this, NetloginController.class);
        preferenceEditor = preferences.edit();
        
      //Setup UI
        try {
        	setContentView(R.layout.main);
        	mActionBar = getSupportActionBar();
        	mAdapter = new MyAdapter(getSupportFragmentManager());
            mPager = (ViewPager)findViewById(R.id.pager);
            mPager.setAdapter(mAdapter);
            mPager.setOffscreenPageLimit(2);
            TabPageIndicator tabIndicator = (TabPageIndicator) findViewById(R.id.indicator);
            tabIndicator.setViewPager(mPager);

			} 
        catch (Exception e) {
			Log.e("ViewPager", e.toString());
			}

        //initial check for root and iptables
        
       
		
		File f = new File("/system/xbin/iptables");
		if (!f.exists()) {
			f = new File("/system/bin/iptables");
			if (!f.exists()) {
				 alert("Please install Iptables from the <a href = 'market://details?id=com.mgranja.iptables'>Android Play Market</a>.", this);
			}
		}
	
		f = new File("/system/xbin/su");
		if (!f.exists()) {
			f = new File("/system/bin/su");
			if (!f.exists()) {
				alert("Your device must be rooted.", this);
			}
		}
		
		try {
			 basedir = getBaseContext().getFilesDir().getAbsolutePath();
			} catch (Exception e) {
				e.printStackTrace();
				alert("cannot get application location", this);
			}
			
			copyfile("redsocks");
			copyfile("proxy.sh");
			copyfile("redirect.sh");
    
    }
	    
	public void copyfile(String file) {
			
			String of = file;
			File f = new File(of);
			
			if (!f.exists()) {
				try {
					InputStream in = getAssets().open(file);
					FileOutputStream out = getBaseContext().openFileOutput(of, MODE_PRIVATE);
	
					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					out.close();
					in.close();
					Runtime.getRuntime().exec("chmod 700 " + basedir + "/" + of);
				} catch (IOException e) {
				}
			}
		}
		
	public void alert(String msg, Activity a) {
		
		final Activity act = a;
		final AlertDialog builder = new AlertDialog.Builder(a)
			.setMessage(Html.fromHtml(msg))
			.setCancelable(false)
			.setNegativeButton("OK",					
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (act != null)
							act.finish();
						else
							dialog.cancel();
					}
				}).show();		
		 ((TextView)builder.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
	}
						
	
    
    protected void onPause() {
		super.onPause();
		//unregisterReceiver(broadcastReceiver);
		preferences.unregisterOnSharedPreferenceChangeListener(preference_listener);
	}

	
	private void updateUI(){
    	
		LoginFragment Login_Fragment = (LoginFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":0");
    		
		if(Login_Fragment != null&&Login_Fragment.getView()!=null){
			TextView t = (TextView)Login_Fragment.getActivity().findViewById(R.id.Status);
	    	TextView t2 = (TextView)Login_Fragment.getActivity().findViewById(R.id.Usage);
	    	TextView t3 = (TextView)Login_Fragment.getActivity().findViewById(R.id.proxy_status);
	    	ToggleButton b = (ToggleButton)Login_Fragment.getActivity().findViewById(R.id.Auto);
	    	
	    	
	    	Status = preferences.getInt("Status", -1);
	    	Proxy = preferences.getBoolean("Proxy", false);
	    	IP_Usage = preferences.getInt("IP_Usage", 0);
	    	
	    	switch(Status){
			case 1: t.setText("Status: Logging in");
					break;
			case 2: t.setText("Status: Connected!");
					break;
			case 3: t.setText("Status: Disconnected");
					break;
			case 4: t.setText("Status: Error");
					break;
	    	}
					
			BigDecimal Usage = new BigDecimal(IP_Usage);
			
			t2.setText("Usage: " + Usage.divide(new BigDecimal(1000)) + " MB");
			
			if (Proxy){
				t3.setText("Proxy: Started");
			} else {
				t3.setText("Proxy: Stopped");
			}
			
			if(Proxy_Option){
				if(Status==2&&Proxy==true){
					b.setChecked(true);
				}else{
					b.setChecked(false);
				}
			}else{
				if(Status==2){
					b.setChecked(true);
				}else{
					b.setChecked(false);
				}
			}

		}
    }
		   	
	    	
    
	public class PreferenceChangeListener implements OnSharedPreferenceChangeListener{

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			updateUI();
		}
	}
	
	
	//Workaround to make sure keyboard is hidden after focus away from textbox
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {

	    View v = getCurrentFocus();
	    boolean ret = super.dispatchTouchEvent(event);

	    if (v instanceof EditText) {
	        View w = getCurrentFocus();
	        int scrcoords[] = new int[2];
	        w.getLocationOnScreen(scrcoords);
	        float x = event.getRawX() + w.getLeft() - scrcoords[0];
	        float y = event.getRawY() + w.getTop() - scrcoords[1];

	        Log.d("Activity", "Touch event "+event.getRawX()+","+event.getRawY()+" "+x+","+y+" rect "+w.getLeft()+","+w.getTop()+","+w.getRight()+","+w.getBottom()+" coords "+scrcoords[0]+","+scrcoords[1]);
	        if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom()) ) { 

	            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
	        }
	    }
	return ret;
	}
    
    public class MyAdapter extends FragmentPagerAdapter implements TitleProvider{
    	PreferenceListFragment[] fragments;
    	String[] titles;
    	                                       
        public MyAdapter(FragmentManager fm) {
            super(fm);
            
            titles = new String[3];
            titles[0] = getString(R.string.title_login);
            titles[1] = getString(R.string.title_options);
            titles[2] = getString(R.string.title_about);
            
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
           	if (position == 0 ){
           		return new LoginFragment();
        	}else if (position == 1){
        		return PreferenceListFragment.newInstance(R.layout.fragment_options);
        	}else if (position == 2){
        		return new AboutFragment();
        	}
			return null;
        }

		@Override
		public String getTitle(int position) {
			// TODO Auto-generated method stub
			return titles[position];
		}
    }
    
    
    private boolean Service_Running() {
		String Service = getString(R.string.app_id)+".NetloginController";
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	 if (Service.equals(service.service.getClassName())) {
	     return true;
	 }
	    }
	    return false;
	}
    
    //This defines the fragment of the about page
    public static class AboutFragment extends SherlockListFragment {
    	Context activity = this.getActivity();
    	private static String[] about_list = new String[]{
    		"About DroidNetLogin","License","Donate"	
    		};
    		
    	private ArrayAdapter<String> listAdapter;

    	@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
    		
    		View v = inflater.inflate(R.layout.fragment_about, container, false);
    		setupListAdapter();
    		
    		
    	

    		return v;
    	}
    		
    		
    	private void setupListAdapter(){
    		listAdapter = new ArrayAdapter<String>(this.getSherlockActivity(), R.layout.list_item, about_list);
    		setListAdapter(listAdapter);

    	}
    		
    
    	@Override
		public void onListItemClick(ListView l, View v, int position, long id){
    		super.onListItemClick(l, v, position, id);
    		
    		
    		
    		switch(position){
    		
    			case 0: showDialog(position);
    					break;
    			case 1: showDialog(position);
    					break;
    			case 2: showDialog(position);
    					break;
    		}
    	}
    	
    	void showDialog(int position){
    		FragmentTransaction ft = getFragmentManager().beginTransaction();
    		SherlockDialogFragment newFragment = MyDialogFragment.newInstance(position);
            newFragment.show(ft, "dialog");
    		
    		
    		
    	}
  	
    }
    
    public static class MyDialogFragment extends SherlockDialogFragment {
        int type;


        static MyDialogFragment newInstance(int type) {
            MyDialogFragment f = new MyDialogFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("type", type);
            f.setArguments(args);

            return f;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            type = getArguments().getInt("type");
            setStyle(SherlockDialogFragment.STYLE_NORMAL,R.style.Theme_Light_Dialog);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_dialog, container, false);
            View tv = v.findViewById(R.id.dialog);
            //View iv = v.findViewById(R.id.DonateButton);
            LinearLayout lv = (LinearLayout) v.findViewById(R.id.dialog_layout);
            
            switch(type){
            case 0: 
            	getDialog().setTitle("About");
            	((TextView)tv).setText(getString(R.string.about_DroidNetLogin));
            	break;
            case 1:
            	getDialog().setTitle("License");
            	((TextView)tv).setText(getString(R.string.about_license));
            	break;
            case 2:
            	getDialog().setTitle("Donate");
            	((TextView)tv).setText(getString(R.string.about_donate));
     		  
            	ImageButton iv = new ImageButton(getActivity());           	
            	iv.setBackgroundResource(R.drawable.donate);
            	LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            	lp.setMargins(10, 10, 10, 10);
            	iv.setLayoutParams(lp);
            	
              	iv.setOnClickListener(new OnClickListener() {
 	     		   public void onClick(View iv) {
 	     			   Intent intent = new Intent(Intent.ACTION_VIEW);
 	     			   intent.setData(Uri.parse("http://www.google.com"));
 	     			   startActivity(intent);
 	     		   }
 	     		   });
            	
            	lv.addView(iv);
            	break;

            }
            	
            	this.getDialog().setCanceledOnTouchOutside(true);
            	((TextView)tv).setMovementMethod(LinkMovementMethod.getInstance());
            
            
            return v;
        }
    }
    
    
    //This defines the fragment of the login page 
    public static class LoginFragment extends Fragment {
    	EditText editUsername;
    	EditText editPassword;
    	
    	public  LoginFragment newInstance(){
    		LoginFragment l = new LoginFragment();
    		return l;    		
    	}
   
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            
        	View v = inflater.inflate(R.layout.fragment_login, container, false);
            TextView t = (TextView)v.findViewById(R.id.Status);
	    	TextView t2 = (TextView)v.findViewById(R.id.Usage);
	    	TextView t3 = (TextView)v.findViewById(R.id.proxy_status);
	    	ToggleButton b = (ToggleButton)v.findViewById(R.id.Auto);
	    	
	    	
	    	Status = preferences.getInt("Status", -1);
	    	Proxy = preferences.getBoolean("Proxy", false);
	    	IP_Usage = preferences.getInt("IP_Usage", 0);
	    	
	    	switch(Status){
			case 1: t.setText("Status: Logging in");
					break;
			case 2: t.setText("Status: Connected!");
					break;
			case 3: t.setText("Status: Disconnected");
					break;
			case 4: t.setText("Status: Error");
					break;
	    	}
					
			BigDecimal Usage = new BigDecimal(IP_Usage);
			
			t2.setText("Usage: " + Usage.divide(new BigDecimal(1000)) + " MB");
			
			if (Proxy){
				t3.setText("Proxy: Started");
			} else {
				t3.setText("Proxy: Stopped");
			}
			
			if(Proxy_Option){
				if(Status==2&&Proxy==true){
					b.setChecked(true);
				}else{
					b.setChecked(false);
				}
			}else{
				if(Status==2){
					b.setChecked(true);
				}else{
					b.setChecked(false);
				}
			}
			
			//Restoring saved username and password
			
			String encrypted = preferences.getString("password","");
			String username = preferences.getString("username","");
			String password = "";
			
			if (encrypted!=""){
				password = decrypt_pass(encrypted,username);
			}
			
    	    editUsername = (EditText) v.findViewById(R.id.usernameEdit);
    		editUsername.setText(username);
    		editPassword = (EditText) v.findViewById(R.id.passwordEdit);
    		editPassword.setText(password);
    		

    	    final ToggleButton b5 = (ToggleButton) v.findViewById(R.id.Auto);
    	    b5.setOnClickListener(new OnClickListener(){
    			@Override
    			public void onClick(View v) {
    				if(b5.isChecked()){
    					if(true){
    					intent.putExtra("action", 5);
    				
    					 //let Controller know what to do based on the options selected
    					intent.putExtra("debug_option", Debug_Option);
    					intent.putExtra("proxy_option", Proxy_Option);
    					preferenceEditor.putString("username", editUsername.getText().toString());
        				preferenceEditor.putString("password", editPassword.getText().toString());
            			preferenceEditor.commit();
    					LoginFragment.this.getActivity().startService(intent);
    					}
    				}else{
    					intent.putExtra("action",6);
    					LoginFragment.this.getActivity().startService(intent);
    				}
    			}
    			
    		});
        
return v;
    }
        
        @Override
        public void onStop(){
        	super.onStop();
        	String password;
        	String encrypted;
        	String username;
        	//Storing or clearing username and password fields depending on option selected
           	switch(Login_Option){
	    	case 0: preferenceEditor.putString("username", "");
	    			preferenceEditor.putString("password", "");
	    			preferenceEditor.commit();
	    			break;
	    	case 1: preferenceEditor.putString("username", editUsername.getText().toString());
					preferenceEditor.putString("password", "");
					preferenceEditor.commit();
					break;
	    	case 2: username = editUsername.getText().toString();
	    			password = editPassword.getText().toString();
	    			encrypted = encrypt_pass(password,username);
	    			preferenceEditor.putString("username",username);
					preferenceEditor.putString("password", encrypted);
					preferenceEditor.commit();
					break;
					
           	}
        }
        
        
        private String encrypt_pass(String pass,String key){
        	
        	C_Block block[];        	
        	String code="";
        	int i;
        	Key_schedule schedule = new Key_schedule(key);
        	des_encrypt encryptor = new des_encrypt(pass+=1);
        	encryptor.des_cbc_encrypt(schedule);	
        	block = encryptor.get_input_C_Block();
        		
        	for(i=0; i<block.length;i++)
        		code += block[i].toHexString();
        	return code;
        	
        	
        }
        
        private String decrypt_pass(String code, String key){
        	
          	String pass="";
          	
          	//split code in to groups of 16 hex digits as required by hex to C_Block
          	
          	String[] splitLine = new String[(code.length()+15)/16];

          	if (splitLine.length > 0)

	          	{
	          	for (int index = 0, len = splitLine.length-1, lineIndex = 0; index < len; index++)
	
	          	splitLine[index] = code.substring(lineIndex, lineIndex += 16);
	
	          	splitLine[splitLine.length-1] = code.substring(splitLine.length*16 - 16);
	          	}
          	
          	//convert string hex to byte []
          	byte[] temp_byte = new byte [8];
          	byte[] decrypted = new byte[splitLine.length*8];
          	//C_Block block = new C_Block();
          	for (int i =0; i<splitLine.length;i++){
          		try{
          		C_Block block = new C_Block(splitLine[i],16);
          		temp_byte = block.data;
          		}catch(NumberFormatException e){
          			return "";	
          		}
          		System.arraycopy(temp_byte, 0, decrypted, i*8, 8);
          	}
          	
          	//passing the byte[] to the decryptor          	
          	des_encrypt decryptor;
          	byte[] decrypted_all;
          	Key_schedule schedule = new Key_schedule(key);
          	ArrayList<String> pass_array = new ArrayList<String>();
          	
          	decryptor = new des_encrypt(decrypted);
          	decryptor.des_cbc_decrypt(schedule);
          	decrypted = decryptor.get_input();
          	pass = new String(decrypted).trim(); 

        	return pass;
        	        	
        }
        
}


	    
	    
	    @Override
	    public void onPreferenceAttached(PreferenceScreen root, int xmlId){
	    	
	        if(root == null)
	           return; //for whatever reason in very rare cases this is null	        	
	        updatePreference(root.getSharedPreferences());
	        
	        //registering the listeners to the preference screen
	        root.findPreference("login_preference").setOnPreferenceChangeListener(this);
	        root.findPreference("debug_preference").setOnPreferenceChangeListener(this);
	        root.findPreference("proxy_preference").setOnPreferenceChangeListener(this);
	        root.findPreference("manual_login").setOnPreferenceClickListener(this);
	        root.findPreference("manual_logout").setOnPreferenceClickListener(this);
	        root.findPreference("manual_startProxy").setOnPreferenceClickListener(this);
	        root.findPreference("manual_stopProxy").setOnPreferenceClickListener(this);
	        
	        //match summary for Login list preference to selected preference
	        ListPreference login_preference = (ListPreference) root.findPreference("login_preference");
	        CharSequence Login_Summary = login_preference.getEntry();
	        login_preference.setSummary(Login_Summary);
	        
	    }
	    
	    @Override
	    public boolean onPreferenceChange(Preference pref, Object newValue) {
	    	
	    	//match summary for Login list preference to selected preference
	    	
	    	if (pref.getKey().equals("login_preference")){
	    		 ListPreference login_preference = (ListPreference)pref;
	    		 login_preference.setValue((String) newValue);
	    		 CharSequence Login_Summary = login_preference.getEntry();
	    		 login_preference.setSummary(Login_Summary);    		 
	    	}
	    	if (pref.getKey().equals("debug_preference")){
	    		 CheckBoxPreference debug_preference = (CheckBoxPreference)pref;
	    		 debug_preference.setChecked((Boolean) newValue); 		 
	    	}
	    	if (pref.getKey().equals("proxy_preference")){
	    		 CheckBoxPreference proxy_preference = (CheckBoxPreference)pref;
	    		 proxy_preference.setChecked((Boolean) newValue); 		 
	    	}
	    	updatePreference(pref.getSharedPreferences());
	        return true;
	    }
	    
	    @Override
	    public boolean onPreferenceClick(Preference pref){
	    	if (pref.getKey().equals("manual_login")){
	    		intent.putExtra("debug_option", Debug_Option);
				intent.putExtra("proxy_option", Proxy_Option);
		    	LoginFragment Login_Fragment = (LoginFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":0");
		    	EditText usernameEdit = (EditText)Login_Fragment.getActivity().findViewById(R.id.usernameEdit);
				EditText passwordEdit = (EditText)Login_Fragment.getActivity().findViewById(R.id.passwordEdit);
				
	    		SharedPreferences.Editor preferenceEditor = preferences.edit();
				preferenceEditor.putString("username", usernameEdit.getText().toString());
				preferenceEditor.putString("password", passwordEdit.getText().toString());
    			preferenceEditor.commit();
    			intent.putExtra("action", 1);
    			startService(intent);	    		
	    	}else if (pref.getKey().equals("manual_logout")){
    			intent.putExtra("action", 2);
    			startService(intent);	    		
	    	}else if(pref.getKey().equals("manual_startProxy")){
	    		intent.putExtra("debug_option", Debug_Option);
	    		intent.putExtra("action", 3);
				startService(intent);	    		
	    	}else if(pref.getKey().equals("manual_stopProxy")){
	    		intent.putExtra("debug_option", Debug_Option);
	    		intent.putExtra("action", 4);
				startService(intent);	    		
	    	}
	        return true;
	    }
	    
	    private void updatePreference(SharedPreferences pref){
	    	
	    	Debug_Option = pref.getBoolean("debug_preference", false);
	    	Proxy_Option = pref.getBoolean("proxy_preference", true);
	    	
	    	String Login_Option_String = pref.getString("login_preference", "1");
	    	Login_Option = Integer.parseInt(Login_Option_String);

	    	
	    }
	    	
}
	    

	    

   
    
    
