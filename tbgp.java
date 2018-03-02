package Game;

import java.io.IOException;

public interface tbgp<T> {
/**
 * start a new game
 * @throws IOException
 */
	public void start() throws IOException;
/**
 * go to a next stage
 * @throws IOException
 */
	public void next() throws IOException;
	/**
	 * 
	 * @return true if the game is finished
	 */
	public boolean isFinish();
	/**
	 * 
	 * @return return the current active game.
	 */
	public ActiveGame<T> getCurrentGame();
}
