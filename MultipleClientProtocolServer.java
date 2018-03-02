package Server.ThreadPerClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import Server.protocol.ServerProtocolFactory;
import Server.tokenizer.FixedSeparatorMessageTokenizer;
import Server.tokenizer.MessageTokenizer;
import Server.tokenizer.StringMessage;
import Server.tokenizer.TokenizerFactory;

class MultipleClientProtocolServer<T> implements Runnable {
	private ServerSocketChannel serverSocket;
	private int listenPort;
	private ServerProtocolFactory<T> factory;
	private int port;
	private TokenizerFactory<T> tokenizer;
	
	 
	public MultipleClientProtocolServer(int port, ServerProtocolFactory<T> p,TokenizerFactory<T> tokenizer)
	{
		serverSocket = null;
		listenPort = port;
		factory = p;
		this.tokenizer = tokenizer;
		this.port = port;

	}
	
	public void run()
	{
		try {
			serverSocket = ServerSocketChannel.open();
			serverSocket.configureBlocking(true);
			serverSocket.socket().bind(new InetSocketAddress(this.port));
			Logger.getLogger("edu.spl.reactor").info("Listening...");
		}
		catch (IOException e) {
			Logger.getLogger("edu.spl.reactor").warning("Cannot listen on port " + this.port);
		}
		
		while (true)
		{

			try {
			ConnectionHandler<T> newConnection = new ConnectionHandler<T>(serverSocket.accept(),factory.create(),tokenizer.create());
            new Thread(newConnection).start();
			}
			catch (IOException e)
			{
				Logger.getLogger("edu.spl.reactor").info("Failed to accept on port " + listenPort);
			}catch(NotYetBoundException e){
				return;
			}
		}
	}
	

	// Closes the connection
	public void close() throws IOException
	{
		serverSocket.close();
	}
	
	public static void main(String[] args) throws IOException
	{
		// Get port
        if (args.length != 1) {
            System.err.println("Usage: java MultipleClientProtocolServer <port>");
            System.exit(1);
        }
		
		int port = Integer.decode(args[0]).intValue();
		final Charset charset = Charset.forName("UTF-8");
		
        TokenizerFactory<StringMessage> tokenizer = new TokenizerFactory<StringMessage>() {
            public MessageTokenizer<StringMessage> create() {
                return new FixedSeparatorMessageTokenizer("\n", charset);
            }
        };
        
		MultipleClientProtocolServer<StringMessage> server = new MultipleClientProtocolServer<StringMessage>(port, new GameProtocolFactory(),tokenizer);
		Thread serverThread = new Thread(server);
		serverThread.start();
		try {
			serverThread.join();
		}
		catch (InterruptedException e)
		{
			Logger.getLogger("edu.spl.reactor").info("Server stopped");
		}
		
		
				
	}
}