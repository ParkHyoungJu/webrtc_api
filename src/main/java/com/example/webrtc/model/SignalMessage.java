package com.example.webrtc.model;

import java.io.Serializable;

import com.google.gson.Gson;

public class SignalMessage implements Serializable {

	private static final long serialVersionUID = -7541435379370078682L;

	private String method;
    private String from;
    private String to;
    private String status;
    private String message;
    private String transaction;
    private String sessionId;
    private Object data;

    public SignalMessage() {
    }
    
    public SignalMessage(SignalMessage signalMessage) {
		this.method = signalMessage.getMethod();
		this.from = signalMessage.getFrom();
		this.to = signalMessage.getTo();
		this.status = signalMessage.getStatus();
		this.message = signalMessage.getMessage();
		this.transaction = signalMessage.getTransaction();
		this.sessionId = signalMessage.getSessionId();
		this.data = signalMessage.getData();
    }
    
    public SignalMessage(String method, String from, String to, String status, String message, String transaction, String sessionId, Object data) {
		this.method = method;
		this.from = from;
		this.to = to;
		this.status = status;
		this.message = message;
		this.transaction = transaction;
		this.sessionId = sessionId;
		this.data = data;
	}
    
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.message = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getTransaction() {
		return transaction;
	}
	public void setTransaction(String transaction) {
		this.transaction = transaction;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public String toString() {
        return new Gson().toJson(this);
	}
}
