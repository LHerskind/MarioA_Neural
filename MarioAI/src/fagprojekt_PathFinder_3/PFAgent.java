package fagprojekt_PathFinder_3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

public class PFAgent extends BasicMarioAIAgent {

	private static String name = "PFAgent";
	int debugPos = 0;
	boolean debug = true;

	public PFAgent(String s) {
		super(s);
	}

	private CustomEngine customEngine;
	private Move bestMove;

	public PFAgent() {
		super(name);
		customEngine = new CustomEngine(this);
	}

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

	@Override
	public boolean[] getAction() {
		boolean[] action = new boolean[9];

		customEngine.toScene(marioFloatPos[0]);

		calculateMove();

		action = bestMove.getState().getAction();

		if (!isMarioAbleToJump && isMarioOnGround) {
			action[Mario.KEY_JUMP] = false;
		}

		// action[Mario.KEY_SPEED] = true;

		// print();

		return action;
	}

	public byte getBlock(int y) {
		System.out.print(levelScene[y][18] + " ");
		return levelScene[y][18];
	}

	public void print() {
		int x = (int) marioFloatPos[0] / 16;
		int y = (int) marioFloatPos[1] / 16;

		for (int i = 0; i < 19; i++) {
			for (int j = 0; j < 19; j++) {
				if (i == y && j == x) {
					System.out.print("M" + "\t");
				} else {
					System.out.print(levelScene[i][j] + "\t");
				}
			}
			System.out.println();
		}
		System.out.println();
	}

	ArrayList<Move> frontier = new ArrayList<>();
	HashMap<Integer, State> explored = new HashMap<>();

	private void addState(Move parent, boolean[] action) {
		Move possibleNext = customEngine.getMove(parent, action);
		if (possibleNext != null) {
			if (!explored.containsKey(possibleNext.getState().hashCode())) {
				explored.put(possibleNext.getState().hashCode(), possibleNext.getState());
				frontier.add(possibleNext);
			}
		}
	}

	private boolean first = true;

	private void calculateMove() {
		frontier.clear();
		explored.clear();

		// System.out.println(marioFloatPos[0] + " " + marioFloatPos[1]);

		if (first) {
			customEngine.setScene(levelScene);
			first = false;
		}

		State firstState = new State(marioFloatPos[0], marioFloatPos[1], 0, 0, null);

		if (bestMove != null) {
			firstState.setJump(bestMove.getState().getJump());
			firstState.setVX(bestMove.getState().getVX());
			firstState.setVY(bestMove.getState().getVY());
		}

		Move firstBestMove = new Move(marioFloatPos[0], null, firstState);

		toFrontier(firstBestMove);

		while (!frontier.isEmpty()) {
			frontier.sort(new Comparator<Move>() {
				public int compare(Move o1, Move o2) {
					if (o1.getPoints() > o2.getPoints()) {
						return -1;
					} else {
						return 1;
					}
				}
			});

			Move next = frontier.remove(0);

			if (next.getPoints() >= firstBestMove.getPoints() + 16 * 4) {
				if (debug) {
//					customEngine.printOnGoing(marioFloatPos[0], marioFloatPos[1]);
				}
				bestMove = next;
				break;
			}
			toFrontier(next);
		}
		if (bestMove != null) {
			getBestMove();
		}
//		System.out.println();
	}

	private void toFrontier(Move parent) {
		boolean[] actions = new boolean[9];

		actions[Mario.KEY_LEFT] = false;
		actions[Mario.KEY_RIGHT] = true;
		actions[Mario.KEY_JUMP] = false;
		addState(parent, actions);

		actions = new boolean[9];
		actions[Mario.KEY_LEFT] = false;
		actions[Mario.KEY_RIGHT] = true;
		actions[Mario.KEY_JUMP] = true;
		addState(parent, actions);

		actions = new boolean[9];
		actions[Mario.KEY_LEFT] = true;
		actions[Mario.KEY_RIGHT] = false;
		actions[Mario.KEY_JUMP] = false;
		addState(parent, actions);

		actions = new boolean[9];
		actions[Mario.KEY_LEFT] = false;
		actions[Mario.KEY_RIGHT] = false;
		actions[Mario.KEY_JUMP] = true;
		addState(parent, actions);

		actions = new boolean[9];
		actions[Mario.KEY_LEFT] = true;
		actions[Mario.KEY_RIGHT] = false;
		actions[Mario.KEY_JUMP] = true;
		addState(parent, actions);

	}

	private void draw(float x, float y) {
		if (debug) {
			GlobalOptions.marioPos[debugPos][0] = (int) (x);
			GlobalOptions.marioPos[debugPos][1] = (int) (y);
			debugPos++;
			if (debugPos >= 600) {
				debugPos = 0;
			}
		}
	}

	private void getBestMove() {
		debugPos = 0;
		GlobalOptions.marioPos = new int[600][2];
		while (bestMove.getParent().getParent() != null) {
			draw(bestMove.getState().getX(), bestMove.getState().getY());
			bestMove = bestMove.getParent();
		}
		draw(bestMove.getState().getX(), bestMove.getState().getY());
	}

}
