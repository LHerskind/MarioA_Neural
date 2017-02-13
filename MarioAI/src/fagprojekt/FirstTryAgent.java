package fagprojekt;

import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.evolution.Evolvable;

public class FirstTryAgent extends BasicMarioAIAgent implements Evolvable {

	// Virkede det
	
	private MultiLayerNeuralNetwork MLNN;
	private int numberOfInputs = 3;
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

		int which = 0;
		
		for (int i = 0; i < 4; i+=2) {
				inputs[which++] = (sample(i, i, scene) != 0 || sample(i+1, i, scene) != 0 || sample(i, i+1, scene) != 0 || sample(i+1, i+1, scene) != 0) ? 1 : 0;
//				System.out.print(inputs[which - 1] + " ");
//			System.out.println();
		}
//		System.out.println();

		inputs[inputs.length - 1] = isMarioAbleToShoot ? 1 : 0;

		return MLNN.getOutput(inputs);
	}

	private int sample(int y, int x, byte[][] scene) {
		int realX = x + marioEgoRow;
		int realY = y + marioEgoCol - 1;
		int point = scene[realY][realX];
		if (point == 101) { // Mario
			return 0;
		} else if (point >= 26) {
			return -1;
		} else if(point < 0) {
			return 1;
		} else if (point == 25) {
			return 0;
		} else if (point <= 5 && point > 0) {
			return point;
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
