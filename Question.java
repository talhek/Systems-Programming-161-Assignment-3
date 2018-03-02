package Game;

public class Question {
	private String Answer;
	private String Question;
	public Question(String question,String Answer){
		this.Answer = Answer;
		this.Question = question;
	}
	public String getAnswer() {
		return Answer;
	}
	public void setAnswer(String answer) {
		Answer = answer;
	}
	public String getQuestion() {
		return Question;
	}
	public void setQuestion(String question) {
		Question = question;
	}

}
