package fagprojekt;

public class BlueGoomba extends Enemy {

	private float amplitude = 10f;
	private float lastSin;
	public BlueGoomba(float x, float y, byte kind, float ya, int facing, boolean dead) {
		super(x, y, kind, ya, facing, dead);
		this.height = 12;
	}

	@Override
	public void move(byte[][] map) {

		float sideWaysSpeed = 0.55f;

		if (xa > 2) {
			facing = 1;
		}
		if (xa < -2) {
			facing = -1;
		}

		xa = facing * sideWaysSpeed;
		lastSin = (float) Math.sin(x / 10);
		if (!move(map, xa, 0))
			facing = -facing;
		onGround = false;
		if (winged) {
			float curSin = (float) Math.sin(x / 10);
			ya = (curSin - lastSin) * amplitude;
		}
		move(map, 0, ya);

		ya *= winged ? 0.95 : 0.85f;
		if (onGround)
	    {
	        xa *= (GROUND_INERTIA);
	    } else
	    {
	        xa *= (AIR_INERTIA);
	    }

	    if (!onGround && !winged)
	    {
	        ya += yaa;
	    }
	}
}
