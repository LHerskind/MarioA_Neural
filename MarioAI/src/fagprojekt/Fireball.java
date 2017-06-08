package fagprojekt;

import fagprojekt.AStarAgent.State;

public class Fireball extends Enemy {

	public Fireball(float x, float y, byte kind, float ya, int facing, boolean dead) {
		super(x, y, kind, ya, facing, dead);

		height = 8;
	}

	public void move(State state, byte[][] map) {
		if (dead)
			return;

		float sideWaysSpeed = 8f;

		if (xa > 2) {
			facing = 1;
		}
		if (xa < -2) {
			facing = -1;
		}

		xa = facing * sideWaysSpeed;

		state.fireballsToCheck.add(this);

		if (!move(map, xa, 0)) {
			dead = true;
		}

		onGround = false;
		move(map, 0, ya);
		if (onGround)
			ya = -10;

		ya *= 0.95f;
		if (onGround) {
			xa *= GROUND_INERTIA;
		} else {
			xa *= AIR_INERTIA;
		}

		if (!onGround) {
			ya += 1.5;
		}
	}

	@Override
	public void move(byte[][] map) {
		// TODO Auto-generated method stub

	}

}