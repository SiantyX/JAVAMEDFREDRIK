package gamestates;

import game.Entity;
import game.Game;
import game.Player;
import game.QuadTree;
import game.ViewPort;
import game.maps.GameMap;
import game.maps.interactables.Interactable;

import java.awt.Font;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;
import org.newdawn.slick.util.FontUtils;

public abstract class AbstractInGameState extends BasicGameState {
	protected final int ID;

	protected GameMap map;
	public static CopyOnWriteArrayList<Player> players;
	static int numLocalPlayers = 2;

	public static boolean finished = true;

	protected TrueTypeFont ttf;
	protected TrueTypeFont scoreFont;

	protected ArrayList<Player> playersAlive;

	protected static double countDown;
	protected static boolean onCountDown;
	
	protected int scoreLimit;

	protected Image bg;
	
	protected ViewPort vp;
	protected QuadTree qt;

	protected int[] keyBinds;

	public AbstractInGameState(int id) {
		ID = id;
	}

	@Override
	public void init(GameContainer gc, StateBasedGame sb) throws SlickException {
		if(!finished) {
			reInit(gc, sb);
			return;
		}

		bg = new Image("res/orbitalbg1.jpg");
		vp = new ViewPort(new Vector2f(Game.WIDTH, Game.HEIGHT));
		vp.setZoom(numberOfPlayers());

		map = ((BeforeGameState)sb.getState(Game.State.BEFOREGAMESTATE.ordinal())).getMap();
		map.setBoundsAndCreateMap(vp);
		
		playersAlive = new ArrayList<Player>();
		players = new CopyOnWriteArrayList<Player>();
		Player.anchorList = map.getAnchors();
		for(int i = 0; i < numberOfPlayers(); i++) {
			Player p = createPlayer(i, map);
			Vector2f startPos = map.getStartPos(p, vp);
			p.setCenterPosition(startPos);
			players.add(p);
			playersAlive.add(players.get(i));
		}
		
		specificInit(gc, sb);
		
		// font for winner
		Font f = new Font("Comic Sans", Font.ITALIC, 50);
		ttf = new TrueTypeFont(f, true);

		scoreFont = new TrueTypeFont(new Font("Arial", Font.BOLD, 18), true);

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
			Vector2f v = new Vector2f(player.getCenterPosition().x/DisplayModeState.OLD_WIDTH * Game.WIDTH, player.getCenterPosition().y/DisplayModeState.OLD_HEIGHT * Game.HEIGHT);
			player.setCenterPosition(v);
			player.setScale(Player.stdScale*Game.WIDTH);
		}

		map = ((BeforeGameState)sb.getState(Game.State.BEFOREGAMESTATE.ordinal())).getMap();
		ArrayList<Entity> anchors = map.getAnchors();
		for(Entity e : anchors) {
			Vector2f v = new Vector2f(e.getCenterPosition().x/DisplayModeState.OLD_WIDTH * Game.WIDTH, e.getCenterPosition().y/DisplayModeState.OLD_HEIGHT * Game.HEIGHT);
			e.setCenterPosition(v);
		}
	}

	protected void newRound(StateBasedGame sb) throws SlickException {
		playersAlive.clear();
		for(int i = 0; i < players.size(); i++) {
			players.get(i).reset();
			Vector2f startPos = map.getStartPos(players.get(i), vp);
			players.get(i).setCenterPosition(startPos);
			playersAlive.add(players.get(i));
		}
		
		startCountDown();
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sb, Graphics g)
			throws SlickException {
		bg.draw(0, 0, (float) Game.WIDTH/2560);

		map.render(gc, sb, g, vp);

		if(players.isEmpty()) return;
		for(Player player : players) {
			player.render(gc, sb, g, vp);
		}

		FontUtils.drawCenter(scoreFont, "Score limit: " + scoreLimit, 10, 10, 200);

		for(int i = 0; i < numberOfPlayers(); i++) {
			FontUtils.drawCenter(scoreFont, "Player " + (i+1) + ": " + players.get(i).getScore(),map.getScorePlacementX(i), map.getScorePlacementY(), 100, Player.PLAYER_COLORS[i]);
		}

		if(onCountDown) {
			FontUtils.drawCenter(ttf, new Integer((((int)countDown/1000) + 1) == 4 ? 3 : (((int)countDown/1000) + 1)).toString(), Game.centerWidth, Game.centerHeight - 100, 20);
		}

		if(finished) {
			g.setColor(new Color(0, 0, 0, 125));
			g.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);
			g.setColor(Color.white);
			if(sb.getCurrentStateID() != Game.State.AFTERGAMESTATE.ordinal()) {
				if(playersAlive.size() < 1) {
					FontUtils.drawCenter(ttf, "It's a Draw!", Game.centerWidth - 200, Game.centerHeight - 25, 400);
				}
				else {
					FontUtils.drawCenter(ttf, playersAlive.get(0).toString() + " Wins!", Game.centerWidth - 200, Game.centerHeight - 25, 400, Player.PLAYER_COLORS[players.indexOf(playersAlive.get(0))]);
				}
			}
		}
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sb, int delta) throws SlickException {
		Game.UPDATE_BACKGROUND = 0;
		
		if(finished)
			return;
		Input input = gc.getInput();
		if (input.isKeyPressed(Input.KEY_ESCAPE)) {
			Game.LASTID = getID();
			finished = false;
			Game.MENU_MUSIC.loop();
			Game.MENU_MUSIC.setVolume(AudioSettingsState.MUSIC_LEVEL*AudioSettingsState.MASTER_LEVEL);
			sb.enterState(Game.State.PAUSEMENUSTATE.ordinal());
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
					updatePlayer(gc, sb, delta, vp, player);
				}
			}
		}
		
		map.update(gc, sb, delta);
		deathCheck();

		ArrayList<Player> winners = new ArrayList<Player>();

		if((playersAlive.size() == 1 && numberOfPlayers() > 1) || (playersAlive.size() < 1)) {
			for(Player player : players) {
				if(player.getScore() >= scoreLimit) {
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
				sb.getState(Game.State.AFTERGAMESTATE.ordinal()).init(gc, sb);
				sb.enterState(Game.State.AFTERGAMESTATE.ordinal(), new FadeOutTransition(Color.black, 2000), new FadeInTransition(Color.black,
						2000));
			}
			else {
				map.reset();
				newRound(sb);
			}
		}

		collisionCheck();
	}

	protected ArrayList<Interactable> getAllInteractables() {
		ArrayList<Interactable> inter = new ArrayList<Interactable>();
		for(Player player : playersAlive) {
			inter.add(player);
		}
		for(Interactable i : map.getInteractables()) {
			inter.add(i);
		}
		
		return inter;
	}


	private void deathCheck() {
		// check if dead
		for(Player player : playersAlive) {
			if(player.deathCheck(vp)) {
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
	
	public ArrayList<Player> getPlayers(){
		return playersAlive;
	}
	
	public void setScoreLimit(int score) {
		scoreLimit = score;
	}
	
	public void setNumPlayers(int num) {
		numLocalPlayers = num;
	}
	
	public int getNumPlayers() {
		return numLocalPlayers;
	}
	
	@Override
	public int getID() {
		return ID;
	}

	public void setControls(int[] keyBinds) {
		for(int i = 0; i < players.size(); i++) {
			players.get(i).KEYBIND = keyBinds[i];
		}
	}
	
	public void setKeyBinds(int keyBinds[]) {
		this.keyBinds = keyBinds;
	}
	
	protected abstract void close();
	
	protected abstract void specificInit(GameContainer gc, StateBasedGame sb);
	
	protected abstract int numberOfPlayers();
	
	protected abstract Player createPlayer(int i, GameMap map) throws SlickException;
	
	protected abstract void collisionCheck();
	
	protected abstract void updatePlayer(GameContainer gc, StateBasedGame sb, int delta, ViewPort vp, Player player);
}
