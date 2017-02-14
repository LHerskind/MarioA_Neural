package fagprojekt;

import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.evolution.Evolvable;

public class FirstTryAgent extends BasicMarioAIAgent implements Evolvable {

	// Virkede det

	private MultiLayerNeuralNetwork MLNN;
	private int numberOfInputs = 6;
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

//		inputs[0] = (sample(2, 1, scene) != 0 && (sample(1, 1, scene) != 0 || sample(3, 1, scene) != 0)) ? 1 : 0;

		inputs[0] = sample(2, 1, scene) != 0 ? 1 : 0; // 1 Foran
		inputs[1] = (sample(1, 1, scene) != 0) ? 1 : 0; // 1 Frem 1 Op

		inputs[2] = sample(2, 2, scene) != 0 ? 1 : 0; // 2 Foran

		inputs[3] = sample(3, 0, scene) == 0 ? 1 : 0; // Under ham

		inputs[4] = sample(3, -1, scene) == -1 ? 1 : 0; // Bag ham

		inputs[inputs.length - 1] = isMarioAbleToShoot ? 1 : 0;

		/*
		 * 
		 * 0 0 0 0 0 0 0 0 0 0 7 0 0 0 0 0 0 0 0 0 0 0 0 0 0
		 */

		return MLNN.getOutput(inputs);
	}

	private int sample(int y, int x, byte[][] scene) {
		int realX = x + marioEgoRow;
		int realY = y + marioEgoCol - 2;
		int point = scene[realY][realX];
		if (point == 101) { // Mario
			return 7;
		} else if (point >= 26) {
			return -1;
		} else if (point < 0) {
			return 1;
		} else if (point == 25) {
			return 0;
		} else if (point <= 5 && point > 0) {
			return 0;
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
