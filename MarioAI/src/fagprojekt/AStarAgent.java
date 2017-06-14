// Main class for all the source code. The agent returns an action that mario should do in the next step
package fagprojekt;

import java.util.ArrayList;
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
	public final int maxRight = 16 * 17;
	public int searchDepth = maxRight;

	public boolean firstScene;
	boolean cheatMap = true;
	private double speedPriority = 9;
	private State testState;

	private int numberOfStates = 20000;
	private State[] stateArray = new State[numberOfStates];
	private int indexStateArray = 0;

	public int debugPos;
	private CustomEngine ce;

	// Need-to-know states persisting for each tick
	public State bestState;
	public int prevJumpTime;
	public float prevXa;
	public float prevYa;
	public int prevInvulnerable;
	public float prevYJumpSpeed;
	public float prevXJumpSpeed;
	public int prevFacing;
	public boolean prevSliding;
	// Same but for enemies
	public float[] prevEnemyXArr;
	public float[] prevEnemyYaArr;
	public int[] prevEnemyFacingArr;
	public boolean[] prevEnemyOnGroundArr;
	public boolean[] prevEnemyCarriedArr;
	public ArrayList<Float> prevEnemiesX;
	// Same but for fireballs
	public ArrayList<Fireball> prevFireballs;
	// closed set and frontier/openSet, relevant for the A* algorithm
	private HashMap<Long, State> closed = new HashMap<>();

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
			stateArray[i] = state;
		}
		reset();
	}

	public int actionInt(boolean[] input) {
		int z = 0;
		if (input[Mario.KEY_LEFT]) {
			z += 1;
		}
		if (input[Mario.KEY_RIGHT]) {
			z += 2;
		}
		if (input[Mario.KEY_JUMP]) {
			z += 4;
		}
		return z;
	}

	public class State {
		// Movement
		public float x;
		public float y;
		public float xa;
		public float ya;
		public int jumpTime;
		public float yJumpSpeed;
		public float xJumpSpeed;
		public int facing;
		public boolean mayJump;
		// Misc
		public int marioHeight;
		public int marioMode;
		public int height;
		public boolean onGround;
		public boolean wasOnGround;
		public boolean sliding;
		public boolean ableToShoot;
		public int invulnerable;

		// A* relevant values
		public int penalty;
		public State parent;
		public int heuristic;
		public boolean[] action;
		public int g;

		// enemy stuff
		public ArrayList<Enemy> enemyList;
		public boolean stomp;
		public Enemy carried = null;
		public ArrayList<Shell> shellsToCheck;

		// fireballs
		public ArrayList<Fireball> fireballs;
		public ArrayList<Fireball> fireballsToCheck;
		public int fireballsOnScreen;

		public State() {
		}

		/**
		 * Initial state
		 * 
		 * @param initial
		 */
		public State(boolean initial) {
			initValues();
			penalty = 0;
			parent = null;
			action = null;
			heuristic = (int) (searchDepth - x);
		}

		public void initValues() {
			this.marioMode = AStarAgent.this.marioMode;
			this.onGround = isMarioOnGround;
			this.mayJump = isMarioAbleToJump;
			this.ableToShoot = isMarioAbleToShoot;
			this.x = marioFloatPos[0];
			this.y = marioFloatPos[1];
			this.penalty = 0;
			this.g = 0;
			this.jumpTime = prevJumpTime;
			this.xa = prevXa;
			this.ya = prevYa;
			this.sliding = prevSliding;
			this.facing = prevFacing;
			this.xJumpSpeed = prevXJumpSpeed;
			this.invulnerable = prevInvulnerable;
			this.yJumpSpeed = prevYJumpSpeed;
			this.height = marioMode > 0 ? 24 : 12;
			this.carried = null;
			this.shellsToCheck = new ArrayList<Shell>();
			// For the first tick, ya value is 3.0f. Its a small detail
			if (prevYa == 0)
				this.ya = 3.0f;

			this.fireballs = new ArrayList<Fireball>();
			if (prevFireballs != null) {
				for (Fireball f : prevFireballs) {
					this.fireballs.add(new Fireball(f.x, f.y, f.kind, f.ya, f.facing, f.dead));

				}
			}
			this.enemyList = new ArrayList<Enemy>();
			for (int i = 0; i < enemiesFloatPos.length; i += 3) {
				float currEnemyX = (marioFloatPos[0] + enemiesFloatPos[i + 1]);
				float currEnemyY = (marioFloatPos[1] + enemiesFloatPos[i + 2]);
				byte kind = (byte) enemiesFloatPos[i];
				boolean EnemyOnGround = false;
				boolean EnemyCarried = false;
				// When enemies spawn they tick once, giving them an initial
				// value of ya, etc, unless
				// they persisted from the previous tick, in which case they
				// pertain their previous values.
				float EnemyYa = 2.0f;
				// Following values are for winged enemies
				if (kind == 96 || kind == 97 || kind == 95 || kind == 99)
					EnemyYa = 0.6f;
				// This value is for flowerzzz
				else if (kind == 91)
					EnemyYa = -4.31441f;
				// This value is for shells
				else if (kind == 13) {
					EnemyYa = -2.25f;
				}
				int facing = -1;

				if (kind == 84) {
					if (currEnemyX > this.x)
						facing = -1;
					else if (currEnemyX < this.x)
						facing = 1;
				}
				// Shells
				if (kind == 13) {
					facing = 0;
				}
				if (prevEnemyFacingArr != null && prevEnemyFacingArr.length > i / 3 && prevEnemyFacingArr[i / 3] != 0) {
					facing = prevEnemyFacingArr[i / 3];
				}
				if (prevEnemyYaArr != null && prevEnemyYaArr.length > i / 3 && prevEnemyYaArr[i / 3] != 0) {
					EnemyYa = prevEnemyYaArr[i / 3];
				}
				if (prevEnemyOnGroundArr != null && prevEnemyOnGroundArr.length > i / 3
						&& prevEnemyOnGroundArr[i / 3]) {
					EnemyOnGround = prevEnemyOnGroundArr[i / 3];
				}
				if (prevEnemyCarriedArr != null && prevEnemyCarriedArr.length > i / 3 && prevEnemyCarriedArr[i / 3]) {
					EnemyCarried = prevEnemyCarriedArr[i / 3];
				}

				if (kind == 98) {
					// Blue enemies ya value is set according to x, so it doesnt
					// matter at all, thus set to 0
					this.enemyList.add(new BlueGoomba(currEnemyX, currEnemyY, kind, 0, facing, false));

				} else if (kind == 84) {
					this.enemyList.add(new Bullet(currEnemyX, currEnemyY, kind, EnemyYa, facing, false));
				} else if (kind == 91) {
					this.enemyList.add(new Flower(currEnemyX, currEnemyY, kind, EnemyYa, facing, false));
				} else if (kind == 13) {
					this.enemyList.add(new Shell(currEnemyX, currEnemyY, kind, EnemyYa, facing, EnemyCarried, false));
				} else if (kind == 82) {
					this.enemyList
							.add(new NormalEnemy(currEnemyX, currEnemyY, kind, EnemyYa, facing, false, EnemyOnGround));
				} else {
					this.enemyList.add(new NormalEnemy(currEnemyX, currEnemyY, kind, EnemyYa, facing, false));
				}

			}
		}

		/**
		 * Is used to differentiate between states in the closed set
		 */
		public long superHashCode() {
			int x = (int) (500 + this.x - marioFloatPos[0]);
			int y = (int) (500 + this.y - marioFloatPos[1]);
			int z = 0;
			if (parent != null) {
				z = actionInt(this.action);
			}
			String superHashCode = x + "" + y + "" + z;

			while (superHashCode.length() < 10) {
				superHashCode += 0;
			}
			return Long.parseLong(superHashCode);
		}

		public State copyState(State toCopy) {
			State copy = new State();
			copy.parent = toCopy.parent;
			copy.marioMode = toCopy.marioMode;
			copy.action = toCopy.action;
			copy.height = toCopy.height;
			copy.marioHeight = toCopy.marioHeight;
			copy.ableToShoot = toCopy.ableToShoot;
			copy.carried = toCopy.carried;
			copy.enemyList = toCopy.enemyList;
			copy.facing = toCopy.facing;
			copy.fireballs = toCopy.fireballs;
			copy.fireballsOnScreen = toCopy.fireballsOnScreen;
			copy.fireballsToCheck = toCopy.fireballsToCheck;
			copy.onGround = toCopy.onGround;
			copy.mayJump = toCopy.mayJump;
			copy.g = toCopy.g;
			copy.penalty = toCopy.penalty;
			copy.sliding = toCopy.sliding;
			copy.heuristic = toCopy.heuristic;
			copy.jumpTime = toCopy.jumpTime;
			copy.shellsToCheck = toCopy.shellsToCheck;
			copy.stomp = toCopy.stomp;
			copy.wasOnGround = toCopy.wasOnGround;
			copy.x = toCopy.x;
			copy.xa = toCopy.xa;
			copy.xJumpSpeed = toCopy.xJumpSpeed;
			copy.y = toCopy.y;
			copy.ya = toCopy.ya;
			copy.yJumpSpeed = toCopy.yJumpSpeed;
			return copy;
		}

		/**
		 * <h1>Get Next State</h1> Given a parent state, an action and the
		 * prediction of the enemies, this method will return a child, which
		 * contains the predicted movement of Mario, when he will use the action
		 * in next tick
		 * 
		 * @param parent
		 * @param action
		 * @param enemies
		 * @return State
		 */

		public State getNextState(State parent, boolean[] action, ArrayList<Enemy> enemies) {
			if (indexStateArray < numberOfStates) {
				State nextState = stateArray[indexStateArray++];
				// State nextState = new State();
				nextState.parent = parent;

				nextState.marioMode = parent.marioMode;
				nextState.action = action;
				nextState.height = parent.height;
				nextState.onGround = parent.onGround;
				nextState.ableToShoot = parent.ableToShoot;
				nextState.invulnerable = parent.invulnerable;
				nextState.mayJump = parent.mayJump;
				nextState.xa = parent.xa;
				nextState.ya = parent.ya;
				nextState.yJumpSpeed = parent.yJumpSpeed;
				nextState.xJumpSpeed = parent.xJumpSpeed;
				nextState.x = parent.x;
				nextState.y = parent.y;
				nextState.jumpTime = parent.jumpTime;
				nextState.sliding = parent.sliding;
				nextState.facing = parent.facing;
				nextState.carried = null;
				if (parent.carried != null) {
					Enemy prevCar = parent.carried;
					nextState.carried = new Shell(prevCar.x, prevCar.y, prevCar.kind, prevCar.ya, prevCar.facing,
							prevCar.carried, prevCar.dead);
				}
				nextState.penalty = parent.penalty;

				nextState.enemyList = enemies;

				nextState.fireballs = new ArrayList<Fireball>();
				nextState.fireballsToCheck = new ArrayList<Fireball>();
				nextState.shellsToCheck = new ArrayList<Shell>();
				for (Fireball f : parent.fireballs) {
					if (!f.dead) {
						nextState.fireballs.add(new Fireball(f.x, f.y, f.kind, f.ya, f.facing, f.dead));
					}
				}
				ce.predictFuture(nextState);
				// the g-value is set parent + 9. The value 9 is obtained
				// through trial and error and does not have any specific
				// meaning
				nextState.g = parent.g + (int) speedPriority;
				// The heuristic is the distance to the end of the screen
				nextState.heuristic = (int) (searchDepth - nextState.x);

				return nextState;
			}
			return null;
		}

		/**
		 * The priority for the A*, also called f-function. It is an addition of
		 * the heuristic, g and penalty
		 */
		public int priority() {
			int onGroundPrio = this.onGround ? 0 : 15;
			return heuristic + g + penalty + (int) y / 16 + onGroundPrio;
		}

		/**
		 * <h1>Penalty</h1> Adds a given amount to the penalty of the state
		 * 
		 * @param amount
		 */
		public void penalty(int amount) {
			penalty += amount;
		}

		/**
		 * <h1>IsGoal</h1> Returns true when we have moved to at least the goal.
		 * 
		 * @return boolean
		 */

		public boolean isGoal() {
			return x >= searchDepth;
		}

		// Below is all the needed future states, defined by the future action
		State moveNE(ArrayList<Enemy> enemies) {
			return getNextState(this, createAction(false, true, true, false), enemies);
		}

		State SmoveNE(ArrayList<Enemy> enemies) {
			return getNextState(this, createAction(false, true, true, true), enemies);
		}

		State moveE(ArrayList<Enemy> enemies) {
			return getNextState(this, createAction(false, true, false, false), enemies);
		}

		State SmoveE(ArrayList<Enemy> enemies) {
			return getNextState(this, createAction(false, true, false, true), enemies);
		}

		State moveN(ArrayList<Enemy> enemies) {
			return getNextState(this, createAction(false, false, true, false), enemies);
		}

		State moveNW(ArrayList<Enemy> enemies) {
			return getNextState(this, createAction(true, false, true, false), enemies);
		}

		State SmoveNW(ArrayList<Enemy> enemies) {
			return getNextState(this, createAction(true, false, true, true), enemies);
		}

		State moveW(ArrayList<Enemy> enemies) {
			return getNextState(this, createAction(true, false, false, false), enemies);
		}

		State SmoveW(ArrayList<Enemy> enemies) {
			return getNextState(this, createAction(true, false, false, true), enemies);
		}

		State still(ArrayList<Enemy> enemies) {
			return getNextState(this, createAction(false, false, false, false), enemies);
		}

		State duck() {
			ArrayList<Enemy> enemies = new ArrayList<Enemy>();
			State togo = getNextState(this, duckAction(), enemies);
			if (togo != null) {
				togo.marioHeight = 12;
			}
			return togo;
		}

		private boolean[] duckAction() {
			boolean[] action = new boolean[6];
			action[Mario.KEY_DOWN] = true;
			return action;
		}
	}

	/**
	 * This function adds the next state(successor) to the open set, unless it
	 * has been visited before.
	 */
	public void addSuccessor(State successor) {
		if (successor != null) {
			if (!closed.containsKey(successor.superHashCode())) {
				if (successor.penalty < Values.penaltyDie) {
					openSet.add(successor);
				}
				closed.put(successor.superHashCode(), successor);
			} else {
				indexStateArray--;
			}
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

	public State getTestState() {
		return testState;
	}

	/**
	 * When a route has been calculated, the function returns the first action
	 * in the chain to the goal. When tracing backwards, it saves all the
	 * coordinates on the path in a debug array which is drawn in the
	 * LevelRenderer-class.
	 */
	public State getRootState(State state) {
		if (state.parent == null) {
			return null;
		}
		if (state.parent.parent != null) {
			if (debugPos < 400) {
				if (GlobalOptions.enemyDebug) {
					if (!state.enemyList.isEmpty()) {
						for (int i = 0; i < GlobalOptions.enemyPos.length; i++) {
							if (state.enemyList.size() > i) {
								GlobalOptions.enemyPos[i][debugPos][0] = (int) state.enemyList.get(i).x;
								GlobalOptions.enemyPos[i][debugPos][1] = (int) state.enemyList.get(i).y;
							}
						}
					}
				}
				if (GlobalOptions.fireballDebug) {
					if (!state.fireballs.isEmpty()) {
						for (int i = 0; i < GlobalOptions.fireballPos.length; i++) {
							if (state.fireballs.size() > i) {
								GlobalOptions.fireballPos[i][debugPos][0] = (int) state.fireballs.get(i).x;
								GlobalOptions.fireballPos[i][debugPos][1] = (int) state.fireballs.get(i).y;
							}
						}
					}
				}
				if (GlobalOptions.marioDebug) {
					GlobalOptions.marioPos[debugPos][0] = (int) state.x;
					GlobalOptions.marioPos[debugPos][1] = (int) state.y;
				}
				debugPos++;
			}
			return getRootState(state.parent);
		} else {
			return state;
		}
	}

	/**
	 * 
	 * @param state
	 */

	public void debug(State state) {

		if (GlobalOptions.enemyDebug) {
			for (int i = 0; i < GlobalOptions.enemyPos.length; i++) {
				for (int j = 0; j < 400; j++) {
					if (enemiesFloatPos.length / 3 > i) {
						GlobalOptions.enemyPos[i][j][0] = (int) (state.enemyList.get(i).x);
						GlobalOptions.enemyPos[i][j][1] = (int) (state.enemyList.get(i).y);
					}
				}
			}
		}

		if (GlobalOptions.fireballDebug) {
			for (int i = 0; i < GlobalOptions.fireballPos.length; i++) {
				for (int j = 0; j < 400; j++) {
					if (state.fireballs.size() > i) {
						GlobalOptions.fireballPos[i][j][0] = (int) (state.fireballs.get(i).x);
						GlobalOptions.fireballPos[i][j][1] = (int) (state.fireballs.get(i).y);
					}
				}
			}
		}

		if (GlobalOptions.marioDebug) {
			for (int i = 0; i < 400; i++) {
				GlobalOptions.marioPos[i][0] = (int) state.x;
				GlobalOptions.marioPos[i][1] = (int) state.y;
			}
		}
	}

	/**
	 * The core of the A*-algorithm. Pulls the front state from the open set and
	 * checks whether its the goal. If not, add its successors to the openSet.
	 * 
	 * @return
	 */
	public State solve() {
		long startTime = System.currentTimeMillis();

		if ((int) marioFloatPos[0] + maxRight > searchDepth) {
			searchDepth = (int) marioFloatPos[0] + maxRight;
		}

		openSet.clear();
		closed.clear();
		indexStateArray = 1;
		State initial = new State(true);
		stateArray[indexStateArray++] = initial;
		openSet.add(initial);
		closed.put(initial.superHashCode(), initial);
		stateArray[0] = initial.duck();

		debug(initial);
		debugPos = 0;

		while (!openSet.isEmpty()) {
			State state = openSet.poll();

			if (state.isGoal()) {
				// testState = state;
				return getRootState(state);
			}

			if (System.currentTimeMillis() - startTime > 25 || indexStateArray >= numberOfStates) {
				return getRootState(state);
			}

			ArrayList<Enemy> enemiesNextState = ce.predictEnemies(state);
			addSuccessor(state.SmoveE(enemiesNextState));
			addSuccessor(state.SmoveNE(enemiesNextState));
			addSuccessor(state.SmoveNW(enemiesNextState));
			addSuccessor(state.SmoveW(enemiesNextState));
			if (state.carried != null || state.fireballs.size() < 2 && state.marioMode == 2) {
				addSuccessor(state.still(enemiesNextState));
			}
		}
		return stateArray[0];
	}

	@Override
	public void reset() {
		for (int i = 0; i < action.length; ++i)
			action[i] = false;
		ce = new CustomEngine();
		firstScene = true;
		resetValues();
	}

	public boolean[] getAction() {
		// System.out.println("NEW TICK");

		// If mario position is 32, it means we have started a new map in
		// GamePlayTrack, and we need a reset of values
		if (marioFloatPos[0] == 32) {
			reset();
		}

		if (firstScene) {
			if (cheatMap) {
				ce.setCheatScene(levelScene);
			} else {
				ce.setScene(levelScene);
			}
			firstScene = false;
		} else {
			ce.setLevelScene(levelScene);
			if (cheatMap) {
				ce.toCheatScene(marioFloatPos[0]);
			} else {
				ce.toScene(marioFloatPos[0], marioFloatPos[1]);
			}
			// ce.print();
		}
		validatePrevArr();

		bestState = solve();
		if (bestState == null) {
			return createAction(false, false, false, false);
		}
		// The values for the best future state are saved and called in the next
		// runthrough
		prevJumpTime = bestState.jumpTime;
		prevXa = bestState.xa;
		prevYa = bestState.ya;
		prevYJumpSpeed = bestState.yJumpSpeed;
		prevXJumpSpeed = bestState.xJumpSpeed;
		prevInvulnerable = bestState.invulnerable;
		prevSliding = bestState.sliding;
		prevFacing = bestState.facing;
		prevFireballs = new ArrayList<Fireball>();
		for (Fireball f : bestState.fireballs) {
			prevFireballs.add(f);
		}

		prevEnemiesX = new ArrayList<Float>();
		for (int i = 0; i < enemiesFloatPos.length; i += 3) {
			prevEnemiesX.add(enemiesFloatPos[i + 1] + marioFloatPos[0]);
		}
		return bestState.action;
	}

	/**
	 * Merges the previous enemy list with the input enemy list, to make sure
	 * the indexes of the enemies are the same
	 */
	public void validatePrevArr() {
		if (bestState != null) {
			prevEnemyFacingArr = new int[enemiesFloatPos.length / 3];
			prevEnemyYaArr = new float[enemiesFloatPos.length / 3];
			prevEnemyOnGroundArr = new boolean[enemiesFloatPos.length / 3];
			prevEnemyCarriedArr = new boolean[enemiesFloatPos.length / 3];

			for (int i = 0; i < enemiesFloatPos.length; i += 3) {
				for (int j = 0; j < bestState.enemyList.size(); j++) {
					Enemy e = bestState.enemyList.get(j);
					if (e.x == (enemiesFloatPos[i + 1] + marioFloatPos[0]) && e.kind == (enemiesFloatPos[i])) {
						prevEnemyFacingArr[i / 3] = e.facing;
						prevEnemyYaArr[i / 3] = e.ya;
						// Red Koopa is 82
						if (e.kind == 82)
							prevEnemyOnGroundArr[i / 3] = e.onGround;
						// Shell is 13
						if (e.kind == 13) {
							prevEnemyCarriedArr[i / 3] = e.carried;
						}
						bestState.enemyList.remove(e);
						break;
					}

				}
			}
		}
	}

	/**
	 * We use this to change what input w are getting, this is seen inside the
	 * if(cheatMap)
	 */
	@Override
	public void integrateObservation(Environment environment) {
		this.marioFloatPos = environment.getMarioFloatPos();
		this.enemiesFloatPos = environment.getEnemiesFloatPos();
		this.marioState = environment.getMarioState();

		levelScene = environment.getLevelSceneObservationZ(1); // The normal
		if (cheatMap) {
			levelScene = environment.getLevelSceneObservationZ(1, 2, (int) marioFloatPos[1] / 16);
		}

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

	public void resetValues() {
		prevEnemyFacingArr = null;
		prevEnemyYaArr = null;
		prevEnemyOnGroundArr = null;
		prevEnemiesX = null;
		prevFireballs = null;
		prevJumpTime = 0;
		prevXa = 0;
		prevYa = 0;
		prevYJumpSpeed = 0;
		prevXJumpSpeed = 0;
		prevInvulnerable = 0;
		prevSliding = false;
		prevFacing = 0;
	}

}