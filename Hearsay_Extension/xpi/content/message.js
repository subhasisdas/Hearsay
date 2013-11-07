/**
 /* This class represents the messaging layer on the client end for HearSay
 */

function message(messagetype, tabid) {
	this.messagetype = messagetype;
	this.tabid = tabid;
	this.parameters = {};
	this.payload = "";

	this.convertToString = convertToString;	

	function convertToString() {
		var myXMLdoc;
		/*if (window.ActiveXObject) {
			myXMLdoc = new ActiveXObject("Microsoft.XMLDOM");
			myXMLdoc.async = false;
		}
		else*/ 
		if (document.implementation && document.implementation.createDocument) {
			myXMLdoc = document.implementation.createDocument("", "", null);
		}
		var hearsayMessage = myXMLdoc.appendChild(myXMLdoc.createElement("hearsayMessage"));
		var tabId = myXMLdoc.createElement("tabId");
		tabId.appendChild(myXMLdoc.createTextNode(this.tabid));
		hearsayMessage.appendChild(tabId);
		var type = myXMLdoc.createElement("type");
		type.appendChild(myXMLdoc.createTextNode(this.messagetype));
		hearsayMessage.appendChild(type);
		var parameters = myXMLdoc.createElement("parameters");
		for(parameterName in this.parameters)
		{
			var parameter = myXMLdoc.createElement("parameter");
			var parameterNameNode = myXMLdoc.createElement("parameterName");
			parameterNameNode.appendChild(myXMLdoc.createTextNode(parameterName));
			var parameterValues = myXMLdoc.createElement("parameterValues");
			var valuesList = this.parameters[parameterName];
			for(valueIndex = 0; valueIndex < valuesList.length; valueIndex++)
			{
				var parameterValue = myXMLdoc.createElement("parameterValue");
				parameterValue.appendChild(myXMLdoc.createTextNode(valuesList[valueIndex]));
				parameterValues.appendChild(parameterValue);
			}
			parameter.appendChild(parameterNameNode);
			parameter.appendChild(parameterValues);
			parameters.appendChild(parameter);
		}
		hearsayMessage.appendChild(parameters);
		var hearsayXMLString = new XMLSerializer().serializeToString(hearsayMessage);
		return hearsayXMLString;
	}

	this.getParameter = getParameter;

	function getParameter(parametername) {
		return this.parameters[parametername];
	}

	this.setParameter = setParameter;

	function setParameter(parametername, parametervalues) {
		this.parameters[parametername] = parametervalues;
	}
	
	this.getTabId = getTabId;

	function getTabId() {
		return this.tabid;
	}
	
	this.setTabId = setTabId;

	function setTabId(newtabid) {
		this.tabId = newtabid;
	}
	
	this.getMessageType = getMessageType;

	function getMessageType() {
		return this.messagetype;
	}

	this.setMessageType = setMessageType;
	
	function setMessageType(newmessagetype) {
		this.messagetype = newmessagetype;
	}
}

function createmessage(xmlMessage) {
//	var myXMLMessage = "<hearsayMessage><tabId>4</tabId><type>NEWDOM</type><parameters><parameter><parameterName>param1</parameterName><parameterValues><parameterValue>value1</parameterValue></parameterValues></parameter><parameter><parameterName>param2</parameterName><parameterValues><parameterValue>value2</parameterValue></parameterValues></parameter></parameters></hearsayMessage>";
	var myXMLMessage = xmlMessage;
	parser=new DOMParser();
	xmlDoc=parser.parseFromString(myXMLMessage,"text/xml");
	log("Message being parsed : " + myXMLMessage);
	var tabId = xmlDoc.getElementsByTagName('tabId')[0].childNodes[0].nodeValue;
	var messageType = xmlDoc.getElementsByTagName('type')[0].childNodes[0].nodeValue;
	var parameterList = xmlDoc.getElementsByTagName('parameters')[0].getElementsByTagName('parameter');
	var parameterCount = parameterList.length;
	messageReference = new message(messageType, tabId);
	log("Parameter count : " + parameterList.length);
	for(var index = 0; index < parameterList.length ; index++)
	{
		log("index :"+ index);
		log("Index value is : " + parameterList[index]);
		//log("Count of parameter is " + xmlDoc.getElementsByTagName('parameter'));
		//log("The type of this parameter is : " + typeof parameterList[index]);
		var parameterName = parameterList[index].getElementsByTagName('parameterName')[0].textContent;
		log("The param name is : " + parameterName);
		var parameterValueList = parameterList[index].getElementsByTagName('parameterValues')[0].childNodes;
		var parameterValues = [];
		for(var parameterValueIndex = 0; parameterValueIndex < parameterValueList.length ; parameterValueIndex++)
		{
			
			var parameterValue = parameterValueList[parameterValueIndex].textContent;
			parameterValues.push(parameterValue);
		}
		messageReference.parameters[parameterName] = parameterValues;
		log("Message type is : " + messageReference.messagetype);
		return messageReference;
	}
}
 