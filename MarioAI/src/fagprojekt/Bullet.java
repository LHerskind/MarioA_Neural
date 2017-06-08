package fagprojekt;

import ch.idsia.benchmark.mario.engine.LevelScene;
import fagprojekt.AStarAgent.State;

public class Bullet extends Enemy {

	public Bullet(float x, float y, byte kind, float ya, int facing, boolean dead) {
		super(x, y, kind, ya, facing, dead);
		this.height = 12;
		noFireballDeath = true;
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

//	@Override
//	public boolean collideCheck(State state) {
//		System.out.println("SHIT");
//		float xMarioD = state.x - this.x;
//		float yMarioD = state.y - this.y;
//		if (xMarioD > -16 && xMarioD < 16) {
//			if (yMarioD > -height && yMarioD < state.height) {
//				if (state.ya > 0 && yMarioD <= 0 && (!state.onGround || !state.wasOnGround)) {
//					return true;
///*					state.stomp = true;
//					dead = true;*/
//				} else {
//					if (state.invulnerable <= 0) {
//						if (state.height != 12) {
//							state.invulnerable = 32;
//							state.penalty(500);
//						} else {
//							state.penalty(2000);
//						}
//					}
//				}
//			}
//		}
//		return false;
//	}
}
