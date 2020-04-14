package com.example.webrtc.repository;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Repository;
import org.springframework.web.socket.WebSocketSession;
import com.example.webrtc.utils.LogUtils;


@Repository
public class LiveRoomRepository {

	// Socket Session의 devicePid 정보 Map <sessionId, devicePid>
	private final ConcurrentHashMap<String, String> sessionDeviceMap = new ConcurrentHashMap<>();
	// devicePid의 WebSocketSession 정보 Map <devicePid, WebSocketSession>
	private final ConcurrentHashMap<String, WebSocketSession> deviceSessionMap = new ConcurrentHashMap<>();
	
	// Socket Session의 Room(devicePidByRoom) 정보 Map <devicePid, devicePidByRoom>
	private final ConcurrentHashMap<String, String> deviceRoomMap = new ConcurrentHashMap<>();	
	// Room(devicePidByRoom)에 접속한 device 리스트 정보 Map < devicePidByRoom, List<devicePid> >
	private final ConcurrentHashMap<String, CopyOnWriteArrayList<String>> roomDevicessMap = new ConcurrentHashMap<>();	

	public HashMap<String, String> getSessionDeviceMap() {
		return new HashMap<String, String>(sessionDeviceMap);
	}

	public HashMap<String, WebSocketSession> getDeviceSessionMap() {
		return new HashMap<String, WebSocketSession>(deviceSessionMap);
	}

	public HashMap<String, String> getDeviceRoomMap() {
		return new HashMap<String, String>(deviceRoomMap);
	}

	public void connect(String devicePid, WebSocketSession session) {
		if( isConnected(devicePid) ) {
			// devicePid로 접속되어 있다면 기존 Session 은 삭제 한다.
			closeSession(devicePid);
		}
		sessionDeviceMap.put(session.getId(), devicePid);
		deviceSessionMap.put(devicePid, session);
	}
	
	// 사용자 디바이스가 접속 했을때 사용자의 고유 Room인 devicePidByRoom 방을 생성, 기 존재시에는 Room 에 등록 한다.
	public void join(String roomId, String devicePid, WebSocketSession session) {
    	LogUtils.d("join >>>> roomId="+roomId+", devicePid=" + devicePid + ", session=" + session.getId());

		// Room입장, 이미 Room에 접속되어 있다면 삭제 하고 새로운 Room에 입장시킨다.
		if( isJoined(devicePid) ) {
			leaveRoom(devicePid);
		}

		CopyOnWriteArrayList<String> deviceList = roomDevicessMap.get(roomId);
		if (deviceList == null) {
			// 방생성
			deviceList = new CopyOnWriteArrayList<String>();
			
			CopyOnWriteArrayList<String> inMap = roomDevicessMap.putIfAbsent(roomId, deviceList);
			if (inMap != null) deviceList = inMap; // already in map
		}
		deviceList.add(devicePid);
		roomDevicessMap.put(roomId, deviceList);
		deviceRoomMap.put(devicePid, roomId);
    	LogUtils.d("join roomDevicessMap >>>> " + roomDevicessMap);
    	LogUtils.d("join deviceRoomMap >>>> " + deviceRoomMap);
	}

	
	// 디바이스가 WebSocket에 접속되어 있는지 확인
	public boolean isConnected(String device) {
    	LogUtils.d("isConnected >>>> device="+device+" is " + deviceSessionMap.containsKey(device));
		return deviceSessionMap.containsKey(device);
	}
	
	// 디바이스가 Room에 접속되어 있는지 확인
	public boolean isJoined(String devicePid) {
    	LogUtils.d("isJoined >>>> devicePid="+devicePid+" is " + deviceRoomMap.containsKey(devicePid));
		return deviceRoomMap.containsKey(devicePid);
	}

	// Room 이 있는지 확인
	public boolean isExistRoom(String room) {
    	LogUtils.d("isExistRoom >>>> room="+room+" is " + roomDevicessMap.containsKey(room));
		return roomDevicessMap.containsKey(room);
	}

	// 디바이스를 Room에서 제거
	public void leaveRoom(String devicePid) {
		String room = deviceRoomMap.get(devicePid);
		leaveRoom(room, devicePid);
	}

	public void leaveRoom(String room, String devicePid) {
    	LogUtils.d("leaveRoom >>>> room="+room+", devicePid=" + devicePid);
		
		if( room == null || devicePid == null ) return;

		if( deviceRoomMap != null ) deviceRoomMap.remove(devicePid);
		if( roomDevicessMap != null && isExistRoom(room) ) {
	    	LogUtils.d("roomDevicessMap["+room+"] is not null");

	    	CopyOnWriteArrayList<String> deviceList = roomDevicessMap.get(room);
			int i = deviceList.indexOf(devicePid);
	    	LogUtils.d("sessionList " + i + "/" + deviceList.size());

	    	if( i >= 0 ) deviceList.remove(i);

	    	LogUtils.d("sessionList removed. sessionList size = "+deviceList.size() );
	    	
	    	roomDevicessMap.remove(room);
	    	
			if( deviceList.size() > 0 ) {
				roomDevicessMap.put(room, deviceList);
		    	LogUtils.d("roomDevicessMap put room = " + room + ", new size = " + deviceList.size());
			}
		}
	}
	
	public String getSessionId(String devicePid) {
		if( deviceSessionMap.containsKey(devicePid) ) return deviceSessionMap.get(devicePid).getId();
		else return null;
	}
	
	public WebSocketSession getSession(String devicePid) {
		if( deviceSessionMap.containsKey(devicePid) ) return deviceSessionMap.get(devicePid);
		else return null;
	}
	
	public String getDevicePid(WebSocketSession session) {
		if( sessionDeviceMap.containsKey(session.getId()) ) return sessionDeviceMap.get(session.getId());
		else return null;
	}
	
	public String getDevicePid(String session) {
		if( sessionDeviceMap.containsKey(session) ) return sessionDeviceMap.get(session);
		else return null;
	}
	
	public String getRoom(String devicePid) {
		if( deviceRoomMap.containsKey(devicePid) ) return deviceRoomMap.get(devicePid);
		else return null;
	}

	public List<String> getDeviceList(String room) {
		if(room == null) return null;
		
		if( roomDevicessMap.containsKey(room) ) return roomDevicessMap.get(room);
		else return null;
	}

	public List<String> getDeviceListByDevicePid(String devicePid) {
		String room = getRoom(devicePid);
		return getDeviceList(room);
	}
	
	public void closeSession(String devicePid) {
		WebSocketSession oldSession = deviceSessionMap.get(devicePid);
		try {
			if( oldSession != null ) {
				if( sessionDeviceMap != null ) sessionDeviceMap.remove(oldSession.getId());
				if( deviceSessionMap != null ) deviceSessionMap.remove(devicePid);
				oldSession.close();
			}

		} catch (Exception e) {
			LogUtils.d("WebSocket Close Exception :: devicePid = " + devicePid);
			e.printStackTrace();
		}
	}
	
	public HashMap<String, List<String>> getRooms() {
		HashMap<String, List<String>> rooms = new HashMap<>(roomDevicessMap);
        return rooms;
	}

	@Override
	public String toString() {
		StringBuffer retVal = new StringBuffer();
    	retVal.append("{").append("\n")
		    	.append("sessionDeviceMap = ").append(this.sessionDeviceMap.toString()).append("\n")
		    	.append("deviceSessionMap = ").append(this.deviceSessionMap.toString()).append("\n")
		    	.append("deviceRoomMap = ").append(this.deviceRoomMap.toString()).append("\n")
		    	.append("roomDevicessMap = ").append(this.roomDevicessMap.toString()).append("\n")
		    	.append("}");

        return retVal.toString();

        // return new Gson().toJson(this);
	}
}
