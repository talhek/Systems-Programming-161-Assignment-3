package Game;

import java.io.IOException;
import java.util.HashMap;

import Server.protocol.*;
import Server.tokenizer.StringMessage;

public class Game {

	private HashMap<String,GameFactory<StringMessage>> GameList;
	//each user saved by is nick
	private HashMap<String,User> UserMap;
	//each room saved by is roomName
	private HashMap<String,Room> RoomMap;

	private HashMap<ProtocolCallback<StringMessage>,String> callBackMap;


	private static class GameHolder {
		private static Game instance = new Game();
	}

	private Game() {
		this.UserMap = new HashMap<String,User>();
		this.RoomMap = new HashMap<String,Room>();
		this.callBackMap = new HashMap<ProtocolCallback<StringMessage>,String>();
		this.GameList = new HashMap<String,GameFactory<StringMessage>>();
		this.GameList.put("BLUFFER",new BlufferGameFactory());

	}
	/**
	 * 
	 * @return and instance for the Game Board.
	 */
	public static Game getInstance(){
		return GameHolder.instance;
	}

	/**
	 * Adding a new User to the game.
	 * @param NickName Nick Name to register
	 * @param callBack callback of the client.
	 */
	public void AddNewUser(String NickName,ProtocolCallback<StringMessage> callBack){
		if(!UserMap.containsKey(NickName)){
			User user = new User(NickName,callBack);
			UserMap.put(NickName,user);
			callBackMap.put(callBack,NickName);
		}
	}

	/**
	 * 
	 * @param callback - client callback
	 * @return - client nick name , null if unregisterd
	 */
	public String getNick(ProtocolCallback<StringMessage> callback){
		if(callBackMap.containsKey(callback)){
			return callBackMap.get(callback);
		}
		return null;
	}

	/**
	 * 
	 * @param cb - client callback
	 * @return - true if the client has registerd
	 */
	public boolean isRegisterd(ProtocolCallback<StringMessage> cb){
		return this.getNick(cb)!=null;
	}

	/**
	 * delete user.
	 * @param NickName - user nick name
	 * @throws IOException
	 */
	private void deleteUser(String NickName) throws IOException{
		if(UserMap.containsKey(NickName)){
			this.leaveRoom(NickName);
			UserMap.remove(NickName);
			callBackMap.remove(NickName);
		}
	}

	/**
	 * create new room
	 * @param RoomName
	 */
	public void CreateRoom(String RoomName){
		Room room = new Room(RoomName);
		RoomMap.put(RoomName,room);
	}

	/**
	 * checks if the user is in room
	 * @param Nick
	 * @return true if the user is in room.
	 */
	public boolean isInRoom(String Nick){
		return UserMap.get(Nick).isInRoom();
	}
	/**
	 * @param roomName 
	 * @return an instance to a room.
	 */
	public Room GetRoomInstance(String roomName){
		return RoomMap.get(roomName);
	}
	/**
	 * join a specific room.
	 * @param RoomName
	 * @param NickName
	 */
	public void joinRoom(String RoomName,String NickName){
		if(RoomMap.containsKey(RoomName)){
			RoomMap.get(RoomName).join(UserMap.get(NickName));
		}
	}

	/** 
	 * checks if a specific nick is taken
	 * @param nick
	 * @return true if the nick is taken
	 */
	public boolean containNick(String nick) {
		return UserMap.containsKey(nick);
	}

	/**
	 * checks if a specific room exist
	 * @param roomName
	 * @return true the specific room exist
	 */
	public boolean containRoom(String roomName) {
		return RoomMap.containsKey(roomName);
	}
	/**
	 * take a specific user out of is room.
	 * @param Nick
	 * @return false if fail (game already started)
	 * @throws IOException
	 */
	public boolean leaveRoom(String Nick) throws IOException {
		if(UserMap.get(Nick).isInRoom()){
			if(!RoomMap.get(UserMap.get(Nick).getRoomName()).isActive()){
				RoomMap.get(UserMap.get(Nick).getRoomName()).Leave(Nick);
				return true;
			}
			else return false;
		}
		return true;
	}
	/**
	 * checks if the game is active
	 * @param roomName
	 * @return true if the game in the specific room is active
	 */
	public boolean isActivate(String roomName) {
		return (RoomMap.get(roomName).isActive());
	}
	/**
	 * send a message to all the players in the room
	 * @param roomName - room to send message to
	 * @param msg - message to send
	 * @param NickName - sender nick name (will not receive a message)
	 * @throws IOException
	 */
	public void sendMsg(String roomName, String msg,String NickName) throws IOException {
		this.RoomMap.get(roomName).sendMsg(msg, NickName);
	}
	/**
	 * return the name of the room the specific user is member of
	 * @param nick 
	 * @return room name
	 */
	public String getRoom(String nick) {
		return UserMap.get(nick).getRoomName();
	}
	/**
	 * start a game.
	 * @param nick 
	 * @param gameType
	 * @throws IOException
	 */
	public void Start(String nick,String gameType) throws IOException {
		String room = getRoom(nick);
		RoomMap.get(room).start(this.GameList.get(gameType));
	}

	/**
	 * handles a text resp
	 * @param msg
	 * @param nick
	 * @param room
	 * @throws IOException
	 */
	public void TxTresp(String msg, String nick, String room) throws IOException {
		this.GetRoomInstance(room).getActiveGame().TxtResp(msg,this.UserMap.get(nick).getCallBack());
	}

	/**
	 * handles a select resp
	 * @param choice
	 * @param nick
	 * @param room
	 * @throws IOException
	 */
	public void selectResp(int choice, String nick, String room) throws IOException {
		this.GetRoomInstance(room).getActiveGame().SelectResp(choice, nick);
	}

	/**
	 * 
	 * @return list of supported game.
	 */
	public String ListOfGames() {
		String tmp="[";
		for(String gameName : this.GameList.keySet())
			tmp+=""+gameName+"], ";
		return tmp.substring(0,tmp.length()-2);
	}
/**
 * return a specific client callback
 * @param nick
 * @return client callback
 */
	public ProtocolCallback<StringMessage> getCallBack(String nick) {
		return UserMap.get(nick).getCallBack();
	}
/**
 * quit from the server.
 * @param nick
 * @throws IOException
 */
	public void quit(String nick) throws IOException {
		this.deleteUser(nick);
	}

	/**
	 * checks if the game is supported
	 * @param gameName
	 * @return true if supported
	 */
	public boolean isSupported(String gameName) {
		return this.GameList.containsKey(gameName);
	}
}
