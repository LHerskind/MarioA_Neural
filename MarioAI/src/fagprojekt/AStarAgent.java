package fagprojekt;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.mario.engine.LevelScene;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

public class AStarAgent extends BasicMarioAIAgent implements Agent{
	// General dimensions
	public final int screenWidth = GlobalOptions.VISUAL_COMPONENT_WIDTH;
	public final int screenHeight = GlobalOptions.VISUAL_COMPONENT_HEIGHT;
	public final int cellSize = LevelScene.cellSize;
	public final int maxRight = 176;
	public final int searchDepth = maxRight;
	public boolean firstScene = true;
	
	public int debugPos;
	private CustomEngine ce;
	
	// Need-to-know states persisting for each tick
	public int prevJumpTime;
	public float prevXa;
	public float prevYa;
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
		public int g;
		
		// initial state
		public State() {

			initValues();
			penalty = 0;
			parent = null;
			action = null;
			heuristic = (int) ((searchDepth + 10) - (x - marioFloatPos[0]));
			//heuristic = 18 - this.xGrid;
			
			
		}
		public void initValues() {
			onGround = isMarioOnGround;
			mayJump = isMarioAbleToJump;
			x = marioFloatPos[0];
			y = marioFloatPos[1];
			this.xGrid = 9;
			this.yGrid = 9;
			
			penalty = 0;
			this.g = 0;
			
			jumpTime = prevJumpTime;
			xa = prevXa;
			ya = prevYa;
		}
		public State(State parent, boolean[] action) {
			this.action = action;
			this.parent = parent;
			this.onGround = parent.onGround;
			this.mayJump = parent.mayJump;
			this.xa = parent.xa;
			this.ya = parent.ya;
			this.x = parent.x;
			this.y = parent.y;
			this.jumpTime = parent.jumpTime;

			this.g = parent.g + 2;
		
			penalty = 0;
			// Sets all relevant values for the state
			ce.predictFuture(this);
			// TODO - HEURISTIC NEEEEEDS TO BE AMOUNT OF TICKS TO GOAL, BASED ON HIS MAXIMUM SPEED!!!!!!!
			heuristic = ((searchDepth + 10) - (int)(x - marioFloatPos[0]));
			
			
			// grid heuristic: doesn't work currently
//			this.xGrid = (int) ((x - marioFloatPos[0]) / cellSize + 9);
//			this.yGrid = (int) ((y - marioFloatPos[1]) / cellSize + 9);
//			heuristic = 18 - this.xGrid;
			
			
		}
		public int penalty() {
			return 0;
		}
		public boolean isLegalMove() {
			return xGrid > 0 && yGrid > 0 && xGrid < 18 && yGrid < 18;
		}
		public int priority() {
			return heuristic + g + penalty;
		}
		public void penalty(int amount) {
			penalty += amount;
		}
		public boolean isGoal() {
			if (x > marioFloatPos[0]+searchDepth) {
				return true;
			}
			return false;
        }
		
		State moveNE() {return new State(this, createAction(false,true,true,false));}
		State SmoveNE() {return new State(this, createAction(false,true,true,true));}
		State moveE() {return new State(this, createAction(false,true,false,false)); }  
		State SmoveE() {return new State(this, createAction(false,true,false,true)); }    
		State moveN() { return new State(this, createAction(false,false,true,false));}
		State SmoveN() { return new State(this, createAction(false,false,true,true));}
		State moveNW() {return new State(this, createAction(true,false,true,false));}  
		State SmoveNW() {return new State(this, createAction(true,false,true,true));}  
		State moveW() { return new State(this, createAction(true,false,false,false)); }
		State SmoveW() { return new State(this, createAction(true,false,false, true)); }
		State still() { return new State(this, createAction(false,false,false,false)); }
	}
	private boolean[] createAction(boolean left, boolean right, boolean jump, boolean speed) {
		boolean[] action = new boolean[6];
		action[Mario.KEY_JUMP] = jump;
		action[Mario.KEY_LEFT] = left;
		action[Mario.KEY_RIGHT] = right;
		action[Mario.KEY_SPEED] = speed;
		return action;
	}
	
	public State getRootState(State state) {
		
		if(state.parent.parent != null) {
			//System.out.println(state.x + "  " + state.y); // Track the solve path
			
			if (debugPos < 600)
            {
                GlobalOptions.Pos[debugPos][0] = (int) state.x;
                GlobalOptions.Pos[debugPos][1] = (int) state.y;
                debugPos++;
            }
			return getRootState(state.parent);
		} else{
			//ce.printOnGoing(state.x, state.y);
			return state;
		}
	}
	public void addSuccessor(State successor) {
		if (successor != null && !closed.contains(successor)) 
			openSet.add(successor);
	}
	public State solve() {
		long startTime = System.currentTimeMillis();
        openSet.clear();
        closed.clear();
        
        // FOR DEBUGGING
        GlobalOptions.Pos = new int[600][2];
		for(int i = 0; i <600; i++)
		{
			GlobalOptions.Pos[i][0] = 0;
			GlobalOptions.Pos[i][1] = 0;
		}
    	debugPos = 0;
    	
        // Add initial state to queue.
        State initial = new State();
        openSet.add(initial);

        while (!openSet.isEmpty()) {
            // Get the lowest priority state.
        	//System.out.println("OPEN: " + openSet.size());
        	//System.out.println("CLOSED: " + closed.size());
            State state = openSet.poll();  
            
            // If it's the goal, we're done.
            if (state.isGoal()) {
            	return getRootState(state);
            }
            // Debugging for being stuck in loop
            if(System.currentTimeMillis() - startTime > 25) {
            	System.out.println("stuck in while-loop");
            	return getRootState(state);
            }
            

            // Make sure we don't revisit this state.
            closed.add(state);

            // Add successors to the queue.
            addSuccessor(state.moveE());
            addSuccessor(state.moveNE());
            /*
            addSuccessor(state.moveNW());
            addSuccessor(state.SmoveNE());
            addSuccessor(state.still());
            addSuccessor(state.SmoveN());
            addSuccessor(state.moveN());
            addSuccessor(state.SmoveE());
            addSuccessor(state.moveW());
            addSuccessor(state.SmoveNW());
            addSuccessor(state.SmoveW());
           */
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
		// DEBUG
		/*//mergedObservation
		for(int i = 0; i < mergedObservation.length; i++) {
			for(int j = 0; j < mergedObservation[i].length; j++) {
				if(i == 9 && j == 9) {
					System.out.print("M" + " ");
				}
				else System.out.print(mergedObservation[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
		*/
		//levelScene in grid
		/*
		for(int i = 0; i < levelScene.length; i++) {
			for(int j = 0; j < levelScene[i].length; j++) {
				System.out.print(levelScene[i][j] + "\t");
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
		//ce.updateMap(mergedObservation);
		System.out.println(marioFloatPos[0] + " " + (int) marioFloatPos[0]/16); 

		ce.setScene(levelScene, marioFloatPos);
		
		State bestState = solve();
		prevJumpTime = bestState.jumpTime;
		prevXa = bestState.xa;
		prevYa = bestState.ya;

		
		return bestState.action;
	}
	@Override
	public void integrateObservation(Environment environment) {
		this.marioFloatPos = environment.getMarioFloatPos();
		this.enemiesFloatPos = environment.getEnemiesFloatPos();
		this.marioState = environment.getMarioState();
				
		levelScene = environment.getLevelSceneObservationZ(1, 2, (int) marioFloatPos[1] / 16);
		enemies = environment.getEnemiesObservationZ(0);
		mergedObservation = environment.getMergedObservationZZ(1, 0);

		receptiveFieldWidth = environment.getReceptiveFieldWidth();
		receptiveFieldHeight = environment.getReceptiveFieldHeight();

		marioStatus = marioState[0];
		marioMode = marioState[1];
		isMarioOnGround = marioState[2] == 1;
		isMarioAbleToJump = marioState[3] == 1;
		isMarioAbleToShoot = marioState[4] == 1;
		isMarioCarrying = marioState[5] == 1;
		getKillsTotal = marioState[6];
		getKillsByFire = marioState[7];
		getKillsByStomp = marioState[8];
		getKillsByShell = marioState[9];
	}

	public String getName() { return name; }

	public void setName(String Name) { this.name = Name; }
}

