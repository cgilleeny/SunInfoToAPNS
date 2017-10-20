import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;


public class Sunset {
	private static Connection con = null;
	
	public void updateTimes(String timeStampString) throws ClassNotFoundException, SQLException, JsonSyntaxException, Exception {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
	    	
	    	con = DriverManager.getConnection("jdbc:mysql://10.0.2.195:3306/chickensaver", "tutorial_user", "ABCD3fgh!");
	    	stmt = con.createStatement();
	    	
	    	SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	dateFormatUTC.setTimeZone(TimeZone.getTimeZone("GMT"));
	    	String UTCDate = dateFormatUTC.format(new Date());
	    	

	    	
	    	
	        String sql = "Select * from chickensaver.apns_devices where status = 'active' and (sunset < (STR_TO_DATE('" + dateFormatUTC.format(new Date()) + "','%Y-%m-%d %H:%i:%s') - Interval 1 hour) or modified >= '" + timeStampString + "') and latitude <> 0 and longitude <> 0";
	
	       
	        
	        
	        rs = stmt.executeQuery(sql);
	    	ArrayList<Device> deviceList = new ArrayList<Device>();
	    	while(rs.next()) {
	    		Device device = new Device(rs);
	    		deviceList.add(device);
	    	}

	    	if (deviceList.size() > 0) {
	    		 System.out.println("UTC Now: " + UTCDate + ", last runtimestamp: " + timeStampString);
	    		 System.out.println(sql);
	    		 System.out.println("Sunset Devices List Count: " + deviceList.size());
	    	}
	        
	        Date now = new Date();
	        for(Device device : deviceList) {
	        	/*
	        	System.out.println("1 now: " + UTCDate + ", device.sunset: " + device.sunset);
	        	if (device.sunset.before(now)) {
	        		System.out.println("2 now: " + UTCDate + ", device.sunset: " + device.sunset);
	                Calendar today = Calendar.getInstance();
	                today.setTime(now);
	                today.add(Calendar.DAY_OF_YEAR, 1);
	                now = today.getTime();
	        	}
	        	*/
	        	updateTime(device, now, stmt);
	        }
		} finally {
			 if (rs != null) {
				 rs.close();
			 }
	   		 if (stmt != null ) {
				 stmt.close();
			 }
	         if (con != null) {
	        	 con.close();
	         }
		}
	}
	
	
	
	private void updateTime(Device device, Date today, Statement stmt) throws Exception {
		Date date = today;
		String sunsetString;
		int dayLength;
		Date sunset;
		Calendar calendar = Calendar.getInstance();
		do {
			String sunData = sunDataForDate(device, date);
			sunsetString = extractSunsetString(sunData);
			dayLength = extractDayLength(sunData);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sunset = dateFormat.parse(sunsetString);
			calendar.setTime(date);
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			date = calendar.getTime();
		} while(sunset.before(today));

		String sql = "Update chickensaver.apns_devices set sunset = '" + sunsetString + "', daylength = " 
    			+ dayLength + ", push = 'push' where pid = " + device.pid;
    	System.out.println("sql: " + sql);
    	stmt.executeUpdate(sql);
		
		/*
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String url = "http://api.sunrise-sunset.org/json?lat=" + device.latitude + "&lng=" + device.longitude + "&date=" + dateFormat.format(today) + "&formatted=0";
		
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		//con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		System.out.println(response.toString());
		

		
		JsonElement je = new Gson().fromJson(response.toString(), JsonElement.class);
		je = je.getAsJsonObject().get("results");
    	String sunsetStr = je.getAsJsonObject().get("sunset").getAsString();
		System.out.println("sunsetStr: " + sunsetStr);
    	String[] dateTimeParts = sunsetStr.split("T", 2);
    	System.out.println("dateTimeParts[0]: " + dateTimeParts[0] + "dateTimeParts[1]: " + dateTimeParts[1]);

    	int end = dateTimeParts[1].length() - 6;
    	System.out.println("dateTimeParts[0]: " + dateTimeParts[0] + ", dateTimeParts[1].substring(0, end): " + dateTimeParts[1].substring(0, end));
    	int dayLength = je.getAsJsonObject().get("day_length").getAsInt();
    	
    	
    	String sql = "Update chickensaver.apns_devices set sunset = '" + dateTimeParts[0] + " " 
    			+ dateTimeParts[1].substring(0, end) + "', daylength = " 
    			+ dayLength + ", push = 'push' where pid = " + device.pid;
    	System.out.println("sql: " + sql);
    	int result = stmt.executeUpdate(sql);
		//print result
		System.out.println("result: " + result);
		*/

	}
	
	private String sunDataForDate(Device device, Date date) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String url = "http://api.sunrise-sunset.org/json?lat=" + device.latitude + "&lng=" + device.longitude + "&date=" + dateFormat.format(date) + "&formatted=0";
		
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("GET");

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		System.out.println(response.toString());
		return response.toString();
	}
	
	private String extractSunsetString(String response) throws Exception {
		JsonElement je = new Gson().fromJson(response, JsonElement.class);
		je = je.getAsJsonObject().get("results");
    	String sunsetStr = je.getAsJsonObject().get("sunset").getAsString();
		//System.out.println("sunsetStr: " + sunsetStr);
    	String[] dateTimeParts = sunsetStr.split("T", 2);

    	int end = dateTimeParts[1].length() - 6;
    	System.out.println("dateTimeParts[0]: " + dateTimeParts[0] + ", dateTimeParts[1].substring(0, end): " + dateTimeParts[1].substring(0, end));
    	return dateTimeParts[0] + " " 
		+ dateTimeParts[1].substring(0, end);
    	
	}
	
	private int extractDayLength(String response) throws Exception {
		JsonElement je = new Gson().fromJson(response, JsonElement.class);
		je = je.getAsJsonObject().get("results");
		return je.getAsJsonObject().get("day_length").getAsInt();
	}
	
}
