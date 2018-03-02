package Server.ThreadPerClient;

import Server.protocol.AsyncServerProtocol;
import Server.protocol.GameProtocol;
import Server.protocol.ServerProtocolFactory;
import Server.tokenizer.StringMessage;

public class GameProtocolFactory implements ServerProtocolFactory<StringMessage>{

	@Override
	public AsyncServerProtocol<StringMessage> create() {
		return new GameProtocol();
	}

}
