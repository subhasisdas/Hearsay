function log(msg) {
	consoleService.logStringMessage(msg);
}

var Hearsay_Extension = 
{
		onConnect: function() 
		{
			hstInit(function()
					{
				log("We've connected to the server!");
//				hstSendMsg("Connected!");

					}, 
					function()
					{
						log("Server disconnected!");
					}, 
					function(hearsayXMLMessage)
					{
						log("onConnect! " + hearsayXMLMessage);
						newmessageref = createmessage(hearsayXMLMessage);
						
						log("Received from server : " + newmessageref.getTabId() + " " + newmessageref.getMessageType() + " " + newmessageref.getParameter("param1"));
//						log("Received from Server : "+msg);
					}
			);
		},
		onDisconnect: function()
		{
			hstDone();
		},
		onSend: function()
		{
			log("[Firefox Client]: sending");
			messageref = new message("NEW_DOM",1);
			var parameterName = "param1";
			var parameterValues = ["value1", "value2"];
			messageref.setParameter(parameterName, parameterValues);
			var hearsayXMLMessage = messageref.convertToString();
			log("[Firefox Client]: XML Message is : " + hearsayXMLMessage);
//			newmessageref = createmessage(hearsayXMLMessage);
//			log(newmessageref.getTabId() + " " + newmessageref.getMessageType() + " " + newmessageref.getParameter());
			
			hstSendMsg(hearsayXMLMessage);
		}
};

log("Hearsay_Extension is loaded");

//window.onload=function()("load", function(e) { Hearsay_Extension.onConnect(); }, false); 