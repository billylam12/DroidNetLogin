package nz.Intelx.DroidNetLogin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import nz.Intelx.DroidNetLogin.Proxy.ShellCommand.CommandResult;


import android.content.Intent;
import android.util.Log;

public class Proxy {
	private static String TAG = "AndNetlogin";
	static final String NET_PREF = "NetloginPref";

	String basedir = null;
	Intent intent_self; //intent received from request to start or stop proxy
	Intent intent_status; //intent to broadcast proxy status
	int START = 1;
	int STOP = 2;
	int action;
	String ipaddr;
	String server = "gate.ec.auckland.ac.nz";
	public static String BROADCAST_ACTION = "nz.Intelx.AndNetlogin.proxy_status";
	
	
	
	public Proxy(String basedir){
		this.basedir = basedir;

	}

		public boolean start(){
		try {
			InetAddress addr = InetAddress.getByName(server);
			ipaddr = addr.getHostAddress();
		} catch (UnknownHostException e) {
			Log.e(TAG, "Cannot resolve hostname"+server);
			return false;
		}
			 		
		ShellCommand cmd = new ShellCommand();
		CommandResult r = cmd.sh.runWaitFor(basedir+"/proxy.sh start " + basedir
					 	+ " " + "socks"
					 	+ " " + ipaddr
						+ " " + "1080"
						+ " " + false
						+ " " + ""
						+ " " + ""
						+ " " + "");
			 
						

			 if (!r.success()) {
				    Log.v("tproxy", "Error starting proxy.sh (" + r.stderr + ")");
					cmd.sh.runWaitFor(basedir+"/proxy.sh stop "+ basedir);
					Log.e(TAG,"Failed to start proxy.sh ("+ r.stderr + ")");
				    return false;
			} 
			 
		if (checklistener()) {
			 r = cmd.su.runWaitFor(basedir+"/redirect.sh start " + "socks");
			 
			 if (!r.success()) {
				    Log.v("tproxy", "Error starting redirect.sh (" + r.stderr +")");
					cmd.sh.runWaitFor(basedir+"/proxy.sh stop "+ basedir);
			} else {
				 	Log.v("tproxy", "Successfully ran redirect.sh start " + "socks");
				 	return true;
			}
		} else {
			Log.w(TAG, "Proxy failed to start");
			return false;
		}
		return false;
	}	
	
	
	public boolean stop() {
		ShellCommand cmd = new ShellCommand();
		cmd.sh.runWaitFor(basedir+"/proxy.sh stop "+basedir);
		cmd.su.runWaitFor(basedir+"/redirect.sh stop");
		Log.v("tproxy", "Successfully ran redirect.sh stop");
		return false;
		
	}
	
	private boolean checklistener() {
		Socket socket = null;
		try {
			socket = new Socket("127.0.0.1", 8123);
		} catch (Exception e) {
		}

		if (socket != null && socket.isConnected()) {
			try {
				socket.close();
			} catch (Exception e) {
			}
			return true;
		} else {
			return false;
		}
	}
	
	public class ShellCommand {
	    private static final String TAG = "ShellCommand.java";
	    private Boolean can_su;   

	    public SH sh;
	    public SH su;
	   
	    public ShellCommand() {
	        sh = new SH("sh");
	        su = new SH("su");
	    }
	   
	    public boolean canSU() {
	        return canSU(false);
	    }
	   
	    public boolean canSU(boolean force_check) {
	        if (can_su == null || force_check) {
	            CommandResult r = su.runWaitFor("id");
	            StringBuilder out = new StringBuilder();
	           
	            if (r.stdout != null)
	                out.append(r.stdout).append(" ; ");
	            if (r.stderr != null)
	                out.append(r.stderr);
	           
	            Log.v(TAG, "canSU() su[" + r.exit_value + "]: " + out);
	            can_su = r.success();
	        }
	        return can_su;
	    }

	    public SH suOrSH() {
	        return canSU() ? su : sh;
	    }
	   
	    public class CommandResult {
	        public final String stdout;
	        public final String stderr;
	        public final Integer exit_value;
	       
	        CommandResult(Integer exit_value_in, String stdout_in, String stderr_in)
	        {
	            exit_value = exit_value_in;
	            stdout = stdout_in;
	            stderr = stderr_in;
	        }
	       
	        CommandResult(Integer exit_value_in) {
	            this(exit_value_in, null, null);
	        }
	       
	        public boolean success() {
	            return exit_value != null && exit_value == 0;
	        }
	    }

	    public class SH {
	        private String SHELL = "sh";

	        public SH(String SHELL_in) {
	            SHELL = SHELL_in;
	        }

	        public Process run(String s) {
	            Process process = null;
	            try {
	                process = Runtime.getRuntime().exec(SHELL);
	                DataOutputStream toProcess = new DataOutputStream(process.getOutputStream());
	                toProcess.writeBytes("exec " + s + "\n");
	                toProcess.flush();
	            } catch(Exception e) {
	                Log.e(TAG, "Exception while trying to run: '" + s + "' " + e.getMessage());
	                process = null;
	            }
	            return process;
	        }
	       
	        private String getStreamLines(InputStream is) {
	            String out = null;
	            StringBuffer buffer = null;
	            DataInputStream dis = new DataInputStream(is);

	            try {
	                if (dis.available() > 0) {
	                    buffer = new StringBuffer(dis.readLine());
	                    while(dis.available() > 0)
	                        buffer.append("\n").append(dis.readLine());
	                }
	                dis.close();
	            } catch (Exception ex) {
	                Log.e(TAG, ex.getMessage());
	            }
	            if (buffer != null)
	                out = buffer.toString();
	            return out;
	        }

	        public CommandResult runWaitFor(String s) {
	            Process process = run(s);
	            Integer exit_value = null;
	            String stdout = null;
	            String stderr = null;
	            if (process != null) {
	                try {
	                    exit_value = process.waitFor();
	                   
	                    stdout = getStreamLines(process.getInputStream());
	                    stderr = getStreamLines(process.getErrorStream());
	                   
	                } catch(InterruptedException e) {
	                    Log.e(TAG, "runWaitFor " + e.toString());
	                } catch(NullPointerException e) {
	                    Log.e(TAG, "runWaitFor " + e.toString());
	                }
	            }
	            return new CommandResult(exit_value, stdout, stderr);
	        }
	    }
	
				
	}
}
	
	
	
	
	
	
	
	
	
	
	
	
	
	

