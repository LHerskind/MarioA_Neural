package fagprojekt_PathFinder;

import java.util.ArrayList;

import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.tasks.SystemOfValues;

public class PFAgent extends BasicMarioAIAgent {

	public PFAgent(String s) {
		super(s);
		// TODO Auto-generated constructor stub
	}

	private SystemOfValues sov = new SystemOfValues();

	@Override
	public boolean[] getAction() {
		boolean[] action = new boolean[9];

		return action;
	}

	private void calculateNextState(byte[][] scene) {
		int[][] level = getSmallView(levelScene, 0, 0);

	}

	private ArrayList<Integer[]> getPossibleStates(int[][] view) {
		ArrayList<Integer[]> possibleMoves = new ArrayList<>();

		// TODO: Find noget smart 
		
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (view[i][j] != 0) {
					possibleMoves.add(new Integer[] { 0, i });
				}
			}
		}
		return possibleMoves;
	}

	private int[][] getSmallView(byte[][] scene, int x, int y) {
		int[][] view = new int[3][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				view[i][j] = (int) sample(i + 1 + y, j - 1 + x, scene);
			}
		}
		return view;
	}

	private double sample(int y, int x, byte[][] scene) {
		int realX = x + marioEgoRow;
		int realY = y + marioEgoCol - 2;
		int point = scene[realY][realX];
		return point;
	}

}
