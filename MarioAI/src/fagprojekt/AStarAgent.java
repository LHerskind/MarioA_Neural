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
	public ArrayList<Float> prevEnemiesX;

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
			/*
			 * for(int j = 0; j < maxNumberOfEnemies; j++) { enemyArray[i][j] =
			 * new Enemy(); } state.enemyList = enemyArray[i];
			 */
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
		// if (input[Mario.KEY_SPEED]) {
		// z += 11;
		// }
		return z;
	}

	public class State {
		public float x;
		public float y;
		public float xa;
		public int facing;
		public int marioHeight;

		public float ya;
		public int jumpTime;
		public float yJumpSpeed;
		public float xJumpSpeed;
		public int height;

		public boolean onGround;
		public boolean wasOnGround;
		public boolean sliding;
		public boolean mayJump;
		public boolean ableToShoot;
		public int invulnerable;

		public int penalty;
		public State parent;
		public int heuristic;
		public boolean[] action;
		public int g;

		// enemy stuff
		public ArrayList<Enemy> enemyList;
		public boolean stomp;

		public State() {
		}

		public State(boolean lala) {
			initValues();
			penalty = 0;
			parent = null;
			action = null;
			heuristic = (int) (searchDepth - x);
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
			this.sliding = prevSliding;
			this.facing = prevFacing;
			this.xJumpSpeed = prevXJumpSpeed;
			this.invulnerable = prevInvulnerable;
			// For the first tick, ya value is 3.0f. Its a small detail
			if (prevYa == 0)
				this.ya = 3.0f;
			this.yJumpSpeed = prevYJumpSpeed;
			this.height = marioMode > 0 ? 24 : 12;

			this.enemyList = new ArrayList<Enemy>();

			for (int i = 0; i < enemiesFloatPos.length; i += 3) {
				float currEnemyX = (marioFloatPos[0] + enemiesFloatPos[i + 1]);
				float currEnemyY = (marioFloatPos[1] + enemiesFloatPos[i + 2]);
				byte kind = (byte) enemiesFloatPos[i];
				boolean EnemyOnGround = false;
				// Check facing & Ya
				// 2.0 is the value all new enemies WITHOUT wings are assigned
				// for first tick, else it is 0.6.
				// The reason this is not set to 0, is because when enemies
				// spawn in the engine, they are put through one single tick for
				// themselves,
				// giving them some custom values for xa, ya, etc, which
				// (luckily) are the same for all enemies of same kind. Flowers
				// are given 5 ticks
				// upon spawning, because why the fuck not??
				float EnemyYa = 2.0f;
				// Following values are for winged enemies
				if (kind == 96 || kind == 97 || kind == 95 || kind == 99)
					EnemyYa = 0.6f;
				// This value is for flowerzzz
				else if (kind == 91)
					EnemyYa = -4.31441f;

				int facing = -1;
				if (kind == 84) {
					if (currEnemyX > this.x)
						facing = -1;
					else if (currEnemyX < this.x)
						facing = 1;
				}
				if (prevEnemyFacingArr != null && prevEnemyFacingArr[i / 3] != 0) {
					facing = prevEnemyFacingArr[i / 3];
				}
				if (prevEnemyYaArr != null && prevEnemyYaArr[i / 3] != 0) {
					EnemyYa = prevEnemyYaArr[i / 3];
				}
				if (prevEnemyOnGroundArr != null && prevEnemyOnGroundArr[i / 3]) {
					EnemyOnGround = prevEnemyOnGroundArr[i / 3];
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
					this.enemyList.add(new Shell(currEnemyX, currEnemyY, kind, 0, 0, false));
				} else if (kind == 82) {
					this.enemyList
							.add(new NormalEnemy(currEnemyX, currEnemyY, kind, EnemyYa, facing, false, EnemyOnGround));
				} else {
					this.enemyList.add(new NormalEnemy(currEnemyX, currEnemyY, kind, EnemyYa, facing, false));
				}

			}

		}

		public long superHashCode() {
			int x = (int) ((300 + this.x - marioFloatPos[0]));
			int y = (int) ((300 + this.y - marioFloatPos[1]));
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

		public State getNextState(State parent, boolean[] action, ArrayList<Enemy> enemies) {
			if (indexStateArray < numberOfStates) {
				State nextState = stateArray[indexStateArray++];
				nextState.parent = parent;

				nextState.action = action;
				nextState.height = parent.height;
				nextState.onGround = parent.onGround;
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
				nextState.penalty = parent.penalty;

				nextState.enemyList = enemies;

				ce.predictFuture(nextState);
				nextState.g = parent.g + (int) speedPriority;
				nextState.heuristic = (int) (searchDepth - nextState.x);

				return nextState;
			}
			return null;
		}

		public int priority() {
			int lala = this.onGround ? 0 : 15;
			return heuristic + g + penalty + lala + (int) y / 16;
		}

		public void penalty(int amount) {
			penalty += amount;
		}

		public boolean isGoal() {
			return x >= searchDepth;
		}

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

		State SmoveN(ArrayList<Enemy> enemies) {
			return getNextState(this, createAction(false, false, true, true), enemies);
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
			ArrayList<Enemy> newList = new ArrayList<>();
			State togo = getNextState(this, duckAction(), newList);
			if (togo != null) {
				togo.marioHeight = 12;
			}
			return togo;
		}
	}

	public void addSuccessor(State successor) {
		if (successor != null) {
			if (!closed.containsKey(successor.superHashCode())) {
				if (successor.penalty < 2000) {
					openSet.add(successor);
				}
				closed.put(successor.superHashCode(), successor);
			} else {
				indexStateArray--;
			}
		}
	}

	private boolean[] duckAction() {
		boolean[] action = new boolean[6];
		action[Mario.KEY_DOWN] = true;
		return action;
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

	public State solve() {
		long startTime = System.currentTimeMillis();

		if ((int) marioFloatPos[0] + maxRight > searchDepth) {
			searchDepth = (int) marioFloatPos[0] + maxRight;
		}

		openSet.clear();
		closed.clear();
		indexStateArray = 1;

		// Add initial state to queue.
		State initial = new State(true);
		stateArray[indexStateArray++] = initial;
		openSet.add(initial);
		closed.put(initial.superHashCode(), initial);

		stateArray[0] = initial.duck();

		// FOR DEBUGGING
		if (GlobalOptions.enemyDebug) {
			for (int i = 0; i < GlobalOptions.enemyPos.length; i++) {
				for (int j = 0; j < 400; j++) {
					if (enemiesFloatPos.length / 3 > i) {
						GlobalOptions.enemyPos[i][j][0] = (int) (enemiesFloatPos[i * 3 + 1] + marioFloatPos[0]);
						GlobalOptions.enemyPos[i][j][1] = (int) (enemiesFloatPos[i * 3 + 2] + marioFloatPos[1]);
					}
				}
			}
		}
		if (GlobalOptions.marioDebug) {
			for (int i = 0; i < 400; i++) {
				GlobalOptions.marioPos[i][0] = (int) marioFloatPos[0];
				GlobalOptions.marioPos[i][1] = (int) marioFloatPos[1];
				debugPos = 0;
			}
		}

		while (!openSet.isEmpty()) {
			State state = openSet.poll();

			if (state.isGoal()) {
				testState = state;
				return getRootState(state);
			}

			if (System.currentTimeMillis() - startTime > 25 || indexStateArray >= numberOfStates) {
				if(indexStateArray >= numberOfStates){
					System.out.println("SHIT");
				}
				return getRootState(state);
			}

			ArrayList<Enemy> enemiesNextState = ce.predictEnemies(state.enemyList);
			// System.out.println("New enemies predicted");
			addSuccessor(state.SmoveE(enemiesNextState));
			addSuccessor(state.SmoveNE(enemiesNextState));
			addSuccessor(state.SmoveNW(enemiesNextState));
			addSuccessor(state.SmoveW(enemiesNextState));
		}
		// System.out.println("DISASTER: OPEN-SET IS EMPTY");
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
		// System.out.println("NEW TICK!");

		// If mario position is 32, it means we have started a new map in
		// GamePlayTrack, and firstScene should be set true
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

		// print();

		bestState = solve();
		if (bestState == null) {
			return createAction(false, false, false, false);
		}
		prevJumpTime = bestState.jumpTime;
		prevXa = bestState.xa;
		prevYa = bestState.ya;
		prevYJumpSpeed = bestState.yJumpSpeed;
		prevXJumpSpeed = bestState.xJumpSpeed;
		prevInvulnerable = bestState.invulnerable;
		prevSliding = bestState.sliding;
		prevFacing = bestState.facing;

		prevEnemiesX = new ArrayList<Float>();
		for (int i = 0; i < enemiesFloatPos.length; i += 3) {
			prevEnemiesX.add(enemiesFloatPos[i + 1] + marioFloatPos[0]);
		}
		return bestState.action;
	}

	public void validatePrevArr() {
		if (bestState != null) {
			prevEnemyFacingArr = new int[enemiesFloatPos.length / 3];
			prevEnemyYaArr = new float[enemiesFloatPos.length / 3];
			prevEnemyOnGroundArr = new boolean[enemiesFloatPos.length / 3];

			for (int i = 0; i < enemiesFloatPos.length; i += 3) {
				for (int j = 0; j < bestState.enemyList.size(); j++) {
					Enemy e = bestState.enemyList.get(j);
					if (e.x == (enemiesFloatPos[i + 1] + marioFloatPos[0]) && e.kind == (enemiesFloatPos[i])) {
						prevEnemyFacingArr[i / 3] = e.facing;
						prevEnemyYaArr[i / 3] = e.ya;

						if (e.kind == 82)
							prevEnemyOnGroundArr[i / 3] = e.onGround;
						bestState.enemyList.remove(e);
						break;
					}

				}
			}
		}
	}

	public int getBlock(int x, int y) {
		return levelScene[y][x];
	}

	/*
	 * private void print() { for (int i = 0; i < 19; i++) { for (int j = 0; j <
	 * 19; j++) { System.out.print(levelScene[i][j] + "\t");
	 * 
	 * } System.out.println(); } System.out.println(); }
	 */
	@Override
	public void integrateObservation(Environment environment) {
		this.marioFloatPos = environment.getMarioFloatPos();
		this.enemiesFloatPos = environment.getEnemiesFloatPos();
		this.marioState = environment.getMarioState();

		levelScene = environment.getLevelSceneObservationZ(1); // The normal
		if (cheatMap)
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

	public void resetValues() {
		prevEnemyFacingArr = null;
		prevEnemyYaArr = null;
		prevEnemyOnGroundArr = null;
		prevEnemiesX = null;
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