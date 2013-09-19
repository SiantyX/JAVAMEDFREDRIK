package gamestates;

import game.Game;
import game.MenuButton;
import game.Player;

import java.awt.Font;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import networking.Hosting;
import networking.LobbyHosting;
import networking.NetHandler;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;
import org.newdawn.slick.util.FontUtils;

public abstract class LobbyState extends BasicGameState implements KeyListener {
	public static final int ID = 99;
	protected String hostname;
	protected ArrayList<MenuButton> buttons;
	protected MenuButton startButton, cancelButton, textButton;
	protected boolean justChanged;
	protected boolean textChange;
	protected TrueTypeFont ttf, bigText;
	//private ArrayList<Player> players;
	protected ArrayList<MenuButton> users;
	protected NetHandler hndlr;
	protected int alpha;
	protected LobbyHosting hosted;
	protected CopyOnWriteArrayList<String> players;

	public void init(GameContainer gc, StateBasedGame sb) throws SlickException {
		hostname = NetHandler.getHostName();

		buttons = new ArrayList<MenuButton>();
		//players = new ArrayList<Player>();
		users = new ArrayList<MenuButton>();
		players = new CopyOnWriteArrayList<String>();

		textChange = false;
		justChanged = false;

		Font f = new Font("Arial", Font.PLAIN, 18);
		ttf = new TrueTypeFont(f, true);

		f = new Font("Comic Sans", Font.ITALIC, 50);
		bigText = new TrueTypeFont(f, true);

		cancelButton = new MenuButton("cancel", new Rectangle(Game.centerWidth - 300, Game.centerHeight + 400, 200, 50), Color.white, "Cancel", ttf);
		textButton = new MenuButton("text", new Rectangle(Game.centerWidth - 100, Game.centerHeight + 300, 200, 50), Color.black, "|...", ttf);


		buttons.add(cancelButton);
		buttons.add(textButton);
	}

	public void render(GameContainer gc, StateBasedGame sb, Graphics g) throws SlickException {
		FontUtils.drawCenter(bigText, hostname, Game.centerWidth - 300, Game.centerHeight/6, 600);

		for (MenuButton button : buttons) {
			button.render(gc, sb, g);
		}
		for (MenuButton button : users) {
			button.render(gc, sb, g);
		}
	}

	public void update(GameContainer gc, StateBasedGame sb, int delta) throws SlickException {
		for (MenuButton button : buttons) {
			button.update(gc, sb, delta);
		}
		for (MenuButton button : users) {
			button.update(gc, sb, delta);
		}

		Input input = gc.getInput();
		
		if(textButton.isMousePressed()) {
			textChange = true;
			textButton.setText("");
		}
	}

	public void keyPressed(int key, char c) {
		if(textChange) {
			if(key == Input.KEY_ENTER) {
				textChange = false;
				sendText(textButton.getText());
				textButton.setText("|...");
			}
			else if(key == Input.KEY_ESCAPE) {
				textChange = false;
				justChanged = true;
				textButton.setText("|...");
			}
			else if(((int) c >= 32 && (int) c <= 126) || (int) c == 229 || (int) c == 228 || (int) c == 246) {
				textButton.setText(textButton.getText() + c);
			}
		}
	}

	public void sendText(String str) {

	}

	public abstract int getID();
}


/*package gamestates;

import game.Game;
import game.MenuButton;
import game.Player;

import java.awt.Font;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;
import org.newdawn.slick.util.FontUtils;

public class LobbyState extends BasicGameState implements KeyListener {
	public static final int ID = 11;
	private String hostname;
	private ArrayList<MenuButton> buttons;
	private MenuButton startButton, cancelButton, textButton;
	private boolean justChanged;
	private boolean textChange;
	private TrueTypeFont ttf, bigText;
	//private ArrayList<Player> players;
	private ArrayList<MenuButton> users;

	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		hostname = "Unknown";

		try
		{
		    InetAddress addr;
		    addr = InetAddress.getLocalHost();
		    hostname = addr.getHostName();
		}
		catch (UnknownHostException ex)
		{
		    System.out.println("Hostname can not be resolved");
		}
		
		buttons = new ArrayList<MenuButton>();
		//players = new ArrayList<Player>();
		users = new ArrayList<MenuButton>();
		
		textChange = false;
		justChanged = false;
		
		Font f = new Font("Arial", Font.PLAIN, 18);
		ttf = new TrueTypeFont(f, true);
		
		f = new Font("Comic Sans", Font.ITALIC, 50);
		bigText = new TrueTypeFont(f, true);
		
		startButton = new MenuButton("start", new Rectangle(Game.centerWidth + 100, Game.centerHeight + 400, 200, 50), Color.white, "Start", ttf);
		cancelButton = new MenuButton("cancel", new Rectangle(Game.centerWidth - 300, Game.centerHeight + 400, 200, 50), Color.white, "Cancel", ttf);
		textButton = new MenuButton("text", new Rectangle(Game.centerWidth - 100, Game.centerHeight + 300, 200, 50), Color.black, "|...", ttf);
		
		users.add(new MenuButton("self", new Rectangle(Game.centerWidth - 100, Game.centerHeight + 300, 200, 50), Color.black, Game.username, ttf, Color.yellow));
		
		buttons.add(startButton);
		buttons.add(cancelButton);
		buttons.add(textButton);
	}

	public void render(GameContainer gc, StateBasedGame sb, Graphics g) throws SlickException {
		FontUtils.drawCenter(bigText, hostname, Game.centerWidth - 300, Game.centerHeight/6, 600);
		
		for (MenuButton button : buttons) {
			button.render(gc, sb, g);
		}
		for (MenuButton button : users) {
			button.render(gc, sb, g);
		}
	}

	public void update(GameContainer gc, StateBasedGame sb, int delta) throws SlickException {
		for (MenuButton button : buttons) {
			button.update(gc, sb, delta);
		}
		for (MenuButton button : users) {
			button.update(gc, sb, delta);
		}
		
		Input input = gc.getInput();
		
		if(cancelButton.isMousePressed()) {
			sb.enterState(BrowserState.ID, new FadeOutTransition(Color.black, 100), new FadeInTransition(Color.black,
					100));
		}
		
		if(startButton.isMousePressed()) {
			sb.enterState(MultiplayerState.ID, new FadeOutTransition(Color.black, 100), new FadeInTransition(Color.black,
					100));
		}
		
		if(textButton.isMousePressed()) {
			textChange = true;
			textButton.setText("");
		}
	}
	
	public void keyPressed(int key, char c) {
		if(textChange) {
			if(key == Input.KEY_ENTER) {
				textChange = false;
				sendText(textButton.getText());
				textButton.setText("|...");
			}
			else if(key == Input.KEY_ESCAPE) {
				textChange = false;
				justChanged = true;
				textButton.setText("|...");
			}
			else if(((int) c >= 32 && (int) c <= 126) || (int) c == 229 || (int) c == 228 || (int) c == 246) {
				textButton.setText(textButton.getText() + c);
			}
		}
	}
	
	public void sendText(String str) {
		
	}

	public int getID() {
		return ID;
	}
}*/
