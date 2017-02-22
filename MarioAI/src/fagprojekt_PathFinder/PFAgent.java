package fagprojekt_PathFinder;

import java.util.ArrayList;

import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.tasks.SystemOfValues;

public class PFAgent extends BasicMarioAIAgent {

	private int amplifier = 16;
	private SystemOfValues sov = new SystemOfValues();

	private static String name = "PFAgent";

	private int steps = 2;

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
			if (bestMove.getX() > 9) {
				action[Mario.KEY_RIGHT] = true;
			} else if (bestMove.getX() < 9) {
				action[Mario.KEY_LEFT] = true;
			}

			if (bestMove.getY() < 9) {
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
				System.out.print(mergedObservation[i][j]);
			}
			System.out.println();
		}
		System.out.println();
	}

	ArrayList<Move> moves_one = new ArrayList<>();
	ArrayList<Move> moves_two = new ArrayList<>();

	private void calculateMove() {
		bestMove = new Move(9, 9, 0, null);
		calculateBestMove(9, 9, null, moves_one);

		for (int j = 0; j < steps; j++) {

			for (Move move : moves_one) {
				calculateBestMove(move.getX(), move.getY(), move, moves_two);
			}
			moves_one.clear();

			for (Move move : moves_two) {
				calculateBestMove(move.getX(), move.getY(), move, moves_one);
			}
			moves_two.clear();
		}

		for (Move move : moves_one) {
			if (move.getPoints() > bestMove.getPoints()) {
				bestMove = move;
			}
		}
		moves_one.clear();

		getBestMove();

	}

	private void getBestMove() {
		while (bestMove.getParent() != null) {
			bestMove = bestMove.getParent();
		}
	}

	private void calculateBestMove(int currentX, int currentY, Move parent, ArrayList<Move> moves_list) {
		Move move;
		for (int i = 1; i < 2; i++) {
			for (int j = 0; j < 4; j++) {
				if (possibleMove(currentX + i, currentY - j)) {
					if ((move = getMove(currentX + i, currentY - j, parent)).getPoints() > 0) {
						moves_list.add(move);
					}
				}
			}
		}
	}

	private Move getMove(int x, int y, Move parent) {
		double points = 0;
		if (parent != null) {
			points += parent.getPoints();
		}

		points += sov.distance * (x - 9) * amplifier;

		if (mergedObservation[y][x] == 2) {
			points += sov.coins;
		}
		if (marioMode > 0) {
			if (mergedObservation[y - 1][x] == 2) {
				points += sov.coins;
			}
		}

		return new Move(x, y, points, parent);
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
