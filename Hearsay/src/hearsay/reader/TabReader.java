package hearsay.reader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import hearsay.browserstate.Tab;

/**
 * Responsible for reading a given tab
 * @author Manoj Chandwani
 *
 */
public class TabReader implements Runnable
{
	Tab tabToRead = null;
	
	boolean cancelSpeaking = false;
	
	public TabReader(Tab tabToRead)
	{
		this.tabToRead = tabToRead;
	}
	
	public void cancelSpeaking()
	{
		cancelSpeaking = true;
	}
	
	public InputStream readTab() throws Exception
	{
		if(tabToRead == null)
		{
			throw new Exception("Tab to read not initialized");
		}
		String tabContent = tabToRead.getDomTree().toString();
		InputStream stream = new ByteArrayInputStream(tabContent.getBytes("UTF-8"));
		return stream;
	}

	@Override
	public void run()
	{
		try
		{
			InputStream tabInputStream = readTab();
			while(!cancelSpeaking)
			{
				//Read from the tab input stream and issue speak requests
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
