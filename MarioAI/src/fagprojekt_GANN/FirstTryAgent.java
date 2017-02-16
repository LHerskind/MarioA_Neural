package fagprojekt_GANN;

import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.evolution.Evolvable;

public class FirstTryAgent extends BasicMarioAIAgent implements Evolvable {

	// Virkede det

	private MultiLayerNeuralNetwork MLNN;
	private int numberOfInputs = 10;
	private int numberOfOutputs = 6; // Environment.numberOfKeys;

	public FirstTryAgent() {
		super("FirstTryAgent");
		MLNN = new MultiLayerNeuralNetwork(numberOfInputs, numberOfOutputs);
	}

	public FirstTryAgent(MultiLayerNeuralNetwork MLNN) {
		super("FirstTryAgent");
		this.MLNN = MLNN;
	}

	public MultiLayerNeuralNetwork getMLNN() {
		return MLNN;
	}

	@Override
	public boolean[] getAction() {

		byte[][] scene = mergedObservation;
		// byte[][] enemies = observation.getEnemiesObservation(/*0*/);
		double[] inputs = new double[numberOfInputs];
	
		int number = 0;
		for (int i = 1; i < 4; i++) {
			for (int j = 0; j < 3; j++) {
				inputs[number++] = sample(i, j, mergedObservation);
//				inputs[number++] = sample(i, j, levelScene);
			}
		}
		inputs[inputs.length - 1] = isMarioOnGround ? 1 : 0;

		// 0 0 0 0 0
		// 0 0 0 0 0
		// M 0 0 0 0
		// 0 0 0 0 0
		// 0 0 0 0 0

		return MLNN.getOutput(inputs);
	}

	private double sample(int y, int x, byte[][] scene) {
		int realX = x + marioEgoRow;
		int realY = y + marioEgoCol - 2;
		int point = scene[realY][realX];
		
		if (point < 0 || point > 25) {
			return 1;
		} else {
			return 0;
		}
		/*
		 * if (point == 101) { // Mario return 7; } else if (point >= 26) { //
		 * Fjender return 0.5; } else if (point < 0) { // Væg return 1; } else {
		 * return 0; }
		 */
	}

	@Override
	public Evolvable getNewInstance() {
		return new FirstTryAgent(MLNN.getNewInstance());
	}

	@Override
	public Evolvable copy() {
		return new FirstTryAgent(MLNN.copy());
	}

	@Override
	public void mutate() {
		MLNN.mutate();
	}

}
