package fagprojekt;

import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.mario.engine.LevelScene;
import ch.idsia.benchmark.mario.engine.level.Level;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import fagprojekt.AStarAgent.State;

public class CustomEngine {
	// Jumping
	public final int jumpPower = 7;
	public int jumpTime = jumpPower;
	public float xJumpSpeed;
	public float yJumpSpeed;
	// Gravity and friction
	public final float marioGravity = 1.0f;
	public final float GROUND_INERTIA = 0.89f; //Need this?
	public final float AIR_INERTIA = 0.89f; // Need this?
	// Mario dimensions
	public final int marioWidth = 4;
	// Make small mario and ducking mario compatible with height
	public final int marioHeight = 24;
	// General dimensions
	public final int screenWidth = GlobalOptions.VISUAL_COMPONENT_WIDTH;
	public final int screenHeight = GlobalOptions.VISUAL_COMPONENT_HEIGHT;
	public final int cellSize = LevelScene.cellSize;
	public float[] marioFloatPos;
	// Map
	public byte[][] mergedObservation;
	private byte[][] map = new byte[19][600];
	private int mapX = 0;
	private float highestX = 0;
	// DEBUG
	public boolean debug = false;
	// TEMPORARY
	public static byte[] TILE_BEHAVIORS = Level.TILE_BEHAVIORS;
	public static final int BIT_BLOCK_UPPER = 1 << 0;
	public static final int BIT_BLOCK_ALL = 1 << 1;
	public static final int BIT_BLOCK_LOWER = 1 << 2;
	public static final int BIT_SPECIAL = 1 << 3;
	public static final int BIT_BUMPABLE = 1 << 4;
	public static final int BIT_BREAKABLE = 1 << 5;
	public static final int BIT_PICKUPABLE = 1 << 6;
	public static final int BIT_ANIMATED = 1 << 7;
	
	public void updateMap(byte[][] mergedObservation) {
		this.mergedObservation = mergedObservation;
	}
	public void predictFuture(State state)
	{
	    float sideWaysSpeed = state.action[Mario.KEY_SPEED] ? 1.2f : 0.6f;
	    /*// FOR DUCKING
	    if (state.onGround)
	    {
	        ducking = keys[KEY_DOWN] && large;
	    }
	    */
	    
	    if (state.action[Mario.KEY_JUMP] || (state.jumpTime < 0 && !state.onGround && !state.sliding))
	    {
	    	
	        if (state.jumpTime < 0)
	        {
	            state.xa = xJumpSpeed;
	            state.ya = -state.jumpTime * yJumpSpeed;
	            state.jumpTime++;
	        } else if (state.onGround && state.mayJump)
	        {
	            xJumpSpeed = 0;
	            yJumpSpeed = -1.9f;
	            state.jumpTime = 7;
	            state.ya = state.jumpTime * yJumpSpeed;
	            state.onGround = false;
	            state.sliding = false;
	     
	        } else if (state.jumpTime > 0)
	        {
	            state.xa += xJumpSpeed;
	            state.ya = state.jumpTime * yJumpSpeed;
	            state.jumpTime--;
	            
	        } else if(state.jumpTime == 0) { //  SELF-MADE
	        	state.action[Mario.KEY_JUMP] = false;
	        } 
	    } else
	    {
	        state.jumpTime = 0;
	        
	    }

	    if (state.action[Mario.KEY_LEFT]/* && !ducking*/)
	    {
	        state.xa -= sideWaysSpeed;
	    }

	    if (state.action[Mario.KEY_RIGHT] /*&& !ducking*/)
	    {
	        state.xa += sideWaysSpeed;
	    }

	    
 		
	    /*//FIREBALLS
	    if (keys[KEY_SPEED] && ableToShoot && Mario.fire && levelScene.fireballsOnScreen < 2)
	    {
	        levelScene.addSprite(new Fireball(levelScene, x + facing * 6, y - 20, facing));
	    }
	    ableToShoot = !keys[KEY_SPEED];
	    */

	    state.mayJump = (state.onGround || state.sliding) && !state.action[Mario.KEY_JUMP];
	    /*// WHAT IS RUNTIME?!
	    runTime += (Math.abs(state.xa)) + 5;
	    if (Math.abs(state.xa) < 0.5f)
	    {
	       // runTime = 0;
	        state.xa = 0;
	    }
	    */
	    state.onGround = false;
	    move(state, state.xa, 0);
	    move(state, 0, state.ya);
	
	    
	     //GAPS - VERY IMPORTANT!
	    if (state.y > LevelScene.level.height * LevelScene.cellSize + LevelScene.cellSize)
	        state.penalty(1000);
	     
	    if (state.x < 0)
	    {
	        state.x = 0;
	        state.xa = 0;
	    }
	    state.ya *= 0.85f;
	    if (state.onGround)
	    {
	        state.xa *= (GROUND_INERTIA);
	    } else
	    {
	        state.xa *= (AIR_INERTIA);
	    }

	    if (!state.onGround)
	    {
	        state.ya += 3;
	    }
	}
	private boolean move(State state, float xa, float ya) {
		
		while (xa > 8)
	    {
	        if (!move(state, 8, 0)) return false;
	        xa -= 8;
	    }
	    while (xa < -8)
	    {
	        if (!move(state, -8, 0)) return false;
	        xa += 8;
	    }
	    while (ya > 8)
	    {
	        if (!move(state, 0, 8)) return false;
	        ya -= 8;
	    }
	    while (ya < -8)
	    {
	        if (!move(state, 0, -8)) return false;
	        ya += 8;   
	    }
	    boolean collide = false;
	    if (ya > 0)
	    {	
	        if (isBlocking(state, state.x + xa - marioWidth, state.y + ya, xa, 0)) collide = true;
	        else if (isBlocking(state, state.x + xa + marioWidth, state.y + ya, xa, 0)) collide = true;
	        else if (isBlocking(state, state.x + xa - marioWidth, state.y + ya + 1, xa, ya)) collide = true;
	        else if (isBlocking(state, state.x + xa + marioWidth, state.y + ya + 1, xa, ya)) collide = true;
	    }
	    if (ya < 0)
	    {	
	        if (isBlocking(state, state.x + xa, state.y + ya - marioHeight, xa, ya)) collide = true;
	        else if (collide || isBlocking(state, state.x + xa - marioWidth, state.y + ya - marioHeight, xa, ya)) collide = true;
	        else if (collide || isBlocking(state, state.x + xa + marioWidth, state.y + ya - marioHeight, xa, ya)) collide = true;
	    }
	    if (xa > 0)
	    {
	    	if (isBlocking(state, state.x + xa + marioWidth, state.y + ya - marioHeight, xa, ya)) collide = true;
	        if (isBlocking(state, state.x + xa + marioWidth, state.y + ya - marioHeight / 2, xa, ya)) collide = true;
	        if (isBlocking(state, state.x + xa + marioWidth, state.y + ya, xa, ya)) collide = true; 
	    }
	    if (xa < 0)
	    {
	        if (isBlocking(state, state.x + xa - marioWidth, state.y + ya - marioHeight, xa, ya)) collide = true;
	        if (isBlocking(state, state.x + xa - marioWidth, state.y + ya - marioHeight / 2, xa, ya)) collide = true;
	        if (isBlocking(state, state.x + xa - marioWidth, state.y + ya, xa, ya)) collide = true;
	    }
		
	    if (collide)
	    {
	        if (xa < 0)
	        {
	            state.x = (int) ((state.x - marioWidth) / 16) * 16 + marioWidth;
	            state.xa = 0;
	        }
	        if (xa > 0)
	        {
	            state.x = (int) ((state.x + marioWidth) / 16 + 1) * 16 - marioWidth - 1;
	            state.xa = 0;
	        }
	        if (ya < 0)
	        {
	        	
	            state.y = (int) ((state.y - marioHeight) / 16) * 16 + marioHeight;
	            jumpTime = 0;
	            state.ya = 0;
	        }
	        if (ya > 0)
	        {
	            state.y = (int) ((state.y - 1) / 16 + 1) * 16 - 1;
	            state.onGround = true;
	        }
	        return false;
	    } else
	    {
	        state.x += xa;
	        state.y += ya;
	        return true;
	    }
	}

	private boolean isBlocking(State state, final float _x, final float _y, final float xa, final float ya)
	{
		int x = (int) (_x / 16);
	    int y = (int) (_y / 16);
	    state.xGrid = (int) (((_x - marioFloatPos[0] +4) / cellSize) + 3);	    	
	    if (x == (int) (state.x / 16) && y == (int) (state.y / 16)) return false;
	    /*  // CHEATER COLLISION!
	    byte block = LevelScene.level.getBlock(x, y);
	    boolean blocking = ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_ALL) > 0;
	    blocking |= (ya > 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_UPPER) > 0;
	    blocking |= (ya < 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_LOWER) > 0;
	    return blocking;
	    */
	    
	    if(state.xGrid >= 0 && state.xGrid < 19 && y >= 0 && y < 16) {
	    	byte block = map[y][state.xGrid];
	    	if(ya < 0){
	    		if(block == -62) {
	    			return false;
	    		} 
	    	}
	    	return block < 0;
	    } else {
	    	return false;
	    }  
	    
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
	public void setScene(byte[][] levelScene, float[] marioFloatPos) {
		this.marioFloatPos = marioFloatPos;

		boolean same = true;
		for(int i = 0; i < 19; i++){
			for(int j = 0; j < 19; j++){
				if(map[i][j] != levelScene[i][j]){
					same = false;
				}
			}
		}
		
		if(!same){
		for(int i = 0; i < 19; i++) {
			for(int j = 0; j < 19; j++) {
				this.map[i][j] = levelScene[i][j];
			}
		}
		mapX = 18;
		}
	}
	public void toScene(float x) {
		if(x > highestX) {
			highestX = x;
			if((int) (highestX / 16) > mapX - 9) {
				for(int i = 0; i < 19; i++) {
					map[i][mapX] = mergedObservation[i][18];
				}
				mapX++;
			}
		}
	}
	public void addToScene(byte[] sceneArr) {
		for(int i = 0; i < 19; i++) {
			map[i][mapX] = sceneArr[i];
		}
		mapX++;
		
	}
	
}
