package fagprojekt;

import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.mario.engine.LevelScene;
import ch.idsia.benchmark.mario.engine.level.Level;
import ch.idsia.benchmark.mario.engine.sprites.Fireball;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.engine.sprites.Sparkle;
import fagprojekt.AStarAgent.State;

public class CustomEngine {
	// Jumping
	public final int jumpPower = 7;
	public int jumpTime = jumpPower;
	public float jT; // What is this?
	public float xJumpSpeed;
	public float yJumpSpeed;
	// Gravity and friction
	public final float marioGravity = 1.0f;
	public final float GROUND_INERTIA = 0.89f; //Need this?
	public final float AIR_INERTIA = 0.89f; // Need this?
	// Acceleration
	public float xa;
	public float ya;
	// Mario boolean states
	public boolean wasOnGround; // Need this?
	public boolean onGround;
	// Mario dimensions
	public final int marioWidth = 4;
	public final int marioHeightL = 24;
	public final int marioHeightS = 12;
	// General dimensions
	public final int width = GlobalOptions.VISUAL_COMPONENT_WIDTH;
	public final int height = GlobalOptions.VISUAL_COMPONENT_HEIGHT;
	public final int cellSize = LevelScene.cellSize;
	// End right of screen
	public final int maxRight = 18;
	public final int realMaxRight = width/2;
	// Map
	private byte[][] mergedObservation;
	
	public void updateMap(byte[][] mergedObservation) {
		this.mergedObservation = mergedObservation;
	}
	public void predictFuture(State state, byte[][] mergedObs)
	{
	    //state.wasonGround = state.onGround; // What is this used for?
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
	        }
	    } else
	    {
	        state.jumpTime = 0;
	    }

	    if (state.action[Mario.KEY_LEFT]/* && !ducking*/)
	    {
	        state.xa -= sideWaysSpeed;
	        //if (state.jumpTime >= 0) facing = -1;
	    }

	    if (state.action[Mario.KEY_RIGHT] /*&& !ducking*/)
	    {
	        state.xa += sideWaysSpeed;
	        //if (state.jumpTime >= 0) facing = 1;
	    }

	    
 		
	    /*//FIREBALLS
	    if (keys[KEY_SPEED] && ableToShoot && Mario.fire && levelScene.fireballsOnScreen < 2)
	    {
	        levelScene.addSprite(new Fireball(levelScene, x + facing * 6, y - 20, facing));
	    }
	    */
	    //ableToShoot = !keys[KEY_SPEED];

	    state.mayJump = (state.onGround || state.sliding) && !state.action[Mario.KEY_JUMP];

	    //runTime += (Math.abs(state.xa)) + 5; //What is runTime?
	    if (Math.abs(state.xa) < 0.5f)
	    {
	       // runTime = 0;
	        state.xa = 0;
	    }
	    state.onGround = false;
	    move(state, state.xa, 0);
	    move(state, 0, state.ya);
	    /* GAPS - VERY IMPORTANT!
	    if (state.y > levelScene.level.height * LevelScene.cellSize + LevelScene.cellSize)
	        die("Gap");
	*/
	    if (state.x < 0)
	    {
	        state.x = 0;
	        state.xa = 0;
	    }
	    /*// WIN???
	    if (mapX >= levelScene.level.xExit && mapY <= levelScene.level.yExit)
	    {
	        x = (levelScene.level.xExit + 1) * LevelScene.cellSize;
	        win();
	    }
	     
	    if (x > levelScene.level.length * LevelScene.cellSize)
	    {
	        x = levelScene.level.length * LevelScene.cellSize;
	        state.xa = 0;
	    }
		*/
	    state.ya *= 0.85f;
	    if (state.onGround)
	    {
	        state.xa *= (GROUND_INERTIA);
	    } else
	    {
	        state.xa *= (AIR_INERTIA);
	    }

//	    if /

	    if (!state.onGround)
	    {
//	        
	        state.ya += 3;
	    }
	}
	
//	---Predict Futre--- END
	
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
	        if (isBlocking(state, mergedObservation, state.x + xa - width, state.y + ya, xa, 0)) collide = true;
	        else if (isBlocking(state, mergedObservation, state.x + xa + width, state.y + ya, xa, 0)) collide = true;
	        else if (isBlocking(state, mergedObservation, state.x + xa - width, state.y + ya + 1, xa, ya)) collide = true;
	        else if (isBlocking(state, mergedObservation, state.x + xa + width, state.y + ya + 1, xa, ya)) collide = true;
	    }
	    if (ya < 0)
	    {
	        if (isBlocking(state, mergedObservation, state.x + xa, state.y + ya - height, xa, ya)) collide = true;
	        else if (collide || isBlocking(state, mergedObservation, state.x + xa - width, state.y + ya - height, xa, ya)) collide = true;
	        else if (collide || isBlocking(state, mergedObservation, state.x + xa + width, state.y + ya - height, xa, ya)) collide = true;
	    }
	    if (xa > 0)
	    {
	        state.sliding = true;
	        if (isBlocking(state, mergedObservation, state.x + xa + width, state.y + ya - height, xa, ya)) collide = true;
	        else state.sliding = false;
	        if (isBlocking(state, mergedObservation, state.x + xa + width, state.y + ya - height / 2, xa, ya)) collide = true;
	        else state.sliding = false;
	        if (isBlocking(state, mergedObservation, state.x + xa + width, state.y + ya, xa, ya)) collide = true;
	        else state.sliding = false;
	    }
	    if (xa < 0)
	    {
	        state.sliding = true;
	        if (isBlocking(state,mergedObservation, state.x + xa - width, state.y + ya - height, xa, ya)) collide = true;
	        else state.sliding = false;
	        if (isBlocking(state, mergedObservation, state.x + xa - width, state.y + ya - height / 2, xa, ya)) collide = true;
	        else state.sliding = false;
	        if (isBlocking(state, mergedObservation, state.x + xa - width, state.y + ya, xa, ya)) collide = true;
	        else state.sliding = false;
	    }

	    if (collide)
	    {
	        if (xa < 0)
	        {
	            state.x = (int) ((state.x - width) / 16) * 16 + width;
	            this.xa = 0;
	        }
	        if (xa > 0)
	        {
	            state.x = (int) ((state.x + width) / 16 + 1) * 16 - width - 1;
	            this.xa = 0;
	        }
	        if (ya < 0)
	        {
	            state.y = (int) ((state.y - height) / 16) * 16 + height;
	            jumpTime = 0;
	            this.ya = 0;
	        }
	        if (ya > 0)
	        {
	            state.y = (int) ((state.y - 1) / 16 + 1) * 16 - 1;
	            onGround = true;
	        }
	        return false;
	    } else
	    {
	        state.x += xa;
	        state.y += ya;
	        return true;
	    }
	}
	private boolean isBlocking(State state, byte[][]mergedObservation, final float _x, final float _y, final float xa, final float ya)
	{
	    int x = (int) (_x / cellSize);
	    int y = (int) (_y / cellSize);
	    if (x == (int) (state.x / cellSize) && y == (int) (state.y / cellSize)) return false;
	    //LEVELSCENE?!?!?!?!
	    //boolean blocking = levelScene.level.isBlocking(x, y, xa, ya); 
	    byte block = mergedObservation[x][y];
	    /*  // COIN PICKUP - SET THE CORRESPONDING TILE TO 0
	    if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_PICKUPABLE) > 0)
	    {
	        Mario.gainCoin();
	        levelScene.level.setBlock(x, y, (byte) 0);
	        for (int xx = 0; xx < 2; xx++)
	            for (int yy = 0; yy < 2; yy++)
	                levelScene.addSprite(new Sparkle(x * cellSize + xx * 8 + (int) (Math.random() * 8), y * cellSize + yy * 8 + (int) (Math.random() * 8), 0, 0, 0, 2, 5));
	    }
	     */
	    /*// WTF IS THIS?!?!?!
	    if (blocking && ya < 0)
	    {
	        levelScene.bump(x, y, large);
	    }
	     */
	    //return blocking;
	    return false;
	}
}
