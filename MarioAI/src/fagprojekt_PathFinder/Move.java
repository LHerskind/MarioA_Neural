package fagprojekt_PathFinder;

public class Move {

	private int x;
	private int y;
	private double points;
	private Move parent;

	public Move(int x, int y, double points, Move parent) {
		this.x = x;
		this.y = y;
		this.points = points;
		this.parent = parent;
	}

	public double getPoints() {
		return points;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Move getParent() {
		return parent;
	}

}
