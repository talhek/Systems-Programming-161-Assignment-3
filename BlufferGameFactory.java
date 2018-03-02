package Game;

import java.io.IOException;

import Server.tokenizer.StringMessage;

public class BlufferGameFactory implements GameFactory<StringMessage> {

	public BlufferGameFactory(){}
	
	@Override
	public tbgp<StringMessage> create(Room room) throws IOException {
		return new BlufferGame(room);
	}

}
