package fagprojekt_PathFinder;

import java.util.ArrayList;

import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.tasks.SystemOfValues;

public class PFAgent extends BasicMarioAIAgent {

	private int amplifier = 10;
	private SystemOfValues sov = new SystemOfValues();

	private static String name = "PFAgent";

	public PFAgent(String s) {
		super(s);
	}

	public PFAgent() {
		super(name);
	}

	private Move bestMove = new Move(9, 9, 0);

	@Override
	public boolean[] getAction() {
		boolean[] action = new boolean[9];

		bestMove = new Move(9, 9, 0);
		calculateBestMove(9, 9);

		if (bestMove != null) {
			if (bestMove.getX() > 9) {
				action[Mario.KEY_RIGHT] = true;
			} else if (bestMove.getX() < 9) {
				action[Mario.KEY_LEFT] = true;
			}

			if (bestMove.getY() == 10 && bestMove.getX() == 10) {
				System.out.println("HMM");
				action[Mario.KEY_RIGHT] = false;
				action[Mario.KEY_LEFT] = true;
			}

			if (bestMove.getY() < 9) {
				action[Mario.KEY_JUMP] = true;
				if (!isMarioAbleToJump && isMarioOnGround) {
					action[Mario.KEY_JUMP] = false;
				}
			}
		}

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

	ArrayList<Move> moves = new ArrayList<>();

	private void calculateMoves() {

	}

	private void calculateBestMove(int currentX, int currentY) {
		Move move;

		if (possibleMove(currentX + 1, currentY - 1)) {
			if ((move = getMove(currentX + 1, currentY - 1)).getPoints() > bestMove.getPoints()) {
				bestMove = move;
			}
		}

		if (possibleMove(currentX + 1, currentY)) {
			if ((move = getMove(currentX + 1, currentY)).getPoints() > bestMove.getPoints()) {
				bestMove = move;
			}
		}

		if (possibleMove(currentX + 1, currentY + 1)) {
			if ((move = getMove(currentX + 1, currentY + 1)).getPoints() > bestMove.getPoints()) {
				bestMove = move;
			}
		}

		if (possibleMove(currentX + 1, currentY - 2)) {
			if ((move = getMove(currentX + 1, currentY - 2)).getPoints() >= bestMove.getPoints()) {
				bestMove = move;
			}
		}

		if (possibleMove(currentX + 1, currentY - 3)) {
			if ((move = getMove(currentX + 1, currentY - 3)).getPoints() >= bestMove.getPoints()) {
				bestMove = move;
			}
		}

	}

	private Move getMove(int x, int y) {
		double points = 0;
		points += sov.distance * (x - 9) * amplifier;

		if (mergedObservation[y][x] == 2) {
			points += sov.coins;
		}

		if (mergedObservation[y][x] > 26) {
			points += sov.killedByStomp + sov.kills;
		}

		return new Move(x, y, points);
	}

	private boolean possibleMove(int x, int y) {
		if (mergedObservation[y][x] < 0) {
			return false;
		}

		if (y <= 9) {
			if (mergedObservation[y][x] > 26) {
				return false;
			}
		}

		return true;
	}

}
