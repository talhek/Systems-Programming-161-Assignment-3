package Game;

import java.io.IOException;
import java.util.HashMap;

import Server.protocol.*;
import Server.tokenizer.StringMessage;
/**
 * this class represent a game room
 * @author razbenya
 *
 */
public class Room {
	private String Name;
	private boolean isActive;
	private HashMap<String,User> Users;
	private HashMap<String,Integer> Score;
	private tbgp<StringMessage> game;
	
	/**
	 *  Initialize a game room
	 * @param roomName - name of the room
	 */
	public Room(String roomName) {
		this.Name = roomName;
		this.isActive = false;
		Users = new HashMap<String,User>();
		Score = new HashMap<String,Integer>();
	}
/**
 * 
 * @return name of the room
 */
	public String getName() {
		return Name;
	}
	/**
	 * 
	 * @return the current Active Game in the room
	 */
	public ActiveGame<StringMessage> getActiveGame(){
		return game.getCurrentGame();
	}
/**
 * add a user to the room
 * @param user
 */
	public void join(User user){
		user.joinRoom(this.Name);
		Score.put(user.getNickName(), 0);
		Users.put(user.getNickName(), user);
	}
	/**
	 * Start A new Game
	 * @param game 
	 * @throws IOException
	 */
	public void start(GameFactory<StringMessage> game) throws IOException{
		this.isActive = true;
			this.game = game.create(this);
			this.game.start();	
	}
	
	/**
	 * leave the game
	 * @param NickName
	 * @throws IOException
	 */
	public void Leave(String NickName) throws IOException{
		sendMsg("GAMEMSG "+NickName+" LEAVE THE GAME ",NickName);
		Users.get(NickName).LeaveRoom();
		Users.remove(NickName);
	}
	
	/**
	 *  send message to all the game members
	 * @param msg - message to send
	 * @param NickName - the nick name of the sender (this player will not receive the message
	 * @throws IOException
	 */
	public void sendMsg(String msg,String NickName) throws IOException{
		for(String user : Users.keySet()){
			if(!user.equals(NickName)){
				Users.get(user).getCallBack().sendMessage(new StringMessage(msg));
			}
		}
	}
/**
 * 
 * @return true if the game is active
 */
	public boolean isActive() {
		return this.isActive;
	}
/**
 * 
 * @return amount of players in the room
 */
	public int getAamountOfPlayers() {
		return Users.size();
	}
	
	/**
	 * add score to a player
	 * @param nick - nick name of the player
	 * @param score - amount of score to add
	 */
	public void addScore(String nick, int score) {
		this.Score.put(nick,Score.get(nick)+score);
	}
/**
 * 
 * @param nick
 * @return return the call back of the specific nick 
 */
	public ProtocolCallback<StringMessage> getCallBack(String nick) {
		return Users.get(nick).getCallBack();
	}
/**
 * 
 * @return score summary
 */
	public String Summary(){
		String tmp="";
		for(String nick : Score.keySet()){
			tmp+=nick+": "+Score.get(nick)+"pts ";
		}
		return tmp;
	}
/**
 * finish a game.
 */
	public synchronized void finish() {
		this.isActive = false;
		for(String tmp : Score.keySet()){
			Score.put(tmp,0);
		}
		game = null;
	}
}
