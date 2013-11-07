package hearsay.messaging;

import java.util.List;

import server.Message;
import hearsay.browserstate.Browser;
import hearsay.browserstate.Tab;
import hearsay.messagehandler.MessageHandler;

public class Dispatcher
{

	List<MessageHandler> messageHandlers;

	public Dispatcher(List<MessageHandler> messageHandlers)
	{
		this.messageHandlers = messageHandlers;
	}

	public void processMessage(Browser browser, Message message) throws Exception
	{
		//Create Tab if does not exist, else fetch existing tab
		Integer tabId = message.getTabId();
		Tab tab = browser.getTab(tabId);
		if(tab == null)
		{
			tab = browser.createNewTab(tabId);
		}
		synchronized(tab)
		{
			for(MessageHandler messageHandler : messageHandlers)
			{
				if(messageHandler.handleMessage(message, tab, browser))
				{
					return;
				}
			}
			throw new Exception("No handler found for message : " + message.toString());
		}
	}

}
