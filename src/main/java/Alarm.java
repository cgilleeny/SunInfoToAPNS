
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;


public class Alarm {
	public int id;
	public String sound;
	public int offset;
	public String status;
	public String deviceuid;
	public float latitude;
	public float longitude;
	public int pid;
	public Timestamp sunset;
	public String token;
	public int daylength;
	public String development;
	
	public Alarm(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		sound = rs.getString("sound");
    	offset = rs.getInt("offset");
    	status = rs.getString("status");
		deviceuid = rs.getString("deviceuid");
		latitude = rs.getFloat("latitude");
		longitude = rs.getFloat("longitude");
		pid = rs.getInt("pid");
		token = rs.getString("devicetoken");
		sunset = rs.getTimestamp("sunset");
		daylength = rs.getInt("daylength");
		development = rs.getString("development");
	}

}

