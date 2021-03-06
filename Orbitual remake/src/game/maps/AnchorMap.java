package game.maps;

import game.Entity;
import game.Game;
import game.Player;
import game.ViewPort;

import java.util.ArrayList;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;


public class AnchorMap extends GameMap {
	
	private int numAncPerRow;
	private int numAncPerColumn;
	
	// default map
	public AnchorMap() throws SlickException {
		
		super();
		
		numAnc = 12;
			
		numAncPerRow = 4;
		
		numAncPerColumn = numAnc/numAncPerRow;
		
		
		
	}
	
	// debug map
	public AnchorMap(int numAnc, int startPercentX, int numAncPerRow, int startPercentY, int numPlayers) throws SlickException {
		this.numAnc = numAnc;
		
		this.startPercentX = startPercentX;
		
		this.numAncPerRow = numAncPerRow;
		
		this.startPercentY = startPercentY;
		
		this.numAncPerColumn = numAnc/numAncPerRow;
	}
	
	public void createMap(ViewPort vp) throws SlickException {
		anchors.clear();
		
		for(int i = 0; i < numAnc; i++) {
			Vector2f pos = new Vector2f(startPosX + (i%numAncPerRow) * (((Game.WIDTH-(2*startPosX))/(numAncPerRow-1))), startPosY + (i%numAncPerColumn) * (((Game.HEIGHT-(2*startPosY))/(numAncPerColumn-1))));
			addAnchor(i, pos, vp);
		}
		
		Vector2f tmp = new Vector2f(startPosX, startPosY);
		tmp = vp.toAbsolute(tmp);
		this.startPosY = Math.round(tmp.x);
		this.startPosX = Math.round(tmp.y);
	}
	
	
	public ArrayList<Entity> getAnchors() {
		return anchors;
	}
	

	@Override
	public Vector2f getStartPos(Player p, ViewPort vp) {
		return standardStartPosition(p.getNum(), vp);
	}

	@Override
	public String toString() {
		return "Anchor Map";
	}

	@Override
	public void mapSpecificChange() {
		// TODO Auto-generated method stub
		
	}

}
