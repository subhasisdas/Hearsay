package server;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

enum MessageType
{
	NEW_DOM,
	NEW_TAB,
	FOCUS_CHANGE,
	VOICE_PAYLOAD;
}

/**
 * Represents a message in the hearsay system
 * @author Manoj Chandwani
 *
 */
public class Message
{
	private MessageType type;

	private int tabId;

	private final Map<String, List<String>> arguments;

	private Node payload;
	
	private static DocumentBuilder builder;
	
	static
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	public Message(String s)
	{
		arguments = null;
	}

	public Message(MessageType type, Integer tabId)
	{
		this.type = type;
		this.tabId = tabId;
		arguments = new HashMap<String, List<String>>();
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public Integer getTabId() {
		return tabId;
	}

	public void setTabId(Integer tabId) {
		this.tabId = tabId;
	}

	public Map<String, List<String>> getArguments() {
		return arguments;
	}

	public Node getPayload() {
		return payload;
	}

	public void setPayload(Document payload) {
		this.payload = payload;
	}

	private static Message parseDocument(Document xmlDocument) throws Exception
	{

		Integer tabId = Integer.parseInt((xmlDocument.getElementsByTagName("tabId").item(0)).getFirstChild().getNodeValue());
		//TODO Illegal argument catch
		String type = xmlDocument.getElementsByTagName("type").item(0).getFirstChild().getNodeValue();
		Message message = null;
		try
		{
			message = new Message(MessageType.valueOf(type), tabId);
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace();
			throw e;
		}
		Node parameters = xmlDocument.getElementsByTagName("parameters").item(0);
		if(parameters.getNodeType() == Node.ELEMENT_NODE)
		{
			Element parametersElement = (Element) parameters;
			NodeList parameterList = parametersElement.getElementsByTagName("parameter");
			int index = 0;
			while(index < parameterList.getLength())
			{
				Element eParameter = (Element) parameterList.item(index);
				NodeList parameterNameNodes = eParameter.getElementsByTagName("parameterName");
				if(parameterNameNodes.getLength() > 1 || parameterNameNodes.getLength() < 1)
				{
					throw new Exception("Node for parameter name not found");
				}
				String parameterName = parameterNameNodes.item(0).getTextContent();
				NodeList parameterValuesNodeList = eParameter.getElementsByTagName("parameterValues");
				if(parameterValuesNodeList.getLength() > 1 || parameterValuesNodeList.getLength() < 1)
				{
					throw new Exception("Node for parameter name not found");
				}
				Element parameterValues = (Element)parameterValuesNodeList.item(0);
				NodeList parameterValueNodeList = parameterValues.getElementsByTagName("parameterValue");
				int iterator = 0;
				int parameterValueCount = parameterValueNodeList.getLength();
				ArrayList<String> parameterValueList = new ArrayList<String>();
				while(iterator < parameterValueCount)
				{
					Element parameterValue = (Element)parameterValueNodeList.item(iterator);
					parameterValueList.add(parameterValue.getTextContent());
					iterator++;
				}
				if(message != null)
				{
					message.getArguments().put(parameterName, parameterValueList);
				}
				index++;
			}
		}
		return message;
	}

	public static Message fromString(String hearsayMessage) throws Exception
	{
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(hearsayMessage));
		Document hearsayDocument = builder.parse(is);
		return parseDocument(hearsayDocument);
	}

	public String convertToString() throws Exception
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			//Could make a static
			builder = factory.newDocumentBuilder();

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		if(builder != null)
		{
			Document hearsayXMLMessage = builder.newDocument();
			Element hearsayMessage = (Element) hearsayXMLMessage.createElement("hearsayMessage");
			hearsayXMLMessage.appendChild(hearsayMessage);

			Element id = hearsayXMLMessage.createElement("tabId");
			Text idValue = hearsayXMLMessage.createTextNode(Integer.toString(this.tabId));
			id.appendChild(idValue);
			hearsayMessage.appendChild(id);

			Element type = hearsayXMLMessage.createElement("type");
			Text typeValue = hearsayXMLMessage.createTextNode(this.type.toString());
			type.appendChild(typeValue);
			hearsayMessage.appendChild(type);

			Element parameters = hearsayXMLMessage.createElement("parameters");
			for(String parameterName : this.arguments.keySet())
			{
				Element parameter = hearsayXMLMessage.createElement("parameter");
				Element paramName = hearsayXMLMessage.createElement("parameterName");
				paramName.appendChild(hearsayXMLMessage.createTextNode(parameterName));
				List<String> parameterValues = this.arguments.get(parameterName);
				Element paramValues = hearsayXMLMessage.createElement("parameterValues");
				parameter.appendChild(paramName);
				parameter.appendChild(paramValues);
				for(String parameterValue : parameterValues)
				{
					Element paramValue = hearsayXMLMessage.createElement("parameterValue");
					Text paramValueField = hearsayXMLMessage.createTextNode(parameterValue);
					paramValue.appendChild(paramValueField);
					paramValues.appendChild(paramValue);
				}
				parameters.appendChild(parameter);
			}
			hearsayMessage.appendChild(parameters);
			//Clone the payload that may be from a different document
			if(this.payload != null)
			{
				Element payloadElement = hearsayXMLMessage.createElement("payload");
				payloadElement.appendChild(this.payload.cloneNode(true));
				hearsayMessage.appendChild(payloadElement);
			}

			Writer outWriter = new StringWriter();
			StreamResult result=new StreamResult(outWriter);
			TransformerFactory tranFactory = TransformerFactory.newInstance();
			Transformer transformer = tranFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			Source source = new DOMSource(hearsayXMLMessage);
			transformer.transform(source, result);
			return outWriter.toString();
		}
		else
		{
			throw new Exception("Could not create document builder");
		}
	}

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException
	{
		Message myMessage = new Message(MessageType.NEW_DOM, 5);
		myMessage.getArguments().put("param1", new ArrayList<String>());
		myMessage.getArguments().put("param2", new ArrayList<String>());
		(myMessage.getArguments().get("param1")).add("value1");
		(myMessage.getArguments().get("param2")).add("value2");
		try {
			System.out.println(Message.fromString(myMessage.convertToString()).convertToString());
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
