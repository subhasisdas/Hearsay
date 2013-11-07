package hearsay.browserstate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import server.Message;
import hearsay.messaging.Dispatcher;
import hearsay.reader.TabReader;

/**
 * A Runnable that represents a browser instance and manages state of tabs in the instance
 *
 */
public class Browser implements Runnable
{
	private Map<Integer, Tab> inactiveTabs;
	
	private Integer activeTabId = null;

	private boolean browserInstanceClosed = false;
	
	private Dispatcher messageDispatcher = null;
	
	private BlockingQueue<Message> outstandingMessages = null;
	
	public Browser(BlockingQueue<Message> outstandingMessages, Dispatcher messageDispatcher) throws Exception
	{
		this.messageDispatcher = messageDispatcher;
		this.outstandingMessages = outstandingMessages;
		inactiveTabs = new HashMap<Integer, Tab>();
		this.processOutstandingMessages();
		if(activeTab == null)
		{
			throw new Exception("No active tab found after initialization");
		}
	}
	
	public Tab createNewTab(Integer tabId) throws Exception
	{
		if(getTab(tabId) != null)
		{
			throw new Exception("A tab already exists with this tab identifier");
		}
		else
		{
			Tab newTab = new Tab(tabId, null);
			this.inactiveTabs.put(tabId, newTab);
			return newTab;
		}
	}
	
	public Tab getTab(Integer tabId)
	{
		if(inactiveTabs.containsKey(tabId))
		{
			return inactiveTabs.get(tabId);
		}
		if(activeTab.getTabId().equals(tabId))
		{
			return activeTab;
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Works on speaking the current tab and interruptible by events 
	 */
	@Override
	public void run()
	{
		while(!browserInstanceClosed)
		{
			//Read the active tab until an interrupt happens
			while(!Thread.interrupted())
			{	
				TabReader reader = new TabReader(activeTab);
				Thread tabReader = new Thread(reader);
				tabReader.start();
				try
				{
					tabReader.join();
				}
				catch (InterruptedException e)
				{
					//Cancel speaking and Process any outstanding messages
					reader.cancelSpeaking();
					try
					{
						this.processOutstandingMessages();
					}
					catch(Exception messageError)
					{
						messageError.printStackTrace();
					}
				}
			}
			try {
				this.processOutstandingMessages();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Process any outstanding messages and cancel previous TTS speaking
			
		}
	}
	
	public void processOutstandingMessages() throws Exception
	{
		while(!this.outstandingMessages.isEmpty())
		{
			Message message = this.outstandingMessages.poll();
			if(message != null)
			{
				messageDispatcher.processMessage(this, message);
			}
		}
	}
	
}
