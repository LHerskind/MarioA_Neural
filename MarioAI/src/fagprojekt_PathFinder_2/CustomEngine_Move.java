package fagprojekt_PathFinder_2;

import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.mario.engine.LevelScene;
import ch.idsia.benchmark.mario.engine.sprites.Fireball;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.engine.sprites.Sparkle;

public class CustomEngine_Move {
	
	float INERTIA = 0.89f;
	
	int width = 8;
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
	private boolean ableToShoot;
	private int runTime;
	
	float x;
	float y;
	
	public void move(State last, boolean[] keys) {

/*		if (this.inLadderZone) {
			if (keys[KEY_UP] && !onLadder) {
				onLadder = true;
			}

			if (!keys[KEY_UP] && !keys[KEY_DOWN] && onLadder)
				ya = 0;

			if (onLadder) {
				if (!onTopOfLadder) {
					ya = keys[KEY_UP] ? -10 : ya;
				} else {
					ya = 0;
					ya = keys[KEY_DOWN] ? 10 : ya;
					if (keys[KEY_DOWN])
						onTopOfLadder = false;
				}
				onGround = true;
			}
		}*/

		wasOnGround = onGround;
		float sideWaysSpeed = keys[Mario.KEY_SPEED] ? 1.2f : 0.6f;

		// float sideWaysSpeed = onGround ? 2.5f : 1.2f;

		if (onGround) {
			ducking = keys[Mario.KEY_DOWN] && large;
		}

		if (xa > 2) {
			facing = 1;
		}
		if (xa < -2) {
			facing = -1;
		}

		// float Wind = 0.2f;
		// float windAngle = 180;
		// xa += Wind * Math.cos(windAngle * Math.PI / 180);

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
/*
		if (keys[KEY_SPEED] && ableToShoot && Mario.fire && levelScene.fireballsOnScreen < 2) {
			levelScene.addSprite(new Fireball(levelScene, x + facing * 6, y - 20, facing));
		}
		*/

		
		ableToShoot = !keys[Mario.KEY_SPEED];

		mayJump = (onGround || sliding) && !keys[Mario.KEY_JUMP];

//		xFlipPic = facing == -1;

		runTime += (Math.abs(xa)) + 5;
		if (Math.abs(xa) < 0.5f) {
			runTime = 0;
			xa = 0;
		}

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

		// if /

		if (!onGround) {
			 ya += 3;
//			ya += yaa;
		}
/*
		if (carried != null) {
			carried.x = x + facing * 8; // TODO:|L| move to cellSize_2 =
										// cellSize/2;
			carried.y = y - 2;
			if (!keys[KEY_SPEED]) {
				carried.release(this);
				carried = null;
				setRacoon(false);
				// System.out.println("carried = " + carried);
			}
			// System.out.println("sideWaysSpeed = " + sideWaysSpeed);
		}
		*/
	}

	private boolean move(float xa, float ya) {
		while (xa > 8) {
			if (!move(8, 0))
				return false;
			xa -= 8;
		}
		while (xa < -8) {
			if (!move(-8, 0))
				return false;
			xa += 8;
		}
		while (ya > 8) {
			if (!move(0, 8))
				return false;
			ya -= 8;
		}
		while (ya < -8) {
			if (!move(0, -8))
				return false;
			ya += 8;
		}

		boolean collide = false;
		if (ya > 0) {
			if (isBlocking(x + xa - width, y + ya, xa, 0))
				collide = true;
			else if (isBlocking(x + xa + width, y + ya, xa, 0))
				collide = true;
			else if (isBlocking(x + xa - width, y + ya + 1, xa, ya))
				collide = true;
			else if (isBlocking(x + xa + width, y + ya + 1, xa, ya))
				collide = true;
		}
		if (ya < 0) {
			if (isBlocking(x + xa, y + ya - height, xa, ya))
				collide = true;
			else if (collide || isBlocking(x + xa - width, y + ya - height, xa, ya))
				collide = true;
			else if (collide || isBlocking(x + xa + width, y + ya - height, xa, ya))
				collide = true;
		}
		if (xa > 0) {
			sliding = true;
			if (isBlocking(x + xa + width, y + ya - height, xa, ya))
				collide = true;
			else
				sliding = false;
			if (isBlocking(x + xa + width, y + ya - height / 2, xa, ya))
				collide = true;
			else
				sliding = false;
			if (isBlocking(x + xa + width, y + ya, xa, ya))
				collide = true;
			else
				sliding = false;
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
			}
			return false;
		} else {
			x += xa;
			y += ya;
			return true;
		}
	}
	
}
