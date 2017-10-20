import java.sql.SQLException;



//import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	//Gson gson = new Gson();
        //System.out.println(gson.toJson( "Hello World!") );
        
        Sunset sunset = new Sunset();
        APNS apns = new APNS();
        try {
        	do {
        		String timeStampString = apns.getLastRunTimeStamp();
            	
            	sunset.updateTimes(timeStampString);
            	apns.sendPushNotifications();
            	//System.out.println("isActive: " + apns.isActive());
        	} while(apns.isActive());  
        } catch (ClassNotFoundException e) {
	    	e.printStackTrace();
			System.out.println("\n'Class.forName' exception: " + e);
        } catch (SQLException e) {
	    	e.printStackTrace();
			System.out.println("\n'SQL' exception: " + e);
        } catch (JsonSyntaxException e) {
	    	e.printStackTrace();
			System.out.println("\n'JSON' exception: " + e);
        } catch (Exception e) {
			e.printStackTrace();
			System.out.println("\n'GET' exception: " + e);
	    }
    }
}
