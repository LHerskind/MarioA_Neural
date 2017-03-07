package fagprojekt_PathFinder_2;

import ch.idsia.benchmark.mario.engine.sprites.Mario;

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
		hash += (int) Math.round(x*100);
		if (y < 0) {
			hash += "0";
			y *= (-1);
		}
		hash += (int) Math.round(y*100);
		
/*		int j = 0;
		if(action[Mario.KEY_LEFT]){
			j +=1;
		}
		if(action[Mario.KEY_JUMP]){
			j += 2;
		}
		if(action[Mario.KEY_RIGHT]){
			j += 4;
		}
		hash += j;*/
		return Integer.parseInt(hash);
	}

}
