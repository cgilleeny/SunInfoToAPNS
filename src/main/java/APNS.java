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
import java.util.logging.Logger;

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
	    	//stmt = con.createStatement();
	    	SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	dateFormatUTC.setTimeZone(TimeZone.getTimeZone("GMT"));
	    	String UTCDate = dateFormatUTC.format(new Date());	   
	    	
	    	cStmt = con.prepareCall("{ CALL getDeviceNeedingPush(?) }");
	    	cStmt.setString(1, UTCDate);
	   	    cStmt.execute();
	   	    rs = cStmt.getResultSet();

	        ArrayList<Device> devicesList = new ArrayList<Device>();
	        while (rs.next()) {
	        	//System.out.println("rs.pid: " + rs.getInt("pid"));
	        	Device device  = new Device(rs);
	        	//System.out.println("token: " + device.token + " device.sunset.toString(): " + device.sunset.toString());
			    devicesList.add(device); 
	        }

	        //System.out.println("Testing - sendPushNotifications: " + devicesList.size());
	        
	        if (devicesList.size() > 0) {
		        final ApnsClient apnsClient = new ApnsClientBuilder()
		        .setClientCredentials(new File("/home/ec2-user/DevelopmentPushCertificate.p12"), "ABCD3fgh!")
		        .build();
				
				final Future<Void> connectFuture = apnsClient.connect(ApnsClient.DEVELOPMENT_APNS_HOST);
				connectFuture.await();
		        
				//Time now = new Time(0);
				Date now = new Date();
		        for (Device device : devicesList) {
		        	System.out.println("timestamp: " + now.getTime() + " pid: " + device.pid);
					sendPushNotification(device, apnsClient);
					System.out.println("pid after: " + device.pid);
				}
		        
		        final Future<Void> disconnectFuture = apnsClient.disconnect();
		        disconnectFuture.await();
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
	
	private void sendPushNotification(Device device, ApnsClient apnsClient) throws Exception {


	    final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
	    //payloadBuilder.setAlertBody(device.sunset.toString());
	    payloadBuilder.addCustomProperty("daylength", device.daylength);
	    payloadBuilder.addCustomProperty("sunset", device.sunset.toString());
	    payloadBuilder.setContentAvailable(true);
	    

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
}
