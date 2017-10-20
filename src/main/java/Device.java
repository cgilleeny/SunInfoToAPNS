import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;


public class Device {
	  public String deviceuid;
	  public float latitude;
	  public float longitude;
	  public int pid;
	  public Timestamp sunset;
	  public String token;
	  public int daylength;
	  
	  /*
	  public Device(String deviceuid, String token, float latitude, float longitude, int pid, Timestamp sunset, int daylength) {
	    this.deviceuid = deviceuid;
	    this.latitude = latitude;
	    this.longitude = longitude;
	    this.pid = pid;
	    this.token = token;
	    this.sunset = sunset;
	    this.daylength = daylength;
	  }
	  */
	  
	  public Device(ResultSet rs) throws SQLException {
		  this.deviceuid = rs.getString("deviceuid");
		    this.latitude = rs.getFloat("latitude");
		    this.longitude = rs.getFloat("longitude");
		    this.pid = rs.getInt("pid");
		    this.token = rs.getString("devicetoken");
		    this.sunset = rs.getTimestamp("Sunset");
		    this.daylength = rs.getInt("daylength");
	  }
}
