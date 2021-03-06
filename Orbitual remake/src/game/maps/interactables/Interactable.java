package game.maps.interactables;

import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.StateBasedGame;

import game.Entity;
import game.Player;

public abstract class Interactable extends Entity {
	protected final float scale = 0.2f/1920;
	
	public Interactable(String id) {
		super(id);
	}
	
	public abstract boolean collisionCheck(Interactable inter);
	public abstract void collision(Interactable inter);
	public abstract void reset();
}
/*public abstract class Interactable extends Entity {
	protected Vector2f vector;

	protected Image img;

	public Interactable(String id) {
		super(id);
		
	}
	
	public void setPosition(Vector2f vector){
		this.setCenterPosition(vector);
		
	}
	
	public abstract void collisionCheck(StateBasedGame sb);
	
	public abstract void collision(Player player);
	
	public abstract void reset();

}*/
