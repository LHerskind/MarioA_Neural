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

	public CustomEngine() {

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

	private State nextState;

	public State tic(State last, boolean[] action) {
		onGround = false;
		ableToJump = false;
		nextState = new State();
		nextState.setAction(action);

		// System.out.println("TIC: " + last.getY() + " " + yCood + " " +
		// last.getVY());

		moveX(last, action);
		moveY(last, action);

		int xCood = (int) Math.ceil(nextState.getX() / 16) + 9;
		int yCood = (int) Math.ceil(nextState.getY() / 16) + 9;

		if (!possible(xCood, yCood)) {
			return null;
		}
		return nextState;
	}
	
	
	private void move(State last, boolean[] action){
		
		
		
	}

	private void moveX(State last, boolean[] action) {
		float vx = last.getVX();
		float ax_1 = action[Mario.KEY_SPEED] ? 2 * ax : ax;
		if (action[Mario.KEY_LEFT]) {
			vx -= ax_1;
		}
		if (action[Mario.KEY_RIGHT]) {
			vx += ax_1;
		}
		vx *= INERTIA_X;

		float x = last.getX() + vx;

		nextState.setVX(vx);
		nextState.setX(x);
	}

	private void moveY(State last, boolean[] action) {
		if (last.getY() >= 0) {
			if (scene[(int) ((last.getY()) / 16 + 9)][(int) (last.getX() / 16) + 9] < 0) {
				onGround = true;
				ableToJump = true;
			}
		}

		float vy = last.getVY();
		if (onGround) {
			vy = 0;
			nextState.setJump(0);
		} else {
			vy += gravity;
			nextState.setJump(last.getJump());
		}

		if (action[Mario.KEY_JUMP]) {
			boolean lastJump = true;
			if (last != null && last.getAction() != null) {
				lastJump = last.getAction()[Mario.KEY_JUMP];
			}

			if ((nextState.getJump() < 7 && lastJump) || ableToJump) {
				nextState.setJump(nextState.getJump() + 1);
				vy += ay * nextState.getJump();
			}
		}
		vy *= INERTIA_Y;

		float y = last.getY() + vy;

		nextState.setVY(vy);
		nextState.setY(y);
	}

	private boolean possible(int x, int y) {
		if (x >= 0 && x <= 18 && y >= 0 && y < 18) {
			return (scene[y][x] >= 0);
		}
		return false;
	}

}
