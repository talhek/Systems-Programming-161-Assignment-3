package Game;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import Server.protocol.ProtocolCallback;
import Server.tokenizer.*;
/**
 * an initialize one question in an active bluffer game.
 *
 */
public class ActiveBlufferGame implements ActiveGame<StringMessage>{

	private String question;
	private String answer;
	private int amount;
	private int choosedAnswerd;
	private Map<String,ProtocolCallback<StringMessage>> FakeAnswerMap;
	private Map<String,Integer> Answerd;
	private List<String> AnswersList;
	private Room room;
	private BlufferGame Bluffer;
	private int lvl = 0;

	/**
	 * 
	 * @param bluffer - the current BlufferGame
	 * @param question - question to ask 
	 * @param room - host room
	 */
	public ActiveBlufferGame(tbgp<StringMessage> bluffer,Question question,Room room){
		this.question = question.getQuestion();
		this.Bluffer = (BlufferGame) bluffer;
		this.room = room;
		this.answer = question.getAnswer();
		this.choosedAnswerd=room.getAamountOfPlayers();
		this.amount = room.getAamountOfPlayers();
		this.Answerd = new ConcurrentHashMap<String,Integer>();
		this.FakeAnswerMap = new ConcurrentHashMap<String,ProtocolCallback<StringMessage>>();
		this.AnswersList = new LinkedList<String>();
	}

	public synchronized void TxtResp(String Answer,ProtocolCallback<StringMessage> cb) throws IOException {
		if(amount>0&&!FakeAnswerMap.containsValue(cb) && lvl == 0){
			cb.sendMessage(new StringMessage("SYSMSG TXTRESP ACCEPTED"));
			FakeAnswerMap.put(Answer,cb);
			AnswersList.add(Answer);
			amount --;
			if(amount==0){
				this.SendsChoice();
			}
		}
		else
			cb.sendMessage(new StringMessage("SYSMSG TXTRESP REJECTED"));
	}
	/**
	 * randomize the answers and present them to the players
	 * @throws IOException
	 */
	private synchronized void SendsChoice() throws IOException {
		int randomIndex = (int)((AnswersList.size()+1)*Math.random());
		AnswersList.add(randomIndex, this.answer);
		String tmp = "";
		int i=0;
		for(String ans : AnswersList){
			tmp+=i+"."+ans+" ";
			i++;
		}
		lvl++;
		room.sendMsg("ASKCHOICES "+tmp,"");		
	}
	/**
	 * 
	 * @param num the index of the chosen answer 
	 * @return true if the answer is correct 
	 */
	public boolean isCorrect(int num){
		return AnswersList.get(num).equals(this.answer);
	}

	/**
	 * @return the current question
	 */
	public String getMessage() {
		return question;
	}

	public synchronized void SelectResp(int choice,String nick) throws IOException {
		if(!Answerd.containsKey(nick) && (choice<=room.getAamountOfPlayers() && choice>=0) && lvl==1){
			Answerd.put(nick,choice);
			choosedAnswerd--;
			Game.getInstance().getCallBack(nick).sendMessage(new StringMessage("SYSMSG SELECTRESP ACCEPTED"));
			if(choosedAnswerd == 0){
				for(String nickName : Answerd.keySet() ){
					if(isCorrect(Answerd.get(nickName))){
						room.addScore(nickName,10);
						Game.getInstance().getCallBack(nickName).sendMessage(new StringMessage("GAMEMSG Correct! +10 point"));
					}
					else{
						room.addScore(Game.getInstance().getNick(FakeAnswerMap.get(AnswersList.get(Answerd.get(nickName)))),5);
						Game.getInstance().getCallBack(nickName).sendMessage(new StringMessage("GAMEMSG WORNG! +0pts"));
						Game.getInstance().getCallBack(nickName).sendMessage(new StringMessage("the correct answer is: "+this.answer));
						FakeAnswerMap.get(AnswersList.get(Answerd.get(nickName))).sendMessage(new StringMessage("GAMEMSG "+nickName+" Choose you fake answer +5pt"));
					}
				}
				if(!Bluffer.isFinish()){
					Answerd.clear();
					lvl = 0;
					Bluffer.next();	
				}else {
					room.sendMsg("SYSMSG Summery: "+room.Summary(),"");
					room.sendMsg("GAMEMSG Game Finished","");
					room.finish();
				}
			}
		}
		else
			Game.getInstance().getCallBack(nick).sendMessage(new StringMessage("SYSMSG SELECTRESP REJECTED"));
	}	

}
