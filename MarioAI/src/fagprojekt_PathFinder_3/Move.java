package fagprojekt_PathFinder_3;

public class Move {

	private double points;
	private Move parent;
	private State state;

	public Move(double points, Move parent, State state) {
		this.points = points;
		this.parent = parent;
		this.state = state;
	}

	public double getPoints() {
		return points;
	}

	public State getState(){
		return state;
	}

	public Move getParent() {
		return parent;
	}

}
