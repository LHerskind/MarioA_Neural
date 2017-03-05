package fagprojekt_PathFinder_2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.tasks.SystemOfValues;

public class PFAgent extends BasicMarioAIAgent {

	private static String name = "PFAgent";

	public PFAgent(String s) {
		super(s);
	}

	public PFAgent() {
		super(name);
		customEngine = new CustomEngine();
	}

	private Move bestMove;

	@Override
	public boolean[] getAction() {
		boolean[] action = new boolean[9];
		
		System.out.println(marioFloatPos[0] + " " + marioFloatPos[1]);

		calculateMove();

		action = bestMove.getState().getAction();

/*
		if (bestMove != null) {
			if (bestMove.getState().getX() > 0) {
				action[Mario.KEY_RIGHT] = true;
			} else if (bestMove.getState().getX() < 0) {
				action[Mario.KEY_LEFT] = true;
			}

			if (bestMove.getState().getY() < 0) {
				action[Mario.KEY_JUMP] = true;
			}

			if (!isMarioAbleToJump && isMarioOnGround) {
				action[Mario.KEY_JUMP] = false;
			}
		}
	*/	
		if (!isMarioAbleToJump && isMarioOnGround) {
			action[Mario.KEY_JUMP] = false;
		}


		// action[Mario.KEY_SPEED] = true;

		// print();

		return action;
	}

	private void print() {
		for (int i = 0; i < 19; i++) {
			for (int j = 0; j < 19; j++) {
				if (i == 9 && j == 9) {
					System.out.print("M \t");
				} else {
					System.out.print(mergedObservation[i][j] + "\t");
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
			// if (!explored.containsKey(possibleNext.getState().hashCode())) {
			// explored.put(possibleNext.getState().hashCode(),
			// possibleNext.getState());
			frontier.add(possibleNext);
			// }
		}
	}

	CustomEngine customEngine;

	private void calculateMove() {
		frontier.clear();
		explored.clear();

		customEngine.setMerged(mergedObservation);

		State firstState = new State(0, 0, 0, 0, null);

		Move firstBestMove = new Move(0, null, firstState);

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

			if (next.getPoints() >= 16 * 8) {
				bestMove = next;
				break;
			}
			toFrontier(next);
		}
		if (bestMove != null) {
			getBestMove();
		}
		System.out.println();
	}

	private void toFrontier(Move parent) {
		boolean[] actions = new boolean[9];

		actions[Mario.KEY_LEFT] = false;
		actions[Mario.KEY_RIGHT] = true;
		actions[Mario.KEY_JUMP] = false;
		addState(parent, actions);

		actions = new boolean[9];
		actions[Mario.KEY_LEFT] = true;
		actions[Mario.KEY_RIGHT] = false;
		actions[Mario.KEY_JUMP] = false;
		addState(parent, actions);

		actions = new boolean[9];
		actions[Mario.KEY_LEFT] = false;
		actions[Mario.KEY_RIGHT] = true;
		actions[Mario.KEY_JUMP] = true;
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

	private void getBestMove() {

		while (bestMove.getParent().getParent() != null) {
			System.out.println("PATH: " + bestMove.getState().getAction()[Mario.KEY_LEFT] + " "
					+ bestMove.getState().getAction()[Mario.KEY_JUMP] + " "
					+ bestMove.getState().getAction()[Mario.KEY_RIGHT] + " " + bestMove.getState().getX() + " "
					+ bestMove.getState().getY());
			bestMove = bestMove.getParent();
		}
		System.out.println("Action: " + bestMove.getState().getAction()[Mario.KEY_LEFT] + " "
				+ bestMove.getState().getAction()[Mario.KEY_JUMP] + " "
				+ bestMove.getState().getAction()[Mario.KEY_RIGHT]);
	}

}
