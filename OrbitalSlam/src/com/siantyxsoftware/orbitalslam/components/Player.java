package com.siantyxsoftware.orbitalslam.components;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;

import com.siantyxsoftware.framework.Game;
import com.siantyxsoftware.framework.Graphics;
import com.siantyxsoftware.framework.Input.TouchEvent;
import com.siantyxsoftware.orbitalslam.Assets;

public class Player {
	private boolean dead;
	private Entity entity;
	//private final String[] playerImg = new String[]{"res/sprites/smiley1.png", "res/sprites/smiley2.png", "res/sprites/smiley3.png", "res/sprites/smiley4.png", "res/sprites/smiley5.png", "res/sprites/smiley6.png", "res/sprites/smiley7.png", "res/sprites/smiley8.png"};
	public static final float stdScale = 0.0005208f;
	//public static final Color[] PLAYER_COLORS = new Color[]{Color.red, Color.blue, new Color(25, 235, 184), new Color(84, 0, 140), Color.yellow, Color.orange, Color.green, Color.pink, Color.gray, new Color(89, 42, 4)};
	//public Color myColor;
	private int num;
	private int score;
	//private Sound sound;

	private double dx;
	private double dy;

	private double speed;
	private double mass;

	private double stunTime;

	// hook variables
	private boolean hooked;
	private Entity hookedTo;

	private double wSpeed;
	private double degrees;
	private double hookLength;
	
	private boolean clockWise;

	private double centriAcc;
	private final double MAXSPINSPEED = 16;
	private final double ACC_CONST = 300;
	// -----------------------

	public static ArrayList<Entity> anchorList;

	public int KEYBIND;

	private final float gravity = 0.07f;
	private final float SPEED_LOST = 0.6f;
	private final double STUN_LENGTH = 50;

	public Player(Player player) {
		entity = new Entity(player.getEntity().getId());
		entity.AddComponent(new ImageRenderComponent("Player " + num, Assets.player));
		entity.setCenterPosition(player.getEntity().getPosition());
		entity.setScale(stdScale*AnchorMap.w);
		dx = player.getDx();
		dy = player.getDy();
		mass = 1;
		speed = player.getSpeed();
	}

	public Player(int num, AnchorMap map) {
		Vector2f startPos = new Vector2f(map.getStartPosX() + (num) * (((AnchorMap.w-(2*map.getStartPosX()))/(map.getNumAncPerRow()-1))) - AnchorMap.w/14, map.getStartPosY() - AnchorMap.h/10);
		entity = new Entity("Player " + num);
		entity.AddComponent(new ImageRenderComponent("Player " + num, Assets.player));
		entity.setCenterPosition(new Vector2f(AnchorMap.w/2, AnchorMap.h/2));
		entity.setScale(0.8f);

		dead = false;

		dx = 0;
		dy = 0;
		speed = 0;
		mass = 1;
		stunTime = 0;
		score = 0;
		
		// hook variables
		hookedTo = null;
		hooked = false;

		wSpeed = 0;
		degrees = 0;
		hookLength = 0;

		clockWise = false;

		centriAcc = 0;
		// -----------------

		this.num = num;
	}

	public void update(Game game, float delta) {		
		// check if dead
		/*if(entity.getCenterPosition().x < 0 || entity.getCenterPosition().x > Game.WIDTH
				|| entity.getCenterPosition().y < 0 || entity.getCenterPosition().y > Game.HEIGHT) {
			dead = true;
		}*/
		if(dead) return;

		if(stunTime != 0) {
			stunTime -= delta;
			if(stunTime < 0) {
				stunTime = 0;
			}
		}
		else {
			// hook button
			List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
			for(TouchEvent ev : touchEvents) {
				if(ev.type == TouchEvent.TOUCH_UP) {
					hooked = !hooked;
					if(hooked) {
						hook();
					}
				}
			}
		}

		// fall
		if(!hooked) {
			dy += gravity*delta;
		}
		// spin
		else {
			if(clockWise) {
				degrees -= wSpeed;
			}
			else {
				degrees += wSpeed;
			}
			dx = hookedTo.getCenterPosition().x + Math.cos(degrees*Math.PI/180) * hookLength - entity.getCenterPosition().x;
			dy = hookedTo.getCenterPosition().y - Math.sin(degrees*Math.PI/180) * hookLength - entity.getCenterPosition().y;

			if(wSpeed >= MAXSPINSPEED) {
				wSpeed = MAXSPINSPEED;
			}
			else {
				wSpeed += centriAcc;
			}
		}

		dx = dx / (1920 / AnchorMap.w);
		dy = dy / (1080 / AnchorMap.h);
		// move
		speed = Math.hypot(dx, dy);
		entity.translate((float)dx, (float)dy);
	}

	private void hook() {
		// get closest anchor
		hookLength = 10000;
		for(Entity e : anchorList) {
			double eHypot = Math.hypot(e.getCenterPosition().x - entity.getCenterPosition().x, e.getCenterPosition().y - entity.getCenterPosition().y);
			if(eHypot < hookLength) {
				hookLength = eHypot;
				hookedTo = e;
			}
		}

		degrees = Math.atan2(hookedTo.getCenterPosition().y - entity.getCenterPosition().y, 
				-(hookedTo.getCenterPosition().x - entity.getCenterPosition().x)) * 180/Math.PI;

		// --------------
		// clockwise
		double e1x, e1y, e2x, e2y;
		double p1x, p1y, p2x, p2y, p3x, p3y;
		p1x = entity.getCenterPosition().x;
		p1y = entity.getCenterPosition().y;
		p2x = entity.getCenterPosition().x + 99999*dx;
		p2y = entity.getCenterPosition().y + 99999*dy;
		p3x = hookedTo.getCenterPosition().x;
		p3y = hookedTo.getCenterPosition().y;

		e1x = p1x-p2x;
		e1y = p1y-p2y;
		e2x = p3x-p2x;
		e2y = p3y-p2y;

		if((e1x * e2y - e1y * e2x) >= 0) {
			clockWise = false;
		}
		else {
			clockWise = true;
		}
		// ----------
		// speed reduction at weird angles
		double tmpSpeed = Math.hypot(Math.cos(degrees*Math.PI/180) * dy, Math.sin(degrees*Math.PI/180) * dx);
		wSpeed = tmpSpeed / hookLength * 180/Math.PI;

		p1x = hookedTo.getCenterPosition().x;
		p1y = hookedTo.getCenterPosition().y;

		p2x = entity.getCenterPosition().x;
		p2y = entity.getCenterPosition().y;

		p3x = entity.getCenterPosition().x + 100*dx;
		p3y = entity.getCenterPosition().y + 100*dy;

		e1x = p2x - p1x;
		e1y = p2y - p1y;

		e2x = p2x - p3x;
		e2y = p2y - p3y;

		double test = ((e1x*e2x) + (e1y*e2y)) / (Math.hypot(e1x, e1y) * Math.hypot(e2x, e2y));
		test = Math.acos(test) * 180/Math.PI;
		if(test > 90) {
			test = Math.abs(test - 180);
		}
		wSpeed = test/90 * wSpeed;
		// ----------------------------------

		centriAcc = ACC_CONST/(hookLength*hookLength);
	}

	public void render(Game game, float delta) {
		if(dead) return;

		if(hooked) {
			Graphics g = game.getGraphics();
			g.drawLine(Math.round(entity.getCenterPosition().x), Math.round(entity.getCenterPosition().y), Math.round(hookedTo.getCenterPosition().x), Math.round(hookedTo.getCenterPosition().y), Color.WHITE);
		}
		entity.render(game, delta);
		
		// debug stun time
		//g.drawString(new Double(stunTime).toString(), entity.getPosition().x, entity.getPosition().y);
	}

	@Override
	public String toString() {
		return "Player " + Integer.valueOf(num + 1);
	}

	public boolean isDead() {
		return dead;
	}

	public Entity getEntity() {
		return entity;
	}

	public double getDx() {
		return dx;
	}

	public double getDy() {
		return dy;
	}

	public double getSpeed() {
		return speed;
	}

	public double getDegSpeed(double deg) {
		return Math.hypot(dx*Math.cos(deg), dy*Math.sin(deg));
	}

	public double getMass() {
		return mass;
	}
	
	public int getScore() {
		return score;
	}
	

	public Vector2f getVelocity() {
		return new Vector2f((float)dx, (float)dy);
	}

	public void setVelocity(Vector2f v) {
		dx = v.x;
		dy = v.y;
	}

	public void setStunTime(double time) {
		stunTime = time;
	}
	
	public void setHooked(boolean h) {
		hooked = h;
	}
	
	public void die() {
		dead = true;
	}
	
	public void addScore(int score) {
		this.score += score;
	}
	
	public void setScore(int score) {
		this.score = score;
	}

	// COLLISION PHYSICS
	public void collision(Player player) {
		// ------------------------------------------------------------------------------------------------------
		// TAKEN FROM http://stackoverflow.com/questions/345838/ball-to-ball-collision-detection-and-handling user Simucal
		// ------------------------------------------------------------------------------------------------------
		Vector2f delta = new Vector2f(entity.getCenterPosition().x - player.getEntity().getCenterPosition().x, entity.getCenterPosition().y - player.getEntity().getCenterPosition().y);
		float r = entity.getRadius() + player.entity.getRadius();
		float dist2 = delta.dot(delta);

		if(dist2 > r*r) return;

		float d = delta.length();

		Vector2f mtd;
		if(d == 0.0f) {
			d = player.entity.getRadius() + entity.getRadius() - 1;
			delta = new Vector2f(player.entity.getRadius() + entity.getRadius(), 0);
		}

		mtd = new Vector2f(delta.x * (((entity.getRadius() + player.entity.getRadius()) - d)/d), delta.y * (((entity.getRadius() + player.entity.getRadius()) - d)/d));

		float im1 = (float) (1 / getMass());
		float im2 = (float) (1 / player.getMass());

		Vector2f mtdScaled1 = new Vector2f(mtd.x * (im1 / (im1 + im2)), mtd.y * (im1 / (im1 + im2)));
		Vector2f mtdScaled2 = new Vector2f(mtd.x * (im2 / (im1 + im2)), mtd.y * (im2 / (im1 + im2)));
		entity.setCenterPosition(new Vector2f(entity.getCenterPosition().x + mtdScaled1.x, entity.getCenterPosition().y + mtdScaled1.y));
		player.getEntity().setCenterPosition(new Vector2f(player.getEntity().getCenterPosition().x + mtdScaled2.x, player.getEntity().getCenterPosition().y + mtdScaled2.y));

		Vector2f v = new Vector2f((float)dx - player.getVelocity().x, (float)dy - player.getVelocity().y);

		float vn = v.dot(mtd.normalise());

		if(vn > 0.0f) return;

		// impulse
		float i = (-(1.0f + SPEED_LOST) * vn) / (im1 + im2);
		Vector2f impulse = new Vector2f(mtd.x * i, mtd.y * i);

		// momentum
		Vector2f dim1 = new Vector2f(impulse.x * im1, impulse.y * im1);
		Vector2f dim2 = new Vector2f(impulse.x * im2, impulse.y * im2);
		Vector2f newV = new Vector2f((float)dx + dim1.x, (float)dy + dim1.y);
		Vector2f otherNewV = new Vector2f(player.getVelocity().x - dim2.x, player.getVelocity().y - dim2.y);
		// ------------------------------------------------------------------------------------------------------
		// ------------------------------------------------------------------------------------------------------

		hooked = false;
		player.setHooked(false);

		double dSpeed = 0;
		double deg1 = Math.atan2(player.entity.getCenterPosition().y - entity.getCenterPosition().y, 
				-(player.entity.getCenterPosition().x - entity.getCenterPosition().x)) * 180/Math.PI;
		
		double deg2 = Math.atan2(entity.getCenterPosition().y - player.entity.getCenterPosition().y, 
				-(entity.getCenterPosition().x - player.entity.getCenterPosition().x)) * 180/Math.PI;
		if(getDegSpeed(deg1) < player.getDegSpeed(deg2)) {
			// this stunnad
			// stunduration i skillnad
			dSpeed = Math.abs(getDegSpeed(deg1) - player.getDegSpeed(deg2));
			stunTime = dSpeed*STUN_LENGTH;
		}
		else if(getDegSpeed(deg1) > player.getDegSpeed(deg2)){
			// player stunnad
			// stunduration i skillnad
			dSpeed = Math.abs(getDegSpeed(deg1) - player.getDegSpeed(deg2));
			player.setStunTime(dSpeed*STUN_LENGTH);
		}

		setVelocity(newV);
		player.setVelocity(otherNewV);
		
		Assets.hit.play(50);
		//sound.play(1, AudioSettingsState.SOUND_LEVEL*AudioSettingsState.MASTER_LEVEL);
	}
}
