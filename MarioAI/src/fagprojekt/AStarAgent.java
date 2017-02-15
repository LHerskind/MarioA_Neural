package fagprojekt;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.environments.Environment;

public class AStarAgent extends BasicMarioAIAgent implements Agent{
	public AStarAgent(String s) {
		super("AStarAgent");
		reset();
	}

	private AStar aStar;
	
	@Override
	public void reset() {
		aStar = new AStar();
	}
	@Override
	public boolean[] getAction() {
		boolean[] ret = new boolean[Environment.numberOfKeys];
		return ret;
	}
}
