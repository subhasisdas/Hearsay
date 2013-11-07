package hearsay.messaging;

import java.util.List;

import server.Communicator;
import server.Message;
import server.SocketProcessor;
import server.Tab;
import hearsay.messagehandler.MessageHandler;

public class Dispatcher
{

	List<MessageHandler> messageHandlers;

	public Dispatcher(List<MessageHandler> messageHandlers)
	{
		this.messageHandlers = messageHandlers;
	}

	public void processMessage(SocketProcessor sp, Message message) throws Exception
	{
		//Create Tab if does not exist, else fetch existing tab
		Integer tabId = message.getTabId();
		Communicator comm = sp.getComm();
		Tab tab = comm.getTab(tabId);
		if(tab == null)
		{
			tab = comm.createNewTab(tabId);
		}
		synchronized(tab)
		{
			for(MessageHandler messageHandler : messageHandlers)
			{
				if(messageHandler.handleMessage(message, tab, comm))
				{
					return;
				}
			}
			throw new Exception("No handler found for message : " + message.toString());
		}
	}

}
