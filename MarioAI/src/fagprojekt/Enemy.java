/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Mario AI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package fagprojekt;

import fagprojekt.AStarAgent.State;

public abstract class Enemy {
	public static final int KIND_GOOMBA = 80;
	public static final int KIND_GOOMBA_WINGED = 95;
	public static final int KIND_RED_KOOPA = 82;
	public static final int KIND_RED_KOOPA_WINGED = 97;
	public static final int KIND_GREEN_KOOPA = 81;
	public static final int KIND_GREEN_KOOPA_WINGED = 96;
	public static final int KIND_BULLET_BILL = 84;
	public static final int KIND_SPIKY = 93;
	public static final int KIND_SPIKY_WINGED = 99;
	public static final int KIND_ENEMY_FLOWER = 91;
	public static final int KIND_WAVE_GOOMBA = 98;
	public static final int KIND_SHELL = 13;
	public static final int KIND_FIRE_FLOWER = 3;
	public static final int KIND_FIREBALL = 25; // SELF-MADE

	protected static float GROUND_INERTIA = 0.89f;
	protected static float AIR_INERTIA = 0.89f;

	public byte kind;
	public boolean onGround = false;
	public boolean carried;

	public int width = 4;
	public int height = 24;

	public float x;
	public float y;
	public float ya;
	public float xa;

	public int facing;

	public boolean avoidCliffs = false;

	public boolean winged;

	public boolean noFireballDeath = false;
	public boolean dead;

	public Enemy(float x, float y, byte kind, float ya, int facing, boolean dead) {
		this.dead = dead;
		this.facing = facing;
		this.kind = kind;
		this.x = x;
		this.y = y;
		this.ya = ya;

		if (kind == KIND_GOOMBA_WINGED || kind == KIND_SPIKY_WINGED || kind == KIND_RED_KOOPA_WINGED
				|| kind == KIND_GREEN_KOOPA_WINGED || kind == KIND_WAVE_GOOMBA)
			this.winged = true;
		if (kind == KIND_GREEN_KOOPA || kind == KIND_GREEN_KOOPA_WINGED || kind == KIND_RED_KOOPA
				|| kind == KIND_RED_KOOPA_WINGED)
			this.height = 24;
		else
			this.height = 12;

		avoidCliffs = kind == KIND_RED_KOOPA;
		noFireballDeath = (kind == KIND_SPIKY || kind == KIND_SPIKY_WINGED);
	}

	public int collideCheck(State state, CustomEngine ce) {
		float xMarioD = state.x - this.x;
		float yMarioD = state.y - this.y;
		if (xMarioD > -width * 2 - 4 && xMarioD < width * 2 + 4) {
			if (yMarioD > -height && yMarioD < state.height) {
				if ((kind != KIND_SPIKY && kind != KIND_SPIKY_WINGED && kind != KIND_ENEMY_FLOWER) && state.ya > 0
						&& yMarioD <= 0 && (!state.onGround || !state.wasOnGround)) {
					return 1;
					//
					// state.stomp = true;
					//
					// if (winged) {
					// winged = false;
					// ya = 0;
					// } else {
					// dead = true;
					// winged = false;
					// }
				} else {
					if (state.invulnerable <= 0) {
						if (state.height != 12) {
							state.invulnerable = 32;
							state.penalty(Values.penaltyLoseLife); // Can be
																	// changed
							if (state.marioMode == 2) {
								state.penalty(Values.penaltyLoseLife);
							}
							state.marioMode--;
						} else {
							state.penalty(Values.penaltyDie); // Can be changed
							state.marioMode = -1;
						}
					}
				}
			}
		}
		return 0;
	}

	public abstract void move(byte[][] map);

	public void move(State state, byte[][] map) {
	}

	public boolean move(byte[][] map, float xa, float ya) {
		while (xa > 8) {
			if (!move(map, 8, 0))
				return false;
			xa -= 8;
		}
		while (xa < -8) {
			if (!move(map, -8, 0))
				return false;
			xa += 8;
		}
		while (ya > 8) {
			if (!move(map, 0, 8))
				return false;
			ya -= 8;
		}
		while (ya < -8) {
			if (!move(map, 0, -8))
				return false;
			ya += 8;
		}

		boolean collide = false;
		if (ya > 0) {
			if (isBlocking(map, x + xa - width, y + ya, xa, 0))
				collide = true;
			else if (isBlocking(map, x + xa + width, y + ya, xa, 0))
				collide = true;
			else if (isBlocking(map, x + xa - width, y + ya + 1, xa, ya))
				collide = true;
			else if (isBlocking(map, x + xa + width, y + ya + 1, xa, ya))
				collide = true;
		}
		if (ya < 0) {
			if (isBlocking(map, x + xa, y + ya - height, xa, ya))
				collide = true;
			else if (collide || isBlocking(map, x + xa - width, y + ya - height, xa, ya))
				collide = true;
			else if (collide || isBlocking(map, x + xa + width, y + ya - height, xa, ya))
				collide = true;
		}
		if (xa > 0) {
			if (isBlocking(map, x + xa + width, y + ya - height, xa, ya))
				collide = true;
			if (isBlocking(map, x + xa + width, y + ya - height / 2, xa, ya))
				collide = true;
			if (isBlocking(map, x + xa + width, y + ya, xa, ya))
				collide = true;

			 if (avoidCliffs && onGround && !isBlocking(map, (x + xa + width), y + 16, xa, 1))
				 collide = true;
		}
		if (xa < 0) {
			if (isBlocking(map, x + xa - width, y + ya - height, xa, ya))
				collide = true;
			if (isBlocking(map, x + xa - width, y + ya - height / 2, xa, ya))
				collide = true;
			if (isBlocking(map, x + xa - width, y + ya, xa, ya))
				collide = true;

			 if (avoidCliffs && onGround && !isBlocking(map, x+xa-width, y+16, xa, 1))
				 collide = true;
		}

		if (collide) {
			if (xa < 0) {
				x = (int) ((x - width) / 16) * 16 + width;
				this.xa = 0;
			}
			if (xa > 0) {
				x = (int) ((x + width) / 16 + 1) * 16 - width - 1;
				this.xa = 0;
			}
			if (ya < 0) {
				y = (int) ((y - height) / 16) * 16 + height;
				this.ya = 0;
			}
			if (ya > 0) {
				y = (int) (y / 16 + 1) * 16 - 1;
				onGround = true;
			}
			return false;
		} else {
			x += xa;
			y += ya;

			return true;
		}

	}

	public boolean isBlocking(byte[][] map, final float _x, final float _y, final float xa, final float ya) {
		int x = (int) (_x / 16);
		int y = (int) (_y / 16);
		if (x == (int) (this.x / 16) && y == (int) (this.y / 16)) {
			return false;
		}

		if (this.x >= 0 && this.x < 600 * 16 && y >= 0 && y < 16) {
			byte block = map[y][x];
			if (ya <= 0) {
				if (block == -62) {
					return false;
				}
			}
			return block < 0;
		} else {
			return false;
		}

	}

	public boolean shellCollideCheck(State state, Shell shell) {
		if (dead)
			return false;
		float xD = shell.x - x;
		float yD = shell.y - y;

		if (xD > -16 && xD < 16) {
			if (yD > -height && yD < shell.height) {
				// dead = true;
				return true;
			}
		}
		return false;
	}

	public int fireballCollideCheck(Fireball fireball) {
		if (dead) {
			return 0;
		}
		float xD = fireball.x - x;
		float yD = fireball.y - y;

		if (xD > -16 && xD < 16) {
			if (yD > -height && yD < fireball.height) {
				if (noFireballDeath)
					return 2;
				// dead = true;
				return 1;
			}
		}
		return 0;
	}

	public void release(State state) {
	}
}