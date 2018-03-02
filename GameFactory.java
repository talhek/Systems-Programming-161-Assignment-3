package Game;

import java.io.IOException;

public interface GameFactory<T> {
	/**
	 * 
	 * create a new game.
	 * @param room - the host room.
	 * @return a the created game.
	 * @throws IOException
	 */
	public tbgp<T> create(Room room) throws IOException;
}
