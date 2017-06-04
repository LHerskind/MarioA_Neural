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
	public final int searchDepth = maxRight;
	public boolean firstScene;
	boolean cheatMap = true;
	private double speedPriority = 9;
	private State testState;

	private int numberOfStates = 4000;
	private State[] stateArray = new State[numberOfStates];
	private int indexStateArray = 0;
	// enemies
	// private int estimatedMaxSearchDepth = 100;
	// private int maxNumberOfEnemies = 35;
	// private Enemy[][] enemyArray = new
	// Enemy[numberOfStates][maxNumberOfEnemies];

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
	public ArrayList<Enemy> enemyList;
	private int numberOfEnemies = 20;
	private BlueBeetle[] blueBeetleArray = new BlueBeetle[numberOfEnemies];
	private Bullet[] bulletArray = new Bullet[numberOfEnemies];
	private Flower[] flowerArray = new Flower[numberOfEnemies];
	private NormalEnemy[] normalEnemyArray = new NormalEnemy[numberOfEnemies*4];
	
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
		for (int i = 0; i < numberOfEnemies; i++) {
			BlueBeetle blueBeetle = new BlueBeetle(0, 0, (byte)0, 0, 0, false);
			Bullet bullet = new Bullet(0, 0, (byte)0, 0, 0, false);
			Flower flower = new Flower(0, 0, (byte)0, 0, 0, false);
			
			blueBeetleArray[i] = blueBeetle;
			bulletArray[i] = bullet;
			flowerArray[i] = flower;
		}
		for (int i = 0; i < numberOfEnemies*4; i++) {
			NormalEnemy normalEnemy= new NormalEnemy(0, 0, (byte)0, 0, 0, false);
			
			normalEnemyArray[i] = normalEnemy;
		}
		
		enemyList = new ArrayList<Enemy>();
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
		public boolean sliding; // Not needed
		public boolean mayJump;
		public boolean ableToShoot;
		public int invulnerable;

		public int penalty;
		public State parent;
		public int heuristic;
		public boolean[] action;
		public int g;

		// enemy stuff
		public ArrayList<Float> enemyVarList;
		public boolean stomp;

		public State() {
			enemyVarList = new ArrayList<Float>();
		}

		public State(boolean lala) {
			enemyVarList = new ArrayList<Float>();
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
			this.sliding = prevSliding;
			this.facing = prevFacing;
			this.xJumpSpeed = prevXJumpSpeed;
			this.invulnerable = prevInvulnerable;
			// For the first tick, ya value is 3.0f. Its a small detail
			if (prevYa == 0)
				this.ya = 3.0f;
			this.yJumpSpeed = prevYJumpSpeed;
			this.height = marioMode > 0 ? 24 : 12;

			//this.enemyList = new ArrayList<Enemy>();

			
			for (int i = 0; i < enemiesFloatPos.length; i += 3) {
				float currEnemyX = (marioFloatPos[0] + enemiesFloatPos[i + 1]);
				float currEnemyY = (marioFloatPos[1] + enemiesFloatPos[i + 2]);
				float kind = enemiesFloatPos[i];
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

				float facing = -1;
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
					//this.enemyList.add(new BlueBeetle(currEnemyX, currEnemyY, kind, 0, facing, false));
					enemyVarList.add(currEnemyX);
					enemyVarList.add(currEnemyY);
					enemyVarList.add(kind);
					enemyVarList.add((float) 0);
					enemyVarList.add(facing);
					enemyVarList.add((float)0);
					enemyVarList.add((float)0);
					
				} else if (kind == 84) {
					//this.enemyList.add(new Bullet(currEnemyX, currEnemyY, kind, EnemyYa, facing, false));
					enemyVarList.add(currEnemyX);
					enemyVarList.add(currEnemyY);
					enemyVarList.add(kind);
					enemyVarList.add(EnemyYa);
					enemyVarList.add(facing);
					enemyVarList.add((float)0);
					enemyVarList.add((float)0);
		
				} else if (kind == 91) {
					//this.enemyList.add(new Flower(currEnemyX, currEnemyY, kind, EnemyYa, facing, false));
					enemyVarList.add(currEnemyX);
					enemyVarList.add(currEnemyY);
					enemyVarList.add(kind);
					enemyVarList.add(EnemyYa);
					enemyVarList.add(facing);
					enemyVarList.add((float)0);
					enemyVarList.add((float)0);
				} else if (kind == 82) {
					//this.enemyList.add(new NormalEnemy(currEnemyX, currEnemyY, kind, EnemyYa, facing, false, EnemyOnGround));
					enemyVarList.add(currEnemyX);
					enemyVarList.add(currEnemyY);
					enemyVarList.add(kind);
					enemyVarList.add(EnemyYa);
					enemyVarList.add(facing);
					enemyVarList.add((float)0);
					enemyVarList.add(EnemyOnGround ?(float) 1 : (float) 0);
					
				} else {
					//this.enemyList.add(new NormalEnemy(currEnemyX, currEnemyY, kind, EnemyYa, facing, false));
					enemyVarList.add(currEnemyX);
					enemyVarList.add(currEnemyY);
					enemyVarList.add(kind);
					enemyVarList.add(EnemyYa);
					enemyVarList.add(facing);
					enemyVarList.add((float)0);
					enemyVarList.add((float)0);
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
				
				
				nextState.enemyVarList.addAll(parent.enemyVarList);
				
/*
				//nextState.enemyList = new ArrayList<Enemy>();
				for (int i = 0; i < parent.enemyVarList.length; i=+7) {
					Enemy e = parent.enemyList.get(i);
					if (e.kind == 98) {
						//nextState.enemyList.add(new BlueBeetle(e.x, e.y, e.kind, e.ya, e.facing, e.dead));
						nextState.enemyVarList[i]=e.x;
						nextState.enemyVarList[i+1]=e.y;
						nextState.enemyVarList[i+2]=e.kind;
						nextState.enemyVarList[i+3]=e.ya;
						nextState.enemyVarList[i+4]=e.facing;
						nextState.enemyVarList[i+5]=e.dead ? 1 : 0;
						nextState.enemyVarList[i+6]=0;
					} else if (e.kind == 84) {
						nextState.enemyList.add(new Bullet(e.x, e.y, e.kind, e.ya, e.facing, e.dead));
					} else if (e.kind == 91) {
						nextState.enemyList.add(new Flower(e.x, e.y, e.kind, e.ya, e.facing, e.dead));
					} else if (e.kind == 82) {
						nextState.enemyList.add(new NormalEnemy(e.x, e.y, e.kind, e.ya, e.facing, e.dead, e.onGround));
					} else {
						nextState.enemyList.add(new NormalEnemy(e.x, e.y, e.kind, e.ya, e.facing, e.dead));
					}
				}
*/
				
				updateEnemyList(nextState);				// import enemyVarList into enemyList
				ce.predictFuture(nextState, enemyList);
				System.out.println("----------");
				System.out.println("1 :"+nextState.enemyVarList.size()+"  "+parent.enemyVarList.size());
				updateEnemyVarList(nextState); 			// import enemyList into enemyVarList
				System.out.println("2 :"+nextState.enemyVarList.size()+"  "+parent.enemyVarList.size());
				nextState.g = parent.g  + (int) speedPriority;
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
			return x >= (marioFloatPos[0] + searchDepth);
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
				// if (successor.penalty < 2000) {// + marioMode * 500) {
				openSet.add(successor);
				// }
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
	
	public State getTestState(){
		return testState;
	}

	public State getRootState(State state) {
		if (state.parent == null) {
			return null;
		}
		if (state.parent.parent != null) {
			if (debugPos < 400) {
				// ENEMY DEBUG
				if (!enemyList.isEmpty()) {
					for (int i = 0; i < GlobalOptions.enemyPos.length; i++) {
						if (enemyList.size() > i) {
							GlobalOptions.enemyPos[i][debugPos][0] = (int) enemyList.get(i).x;
							GlobalOptions.enemyPos[i][debugPos][1] = (int) enemyList.get(i).y;
						}
					}
				}

				// MARIO DEBUG
				GlobalOptions.marioPos[debugPos][0] = (int) state.x;
				GlobalOptions.marioPos[debugPos][1] = (int) state.y;
				debugPos++;
			}
			return getRootState(state.parent);
		} else {
			return state;
		}
	}

	public State solve() {
		long startTime = System.currentTimeMillis();

		openSet.clear();
		closed.clear();
		indexStateArray = 1;

		// Add initial state to queue.
		State initial = new State(true);
		stateArray[indexStateArray++] = initial;
		openSet.add(initial);
		closed.put(initial.superHashCode(), initial);

		
//		updateEnemyList(initial); // import enemyVarList into enemyList
		
		// FOR DEBUGGING
		// MARIO DEBUG
		for (int i = 0; i < 400; i++) {
			GlobalOptions.marioPos[i][0] = (int) marioFloatPos[0];
			GlobalOptions.marioPos[i][1] = (int) marioFloatPos[1];
		}
		// ENEMY DEBUG
		for (int i = 0; i < GlobalOptions.enemyPos.length; i++) {
			for (int j = 0; j < 400; j++) {
				if (enemiesFloatPos.length / 3 > i) {
					GlobalOptions.enemyPos[i][j][0] = (int) (enemiesFloatPos[i * 3 + 1] + marioFloatPos[0]);
					GlobalOptions.enemyPos[i][j][1] = (int) (enemiesFloatPos[i * 3 + 2] + marioFloatPos[1]);
				}
			}
		}

		debugPos = 0;

		while (!openSet.isEmpty()) {
			State state = openSet.poll();

			
			
			if (state.isGoal()) {
				testState = state;
				return getRootState(state);
			}

			// Debugging for being stuck in loop
			if (System.currentTimeMillis() - startTime > 28 || indexStateArray >= numberOfStates) {
				// System.out.println("stuck in while-loop" + " Index = " +
				// indexStateArray +
				// " Open = " + openSet.size() + " Close = " + closed.size());
				return getRootState(state);
			}

			addSuccessor(state.SmoveE());
			addSuccessor(state.SmoveNE());
			addSuccessor(state.SmoveNW());
			addSuccessor(state.SmoveW());
			// addSuccessor(state.moveE());
			// addSuccessor(state.moveNE());
			// addSuccessor(state.moveW());
			// addSuccessor(state.moveNW());
			// addSuccessor(state.still());
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
//		 System.out.println("NEW TICK!");

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
//			ce.print();
		}
		validatePrevArr();

//		if (checkFrozen())
//			ce.frozenEnemies = checkFrozen();
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
		updateEnemyList(bestState);
		return bestState.action;
	}

	public void validatePrevArr() {
		if (bestState != null) {
			prevEnemyFacingArr = new int[enemiesFloatPos.length / 3];
			prevEnemyYaArr = new float[enemiesFloatPos.length / 3];
			prevEnemyOnGroundArr = new boolean[enemiesFloatPos.length / 3];

			for (int i = 0; i < enemiesFloatPos.length; i += 3) {
				for (int j = 0; j < enemyList.size(); j++) {
					Enemy e = enemyList.get(j);
					if (e.x == (enemiesFloatPos[i + 1] + marioFloatPos[0]) && e.kind == (enemiesFloatPos[i])) {
						prevEnemyFacingArr[i / 3] = e.facing;
						prevEnemyYaArr[i / 3] = e.ya;

						if (e.kind == 82)
							prevEnemyOnGroundArr[i / 3] = e.onGround;
						enemyList.remove(e);
						break;
					}

				}
			}
		}
	}

//	public boolean checkFrozen() {
//		if (prevEnemiesX != null && prevEnemiesX.size() == enemiesFloatPos.length / 3 && prevEnemiesX.size() != 0) {
//			int frozenNo = 0;
//
//			ArrayList<Float> tempList = new ArrayList<Float>();
//			for (int i = 0; i < prevEnemiesX.size(); i++) {
//				tempList.add(prevEnemiesX.get(i));
//			}
//			for (int i = 0; i < enemiesFloatPos.length; i += 3) {
//				for (int j = 0; j < tempList.size(); j++) {
//					if (tempList.get(j) == enemiesFloatPos[i + 1] + marioFloatPos[0]) {
//						if (enemiesFloatPos[i] != 91)
//							frozenNo++;
//						tempList.remove(j);
//						break;
//					}
//				}
//			}
//			if (frozenNo == prevEnemiesX.size())
//				return true;
//		}
//		return false;
//	}

	public int getBlock(int x, int y) {
		return levelScene[y][x];
	}

	public void updateEnemyList(State state){
		
		enemyList.clear();
		
		int blueBeetleCount=0;
		int bulletCount=0;
		int flowerCount=0;
		int normalEnemyCount=0;
		
		for (int i = 0; i < state.enemyVarList.size(); i+=7) {
			
			if(state.enemyVarList.get(i+2)==0)return; //Stops when there's not more enemies.
			
			boolean dead = (state.enemyVarList.get(i+5)==1) ? true : false;
			if (Math.round(state.enemyVarList.get(i+2)) == 98) {
//				try{

					blueBeetleArray[blueBeetleCount].setVariables(state.enemyVarList.get(i), state.enemyVarList.get(i+1),(byte) Math.round(state.enemyVarList.get(i+2)), 0,Math.round(state.enemyVarList.get(i+4)),dead);
					enemyList.add(blueBeetleArray[blueBeetleCount]);
					blueBeetleCount++;
//				} catch (IndexOutOfBoundsException e) {
//				    System.err.println("IndexOutOfBoundsException: " + e.getMessage());
//				} 
				
			} else if (Math.round(state.enemyVarList.get(i+2)) == 84) {
				try{
					bulletArray[bulletCount].setVariables(state.enemyVarList.get(i), state.enemyVarList.get(i+1),(byte) Math.round(state.enemyVarList.get(i+2)), state.enemyVarList.get(i+3),Math.round(state.enemyVarList.get(i+4)),dead);
					enemyList.add(bulletArray[bulletCount]);
					bulletCount++;
				} catch (IndexOutOfBoundsException e) {
				    System.err.println("IndexOutOfBoundsException: " + e.getMessage());
				} 
				
			} else if (Math.round(state.enemyVarList.get(i+2)) == 91) {
				try{
					flowerArray[flowerCount].setVariables(state.enemyVarList.get(i), state.enemyVarList.get(i+1),(byte) Math.round(state.enemyVarList.get(i+2)), state.enemyVarList.get(i+3),Math.round(state.enemyVarList.get(i+4)),dead);
					enemyList.add(flowerArray[flowerCount]);
					flowerCount++;
				} catch (IndexOutOfBoundsException e) {
				    System.err.println("IndexOutOfBoundsException: " + e.getMessage());
				} 
				
			} else if (Math.round(state.enemyVarList.get(i+2)) == 82) {
				boolean onGround = (state.enemyVarList.get(i+6)==1) ? true : false;
				try{
					normalEnemyArray[normalEnemyCount].setVariables(state.enemyVarList.get(i), state.enemyVarList.get(i+1),(byte) Math.round(state.enemyVarList.get(i+2)), state.enemyVarList.get(i+3),Math.round(state.enemyVarList.get(i+4)),dead,onGround);
					enemyList.add(normalEnemyArray[normalEnemyCount]);
					normalEnemyCount++;
				} catch (IndexOutOfBoundsException e) {
				    System.err.println("IndexOutOfBoundsException: " + e.getMessage());
				} 
			} else {
				try{
					normalEnemyArray[normalEnemyCount].setVariables(state.enemyVarList.get(i), state.enemyVarList.get(i+1),(byte) Math.round(state.enemyVarList.get(i+2)), state.enemyVarList.get(i+3), Math.round(state.enemyVarList.get(i+4)),dead);
					enemyList.add(normalEnemyArray[normalEnemyCount]);
					normalEnemyCount++;
				} catch (IndexOutOfBoundsException e) {
				    System.err.println("IndexOutOfBoundsException: " + e.getMessage());
				} 
			}
		}
	}

	public void updateEnemyVarList(State state){
		state.enemyVarList.clear();
		for (int i = 0; i < enemyList.size(); i++) {
			float dead = enemyList.get(i).dead ? 1 : 0;
			float onGround = enemyList.get(i).onGround ? 1 : 0;
			state.enemyVarList.add(enemyList.get(i).x);
			state.enemyVarList.add(enemyList.get(i).y);
			state.enemyVarList.add((float) enemyList.get(i).kind);
			state.enemyVarList.add(enemyList.get(i).ya);
			state.enemyVarList.add((float) enemyList.get(i).facing);
			state.enemyVarList.add(dead);
			state.enemyVarList.add(onGround);
			
		}
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