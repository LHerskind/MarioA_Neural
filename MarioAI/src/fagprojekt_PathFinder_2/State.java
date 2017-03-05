package fagprojekt_PathFinder_2;

public class State {

	private float x;
	private float y;
	private float vx;
	private float vy;
	private int jump;
	
	private boolean[] action;

	public State() {
		jump = 0;
	}

	public State(float x, float y, float vx, float vy, boolean[] action ) {
		this.x = x;
		this.y = y;
		this.vx = vx;
		this.vy = vy;
		this.action = action;
		jump = 0;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getVX() {
		return vx;
	}

	public float getVY() {
		return vy;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public void setVX(float vx) {
		this.vx = vx;
	}

	public void setVY(float vy) {
		this.vy = vy;
	}

	public void setJump(int jump) {
		this.jump = jump;
	}

	public int getJump() {
		return jump;
	}
	
	public void setAction(boolean[] action){
		this.action = action;
	}
	
	public boolean[] getAction(){
		return action;
	}

	@Override
	public int hashCode() {
		String hash = "";
		if (x < 0) {
			hash += "0";
			x *= (-1);
		}
		hash += (int) x;
		if (y < 0) {
			hash += "0";
			y *= (-1);
		}
		hash += (int) y;
//		if (vx < 0) {
//			hash += "0";
//			vx *= (-1);
//		}
//		hash += (int) vx;
//		if (vy < 0) {
//			hash += "0";
//			vy *= (-1);
//		}
//		hash += (int) vy;
		return Integer.parseInt(hash);
	}

}
