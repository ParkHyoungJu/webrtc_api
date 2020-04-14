package com.example.webrtc.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.example.webrtc.model.SignalMessage;
import com.example.webrtc.repository.LiveRoomRepository;
import com.example.webrtc.utils.LogUtils;
import com.google.gson.Gson;

public class SignalingSocketHandler extends TextWebSocketHandler {
  
	
	private static final String ROOM_PREFIX				= "room-";
    private static final String REQUEST_TYPE_JOIN		= "join";
    private static final String REQUEST_TYPE_JOIN_DATA	= "join.data";
    private static final String REQUEST_TYPE_JOIN_WEBRTC= "auth.webrtc";	// PC 접근인 경우
    private static final String REQUEST_TYPE_REGISTER	= "register";
    // private static final String REQUEST_TYPE_WEBRTC		= "webrtc";
	private static final String REQUEST_TYPE_KEEPALIVE = "keep.alive";
	private static final String REQUEST_TYPE_ROOM_MEMBER = "room.member";
    
    private static final String RESULT_TYPE_CREATED		= "created";
    private static final String RESULT_TYPE_REGISTER	= "register";
    private static final String RESULT_TYPE_JOINED		= "joined";
    private static final String RESULT_TYPE_JOINED_DATA= "joined.data";
    // private static final String RESULT_TYPE_WEBRTC		= "webrtc";
    private static final String RESULT_TYPE_CLOSED		= "closed";
	private static final String RESULT_TYPE_KEEPALIVE = "keep.alive";
	private static final String RESULT_TYPE_ROOM_MEMBER = "room.member";
    
    private static final String RESULT_STATUS_SUCC			= "200";
    private static final String RESULT_STATUS_UNAUTHORIZED	= "401";	// 인증(권한) 오류

    private static LiveRoomRepository roomRepository = new LiveRoomRepository();
    
    /*
    // This map saves sockets by usernames
    private static Map<String, WebSocketSession> clients = new HashMap<String, WebSocketSession>();
    // Thus map saves username by socket ID
    private static Map<String, String> clientIds = new HashMap<String, String>();
	*/
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    	
    	String devicePid = (String) session.getAttributes().get("devicePid");
    	String devicePidByRoom = (String) session.getAttributes().get("devicePidByRoom");
    	LogUtils.d("afterConnectionEstablished["+devicePidByRoom+"]/["+devicePid+"] : " + session.getId() + " 접속 IP :" + session.getRemoteAddress().getHostName());
    	LogUtils.d("afterConnectionEstablished["+devicePidByRoom+"]/["+devicePid+"] : " + session.getId() + " is open -> " + session.isOpen());
    	
    	roomRepository.connect(devicePid, session);
    	
    	// 접속한 디바이스에 접속 결과를 보낸다.
    	SignalMessage resMessage = new SignalMessage(RESULT_TYPE_CREATED, devicePid, devicePid, RESULT_STATUS_SUCC, null, null, session.getId(), null);
        broadcast(devicePid, devicePid, resMessage);
        
    	// 접속 정보 저장
        List<String> deviceListByRoom = roomRepository.getDeviceList(devicePidByRoom);
        int camCcuCount = 0;
        if( deviceListByRoom != null ) camCcuCount = deviceListByRoom.size();
        
//        memberHistoryService.saveJoined(devicePid, devicePidByRoom, camCcuCount, session.getRemoteAddress().getHostName(), session.getId());
    }

   
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

    	String devicePid = (String) session.getAttributes().get("devicePid");
    	String devicePidByRoom = (String) session.getAttributes().get("devicePidByRoom");

    	String wsSessionId = session.getId();
    	String ipAddress = session.getRemoteAddress().getHostName();
    	
    	LogUtils.d("afterConnectionClosed["+devicePidByRoom+"]/["+devicePid+"] : " + session.getId() + " 접속종료 IP :" + session.getRemoteAddress() + " isOpen -> " + session.isOpen());

    	// 같은 Room에 있는 사용자들에게 접속 종료를 알린다.
    	SignalMessage resMessage = new SignalMessage(RESULT_TYPE_CLOSED, devicePid, null, RESULT_STATUS_SUCC, null, null, session.getId(), null);
        broadcast(devicePid, null, resMessage);

        // 접속 종료된 디바이스를 Room에서 제거
        // LogUtils.d("roomRepository Before :" + roomRepository.toString());
    	roomRepository.leaveRoom(devicePid);
    	roomRepository.closeSession(devicePid);
    	LogUtils.d("roomRepository After :" + roomRepository.getRooms());
        
//    	SignalMessage resMessage = new SignalMessage(RESULT_TYPE_CLOSED, devicePid, null, RESULT_STATUS_SUCC, null, null, session.getId(), null);
//    	redisPublisher.publish(resMessage);

    	// 접속 종료 정보 저장
        List<String> deviceListByRoom = roomRepository.getDeviceList(devicePidByRoom);
        int camCcuCount = 0;
        if( deviceListByRoom != null ) camCcuCount = deviceListByRoom.size();
        
//        memberHistoryService.saveClosed(devicePid, devicePidByRoom, camCcuCount, ipAddress, wsSessionId);

    }
    
    // Handle an error from the underlying WebSocket message transport.
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // LogUtils.d("handleTransportError = " + exception );

    	// session 으로 devicePid를 구해서 Room에 있다면 close 시킨다
    	String devicePidByRoom = (String) session.getAttributes().get("devicePidByRoom");
    	String devicePid = (String) session.getAttributes().get("devicePid");
    	
    	LogUtils.d("handleTransportError["+devicePidByRoom+"]/["+devicePid+"] : " + session.getId() + " 접속종료 IP :" + session.getRemoteAddress().getHostName() + " isOpen -> " + session.isOpen());

    	String devicePidBySession = roomRepository.getDevicePid(session);

    	if( devicePidBySession != null ) {
        	session.close(CloseStatus.SERVER_ERROR);
    	}
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    	// LogUtils.d("handleTextMessage : " + message.getPayload());

    	String devicePidByRoom = (String) session.getAttributes().get("devicePidByRoom");
    	String devicePid = (String) session.getAttributes().get("devicePid");

    	Gson gson = new Gson();
        SignalMessage signalMessage = gson.fromJson(message.getPayload(), SignalMessage.class);
        LogUtils.d("signalingMessage("+signalMessage.getMethod()+") from "+signalMessage.getFrom()+" to "+signalMessage.getTo());

    	if( ! devicePid.equals(signalMessage.getFrom()) ) {
    		// 데이터를 보낸 사용자와 메시지의 From 사용자가 다른 경우 경고 메시지를 보낸다.
        	LogUtils.d("handleTextMessage["+session.getId()+"]["+devicePid+":"+signalMessage.getFrom()+"] : " + message.getPayload());
        	SignalMessage resMessage = new SignalMessage(signalMessage);
        	resMessage.setStatus(RESULT_STATUS_UNAUTHORIZED);

        	session.sendMessage(new TextMessage(resMessage.toString()));
    		return;
    	}
        
        if (REQUEST_TYPE_JOIN.equalsIgnoreCase(signalMessage.getMethod()) || 
        		REQUEST_TYPE_REGISTER.equalsIgnoreCase(signalMessage.getMethod()) ) {

        	roomRepository.join(devicePidByRoom, devicePid, session);
        	LogUtils.d("roomRepository join :" + roomRepository.toString());

        	SignalMessage resMessage = new SignalMessage(RESULT_TYPE_JOINED, devicePid, null, RESULT_STATUS_SUCC, null, signalMessage.getTransaction(), session.getId(), null);
            broadcast(devicePid, null, resMessage);
            

        } else if (REQUEST_TYPE_JOIN_WEBRTC.equalsIgnoreCase(signalMessage.getMethod())) {

            // PC에서 Join했을 경우, sessionId 에 로그인후 받은 승인토큰 을 담는다.
        	String authToken = signalMessage.getSessionId();
        	// String devicePid = signalMessage.getFrom();
        	String camDevicePid = signalMessage.getTo();

        	LogUtils.d("REQUEST_TYPE_JOIN_WEBRTC::authToken="+authToken+", camDevicePid"+camDevicePid);
        	if(authToken == null) {
            	SignalMessage resMessage = new SignalMessage(signalMessage);
            	resMessage.setStatus(RESULT_STATUS_UNAUTHORIZED);

            	session.sendMessage(new TextMessage(resMessage.toString()));

        		return;
        	}
        	
        	// 승인토큰 확인
//        	HashMap<String, Object> memDevice = memberDeviceService.findDeviceByPidNToken(devicePid, authToken);
//        	if( memDevice == null ) {
//            	SignalMessage resMessage = new SignalMessage(signalMessage);
//            	resMessage.setStatus(RESULT_STATUS_UNAUTHORIZED);
//
//            	session.sendMessage(new TextMessage(resMessage.toString()));
//
//        		return;
//        	}
//        	LogUtils.d("REQUEST_TYPE_JOIN_WEBRTC::memDevice="+memDevice);
        	
        	// 접근 가능한 카메라(ROOM)인지 확인
//        	String memPid = HMapUtils.getString(memDevice, "memPid", "0");
//        	List<HashMap<String, Object>> accessDevices = memberDeviceService.findAccessCamByMemPid(memPid, camDevicePid);
//        	if( accessDevices == null || accessDevices.size() <= 0 ) {
//            	SignalMessage resMessage = new SignalMessage(signalMessage);
//            	resMessage.setStatus(RESULT_STATUS_UNAUTHORIZED);
//
//            	session.sendMessage(new TextMessage(resMessage.toString()));
//
//        		return;
//        	}
//        	LogUtils.d("REQUEST_TYPE_JOIN_WEBRTC::accessDevices="+accessDevices.size());

        	roomRepository.join(devicePidByRoom, devicePid, session);
        	LogUtils.d("roomRepository join :" + roomRepository.toString());

        	SignalMessage resMessage = new SignalMessage(RESULT_TYPE_JOINED, devicePid, null, RESULT_STATUS_SUCC, null, signalMessage.getTransaction(), session.getId(), null);
            broadcast(devicePid, null, resMessage);
        	
        	
        } else if(REQUEST_TYPE_JOIN_DATA.equalsIgnoreCase(signalMessage.getMethod()) ) {
        	// 데이터채널용 WebRTC 접속요청
        	roomRepository.join(devicePidByRoom, devicePid, session);
        	LogUtils.d("roomRepository join.data :" + roomRepository.toString());

        	SignalMessage resMessage = new SignalMessage(RESULT_TYPE_JOINED_DATA, devicePid, null, RESULT_STATUS_SUCC, null, signalMessage.getTransaction(), session.getId(), null);
            broadcast(devicePid, null, resMessage);
            

        } else if(REQUEST_TYPE_ROOM_MEMBER.equalsIgnoreCase(signalMessage.getMethod()) ) {
        	// 룸 접속자 정보 조회 Redis Sub에서 처리하지 않고 직접 처리 한다.
        	List<String> devices = getRooms(devicePid);
   	 		
        	List<HashMap<String, Object>> devicesInfo = new ArrayList<HashMap<String, Object>>();
        	LogUtils.d("devicesInfo :" + (devicesInfo==null?"devicesInfo is NULL":devicesInfo));

//        	if(devices != null && devices.size()>0 ) {
//   				LogUtils.d("memberDeviceService " + (memberDeviceService==null?"is NULL":"is NOT NULL"));
//   				
//   				devicesInfo = memberDeviceService.findDevicesInRoom(devices);
//   				if( devicesInfo == null ) devicesInfo = new ArrayList<HashMap<String, Object>>();
//   			}

//        	String resData = new Gson().toJson(devicesInfo);
//   	     	LogUtils.d("devicesInfo in room :" + resData);

        	SignalMessage resMessage = new SignalMessage(RESULT_TYPE_ROOM_MEMBER, devicePid, null, RESULT_STATUS_SUCC, null, signalMessage.getTransaction(), signalMessage.getSessionId(), null);
        	broadcast(devicePid, devicePid, resMessage);

        	return;
        	
        } else {

        	SignalMessage resMessage = new SignalMessage(signalMessage);
        	resMessage.setStatus(RESULT_STATUS_SUCC);
            broadcast(devicePid, signalMessage.getTo(), resMessage);
            
            
        }

        // redisPublisher.publish(message.getPayload());
//    	redisPublisher.publish(signalMessage);


    }

    
    
    
    
    
    // 결과 메시지 전달
    public static void broadcastAll(String fromDevicePid, SignalMessage signalMessage) throws Exception {
    	LogUtils.d("send message All [ "+fromDevicePid+" ] ==> " + signalMessage.getMethod() );
    	HashMap<String, WebSocketSession> deviceSessionMap = roomRepository.getDeviceSessionMap();
    	
    	Iterator<String> keys = deviceSessionMap.keySet().iterator();
    	 while( keys.hasNext() ){
             String toDevicePid = keys.next();

             broadcast(fromDevicePid, toDevicePid, signalMessage.toString());
         }
    }
    

    public static void broadcast(String fromDevicePid, String toDevicePid, SignalMessage signalMessage) throws Exception {
    	LogUtils.d("send message [ "+fromDevicePid+"==> "+toDevicePid+" ] " + signalMessage.getMethod() );
    	broadcast(fromDevicePid, toDevicePid, signalMessage.toString());
    }
    
    public static void broadcast(String fromDevicePid, String toDevicePid, String jsonMessage) throws Exception {
    	// LogUtils.d("send message [ "+fromDevicePid+"==> "+toDevicePid+" ] " + jsonMessage );

    	if( toDevicePid != null ) {
    		// 전달대상(toDevicePid)에만 메시지 전달
    		// LogUtils.d("send message(1:1) [ "+fromDevicePid+"==> "+toDevicePid+" ]" + jsonMessage );
			sendResult(toDevicePid, jsonMessage);
    		
    	} else {

    		// Room에 접속된 전체에 메시지 전달
    		List<String> roomDeviceList = roomRepository.getDeviceListByDevicePid(fromDevicePid);
    		if( roomDeviceList != null ) {
    			
    			for( int i=0; i<roomDeviceList.size(); i++ ) {
    				String toDevicePidByRoom = roomDeviceList.get(i);

    				if( ! fromDevicePid.equals(toDevicePidByRoom) ) {
    					// LogUtils.d("send message(1:N) [ "+fromDevicePid+"==> "+toDevicePidByRoom+" ]" + jsonMessage );
    					sendResult(toDevicePidByRoom, jsonMessage);
    				}
    				
    			}
    			
    		}
    		
    		
    	}
    }
    
    
    public static void sendResult(String devicePid, String jsonMessage) {
    	WebSocketSession toSession = roomRepository.getSession(devicePid);
    	LogUtils.d("sendResult toDevicePid = "+ devicePid );
    	
    	if( toSession != null ) {
    		sendResult(toSession, jsonMessage);
    	} else {
    		LogUtils.d("sendResult toDevicePid["+ devicePid + "]'s session is null." );
    		// session이 null 인 경우 roomRepository에서 삭제 한다.
    		roomRepository.leaveRoom(devicePid);
    		roomRepository.closeSession(devicePid);
    	}
    }
    
    private static void sendResult(WebSocketSession toSession, String jsonMessage) {
    	TextMessage textMessage = new TextMessage(jsonMessage);
    	
    	try {
			synchronized (toSession) {	// 여러 쓰레드에서 이 메소드에 접근하기 때문에 rock을 건다
            	LogUtils.d("sendResult session isOpen = " + toSession.isOpen() );
            	if( toSession.isOpen() ) {
                	// LogUtils.d("send message [ "+toSession.getId() +"] "+ jsonMessage );
            		toSession.sendMessage(textMessage);
            	} else {
            		// session이 열려있지 않으면 을 종료 시킨다.
            	}
			}
			
		} catch (IllegalStateException ise) {
			// 특정 클라이언트에게 현재 메시지 보내기 작업 중인 경우에 동시에 쓰기작업을 요청하면 오류 발생함
			if (ise.getMessage().indexOf("[TEXT_FULL_WRITING]") != -1) {
				new Thread() {
					@Override
					public void run() {
						int retryCount = 0;
						// 같은 에러가 발생하면 반복문을 통해서 다시 메세지를 전달하게 한다.
						while (true) {
							try {
								retryCount++;
								LogUtils.d("sendObject retry["+retryCount+"][TEXT_FULL_WRITING] = " + textMessage);

								// driverSession.getBasicRemote().sendObject(message);
								toSession.sendMessage(textMessage);
								
								break;
							} catch (IllegalStateException _ise) {
								LogUtils.e("sendObject retry["+retryCount+"][TEXT_FULL_WRITING] = " + textMessage);
								if( retryCount >= 10 ) {
									break;
								}
								try {
									Thread.sleep(100); // 메시지 보내기 작업을 마치도록 기다려준다
								} catch (InterruptedException e) {
								}
							} catch (Exception e) {
								e.printStackTrace();
								break;
							}
						}
					}
				}.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    
    // 룸에 접속 상태 확인(true;접속중, false:미접속)
    public static boolean isJoined(String devicePid) {
        LogUtils.d("isJoined devicePid("+devicePid+") is " + roomRepository.isJoined(devicePid));
		return roomRepository.isJoined(devicePid);
   }
    
    // 웹소켓에 접속 상태 확인(true;접속중, false:미접속)
    public static boolean isConnected(String devicePid) {
        LogUtils.d("isConnected devicePid("+devicePid+") is " + roomRepository.isConnected(devicePid));
    	return roomRepository.isConnected(devicePid);
    }
    
    // 웹소켓에 접속 상태 확인(true;접속중, false:미접속)
    public static HashMap<String, List<String>> kickedOut(String devicePid) {
        LogUtils.d("kickedOut devicePid = " + devicePid);
    	roomRepository.leaveRoom(devicePid);
    	roomRepository.closeSession(devicePid);

    	return getRooms();
    }
    
    // 웹소켓에 접속된 Client 조회
    public static HashMap<String, List<String>> getRooms() {
    	// LogUtils.d("roomRepository = " + roomRepository);

        return roomRepository.getRooms();
    }
    
    // 같은 방에 접속된 Client 조회
    public static List<String> getRooms(String devicePid) {
    	// LogUtils.d("roomRepository = " + roomRepository);
    	
    	return roomRepository.getDeviceListByDevicePid(devicePid);
    }
    

    
    
    
//   	@Override
//    public void onMessage(Message message, byte[] pattern) {
//    	// LogUtils.d("Radis Message received : " + message.toString() );
//   		try {
//   	    	Gson gson = new Gson();
//   	        SignalMessage signalMessage = gson.fromJson(message.toString(), SignalMessage.class);
//   	        
//   	    	// LogUtils.d("Radis Message received : " + signalMessage.toString() );
//   	    	
//   	    	handleReceivedMessage(signalMessage);
//   	    	
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//   	
//    }


   	// Redis Message 처리 프로세스
   	// Signal 서버에 접속된 Client에 메시지 전송
   	private void handleReceivedMessage( SignalMessage signalMessage ) {
   		
   		String devicePid = signalMessage.getFrom();
   		try {
   	   		if (REQUEST_TYPE_JOIN.equalsIgnoreCase(signalMessage.getMethod()) || 
   	        		REQUEST_TYPE_REGISTER.equalsIgnoreCase(signalMessage.getMethod()) ) {

   	        	SignalMessage resMessage = new SignalMessage(RESULT_TYPE_JOINED, devicePid, null, RESULT_STATUS_SUCC, null, signalMessage.getTransaction(), signalMessage.getSessionId(), null);
   	            broadcast(devicePid, null, resMessage);

   	        } else if (REQUEST_TYPE_JOIN_WEBRTC.equalsIgnoreCase(signalMessage.getMethod())) {

   	        	SignalMessage resMessage = new SignalMessage(RESULT_TYPE_JOINED, devicePid, null, RESULT_STATUS_SUCC, null, signalMessage.getTransaction(), signalMessage.getSessionId(), null);
   	            broadcast(devicePid, null, resMessage);
   	        	
   	        	
   	        } else if(REQUEST_TYPE_JOIN_DATA.equalsIgnoreCase(signalMessage.getMethod()) ) {

   	        	SignalMessage resMessage = new SignalMessage(RESULT_TYPE_JOINED_DATA, devicePid, null, RESULT_STATUS_SUCC, null, signalMessage.getTransaction(), signalMessage.getSessionId(), null);
   	            broadcast(devicePid, null, resMessage);
   	        	
   	        } else if(RESULT_TYPE_CLOSED.equalsIgnoreCase(signalMessage.getMethod()) ) {
   	        	
   	        	SignalMessage resMessage = new SignalMessage(RESULT_TYPE_CLOSED, devicePid, null, RESULT_STATUS_SUCC, null, signalMessage.getTransaction(), signalMessage.getSessionId(), null);
   	        	broadcast(devicePid, null, resMessage);
   	        	
   	        	// 접속 종료된 디바이스를 Room에서 제거
	   	        // LogUtils.d("roomRepository Before :" + roomRepository.toString());
	   	     	roomRepository.leaveRoom(devicePid);
	   	     	roomRepository.closeSession(devicePid);
	   	     	LogUtils.d("roomRepository After :" + roomRepository.getRooms());

   	        } else {

   	        	// By pass(1:1)
  	        	String toDevicePid = signalMessage.getTo();
   	        	
  	        	// WebSocketSession toSession = roomRepository.getSession(toDevicePid);

   	        	// if( toSession != null ) {
   	   	        	SignalMessage resMessage = new SignalMessage(signalMessage);
   	   	        	resMessage.setStatus(RESULT_STATUS_SUCC);
   	   	            broadcast(devicePid, toDevicePid, resMessage);
   	        	// } 
   	        }
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

   	}
   	
   	
}