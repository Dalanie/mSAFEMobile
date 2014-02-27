package melb.mSafe.database;

import java.io.IOException;
import java.util.List;

import melb.mSafe.chat.ServerMessage;
import melb.mSafe.chat.UserMessage;
import melb.mSafe.model.RouteGraph;
import android.os.Message;

/**
 * The Interface for the communication between Client and Client-Database
 * 
 * @author Daniel Langerenken
 */
public interface IDatabaseHandler {
	RouteGraph getLatestRouteGraph();

	List<Message> getLatestMessages();

	List<ServerMessage> getLatestServerMessages();

	List<UserMessage> getLatestUserMessages();

}
