package fagprojekt;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.mario.engine.LevelScene;
import ch.idsia.benchmark.mario.engine.sprites.Mario;

public class AStarAgent extends BasicMarioAIAgent implements Agent{
	// General dimensions
	public final int screenWidth = GlobalOptions.VISUAL_COMPONENT_WIDTH;
	public final int screenHeight = GlobalOptions.VISUAL_COMPONENT_HEIGHT;
	public final int cellSize = LevelScene.cellSize;
	
	private final int maxRight = 18;
	private final int maxJumpFrames = 16;
	private CustomEngine ce;
	private int jumpFrame;
    // The closed state set.
    private HashSet <State> closed = new HashSet <State>();
	private PriorityQueue <State> openSet = new PriorityQueue<State>(100, new Comparator<State>() {
		@Override
		public int compare(State a, State b) { 
			return a.priority()-b.priority();
		}
	});
	public AStarAgent() {
		super("AStarAgent");
	    reset();
	}
	
	//----State-----
	public class State {
		public int xGrid;
		public int yGrid;
		public float x;
		public float y;
		public float xa;
		public float ya;
		
		public int jumpTime;
		
		public boolean onGround;
		public boolean sliding; // Not needed
		public boolean mayJump;
		
		public int penalty;
		public State parent;
		public int heuristic;
		public boolean[] action;
		
		public int tempJumpFrame;
		
		// initial state
		public State(int xGrid, int yGrid) {
			this.xGrid = xGrid;
			this.yGrid = yGrid;
			// 0,0 in pixels is always top left of screen
			x = xGrid * cellSize;
			y = yGrid * cellSize;
			
			initValues();
			penalty = 0;
			heuristic = maxRight - xGrid;
			parent = null;
			action = null;
			tempJumpFrame = jumpFrame;
			
			
		}
		public void initValues() {
			// Are these needed?
			onGround = isMarioOnGround;
			mayJump = isMarioAbleToJump;
			penalty = 0;
			// WHAT TO DO WITH xa, ya, jumpTime?!?!
		}
		public State(State parent, boolean[] action) {
			//if(cost!=0)System.out.println(cost);
			this.xGrid = parent.xGrid;
			this.yGrid = parent.yGrid;
			this.parent = parent;
			this.action = action;
			
			this.onGround = parent.onGround;
			this.mayJump = parent.mayJump;
			this.x = parent.x;
			this.y = parent.y;
			this.jumpTime = parent.jumpTime;
			penalty = penalty();
			//if(isMarioOnGround && action[Mario.KEY_JUMP]) action[Mario.KEY_JUMP]=false;
			//System.out.println(priority());
			if(action != null && action[Mario.KEY_JUMP]) tempJumpFrame = parent.tempJumpFrame+1;
			// Sets all relevant values for the state
			ce.predictFuture(this);
			heuristic = (int) (screenWidth-x-25);
		}
		
		public int penalty() {
			//System.out.print(x + "  " + y + "  ");
			//System.out.println();
			if(isLegalMove() && enemies[yGrid][xGrid] != 0) {
				//System.out.println(x + "  " + y + "  " + "Enemy here");
				return 1000;
			}
			else return 0;
		}
		public boolean isLegalMove() {
			return xGrid > 0 && yGrid > 0 && xGrid < 18 && yGrid < 18;
		}
		public int priority() {
			//if(cost!=0)System.out.println(cost);
			return heuristic + penalty;
		}
		public boolean isGoal() {
			if(heuristic <= 0) {
				return true;
			}
			return false;
        }
		
		State moveE() {return levelScene[yGrid][xGrid+1] == 0 && levelScene[yGrid+1][xGrid+1]!=0 ? new State(this, createAction(false,true,false,false)) : null; }  
		State SmoveE() {return levelScene[yGrid][xGrid+1] == 0 ? new State(this, createAction(false,true,false,true)) : null; }    
		State moveNE() {return tempJumpFrame < maxJumpFrames/2 ?  new State(this, createAction(false,true,true,false)) : null;}
		State SmoveNE() {return tempJumpFrame < maxJumpFrames/2 ? new State(this, createAction(false,true,true,true)): null;}
		State moveN() { return new State(this, createAction(false,false,true,false));}
		State SmoveN() { return new State(this, createAction(false,false,true,true));}
		State moveNW() {return new State(this, createAction(true,false,true,false));}  
		State SmoveNW() {return new State(this, createAction(true,false,true,true));}  
		State moveW() { return levelScene[yGrid][xGrid-1] == 0 ? new State(this, createAction(true,false,false,false)) : null; }
		State SmoveW() { return levelScene[yGrid][xGrid-1] == 0  ? new State(this, createAction(true,false,false, true)) : null; }
		State still() { return new State(this, createAction(false,false,false,false)); }
	}
	
	//----State---- END
	
	
	
	private boolean[] createAction(boolean left, boolean right, boolean jump, boolean speed) {
		boolean[] action = new boolean[6];
		action[Mario.KEY_JUMP] = jump;
		action[Mario.KEY_LEFT] = left;
		action[Mario.KEY_RIGHT] = right;
		action[Mario.KEY_SPEED] = speed;
		return action;
	}
	
	public boolean [] getRootAction(State state) {
		if(state.parent.parent != null) {
			//System.out.println(state.x + "  " + state.y); // Track the solve path
			return getRootAction(state.parent);
		} else{
			return state.action;
		}
	}
	public void addSuccessor(State successor) {
		if (successor != null && !closed.contains(successor)) 
			openSet.add(successor);
	}
	public boolean[] solve() {
		long startTime = System.currentTimeMillis();
        openSet.clear();
        closed.clear();
        // Add initial state to queue.
        State initial = new State(marioEgoCol, marioEgoRow);
        openSet.add(initial);

        while (!openSet.isEmpty()) {
            // Get the lowest priority state.
        	//System.out.println("OPEN: " + openSet.size());
        	//System.out.println("CLOSED: " + closed.size());
            State state = openSet.poll();  
           // System.out.println(state.y);
            
            // If it's the goal, we're done.
            if (state.isGoal()) {
            	
            	// GET THE ACTION ARRAY HERE!
            	/*
            	System.out.println("rootaction: ");
            	for(int i = 0; i < getRootAction(state).length; i++) {
            		System.out.print(action[i] + " ");
            	}
            	System.out.println();
            	
            	System.out.println("regular action: ");
            	for(int i = 0; i < action.length; i++) {
            		System.out.print(action[i] + " ");
            	}
            	System.out.println();
            	*/
            	/*
            	System.out.println("printing");
            	state.printAll();
            	System.out.println("rootpath:");
            	*/
            	return getRootAction(state);
            }
            
            if(System.currentTimeMillis() - startTime > 30) {
            	System.out.println("stuck in while loop");
            	return new boolean[]{false,false,false,false,false,false};
            }
            

            // Make sure we don't revisit this state.
            closed.add(state);

            // Add successors to the queue.
           
            addSuccessor(state.moveNE());
            addSuccessor(state.moveE());
            addSuccessor(state.SmoveE());
            addSuccessor(state.SmoveNE());
            addSuccessor(state.SmoveN());
            addSuccessor(state.moveN());
            addSuccessor(state.SmoveNW());
            addSuccessor(state.moveNW());
            addSuccessor(state.SmoveW());
            addSuccessor(state.moveW());
            addSuccessor(state.still());
           
        }
        return null;
	}
	@Override
	public void reset()
	{
	    for (int i = 0; i < action.length; ++i)
	        action[i] = false;
	    ce = new CustomEngine();
	}
	
	public boolean[] getAction() {
		// TODO - Write the AI code in here
		
		// DEBUG
		/*//mergedObservation
		for(int i = 0; i < mergedObservation.length; i++) {
			for(int j = 0; j < mergedObservation[i].length; j++) {
				System.out.print(mergedObservation[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
		*/
		//Enemies location
		//for(int i = 0; i < enemiesFloatPos.length; i++) {
		//	System.out.println(enemiesFloatPos[i]);
		//}
		//System.out.println();
		
		//if(enemiesFloatPos.length > 0) System.out.println(enemiesFloatPos[1]); // Enemies[0] x location
		 
		
		/* Mario States
		for(int i = 0; i < marioState.length; i++) {
			System.out.print(marioState[i] + "  ");
		}
		System.out.println();
		*/
		
		/* mario position, with [0]=x, and [1]=y
		for(int i = 0; i < marioFloatPos.length; i++) {
			System.out.println(marioFloatPos[i]);
		}
		System.out.println();
		
		*/ 
		/*//levelScene in grid
		
		for(int i = 0; i < levelScene.length; i++) {
			for(int j = 0; j < levelScene[i].length; j++) {
				System.out.print(levelScene[i][j]);
			}
			System.out.println();
		}
		System.out.println();
		*/
		
		/*  //enemies in grid
		System.out.println("enemy: ");
		for(int i = 0; i < enemies.length; i++) {
			for(int j = 0; j < enemies[i].length; j++) {
				if(enemies[i][j] != 0)
				System.out.print(i + "  " + j);
			}
			//System.out.println();
		}
		//System.out.println();
		*/
		if(isMarioAbleToJump ) jumpFrame = 0;
		else if(action[Mario.KEY_JUMP]) jumpFrame++;
		//System.out.println(jumpFrame);
		//System.out.println();
		ce.updateMap(mergedObservation);
		action = solve();
		return action;
		
	}


	public String getName() { return name; }

	public void setName(String Name) { this.name = Name; }
}

