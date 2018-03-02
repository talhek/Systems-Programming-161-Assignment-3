package Server.protocol;

import java.io.IOException;

import Game.Game;
import Server.tokenizer.StringMessage;


public class GameProtocol implements AsyncServerProtocol<StringMessage> {

	Game game = Game.getInstance();
	private boolean shouldClose = false;
	private ProtocolCallback<StringMessage> cb;

	public GameProtocol(){

	}


	@Override
	public void processMessage(StringMessage message, ProtocolCallback<StringMessage> callback) {
		try {
			cb = callback;
			String msg = message.getMessage();
			if(msg.startsWith("NICK ")){
				String nick = null;
				if(msg.split("NICK ").length>0)
					nick = msg.split("NICK ")[1];
				if(!game.isRegisterd(callback) && nick!=null){
					if(!game.containNick(nick)){
						game.AddNewUser(nick, callback);
						callback.sendMessage(new StringMessage("SYSMSG nick "+nick+" Accepeted!"));
					}
					else{
						callback.sendMessage(new StringMessage("SYSMSG "+nick+" ALREADY TAKEN REJECT"));
					}
				} 
				else
					callback.sendMessage(new StringMessage("SYSMSG NICK REJECT"));
			}
			else if(msg.startsWith("JOIN ")){
				String RoomName = null;
				if(msg.split("JOIN ").length>0)
					RoomName = msg.split("JOIN ")[1];
				else{
					callback.sendMessage(new StringMessage("SYSMSG NOT SUPPORTERD REJECTED"));
					return;
				}
				if(game.isRegisterd(callback)){
					String nick = game.getNick(callback);
					boolean b = game.leaveRoom(nick);
					if(b){
						if(game.containRoom(RoomName)){
							if(!game.isActivate(RoomName)){
								game.joinRoom(RoomName, nick);
								game.sendMsg(RoomName,"GAMEMSG "+nick+" JOIN THE GAME!",nick);
								callback.sendMessage(new StringMessage("SYSMSG JOINED ROOM "+RoomName +" ACCEPTED"));
							}
							else
								callback.sendMessage(new StringMessage("SYSMSG GAME ALREADY STARTED REJECT"));
						}
						else
						{
							game.CreateRoom(RoomName);
							game.joinRoom(RoomName,nick);
							callback.sendMessage(new StringMessage("SYSMSG JOINED ROOM "+RoomName +" ACCEPTED"));
						}
					}
					else
						callback.sendMessage(new StringMessage("SYSMSG GAME ALREADY STARTED REJECT"));
				}
				else
					callback.sendMessage(new StringMessage("SYSMSG PLEASE REGISTER nick NAME FIRST REJECTED"));
			}
			else if(msg.startsWith("MSG ")){
				if(game.isRegisterd(callback) && game.isInRoom(game.getNick(callback))&&msg.split("MSG ")[1]!=null){
					String nick = game.getNick(callback);
					String room = game.getRoom(nick);
					game.sendMsg(room,"USERMSG "+nick+": "+msg.split("MSG ")[1], nick);
				}
				else
					callback.sendMessage(new StringMessage("SYSMSG REJECTED"));
			}
			else if(msg.startsWith("STARTGAME ")){
				if(msg.split("STARTGAME" ).length>0 && (game.isRegisterd(callback) && game.isInRoom(game.getNick(callback))) && !game.isActivate(game.getRoom(game.getNick(callback)))){
					String nick = game.getNick(callback);
					String roomName = game.getRoom(nick);
					String GameName = msg.substring(10);
					if(game.isSupported(GameName)){
						callback.sendMessage(new StringMessage("SYSMSG GAME START ACCEPTED"));
						game.sendMsg(roomName,"GAMEMSG GAME STARTED!", nick);
						game.Start(nick,GameName);
					}
					else 
						callback.sendMessage(new StringMessage("SYSMSG NOT SUPPORTED GAME TYPE"));
				}
				else
					callback.sendMessage(new StringMessage("SYSMSG GAME START REJECT!"));
			}
			else if(msg.startsWith("LISTGAMES")){
				if(game.isRegisterd(callback)&& !(game.isInRoom(game.getNick(callback))&&game.isActivate(game.getRoom(game.getNick(callback)))))
					callback.sendMessage(new StringMessage(game.ListOfGames()+""));
				else
					callback.sendMessage(new StringMessage("SYSMSG REJECTED"));
			}
			else if(msg.startsWith("TXTRESP ")){
				if(msg.split("TXTRESP ").length>0 && game.isRegisterd(callback)&& game.isInRoom(game.getNick(callback))&& game.isActivate(game.getRoom(game.getNick(callback)))){
					String nick = game.getNick(callback);
					String room = game.getRoom(nick);
					game.TxTresp(msg.split("TXTRESP ")[1].toLowerCase(),nick,room);
				}
				else
					callback.sendMessage(new StringMessage("TXTRESP REJECTED"));
			}
			else if(msg.startsWith("SELECTRESP ")){
				if(msg.split("SELECTRESP ").length>0 && game.isRegisterd(callback)&& game.isInRoom(game.getNick(callback))&& game.isActivate(game.getRoom(game.getNick(callback)))){
					String nick = game.getNick(callback);
					String room = game.getRoom(nick);
					int num = -1;
					try{
						num = Integer.parseInt(msg.split("SELECTRESP ")[1]);
					}
					catch(NumberFormatException e){
						callback.sendMessage(new StringMessage("SYSMSG REJECTED "));
						return;
					}
					if(num!=-1)
						game.selectResp(num,nick,room);
				}
				else
					callback.sendMessage(new StringMessage("SELECTRESP REJECTED"));
			}
			else if(isEnd(new StringMessage(msg))){
				if(game.isRegisterd(callback) && game.isInRoom(game.getNick(callback))&&game.isActivate(game.getRoom(game.getNick(callback)))){
					callback.sendMessage(new StringMessage("QUIT REJECTED"));
				}
				else{
					game.quit(game.getNick(callback));
					callback.sendMessage(new StringMessage("SYSMSG QUIT ACCEPTED"));
					this.shouldClose = true;
				}
			}
			else
				callback.sendMessage(new StringMessage("NOT SUPPORTED"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean shouldClose(){
		return this.shouldClose;
	}

	@Override
	public boolean isEnd(StringMessage msg) {
		return msg.getMessage().equals("QUIT");
	}

	public void connectionTerminated(){
		this.shouldClose = true;
		try {
			if(game.isRegisterd(cb)){
				String NickName = game.getNick(cb);
				if(game.isInRoom(game.getNick(cb))){
					game.sendMsg(game.getRoom(NickName),"SYSMSG "+NickName+" has disconnected game finish ", NickName);
					if(game.GetRoomInstance(game.getRoom(NickName)).isActive())
						game.GetRoomInstance(game.getRoom(NickName)).finish();
				}
				game.quit(NickName);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
