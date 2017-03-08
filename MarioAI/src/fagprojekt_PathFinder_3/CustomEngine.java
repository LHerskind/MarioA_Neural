package fagprojekt_PathFinder_3;

import ch.idsia.benchmark.mario.engine.sprites.Mario;

public class CustomEngine {

	static float INERTIA_X = 0.89f;
	static float INERTIA_Y = 0.85f;

	static float ax = 1f;
	static float ay = -2f;
	static float gravity = 3f;

	private int width = 4;
	private int height = 24;

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
		onGround = last.getOnGround();
		ableToJump = false;

		nextState = new State();

		nextState.setAction(action);
		nextState.setX(last.getX());
		nextState.setXTot(last.getXTot());
		nextState.setY(last.getY());

		move(last, action);

		return nextState;
	}

	private void move(State last, boolean[] action) {
		collideX = collideY = false;

		onGround = block(last, 0, 1);

		float vx = getVX(last, action);
		float vy = getVY(last, action);

		nextState.setVX(vx);
		nextState.setVY(vy);

		int times = 0;

		if (vx > 0) {
			times = (int) vx / 8;
			for (int i = 0; i < times; i++) {
				if (!collideX) {
					move(last, nextState, 8, 0);
				}
			}
			if (!collideX) {
				move(last, nextState, vx - times * 8, 0);
			}
		} else if (vx < 0) {
			times = (int) -vx / 8;
			for (int i = 0; i < times; i++) {
				if (!collideX) {
					move(last, nextState, -8, 0);
				}
			}
			if (!collideX) {
				move(last, nextState, vx + times * 8, 0);
			}
		}

		if (vy > 0) {
			times = (int) vy / 8;
			for (int i = 0; i < times; i++) {
				if (!collideY) {
					move(last, nextState, 0, 8);
				}
			}
			if (!collideY) {
				move(last, nextState, 0, vy - 8 * times);
			}
		} else if (vy < 0) {
			times = (int) -vy / 8;
			for (int i = 0; i < times; i++) {
				if (!collideY) {
					move(last, nextState, 0, -8);
				}
			}
			if (!collideY) {
				move(last, nextState, 0, vy + 8 * times);
			}
		}

	}

	private boolean collideY;
	private boolean collideX;

	private void move(State last, State next, float xa, float ya) {
		int x = 0;
		int y = 0;

		if (block(last, xa, ya)) {
			if (ya > 0) {
				y = (int) (last.getY()) / 16 + 1;
				float ny = (y * 16 - 1);
				next.setX(next.getX() + xa);
				next.setXTot(next.getXTot() + xa);
				next.setY(ny);
				next.setVY(0);
				next.setOnGround(true);
				collideY = true;
			}
			
			if(xa > 0){
				
			}
			
			
		} else {
			next.setX(next.getX() + xa);
			next.setXTot(next.getXTot() + xa);
			next.setY(next.getY() + ya);
		}
	}

	private boolean block(State last, float xa, float ya) {
		float x = last.getXTot();
		float y = last.getY();
		if (last.getXTot() > 16 * 9) {
			x = 9 * 16 + last.getX();
		}

		if (ya > 0) {
			if (isBlocking(x + xa - width, y + ya + height / 2)) {
				return true;
			} else if (isBlocking(x + xa + width, y + ya + height / 2)) {
				return true;
			}
		} else if (ya < 0) {
			// TODO:
		}

		if (xa > 0) {
			if (isBlocking(x + xa + width, y + ya + height / 2)) {
				return true;
			} else if (isBlocking(x + xa + width, y + ya)) {
				return true;
			}
		}

		return false;
	}

	public boolean isBlocking(float __x, float __y) {
		int x = (int) __x / 16;
		int y = (int) __y / 16;
		if (x >= 0 && x <= 18 && y >= 0 && y < 16) {
			return (scene[y][x] < 0);
		}
		return false;
	}

	private float getVX(State last, boolean[] action) {
		float vx = last.getVX();
		float ax_1 = action[Mario.KEY_SPEED] ? 2 * ax : ax;
		if (action[Mario.KEY_LEFT]) {
			vx -= ax_1;
		}
		if (action[Mario.KEY_RIGHT]) {
			vx += ax_1;
		}
		vx *= INERTIA_X;
		return vx;
	}

	private float getVY(State last, boolean[] action) {
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
		return vy;
	}

}
