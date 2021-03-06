package game;

import game.maps.GameMap;
import game.maps.interactables.Interactable;
import game.maps.interactables.powerups.PowerUp;
import gamestates.AudioSettingsState;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.StateBasedGame;

import components.ImageRenderComponent;

public class Player extends Interactable {
	private boolean resting;
	protected boolean dead;
	protected boolean wasKeyDown;
	protected static final String[] playerImg = new String[] { "res/sprites/smiley1",
		"res/sprites/smiley2", "res/sprites/smiley3",
		"res/sprites/smiley4", "res/sprites/smiley5",
		"res/sprites/smiley6", "res/sprites/smiley7.png",
	"res/sprites/smiley8" };
	public static final float stdScale = 0.0005208f;
	public static final Color[] PLAYER_COLORS = new Color[] { Color.red,
		Color.blue, new Color(25, 235, 184), new Color(84, 0, 140),
		Color.yellow, Color.orange, Color.green, Color.pink, Color.gray,
		new Color(89, 42, 4) };
	public Color myColor;
	protected int num;
	protected int score;
	public static Sound sound;
	protected ArrayList<PowerUp> activePowerUps;

	protected ImageRenderComponent defaultImage;
	protected ImageRenderComponent stunnedImage;

	private final Object dDegreesLock = new Object();
	protected float dDegrees;
	protected float oldDegrees;

	protected double speed;
	protected double mass;

	protected double stunTime;

	// hook variables
	protected boolean hooked;
	protected Entity hookedTo;

	private final Object wSpeedLock = new Object();
	private final Object degreesLock = new Object();
	private double wSpeed;
	private double degrees;
	private double hookLength;

	private boolean clockWise;

	private double centriAcc;
	private final double MAXSPINSPEED = 16;
	private final double ACC_CONST = 600;
	// -----------------------

	public static ArrayList<Entity> anchorList;

	public int KEYBIND;

	private final float gravity = 0.015f;
	private final float SPEED_LOST = 0.6f;
	private final double STUN_LENGTH = 50;

	// -------------------------
	// Online multiplayer
	private String username;
	private String ipaddr;

	// --------------------------

	public Player(int num, GameMap map) throws SlickException {
		super(playerImg[num]);

		defaultImage = new ImageRenderComponent(playerImg[num], new Image(
				playerImg[num] + ".png"));
		stunnedImage = new ImageRenderComponent(playerImg[num] + "xd",
				new Image(playerImg[num] + "xd.png"));

		defaultImage.setScale(stdScale * Game.WIDTH);
		stunnedImage.setScale(stdScale * Game.WIDTH);

		activePowerUps = new ArrayList<PowerUp>();

		addComponent(defaultImage);

		dead = false;
		resting = false;

		dDegrees = 0;
		oldDegrees = 0;
		speed = 0;
		mass = 1;
		stunTime = 0;
		score = 0;
		myColor = PLAYER_COLORS[num];

		sound = new Sound("res/audio/sound/hit.ogg");

		// hook variables
		hookedTo = null;
		hooked = false;

		wSpeed = 0;
		degrees = 0;
		hookLength = 0;

		clockWise = false;

		centriAcc = 0;

		wasKeyDown = false;
		// -----------------

		this.num = num;
	}

	public Player(int num, GameMap map, String username, String ipaddr)
			throws SlickException {
		this(num, map);
		this.username = username;
		this.ipaddr = ipaddr;
	}

	public void update(GameContainer gc, StateBasedGame sb, int delta,
			ViewPort vp) {
		// check if dead
		if (dead)
			return;

		resting = false;

		setAllRotation(dDegrees);

		if (stunTime != 0) {
			changeImageOnNotEqual(playerImg[num] + "xd", stunnedImage);

			stunTime -= delta;
			if (stunTime < 0) {
				stunTime = 0;
			}
		} else {
			changeImageOnNotEqual(playerImg[num], defaultImage);

			// hook button
			Input input = gc.getInput();
			if (input.isKeyPressed(KEYBIND) && !wasKeyDown) {
				wasKeyDown = true;
				hook();
			}
			if (!input.isKeyDown(KEYBIND)) {
				wasKeyDown = false;
			}
		}

		if (!anchorList.contains(hookedTo) || hookedTo.getWidth() == 0) {
			hooked = false;
			hookedTo = null;
		}

		// fall
		if (!hooked) {
			increaseDy(gravity * delta);
			dDegrees -= (getDegrees() - oldDegrees) / 200;
		}

		// spin
		else {
			double oldDegrees = degrees;
			if (clockWise) {
				setDegrees(getDegrees() - wSpeed * delta / TIME_CONST);
			} else {
				setDegrees(getDegrees() + wSpeed * delta / TIME_CONST);
			}

			dDegrees -= getDegrees() - oldDegrees;
			setDx((float) (hookedTo.getCenterPosition().x
					+ Math.cos(getDegrees() * Math.PI / 180) * hookLength
					- getCenterPosition().x));
			setDy((float) (hookedTo.getCenterPosition().y
					- Math.sin(getDegrees() * Math.PI / 180) * hookLength
					- getCenterPosition().y));

			if (getWSpeed() >= MAXSPINSPEED) {
				setWSpeed( MAXSPINSPEED);
			} else {
				setWSpeed(getWSpeed() + centriAcc * delta / TIME_CONST);
			}
		}

		updateEntity(gc, sb, delta, vp);
	}
	
	protected void updateEntity(GameContainer gc, StateBasedGame sb, int delta, ViewPort vp) {
		super.update(gc, sb, delta, vp);
	}

	public boolean acceptHook() {
		if (stunTime > 0 || isDead()) {
			return false;
		}

		return true;
	}

	public int hook() {
		if (!acceptHook() || hooked) {
			hooked = false;
			hookedTo = null;
			return -1;
		}

		hooked = true;

		// get closest anchor
		hookLength = 10000;
		for (Entity e : anchorList) {
			float eHypot = (float) Math.hypot(
					e.getCenterPosition().x - getCenterPosition().x,
					e.getCenterPosition().y - getCenterPosition().y);
			if (eHypot < hookLength) {
				hookLength = eHypot;
				hookedTo = e;
			}
		}

		degrees = (float) (Math
				.atan2(hookedTo.getCenterPosition().y
						- getCenterPosition().y, -(hookedTo
								.getCenterPosition().x - getCenterPosition().x))
								* 180 / Math.PI);

		float dx = getDx();
		float dy = getDy();

		// --------------
		// clockwise
		double e1x, e1y, e2x, e2y;
		double p1x, p1y, p2x, p2y, p3x, p3y;
		p1x = getCenterPosition().x;
		p1y = getCenterPosition().y;
		p2x = getCenterPosition().x + 99999 * dx;
		p2y = getCenterPosition().y + 99999 * dy;
		p3x = hookedTo.getCenterPosition().x;
		p3y = hookedTo.getCenterPosition().y;

		e1x = p1x - p2x;
		e1y = p1y - p2y;
		e2x = p3x - p2x;
		e2y = p3y - p2y;

		if ((e1x * e2y - e1y * e2x) >= 0) {
			clockWise = false;
		} else {
			clockWise = true;
		}
		// ----------
		// speed reduction at weird angles
		float tmpSpeed = (float) Math.hypot(Math.cos(degrees * Math.PI / 180) * dy,
				Math.sin(degrees * Math.PI / 180) * getDx());
		wSpeed = (float) (tmpSpeed / hookLength * 180 / Math.PI);

		p1x = hookedTo.getCenterPosition().x;
		p1y = hookedTo.getCenterPosition().y;

		p2x = getCenterPosition().x;
		p2y = getCenterPosition().y;

		p3x = getCenterPosition().x + 100 * dx;
		p3y = getCenterPosition().y + 100 * dy;

		e1x = p2x - p1x;
		e1y = p2y - p1y;

		e2x = p2x - p3x;
		e2y = p2y - p3y;

		double test = (((e1x * e2x) + (e1y * e2y))
				/ (Math.hypot(e1x, e1y) * Math.hypot(e2x, e2y)));
		test = (Math.acos(test) * 180 / Math.PI);
		if (test > 90) {
			test = Math.abs(test - 180);
		}
		wSpeed = test / 90 * wSpeed;
		// ----------------------------------

		centriAcc = ACC_CONST / (hookLength * hookLength);

		return anchorList.indexOf(hookedTo);
	}

	public void render(GameContainer gc, StateBasedGame sb, Graphics g,
			ViewPort vp) {
		if (dead)
			return;

		if (hooked) {
			g.setColor(Color.white);
			vp.drawLine(g, getCenterPosition(),
					hookedTo.getCenterPosition());
		}
		super.render(gc, sb, g, vp);
	}

	@Override
	public String toString() {
		return "Player " + new Integer(num + 1).toString();
	}

	public boolean isDead() {
		return dead;
	}

	public void setdDegrees(float dDegrees) {
		synchronized (dDegreesLock) {
			this.dDegrees = dDegrees;
		}
	}

	public float getdDegrees() {
		synchronized (dDegreesLock) {
			return dDegrees;
		}
	}

	public void setDegrees(double d) {
		synchronized (degreesLock) {
			degrees = d;
		}
	}

	public double getDegrees() {
		synchronized (degreesLock) {
			return degrees;
		}
	}

	public void setWSpeed(double w) {
		synchronized (wSpeedLock) {
			wSpeed = w;
		}
	}

	public double getWSpeed() {
		synchronized (wSpeedLock) {
			return wSpeed;
		}
	}

	public double getSpeed() {
		return speed;
	}

	public double getDegSpeed(float deg) {
		return Math.hypot(getDx() * Math.cos(deg), getDy() * Math.sin(deg));
	}

	public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public int getScore() {
		return score;
	}

	public Vector2f getVelocity() {
		return new Vector2f(getDx(), getDy());
	}

	public void setVelocity(Vector2f v) {
		setDx(v.x);
		setDy(v.y);
	}

	public double setStunTime(double time) {
		double tmp = time;
		stunTime = time;
		return tmp;
	}

	public double getStunTime() {
		return stunTime;
	}

	public void setHooked(boolean h) {
		hooked = h;
		if(!hooked) hookedTo = null;
	}

	public boolean isHooked() {
		return hooked;
	}

	public boolean deathCheck(ViewPort vp) {
		Vector2f tmp = vp.toRelative(getCenterPosition());
		if (tmp.x < 0 || tmp.x > Game.WIDTH || tmp.y < 0 || tmp.y > Game.HEIGHT) {
			die();
			return true;
		}

		return false;
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

	public String getUsername() {
		return username;
	}

	public String getIpaddr() {
		return ipaddr;
	}

	// powerups
	public void powerUpEpicMass() {
		this.mass *= 5;
	}

	public void AddPowerUp(PowerUp pow) {
		activePowerUps.add(pow);
	}

	// COLLISION PHYSICS
	@Override
	public void collision(Interactable inter) {
		if(inter instanceof Player) {
			Player player = (Player) inter;

			// ------------------------------------------------------------------------------------------------------
			// TAKEN FROM
			// http://stackoverflow.com/questions/345838/ball-to-ball-collision-detection-and-handling
			// user Simucal
			// ------------------------------------------------------------------------------------------------------
			Vector2f delta = new Vector2f(getCenterPosition().x
					- player.getCenterPosition().x,
					getCenterPosition().y
					- player.getCenterPosition().y);
			float r = getRadius() + player.getRadius();
			float dist2 = delta.dot(delta);

			if (dist2 > r * r)
				return;

			float d = delta.length();

			Vector2f mtd;
			if (d == 0.0f) {
				d = player.getRadius() + getRadius() - 1;
				delta = new Vector2f(
						player.getRadius() + getRadius(), 0);
			}

			mtd = new Vector2f(
					delta.x
					* (((getRadius() + player.getRadius()) - d) / d),
					delta.y
					* (((getRadius() + player.getRadius()) - d) / d));

			float im1 = (float) (1 / getMass());
			float im2 = (float) (1 / player.getMass());

			Vector2f mtdScaled1 = new Vector2f(mtd.x * (im1 / (im1 + im2)), mtd.y
					* (im1 / (im1 + im2)));
			Vector2f mtdScaled2 = new Vector2f(mtd.x * (im2 / (im1 + im2)), mtd.y
					* (im2 / (im1 + im2)));
			setCenterPosition(new Vector2f(getCenterPosition().x
					+ mtdScaled1.x, getCenterPosition().y + mtdScaled1.y));
			player.setCenterPosition(
					new Vector2f(player.getCenterPosition().x
							+ mtdScaled2.x, player.getCenterPosition().y + mtdScaled2.y));

			float dx = getDx();
			float dy = getDy();

			Vector2f v = new Vector2f((float) dx - player.getVelocity().x,
					dy - player.getVelocity().y);

			float vn = v.dot(mtd.normalise());

			if (vn > 0.0f)
				return;

			// impulse
			float i = (-(1.0f + SPEED_LOST) * vn) / (im1 + im2);
			Vector2f impulse = new Vector2f(mtd.x * i, mtd.y * i);

			// momentum
			Vector2f dim1 = new Vector2f(impulse.x * im1, impulse.y * im1);
			Vector2f dim2 = new Vector2f(impulse.x * im2, impulse.y * im2);
			Vector2f newV = new Vector2f(dx + dim1.x, dy + dim1.y);
			Vector2f otherNewV = new Vector2f(player.getVelocity().x - dim2.x,
					player.getVelocity().y - dim2.y);
			// ------------------------------------------------------------------------------------------------------
			// ------------------------------------------------------------------------------------------------------

			setHooked(false);
			player.setHooked(false);

			double dSpeed = 0;
			float deg1 = (float) (Math.atan2(
					player.getCenterPosition().y
					- getCenterPosition().y, -(player.getCenterPosition().x - getCenterPosition().x))
					* 180 / Math.PI);

			float deg2 = (float) (Math.atan2(
					getCenterPosition().y
					- player.getCenterPosition().y, -(
							getCenterPosition().x - player.getCenterPosition().x))
							* 180 / Math.PI);
			if (getDegSpeed(deg1) < player.getDegSpeed(deg2)) {
				// this stunnad
				// stunduration i skillnad
				dSpeed = Math.abs(getDegSpeed(deg1) - player.getDegSpeed(deg2));
				stunTime = dSpeed * STUN_LENGTH;
			} else if (getDegSpeed(deg1) > player.getDegSpeed(deg2)) {
				// player stunnad
				// stunduration i skillnad
				dSpeed = Math.abs(getDegSpeed(deg1) - player.getDegSpeed(deg2));
				player.setStunTime(dSpeed * STUN_LENGTH);
			}

			if (this.resting) {
				otherNewV = new Vector2f(otherNewV.x, -Math.abs(newV.getY())
						- Math.abs(otherNewV.getY()));
				newV = new Vector2f(newV.x, 0);
			} else if (player.resting) {
				newV = new Vector2f(this.getVelocity().x, -Math.abs(newV.getY())
						- Math.abs(otherNewV.getY()));
				otherNewV = new Vector2f(otherNewV.x, 0);

			}

			setVelocity(newV);
			player.setVelocity(otherNewV);

			playSound();
		}
	}
	
	public static void playSound() {
		sound.play(1, AudioSettingsState.SOUND_LEVEL
				* AudioSettingsState.MASTER_LEVEL);
	}

	public void reset() {
		for (PowerUp p : activePowerUps) {
			p.powerDown(this);
		}
		activePowerUps.clear();

		setDx(0);
		setDy(0);
		hooked = false;
		dead = false;
		hookedTo = null;
		resting = false;

		stunTime = 0;

		speed = 0;

		wSpeed = 0;
		dDegrees = 0;
		oldDegrees = 0;
		degrees = 0;
		hookLength = 0;

		clockWise = false;

		centriAcc = 0;

		wasKeyDown = false;

		setAllRotation(0);
		changeImage(defaultImage);
	}

	public void rest() {
		resting = true;

	}

	@Override
	public boolean collisionCheck(Interactable inter) {
		return collisionCircle(inter);
	}

	public int getNum() {
		return num;
	}

	public int getHookedTo() {
		return anchorList.indexOf(hookedTo);
	}

	public void setHookedTo(int i) {
		if(i < 0) {
			hooked = false;
			hookedTo = null;
		}
		else {
			try {
				hookedTo = anchorList.get(i);
				hooked = true;
			}
			catch(IndexOutOfBoundsException e) {
				System.out.println("Out of sync. Anchor lost.");
			}
		}
	}
}
