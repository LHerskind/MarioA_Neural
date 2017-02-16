package fagprojekt_GANN;

import ch.idsia.agents.Agent;
import ch.idsia.agents.LearningAgent;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.tasks.LearningTask;
import ch.idsia.benchmark.tasks.Task;
import ch.idsia.evolution.ea.ES;

public class CustomLearner implements LearningAgent {

	private FirstTryAgent agent;
	Agent bestAgent;
	private static float bestScore = 0;
	private Task task;

	private ES es;
	int populationSize = 500;
	int generations = 30;

	long evaluationQuota; // common number of trials
	long currentEvaluation; // number of exhausted trials
	private String name = getClass().getSimpleName();

	public CustomLearner() {
		this.agent = new FirstTryAgent();
	}

	public void init() {
		es = new HardES(task, agent, populationSize);
	}

	public void learn() {
		this.currentEvaluation++;

		for (int gen = 0; gen < generations; gen++) {
			System.out.println(gen + " generation");
			es.nextGeneration();

			float fitn = es.getBestFitnesses()[0];

			if (fitn > bestScore) {
				System.out.println(fitn);
				bestScore = fitn;
				bestAgent = (Agent) es.getBests()[0];
			}
		}
	}

	public void giveReward(float r) {
	}

	public void newEpisode() {
		task = null;
		agent.reset();
	}

	public void setLearningTask(LearningTask learningTask) {
		this.task = learningTask;
	}

	public Agent getBestAgent() {
		return bestAgent;
	}

	public boolean[] getAction() {
		return agent.getAction();
	}

	@Override
	public void integrateObservation(Environment environment) {
		agent.integrateObservation(environment);
	}

	@Override
	public void giveIntermediateReward(float intermediateReward) {
		agent.giveIntermediateReward(intermediateReward);
	}

	@Override
	public void reset() {
		agent.reset();
	}

	@Override
	public void setObservationDetails(int rfWidth, int rfHeight, int egoRow, int egoCol) {
		agent.setObservationDetails(rfWidth, rfHeight, egoRow, egoCol);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setEvaluationQuota(long num) {
		this.evaluationQuota = num;
	}

}
