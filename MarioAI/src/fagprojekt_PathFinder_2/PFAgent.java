package fagprojekt_PathFinder_2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.tasks.SystemOfValues;

public class PFAgent extends BasicMarioAIAgent {

	private int amplifier = 16;
	private SystemOfValues sov = new SystemOfValues();

	private static String name = "PFAgent";

	public PFAgent(String s) {
		super(s);
	}

	public PFAgent() {
		super(name);
	}

	private Move bestMove;

	@Override
	public boolean[] getAction() {
		boolean[] action = new boolean[9];

		calculateMove();

		if (bestMove != null) {
			if (bestMove.getState().getX() > marioFloatPos[0]) {
				action[Mario.KEY_RIGHT] = true;
			} else if (bestMove.getState().getX() < marioFloatPos[0]) {
				action[Mario.KEY_LEFT] = true;
			}

			if (bestMove.getState().getY() < marioFloatPos[1]) {
				action[Mario.KEY_JUMP] = true;
			}

			if (!isMarioAbleToJump && isMarioOnGround) {
				action[Mario.KEY_JUMP] = false;
			}
		}

		action[Mario.KEY_SPEED] = true;

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
			System.out.println(parent.getState().getX() + " " + possibleNext.getState().getX());
			// explored.put(possibleNext.getState().hashCode(),
			// possibleNext.getState());
			frontier.add(possibleNext);
			// }
		}
	}

	private void toFrontier(Move parent) {
		boolean[] actions = new boolean[9];

		actions[Mario.KEY_LEFT] = true;
		actions[Mario.KEY_RIGHT] = false;
		actions[Mario.KEY_JUMP] = false;
		addState(parent, actions);

		actions[Mario.KEY_LEFT] = false;
		actions[Mario.KEY_RIGHT] = true;
		actions[Mario.KEY_JUMP] = false;
		addState(parent, actions);

		actions[Mario.KEY_LEFT] = false;
		actions[Mario.KEY_RIGHT] = false;
		actions[Mario.KEY_JUMP] = true;
		addState(parent, actions);

		actions[Mario.KEY_LEFT] = false;
		actions[Mario.KEY_RIGHT] = true;
		actions[Mario.KEY_JUMP] = true;
		addState(parent, actions);

		actions[Mario.KEY_LEFT] = true;
		actions[Mario.KEY_RIGHT] = false;
		actions[Mario.KEY_JUMP] = true;
		addState(parent, actions);
	}

	CustomEngine customEngine;

	private void calculateMove() {
		frontier.clear();

		customEngine = new CustomEngine(mergedObservation);

		State firstState = new State(9 * 16, 9 * 16, 0, 0);

		bestMove = new Move(0, null, firstState);

		toFrontier(bestMove);
		System.out.println("Size = " + frontier.size());

		while (!frontier.isEmpty()) {
			frontier.sort(new Comparator<Move>() {
				public int compare(Move o1, Move o2) {
					return (int) (o2.getPoints() - o1.getPoints());
				}
			});

			Move next = frontier.remove(0);
			System.out.println(next.getState().getX() + " : " + marioFloatPos[0]);

			if (next.getState().getX() >= marioFloatPos[0] + 16 * 6) {
				bestMove = next;
				break;
			}

			toFrontier(next);
		}
		System.out.println("Size = " + frontier.size());

		getBestMove();
		System.out.println();
	}

	private void getBestMove() {
		System.out.println("BEST1 = " + bestMove.getState().getX() + " : " + bestMove.getState().getY());
/*		while (bestMove.getParent() != null) {
			if (bestMove.getParent().getParent() != null) {
				System.out.println("change");
				bestMove = bestMove.getParent();
			} else {
				break;
			}
		}
		System.out.println("BEST2 = " + bestMove.getState().getX() + " : " + bestMove.getState().getY());*/
	}

}
