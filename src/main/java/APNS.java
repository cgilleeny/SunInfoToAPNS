import io.netty.util.concurrent.Future;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
//import java.util.logging.Logger;
//import java.lang.Math;
import com.google.gson.JsonSyntaxException;
import com.relayrides.pushy.apns.ApnsClient;
import com.relayrides.pushy.apns.ApnsClientBuilder;
import com.relayrides.pushy.apns.ClientNotConnectedException;
import com.relayrides.pushy.apns.PushNotificationResponse;
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;


public class APNS {
	private static Connection con = null;
	
	public APNS() {
		// TODO Auto-generated constructor stub
	}

	public boolean isActive() throws ClassNotFoundException, SQLException, Exception {
		ResultSet rs = null;

		try {
			Class.forName("com.mysql.jdbc.Driver");
	    	
			
			con = DriverManager.getConnection("jdbc:mysql://10.0.2.195:3306/chickensaver", "tutorial_user", "ABCD3fgh!");
	   	    CallableStatement cStmt = null;

	   	    cStmt = con.prepareCall("{ CALL getStatus() }");
	   	    cStmt.execute();
	   	    rs = cStmt.getResultSet();

	   	    if(rs.next()) {
	   	    	String status = rs.getString("status");
	   	    	//System.out.println("status: " + status);
	   	    	if (status.equals("active")){
	   	    		//System.out.println("status == active");
	   	    		return(true);
	   	    	}
	   	    } 
	   	    System.out.println("end is near");
	   	    return(false);
		} finally {
			 if (rs != null) {
				 rs.close();
			 }
	         if (con != null) {
	        	 con.close();
	         }
		}
	}
	
	public String getLastRunTimeStamp() throws ClassNotFoundException, SQLException, Exception {
		ResultSet rs = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
	    	
			
			con = DriverManager.getConnection("jdbc:mysql://10.0.2.195:3306/chickensaver", "tutorial_user", "ABCD3fgh!");
	   	    CallableStatement cStmt = null;

	   	    cStmt = con.prepareCall("{ CALL getLastRunTimeStamp() }");
	   	    cStmt.execute();
	   	    rs = cStmt.getResultSet();

	   	    if(rs.next()) {
	   	    	return(rs.getString("lastRun"));
	   	    } else {
	   	    	System.out.println("lastRun: Failure");
	   	    	return("Failure");
	   	    }
		} finally {
			 if (rs != null) {
				 rs.close();
			 }
	         if (con != null) {
	        	 con.close();
	         }
		}
	}


	
	
	public void sendPushNotifications() throws ClassNotFoundException, SQLException, JsonSyntaxException, Exception {
		CallableStatement cStmt = null;
		ResultSet rs = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
	    	
	    	con = DriverManager.getConnection("jdbc:mysql://10.0.2.195:3306/chickensaver", "tutorial_user", "ABCD3fgh!");

	    	SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	dateFormatUTC.setTimeZone(TimeZone.getTimeZone("GMT"));
	    	String UTCDate = dateFormatUTC.format(new Date());	   
	    	System.out.println("UTCDate: " + UTCDate);
	    	cStmt = con.prepareCall("{ CALL getAlarmToPush(?) }");
	    	cStmt.setString(1, UTCDate);
	   	    cStmt.execute();
	   	    rs = cStmt.getResultSet();
	    	
	   	    ArrayList<Alarm> alarms = new ArrayList<Alarm>();
	   	    while (rs.next()) {
	        	Alarm alarm  = new Alarm(rs);
			    alarms.add(alarm); 
			    System.out.println("token: " + alarm.token + " alarm.sunset.toString(): " + alarm.sunset.toString());
	        }
	   	    
	        if (alarms.size() > 0) {
	        	
		        /*
		        final ApnsClient apnsClient = new ApnsClientBuilder()
		        .setClientCredentials(new File("/home/ec2-user/ProductionPushCertificate.p12"), "ABCD3fgh!")
		        .build();
		        final Future<Void> connectFuture = apnsClient.connect(ApnsClient.PRODUCTION_APNS_HOST);
		         */
	        	
	        	/*
		        final ApnsClient apnsClient = new ApnsClientBuilder()
		        .setClientCredentials(new File("/home/ec2-user/DevelopmentPushCertificate.p12"), "ABCD3fgh!")
		        .build();
				final Future<Void> connectFuture = apnsClient.connect(ApnsClient.DEVELOPMENT_APNS_HOST);
				connectFuture.await();
		        */
				//Date now = new Date();
		        for (Alarm alarm : alarms) {
		        	System.out.println("timestamp: " + new Date().getTime() + " pid: " + alarm.pid);
		        	final ApnsClient apnsClient;
		        	final Future<Void> connectFuture;
		        	if(alarm.development.equals("sandbox")) {
		        		apnsClient = new ApnsClientBuilder().setClientCredentials(new File("/home/ec2-user/DevelopmentPushCertificate.p12"), "ABCD3fgh!")
				        .build();
						connectFuture = apnsClient.connect(ApnsClient.DEVELOPMENT_APNS_HOST);
		        	} else {
				        apnsClient = new ApnsClientBuilder().setClientCredentials(new File("/home/ec2-user/ProductionPushCertificate.p12"), "ABCD3fgh!")
				        .build();
				        connectFuture = apnsClient.connect(ApnsClient.PRODUCTION_APNS_HOST);
		        	}
		        	connectFuture.await();
		        	
					sendAlarmPushNotification(alarm, apnsClient);
					System.out.println("pid after: " + alarm.pid);
					cStmt.close();
					cStmt = con.prepareCall("{ CALL updateAlarmPushCounter(?) }");
			    	cStmt.setInt(1, alarm.id);
			   	    cStmt.execute();
			   	    
			   	    final Future<Void> disconnectFuture = apnsClient.disconnect();
			        disconnectFuture.await();
				}
	        }
		} finally {
			 if (rs != null) {
				 rs.close();
			 }
	   		 if (cStmt != null ) {
	   			cStmt.close();
			 }
	         if (con != null) {
	        	 con.close();
	         }
		}
	}
	
	
	private void sendAlarmPushNotification(Alarm alarm, ApnsClient apnsClient) throws ClassNotFoundException, SQLException, Exception {
	    final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
	    payloadBuilder.setAlertTitle("Sunset Alert");
	    
	    if(alarm.offset == 0) {
	    	payloadBuilder.setAlertSubtitle("The sun is setting.");
	    } else if(alarm.offset > 0) {
	    	payloadBuilder.setAlertSubtitle("The sun set " + alarm.offset + " minutes ago.");
	    } else {
	    	payloadBuilder.setAlertSubtitle("The sun will set in " + java.lang.Math.abs(alarm.offset) + " minutes.");
	    }
	    payloadBuilder.setAlertBody("Do you know where your chickens are?");
	    payloadBuilder.setSoundFileName(alarm.sound);
	    payloadBuilder.setCategoryName("snooze.category");
	    final String payload = payloadBuilder.buildWithDefaultMaximumLength();
	    System.out.println("payload: " + payload);
	    
	    final SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(alarm.token, "com.carolinegilleeny.ChickenSaverPlus", payload);
	    
	    final Future<PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture =
	            apnsClient.sendNotification(pushNotification);
	    
	    try {
	        final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse =
	                sendNotificationFuture.get();

	        if (pushNotificationResponse.isAccepted()) {
	            System.out.println("Push notification accepted by APNs gateway.");
	        } else {
	            System.out.println("Notification rejected by the APNs gateway: " +
	                    pushNotificationResponse.getRejectionReason());

	            if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
	                System.out.println("\t…and the token is invalid as of " +
	                    pushNotificationResponse.getTokenInvalidationTimestamp());
	                setDeviceStatus("invalidated", alarm.pid);
	            } else if(pushNotificationResponse.getRejectionReason().equalsIgnoreCase("Unregistered")) {
	            	setDeviceStatus("unregistered", alarm.pid);
	            } else if(pushNotificationResponse.getRejectionReason().equalsIgnoreCase("BadDeviceToken")) {
	            	setDeviceStatus("BadDeviceToken", alarm.pid);
	            } else {
	            	System.out.println("Unknown pushNotificationResponse rejection reason");
	            }
	        }

	    } catch (final ExecutionException e) {
	        System.err.println("Failed to send push notification.");
	        e.printStackTrace();

	        if (e.getCause() instanceof ClientNotConnectedException) {
	            System.out.println("Waiting for client to reconnect…");
	            apnsClient.getReconnectionFuture().await();
	            System.out.println("Reconnected.");
	        }
	    } 
	}
	
	/*
	private void sendPushNotification(Device device, ApnsClient apnsClient) throws Exception {


	    final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
	    //payloadBuilder.setMutableContent(true);
	    payloadBuilder.setContentAvailable(true);
	    //payloadBuilder.setAlertBody(device.sunset.toString());
	    payloadBuilder.addCustomProperty("daylength", device.daylength);
	    payloadBuilder.addCustomProperty("sunset", device.sunset.toString());
	    //payloadBuilder.setContentAvailable(true);
	    

	    final String payload = payloadBuilder.buildWithDefaultMaximumLength();
	    System.out.println("payload: " + payload);
	    
	    final SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(device.token, "com.carolinegilleeny.ChickenSaverPlus", payload);
	    
	    final Future<PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture =
	            apnsClient.sendNotification(pushNotification);
	    
	    try {
	        final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse =
	                sendNotificationFuture.get();

	        if (pushNotificationResponse.isAccepted()) {
	            System.out.println("Push notification accepted by APNs gateway.");
	        } else {
	            System.out.println("Notification rejected by the APNs gateway: " +
	                    pushNotificationResponse.getRejectionReason());

	            if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
	                System.out.println("\t…and the token is invalid as of " +
	                    pushNotificationResponse.getTokenInvalidationTimestamp());
	                setStatus("invalidated", device);
	            }
	            
	            if(pushNotificationResponse.getRejectionReason().equalsIgnoreCase("Unregistered")) {
	            	setStatus("unregistered", device);
	            }
	        }

	    } catch (final ExecutionException e) {
	        System.err.println("Failed to send push notification.");
	        e.printStackTrace();

	        if (e.getCause() instanceof ClientNotConnectedException) {
	            System.out.println("Waiting for client to reconnect…");
	            apnsClient.getReconnectionFuture().await();
	            System.out.println("Reconnected.");
	        }
	    }
	    
	}
	*/

	private void setDeviceStatus(String status, int pid) throws ClassNotFoundException, SQLException, Exception {
		Statement stmt = null;
		//ResultSet rs = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
	    	
	    	con = DriverManager.getConnection("jdbc:mysql://10.0.2.195:3306/chickensaver", "tutorial_user", "ABCD3fgh!");
	    	stmt = con.createStatement();
	    	System.out.println("Setting status for device" + pid);
	        String sql = "Update chickensaver.apns_devices set status = '" + status + "' where pid = " + pid;
	
	        System.out.println(sql);
	        stmt.executeUpdate(sql);
	        
	        //rs = stmt.executeQuery(sql);
		}finally {
			 //if (rs != null) {
			//	 rs.close();
			 //}
	   		 if (stmt != null ) {
				 stmt.close();
			 }
	         if (con != null) {
	        	 con.close();
	         }
		} 
	}
	
	/*
	private void setStatus(String status, Device device) throws ClassNotFoundException, SQLException, Exception {
		Statement stmt = null;
		//ResultSet rs = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
	    	
	    	con = DriverManager.getConnection("jdbc:mysql://10.0.2.195:3306/chickensaver", "tutorial_user", "ABCD3fgh!");
	    	stmt = con.createStatement();
	    	System.out.println("Setting status for device" + device.pid);
	        String sql = "Update chickensaver.apns_devices set status = '" + status + "' where pid = " + device.pid;
	
	        System.out.println(sql);
	        stmt.executeUpdate(sql);
	        
	        //rs = stmt.executeQuery(sql);
		}finally {
			 //if (rs != null) {
			//	 rs.close();
			 //}
	   		 if (stmt != null ) {
				 stmt.close();
			 }
	         if (con != null) {
	        	 con.close();
	         }
		} 
	}
	*/
}
