package fagprojekt_PathFinder_4;

import ch.idsia.benchmark.mario.engine.level.Level;
import ch.idsia.benchmark.mario.engine.sprites.Mario;

public class CustomEngine {

	static float INERTIA_X = 0.89f;
	static float INERTIA_Y = 0.85f;

	boolean debug = true;

	private CustomEngine_Move customEngine_Move;
	private PFAgent agent;

	private byte[][] map = new byte[19][600];

	public CustomEngine(PFAgent agent) {
		this.agent = agent;
		this.customEngine_Move = new CustomEngine_Move(this);
	}

	public void setScene(byte[][] scene) {
		for (int i = 0; i < 19; i++) {
			for (int j = 0; j < 19; j++) {
				this.map[i][j] = scene[i][j];
			}
		}
		mapX = 18;
	}

	private int mapX = 0;
	private float highestX = 0;

	public void addToScene(byte[] sceneArray) {
		for (int i = 0; i < 19; i++) {
			map[i][mapX] = sceneArray[i];
		}
		mapX++;
	}

	public void printOnGoing(float x, float y) {
		if (debug) {
			System.out.println(mapX);
			int __x = (int) x / 16;
			int __y = (int) y / 16;
			// System.out.println(__x + " " + __y);

			for (int i = 0; i < 19; i++) {
				for (int j = 0; j < mapX + 1; j++) {
					if (i == __y && j == __x) {
						System.out.print("M" + "\t");
					} else {
						System.out.print(map[i][j] + "\t");
					}
				}
				System.out.println();
			}
			System.out.println();
		}
	}

	public Move getMove(Move move, boolean[] action) {
		if (move.getState().getX() > highestX) {
			highestX = move.getState().getX();
			if ((int) highestX / 16 > mapX - 9) {
				byte[] array = new byte[19];
				for (int i = 0; i < 19; i++) {
					array[i] = agent.getBlock(i); // scene[i][18];
				}
				addToScene(array);
			}
		}

		// printOnGoing(move.getState().getX(), move.getState().getY());

		State nState = customEngine_Move.move(move.getState(), action);

		if (nState != null) {
			return new Move(nState.getX(), move, nState);
		}
		return null;
	}

	public boolean isBlocking(float __x, float __y) {
		int x = (int) __x / 16;
		int y = (int) __y / 16;
		if (x >= 0 && x < 600 && y >= 0 && y < 16) {
			return map[y][x] < 0;
		}
		return false;
	}

}
