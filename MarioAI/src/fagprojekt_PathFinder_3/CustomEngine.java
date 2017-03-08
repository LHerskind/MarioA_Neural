package fagprojekt_PathFinder_3;

import ch.idsia.benchmark.mario.engine.sprites.Mario;

public class CustomEngine {

	static float INERTIA_X = 0.89f;
	static float INERTIA_Y = 0.85f;

	static float ax = 0.6f;
	static float ay = -1.9f;
	static float gravity = 3f;

	private int width = 4;
	private int height = 24;

	boolean onGround = false;
	boolean ableToJump = false;

	private boolean collideY;
	private boolean collideX;

	private int size = 8;

	private byte[][] map = new byte[19][500];

	private PFAgent agent;

	public CustomEngine(PFAgent agent) {
		this.agent = agent;
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

	public void toScene(float x) {
		if (x > highestX) {
			highestX = x;
			int la = (int) highestX / 16;
			if (la > mapX - 8) {
				System.out.println(la +  " HMM " + mapX);
				mapX++;
				for (int i = 0; i < 19; i++) {
					map[i][mapX] = agent.getBlock(i); // scene[i][18];
				}
				System.out.println();
			}
		}
	}

	public void printOnGoing(float x, float y) {
		int __x = (int) x / 16;
		int __y = (int) y / 16;
		// System.out.println(__x + " " + __y);

		for (int i = 0; i < mapX; i++) {
			System.out.print(i + mapX - 18 + "\t");
		}
		System.out.println();
		for (int i = 0; i < 19; i++) {
			for (int j = mapX - 18; j < mapX + 1; j++) {
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

	public Move getMove(Move move, boolean[] action) {

		// printOnGoing(move.getState().getX(), move.getState().getY());

		State nState = tic(move.getState(), action);
		if (nState != null) {
			return new Move(nState.getX() - move.getPoints(), move, nState);
		}
		return null;
	}

	private State nextState;

	public State tic(State last, boolean[] action) {
		onGround = false;
		ableToJump = false;

		nextState = new State();

		nextState.setAction(action);
		nextState.setX(last.getX());
		nextState.setY(last.getY());

		move(last, action);

		return nextState;
	}

	private void move(State last, boolean[] action) {
		collideX = false;
		collideY = false;

		onGround = isBlocking(last.getX(), last.getY() + height / 2);
		// System.out.println(onGround + " " + last.getX() / 16 + " " +
		// last.getY() / 16);

		float vx = getVX(last, action);
		float vy = getVY(last, action);

		nextState.setVX(vx);
		nextState.setVY(vy);

		int times = 0;

		if (vx > 0) {
			times = (int) vx / size;
			for (int i = 0; i < times; i++) {
				if (!collideX) {
					move(last, nextState, size, 0);
				}
			}
			if (!collideX) {
				move(last, nextState, vx - times * size, 0);
			}
		} else if (vx < 0) {
			times = (int) -vx / size;
			for (int i = 0; i < times; i++) {
				if (!collideX) {
					move(last, nextState, -size, 0);
				}
			}
			if (!collideX) {
				move(last, nextState, vx + times * size, 0);
			}
		}

		if (vy > 0) {
			times = (int) vy / size;
			for (int i = 0; i < times; i++) {
				if (!collideY) {
					move(last, nextState, 0, size);
				}
			}
			if (!collideY) {
				move(last, nextState, 0, vy - size * times);
			}
		} else if (vy < 0) {
			times = (int) -vy / size;
			for (int i = 0; i < times; i++) {
				if (!collideY) {
					move(last, nextState, 0, -size);
				}
			}
			if (!collideY) {
				move(last, nextState, 0, vy + size * times);
			}
		}

	}

	private void move(State last, State next, float xa, float ya) {
		int x = 0;
		int y = 0;

		if (block(last, xa, ya)) {
			if (ya > 0) {
				y = (int) (last.getY() - 1) / 16 + 1;
				float ny = (y * 16 - 1);
				next.setY(ny);
				next.setVY(0);
				next.setOnGround(true);
				collideY = true;
			}
			if (ya < 0) {
				y = (int) (last.getY() - height) / 16;
				float ny = (y * 16 + height);
				next.setY(ny);
				next.setVY(0);
				collideY = true;
			}
			if (xa > 0) {
				x = (int) (last.getX() + width) / 16 + 1;
				float nx = (x * 16 - width - 1);
				next.setX(nx);
				next.setVX(0);
				collideX = true;
			}
			if (xa < 0) {
				x = (int) (x - width) / 16;
				float nx = (x * 16 + width);
				next.setX(nx);
				next.setVX(0);
				collideX = true;
			}
		} else {
			next.setX(next.getX() + xa);
			next.setY(next.getY() + ya);
		}
	}

	private boolean block(State last, float xa, float ya) {
		float x = last.getX();
		float y = last.getY();

		if (ya > 0) {
			if (isBlocking(x + xa - width, y + ya)) {
				return true;
			} else if (isBlocking(x + xa + width, y + ya)) {
				return true;
			} else if (isBlocking(x + xa - width, y + ya + 1)) {
				return true;
			} else if (isBlocking(x + xa + width, y + ya + 1)) {
				return true;
			}
		} else if (ya < 0) {
			if (isBlocking(x + xa, y + ya - height)) {
				return true;
			} else if (isBlocking(x + xa - width, y + ya - height)) {
				return true;
			} else if (isBlocking(x + xa + width, y + ya - height)) {
				return true;
			}
		}
		if (xa > 0) {
			if (isBlocking(x + xa + width, y + ya - height)) {
				return true;
			} else if (isBlocking(x + xa + width, y + ya - height / 2)) {
				return true;
			} else if (isBlocking(x + xa + width, y + ya - 4)) {
				return true;
			}
		} else if (xa < 0) {
			if (isBlocking(x + xa - width, y + ya - height)) {
				return true;
			} else if (isBlocking(x + xa - width, y + ya - height / 2)) {
				return true;
			} else if (isBlocking(x + xa - width, y + ya)) {
				return true;
			}
		}

		return false;
	}

	public boolean isBlocking(float __x, float __y) {
		int x = (int) __x / 16;
		int y = (int) __y / 16;
		if (x >= 0 && x <= 600 && y >= 0 && y < 16) {
			return map[y][x] < 0;
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
