package fagprojekt_PathFinder_4;

import ch.idsia.benchmark.mario.engine.sprites.Mario;

public class CustomEngine_Move {

	float INERTIA = 0.89f;

	int width = 4;
	int height = 24;

	boolean wasOnGround;
	boolean onGround;
	boolean ducking;
	boolean sliding;
	boolean large;
	boolean mayJump;

	float xJumpSpeed;
	float yJumpSpeed;

	private int facing;

	private float xa;
	private float ya;

	private int jumpTime;
	private int jT;

	float x;
	float y;

	private CustomEngine customEngine;

	public CustomEngine_Move(CustomEngine customEngine) {
		this.customEngine = customEngine;
	}

	private void setup(State last) {
		x = last.getX();
		y = last.getY();
		xa = last.getVX();
		ya = last.getVY();
		jT = last.getJump();
		onGround = last.getOnGround();
	}

	private State getState(boolean[] action) {
		State state = new State();
		state.setAction(action);
		state.setJump(jT);
		state.setVX(xa);
		state.setVY(ya);
		state.setX(x);
		state.setY(y);
		state.setOnGround(onGround);
		// System.out.println(x + " " + y);
		return state;
	}

	public State move(State last, boolean[] keys) {
		setup(last);
		move(keys);
		return getState(keys);
	}

	public void move(boolean[] keys) {

		/*
		 * if (this.inLadderZone) { if (keys[KEY_UP] && !onLadder) { onLadder =
		 * true; }
		 * 
		 * if (!keys[KEY_UP] && !keys[KEY_DOWN] && onLadder) ya = 0;
		 * 
		 * if (onLadder) { if (!onTopOfLadder) { ya = keys[KEY_UP] ? -10 : ya; }
		 * else { ya = 0; ya = keys[KEY_DOWN] ? 10 : ya; if (keys[KEY_DOWN])
		 * onTopOfLadder = false; } onGround = true; } }
		 */

		wasOnGround = onGround;
		float sideWaysSpeed = keys[Mario.KEY_SPEED] ? 1.2f : 0.6f;

		if (onGround) {
			ducking = keys[Mario.KEY_DOWN] && large;
		}

		if (xa > 2) {
			facing = 1;
		}
		if (xa < -2) {
			facing = -1;
		}

		if (keys[Mario.KEY_JUMP] || (jumpTime < 0 && !onGround && !sliding)) {
			if (jumpTime < 0) {
				xa = xJumpSpeed;
				ya = -jumpTime * yJumpSpeed;
				jumpTime++;
			} else if (onGround && mayJump) {
				xJumpSpeed = 0;
				yJumpSpeed = -1.9f;
				jumpTime = (int) jT;
				ya = jumpTime * yJumpSpeed;
				onGround = false;
				sliding = false;
			} else if (sliding && mayJump) {
				xJumpSpeed = -facing * 6.0f;
				yJumpSpeed = -2.0f;
				jumpTime = -6;
				xa = xJumpSpeed;
				ya = -jumpTime * yJumpSpeed;
				onGround = false;
				sliding = false;
				facing = -facing;
			} else if (jumpTime > 0) {
				xa += xJumpSpeed;
				ya = jumpTime * yJumpSpeed;
				jumpTime--;
			}
		} else {
			jumpTime = 0;
		}

		if (keys[Mario.KEY_LEFT] && !ducking) {
			if (facing == 1)
				sliding = false;
			xa -= sideWaysSpeed;
			if (jumpTime >= 0)
				facing = -1;
		}

		if (keys[Mario.KEY_RIGHT] && !ducking) {
			if (facing == -1)
				sliding = false;
			xa += sideWaysSpeed;
			if (jumpTime >= 0)
				facing = 1;
		}

		if ((!keys[Mario.KEY_LEFT] && !keys[Mario.KEY_RIGHT]) || ducking || ya < 0 || onGround) {
			sliding = false;
		}

		mayJump = (onGround || sliding) && !keys[Mario.KEY_JUMP];

		if (sliding) {
			ya *= 0.5f;
		}

		onGround = false;
		move(xa, 0);
		move(0, ya);

		ya *= 0.85f;
		if (onGround) {
			xa *= (INERTIA);
		} else {
			xa *= (INERTIA);
		}

		if (!onGround) {
			ya += 3f;
		}

	}

	private int size = 8;

	private boolean move(float xa, float ya) {
		while (xa > size) {
			if (!move(size, 0))
				return false;
			xa -= size;
		}
		while (xa < -size) {
			if (!move(-size, 0))
				return false;
			xa += size;
		}
		while (ya > size) {
			if (!move(0, size))
				return false;
			ya -= size;
		}
		while (ya < -size) {
			if (!move(0, -size))
				return false;
			ya += size;
		}

		boolean collide = false;
		if (ya > 0) {
			if (isBlocking(x + xa - width, y + ya, xa, 0)) {
				collide = true;
			} else if (isBlocking(x + xa + width, y + ya, xa, 0)) {
				collide = true;
			} else if (isBlocking(x + xa - width, y + ya + 1, xa, ya)) {
				collide = true;
			} else if (isBlocking(x + xa + width, y + ya + 1, xa, ya)) {
				collide = true;
			}
		}
		if (ya < 0) {
			if (isBlocking(x + xa, y + ya - height, xa, ya)) {
				collide = true;
			} else if (collide || isBlocking(x + xa - width, y + ya - height, xa, ya)) {
				collide = true;
			} else if (collide || isBlocking(x + xa + width, y + ya - height, xa, ya)) {
				collide = true;
			}
		}
		if (xa > 0) {
			sliding = true;
			if (isBlocking(x + xa + width, y + ya - height, xa, ya)) {
				collide = true;
			} else {
				sliding = false;
			}
			if (isBlocking(x + xa + width, y + ya - height / 2, xa, ya)) {
				collide = true;
			} else {
				sliding = false;
			}
			if (isBlocking(x + xa + width, y + ya, xa, ya)) {
				collide = true;
			} else {
				sliding = false;
			}
		}
		if (xa < 0) {
			sliding = true;
			if (isBlocking(x + xa - width, y + ya - height, xa, ya))
				collide = true;
			else
				sliding = false;
			if (isBlocking(x + xa - width, y + ya - height / 2, xa, ya))
				collide = true;
			else
				sliding = false;
			if (isBlocking(x + xa - width, y + ya, xa, ya))
				collide = true;
			else
				sliding = false;
		}

		if (collide) {
			if (xa < 0) {
				x = (int) ((x - width) / 16) * 16 + width;
				this.xa = 0;
			}
			if (xa > 0) {
				x = (int) ((x + width) / 16 + 1) * 16 - width - 1;
				this.xa = 0;
			}
			if (ya < 0) {
				y = (int) ((y - height) / 16) * 16 + height;
				jumpTime = 0;
				this.ya = 0;
			}
			if (ya > 0) {
				y = (int) ((y - 1) / 16 + 1) * 16 - 1;
				onGround = true;
				this.ya = 0;
			}
			return false;
		} else {
			x += xa;
			y += ya;
			return true;
		}
	}

	private boolean isBlocking(final float _x, final float _y, final float xa, final float ya) {
		return customEngine.isBlocking(_x, _y);
	}

}
