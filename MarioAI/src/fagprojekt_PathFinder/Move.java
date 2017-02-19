package fagprojekt_PathFinder;

public class Move {
	
	private int x;
	private int y;
	private double points;
	
	public Move(int x, int y, double points){
		this.x = x;
		this.y = y;
		this.points = points;
	}
	
	public double getPoints(){
		return points;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	
	

}
