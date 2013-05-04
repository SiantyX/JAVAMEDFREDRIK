package game;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import components.ImageRenderComponent;

public class Player {
	private boolean dead;
	private Entity entity;
	private static String[] playerImg = new String[]{"res/sprites/smiley1.png", "res/sprites/smiley2.png", "res/sprites/smiley3.png", "res/sprites/smiley4.png", "res/sprites/smiley5.png", "res/sprites/smiley6.png", "res/sprites/smiley7.png", "res/sprites/smiley8.png"};
	private static float stdScale = 0.1f;
	
	public Player() throws SlickException {
		dead = false;
		entity = new Entity("Player");
		entity.AddComponent(new ImageRenderComponent("Player", new Image(playerImg[0])));
		entity.setScale(stdScale);
	}
	
	public void render(GameContainer gc, StateBasedGame sb, Graphics g) {
		entity.render(gc, sb, g);
	}
}
