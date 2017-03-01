package fagprojekt_PathFinder;

public class State {
	
	private int x;
	private int y;
	
	public State(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	@Override
	public int hashCode() {
		String hash = x + ""+y;
		return Integer.parseInt(hash);
	}
	

}
