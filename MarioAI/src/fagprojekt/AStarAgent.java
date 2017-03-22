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
	public final int maxRight = 16 * 11;
	public final int searchDepth = maxRight;
	public boolean firstScene = true;

	private int speedPriority = 9;
	private int penaltySize = 25;

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
			// heuristic = 18 - this.xGrid;
		}

		@Override
		public int hashCode() {
			int x = (int) this.x;
			int y = (int) this.y;
			int z = 0;
			int zz = 0;
			if (parent != null) {
				double __a = Math.pow(this.x - parent.x, 2);
				double __b = Math.pow(this.y - parent.y, 2);
				double __z = Math.sqrt(__a + __b);
				z = (int) __z;
				double __aa = Math.pow(this.xa - parent.xa, 2);
				double __bb = Math.pow(this.ya - parent.ya, 2);
				double __zz = Math.sqrt(__aa + __bb);
				zz = (int) __zz;
			}
			String lort = x + "" + y + "" + z + "" + zz;
			int l = lort.length() > 9 ? 9 : lort.length();
			return Integer.parseInt(lort.substring(0, l));
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

			this.g = parent.g + speedPriority;

			penalty = parent.penalty;

			ce.predictFuture(this);
			// TODO - HEURISTIC NEEEEEDS TO BE AMOUNT OF TICKS TO GOAL, BASED ON
			// HIS MAXIMUM SPEED!!!!!!!
			heuristic = ((searchDepth) - (int) (x - marioFloatPos[0]));

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
			if (x > marioFloatPos[0] + searchDepth) {
				return true;
			}
			return false;
		}

		State moveNE() {
			return new State(this, createAction(false, true, true, false));
		}

		State SmoveNE() {
			return new State(this, createAction(false, true, true, true));
		}

		State moveE() {
			return new State(this, createAction(false, true, false, false));
		}

		State SmoveE() {
			return new State(this, createAction(false, true, false, true));
		}

		State moveN() {
			return new State(this, createAction(false, false, true, false));
		}

		State SmoveN() {
			return new State(this, createAction(false, false, true, true));
		}

		State moveNW() {
			return new State(this, createAction(true, false, true, false));
		}

		State SmoveNW() {
			return new State(this, createAction(true, false, true, true));
		}

		State moveW() {
			return new State(this, createAction(true, false, false, false));
		}

		State SmoveW() {
			return new State(this, createAction(true, false, false, true));
		}

		State still() {
			return new State(this, createAction(false, false, false, false));
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
			if (closed.containsKey(successor.hashCode())) {
				successor.penalty(penaltySize);
			}
		}
		openSet.add(successor);
	}

	public State solve() {
		long startTime = System.currentTimeMillis();
		openSet.clear();
		closed.clear();

		// FOR DEBUGGING
		for (int i = 0; i < 600; i++) {
			GlobalOptions.Pos[i][0] = (int) marioFloatPos[0];
			GlobalOptions.Pos[i][1] = (int )marioFloatPos[1];
		}
		debugPos = 0;

		// Add initial state to queue.
		State initial = new State();
		openSet.add(initial);

		while (!openSet.isEmpty()) {
			State state = openSet.poll();

			if (state.isGoal()) {
				return getRootState(state);
			}

			// Debugging for being stuck in loop
			if (System.currentTimeMillis() - startTime > 25) {
				System.out.println("stuck in while-loop");
				return getRootState(state);
			}

			// Make sure we don't revisit this state.
			closed.put(state.hashCode(), state);

			// Add successors to the queue.
			addSuccessor(state.SmoveE());
			addSuccessor(state.SmoveNE());
			/*
			addSuccessor(state.moveE());
			addSuccessor(state.moveNE());
			addSuccessor(state.moveN());
			addSuccessor(state.still());
			addSuccessor(state.SmoveNW());
			addSuccessor(state.SmoveW());
			addSuccessor(state.moveNW());
			addSuccessor(state.moveW());
			*/
		}
		return null;
	}

	@Override
	public void reset() {
		for (int i = 0; i < action.length; ++i)
			action[i] = false;
		ce = new CustomEngine();
	}
	ArrayList<Enemy> enemyList = new ArrayList<Enemy>();
	public boolean[] getAction() {
		for(int i = 0; i < enemiesFloatPos.length; i+=3) {
			enemyList.add(new Enemy((int) (marioFloatPos[0] + enemiesFloatPos[i+1]),
					(int) (marioFloatPos[1] + enemiesFloatPos[i+2]), (int) enemiesFloatPos[i]));
		}
		
		ce.updateEnemies(enemyList);
		if (firstScene) {
			ce.setScene(levelScene);
			firstScene = false;
		} else {
			ce.setLevelScene(levelScene);
			ce.toScene(marioFloatPos[0]);
		}

		State bestState = solve();
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
