package com.es.vo.comm;

import java.io.Serializable;
import java.util.Properties;

public class SocketServerVo extends ServerBaseVo implements Serializable {
	private static final long serialVersionUID = 826439429623556631L;
	private int socketPort = -1;
	private String interactive = "";
	private int serverTimeout = -1;
	private int socketTimeout = -1;
	private String timeoutUnit = "";
	private int connections = -1;
	
	public SocketServerVo() {
		// dummy
	}
	
	public SocketServerVo (Properties props) {
    	setServerName(props.getProperty("server_name", "socketServer"));
    	
    	int port = -1;
    	try {
    		port = Integer.parseInt(props.getProperty("port","5444"));
    	}
    	catch (NumberFormatException e) {
    		port = 5444;
    	}
    	setSocketPort(port);
    	
    	setInteractive(props.getProperty("interactive", "no"));
    	
    	int serverTimeout = -1;
    	try {
    		serverTimeout = Integer.parseInt(props.getProperty("server_timeout", "20"));
    	}
    	catch (NumberFormatException e) {
    		serverTimeout = 20;
    	}
    	setServerTimeout(serverTimeout);
    	
    	int socketTimeout = -1;
    	try {
    		socketTimeout = Integer.parseInt(props.getProperty("socket_timeout", "10"));
    	}
    	catch (NumberFormatException e) {
    		socketTimeout = 10;
    	}
    	setSocketTimeout(socketTimeout);
    	
    	setTimeoutUnit(props.getProperty("timeout_unit", "minute"));
    	
    	int connections = -1;
    	try {
    		connections = Integer.parseInt(props.getProperty("connections", "10"));
    	}
    	catch (NumberFormatException e) {
    		connections = 10;
    	}
    	setConnections(connections);
    	
    	setPriority(props.getProperty("priority", "low"));
    	setProcessorName(props.getProperty("processor_name"));
	}
	
	public int getConnections() {
		return connections;
	}
	public final void setConnections(int connections) {
		this.connections = connections;
	}
	public String getInteractive() {
		return interactive;
	}
	public final void setInteractive(String interactive) {
		this.interactive = interactive;
	}
	public int getServerTimeout() {
		return serverTimeout;
	}
	public final void setServerTimeout(int serverTimeout) {
		this.serverTimeout = serverTimeout;
	}
	public int getSocketPort() {
		return socketPort;
	}
	public final void setSocketPort(int socketPort) {
		this.socketPort = socketPort;
	}
	public int getSocketTimeout() {
		return socketTimeout;
	}
	public final void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}
	public String getTimeoutUnit() {
		return timeoutUnit;
	}
	public final void setTimeoutUnit(String timeoutUnit) {
		this.timeoutUnit = timeoutUnit;
	}
}