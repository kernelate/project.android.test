package com.ntek.wallpad.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.ntek.wallpad.Database.DbHandler;
import com.ntek.wallpad.Database.DbHelper.EventInquiry;
import com.ntek.wallpad.Model.EventInquiryList;
import com.ntek.wallpad.Model.EventInquiryModel;
import com.ntek.wallpad.Utils.NotificationEmailFacebookData;

public class SocClient implements Runnable{
	private Context m_context = null;
	private String socID;
	private int Soc_Client_Port = 5555;
	
	// 2014.07.18 SmartBean_SHCHO : ADD VARIABLE FOR OSTYPE, SIP, NOTIFICATION
	private static final String OSTYPE = "a";
	private static final String SETUP_SIP = "SETUP_SIP";
	private static final String LOGIN = "LOGIN";
	private static final String NOTIFICATION = "NOTIFICATION";
	private static final String EVENT_INQUIRY_NOTIFICATION = "EVENT_INQUIRY_NOTIFICATION";
	private static final String EVENT_INQUIRY_NOTIFICATION_UPDATE = "EVENT_INQUIRY_NOTIFICATION_UPDATE";
	private static final String EVENT_INQUIRY_NOTIFICATION_UPDATE_EMAIL = "EVENT_INQUIRY_NOTIFICATION_UPDATE_EMAIL";
	private static final String CHANGE_LOGIN_DATA = "CHANGE_LOGIN_DATA";
	private static final String SETUP_NETWORK = "SETUP_NETWORK";
	private static final String EVENT_SENSOR = "EVENT_SENSOR";
	private static final String EVENT_INQUIRY_CLIENT_LIST_UPDATE = "EVENT_INQUIRY_CLIENT_LIST_UPDATE";
	private static final String EVENT_INQUIRY_GET_CLIENT_DATA = "EVENT_INQUIRY_GET_CLIENT_DATA";
	private static final String EVENT_INQUIRY_DELETE_CLIENT_DATA = "EVENT_INQUIRY_DELETE_CLIENT_DATA";
	private static final String SEND_DEVICE_INFO = "SEND_DEVICE_INFO";
	private static final String SEND_INBOUND_BLOCKLIST = "SEND_INBOUND_BLOCKLIST";
	private static final String SEND_OUTBOUND_PRIORITYLIST = "SEND_OUTBOUND_PRIORITYLIST";
	private static final String VOLUME_CONTROL = "VOLUME_CONTROL";
	private static final String EVENT_SENSOR_DOORCONTROL_UPDATE = "EVENT_SENSOR_DOORCONTROL_UPDATE";
	
	Socket socket = null;
	boolean error_flag = false;
	
    public SocClient(String string, int port) {
    	socID = string;
    	if(port > 0) {
    		this.Soc_Client_Port = port;
    	}
	}
    
    public SocClient(String string, int port, Context context) {
    	socID = string;
    	m_context = context; 
    	if(port > 0) {
    		this.Soc_Client_Port = port;
    	}
    }
    
	public void run() {
        try {
        	error_flag = false;
        	Log.d("TCP", "OpenClient : " + Global.getInstance().getData());
            InetAddress serverAddr = InetAddress.getByName(Global.getInstance().getData());
           
            SocketAddress socketAddr = new InetSocketAddress(serverAddr, Soc_Client_Port);
            if(socket == null) {
            	socket = new Socket();
            }            
            Log.d("TCP", "C : Connecting...");
            socket.connect(socketAddr, 7000);
            
            if (socID.equals("doortalk")) {
            	PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
                    
                    out.println("doortalk");
                    Log.d("TCP", "C : Sent.");
                    Log.d("TCP", "C : Done.");
                	
                    socket.setSoTimeout(7000);
                	BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                    String str = in.readLine();
                    
                    // send broadcast after successfully getting data from server
                    sendBroadcast(Global.TCP_GET_DOORTALK_DATA_CALLBACK, "doortalk", "success");
                    
                    System.out.println("----------------------------------------");
                    System.out.println("str value : " + str);
                    System.out.println("----------------------------------------");
                    
                    JSONObject json = new JSONObject(str);
                    
                    String macAddress = json.optString("macAddress", null);
                    Global.getInstance().setMacAddress(macAddress);
                    
                    String platformVersion = json.optString("platformVersion", null);
                    Global.getInstance().setPlatformVersion(platformVersion);
                    
                    Log.d("TCP", "str : "+str);
                    String name = json.getString("name");						// Client SIP Number
                	String server_impi = json.getString("server_impi");					// Device IMPI
                	String password = json.getString("password");				// Device SIP Password
                	String adress = json.getString("adress");					// Device SIP Server address
                	String proxyhost = json.getString("proxyhost");				// Device Proxy Host
//                	String callnumber = json.getString("callnumber");				// CallNumber(Device > Client)
					//String email = json.getString("email");
              	  	String gcm_url= json.getString("gcm_url");					// GCM(APNS) Server address
              	  	//String registerid= "";				// GCM id(APNS id)
              	  	int confPort = json.getInt("confPort");                     // Device proxy host port
              	  	
					// 2014.08.25 SmartBean_SHCHO : ADD FLAG FOR MOTION, ALARM A, ALARM B ON/OFF FLAG
              	  	String motion_onoff = json.getString("motion_onoff");			// Motion onoff type 
              	  	String alarm_a_onoff = json.getString("alarm_a_onoff");			// Alarm A onoff flag
              	  	String alarm_b_onoff = json.getString("alarm_b_onoff");			// Alarm B onoff flag
              	  	
              	  	String motion_emailCheck= json.getString("motion_emailCheck");	// Motion Event E-Mail check flag
              	  	String motion_GCMCheck= json.getString("motion_GCMCheck");     	// Motion Event GCM check flag
              	  	String motion_FACEBOOKCheck= json.getString("motion_FACEBOOKCheck");	// Motion Event FACEBOOK check flag
              	  	
            	  	String alarm_a_EmailCheck= json.getString("alarm_a_EmailCheck");		// Alarm A Event GCM check flag
              	  	String alarm_a_GCMCheck= json.getString("alarm_a_GCMCheck");		// Alarm A Event E-Mail check flag
              	  	String alarm_a_FACEBOOKCheck= json.getString("alarm_a_FACEBOOKCheck");	// Alarm A Event FACEBOOK check flag
              	  	
              	  	String alarm_b_EmailCheck= json.getString("alarm_b_EmailCheck");		// Alarm B Event GCM check flag
            	  	String alarm_b_GCMCheck= json.getString("alarm_b_GCMCheck");		// Alarm B Event E-Mail check flag
            	  	String alarm_b_FACEBOOKCheck= json.getString("alarm_b_FACEBOOKCheck");	// Alarm B Event FACEBOOK check flag
                    //String facebook_name_list = json.getString("facebook_name_list");
                    //String facebook_id_list = json.getString("facebook_id_list");
    				
                    //String facebookID = json.getString("facebookID");
                    //String facebookPassWord = json.getString("facebookPassWord");
                    
                    /*if(registerid!=null && registerid.length()>8 && registerid.substring(0, 9).equals("IDENTITY_")) {
                    	registerid = Global.getInstance().getRegisterId();
                    }*/
            	  	int eventServerPort = json.getInt("event_server_port");
            	  	String eventServerUrl = json.getString("event_server_url");
//            	  	boolean flagEventRegistered = json.getBoolean("flag_event_registered");
            	  	
            		String inboundBlockList = json.getString("inbound_blocklist");
            		String outboundPriorityList = json.getString("outbound_prioritylist");
            		
            		int Server_Speaker_volume = json.getInt("Server_Speaker_volume");
            		int Server_Call_volume = json.getInt("Server_Call_volume");
            		
            		// SET DEVICE VOLUME
            		Global.getInstance().setServer_Speaker_volume(Server_Speaker_volume);
            		Global.getInstance().setServer_Call_volume(Server_Call_volume);
            		
            		// Outbound call priorty and Inbound call blocking
            	  	Global.getInstance().setInboundBlockList(inboundBlockList);
            		Global.getInstance().setOutboundPriorityList(outboundPriorityList);
            		
            	  	//Event Server Data
            	  	Global.getInstance().setEventServerPort(eventServerPort);
            	  	Global.getInstance().setEventServerUrl(eventServerUrl);
//            		Global.getInstance().setFlagEventRegistered(flagEventRegistered);
            		
                    // Get Login Information 
            	    String loginID = json.getString("loginID");
                    String loginPassword = json.getString("loginPassword");
            	  	
                    LoginGlobal.getInstance().setLoginID(loginID);
                    LoginGlobal.getInstance().setLoginPassword(loginPassword);
                    
                    // Get Network Information 
                    String networkType = json.getString("networkType");
                    String networkSSID = json.getString("networkSSID");
                    String networkPassword = json.getString("networkPassword");
                    String networkSecurity = json.getString("networkSecurity");
                    String networkIpSettings = json.getString("networkIpSettings");
                    String networkIpAddress = json.getString("networkIpAddress");
                    String networkNetmask = json.getString("networkNetmask");
                    String networkGateway = json.getString("networkGateway");
                    String networkDNS1 = json.getString("networkDNS1");
                    String networkDNS2 = json.getString("networkDNS2");
                    
                    //Auto provision
                    boolean autoProvisionMode = json.optBoolean("autoProvisionMode");
                    String autoProvisionDeviceKey = json.optString("autoProvisionDeviceKey", null);
                    String autoProvisionType = json.optString("autoProvisionType", null);
                    String autoProvisionIpAddress = json.optString("autoProvisionIpAddress", null);
                    String autoProvisionPort = json.optString("autoProvisionPort", null);
                    
                    NetworkGlobal.getInstance().setType(networkType);
                    NetworkGlobal.getInstance().setSsid(networkSSID);
                    NetworkGlobal.getInstance().setPw(networkPassword);
                    NetworkGlobal.getInstance().setSecurity(networkSecurity);
                    Log.d("TCP", "C : Server networkIpSettings." + networkIpSettings);
                    NetworkGlobal.getInstance().setIpSettings(networkIpSettings);
                    NetworkGlobal.getInstance().setIp(networkIpAddress);
                    NetworkGlobal.getInstance().setNetmask(networkNetmask);
                    NetworkGlobal.getInstance().setGateway(networkGateway);
                    NetworkGlobal.getInstance().setDns1(networkDNS1);
                    NetworkGlobal.getInstance().setDns2(networkDNS2);
                    
                    //Auto Provision
                    NetworkGlobal.getInstance().setProvisionMode(autoProvisionMode);
                    NetworkGlobal.getInstance().setProvisionSerialKey(autoProvisionDeviceKey);
                    NetworkGlobal.getInstance().setProvisionType(autoProvisionType);
                    NetworkGlobal.getInstance().setProvisionIpAddress(autoProvisionIpAddress);
                    NetworkGlobal.getInstance().setProvisionPort(autoProvisionPort);
                    
                    Log.d("TCP", "C : Server server_impi." + server_impi);
                    Log.d("TCP", "C : Server password." + password);
                    Log.d("TCP", "C : Server adress." + adress);                    
//                    Log.d("TCP", "C : Server callnumber." + callnumber);
                    //Log.d("TCP", "C : Server registerid." + registerid);
                    Log.d("TCP", "C : Server gcm_url." + gcm_url);
                    Log.d("TCP", "C : Server name." + name);
                    Log.d("TCP", "C : Server motion_onoff." + motion_onoff);
                    Log.d("TCP", "C : Server alarm_a_onoff." + alarm_a_onoff);
                    Log.d("TCP", "C : Server alarm_b_onoff." + alarm_b_onoff);
                    Log.d("TCP", "C : Server Server_Speaker_volume." + Server_Speaker_volume);
                    Log.d("TCP", "C : Server Server_Call_volume." + Server_Call_volume);
					
                    Global.getInstance().setName(name);
					Global.getInstance().set_server_Impi(server_impi);
					Global.getInstance().setPw(password);
					Global.getInstance().setAdress(adress);
					Global.getInstance().setProxyHost(proxyhost);
//                    Global.getInstance().setCallNumber(callnumber);
                    //Global.getInstance().setEmail(email);
                    Global.getInstance().setGcmUrl(gcm_url);
                    //Global.getInstance().setRegisterId(registerid);
                    Global.getInstance().setConfPort(confPort);
                    
                    Global.getInstance().set_Motion_ONOFF(motion_onoff);
                    Global.getInstance().set_Alarm_A_ONOFF(alarm_a_onoff);
                    Global.getInstance().set_Alarm_B_ONOFF(alarm_b_onoff);
                    
                    Global.getInstance().set_Motion_EmailCheck(motion_emailCheck);
                    Global.getInstance().set_Motion_GCMCheck(motion_GCMCheck);
                    Global.getInstance().set_Motion_FACEBOOKCheck(motion_FACEBOOKCheck);
                    
                    Global.getInstance().setAlarm_A_EmailCheck(alarm_a_EmailCheck);
                    Global.getInstance().setAlarm_A_GCMCheck(alarm_a_GCMCheck);
                    Global.getInstance().setAlarm_A_FACEBOOKCheck(alarm_a_FACEBOOKCheck);
                    
                    Global.getInstance().setAlarm_B_EmailCheck(alarm_b_EmailCheck);
                    Global.getInstance().setAlarm_B_GCMCheck(alarm_b_GCMCheck);
                    Global.getInstance().setAlarm_B_FACEBOOKCheck(alarm_b_FACEBOOKCheck);
                    
                 // send broadcast after successfully getting data from server
                    sendBroadcast(Global.TCP_GET_DOORTALK_DATA_CALLBACK, "doortalk", "success");
                    
                    //Global.getInstance().setFaceBook_Friend_Name_List(facebook_name_list);
                    //Global.getInstance().setFaceBook_Friend_Id_List(facebook_id_list);
                    //Global.getInstance().setFaceBookID(facebookID);
                    //Global.getInstance().setFaceBookPassWord(facebookPassWord);
                    
                    //------------------------EVENT INQUIRY--------------------------------
                    //db.delete_all_event_inquiry(); //delete first all data in event inquiry;
//                    if (Global.getInstance().getEventInquiryObservableList() != null)
//                    {
//                    	if (Global.getInstance().getEventInquiryObservableList().getSize() > 0)
//                    	{
//                    		Global.getInstance().getEventInquiryObservableList().clear();
//                    	}
//                    }
//                    if (checkDataOnJson(json, "Event Inquiry")){
//                    	final JSONArray responseArray = json.getJSONArray("Event Inquiry");
//   		                final int arrayLength = responseArray.length();
//   						EventInquiryList mEventModel = new EventInquiryList();
//   		    			for (int i = 0; i < arrayLength; i++){
//   		    				String gcmID = "";
//   		    				String email = "";
//   		    				String activeStatus = "";
//   		    				String motion_detect_enable = "";
//   		    				String relay_sensors_enable ="";
//   		    				String client_os_type = "";
//   		    				String created_date = "";
//   		    				final JSONObject obj = responseArray.getJSONObject(i);
////   							String unique_id = obj.getString("device_unique_key");
//   		    				if (checkDataOnJson(obj,"client_GCMID"))
//   		    					gcmID = obj.getString("client_GCMID");
//   		    				if (checkDataOnJson(obj,"client_email"))
//   		    					email = obj.getString("client_email");
//   		    				if (checkDataOnJson(obj,"activeStatus"))
//   		    					activeStatus = obj.getString("activeStatus");
//   		    				if (checkDataOnJson(obj,"motionDetect_enable_YN"))
//   		    					motion_detect_enable = obj.getString("motionDetect_enable_YN");
//   		    				if (checkDataOnJson(obj,"relaySensors_enable_YN"))
//   		    					relay_sensors_enable = obj.getString("relaySensors_enable_YN");
//   		    				if (checkDataOnJson(obj,"client_os_type"))
//   		    					client_os_type = obj.getString("client_os_type");
//   		    				if (checkDataOnJson(obj,"created_date"))
//   		    					created_date = obj.getString("created_date");
//   							
//   		    				EventInquiryModel model = new EventInquiryModel();
//   		    				model.setEmail(email);
//   		    				model.setGcmID(gcmID);
//   		    				model.setMotion_detect_enable(motion_detect_enable);
//   		    				model.setRelay_sensors_enable(relay_sensors_enable);
//   		    				model.setClient_os_type(client_os_type);
//   		    				model.setActiveStatus(activeStatus);
//   		    				model.setCreated_date(created_date);
//   		    				mEventModel.add(model);
//   		    			}
//   		    			Global.getInstance().setEventInquiryAccounts(mEventModel);
//                    }
                    
                	DbHandler db = DbHandler.open(NgnEngine.getInstance().getMainActivity());
                    db.delete_all_event_inquiry(); //delete first all data in event inquiry;
                    if (checkDataOnJson(json, "Event Inquiry")){
                    	final JSONArray responseArray = json.getJSONArray("Event Inquiry");
   		                final int arrayLength = responseArray.length();
   		            	SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   						Date date = new Date();
   					
   						String update_date = dateformat.format(date);
   		    			for (int i = 0; i < arrayLength; i++){
   		    				String gcmID = "";
   		    				String email = "";
   		    				String activeStatus = "";
   		    				String motion_detect_enable = "";
   		    				String relay_sensors_enable ="";
   		    				String client_os_type = "";
   		    				String created_date = "";
   		    				final JSONObject obj = responseArray.getJSONObject(i);
//   							String unique_id = obj.getString("device_unique_key");
   		    				if (checkDataOnJson(obj,"client_GCMID"))
   		    					gcmID = obj.getString("client_GCMID");
   		    				if (checkDataOnJson(obj,"client_email"))
   		    					email = obj.getString("client_email");
   		    				if (checkDataOnJson(obj,"activeStatus"))
   		    					activeStatus = obj.getString("activeStatus");
   		    				if (checkDataOnJson(obj,"motionDetect_enable_YN"))
   		    					motion_detect_enable = obj.getString("motionDetect_enable_YN");
   		    				if (checkDataOnJson(obj,"relaySensors_enable_YN"))
   		    					relay_sensors_enable = obj.getString("relaySensors_enable_YN");
   		    				if (checkDataOnJson(obj,"client_os_type"))
   		    					client_os_type = obj.getString("client_os_type");
   		    				if (checkDataOnJson(obj,"created_date"))
   		    					created_date = obj.getString("created_date");
   							
   							long result = 0;
   							if (db.check_if_gcmID_if_exist(gcmID) || 
   									db.check_record_if_exist(gcmID, email, activeStatus, motion_detect_enable, relay_sensors_enable, update_date, created_date))
   								result = db.update_inquiry(gcmID, email, activeStatus, motion_detect_enable, relay_sensors_enable, client_os_type, update_date, created_date);
   							else
   								result = db.insert_inquiry(gcmID, email, activeStatus, motion_detect_enable, relay_sensors_enable, client_os_type, update_date, created_date);
   							
   							if (result > 0) System.out.println(gcmID + " : Success EventInquiry Update : " + result);
   							else System.out.println(gcmID + " : Failed EventInquiry Update : " + result);
   		    			}
                    }
                 
                    //-------------------------Security Functions Motion------------------------------
//	    			if (checkDataOnJson(json, "Motion Detect")){
//	    				final JSONArray responseArray = json.getJSONArray("Motion Detect");
//	    				final int arrayLength = responseArray.length();
//	    				for (int i = 0; i < arrayLength; i++){
//	    					final JSONObject obj = responseArray.getJSONObject(i);
//	    					String motion_status = obj.getString("enable_YN");
//	    					String motion_sensitivity = obj.getString("sensitivity");
//	    					int motionSensitivity = Integer.parseInt(motion_sensitivity);
//	    					
//	    					Global.getInstance().setMotionSensitivity(motionSensitivity);
//	    					Global.getInstance().set_Motion_ONOFF(motion_status);
//	    				}
//	    			}
                    if (checkDataOnJson(json, "Motion Detect")){
	    				final JSONArray responseArray = json.getJSONArray("Motion Detect");
	    				final int arrayLength = responseArray.length();
	    				for (int i = 0; i < arrayLength; i++){
	    					final JSONObject obj = responseArray.getJSONObject(i);
	    					String motion_status = obj.getString("enable_YN");
	    					String motion_sensitivity = obj.getString("sensitivity");
	    					int motionSensitivity = Integer.parseInt(motion_sensitivity);
	    					
	    					Global.getInstance().setMotionSensitivity(motionSensitivity);
	    					Global.getInstance().set_Motion_ONOFF(motion_status);
	    				}
	    			}
                    
	    			//-------------------------Security Functions Door Control------------------------------
//	    			if (checkDataOnJson(json, "Relay Sensor")){
//	    				final JSONArray responseArray = json.getJSONArray("Relay Sensor");
//	    				final int arrayLength = responseArray.length();
//	    				for (int i = 0; i < arrayLength; i++) {
//							final JSONObject obj = responseArray.getJSONObject(i);
//							Global.getInstance().set_Doorcontrol_onoff(obj.getString("enable_yn"));
//							Global.getInstance().setRelay_sensor_nickname(obj.getString("nickname"));
//							Global.getInstance().setSignal1_printed_name(obj.getString("signal1_printed_name"));
//							Global.getInstance().setSignal1_transaction_value(obj.getString("signal1_transaction_value"));
//							Global.getInstance().setSignal1_duration_value(obj.getString("signal1_duration_value"));
//							Global.getInstance().setSignal1_transaction_notification_YN(obj.getString("signal1_transaction_notification_YN"));
//							Global.getInstance().setSignal1_duration_notification_yn(obj.getString("signal1_duration_notification_yn"));
//							Global.getInstance().setSignal2_printed_name(obj.getString("signal2_printed_name"));
//							Global.getInstance().setSignal2_transaction_value(obj.getString("signal2_transaction_value"));
//							Global.getInstance().setSignal2_duration_value(obj.getString("signal2_duration_value"));
//							Global.getInstance().setSignal2_transaction_notification_YN(obj.getString("signal2_transaction_notification_YN"));
//							Global.getInstance().setSignal2_duration_notification_yn(obj.getString("signal2_duration_notification_yn"));
//	    				}
//	    			}
	    			if (checkDataOnJson(json, "Relay Sensor")){
	    				final JSONArray responseArray = json.getJSONArray("Relay Sensor");
	    				final int arrayLength = responseArray.length();
	    				for (int i = 0; i < arrayLength; i++) {
							final JSONObject obj = responseArray.getJSONObject(i);
							Global.getInstance().set_Doorcontrol_onoff(obj.getString("enable_yn"));
							Global.getInstance().set_Door_lock_status(obj.getString("Door_lock_status"));
							Global.getInstance().set_Auto_lock_time(obj.getString("Auto_lock_time"));
							Global.getInstance().set_Auto_lock_onoff(obj.getString("Auto_lock_onoff"));
							Global.getInstance().setRelay_sensor_nickname(obj.getString("nickname"));
							Global.getInstance().setSignal1_printed_name(obj.getString("signal1_printed_name"));
							Global.getInstance().setSignal1_transaction_value(obj.getString("signal1_transaction_value"));
							Global.getInstance().setSignal1_duration_value(obj.getString("signal1_duration_value"));
							Global.getInstance().setSignal1_transaction_notification_YN(obj.getString("signal1_transaction_notification_YN"));
							Global.getInstance().setSignal1_duration_notification_yn(obj.getString("signal1_duration_notification_yn"));
							Global.getInstance().setSignal2_printed_name(obj.getString("signal2_printed_name"));
							Global.getInstance().setSignal2_transaction_value(obj.getString("signal2_transaction_value"));
							Global.getInstance().setSignal2_duration_value(obj.getString("signal2_duration_value"));
							Global.getInstance().setSignal2_transaction_notification_YN(obj.getString("signal2_transaction_notification_YN"));
							Global.getInstance().setSignal2_duration_notification_yn(obj.getString("signal2_duration_notification_yn"));
	    				}
	    			}
	    			
	    			
	    			//----------------------EMAIL NOTIFICATION------------------------------
	    			if (Global.getInstance().getNotificationChannelAccounts() != null)
	    			{
	    				if (Global.getInstance().getNotificationChannelAccounts().size() > 0)
	    				{
	    					Global.getInstance().getNotificationChannelAccounts().clear();
	    				}
	    			}
	    			if (checkDataOnJson(json, "Email Notification")){
	    				final JSONArray responseArray = json.getJSONArray("Email Notification");
	    				final int arrayLength = responseArray.length();
	    				ArrayList<NotificationEmailFacebookData> notificationAccounts = new ArrayList<NotificationEmailFacebookData>();
	    				for (int i = 0; i < arrayLength; i++) {
							final JSONObject obj = responseArray.getJSONObject(i);
							NotificationEmailFacebookData mData = new NotificationEmailFacebookData();
							mData.setmNotificationChannelAccounts(obj.getInt("notificationID"));
							mData.setmChannelCode(obj.getString("channel_code"));
							mData.setmDoorControl_ONOFF(obj.getString("sensors_enable_YN"));
							mData.setmMotion_ONOFF(obj.getString("motionDetect_enable_YN"));
							mData.setmEmail(obj.getString("account"));
							notificationAccounts.add(mData);
						}
	    				Global.getInstance().setNotificationChannelAccounts(notificationAccounts);
	    			}
	    			
	    			sendBroadcast("com.smartbean.servertalk.action.doortalk", "response", "finish");
            }
            else if (socID.equals("VOLUME_CONTROL")) {
            	JSONObject message = new JSONObject();
            	
            	message.put("type", VOLUME_CONTROL);
            	message.put("Server_Speaker_volume", Global.getInstance().getServer_Speaker_volume());
            	message.put("Server_Call_volume", Global.getInstance().getServer_Call_volume());
            	
            	Log.d("TCP", "C : Sending : '" + message + "'");
                PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())), true);
                
                out.println(message);
                Log.d("TCP", "C : Sent.");
                Log.d("TCP", "C : Done.");
                socket.setSoTimeout(7000);
                
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String str = in.readLine();
                Log.d("TCP", "str : "+str);
                JSONObject json = new JSONObject(str);
                String response = json.getString("response");
                
                sendBroadcast("com.smartbean.servertalk.action.SENDING_VOLUME", "response", response);
            }
            else if (socID.equals("outbound_prioritylist")) {
            	JSONObject message = new JSONObject();
            	
            	message.put("type", SEND_OUTBOUND_PRIORITYLIST);
            	message.put("outbound_prioritylist", Global.getInstance().getOutboundPriorityList());
            	
            	Log.d("TCP", "C : Sending : '" + message + "'");
                PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())), true);
                
                out.println(message);
                Log.d("TCP", "C : Sent.");
                Log.d("TCP", "C : Done.");
                socket.setSoTimeout(7000);
                
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String str = in.readLine();
                Log.d("TCP", "str : "+str);
                JSONObject json = new JSONObject(str);
                String response = json.getString("response");
                
                sendBroadcast("com.smartbean.servertalk.action.SENDING_OUTBOUND_PRIORITYLIST_CALLBACK", "response", response);
            }
            else if (socID.equals("inbound_blocklist")) {
            	JSONObject message = new JSONObject();
            	
            	message.put("type", SEND_INBOUND_BLOCKLIST);
            	message.put("inbound_blocklist", Global.getInstance().getInboundBlockList());
            	
            	Log.d("TCP", "C : Sending : '" + message + "'");
                PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())), true);
                
                out.println(message);
                Log.d("TCP", "C : Sent.");
                Log.d("TCP", "C : Done.");
                socket.setSoTimeout(7000);
                
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String str = in.readLine();
                Log.d("TCP", "str : "+str);
                JSONObject json = new JSONObject(str);
                String response = json.getString("response");
                
                sendBroadcast("com.smartbean.servertalk.action.SENDING_INBOUND_BLOCKLIST_CALLBACK", "response", response);
            }
            else if (socID.equals("CheckEventSecurity")) {
            	PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
                    
                out.println("CheckEventSecurity");
                Log.d("TCP", "C : Sent.");
                Log.d("TCP", "C : Done.");
                
                socket.setSoTimeout(7000);
                
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String str = in.readLine();
                Log.d("TCP", "str : "+str);
                
                JSONObject json = new JSONObject(str);
                //-------------------------Security Functions Motion------------------------------
    			if (checkDataOnJson(json, "Motion Detect")){
    				final JSONArray responseArray = json.getJSONArray("Motion Detect");
    				final int arrayLength = responseArray.length();
    				for (int i = 0; i < arrayLength; i++){
    					final JSONObject obj = responseArray.getJSONObject(i);
    					String motion_status = obj.getString("enable_YN");
    					String motion_sensitivity = obj.getString("sensitivity");
    					int motionSensitivity = Integer.parseInt(motion_sensitivity);
    					
    					Global.getInstance().setMotionSensitivity(motionSensitivity);
    					Global.getInstance().set_Motion_ONOFF(motion_status);
    				}
    			}
    			//-------------------------Security Functions Door Control------------------------------
//    			if (checkDataOnJson(json, "Relay Sensor")){
//    				final JSONArray responseArray = json.getJSONArray("Relay Sensor");
//    				final int arrayLength = responseArray.length();
//    				for (int i = 0; i < arrayLength; i++) {
//						final JSONObject obj = responseArray.getJSONObject(i);
//						Global.getInstance().set_Doorcontrol_onoff(obj.getString("enable_yn"));
//						Global.getInstance().setRelay_sensor_nickname(obj.getString("nickname"));
//						Global.getInstance().setSignal1_printed_name(obj.getString("signal1_printed_name"));
//						Global.getInstance().setSignal1_transaction_value(obj.getString("signal1_transaction_value"));
//						Global.getInstance().setSignal1_duration_value(obj.getString("signal1_duration_value"));
//						Global.getInstance().setSignal1_transaction_notification_YN(obj.getString("signal1_transaction_notification_YN"));
//						Global.getInstance().setSignal1_duration_notification_yn(obj.getString("signal1_duration_notification_yn"));
//						Global.getInstance().setSignal2_printed_name(obj.getString("signal2_printed_name"));
//						Global.getInstance().setSignal2_transaction_value(obj.getString("signal2_transaction_value"));
//						Global.getInstance().setSignal2_duration_value(obj.getString("signal2_duration_value"));
//						Global.getInstance().setSignal2_transaction_notification_YN(obj.getString("signal2_transaction_notification_YN"));
//						Global.getInstance().setSignal2_duration_notification_yn(obj.getString("signal2_duration_notification_yn"));
//    				}
//    			}
    			
    			if (checkDataOnJson(json, "Relay Sensor")){
    				final JSONArray responseArray = json.getJSONArray("Relay Sensor");
    				final int arrayLength = responseArray.length();
    				for (int i = 0; i < arrayLength; i++) {
						final JSONObject obj = responseArray.getJSONObject(i);
						Global.getInstance().set_Doorcontrol_onoff(obj.getString("enable_yn"));
						Global.getInstance().set_Door_lock_status(obj.getString("Door_lock_status"));
						Global.getInstance().set_Auto_lock_time(obj.getString("Auto_lock_time"));
						Global.getInstance().set_Auto_lock_onoff(obj.getString("Auto_lock_onoff"));
						Global.getInstance().setRelay_sensor_nickname(obj.getString("nickname"));
						Global.getInstance().setSignal1_printed_name(obj.getString("signal1_printed_name"));
						Global.getInstance().setSignal1_transaction_value(obj.getString("signal1_transaction_value"));
						Global.getInstance().setSignal1_duration_value(obj.getString("signal1_duration_value"));
						Global.getInstance().setSignal1_transaction_notification_YN(obj.getString("signal1_transaction_notification_YN"));
						Global.getInstance().setSignal1_duration_notification_yn(obj.getString("signal1_duration_notification_yn"));
						Global.getInstance().setSignal2_printed_name(obj.getString("signal2_printed_name"));
						Global.getInstance().setSignal2_transaction_value(obj.getString("signal2_transaction_value"));
						Global.getInstance().setSignal2_duration_value(obj.getString("signal2_duration_value"));
						Global.getInstance().setSignal2_transaction_notification_YN(obj.getString("signal2_transaction_notification_YN"));
						Global.getInstance().setSignal2_duration_notification_yn(obj.getString("signal2_duration_notification_yn"));
    				}
    			}
            }
            
            else if (socID.equals("connect")) {
            	PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
                    
                out.println("connect");
                Log.d("TCP", "C : Sent.");
                Log.d("TCP", "C : Done.");
                
                socket.setSoTimeout(7000);
                
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String str = in.readLine();
                Log.d("TCP", "str : "+str);
                JSONObject json = new JSONObject(str);
                String response = json.getString("connect");
                
            	if((response.length() > 8 && response.substring(0, 9).equals("IDENTITY_")))
            		response = "UNNAMED_DOORTALK";
            	// TODO error
                Global.getInstance().setName(response);
                sendBroadcast(Global.TCP_CONNECT_CALLBACK, "connect", "success");
            }
            else if(socID.equals("login")) { // 2014.08.18 Neil : Check device login	
            	JSONObject message = new JSONObject();
            	
            	message.put("type", LOGIN);
            	message.put("loginID", LoginGlobal.getInstance().getLoginID());
            	message.put("loginPassword", LoginGlobal.getInstance().getLoginPassword());
            	
            	Log.d("TCP", "C : Sending : '" + message + "'");
                PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())), true);
                
                out.println(message);
                Log.d("TCP", "C : Sent.");
                Log.d("TCP", "C : Done.");
                
                socket.setSoTimeout(7000);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String str = in.readLine();
                Log.d("TCP", "str : " + str);

                JSONObject json = new JSONObject(str);
                String response = json.getString("login");
                sendBroadcast(LoginGlobal.TCP_LOGIN_CALLBACK, "login", response);
            }
            else if(socID.equals("login_change")) {// 2014.08.18 Neil : Device login change	
            	JSONObject message = new JSONObject();
            	
            	message.put("type", CHANGE_LOGIN_DATA);
            	message.put("loginID", LoginGlobal.getInstance().getLoginID());
            	message.put("loginPassword", LoginGlobal.getInstance().getLoginPassword());
            	
            	Log.d("TCP", "C : Sending : '" + message + "'");
                PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())), true);
                
                out.println(message);
                Log.d("TCP", "C : Sent.");
                Log.d("TCP", "C : Done.");
                
                socket.setSoTimeout(7000);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String str = in.readLine();
                Log.d("TCP", "str : " + str);

                JSONObject json = new JSONObject(str);
                String response = json.getString("login_change");
                sendBroadcast(LoginGlobal.TCP_LOGIN_CHANGE_CALLBACK, "login_change", response);
            }
            else if(socID.equals("network")) {
            	JSONObject message = new JSONObject();
            	
            	message.put("type", SETUP_NETWORK);
            	message.put("networkType", NetworkGlobal.getInstance().getType());
            	message.put("networkSSID", NetworkGlobal.getInstance().getSsid());
            	message.put("networkPassword", NetworkGlobal.getInstance().getPw());
            	message.put("networkSecurity", NetworkGlobal.getInstance().getSecurity());
            	
            	message.put("networkIpSettings", NetworkGlobal.getInstance().getIpSettings());
            	message.put("networkIpAddress", NetworkGlobal.getInstance().getIp());
            	message.put("networkNetmask", NetworkGlobal.getInstance().getNetmask());
            	message.put("networkGateway", NetworkGlobal.getInstance().getGateway());
            	message.put("networkDNS1", NetworkGlobal.getInstance().getDns1());
            	message.put("networkDNS2", NetworkGlobal.getInstance().getDns2());
            	
            	Log.d("TCP", "C : Sending : '" + message + "'");
            	PrintWriter out = new PrintWriter(new BufferedWriter(
            			new OutputStreamWriter(socket.getOutputStream())), true);

            	out.println(message);
            	Log.d("TCP", "C : Sent.");
            	Log.d("TCP", "C : Done.");
            	
                socket.setSoTimeout(7000);
	            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            String str = in.readLine();
	            Log.d("TCP", "str : " + str);

	            JSONObject json = new JSONObject(str);
	            String response = json.getString("network");
            	sendBroadcast(NetworkGlobal.TCP_SETUP_NETWORK_CALLBACK, "network", response);
            }
            else if(socID.equals("sip")) {	// 2014.07.18 SmartBean_SHCHO :  SEND SIP INFORMATION TO DEVICE
            	JSONObject message = new JSONObject();
            	
            	message.put("type", SETUP_SIP);
            	message.put("name", Global.getInstance().getName());
				message.put("server_impi", Global.getInstance().get_server_Impi());
				message.put("password", Global.getInstance().getPw());
				message.put("adress", Global.getInstance().getAdress());
				message.put("proxyhost", Global.getInstance().getProxyHost());
				message.put("callnumber", Global.getInstance().getCallNumber());
				message.put("confPort", Global.getInstance().getConfPort());
				message.put("gcm_url", Global.getInstance().getGcmUrl());
				message.put("ostype", OSTYPE);
				message.put("registerid", Global.getInstance().getRegisterId());
				
                Log.d("TCP", "C : Sending : '" + message + "'");
                PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())), true);
                
                out.println(message);
                Log.d("TCP", "C : Sent.");
                Log.d("TCP", "C : Done.");
                
                socket.setSoTimeout(7000);
	            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            String str = in.readLine();
	            Log.d("TCP", "str : " + str);

	            JSONObject json = new JSONObject(str);
	            String response = json.getString("sip");
            	sendBroadcast(Global.TCP_SETUP_SIP_CALLBACK, "sip", response);
            	
            } else if (socID.equals("send_device_info")) { //2014.17.10 added for sending device info to event server
//            	JSONObject message = new JSONObject();
//            	message.put("type", SEND_DEVICE_INFO);
//            	message.put("event_server_protocol", Global.getInstance().getEventServerProtocol());
//            	message.put("event_server_port", Global.getInstance().getEventServerPort());
//            	message.put("event_server_url", Global.getInstance().getEventServerUrl());
//            	
//            	Log.d("TCP", "C : Sending : '" + message + "'");
//	            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);    
//	            out.println(message);
//	            
//	            Log.d("TCP", "C : Sent.");
//	            Log.d("TCP", "C : Done.");
//	            
//	            socket.setSoTimeout(7000);
//	            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                String str = in.readLine();
//	            
//                Log.d("TCP", "str : " + str);
//                String response = "failed";
//                
//                try {
//                	JSONObject json = new JSONObject(str);
//                	response = json.getString("response");
//                }
//                catch(JSONException e) {
//                }
//                sendBroadcast("com.smartbean.servertalk.action.TCP_SEND_DEVICE_INFO_CALLBACK", "response", response);
            	
            	JSONObject message = new JSONObject();
            	message.put("type", SEND_DEVICE_INFO);
            	message.put("event_server_protocol", Global.getInstance().getEventServerProtocol());
            	message.put("event_server_port", Global.getInstance().getEventServerPort());
            	message.put("event_server_url", Global.getInstance().getEventServerUrl());
            	
            	Log.d("TCP", "C : Sending : '" + message + "'");
	            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);    
	            out.println(message);
	            
	            Log.d("TCP", "C : Sent.");
	            Log.d("TCP", "C : Done.");
	            
	            socket.setSoTimeout(7000);
	            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String str = in.readLine();
	            
                Log.d("TCP", "str : " + str);
                String response = "failed";
                
                try {
                	JSONObject json = new JSONObject(str);
                	response = json.getString("response");
                }
                catch(JSONException e) {
                }
                sendBroadcast("com.smartbean.servertalk.action.TCP_SEND_DEVICE_INFO_CALLBACK", "response", response);
            }
            else if (socID.equals("event_inquiry")){ //2014.17.10 added for event inquiry
            	JSONObject message = new JSONObject();
            	message.put("type", EVENT_INQUIRY_NOTIFICATION); //type
            	message.put("motion_onoff", Global.getInstance().getMotionclient_onoff()); //motion enable/disable
            	message.put("doorcontrol_onoff", Global.getInstance().get_Doorcontrol_onoff()); //doorcontrol enable/disable
            	message.put("registerid", Global.getInstance().getRegisterId());
            	message.put("client_os_type", Global.getInstance().getClient_os_type());
            	message.put("client_inquiry_status", "inactive");
            	String email = NgnEngine.getInstance().getConfigurationService().getString(NgnConfigurationEntry.IDENTITY_CLIENT_EMAIL, NgnConfigurationEntry.DEFAULT_IDENTITY_CLIENT_EMAIL);
            	message.put("email", email); // save email
            	
            	System.out.println("----------------event_inquiry----------------------");
            	System.out.println("Motion : " + Global.getInstance().getMotionclient_onoff());
            	System.out.println("DoorControl : " + Global.getInstance().get_Doorcontrol_onoff());
            	
            	Log.d("TCP", "C : Sending : '" + message + "'");
	            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);    
	            out.println(message);
	            
	            Log.d("TCP", "C : Sent.");
	            Log.d("TCP", "C : Done.");
	            
	            socket.setSoTimeout(7000);
	            String str = convertInputStreamToString(socket.getInputStream());
	            
                Log.d("TCP", "str : " + str);
                //get the reply from the server
                JSONObject json = new JSONObject(str);
                String event_inquiry_id = "";
                String unique_device_key = "";
                String response = "";
                String status = "";
                if (checkDataOnJson(json, "event_inquiry_notification_registration_result")){
                	response = json.getString("event_inquiry_notification_registration_result");
 	                unique_device_key = json.getString("unique_device_key");
 	                event_inquiry_id = json.getString("event_inquiry_id");
                }else{
                	response = "No Reply from Server";
                }
               
                final Intent it = new Intent("com.smartbean.setupeventinquiry.register.result");
                it.putExtra("result", response);
                it.putExtra("unique_device_key", unique_device_key);
                it.putExtra("status", status);
                it.putExtra("event_inquiry_id", event_inquiry_id);
                //send the broadcast
                NgnEngine.getInstance().getMainActivity().sendBroadcast(it);
            }else if (socID.equals("event_inquiry_email_update")){
            	JSONObject message = new JSONObject();
            	message.put("type", EVENT_INQUIRY_NOTIFICATION_UPDATE_EMAIL); //type
            	message.put("registerid", Global.getInstance().getRegisterId());
            	String email = NgnEngine.getInstance().getConfigurationService().getString(NgnConfigurationEntry.IDENTITY_CLIENT_EMAIL, NgnConfigurationEntry.DEFAULT_IDENTITY_CLIENT_EMAIL);
            	message.put("email", email);
            	
            	System.out.println("Message " + message);
            	
            	PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);    
		        out.println(message);
		        
		        Log.d("TCP", "C : Sent.");
	            Log.d("TCP", "C : Done.");
	            
	            socket.setSoTimeout(7000);
	            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String str = in.readLine();
	            
                Log.d("TCP", "str : " + str);
                

                JSONObject json = new JSONObject(str);
                String response = "";
                if (checkDataOnJson(json, "event_inquiry_update_email")){
                	response = json.getString("event_inquiry_update_email");
                }else{
                	response = "No Reply from Server";
                }
                sendBroadcast("com.smartbean.setupeventinquiry.result", "result", response);
            }else if (socID.equals("event_inquiry_motion_door_update")){
            	JSONObject message = new JSONObject();
            	message.put("type", EVENT_INQUIRY_NOTIFICATION_UPDATE); //type
            	message.put("motion_onoff", Global.getInstance().getMotionclient_onoff()); //motion enable/disable
            	message.put("doorcontrol_onoff", Global.getInstance().get_Doorcontrol_onoff()); //doorcontrol enable/disable
            	message.put("registerid", Global.getInstance().getRegisterId());
            	
            	PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);    
		        out.println(message);
		        
		        Log.d("TCP", "C : Sent.");
	            Log.d("TCP", "C : Done.");
	            
	            socket.setSoTimeout(7000);
	            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String str = in.readLine();
	            
                Log.d("TCP", "str : " + str);

                JSONObject json = new JSONObject(str);
                String response = "";
                if (checkDataOnJson(json, "event_inquiry_motion_door_update")){
                	response = json.getString("event_inquiry_motion_door_update");
                }else{
                	response = "No Reply from Server";
                }
                sendBroadcast("com.smartbean.setupeventinquiry.result", "result", response);
            }else if (socID.equals("event_sensors_motion_update")){ //2014.17.10 added for event sensor
            	JSONObject message = new JSONObject();
            	message.put("type", EVENT_SENSOR);
            	
            	message.put("motion_sensitivity", Global.getInstance().getMotionSensitivity()); //motion sensitivity
            	message.put("motion_onoff", Global.getInstance().get_Motion_ONOFF());
            	
            	Log.d("TCP", "C : Sending : '" + message + "'");
	            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);    
	            out.println(message);
	            
	            Log.d("TCP", "C : Sent.");
	            Log.d("TCP", "C : Done.");
	            
	            socket.setSoTimeout(7000);
	            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String str = in.readLine();
	            
                Log.d("TCP", "str : " + str);

                JSONObject json = new JSONObject(str);
                String response = "";
                if (checkDataOnJson(json, "event_sensors_motion_update")){
                	response = json.getString("event_sensors_motion_update");
                }else{
                	response = "No Reply from Server";
                }
                sendBroadcast("com.smartbean.event_sensors_motion_update.result", "result", response);
            	
            
            	
            }else if (socID.equals("event_inquiry_client_list_update")){
            	JSONObject message = new JSONObject();
            	message.put("type", EVENT_INQUIRY_CLIENT_LIST_UPDATE);
            	message.put("client_inquiry_status", Global.getInstance().getClient_inquiry_status());
            	message.put("client_inquiry_gcmID", Global.getInstance().getClient_inquiry_gcmID());
           
            	Log.d("TCP", "C : Sending : '" + message + "'");
	            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);    
	            out.println(message);
	            
	            Log.d("TCP", "C : Sent.");
	            Log.d("TCP", "C : Done.");
	            
	            socket.setSoTimeout(7000);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String str = in.readLine();
                Log.d("TCP", "str : " + str);

                JSONObject json = new JSONObject(str);
                String response = "";
                if (checkDataOnJson(json, "event_inquiry_result")){
                	response = json.getString("event_inquiry_result");
                }else{
                	response = "No Reply from Server";
                }
                sendBroadcast("com.smartbean.setupeventinquiry.result", "result", response);
            }else if (socID.equals("event_inquiry_get_client_data")){
            	
            	JSONObject message = new JSONObject();
            	message.put("type", EVENT_INQUIRY_GET_CLIENT_DATA);
            	message.put("client_inquiry_gcmID", Global.getInstance().getRegisterId());
            	
            	Log.d("TCP", "C : Sending : '" + message + "'");
	            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);    
	            out.println(message);
	            
	            Log.d("TCP", "C : Sent.");
	            Log.d("TCP", "C : Done.");
	            
	            socket.setSoTimeout(7000);
	            String str = convertInputStreamToString(socket.getInputStream());
	            
	            System.out.println("------------------------------------");
	            System.out.println("Received : " + str);
	            System.out.println("------------------------------------");
	  
	            if (str.equals("")){
	            	Global.getInstance().setClient_inquiry_status("Initial");
	            	sendBroadcast("com.smartbean.setupeventinquiry.sync.end", "result", "initial");
	            }else{
			        Log.d("TCP", "str : " + str);
	          		JSONObject jsonResult = new JSONObject(str);
	          		String unique_key_device = "";
            		if (checkDataOnJson(jsonResult, "unique_device_key")){
            			unique_key_device = jsonResult.getString("unique_device_key");
            		}
            		Global.getInstance().setServer_unique_device_id(unique_key_device);
            		String result = null;
            		if (checkDataOnJson(jsonResult, "Data")){
            			 Global.getInstance().setClient_inquiry_status("Initial");
	  			         Global.getInstance().setMotionclient_onoff("enabled");
	  			         Global.getInstance().set_Doorcontrol_onoff("enabled");
	  			         result = "failed";
            		}else{
            			String client_email = jsonResult.getString("client_email");
  			            String client_status = jsonResult.getString("client_status");
  			            String client_motion_status = jsonResult.getString("client_motion_detect");
  			            String client_relay_status = jsonResult.getString("client_relay_detect");
  			            
  			            Global.getInstance().setClient_inquiry_status(client_status);
  			            Global.getInstance().setMotionclient_onoff(client_motion_status);
  			            Global.getInstance().set_Doorcontrol_onoff(client_relay_status);
  			            Global.getInstance().setServer_unique_device_id(unique_key_device); //add for if the user already send a registration on the server device
  			            
  			            NgnEngine.getInstance().getConfigurationService().putString(NgnConfigurationEntry.IDENTITY_CLIENT_EMAIL, client_email);
  			            NgnEngine.getInstance().getConfigurationService().commit();
  			            result = "success";
            		}
 			        sendBroadcast("com.smartbean.setupeventinquiry.sync.end", "result", result);
	            }
            }else if (socID.equals("event_inquiry_delete_event_inquiry")){
            	JSONObject message = new JSONObject();
            	message.put("type", EVENT_INQUIRY_DELETE_CLIENT_DATA);
            	message.put("client_inquiry_gcmID", Global.getInstance().getRegisterId());
            	
            	Log.d("TCP", "C : Sending : '" + message + "'");
	            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);    
	            out.println(message);
	            
	            Log.d("TCP", "C : Sent.");
	            Log.d("TCP", "C : Done.");
	            
	            socket.setSoTimeout(7000);
	            String str = convertInputStreamToString(socket.getInputStream());
                
                JSONObject json = new JSONObject(str);
                String response = "";
                if (checkDataOnJson(json, "event_inquiry_result")){
                	response = json.getString("event_inquiry_result");
                }else{
                	response = "No Reply from Server";
                }

                Intent it = new Intent("com.smartbean.setupeventinquiry.delete.result");
                it.putExtra("result", response);
                NgnEngine.getInstance().getMainActivity().sendBroadcast(it);
            }else if (socID.equals("event_inquiry_delete_event_inquiry_list")){
            	JSONObject message = new JSONObject();
            	message.put("type", EVENT_INQUIRY_DELETE_CLIENT_DATA);
            	message.put("client_inquiry_status", Global.getInstance().getClient_inquiry_status());
            	message.put("client_inquiry_gcmID", Global.getInstance().getClient_inquiry_gcmID());
            	
            	Log.d("TCP", "C : Sending : '" + message + "'");
	            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);    
	            out.println(message);
	            
	            Log.d("TCP", "C : Sent.");
	            Log.d("TCP", "C : Done.");
	            
	            socket.setSoTimeout(7000);
	            String str = convertInputStreamToString(socket.getInputStream());
                
                JSONObject json = new JSONObject(str);
                String response = "";
                String gcmID = "";
                if (checkDataOnJson(json, "event_inquiry_result")){
                	response = json.getString("event_inquiry_result");
                	if (checkDataOnJson(json, "gcmID")){
                		gcmID = json.getString("gcmID");
                	}
                }else{
                	response = "No Reply from Server";
                }
                Intent it = new Intent("com.smartbean.setupeventinquiry.result");
                it.putExtra("gcmID", gcmID);
                it.putExtra("result", response);
                NgnEngine.getInstance().getMainActivity().sendBroadcast(it);
            }else if (socID.equals("event_inquiry_submit_list_changes")){
//              		JSONObject message = new JSONObject();
//              		if (Global.getInstance().getEventInquiryObservableList() != null){
//            			JSONArray jsonArray = new JSONArray();
//            			for (EventInquiryModel model : Global.getInstance().getEventInquiryObservableList().getList()) {
//            				if (model.isUpdated()){
//            					String gcmID = model.getGcmID();
//    	            			String email =  model.getEmail();
//    	            			String motionDetect = model.getMotion_detect_enable();
//    	            			String doorControl = model.getRelay_sensors_enable();
//    	            			String client_os_type = model.getClient_os_type();
//    	            			String status = model.getActiveStatus();
//    	            			String created_date = model.getCreated_date();
//
//    	            			JSONObject messageObj = new JSONObject();
//                				messageObj.put("client_GCMID", gcmID);
//                				messageObj.put("client_email", email);
//                				messageObj.put("activeStatus", status);
//                				messageObj.put("motionDetect_enable_YN", motionDetect);
//                				messageObj.put("relaySensors_enable_YN", doorControl);
//                				messageObj.put("client_os_type", client_os_type);
//                				messageObj.put("created_date", created_date);
//                				jsonArray.put(messageObj);
//            				}
//						}
//	            		message.put("Event_Inquiry", jsonArray);
//	            		
//	            		Log.d("TCP", "C : Sending : '" + message + "'");
//			            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);    
//			            out.println(message);
//			            
//			            Log.d("TCP", "C : Sent.");
//			            Log.d("TCP", "C : Done.");
//			            
//			            socket.setSoTimeout(7000);
//		                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//		                String str = in.readLine();
//		                Log.d("TCP", " event_inquiry_list_update str : " + str);
//
//		                JSONObject json = new JSONObject(str);
//		                String response = json.getString("event_inquiry_list_update");
//		                if (checkDataOnJson(json, "event_inquiry_list_update")){
//		                	response = json.getString("event_inquiry_list_update");
//		                }else{
//		                	response = "No Reply from Server";
//		                }
//		                sendBroadcast("com.smartbean.setupeventinquiry.result", "result", response);
//              		}
            	
            	DbHandler mDbhandler = DbHandler.open(NgnEngine.getInstance().getMainActivity());
            	Cursor cursor = mDbhandler.get_all_event_inquiry();
            	if (cursor != null){
                   	JSONObject message = new JSONObject();
            		if (cursor.moveToFirst()){
            			JSONArray jsonArray = new JSONArray();
	            		do{
	            			String gcmID = cursor.getString(cursor.getColumnIndex(EventInquiry.FIELD_CLIENTGCMID));
	            			String email =  cursor.getString(cursor.getColumnIndex(EventInquiry.FIELD_CLIENTEMAIL));
	            			String motionDetect = cursor.getString(cursor.getColumnIndex(EventInquiry.FIELD_MOTIONDETECT_STATUS));
	            			String doorControl = cursor.getString(cursor.getColumnIndex(EventInquiry.FIELD_RELAYSENSOR_STATUS));
	            			String client_os_type = cursor.getString(cursor.getColumnIndex(EventInquiry.FIELD_CLIENT_OS_TYPE));
	            			String status = cursor.getString(cursor.getColumnIndex(EventInquiry.FIELD_STATUS));
	            			String created_date = cursor.getString(cursor.getColumnIndex(EventInquiry.FIELD_CREATEDDATE));

	            			JSONObject messageObj = new JSONObject();
            				messageObj.put("client_GCMID", gcmID);
            				messageObj.put("client_email", email);
            				messageObj.put("activeStatus", status);
            				messageObj.put("motionDetect_enable_YN", motionDetect);
            				messageObj.put("relaySensors_enable_YN", doorControl);
            				messageObj.put("client_os_type", client_os_type);
            				messageObj.put("created_date", created_date);
            				jsonArray.put(messageObj);
	            		}while(cursor.moveToNext());
	            		message.put("Event_Inquiry", jsonArray);
	            		
	            		Log.d("TCP", "C : Sending : '" + message + "'");
			            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);    
			            out.println(message);
			            
			            Log.d("TCP", "C : Sent.");
			            Log.d("TCP", "C : Done.");
			            
			            socket.setSoTimeout(7000);
		                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		                String str = in.readLine();
		                Log.d("TCP", "str : " + str);

		                JSONObject json = new JSONObject(str);
		                String response = json.getString("event_inquiry_list_update");
		                if (checkDataOnJson(json, "event_inquiry_list_update")){
		                	response = json.getString("event_inquiry_list_update");
		                }else{
		                	response = "No Reply from Server";
		                }
		                sendBroadcast("com.smartbean.setupeventinquiry.result", "result", response);
            		}
            	}
            }else if (socID.equals("event_sensors_doorcontrol_update")){
            	
            	JSONObject message = new JSONObject();
            	
            	message.put("type", EVENT_SENSOR_DOORCONTROL_UPDATE);
            	message.put("doorcontrol_onoff", Global.getInstance().get_Doorcontrol_onoff());
            	message.put("Door_lock_status", Global.getInstance().get_Door_lock_status());
            	message.put("Auto_lock_time", Global.getInstance().get_Auto_lock_time());
            	message.put("Auto_lock_onoff", Global.getInstance().get_Auto_lock_onoff());
            	message.put("nickname", Global.getInstance().getRelay_sensor_nickname());
            	message.put("signal1_transaction_value", Global.getInstance().getSignal1_transaction_value());
            	message.put("signal1_printed_name", Global.getInstance().getSignal1_printed_name());
            	message.put("signal1_duration_value", Global.getInstance().getSignal1_duration_value());
            	message.put("signal1_transaction_noti_YN", Global.getInstance().getSignal1_transaction_notification_YN());
            	message.put("signal1_duration_noti_YN", Global.getInstance().getSignal1_duration_notification_yn());
            	message.put("signal2_transaction_value", Global.getInstance().getSignal2_transaction_value());
            	message.put("signal2_printed_name", Global.getInstance().getSignal2_printed_name());
            	message.put("signal2_duration_value", Global.getInstance().getSignal2_duration_value());
            	message.put("signal2_transaction_noti_YN", Global.getInstance().getSignal2_transaction_notification_YN());
            	message.put("signal2_duration_noti_YN", Global.getInstance().getSignal2_duration_notification_yn());
//            	message.put("ir_control", Global.getInstance().getIrControl());
            	
            	Log.d("TCP", "C : Sending : '" + message + "'");
	            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);    
	            out.println(message);
	            
	            Log.d("TCP", "C : Sent.");
	            Log.d("TCP", "C : Done.");
	            
	            socket.setSoTimeout(7000);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String str = in.readLine();
                Log.d("TCP", "str : " + str);
                
                JSONObject json = new JSONObject(str);
                String response = "";
                if (checkDataOnJson(json, "event_sensors_doorcontrol_update_result")){
                	response = json.getString("event_sensors_doorcontrol_update_result");
                }else{
                	response = "No Reply from Server";
                }
                sendBroadcast("com.smartbean.doorcontrol.update.result", "result", response);
            }else if (socID.equals("notification_channel_accounts")){
            	
            	JSONObject message = new JSONObject();
            	JSONArray jsonArray = new JSONArray();
            	for (NotificationEmailFacebookData mdata : Global.getInstance().getNotificationChannelAccounts()) {
            		JSONObject messageObj = new JSONObject();
                	messageObj.put("NotificationChannelAccounts", mdata.getmNotificationChannelAccounts());
                	messageObj.put("channel_code", mdata.getmChannelCode());
                	messageObj.put("account", mdata.getmEmail());
                	messageObj.put("motionDetect_enable_YN", mdata.getmMotion_ONOFF());
                	messageObj.put("sensors_enable_YN", mdata.getmDoorControl_ONOFF());
                	messageObj.put("status", mdata.isDelete());
                	jsonArray.put(messageObj);
				}
            	message.put("Notification_Accounts", jsonArray);
            	
            	Log.d("TCP", "C : Sending : '" + message + "'");
	            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);    
	            out.println(message);
	            
	            Log.d("TCP", "C : Sent.");
	            Log.d("TCP", "C : Done.");
	            
	            socket.setSoTimeout(7000);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String str = in.readLine();
                Log.d("TCP", "str : " + str);
                
                JSONObject json = new JSONObject(str);
                String response = "";
                if (checkDataOnJson(json, "notification_accounts_result")){
                	response = json.getString("notification_accounts_result");
                }else{
                	response = "No Reply from Server";
                }
                sendBroadcast("com.smartbean.notification.accounts.result", "result", response);
            }
        } catch (JSONException e) {
            Log.e("TCP", "C : JSONException Error", e);
            error_flag = true;
        } catch (IOException e) { // catch ConnectException, SocketTimeoutException & UnknownHostException inside IOException
        	Log.e("TCP", "C : IOException Error", e);
        	error_flag = true;
        } finally {
        	try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e("TCP", "C : Socket Close Error", e);
			}
            socket = null;
            if(error_flag!=false) {
            	error_flag = false;
            	if(socID.equals("connect")) {
            		sendBroadcast(Global.TCP_CONNECT_CALLBACK, "connect", "failed");
            	}
            	else if(socID.equals("login")) {
            		sendBroadcast(LoginGlobal.TCP_LOGIN_CALLBACK, "login", "connection_lost");
            	}
            	else if(socID.equals("login_change")) {
            		sendBroadcast(LoginGlobal.TCP_LOGIN_CHANGE_CALLBACK, "login_change", "connection_lost");
            	}
            	else if(socID.equals("doortalk")) {
            		sendBroadcast(Global.TCP_GET_DOORTALK_DATA_CALLBACK, "doortalk", "connection_lost");
            	}
            	else if(socID.equals("network")) {
            		sendBroadcast(NetworkGlobal.TCP_SETUP_NETWORK_CALLBACK, "network", "connection_lost");
            	}
            	else if(socID.equals("sip")) {
            		sendBroadcast(Global.TCP_SETUP_SIP_CALLBACK, "sip", "connection_lost");
            	}
            	else if(socID.equals("send_device_info")) {
            		sendBroadcast("com.smartbean.servertalk.action.TCP_SEND_DEVICE_INFO_CALLBACK", "response", "connection_lost");
            	}
            	else if(socID.equals("inbound_blocklist")) {
            		 sendBroadcast("com.smartbean.servertalk.action.SENDING_INBOUND_BLOCKLIST_CALLBACK", "response", "connection_lost");
            	}
            	else if(socID.equals("outbound_prioritylist")) {
            		 sendBroadcast("com.smartbean.servertalk.action.SENDING_OUTBOUND_PRIORITYLIST_CALLBACK", "response", "connection_lost");
            	}
            	else if (socID.equals("event_sensors_doorcontrol_update") || socID.equals("event_sensors_motion_update")){
            		sendBroadcast("com.smartbean.servertalk.action.EVENT_SENSORS_ERROR", "error", "Connection Lost");
            	}
            	else if (socID.equals("event_inquiry_submit_list_changes") || socID.equals("event_inquiry_delete_event_inquiry_list")
            			|| socID.equals("event_inquiry_get_client_data") || socID.equals("event_inquiry_delete_event_inquiry") 
            			|| socID.equals("event_inquiry_client_list_update") || socID.equals("event_inquiry_motion_door_update") 
            			|| socID.equals("event_inquiry_email_update") || socID.equals("event_inquiry")){
            		sendBroadcast("com.smartbean.servertalk.action.EVENT_INQUIRY_ERROR", "error", "Connection Lost");
            	}
            	else if (socID.equals("notification_channel_accounts")){
            		sendBroadcast("com.smartbean.servertalk.action.NOTIFICATION_CHANNEL_ACCOUNTS", "error", "Connection Lost");
            	}
            	else if (socID.equals("VOLUME_CONTROL")) {
            		sendBroadcast("com.smartbean.servertalk.action.SENDING_VOLUME", "response", "connection_lost");
            	}
            }
        	Log.d("TCP", "C : Thread End here.");
        }
    }
	
	private void sendBroadcast(String action, String extraName, String extraValue) {
    	final Intent intent = new Intent(action);
		intent.putExtra(extraName, extraValue);
//		m_context.sendBroadcast(intent);
		NgnEngine.getInstance().getMainActivity().sendBroadcast(intent);
	}
	
	private String convertInputStreamToString(InputStream inputStream)
			throws IOException {
		System.out.println("convertInputStreamToString()");
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while ((line = bufferedReader.readLine()) != null)
			result += line;

		inputStream.close();
		return result;
	}
	
	//--------------ADDED TO AVOID JSONEXCEPTION IF DATA IS NOT EXISTED-----------------------
	private boolean checkDataOnJson(JSONObject json, String key){
		try {
			json.getString(key);
		} catch (JSONException e) {
			return false;
		}
		return true;
	}
}
