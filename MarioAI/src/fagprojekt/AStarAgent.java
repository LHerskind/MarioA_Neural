package fagprojekt;

import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.mario.engine.LevelScene;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

public class AStarAgent extends BasicMarioAIAgent implements Agent {
	// General dimensions
	public final int screenWidth = GlobalOptions.VISUAL_COMPONENT_WIDTH;
	public final int screenHeight = GlobalOptions.VISUAL_COMPONENT_HEIGHT;
	public final int cellSize = LevelScene.cellSize;
	public final int maxRight = 16 * 11;
	public final int searchDepth = maxRight;
	public boolean firstScene = true;

	private int speedPriority = 9;


	private int numberOfStates = 100000;
	private State[] stateArray = new State[numberOfStates];
	private int indexStateArray = 0;
	// enemies
	private int estimatedMaxSearchDepth = 100;
	private int maxNumberOfEnemies = 35;
	//private Enemy[][] enemyArray = new Enemy[numberOfStates][maxNumberOfEnemies];

	public int debugPos;
	private CustomEngine ce;

	// Need-to-know states persisting for each tick
	public int prevJumpTime;
	public float prevXa;
	public float prevYa;

	private HashMap<Integer, State> closed = new HashMap<>();

	private PriorityQueue<State> openSet = new PriorityQueue<State>(100, new Comparator<State>() {
		@Override
		public int compare(State a, State b) {
			return a.priority() - b.priority();
		}
	});

	public AStarAgent() {
		super("AStarAgent");
		for (int i = 1; i < numberOfStates; i++) {
			State state = new State();
			/*
			for(int j = 0; j < maxNumberOfEnemies; j++) {
				enemyArray[i][j] = new Enemy();
			}
			//state.enemyList = enemyArray[i];
*/
			stateArray[i] = state;
		}
		reset();
	}

	public class State {
		public float x;
		public float y;
		public float xa;
		public float ya;
		public int jumpTime;
		public int height;

		public boolean onGround;
		public boolean sliding; // Not needed
		public boolean mayJump;
		
		public int penalty;
		public State parent;
		public int heuristic;
		public boolean[] action;
		public int g;
		
		//enemy stuff
		public Enemy[] enemyList;

		public State() {
		}

		public State(boolean lala) {
			initValues();
			penalty = 0;
			parent = null;
			action = null;
			heuristic = (int) ((searchDepth + 10) - (x - marioFloatPos[0]));
		}
		public void initValues() {
			this.onGround = isMarioOnGround;
			this.mayJump = isMarioAbleToJump;
			this.x = marioFloatPos[0];
			this.y = marioFloatPos[1];
			this.penalty = 0;
			this.g = 0;
			this.jumpTime = prevJumpTime;
			this.xa = prevXa;
			this.ya = prevYa;
			this.height = marioMode > 0 ? 24: 12;
			/*
			int eCounter = 0;
			for(int i = 0; i < enemiesFloatPos.length; i+=3) {
				this.enemyList[eCounter].x = (marioFloatPos[0] + enemiesFloatPos[i+1]);
				this.enemyList[eCounter].y = (marioFloatPos[1] + enemiesFloatPos[i+2]);
				this.enemyList[eCounter].kind = (byte) enemiesFloatPos[i];
				eCounter++;
			}
			*/
		}
		@Override
		public int hashCode() {
			double ratio = 1;
			int x = (int) ((300 + this.x - marioFloatPos[0]) / ratio);
			int y = (int) ((300 + this.y - marioFloatPos[1]) / ratio);
			int z = 0;
			if (parent != null) {
				if (this.action[Mario.KEY_LEFT]) {
					z += 1;
				}
				if (this.action[Mario.KEY_RIGHT]) {
					z += 2;
				}
				if (this.action[Mario.KEY_JUMP]) {
					z += 4;
				}
			}
			String lort = x + "" + y + "" + z;
			int l = lort.length() > 9 ? 9 : lort.length();
			for (int i = 0; i < 9 - l; i++) {
				lort += 0;
			}
			return Integer.parseInt(lort.substring(0, l)); // GIVER CRASHES
		}

		public State getNextState(State parent, boolean[] action) {
			if (indexStateArray < numberOfStates) {
				State nextState = stateArray[indexStateArray++];

				nextState.action = action;
				nextState.parent = parent;
				nextState.onGround = parent.onGround;
				nextState.mayJump = parent.mayJump;
				nextState.xa = parent.xa;
				nextState.ya = parent.ya;
				nextState.x = parent.x;
				nextState.y = parent.y;
				nextState.jumpTime = parent.jumpTime;
				nextState.penalty = parent.penalty;
				nextState.g = parent.g + speedPriority;
				//nextState.enemyList = parent.enemyList;
				nextState.height = parent.height;
				ce.predictFuture(nextState);

				nextState.heuristic = ((searchDepth) - (int) (nextState.x - marioFloatPos[0]));
				return nextState;
			}
			return null;
		}

		public int priority() {
			return heuristic + g + penalty;
		}

		public void penalty(int amount) {
			penalty += amount;
		}

		public boolean isGoal() {
			if (x > marioFloatPos[0] + searchDepth) {
				return true;
			}
			return false;
		}

		State moveNE() {
			return getNextState(this, createAction(false, true, true, false));
		}

		State SmoveNE() {
			return getNextState(this, createAction(false, true, true, true));
		}

		State moveE() {
			return getNextState(this, createAction(false, true, false, false));
		}

		State SmoveE() {
			return getNextState(this, createAction(false, true, false, true));
		}

		State moveN() {
			return getNextState(this, createAction(false, false, true, false));
		}

		State SmoveN() {
			return getNextState(this, createAction(false, false, true, true));
		}

		State moveNW() {
			return getNextState(this, createAction(true, false, true, false));
		}

		State SmoveNW() {
			return getNextState(this, createAction(true, false, true, true));
		}

		State moveW() {
			return getNextState(this, createAction(true, false, false, false));
		}

		State SmoveW() {
			return getNextState(this, createAction(true, false, false, true));
		}

		State still() {
			return getNextState(this, createAction(false, false, false, false));
		}
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
		if (state.parent.parent != null) {
			if (debugPos < 600) {
				GlobalOptions.Pos[debugPos][0] = (int) state.x;
				GlobalOptions.Pos[debugPos][1] = (int) state.y;
				debugPos++;
			}
			return getRootState(state.parent);
		} else {
			return state;
		}
	}

	public void addSuccessor(State successor) {
		if (successor != null) {
			if (!closed.containsKey(successor.hashCode())) {
				openSet.add(successor);
				closed.put(successor.hashCode(), successor);
			}
		}
	}

	public State solve() {
		long startTime = System.currentTimeMillis();
		openSet.clear();
		closed.clear();
		indexStateArray = 1;

		// FOR DEBUGGING
		for (int i = 0; i < 600; i++) {
			GlobalOptions.Pos[i][0] = (int) marioFloatPos[0];

			GlobalOptions.Pos[i][1] = (int) marioFloatPos[1];
		}
		debugPos = 0;

		// Add initial state to queue.
		State initial = new State(true);
		stateArray[0] = initial;
		openSet.add(initial);
		closed.put(initial.hashCode(), initial);

		while (!openSet.isEmpty()) {
			State state = openSet.poll();

			if (state.isGoal()) {
				return getRootState(state);
			}

			// Debugging for being stuck in loop
			if (System.currentTimeMillis() - startTime > 25 || indexStateArray >= numberOfStates) {
				System.out.println("stuck in while-loop" + " Index = " + indexStateArray + " Open = " + openSet.size()
						+ " Close = " + closed.size());
				return getRootState(state);
			}
			
			addSuccessor(state.SmoveE());
			addSuccessor(state.SmoveNE());

			addSuccessor(state.SmoveNW());
			addSuccessor(state.SmoveW());

//			addSuccessor(state.SmoveW());
//			addSuccessor(state.SmoveNW());
//			addSuccessor(state.moveE());
//			addSuccessor(state.moveNE());
//			addSuccessor(state.moveW());
//			addSuccessor(state.moveNW());
			addSuccessor(state.still());
		}
		return null;
	}

	@Override
	public void reset() {
		for (int i = 0; i < action.length; ++i)
			action[i] = false;
		ce = new CustomEngine();
	}
	public boolean[] getAction() {
		if (firstScene) {
			ce.setScene(levelScene);
			firstScene = false;
		} else {
			ce.setLevelScene(levelScene);
			ce.toScene(marioFloatPos[0]);
		}

		State bestState = solve();

		if (bestState == null) {
			return createAction(false, false, false, false);
		}
		prevJumpTime = bestState.jumpTime;
		prevXa = bestState.xa;
		prevYa = bestState.ya;

		return bestState.action;
	}

	public int getBlock(int x, int y) {
		return levelScene[y][x];
	}

	@Override
	public void integrateObservation(Environment environment) {
		this.marioFloatPos = environment.getMarioFloatPos();
		this.enemiesFloatPos = environment.getEnemiesFloatPos();
		this.marioState = environment.getMarioState();
		// levelScene = environment.getLevelSceneObservationZ(1); // The normal
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

	public String getName() {
		return name;
	}

	public void setName(String Name) {
		this.name = Name;
	}
}