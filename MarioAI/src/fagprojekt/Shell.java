package fagprojekt;

import fagprojekt.AStarAgent.State;

public class Shell extends Enemy {
	public Shell(float x, float y, byte kind, float ya, int facing, boolean carried, boolean dead) {
		super(x, y, kind, ya, facing, dead);
		this.height = 12;
		this.carried = carried;
	}
	@Override
	public void collideCheck(State state, CustomEngine ce) {
		if (carried || dead)
			return;
		float xMarioD = state.x - this.x;
		float yMarioD = state.y - this.y;
		float w = 16;
		if (xMarioD > -w && xMarioD < w) {
			if (yMarioD > -height && yMarioD < state.height) {
				if (state.ya > 0 && yMarioD <= 0 && (!state.onGround || !state.wasOnGround)) {
					ce.stompShell(state, this);
					if (facing != 0) {
						xa = 0;
						facing = 0;
					} else {
						facing = state.facing;
					}
				} else {
					if (facing != 0) {
						if (state.invulnerable <= 0) {
							if (state.height != 12) {
								state.invulnerable = 32;
								state.penalty(1000); // Can be changed
							} else {
								state.penalty(2000); // Can be changed
							}
						}
					} else {
						ce.kick(state, this);
						facing = state.facing;
					}
				}
			}
		}
	}
	@Override
	public void move(State state, byte[][] map)
	{
	    if (carried) {
	        state.shellsToCheck.add(this);
	        return;
	    }
	    if(dead)
	    	return;

	    float sideWaysSpeed = 11f;

	    if (xa > 2)
	    {
	        facing = 1;
	    }
	    if (xa < -2)
	    {
	        facing = -1;
	    }
	    xa = facing * sideWaysSpeed;

	    if (facing != 0)
	    {
	        state.shellsToCheck.add(this);
	    }


	    if (!move(map, xa, 0)) {
	        facing = -facing;
	    }
	    onGround = false;
	    move(map, 0, ya);

	    ya *= 0.85f;
	    if (onGround)
	    {
	        xa *= GROUND_INERTIA;
	    } else
	    {
	        xa *= AIR_INERTIA;
	    }

	    if (!onGround)
	    {
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
	public boolean fireballCollideCheck(Fireball fireball){
		float xD = fireball.x - x;
	    float yD = fireball.y - y;

	    if (xD > -16 && xD < 16) {
	        if (yD > -height && yD < fireball.height) {
	            if (facing != 0) return true;
	            xa = fireball.facing * 2;
	            ya = -5;
	            return true;
	        }
	    }
	    return false;
	}
	@Override
	public boolean shellCollideCheck(State state, Shell shell)
	{
	    if (dead)
	    	return false;

	    float xD = shell.x - x;
	    float yD = shell.y - y;

	    if (xD > -16 && xD < 16)
	    {
	        if (yD > -height && yD < shell.height)
	        {
	            if (state.carried == shell || state.carried == this) {
	                state.carried = null;
	            }
	            die();
	            shell.die();
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
