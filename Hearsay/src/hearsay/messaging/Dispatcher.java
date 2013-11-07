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
		Tab tab = sp.getTab(tabId);
		if(tab == null)
		{
			tab = sp.createNewTab(tabId);
			if(sp.getActiveTabId() == null)
			{
				sp.setActiveTabId(tabId);
			}
		}
		synchronized(tab)
		{
			for(MessageHandler messageHandler : messageHandlers)
			{
				if(messageHandler.handleMessage(message, tab, sp))
				{
					return;
				}
			}
			throw new Exception("No handler found for message : " + message.toString());
		}
	}

}
