package fagprojekt;

import ch.idsia.benchmark.mario.engine.level.Level;
import fagprojekt.AStarAgent.State;
import fagprojekt.Enemy;
import ch.idsia.benchmark.mario.engine.sprites.Mario;


public class CustomEngine {
	// shooting
//	private int fireBallsOnScreen = 0;

	// Gravity and friction
	public final float marioGravity = 1.0f;
	public final float GROUND_INERTIA = 0.89f; // Need this?
	public final float AIR_INERTIA = 0.89f; // Need this?
	// Mario dimensions
	public final int marioWidth = 4;

	// General dimensions
	public final int cellSize = 16;
	// Map
	
	private byte[][] map = new byte[19][600];
	private int mapX = 0;
	private float highestX = 0;
	// UTILITIES
	public boolean debug = true;
	
	// CHEATER-COLLISION
	public static byte[] TILE_BEHAVIORS = Level.TILE_BEHAVIORS;
	public static final int BIT_BLOCK_UPPER = 1 << 0;
	public static final int BIT_BLOCK_ALL = 1 << 1;
	public static final int BIT_BLOCK_LOWER = 1 << 2;
	public static final int BIT_SPECIAL = 1 << 3;
	public static final int BIT_BUMPABLE = 1 << 4;
	public static final int BIT_BREAKABLE = 1 << 5;
	public static final int BIT_PICKUPABLE = 1 << 6;
	public static final int BIT_ANIMATED = 1 << 7;
	
	public void predictFuture(State state) {
		for(int i = 0; i < state.enemyList.size(); i++) {
			Enemy e = state.enemyList.get(i);
			if(!e.dead)
				e.move(map);
		}
		state.wasOnGround = state.onGround;
		float sideWaysSpeed = state.action[Mario.KEY_SPEED] ? 1.2f : 0.6f;
		if (state.action[Mario.KEY_JUMP] || (state.jumpTime < 0 && !state.onGround && !state.sliding)) {

			if (state.jumpTime < 0) {
				state.ya = -state.jumpTime * state.yJumpSpeed;
				state.jumpTime++;
			} else if (state.onGround && state.mayJump) {
				state.yJumpSpeed = -1.9f;
				state.jumpTime = 7;
				state.ya = state.jumpTime * state.yJumpSpeed;
				state.onGround = false;
				state.sliding = false;

			} else if (state.jumpTime > 0) {
				state.ya = state.jumpTime * state.yJumpSpeed;
				state.jumpTime--;

			} 
			else if (state.jumpTime == 0) { // SELF-MADE
				state.action[Mario.KEY_JUMP] = false;
			}
		} else {
			state.jumpTime = 0;

		}

		if (state.action[Mario.KEY_LEFT]/* && !ducking */) {
			state.xa -= sideWaysSpeed;
		}

		if (state.action[Mario.KEY_RIGHT] /* && !ducking */) {
			state.xa += sideWaysSpeed;
		}

	/*
		 if (state.action[Mario.KEY_SPEED] && state.ableToShoot && Mario.fire && fireBallsOnScreen < 2) {
			 fireBallsOnScreen++;
			 // MAKE FIREBALL CLASS AND CREATE FIREBALL OBJECT HERE
		 }
	*/

		 state.ableToShoot = !state.action[Mario.KEY_SPEED];
		state.mayJump = (state.onGround || state.sliding) && !state.action[Mario.KEY_JUMP];
		state.onGround = false;
		move(state, state.xa, 0); //marioMove
		move(state, 0, state.ya); // marioMove
		


		if (state.y >= 15 * cellSize + cellSize)
			state.penalty(1000);

		if (state.x < 0) {
			state.x = 0;
			state.xa = 0;
		}
		state.ya *= 0.85f;
		if (state.onGround) {
			state.xa *= (GROUND_INERTIA);
		} else {
			state.xa *= (AIR_INERTIA);
		}

		if (!state.onGround) {
			state.ya += 3;
		}
		for(int i = 0; i < state.enemyList.size(); i++) {
			Enemy e = state.enemyList.get(i);
			if(!e.dead) {
				e.collideCheck(state);
				if(state.stomp) 
					stomp(state,e);
			}
		}
		
	}

	private boolean move(State state, float xa, float ya) {

		while (xa > 8) {
			if (!move(state, 8, 0))
				return false;
			xa -= 8;
		}
		while (xa < -8) {
			if (!move(state, -8, 0))
				return false;
			xa += 8;
		}
		while (ya > 8) {
			if (!move(state, 0, 8))
				return false;
			ya -= 8;
		}
		while (ya < -8) {
			if (!move(state, 0, -8))
				return false;
			ya += 8;
		}
		boolean collide = false;
		if (ya > 0) {
			if (isBlocking(state, state.x + xa - marioWidth, state.y + ya, xa, 0))
				collide = true;
			else if (isBlocking(state, state.x + xa + marioWidth, state.y + ya, xa, 0))
				collide = true;
			else if (isBlocking(state, state.x + xa - marioWidth, state.y + ya + 1, xa, ya))
				collide = true;
			else if (isBlocking(state, state.x + xa + marioWidth, state.y + ya + 1, xa, ya))
				collide = true;
		}
		if (ya < 0) {
			if (isBlocking(state, state.x + xa, state.y + ya - state.height, xa, ya))
				collide = true;
			else if (collide || isBlocking(state, state.x + xa - marioWidth, state.y + ya - state.height, xa, ya))
				collide = true;
			else if (collide || isBlocking(state, state.x + xa + marioWidth, state.y + ya - state.height, xa, ya))
				collide = true;
		}
		if (xa > 0) {
			if (isBlocking(state, state.x + xa + marioWidth, state.y + ya - state.height, xa, ya))
				collide = true;
			if (isBlocking(state, state.x + xa + marioWidth, state.y + ya - state.height / 2, xa, ya))
				collide = true;
			if (isBlocking(state, state.x + xa + marioWidth, state.y + ya, xa, ya))
				collide = true;
		}
		if (xa < 0) {
			if (isBlocking(state, state.x + xa - marioWidth, state.y + ya - state.height, xa, ya))
				collide = true;
			if (isBlocking(state, state.x + xa - marioWidth, state.y + ya - state.height / 2, xa, ya))
				collide = true;
			if (isBlocking(state, state.x + xa - marioWidth, state.y + ya, xa, ya))
				collide = true;
		}

		if (collide) {
			if (xa < 0) {
				state.x = (int) ((state.x - marioWidth) / 16) * 16 + marioWidth;
				state.xa = 0;
			}
			if (xa > 0) {
				state.x = (int) ((state.x + marioWidth) / 16 + 1) * 16 - marioWidth - 1;
				state.xa = 0;
			}
			if (ya < 0) {

				state.y = (int) ((state.y - state.height) / 16) * 16 + state.height;
				state.jumpTime = 0;
				state.ya = 0;
			}
			if (ya > 0) {
				state.y = (int) ((state.y - 1) / 16 + 1) * 16 - 1;
				state.onGround = true;
			}
			return false;
		} else {
			state.x += xa;
			state.y += ya;
			return true;
		}
	}

	private boolean isBlocking(State state, final float _x, final float _y, final float xa, final float ya) {
		int x = (int) (_x / 16); // TODO: Lidt gl her
		int y = (int) (_y / 16);

		if (x == (int) (state.x / 16) && y == (int) (state.y / 16)){
			return false;
		}
		// CHEATER COLLISION!
		/*
		  byte block = LevelScene.level.getBlock(x, y);
		  boolean blocking = ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_ALL) > 0;
		  blocking |= (ya > 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_UPPER) > 0;
		  blocking |= (ya < 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_LOWER) > 0;
		  return blocking;
		*/
		// CORRECT COLLISION
		if (state.x >= 0 && state.x < 600 * 16 && y >= 0 && y < 16) {
			byte block = map[y][x];
			boolean blocking = block < 0;
			if (ya <= 0) {
				if (block == -62) {
					return false;
				}
			}
			
			return blocking;
		} else {
			return false;
		}
		
	}
	public void stomp(State state, final Enemy enemy) {
		float targetY = enemy.y - enemy.height / 2;
		move(state, 0, targetY - state.y);

		state.yJumpSpeed = -1.9f;
		
		state.jumpTime = 8;
		state.ya = state.jumpTime * state.yJumpSpeed;
		state.onGround = false;
		state.stomp = false;
	}
	public void printOnGoing(float x, float y) {
		if (debug) {
			int __x = (int) x / 16;
			int __y = (int) y / 16;

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
	}

	private byte[][] levelScene;
	private int currY;

	public void setLevelScene(byte[][] levelScene) {
		this.levelScene = levelScene;
	}

	public void setScene(byte[][] levelScene) {
		for (int i = 7; i < 19; i++) {
			for (int j = 7; j < 19; j++) {
				this.map[i - 7][j - 7] = levelScene[i][j];
			}
		}
		mapX = 18 - 7;
		currY = 2;
	}

	public void toScene(float x, float y) {
		if (x > highestX) {
			highestX = x;
			if ((int) ((highestX) / 16) > mapX - 9) {
				mapX++;
				for (int i = 0; i < 19; i++) {
					if ((int) (y / 16) + i - 9 >= 0 && ((int) (y / 16) + i - 9) < 19) {
						map[(int) (y / 16) + i - 9][mapX] = levelScene[i][18];
					}
				}
			}
		}

		if ((int) y / 16 > currY) {
			currY = (int) y / 16;
			if ((int) y / 16 + 9 < 19) {

				for (int i = 0; i < 19; i++) {
					if ((int) (x / 16) - (9 - i) >= 0) {
						map[(int) (y / 16) + 9][i + (int) (x / 16) - 9] = levelScene[18][i];
					}
				}
			}

		} else if ((int) y / 16 < currY) {
			currY = (int) y / 16;
			if ((int) (y / 16) - 9 >= 0) {

				for (int i = 0; i < 19; i++) {
					if ((int) (x / 16) - (9 - i) >= 0) {
						map[(int) (y / 16) - 9][i + (int) (x / 16) - 9] = levelScene[0][i];

					}
				}
			}
		}
	}
	void print() {

		// System.out.println(marioFloatPos[1]+" : "+marioFloatPos[0]);
		// System.out.println(mapX);
		for (int i = 0; i < 19; i++) {
			for (int j = 0; j < mapX; j++) {

				System.out.print(map[i][j] + "\t");

			}
			System.out.println();
		}
		System.out.println();
	}
}
