package fagprojekt;

import ch.idsia.benchmark.tasks.Task;
import ch.idsia.evolution.Evolvable;
import ch.idsia.evolution.ea.ES;

public class HardES extends ES {
	
	// Initial her er vores FirstTryAgent
	
	public HardES(Task task, Evolvable initial, int populationSize) {
		super(task, initial, populationSize);
		elite = populationSize/5;
	}

	public void nextGeneration() {
		for (int i = 0; i < elite; i++) {
//			evaluate(i);
		}
		for (int i = elite; i < population.length; i++) {
			population[i] = population[i % elite].copy();
			population[i].mutate(); 
			evaluate(i);
		}
		sortPopulationByFitness();
	}

}
