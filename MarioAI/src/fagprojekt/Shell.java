package fagprojekt;

import fagprojekt.AStarAgent.State;

public class Shell extends Enemy {
	public Shell(float x, float y, byte kind, float ya, int facing, boolean carried, boolean dead) {
		super(x, y, kind, ya, facing, dead);
		this.height = 12;
		this.carried = carried;
	}

	@Override
	public int collideCheck(State state, CustomEngine ce) {
		if (carried || dead)
			return 0;
		float xMarioD = state.x - this.x;
		float yMarioD = state.y - this.y;
		float w = 16;
		if (xMarioD > -w && xMarioD < w) {
			if (yMarioD > -height && yMarioD < state.height) {
				if (state.ya > 0 && yMarioD <= 0 && (!state.onGround || !state.wasOnGround)) {
					return 2;
					// ce.stompShell(state, this);
					// if (facing != 0) {
					// xa = 0;
					// facing = 0;
					// } else {
					// facing = state.facing;
					// }
				} else {
					if (facing != 0) {
						if (state.invulnerable <= 0) {
							if (state.height != 12) {
								state.invulnerable = 32;
								state.penalty(Values.penaltyLoseLife); // Can be changed
								if(state.marioMode == 2){
									state.penalty(Values.penaltyLoseLife);
								}
								state.marioMode--;
							} else {
								state.penalty(Values.penaltyDie); // Can be changed
								state.marioMode = -1;
							}
						}
					} else {
						return 3;
						// ce.kick(state, this);
						// facing = state.facing;
					}
				}
			}
		}
		return 0;
	}

	@Override
	public void move(State state, byte[][] map) {
		if (carried) {
			state.shellsToCheck.add(this);
			return;
		}
		if (dead)
			return;

		float sideWaysSpeed = 11f;

		if (xa > 2) {
			facing = 1;
		}
		if (xa < -2) {
			facing = -1;
		}
		xa = facing * sideWaysSpeed;

		if (facing != 0) {
			state.shellsToCheck.add(this);
		}

		if (!move(map, xa, 0)) {
			facing = -facing;
		}
		onGround = false;
		move(map, 0, ya);

		ya *= 0.85f;
		if (onGround) {
			xa *= GROUND_INERTIA;
		} else {
			xa *= AIR_INERTIA;
		}

		if (!onGround) {
			ya += 2;
		}
	}

	@Override
	public void release(State state) {
		this.carried = false;
		facing = state.facing;
		x += facing * 8;
	}

	public void die() {
		dead = true;
		this.carried = false;
	}

	@Override
	public int fireballCollideCheck(Fireball fireball) {
		float xD = fireball.x - x;
		float yD = fireball.y - y;

		if (xD > -16 && xD < 16) {
			if (yD > -height && yD < fireball.height) {
				if (facing != 0)
					return 2;
				xa = fireball.facing * 2;
				ya = -5;
				return 3; // Shell bounce
			}
		}
		return 0;
	}

	@Override
	public boolean shellCollideCheck(State state, Shell shell) {
		System.out.println("SAHI");
		if (dead)
			return false;

		float xD = shell.x - x;
		float yD = shell.y - y;

		if (xD > -16 && xD < 16) {
			if (yD > -height && yD < shell.height) {
				if (state.carried == shell || state.carried == this) {
					state.carried = null;
				}
				// die();
				// shell.die();
				return true;
			}
		}
		return false;
	}

	@Override
	public void move(byte[][] map) {
		// TODO Auto-generated method stub

	}
}
