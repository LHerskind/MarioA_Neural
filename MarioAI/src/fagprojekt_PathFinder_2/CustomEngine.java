package fagprojekt_PathFinder_2;

import ch.idsia.benchmark.mario.engine.sprites.Mario;

public class CustomEngine {

	static float INERTIA_X = 0.89f;
	static float INERTIA_Y = 0.85f;

	static float ax = 1f;
	static float ay = -2f;
	static float gravity = 1f;

	boolean onGround = false;
	boolean ableToJump = false;

	private byte[][] scene;

	private CustomEngine_Move customEngine_Move;

	public CustomEngine() {
		this.customEngine_Move = new CustomEngine_Move(this);
	}

	public void setScene(byte[][] scene) {
		this.scene = scene;
	}

	public Move getMove(Move move, boolean[] action) {
		State nState = tic(move.getState(), action);
		if (nState != null) {
			return new Move(nState.getX(), move, nState);
		}
		return null;
	}

	public State tic(State last, boolean[] action) {
		return customEngine_Move.move(last, action);
	}	

	public boolean isBlocking(int x, int y) {
		if (x >= -9 && x <= 9 && y >= 0 && y < 16) {
			return (scene[y][x + 9] < 0);
		}
		return false;
	}

}
