package fagprojekt;

public class Bullet extends Enemy {

	public Bullet(float x, float y, byte kind, float ya, int facing, boolean dead) {
		super(x, y, kind, ya, facing, dead);
		kind = KIND_BULLET_BILL;

		this.x = x;
		this.y = y;
		this.height = 12;
		this.facing = facing;
	}

	@Override
	public void move(byte[][] map) {
		float sideWaysSpeed = 4f;

		xa = facing * sideWaysSpeed;
		move(xa, 0);
	}

	private boolean move(float xa, float ya) {
		x += xa;
		return true;
	}

}
