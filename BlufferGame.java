package Game;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.google.gson.Gson;

import GSON.Input;
import GSON.question;
import Server.tokenizer.StringMessage;


public class BlufferGame implements tbgp<StringMessage>{

	private Map<Integer,Question> ThreeQuestions;
	private Map<Integer,Question> FullQuestionDataBase;
	private ActiveGame<StringMessage> current;
	private Room HostRoom;
	private int numOfQuestions = 3;
	private boolean finish=false;
	
/**
 * read questions Json File
 * @throws IOException
 */
	private void ReadFromJson() throws IOException{
		Gson gson = new Gson();
		Input input = null;
		try {
			Logger.getLogger("edu.spl.reactor").info("Reading JSON from a file");
			FileReader file = new FileReader(System.getProperty("user.dir") + "/bluffer.json" );
			BufferedReader br = new BufferedReader(file);
			input = gson.fromJson(br, Input.class);
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		question[] q = input.getQuestion();
		for(int i=0;i<q.length;i++){
			FullQuestionDataBase.put(i,new Question(q[i].getQuestionText(),q[i].getRealAnswer()));
		}
		if(q.length<3){
			HostRoom.finish();
			HostRoom.sendMsg("SYSMSG ERROR NO QUESTIONS FOUND","");
		}
	}
	/**
	 * choose 3 random questions from the questions data base.
	 */
	private void ThreeRandomQuestion(){
		ThreeQuestions = new ConcurrentHashMap<Integer,Question>();
		int size = FullQuestionDataBase.size();
		int random1=1;
		int random2=2;
		int random3=3;
		if(size>3){
			random1 = ((int)((size)*Math.random()));
			random2 = ((int)((size)*Math.random()));
			random3 = ((int)((size)*Math.random()));
			while(random1==random2)
				random2 = ((int)((size)*Math.random()));
			while(random3 == random1 || random3 == random2)
				random3 = ((int)((size)*Math.random()));
		}
		ThreeQuestions.put(1, new Question(FullQuestionDataBase.get(random1).getQuestion(),FullQuestionDataBase.get(random1).getAnswer()));
		ThreeQuestions.put(2, new Question(FullQuestionDataBase.get(random2).getQuestion(),FullQuestionDataBase.get(random2).getAnswer()));
		ThreeQuestions.put(3, new Question(FullQuestionDataBase.get(random3).getQuestion(),FullQuestionDataBase.get(random3).getAnswer()));		
	}

	/**
	 * 
	 * @param room - hostRoom
	 * @throws IOException
	 */
	public BlufferGame(Room room) throws IOException{
		FullQuestionDataBase = new ConcurrentHashMap<Integer,Question>();
		this.HostRoom = room;
	}
	@Override
	public synchronized void start() throws IOException{
		ReadFromJson();
		ThreeRandomQuestion();
		finish = false;
		this.HostRoom.sendMsg("GAMEMSG Starting new Bluffer Game!","");
		next();
	}
	@Override
	public synchronized void next() throws IOException {
		this.current = new ActiveBlufferGame(this,this.ThreeQuestions.get(numOfQuestions),HostRoom);
		ThreeQuestions.remove(numOfQuestions);
		this.HostRoom.sendMsg("ASKTXT "+current.getMessage(),"");
		numOfQuestions--;
		if(numOfQuestions==0){
			this.finish = true;
		}
	}
	
	@Override
	public ActiveGame<StringMessage> getCurrentGame() {
		return current;
	}


	@Override
	public boolean isFinish() {
		return finish;
	}
}
