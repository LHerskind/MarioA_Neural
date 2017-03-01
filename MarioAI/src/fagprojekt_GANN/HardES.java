package fagprojekt_GANN;

import java.util.Random;

import ch.idsia.benchmark.tasks.Task;
import ch.idsia.evolution.Evolvable;
import ch.idsia.evolution.ea.ES;

public class HardES extends ES {

	// Initial her er vores FirstTryAgent

	public HardES(Task task, Evolvable initial, int populationSize) {
		super(task, initial, populationSize);
		elite = populationSize / 10;
	}

	public void nextGeneration() {
		for (int i = 0; i < elite; i++) {
			// evaluate(i);
		}
		/*
		 * for (int i = elite; i < population.length; i++) { population[i] =
		 * population[i % elite].copy(); population[i].mutate(); evaluate(i); }
		 */

		for (int i = elite; i < population.length; i++) {
//			population[i] = generateChildren(random.nextInt(elite), random.nextInt(elite));
			population[i] = population[i% elite].copy();
			population[i].mutate();
			evaluate(i);
		}

		sortPopulationByFitness();
	}

	private Evolvable generateChildren(int father, int mother) {
		FirstTryAgent fatherAgent = (FirstTryAgent) population[father];
		double[][][] fatherConn = fatherAgent.getMLNN().getConnections();

		FirstTryAgent motherAgent = (FirstTryAgent) population[mother];
		double[][][] motherConn = motherAgent.getMLNN().getConnections();

		double[][][] childrenConn = new double[fatherConn.length][][];
		for (int i = 0; i < fatherConn.length; i++) {
			childrenConn[i] = new double[fatherConn[i].length][];
			for (int j = 0; j < fatherConn[i].length; j++) {
				childrenConn[i][j] = new double[fatherConn[i][j].length];
				for (int k = 0; k < fatherConn[i][j].length; k++) {
					childrenConn[i][j][k] = getRandom() > 0.5 ? fatherConn[i][j][k] : motherConn[i][j][k];
				}
			}
		}

		Evolvable evolvable = new FirstTryAgent(new MultiLayerNeuralNetwork(childrenConn));
		return evolvable;
	}

	private Random random = new Random();

	private double getRandom() {
		return random.nextGaussian();
	}

}
