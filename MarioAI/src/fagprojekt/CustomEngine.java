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
	public float jT; // What is this?
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
	// End right of screen
	public final int maxRight = 18;
	public final int realMaxRight = screenWidth/2;
	// Map
	public byte[][] mergedObservation;
	public static byte[] TILE_BEHAVIORS = Level.TILE_BEHAVIORS;
	// TEMPORARY
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
	            
	        } /*else if(state.jumpTime == 0) { //  SELF-MADE
	        	state.action[Mario.KEY_JUMP] = false;
	        } */
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
	    //if(isBlocking(state, state.x, state.y, xa, ya)) collide =true;
	    
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
	            //state.x = (int) ((state.x + marioWidth) / 16 + 1) * 16 - marioWidth - 1;
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
	            //state.y = (int) ((state.y - 1) / 16 + 1) * 16 - 1;
	            state.onGround = true;
	        }
	        //System.out.println("x: " + state.x + "  " + "y: " + state.y);
	        
	        return false;
	    } else
	    {

	    	//System.out.println(xa);
	        state.x += xa;
	        //if(ya != 0) System.out.println(ya);
	        state.y += ya;
	        //System.out.println(state.y);
	        return true;
	    }
	}
	private boolean isBlocking(State state, final float _x, final float _y, final float xa, final float ya)
	{
		state.xGrid = (int) (_x - state.xOriginal) / cellSize + 9;
		state.yGrid = (int) (_y - state.yOriginal) / cellSize + 9;
		
		
	   // System.out.println("x: " + x + "  " + "y: " + y);
	    //System.out.println("x after: " + x);
	    //System.out.println(x);
	    
	    //LEVELSCENE?!?!?!?!
	    //boolean blocking = levelScene.level.isBlocking(x, y, xa, ya); 
		//if (state.xGrid == (int) (state.x / 16) && state.yGrid == (int) (state.y / 16)) return false;
	    if(state.xGrid < 0 || state.yGrid < 0 || state.xGrid > 18 || state.yGrid > 18) {
	    	
	    	//System.out.println("NOT LEGAL!: ");
	    	return false; 
	    }
	    
	    byte block = mergedObservation[state.yGrid][state.xGrid];
	    //System.out.println(block);
	    /*
	    boolean blocking = ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_ALL) > 0;
	    blocking |= (ya > 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_UPPER) > 0;
	    blocking |= (ya < 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_LOWER) > 0;
	    */
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
//	    if(block != 0) System.out.println(block);
	    

	    //if(block != 0) System.out.println(block != 0);
	    //System.out.println((block != 0) || (block != 2));
	    //System.out.println(state.xGrid + " " + state.yGrid + "  " + (block!=0));
	   
	    //return false;
	    return ((block != 0) && (block != 2));
	}
}
