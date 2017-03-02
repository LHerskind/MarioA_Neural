package fagprojekt_PathFinder_2;

public class State {

	private float x;
	private float y;
	private float vx;
	private float vy;
	private int jump;

	public State() {
		jump = 0;
	}

	public State(float x, float y, float vx, float vy) {
		this.x = x;
		this.y = y;
		this.vx = vx;
		this.vy = vy;
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

/*	@Override
	public int hashCode() {
		String hash = ((int) Math.abs(x)) + "" + ((int) Math.abs(y)) + "" + ((int) Math.abs(vx)) + "" + ((int) Math.abs((vy)));
		return Integer.parseInt(hash);
	}
*/
}
