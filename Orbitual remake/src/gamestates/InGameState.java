package gamestates;

import game.AnchorMap;
import game.Entity;
import game.Game;
import game.MenuButton;
import game.Player;

import java.awt.Font;
import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;
import org.newdawn.slick.util.FontUtils;

import components.*;

public class InGameState extends BasicGameState {

	public static final int ID = 1;
	private AnchorMap map;
	private ArrayList<Player> players;
	private int numLocalPlayers;

	public static boolean finished = true;

	private TrueTypeFont ttf;
	private TrueTypeFont scoreFont;

	private ArrayList<Player> playersAlive;
	
	private static double countDown;
	private static boolean onCountDown;

	@Override
	public void init(GameContainer gc, StateBasedGame sb) throws SlickException {
		if(!finished) {
			reInit(gc, sb);
			return;
		}

		playersAlive = new ArrayList<Player>();
		map = new AnchorMap();
		players = new ArrayList<Player>();

		numLocalPlayers = 2;
		if(numLocalPlayers > map.getNumPlayers()) numLocalPlayers = map.getNumPlayers();

		Player.anchorList = map.getEntities();
		// players
		for(int i = 0; i < numLocalPlayers; i++) {
			Player p = new Player(i, map);
			p.KEYBIND = ControlsSettingsState.KEYBINDS[i];
			players.add(p);
			playersAlive.add(players.get(i));
		}

		// font for winner
		Font f = new Font("Comic Sans", Font.ITALIC, 50);
		ttf = new TrueTypeFont(f, true);
		
		scoreFont = new TrueTypeFont(new Font("Arial", Font.BOLD, 18), true);

		finished = false;

		DisplayModeState.OLD_WIDTH = Game.WIDTH;
		DisplayModeState.OLD_HEIGHT = Game.HEIGHT;
		
		countDown = 3 * 1000;
		onCountDown = true;
	}

	/**
	 * Runs if the game is initiated when not finished.
	 * Happens if you change resolution.
	 * 
	 * @param gc
	 * @param sb
	 * @throws SlickException
	 */
	private void reInit(GameContainer gc, StateBasedGame sb) throws SlickException {
		for(Player player : players) {
			Entity e = player.getEntity();
			Vector2f v = new Vector2f(e.getPosition().x/DisplayModeState.OLD_WIDTH * Game.WIDTH, e.getPosition().y/DisplayModeState.OLD_HEIGHT * Game.HEIGHT);
			e.setPosition(v);
			e.setScale(Player.stdScale*Game.WIDTH);
		}

		ArrayList<Entity> anchors = map.getEntities();
		for(Entity e : anchors) {
			Vector2f v = new Vector2f(e.getPosition().x/DisplayModeState.OLD_WIDTH * Game.WIDTH, e.getPosition().y/DisplayModeState.OLD_HEIGHT * Game.HEIGHT);
			e.setPosition(v);
		}

		for(int i = 0; i < numLocalPlayers; i++) {
			players.get(i).KEYBIND = ControlsSettingsState.KEYBINDS[i];
		}
	}
	
	private void newRound() throws SlickException {
		ArrayList<Integer> tmpAL = new ArrayList<Integer>();
		
		for(Player player : players) {
			tmpAL.add(player.getScore());
		}
		
		// players
		playersAlive.clear();
		players.clear();
		for(int i = 0; i < numLocalPlayers; i++) {
			Player p = new Player(i, map);
			p.KEYBIND = ControlsSettingsState.KEYBINDS[i];
			p.setScore(tmpAL.get(i));
			players.add(p);
			playersAlive.add(players.get(i));
		}
	
		startCountDown();
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sb, Graphics g)
			throws SlickException {
		map.render(gc, sb, g);

		if(players.isEmpty()) return;
		for(Player player : players) {
			player.render(gc, sb, g);
		}
		
		for(int i = 0; i < numLocalPlayers; i++) {
			FontUtils.drawCenter(scoreFont, "Player " + (i+1) + ": " + players.get(i).getScore(), 200 + i * ((Game.WIDTH - 200) / (Game.MAX_PLAYERS-1)), 40, 100, Player.PLAYER_COLORS[i]);
		}
		
		if(onCountDown) {
			FontUtils.drawCenter(ttf, new Integer((((int)countDown/1000) + 1) == 4 ? 3 : (((int)countDown/1000) + 1)).toString(), Game.centerWidth, Game.centerHeight - 100, 20);
		}
		
		if(finished) {
			g.setColor(new Color(0, 0, 0, 125));
			g.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);
			g.setColor(Color.white);
			if(playersAlive.size() < 1) {
				FontUtils.drawCenter(ttf, "It's a Draw!", Game.centerWidth - 200, Game.centerHeight - 25, 400);
			}
			else {
				FontUtils.drawCenter(ttf, playersAlive.get(0).toString() + " Wins!", Game.centerWidth - 200, Game.centerHeight - 25, 400, Player.PLAYER_COLORS[players.indexOf(playersAlive.get(0))]);
			}
		}
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sb, int delta) throws SlickException {
		if(finished) return;
		Input input = gc.getInput();
		if (input.isKeyPressed(Input.KEY_ESCAPE)) {
			Game.LASTID = getID();
			finished = false;
			sb.enterState(PauseMenuState.ID);
		}
		
		// 3 sec countdown stop update
		if(onCountDown) {
			countDown -= delta;
			if(countDown <= 0) {
				onCountDown = false;
			}
			input.clearKeyPressedRecord();
			return;
		}

		if(players.isEmpty()) return;
		if(!playersAlive.isEmpty()) {
			ArrayList<Player> tmpPlayers = new ArrayList<Player>();
			for(Player player : playersAlive) {
				tmpPlayers.add(player);
			}
			for(Player player : tmpPlayers) {
				if(player.isDead()) {
					playersAlive.remove(player);
				}
				else {
					player.update(gc, sb, delta);
				}
			}
		}
		
		deathCheck();
		
		ArrayList<Player> winners = new ArrayList<Player>();
		
		if((playersAlive.size() == 1 && numLocalPlayers > 1) || (playersAlive.size() < 1)) {
			for(Player player : players) {
				if(player.getScore() >= Game.SCORE_LIMIT) {
					winners.add(player);
				}
			}
			
			if(!winners.isEmpty()) {
				Player bestScore = winners.get(0);
				for(Player p : winners) {
					if(p.getScore() > bestScore.getScore()) {
						bestScore = p;
					}
				}
				// player wins
				// playersAlive.get(0)
				Game.LASTID = getID();
				finished = true;
				sb.enterState(MenuState.ID, new FadeOutTransition(Color.black, 2000), new FadeInTransition(Color.black,
						2000));
			}
			else {
				newRound();
			}
		}

		// check for collision
		if(!playersAlive.isEmpty() && playersAlive.size() > 1) {
			for(int i = 0; i < playersAlive.size() - 1; i++) {
				for(int j = i+1; j < playersAlive.size(); j++) {
					if(collisionCircle(playersAlive.get(i).getEntity(), playersAlive.get(j).getEntity())) {
						playersAlive.get(i).collision(playersAlive.get(j));
					}
				}
			}
		}
	}

	private boolean collisionCircle(Entity e1, Entity e2) {
		float radii = e1.getRadius() + e2.getRadius();
		float dx = e2.getPosition().x + e2.getRadius() - e1.getPosition().x - e1.getRadius();
		float dy = e2.getPosition().y + e2.getRadius() - e1.getPosition().y - e1.getRadius();
		if( dx * dx + dy * dy < radii * radii){
			return true;
		}
		return false;
	}

	private void deathCheck() {
		// check if dead
		for(Player player : playersAlive) {
			if(player.getEntity().getCenterPosition().x < 0 || player.getEntity().getCenterPosition().x > Game.WIDTH
					|| player.getEntity().getCenterPosition().y < 0 || player.getEntity().getCenterPosition().y > Game.HEIGHT) {
				player.die();
				for(Player otherPlayer : playersAlive) {
					if(otherPlayer.equals(player)) continue;
					otherPlayer.addScore(1);
				}
				
				// SCREEN FLASH HERE
			}
		}
	}
	
	public static void startCountDown() {
		countDown = 3000;
		onCountDown = true;
	}
	
	@Override
	public int getID() {
		return ID;
	}
}
