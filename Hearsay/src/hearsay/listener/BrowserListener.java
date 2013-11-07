package hearsay.listener;

import server.Communicator;
import server.SocketProcessor;

/**
 * Represents a browserlistener in the hearsay system
 * Note: not currently used
 * @author Subhasis Das
 *
 */
public interface BrowserListener 
{
		void pushBrowserEvent(Communicator source, SocketProcessor sp,String event);
}
