package fagprojekt_PathFinder;

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
			if (bestMove.getState().getX() > 9) {
				action[Mario.KEY_RIGHT] = true;
			} else if (bestMove.getState().getX() < 9) {
				action[Mario.KEY_LEFT] = true;
			}

			if (bestMove.getState().getY() < 9) {
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

	private void addState(State state, Move parent) {
		if (possibleMove(state.getX(), state.getY())) {
			if (!explored.containsKey(state.hashCode())) {
				Move nextMove = getMove(state, parent);
				explored.put(state.hashCode(), state);
				frontier.add(nextMove);
			}
		}
	}

	private Move getMove(State state, Move parent) {
		double points = 0;
		if (parent != null) {
			points += parent.getPoints();
		}

		points += sov.distance * (state.getX() - 9) * amplifier;

		if (mergedObservation[state.getY()][state.getX()] == 2) {
			points += sov.coins;
		}
		if (marioMode > 0) {
			if (mergedObservation[state.getY() - 1][state.getX()] == 2) {
				points += sov.coins;
			}
		}
		return new Move(points, parent, state);
	}

	private void firstFrontier() {
		for (int j = -1; j < 2; j++) {
			for (int i = -1; i < 2; i++) {
				addState(new State(9 - j, 9 - i), null);
			}
		}
	}

	private void calculateMove() {
		frontier.clear();
		explored.clear();

		bestMove = getMove(new State(9, 9), null);
		firstFrontier();

		while (!frontier.isEmpty()) {
			frontier.sort(new Comparator<Move>() {
				public int compare(Move o1, Move o2) {
					return (int) (o2.getPoints() - o1.getPoints());
				}
			});

			Move next = frontier.remove(0);

			if (next.getState().getX() >= 18) {
				bestMove = next;
				break;
			}

			for (int j = -1; j < 2; j++) {
				for (int i = -1; i < 2; i++) {
					State nextState = new State(next.getState().getX() - j, next.getState().getY() - i);
					addState(nextState, next);
				}
			}
		}

		getBestMove();
	}

	private void getBestMove() {
		while (bestMove.getParent() != null) {
			bestMove = bestMove.getParent();
		}
	}

	private boolean possibleMove(int x, int y) {
		if (x < 0 || y < 0 || x > 18 || y > 18) {
			return false;
		}
		if (mergedObservation[y][x] < 0 && mergedObservation[y][x] != -62) {
			return false;
		}

		if (marioMode > 0) {
			if (x < 1 || y < 1 || x > 18 || y > 18) {
				return false;
			}

			if (mergedObservation[y - 1][x] < 0 && mergedObservation[y - 1][x] != -62) {
				return false;
			}
		}
		return true;
	}

}
