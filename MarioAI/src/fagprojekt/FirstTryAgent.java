package fagprojekt;

import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.evolution.Evolvable;

public class FirstTryAgent extends BasicMarioAIAgent implements Evolvable {

	private MultiLayerNeuralNetwork MLNN;
	private int numberOfInputs = 4;
	private int numberOfOutputs = Environment.numberOfKeys;

	public FirstTryAgent() {
		super("FirstTryAgent");
		MLNN = new MultiLayerNeuralNetwork(numberOfInputs, numberOfOutputs);
	}

	public FirstTryAgent(MultiLayerNeuralNetwork MLNN) {
		super("FirstTryAgent");
		this.MLNN = MLNN;
	}

	@Override
	public boolean[] getAction() {

		byte[][] scene = mergedObservation;
		// byte[][] enemies = observation.getEnemiesObservation(/*0*/);
		double[] inputs = new double[numberOfInputs];

		/*
		 * int which = 0; for (int i = 0; i < 5; i++) { for (int j = 0; j < 5;
		 * j++) { inputs[which++] = sample(i, j, scene);
		 * System.out.print(inputs[which-1] + " "); } System.out.println(); }
		 * System.out.println();
		 */
		inputs[inputs.length - 4] = (sample(3, 2, scene) == 1 && sample(2, 2, scene) == 1 || sample(1, 2, scene) == 1 && sample(2, 2, scene) == 1) ? 1 : 0;
		inputs[inputs.length - 3] = (sample(3, 2, scene) == 1 || sample(3, 3, scene) == 1) ? 1 : 0;
		inputs[inputs.length - 2] = isMarioAbleToJump ? 1 : 0;
		inputs[inputs.length - 1] = isMarioAbleToShoot ? 1 : 0;

		return MLNN.getOutput(inputs);
	}

	private int sample(int x, int y, byte[][] scene) {
		int realX = x + marioEgoRow - 3;
		int realY = y + marioEgoCol - 1;
		int point = scene[realX][realY];
		if (point == 101) { // Mario
			return 0;
		} else if (point >= 26 || point < 1) {
			return 1;
		} else if (point == 25) {
			return 0;
		} else if (point == 5) {
			return 2;
		} else {
			return 0;
		}
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
