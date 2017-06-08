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

	private int numberOfStates = 10000;
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
	public Enemy prevCarried;
	// Same but for enemies
	public float[] prevEnemyXArr;
	public float[] prevEnemyYaArr;
	public int[] prevEnemyFacingArr;
	public boolean[] prevEnemyOnGroundArr;
	public boolean[] prevEnemyCarriedArr;
	public ArrayList<Float> prevEnemiesX;
	// Same but for fireballs
	public ArrayList<Fireball> prevFireballs;
	
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
		public boolean carrying;
		public int invulnerable;

		public int penalty;
		public State parent;
		public int heuristic;
		public boolean[] action;
		public int g;

		// enemy stuff
		public ArrayList<Enemy> enemyList;
		public boolean stomp;
		public boolean stompShell;
		public boolean kick;
		public Enemy carried = null;
		// fireballs
		public ArrayList<Fireball> fireballs;
		public ArrayList<Fireball> fireballsToCheck;
		public ArrayList<Shell> shellsToCheck;
		public int fireballsOnScreen;

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
			this.ableToShoot = isMarioAbleToShoot;
			this.carrying = isMarioCarrying;
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
			this.carried = null;
//			if(prevCarried != null) {
//				this.carried = new Shell(prevCarried.x,prevCarried.y,prevCarried.kind,
//						prevCarried.ya,prevCarried.facing,prevCarried.carried,prevCarried.dead);
//				this.carried = prevCarried;
//			}
//			 For the first tick, ya value is 3.0f. Its a small detail
			if (prevYa == 0)
				this.ya = 3.0f;
			this.yJumpSpeed = prevYJumpSpeed;
			this.height = marioMode > 0 ? 24 : 12;

			this.fireballs = new ArrayList<Fireball>();
			if(prevFireballs != null) {
				for(Fireball f: prevFireballs) {
					this.fireballs.add(new Fireball(f.x,f.y,f.kind,f.ya,f.facing,f.dead));

				}
			}
			this.enemyList = new ArrayList<Enemy>();
			for (int i = 0; i < enemiesFloatPos.length; i += 3) {
				float currEnemyX = (marioFloatPos[0] + enemiesFloatPos[i + 1]);
				float currEnemyY = (marioFloatPos[1] + enemiesFloatPos[i + 2]);
				byte kind = (byte) enemiesFloatPos[i];
				boolean EnemyOnGround = false;
				boolean EnemyCarried = false;
				// Check facing & Ya
				// 2.0 is the value all new enemies WITHOUT wings are assigned for first tick, else it is 0.6.
				// The reason this is not set to 0, is because when enemies
				// spawn in the engine, they are put through one single tick for themselves,
				// giving them some custom values for xa, ya, etc, which
				// (luckily) are the same for all enemies of same kind. Flowers are given 5 ticks
				// upon spawning, because why the fuck not??
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
				// This is for bullets
				if(kind == 84) {
					if (currEnemyX > this.x) 
						facing = -1;
					else if (currEnemyX < this.x) 
						facing = 1;
				}
				// Shells
				if(kind == 13) {
					facing = 0;
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
				if(prevEnemyCarriedArr != null && prevEnemyCarriedArr[i / 3]) {
					EnemyCarried = prevEnemyCarriedArr[i / 3];
				}

				if (kind == 98) {
					// Blue enemies ya value is set according to x, so it doesnt matter at all, thus set to 0
					this.enemyList.add(new BlueGoomba(currEnemyX, currEnemyY, kind, 0, facing, false));

				} else if (kind == 84) {
					this.enemyList.add(new Bullet(currEnemyX, currEnemyY, kind, EnemyYa, facing, false));
				} else if (kind == 91) {
					this.enemyList.add(new Flower(currEnemyX, currEnemyY, kind, EnemyYa, facing, false));
				} else if(kind == 13) {
					this.enemyList.add(new Shell(currEnemyX, currEnemyY, kind, EnemyYa, facing, EnemyCarried, false));
				} else if(kind == 82) {
					this.enemyList.add(new NormalEnemy(currEnemyX, currEnemyY, kind, EnemyYa, facing, false, EnemyOnGround));
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

		public State getNextState(State parent, boolean[] action) {
			if (indexStateArray < numberOfStates) {
				State nextState = stateArray[indexStateArray++];
				nextState.parent = parent;
				
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
				nextState.carrying = parent.carrying;
				nextState.carried = null;
				if(parent.carried != null) {
					Enemy prevCar = parent.carried;
					nextState.carried = new Shell(prevCar.x, prevCar.y, prevCar.kind,
							prevCar.ya, prevCar.facing, prevCar.carried, prevCar.dead);	
				}
				nextState.penalty = parent.penalty;

				nextState.enemyList = new ArrayList<Enemy>();
				for (int i = 0; i < parent.enemyList.size(); i++) {
					Enemy e = parent.enemyList.get(i);
					if (e.kind == 98) {
						nextState.enemyList.add(new BlueGoomba(e.x, e.y, e.kind, e.ya, e.facing, e.dead));
					} else if (e.kind == 84) {
						nextState.enemyList.add(new Bullet(e.x, e.y, e.kind, e.ya, e.facing, e.dead));
					} else if (e.kind == 91) {
						nextState.enemyList.add(new Flower(e.x, e.y, e.kind, e.ya, e.facing, e.dead));
					} else if (e.kind == 13) {
						nextState.enemyList.add(new Shell(e.x, e.y, e.kind, e.ya, e.facing, e.carried, e.dead));
					} else if (e.kind == 82) {
						nextState.enemyList.add(new NormalEnemy(e.x, e.y, e.kind, e.ya, e.facing, e.dead, e.onGround));
					} else {
						nextState.enemyList.add(new NormalEnemy(e.x, e.y, e.kind, e.ya, e.facing, e.dead));
					}
				}
				nextState.fireballs = new ArrayList<Fireball>();
				nextState.fireballsToCheck = new ArrayList<Fireball>();
				nextState.shellsToCheck = new ArrayList<Shell>();
				for(Fireball f: parent.fireballs) {
					nextState.fireballs.add(new Fireball(f.x,f.y,f.kind,f.ya, f.facing, f.dead));
				}
				ce.predictFuture(nextState);
				nextState.g = parent.g + (int) speedPriority;
				nextState.heuristic = (int) (searchDepth - nextState.x);

				return nextState;
			}
			return null;
		}

		public int priority() {
			return heuristic + g + penalty + (int) y / 16;
		}

		public void penalty(int amount) {
			penalty += amount;
		}

		public boolean isGoal() {
			return x >= searchDepth;
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

	public void addSuccessor(State successor) {
		if (successor != null) {
			if (!closed.containsKey(successor.superHashCode())) {
				openSet.add(successor);
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

	public State getRootState(State state) {
		if (state.parent == null) {
			return null;
		}
		if (state.parent.parent != null) {
			if (debugPos < 400) {
				if(GlobalOptions.enemyDebug) {
					if(!state.enemyList.isEmpty()) {
						for(int i = 0; i < GlobalOptions.enemyPos.length; i++) {
							if(state.enemyList.size() > i) {
								GlobalOptions.enemyPos[i][debugPos][0] = (int) state.enemyList.get(i).x;
								GlobalOptions.enemyPos[i][debugPos][1] = (int) state.enemyList.get(i).y;
							}
						}
					}
				}
				if(GlobalOptions.fireballDebug) {
					if(!state.fireballs.isEmpty()) {
						for(int i = 0; i < GlobalOptions.fireballPos.length; i++) {
							if(state.fireballs.size() > i) {
								GlobalOptions.fireballPos[i][debugPos][0] = (int) state.fireballs.get(i).x;
								GlobalOptions.fireballPos[i][debugPos][1] = (int) state.fireballs.get(i).y;
							}
						}
					}
				}
				
				if(GlobalOptions.marioDebug) {
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

		// FOR DEBUGGING
		if(GlobalOptions.enemyDebug) {
			for(int i = 0; i < GlobalOptions.enemyPos.length; i++) {
				for(int j = 0; j < 400; j++) {
					if(enemiesFloatPos.length/3 > i) {
						GlobalOptions.enemyPos[i][j][0] = (int) (enemiesFloatPos[i*3+1] + marioFloatPos[0]);
						GlobalOptions.enemyPos[i][j][1] = (int) (enemiesFloatPos[i*3+2] + marioFloatPos[1]);
					}
				}
			}
		}
		
		if(GlobalOptions.fireballDebug) {
			for(int i = 0; i < GlobalOptions.fireballPos.length; i++) {
				for(int j = 0; j < 400; j++) {
					if(initial.fireballs.size() > i){
						GlobalOptions.fireballPos[i][j][0] = (int) (initial.fireballs.get(i).x);
						GlobalOptions.fireballPos[i][j][1] = (int) (initial.fireballs.get(i).y);
					}
				}
			}
		}
		
		if(GlobalOptions.marioDebug) {
			for (int i = 0; i < 400; i++) {
				GlobalOptions.marioPos[i][0] = (int) marioFloatPos[0];
				GlobalOptions.marioPos[i][1] = (int) marioFloatPos[1];
			}
		}
		debugPos = 0;

		while (!openSet.isEmpty()) {
			State state = openSet.poll();

			if (state.isGoal()) {

				testState = state;
				return getRootState(state);
			}

			if (System.currentTimeMillis() - startTime > 25 || indexStateArray >= numberOfStates) {
//				 System.out.println("SHIT " + indexStateArray);
				return getRootState(state);
			}

			addSuccessor(state.SmoveE());
			addSuccessor(state.SmoveNE());
			addSuccessor(state.SmoveNW());
			addSuccessor(state.SmoveW());
			addSuccessor(state.still());

		}
		System.out.println("DISASTER: OPEN-SET IS EMPTY");
		// This should never happen. If it does, it fucks up so much with the
		// previous value arrays, etc.
		return null;
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
//		System.out.println("NEW TICK");

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
//		print(); 
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
		prevFireballs = new ArrayList<Fireball>();
		for(Fireball f: bestState.fireballs) {
			prevFireballs.add(f);
		}
		prevCarried = bestState.carried;


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
						if(e.kind == 13) {
							prevEnemyCarriedArr[i / 3] = e.carried;
							if(e.carried) System.out.println("getCarried scrubs");
						}
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
		prevFireballs = null;
		prevCarried = null;
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