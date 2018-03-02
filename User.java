package Game;

import Server.protocol.*;
import Server.tokenizer.StringMessage;

public class User {
	private String nickName;
	private String roomName;
	private boolean isInRoom;
	private ProtocolCallback<StringMessage> callBack;
	
	public User(String nickName , ProtocolCallback<StringMessage> callBack){
		this.nickName = nickName;
		this.callBack = callBack;
	}
	
	public String getNickName() {
		return nickName;
	}
	
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	
	public String getRoomName() {
		return roomName;
	}

	public boolean isInRoom() {
		return isInRoom;
	}
	
	public void joinRoom(String roomName) {
		isInRoom = true;
		this.roomName = roomName;
	}
	
	public void LeaveRoom(){
		isInRoom = false;
		this.roomName = "";
	}
	
	public ProtocolCallback<StringMessage> getCallBack() {
		return callBack;
	}
}
