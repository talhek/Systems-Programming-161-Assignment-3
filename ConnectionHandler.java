package Server.ThreadPerClient;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

import Server.protocol.AsyncServerProtocol;
import Server.protocol.ProtocolCallback;
import Server.tokenizer.MessageTokenizer;


class ConnectionHandler<T> implements Runnable {

	private final MessageTokenizer<T> tokenizer;
	SocketChannel clientSocket;
	AsyncServerProtocol<T> protocol;
	private ProtocolCallback<T> cb;



	public ConnectionHandler(SocketChannel acceptedSocket, AsyncServerProtocol<T> asyncServerProtocol, MessageTokenizer<T> messageTokenizer) {
		this.tokenizer = messageTokenizer;
		clientSocket = acceptedSocket;
		protocol = asyncServerProtocol;
		System.out.println("Accepted connection from client!");
		cb = response->{
			if (response != null) {
				ByteBuffer bytes = messageTokenizer.getBytesForMessage(response);
				clientSocket.write(bytes);
			}
		};
	}


	public void run()
	{

		try {
			initialize();
		}
		catch (IOException e) {
			System.out.println("Error in initializing I/O");
		}

		try {
			process();
		} 
		catch (IOException e) {
			System.out.println("Error in I/O");
		} 

		System.out.println("Connection closed - bye bye...");
		close();

	}

	public void process() throws IOException
	{
		T msg;

		ByteBuffer inbuf = ByteBuffer.allocate(1024); 

		while (true)
		{
			if(!tokenizer.hasMessage()){
				inbuf.clear();
				if(this.clientSocket.read(inbuf) == -1){
					protocol.connectionTerminated();
					break;
				}
				inbuf.flip();
				tokenizer.addBytes(inbuf);
			}
			else{
				msg = tokenizer.nextMessage();
				protocol.processMessage(msg,cb);
				if(((AsyncServerProtocol<T>)this.protocol).shouldClose())
					break;
			}
		}
	}

	// Starts listening
	public void initialize() throws IOException
	{


	}

	// Closes the connection
	public void close()
	{
		try {
			clientSocket.close();
		}
		catch (IOException e)
		{
			Logger.getLogger("edu.spl.reactor").warning("Exception in closing I/O");
		}
	}

}