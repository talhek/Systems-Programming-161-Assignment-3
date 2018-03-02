package Game;
import java.io.IOException;
import Server.protocol.ProtocolCallback;


public interface ActiveGame<T> {
	/**
	 * handle a text response from the client
	 * @param txt text respond from the client 
	 * @param cb - the client callback
	 * @throws IOException
	 */
	public void TxtResp(String txt,ProtocolCallback<T> cb) throws IOException;
	/**
	 * handle a select response from the client
	 * @param choice select respond from the client
	 * @param nick - the client nick name
	 * @throws IOException
	 */
	public void SelectResp(int choice,String nick) throws IOException;
	
	/**
	 * 
	 * @return the MSGTXT sent to the client.
	 */
	public String getMessage();	
}
