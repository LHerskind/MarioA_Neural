package fagprojekt_PathFinder;

public class Move {

	private double points;
	private Move parent;
	private State state;
	private int depth;

	public Move(double points, Move parent, State state) {
		this.points = points;
		this.parent = parent;
		this.state = state;
		if(parent == null){
			depth = 0;
		} else {
			depth = parent.getDepth() + 1;
		}
	}
	
	public int getDepth(){
		return depth;
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
