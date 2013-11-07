package hearsay.messagehandler;

import server.Communicator;
import server.Message;
import server.SocketProcessor;
import server.Tab;

public interface MessageHandler
{
	public boolean handleMessage(Message message, Tab tab, SocketProcessor sp) throws Exception;	
}
