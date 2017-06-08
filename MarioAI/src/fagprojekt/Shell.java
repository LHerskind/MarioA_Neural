package fagprojekt;

import fagprojekt.AStarAgent.State;

public class Shell extends Enemy {
	public Shell(float x, float y, byte kind, float ya, int facing, boolean dead) {
		super(x, y, kind, ya, facing, dead);
		this.height = 12;
		ya = -5;
	}

//	public boolean collideCheck(State state) {
//		float xMarioD = state.x - this.x;
//		float yMarioD = state.y - this.y;
//		float w = 16;
//		if (xMarioD > -w && xMarioD < w) {
//			if (yMarioD > -height && yMarioD < state.height) {
//				if (state.ya > 0 && yMarioD <= 0 && (!state.onGround || !state.wasOnGround)) {
//					return true;
//				} else {
//					if (facing != 0 && state.invulnerable <= 0) {
//						if (state.height != 12) {
//							state.invulnerable = 32;
//							state.penalty(1000);
//						} else {
//							state.penalty(2000);
//						}
//					}
//				}
//			}
//		}
//		return false;
//	}

	@Override
	public void move(byte[][] map) {
		// TODO Auto-generated method stub

	}

}
