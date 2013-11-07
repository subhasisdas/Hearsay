package hearsay.messagehandler.impl;

import server.Communicator;
import server.Message;
import server.SocketProcessor;
import server.Tab;
import hearsay.messagehandler.MessageHandler;

public class NewDOMMessageHandler implements MessageHandler
{

	@Override
	public boolean handleMessage(Message message, Tab tab, SocketProcessor sp) throws Exception
	{
		System.out.println(sp.getId()+"[Handling the message] : " + message.convertToString());
		//Populate the tab with the new dom tree from the given message
		return true;
	}

}
