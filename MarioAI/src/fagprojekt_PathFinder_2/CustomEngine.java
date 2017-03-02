package fagprojekt_PathFinder_2;

import ch.idsia.benchmark.mario.engine.sprites.Mario;

public class CustomEngine {

	static float INERTIA = 0.89f;
	static float ax = 2;
	static float ay = -2;
	static float gravity = 1;

	boolean onGround = false;
	boolean ableToJump = false;

	private State nextState = new State();

	private byte[][] merged;

	public CustomEngine(byte[][] merged) {
		this.merged = merged;
	}

	public Move getMove(Move move, boolean[] action) {
		double points = 0;
		if (move != null) {
			points += move.getPoints();
		}

		State nState = tic(move.getState(), action);
		if (nState == null) {
			return null;
		}
		points += (nState.getX() - move.getState().getX());

		return new Move(points, move, nState);
	}

	public State tic(State last, boolean[] action) {
		int xCood = (int) last.getX() / 16;
		int yCood = (int) last.getY() / 16;

		if (xCood <= 0 || xCood >= 18 || yCood <= 0 || yCood >= 18) {
			System.out.println(xCood + " : " + yCood);
			System.out.println("LORT");
			return null;
		}

		if (merged[yCood + 1][xCood] < 0) {
			onGround = true;
			ableToJump = true;
		}

		moveX(last, action);
		moveY(last, action);

		xCood = (int) (nextState.getX() - last.getX()) / 16;
		yCood = (int) (nextState.getY()) / 16;
//		System.out.println(nextState.getX() + " = " + xCood + " : " + nextState.getY() + " = " + yCood);

		if (!possible(xCood, yCood)) {
			return null;
		}

		return nextState;
	}

	private void moveX(State last, boolean[] action) {
		float vx = last.getX();
		if (action[Mario.KEY_LEFT]) {
			vx -= ax;
		}
		if (action[Mario.KEY_RIGHT]) {
			vx += ax;
		}
		vx *= INERTIA;
		nextState.setVX(vx);
		float x = last.getX() + vx;
		nextState.setX(x);
	}

	private void moveY(State last, boolean[] action) {
		float vy = last.getVY();
		if (onGround) {
			vy = 0;
		}

		if (ableToJump) {
			nextState.setJump(0);
		} else {
			nextState.setJump(last.getJump());
		}

		if (action[Mario.KEY_JUMP]) {
			if (ableToJump) {
				if (nextState.getJump() < 7) {
					nextState.setJump(nextState.getJump() + 1);
					vy += ay;
				} else {
					ableToJump = false;
				}
			}
		}

		if (!onGround) {
			vy += gravity;
		}

		float y = last.getY() + vy;
		nextState.setVY(vy);
		nextState.setY(y);
	}

	private boolean possible(int x, int y) {
		if (x >= 0 && x <= 18 && y >= 0 && y <= 18) {
			return (merged[y][x] >= 0);
		}
		return false;
	}

}
