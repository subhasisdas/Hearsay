package hearsay.messagehandler;

import server.Message;
import hearsay.browserstate.Browser;
import hearsay.browserstate.Tab;

public interface MessageHandler
{
	public boolean handleMessage(Message message, Tab tab, Browser browser);	
}
