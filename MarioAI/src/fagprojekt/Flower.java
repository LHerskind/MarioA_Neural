
package fagprojekt;

public class Flower extends Enemy {

	private float yStart;
	private int jumpTime = 0;

	public Flower(float x, float y, byte kind, float ya, int facing, boolean dead) {
		super(x, y, kind, ya, facing, dead);
		this.yStart = y;
	}

	@Override
	public void move(byte[][] map) {
		if (y >= yStart) {
			y = yStart;

			// int xd = (int) (Math.abs(world.mario.x - x));
			jumpTime++;
			if (jumpTime > 40) { // && xd > 24) {
				ya = -8;
			} else {
				ya = 0;
			}
		} else {
			jumpTime = 0;
		}

		y += ya;
		ya *= 0.9;
		ya += 0.1f;
	}

}
