package hearsay.listener;

import server.Communicator;
import server.SocketProcessor;

/**
 * Represents a communication listener in the hearsay system
 * @author Subhasis Das
 *
 */
public interface CommunicationListener {

	void onConnect(Communicator source, SocketProcessor sp);

/*  can be added when required
	void onStart(Communicator source);
 	void onStop(Communicator source);
*/
}
