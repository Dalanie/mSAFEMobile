package melb.mSafe.database;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import melb.mSafe.chat.ServerMessage;
import melb.mSafe.chat.UserMessage;
import melb.mSafe.model.RouteGraph;
import android.os.Message;

/**
 * A Mock-DatabaseHandler for the communiciation between Client and
 * Client-Databse
 * 
 * @author Daniel Langerenken
 */
public class MockDatabaseHandler implements IDatabaseHandler {

    public MockDatabaseHandler() {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
	}

	@Override
	public RouteGraph getLatestRouteGraph() {
		return  new RouteGraph();
	}

	@Override
	public List<Message> getLatestMessages() {
		return new ArrayList<Message>();
	}

	@Override
	public List<ServerMessage> getLatestServerMessages() {
		return new ArrayList<ServerMessage>();
	}

	@Override
	public List<UserMessage> getLatestUserMessages() {
		return new ArrayList<UserMessage>();
	}

}
