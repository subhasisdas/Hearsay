/*
 * Author Valentyn Melnik
 * this file contain parts of communication.js and SocketComponent.js
 * whose really need to send and receive communication messages between
 * HearSay and FireFix
 *
 * rem:
 *  methods & variables with name like as "_*" are only for internal use
 */

var consoleService = Components.classes["@mozilla.org/consoleservice;1"].getService(Components.interfaces.nsIConsoleService);

/**
 * To maintain logs of any message in the error console
 * @param {Object} msg Logged message.
 */
function log(msg) {
	consoleService.logStringMessage(msg);
}


const hstHost = "localhost"; 		// HearSay host,                            vartype: string
const hstPort = 12330;       		// HearSay host's port,                     vartype: integer
const hstConnectTimeout = 5000;		// connect timeout in milliseconds
const hstReconnectInterval = 2000;	// if connect failed, trying reconnect with this interval

const hstThreadManager = Components.classes["@mozilla.org/thread-manager;1"]
.createInstance(Components.interfaces.nsIThreadManager);

const hstTransportService = Components.classes["@mozilla.org/network/socket-transport-service;1"]
.getService(Components.interfaces.nsISocketTransportService);

const hstLogLevel = 2;				// log levels, by importance: the bigger level value means the little event  
// 0 - no messages
// 1 - critical messages
// 2 - work information
// 3 - some debug data, 
// ... etc

var TimerComponent 				= Components.Constructor("@mozilla.org/timer;1", Components.interfaces.nsITimer);   
var BinaryInputStreamComponent  = Components.Constructor("@mozilla.org/binaryinputstream;1", Components.interfaces.nsIBinaryInputStream);

var _hstInstance;			// instance of HearSayTransport 

/*
 * this function is HearSayTransport constructor.
 *
 *  @param connect_callback 	HearSayTransport call this callback after 
 * 								successfull connect to HearSay server
 *  @param disconnect_callback	HearSayTransport call this callback 
 * 								before cleanup transport on disconnect by exception
 *  @param message_callback		HearSayTransport call this callback after complete
 * 								loading message from HS server.
 */
function HearSayTransport(connect_callback, disconnect_callback, message_callback)
{
	this._OnMsgCallback  		= message_callback;
	this._OnConnectCallBack		= connect_callback;
	this._OnDisconnectCallBack	= disconnect_callback;

	// this._transport.setTimeout(this._transport.TIMEOUT_CONNECT, hstConnectTimeout);	// it doesnt work. I dont know, why.
	// workaround of connection timeout
	// after start connecting to server starting _ConnectTimeOutTimer.
	// on signal from this timer status changed to "canceled", 
	// and then starting _ReConnectTimer, after it HearSayTransport try connect again 
	this._ConnectTimeOutTimer = new TimerComponent();
	this._ReConnectTimer 	  = new TimerComponent(); 

	this._InStream	   = null;
	this._in		   = null;  

	if(!this._InitTransport())
		this._ReConnectTimer.initWithCallback(this, hstReconnectInterval, 0);	// Type: TYPE_ONE_SHOT

	this.Log(1, "registered ok");

	return this;
}

HearSayTransport.prototype = {
		Log: function(/*int*/ level,/*string*/ msg) {
			if(level<=hstLogLevel)
				log(msg);
		},
//		internal data
//		data
//		methods
		Done: function() {
			this.Log(1, "hstDone");
			if(this._ReConnectTimer != null)
			{
				this._ReConnectTimer.cancel();
				this._ReConnectTimer = null;
			}

			this._DestroyTransport();

			this._StopConnectTimeoutTimer();       	
			this._ConnectTimeOutTimer = null;

		},

		/*
		 * internal use only function. Canceled connect timeout timer  
		 */
		_StopConnectTimeoutTimer: function() {
			this.Log(4, "Cancel ConnectTimeout timer");
			if(this._ConnectTimeOutTimer != null)
				this._ConnectTimeOutTimer.cancel();
		},

		/*
		 * internal use only function. initialize all data and objects, which
		 * work with channel data, such as: input/output stream, delivered data,
		 * internal state of message loader.   
		 */
		_InitTransport: function(){
			try {
				this._data 		   	 	= "";
				this._DataLength   	 	= 0;
				this._ReadCounter  	 	= 0;
				this._transport    		= hstTransportService.createTransport(null, 0, hstHost, hstPort, null);

				this._ConnectTimeOutTimer.initWithCallback(this, hstConnectTimeout, 0);	// Type: TYPE_ONE_SHOT  				    
				this._transport.setEventSink(this, hstThreadManager.mainThread); // this forces the async transport run in main thread of FF		

				this._OutStream    		= this._transport.openOutputStream(1, 0, 0);
			}
			catch(e){			
				this.Log(2, "_InitTransport exception: " + e.name);
				this.Log(2, "\t" + e.message);
				this._DestroyTransport();
				this._StopConnectTimeoutTimer();
				return false;
			}  
			return true;
		},

		/*
		 * internal use only function. an antipode of _InitTransport().
		 * destroy all associated with socket   
		 */	
		_DestroyTransport: function(){
			if(this._transport != null) {
				try
				{
					if(this._OutStream != null)
						this._OutStream.close();
					this._transport.close(0);
				}
				catch(e)
				{
					this.Log(4, "Shit happens");
				}
				this._OutStream = null;
				this._InStream  = null;
				this._in		= null;            	
				this._transport = null;
			}  
		},

		/*
		 * nsITimerCallback interface.
		 * this function invoke by both timers: timeout and reconnect.
		 * which timer and which action it will do, depend of parameter timer
		 * 
		 * @param timer                which timer invoked this function
		 */
		notify: function(/*in nsITimer*/ timer) {
			if(this._ReConnectTimer == timer)	// is it signal from Reconnect timer?
			{	// ok, now time to reconnect
				this.Log(4, "I live again. Hardrock Hallelujah!");
				//this._StopConnectTimeoutTimer();
				if(!this._InitTransport())
					this._ReConnectTimer.initWithCallback(this, hstReconnectInterval, 0);	// Type: TYPE_ONE_SHOT

				return;	
			}

			if(this._ConnectTimeOutTimer == timer)	// is it signal from timeout timer?
			{	// time to try connect to server is up.
				// cleanup socket data
				this._DestroyTransport();
				// wait some time, then try to reconnect
				this._ReConnectTimer.initWithCallback(this, hstReconnectInterval, 0);	// Type: TYPE_ONE_SHOT
				return;	
			}

			this.Log(1, "HearSayTransport::notify: a very wrong state.");	// must be error	
		},

		/*
		 * this function is called when we got data in input stream
		 * 
		 * @param aStream                not used
		 */
		onInputStreamReady: function(/*nsIAsyncInputStream*/ aStream) {
			try {
				var avail_bytes = this._in.available();
				this.Log(4, "avail = " + avail_bytes);
				//this._data += this._in.read(avail_bytes);
				this._data += this._in.readBytes(avail_bytes);

				for(;;) {
					if(this._DataLength==0) {	// // read length
						// log("Really readed: " + this._data.length);
						if(this._data.length<8)
							break;

						this._DataLength = this._data.substr(0, 8);
						this._data 		 = this._data.substr(8);
						// log("Message length detected = " + this._DataLength); 
					}

					if(this._data.length<this._DataLength)
						break;			  				

					var message 	 = decodeURIComponent(escape(this._data.substr(0, this._DataLength)));
					this._data  	 = this._data.substr(this._DataLength);
					this._DataLength = 0;
					this.Log(3, "execute message: \"" + message + "\":" + message.length);
					if(this._OnMsgCallback != null)
						this._OnMsgCallback(message);
				}
				this._InStream.asyncWait(this, 0, 0, hstThreadManager.mainThread);
			}
			catch(e) {
				this.Log(2, "onInputStreamReady exception. finish");
				this.Log(2, e.name);
				this.Log(2, e.message);
				this._OnDisconnect();
			}
		},

		/*
		 * internal use only. this function is called when channel got CONNECTED_TO status
		 * stops timeout timer, setup input stream callback, calls to external callback 
		 */
		_onConnect: function() {
			this._StopConnectTimeoutTimer();
			this._InStream     = this._transport.openInputStream(0, 0, 0);
			this._in	   	   = new BinaryInputStreamComponent();            						

			this._in.setInputStream(this._InStream);
			this._InStream.asyncWait(this, 0, 0, hstThreadManager.mainThread);    			

			if(this._OnConnectCallBack != null)
				this._OnConnectCallBack();		
		},

		/*
		 * internal use only. this function is called when channel disconnected from server
		 * calls to external callback, clear state, setup reconnect timer. 
		 */
		_OnDisconnect: function() {		
			if(this._OnDisconnectCallBack != null)
				this._OnDisconnectCallBack();

			this._StopConnectTimeoutTimer();
			this._DestroyTransport();

			if(this._ReConnectTimer != null)
				this._ReConnectTimer.initWithCallback(this, hstReconnectInterval, 0);	// Type: TYPE_ONE_SHOT			
		},

		/*
		 * nsITransportEventSink interface
		 * callback that monitoring connection state.
		 */	 
		onTransportStatus: function(/*nsITransport*/ aTransport,/*nsresult*/ aStatus,/*unsigned long long*/ aProgress,/*unsigned long long*/ aProgressMax) {
			switch(aStatus)
			{
			case aTransport.STATUS_READING:
				this.Log(4, "onTransportStatus::status: Reading, aProgress=" + aProgress + ", aProgressMax="+aProgressMax);
				break;
			case aTransport.STATUS_WRITING:
				this.Log(4, "onTransportStatus::status: Writing, aProgress=" + aProgress + ", aProgressMax="+aProgressMax);
				break;
			case aTransport.STATUS_RESOLVING:
				this.Log(4, "onTransportStatus::status: Resolving, aProgress=" + aProgress + ", aProgressMax="+aProgressMax);
				break; 			
			case aTransport.STATUS_CONNECTED_TO: 			
				this.Log(4, "onTransportStatus::status: Connected, aProgress=" + aProgress + ", aProgressMax="+aProgressMax);
				this._onConnect();		    			    
				break; 			
			case aTransport.STATUS_SENDING_TO:
				this.Log(4, "onTransportStatus::status: Sending, aProgress=" + aProgress + ", aProgressMax="+aProgressMax);
				break; 			

			case aTransport.STATUS_RECEIVING_FROM:
				this.Log(4, "onTransportStatus::status: Receiving, aProgress=" + aProgress + ", aProgressMax="+aProgressMax);  				
				break;

			case aTransport.STATUS_CONNECTING_TO:
				this.Log(4, "onTransportStatus::status: Connecting, aProgress=" + aProgress + ", aProgressMax="+aProgressMax);
				break; 			
			case aTransport.STATUS_WAITING_FOR:
				this.Log(4, "onTransportStatus::status: Waiting, aProgress=" + aProgress + ", aProgressMax="+aProgressMax);
				break;
			default:
				this.Log(4, "onTransportStatus::status: Unknown, code=" + aStatus);
			}
		},

		/*
		 * Just send message from FireFox to Hearsay
		 * NOTE: format browser messages and hearsay messages are different!
		 * message - just formatted string from Message object (see Message.js)
		 */
		send: function(message)	{
			if(this._OutStream == null)
				this.Log(1, "Error: call hs_transport.js::hstSendMsg() without initialize transport");
			else {
				try
				{
					var strbytes = _hstFormatMessageLength(message);
					this._OutStream.write(strbytes, 8);
					this._OutStream.write(message, message.length);
					this.Log(4, "hs_transport.js::hstSendMsg, msg.length=" + message.length);
					this.Log(5, "hs_transport.js::hstSendMsg, msg=<" + message +">");
				}
				catch(e)
				{
					this.Log(2, "hs_transport.js::hstSendMsg exception, disconnect");
					this._OnDisconnect();
				}
			}		
		}, 

		IsConnected: function() {
			return this._InStream!= null;
		}
}

/** 
 * ---------------------------------------------------------------------------------------------
 * Init tcp stream for connection to HearSay host 
 * parameters are the same, as in HearSayTransport constructor. 
 */


function hstInit(onconnect_callback, ondisconnect_callback, message_callback) {
	if(_hstInstance != null) {
		alert("hstInit() called twice");
	}
	_hstInstance = new HearSayTransport(onconnect_callback, ondisconnect_callback, message_callback);    
}

/** 
 * ---------------------------------------------------------------------------------------------
 * Close connection to HearSay 
 */

function hstDone() {
	if(_hstInstance != null)
	{		
		_hstInstance.Done();
		_hstInstance = null;
	}
}

/** 
 * ---------------------------------------------------------------------------------------------
 * Function to format the length of the message to be sent to HS 
 * @param {Object} mesg the message to be sent
 * moved from SocketComponent.js
 */
function _hstFormatMessageLength(msg) {
	var len_s = msg.length.toString();
	return "00000000".slice(len_s.length)+len_s;
}

/** 
 * ---------------------------------------------------------------------------------------------
 * Function that sends a message to HS 
 * @param {Object} message to be sent
 */
function hstSendMsg(message) {
	if(_hstInstance != null)        
		_hstInstance.send(message);
}

function hstIsConnected()
{
	return _hstInstance != null && _hstInstance.IsConnected(); 
}

log("HsTransport.js loaded");