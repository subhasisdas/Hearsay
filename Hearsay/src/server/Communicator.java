package server;

import hearsay.listener.CommunicationListener;
import hearsay.messaging.Dispatcher;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the basic communication class which is used to render to the incoming
 * browser client connections. This is a singleton class and cannot be
 * instantiated but the singleton instance can be obtained.
 * 
 * @author Subhasis Das
 * 
 */


public class Communicator  extends hearsay.util.Loggable implements Runnable
{
	private ServerSocket server=null;
	private Thread threadClient= null;
	private final int port;
	private CommunicationListener listener;
	private Map<Integer, Tab> tabs;
	
	public Map<Integer, Tab> getTabs() {
		return tabs;
	}

	private Integer activeTabId = null;
	private Dispatcher messageDispatcher = null;
	
	
	public Tab createNewTab(Integer tabId) throws Exception
	{
		if(getTab(tabId) != null)
		{
			throw new Exception("A tab already exists with this tab identifier");
		}
		else
		{
			Tab newTab = new Tab(tabId, null);
			this.tabs.put(tabId, newTab);
			return newTab;
		}
	}
	
	public Tab getTab(Integer tabId)
	{
		if(tabs.containsKey(tabId))
		{
			return tabs.get(tabId);
		}
		return null;
	}
	
	public Integer getActiveTabId() {
		return activeTabId;
	}


	public void setActiveTabId(Integer activeTabId) {
		this.activeTabId = activeTabId;
	}


	/**
	 * New browser listeners can register using this call to be notified on events
	 * 
	 * @param listener -
	 *            A BrowserListener type of a class to be registered
	 * @return 
	 * @throws Exception 
	 */
	public synchronized  CommunicationListener setListener(CommunicationListener l) throws Exception{
		if(server!=null)
			throw new Exception("Server is still running!");

		final CommunicationListener prev = listener;
		listener = l;
		return prev;
	}

	
	public synchronized CommunicationListener getListener(){
		return listener;
	}

	//Constructor
	public Communicator(Dispatcher messageDispatcher, int COMMUNICATOR_PORT) 
	{
		port = COMMUNICATOR_PORT;
		this.messageDispatcher = messageDispatcher;
		tabs = new HashMap<Integer, Tab>();
		activeTabId = null;
	}

	/**
	 * Run method for this thread which blocks on the accept call of the server
	 * and spawns new threads for every connection.
	 */
	@Override
	public void run() {
		try {
			while (true) {                	                	
				/**
				 * This is a blocking call which would unblock as soon as a
				 * client connects to it.
				 */

				// use loggable for this to mask output
				//sdas: done

				SetLogLevel(0);SetLinePrefix("[Communicator] :");
				log(1,this.getClass().getSimpleName()+" : accepting");

				Socket clientSocket = server.accept();

				log(1,this.getClass().getSimpleName()+"accepted");

				final SocketProcessor browserConnect = new SocketProcessor(clientSocket, this);

				//Communication Listener
				listener.onConnect(this, browserConnect);

				/**
				 * Delegate the processing of the client to a separate
				 * thread and return to the accept state. This is the usual
				 * pattern of a multi-client server.
				 */
				new Thread(browserConnect, "SocketConnect " + clientSocket.getInetAddress()+":"+ clientSocket.getPort()).start();
			}
		} catch (Exception e) {
			/**
			 * Thrown when a socket.close() is called when the socket is
			 * blocked in the accept call.
			 */
			log(0, e.getMessage());
			e.printStackTrace();
			log(0,"Communicator aux thread is about stop");               
		}
	}

	/**
	 * This function starts the communicator socket if it was previously
	 * shutdown.
	 * @throws Exception 
	 */
	public synchronized void  start() throws Exception {
		if (server == null) {
			server = new ServerSocket(port);
			log(1,"creating thread");
			threadClient = new Thread(this);
			threadClient.start();
			log(1,"Communicator Socket is UP and Running!");
		} else
			log(1,"Communicator Socket is already Running!");

	}

	/**
	 * This function stops the communicator socket.
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public synchronized void stop() throws IOException, InterruptedException 
	{
		log(1,"Communicator Stopped");
		server.close();
		server = null;
		threadClient.join();
	}


}



