package fagprojekt;

public class BlueBeetle extends Enemy {

	private float amplitude = 10f;
	private float lastSin;
	private int sideWayCounter = 0;

	public BlueBeetle(float x, float y, byte kind, float ya, int facing, boolean dead) {
		super(x, y, kind, ya, facing, dead);
		noFireballDeath = false;
		lastSin = (float) Math.sin(x);
	}

	@Override
	public void move(byte[][] map) {
		x += xa;
		y += ya;

		float sideWaysSpeed = onGround ? 1.75f : 0.55f;

		if (xa > 2) {
			facing = 1;
		}
		if (xa < -2) {
			facing = -1;
		}

		xa = facing * sideWaysSpeed;

		if (!move(map, xa, 0))
			facing = -facing;
		onGround = false;
		if (winged) {
			float curSin = (float) Math.sin(x / 10);
			ya = (curSin - lastSin) * amplitude;
			lastSin = curSin;
			sideWayCounter++;
		}
		move(map, 0, ya);

		if (sideWayCounter >= 100) {
			sideWayCounter = 0;
			facing *= -1;
		}

		ya *= winged ? 0.95 : 0.85f;
	}
}
