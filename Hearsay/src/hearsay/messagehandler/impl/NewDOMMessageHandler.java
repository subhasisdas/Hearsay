package hearsay.messagehandler.impl;

import server.Message;
import hearsay.browserstate.Browser;
import hearsay.browserstate.Tab;
import hearsay.messagehandler.MessageHandler;

public class NewDOMMessageHandler implements MessageHandler
{

	@Override
	public boolean handleMessage(Message message, Tab tab, Browser browser)
	{
		//Populate the tab with the new dom tree from the given message
		return true;
	}

}
