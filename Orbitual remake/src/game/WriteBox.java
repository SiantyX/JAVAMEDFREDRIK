package game;

import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.state.StateBasedGame;

import components.SXTimer;

public class WriteBox extends MenuButton implements KeyListener, MouseListener {
	private boolean focused;
	private String acceptable;
	private SXTimer timer;
	private boolean globalLeftPress;
	private boolean globalRightPress;
	private boolean doubleClick;

	public WriteBox(String id, Shape shape, Color color, String text, TrueTypeFont ttf) throws SlickException {
		super(id, shape, color, text, ttf);
		init();
	}
	
	public WriteBox(String id, Shape shape, Color color, String text, TrueTypeFont ttf, Color color2) throws SlickException {
		super(id, shape, color, text, ttf, color2);
		init();
	}
	
	private void init() {
		focused = false;
		acceptable = "";
		timer = new SXTimer(500);
		globalLeftPress = false;
		globalRightPress = false;
		doubleClick = false;
	}

	public void setAcceptable(String a) {
		acceptable = a;
	}
	
	public void removeFocus() {
		focused = false;
	}
	
	@Override
	public void update(GameContainer gc, StateBasedGame sb, int delta) {
		super.update(gc, sb, delta);
		
		int mousePosX = Mouse.getX();
		int mousePosY = Math.abs(Mouse.getY() - Game.app.getHeight());
		
		if(globalLeftPress) {
			if(mouseInThis(mousePosX, mousePosY)) {
				if(doubleClick) {
					focused = true;
					text = "";
				}
				if(!focused) {
					focused = true;
					if(text.endsWith("|")) {
						text = text.substring(0, text.length()-1);
					}
				}
			}
			else {
				focused = false;
			}
		}
		
		doubleClick = false;
		globalLeftPress = false;
		globalRightPress = false;
		
		if(focused) {
			if(timer.isTriggered() >= 0) {
				if(!removeEnd("|")) {
					text += "|";
				}
			}
		}
	}
	
	@Override
	public void render(GameContainer gc, StateBasedGame sb, Graphics g) {
		super.render(gc, sb, g);
	}

	@Override
	public void keyPressed(int key, char c) {
		if(!focused) return;
		
		if(key == Input.KEY_ESCAPE) {
			focused = false;
			text = "";
		}
		else if(key == Input.KEY_ENTER) {
			focused = false;
			removeEnd("|");
		}
		else if(key == Input.KEY_BACK) {
			removeEnd("|");
			if(text.length() == 0){
				return;
			}
			else {
				text = text.substring(0, text.length()-1);
			}
		}
		else if(acceptable.equals("")) {
			addText(c);
		}
		else {
			if(acceptable.contains(Character.toString(c))) {
				addText(c);
			}
		}
	}
	
	public boolean removeEnd(String s) {
		if(text.length()-s.length() < 0) {
			return false;
		}
		if(text.endsWith(s)) {
			text = text.substring(0, text.length()-s.length());
			return true;
		}
		return false;
	}
	
	public void addText(char c) {
		removeEnd("|");
		text += c;
	}

	@Override
	public void inputEnded() {
	}

	@Override
	public void inputStarted() {
	}

	@Override
	public boolean isAcceptingInput() {
		return true;
	}

	@Override
	public void setInput(Input input) {
		input.addKeyListener(this);
		input.addMouseListener(this);
		input.enableKeyRepeat();
	}

	@Override
	public void keyReleased(int key, char c) {
	}

	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {
		if(button == Input.MOUSE_LEFT_BUTTON) {
			globalLeftPress = true;
		}
		else if(button == Input.MOUSE_RIGHT_BUTTON) {
			globalRightPress = true;
		}
		
		if(clickCount % 2 == 0) {
			doubleClick = true;
		}
		else {
			doubleClick = false;
		}
	}

	@Override
	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseWheelMoved(int change) {
		// TODO Auto-generated method stub
		
	}
}
