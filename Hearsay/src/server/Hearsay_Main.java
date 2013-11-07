package server;

import hearsay.listener.CommunicationListener;
import hearsay.listener.SocketProcessorListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/*Main Class
 * @author - Subhasis Das
 * 
 */
public class Hearsay_Main  {

	final static List<SocketProcessor> connections = Collections.synchronizedList(new ArrayList<SocketProcessor>());

	final static CommunicationListener commListener = new CommunicationListener() {
		@Override
		public void onConnect(Communicator source, SocketProcessor sp) {
			// TODO Auto-generated method stub
			System.out.println(sp.getId()+"[Commlistener] : Connected");
			connections.add(sp);
			sp.setSpListener(spListener);
		}
	};

	final static SocketProcessorListener spListener = new SocketProcessorListener() {
		@Override
		public void onReceive(SocketProcessor sp, String message) {
			// TODO Auto-generated method stub
			System.out.println(sp.getId()+"[SocketProcessorListener] : Received :"+ message);
			try {
				Message parsedMsg= Message.fromString(message);
				System.out.println(sp.getId()+"[Parsed Message]:"+parsedMsg.getType()+parsedMsg.getArguments()+parsedMsg.getTabId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("Invalid Message from the client");
				e.printStackTrace();
			}
			
		}

		@Override
		public void onDisconnect(SocketProcessor sp) {
			// TODO Auto-generated method stub
			System.out.println(sp.getId()+"[SocketProcessorListener] : Disconnected");
			connections.remove(sp);
		}
	};


	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

		//randomly generate port number. Connection per browser instance
				
		final int MIN_PORT_NUM = 49152;
		final int MAX_PORT_NUM = 65533;
		
		int [] usedports = new int[100] ;
		
		Random r = new Random();
		int PORT_NUM = 0;
		PORT_NUM = r.nextInt(MAX_PORT_NUM - MIN_PORT_NUM +1) + MIN_PORT_NUM;
		
		//		int COMMUNICATOR_PORT = 12335;
		
		for (int i = 0; i< usedports.length; i++)
		{
			if (usedports[i] == PORT_NUM)
					PORT_NUM = r.nextInt(MAX_PORT_NUM - MIN_PORT_NUM +1) + MIN_PORT_NUM;
		}
		
		System.out.println("Port number used :" + PORT_NUM);
		
		Communicator comm = new Communicator(PORT_NUM);
		comm.setListener(commListener);
		//comm.run();
	 
		comm.start();
		
		
		
		/*XML Parser implementation:*/
		Message myMessage = new Message(MessageType.NEW_DOM, 4);
		myMessage.getArguments().put("param1", new ArrayList<String>());
		myMessage.getArguments().put("param2", new ArrayList<String>());
		myMessage.getArguments().put("param3", new ArrayList<String>());
		(myMessage.getArguments().get("param1")).add("value1");
		(myMessage.getArguments().get("param2")).add("value2");
		
//		System.out.println(hearsayXMLMessage);
//		System.out.println("The string is : " + hearsayXMLMessage);
//		Message newMessage = Message.parseString(hearsayXMLMessage);
//		System.out.println("Results are : " + newMessage.getTabId() + " " + newMessage.getType() + " " + newMessage.getArguments());
		/*end*/
		
		final BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
		for(;;)
		{
						
			String s = bufferRead.readLine();
			(myMessage.getArguments().get("param3")).add(s);
			String hearsayXMLMessage = myMessage.convertToString();
			if(s.isEmpty())
				break;
			
//			System.out.println("Sending: "+hearsayXMLMessage);
			for(SocketProcessor sp: connections)
				{
				sp.send(hearsayXMLMessage);
//				sp.send(s);
				}
			System.out.println("Sent!");
			
		}
		comm.stop();
		System.out.println("Zed is dead");
	}


}
