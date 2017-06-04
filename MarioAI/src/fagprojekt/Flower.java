
package fagprojekt;

public class Flower extends Enemy {

	public Flower(float x, float y, byte kind, float ya, int facing, boolean dead) {
		super(x, y, kind, ya, facing, dead);
		this.height = 12;
	    this.width = 2;
	}

	@Override
	public void move(byte[][] map) {
		y += ya;
		ya *= 0.9f;
		ya += 0.1f;
	}
}
