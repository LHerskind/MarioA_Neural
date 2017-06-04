package fagprojekt;

public class NormalEnemy extends Enemy {
	
	public NormalEnemy(float x, float y, byte kind, float ya, int facing, boolean dead /* ,boolean winged, int mapX, int mapY */) {
		super(x,y,kind,ya,facing,dead);
	}
	// RED KOOPA
	public NormalEnemy(float x, float y, byte kind, float ya, int facing, boolean dead, boolean onGround) {
		super(x, y, kind, ya, facing, dead);
		this.onGround = onGround;
	}
	public void move(byte[][] map) {
		float sideWaysSpeed = 1.75f;

		if (xa > 2)
			facing = 1;
		else if (xa < -2)
			facing = -1;
		xa = facing * sideWaysSpeed;
		if (!move(map, xa, 0))
			facing = -facing;
		onGround = false;
		move(map, 0, ya);
		ya *= winged ? 0.95f : 0.85f;
		xa *= (GROUND_INERTIA);

		if (!onGround) {
			if (winged) {
				ya += 0.6f * yaw;
			} else {
				ya += yaa;
			}
		} else if (winged) {
			ya = -10;
		}
	}

}