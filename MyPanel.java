import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class MyPanel extends JPanel implements MouseListener, MouseMotionListener, ActionListener {
	/* Stuff */
	Graphics2D g2;
	Minesweeper game;
	long startTime = 0;
	long elapsedTime = 0;
	private int gameWidth = 16;
	private int gameHeight = 16;
	private int gameBomb = 40;

	/* Mouse Stuff */
	boolean mouseLeftClick = false; // if false, mouse right clicked
	boolean mouseDown = false;
	int mouseX = 0;
	int mouseY = 0;
	
	/* Constants */
	final JFrame frame;
	final int TILE_SIZE = 16;
	final int FACE_SIZE = 24;
	final int NUMBER_WIDTH = 13;
	final int NUMBER_HEIGHT = 23;
	final int LEFT_PAD = 12;
	final int RIGHT_PAD = 8;
	final int TOP_PAD = 55;
	final int BOT_PAD = 8;
	final int CHROME_PAD = 22;

	/* Textures */
	BufferedImage[] numDisp = new BufferedImage[10];
	BufferedImage blankDisp, negativeDisp;
	BufferedImage[] tiles = new BufferedImage[9];
	BufferedImage unrevealedTile, flaggedTile, wrongFlagTile;
	BufferedImage revealedBomb, bomb;
	BufferedImage borderLeft, borderRight, borderBotLeft, borderBotRight, borderBot;
	BufferedImage borderStatLeft, borderStatRight, borderStatMid, borderStatFill;
	BufferedImage faceDead, faceShocked, faceSmile, faceSmilePressed, faceSunglasses;

	public MyPanel(JFrame frame_) {
		frame = frame_;
		setImages();

		setDifficulty(16, 16, 40);
		resetGame();
	}

	protected void paintComponent(Graphics g) {
		g2 = (Graphics2D)g;
		drawBorder(game.SIZE_X, game.SIZE_Y);
		drawBorderStat(game.SIZE_X);
		drawStateFace(game.SIZE_X, game.getState());
		drawTime(elapsedTime);
		drawFlagsLeft(game.NUM_BOMBS, game.flaggedTiles.size());

		drawBlank(game.SIZE_X, game.SIZE_Y);
		drawPressedTile(mouseX, mouseY);
		drawBoard(game.revealedTiles, game.flaggedTiles, game.tiles);
	}

	private void drawBoard(Set<Coord> revealed, Set<Coord> flagged, Map<Coord, Integer> values) {
		for(Coord c : flagged) {
			int x = c.getX();
			int y = c.getY();
			if(game.getState() == Minesweeper.LOST && !values.containsKey(c)) {
				drawTile(x, y, wrongFlagTile);
				continue;
			}
			drawTile(x, y, flaggedTile);
		}

		for(Coord c : revealed) {
			int v = values.get(c);
			int x = c.getX();
			int y = c.getY();
			if(v >= 0) drawTile(x, y, tiles[v]);
			if(v == -1) {
				if(game.getState() == Minesweeper.LOST) {
					if(flagged.contains(c)) {
						drawTile(x, y, flaggedTile);
					} else {
						drawTile(x, y, bomb);
					}

				} else if(game.getState() == Minesweeper.WON) {
					drawTile(x, y, flaggedTile);
				}
			}
		}

		if(game.getState() == Minesweeper.LOST) {
			Coord wB = game.getWrongBomb();
			drawTile(wB.getX(), wB.getY(), revealedBomb);
		}
	}

	private void drawBlank(int sX, int sY) {
		for(int y = 0; y < sY; y++) {
			for(int x = 0; x < sX; x++) {
				drawTile(x, y, unrevealedTile);
			}
		}
	}

	private void drawPressedTile(int mX, int mY) {
		if(!mouseDown || !mouseLeftClick || game.getState() != Minesweeper.ONGOING)
			return;

		int RIGHT_BOUND = getWidth()-RIGHT_PAD-1;
		int BOT_BOUND = getHeight()-BOT_PAD-1;
		if(!withinBounds(mX, mY, LEFT_PAD, RIGHT_BOUND, TOP_PAD, BOT_BOUND))
			return;

		Coord c = mouseToCoord(mouseX, mouseY);
		drawTile(c.getX(), c.getY(), tiles[0]);
	}

	private void drawTile(int tX, int tY, BufferedImage tile) {
		int x = LEFT_PAD + tX * TILE_SIZE;
		int y = TOP_PAD + tY * TILE_SIZE;
		g2.drawImage(tile, x, y, null);
	}

	private void drawBorder(int sX, int sY) {
		/* Draw Bottom Corners */
		g2.drawImage(borderBotLeft , 0, getHeight()-BOT_PAD, this);
		g2.drawImage(borderBotRight, getWidth()-RIGHT_PAD, getHeight()-BOT_PAD, this);
		
		/* Draw Bottom */
		for(int tX = 0; tX < sX; tX++) {
			int x = tX*TILE_SIZE+LEFT_PAD;
			g2.drawImage(borderBot, x, getHeight()-BOT_PAD, this);
		}

		/* Draw Sides */
		for(int tY = 0; tY < sY; tY++) {
			int y = tY*TILE_SIZE+TOP_PAD;
			g2.drawImage(borderLeft, 0, y, this);
			g2.drawImage(borderRight, getWidth()-RIGHT_PAD, y, this);
		}

	}

	private void drawBorderStat(int sX) {
		final int BORDER_LEFT_PAD  = borderStatLeft.getWidth();
		final int BORDER_RIGHT_PAD = borderStatRight.getWidth();

		/* Top Corners */
		g2.drawImage(borderStatLeft, 0, 0, this);
		g2.drawImage(borderStatRight, getWidth()-BORDER_RIGHT_PAD, 0, this);

		int mid = (int)(BORDER_LEFT_PAD+(sX-8)/2.0*TILE_SIZE);
		g2.drawImage(borderStatMid, mid, 0, this);

		/* Fill */
		for(float tX = 0; tX < (sX-8)/2.0; tX+=0.5) {
			int leftX  = BORDER_LEFT_PAD +(int)(tX*TILE_SIZE);
			int rightX = getWidth()-(BORDER_RIGHT_PAD+(int)((tX+0.5)*TILE_SIZE));
			g2.drawImage(borderStatFill, leftX,  0, this);
			g2.drawImage(borderStatFill, rightX, 0, this);
		}
	}

	private void drawStateFace(int sX, int state){
		final int BORDER_LEFT_PAD = borderStatLeft.getWidth();
		final int OFFSET = 4;
		int x = (int)(BORDER_LEFT_PAD+OFFSET+(sX-8)/2.0*TILE_SIZE);
		int y = 16;
		if(mouseDown && mouseLeftClick) {
			if(withinBounds(mouseX, mouseY, x, x+FACE_SIZE, y, y+FACE_SIZE)) {
				g2.drawImage(faceSmilePressed, x, y, this);
				return;
			}

			if(game.getState() == Minesweeper.ONGOING) {
				g2.drawImage(faceShocked, x, y, this);
				return;
			}
		}
			
		if(state == Minesweeper.ONGOING) {
			g2.drawImage(faceSmile, x, y, this);
		} else if(state == Minesweeper.LOST) {
			g2.drawImage(faceDead, x, y, this);
		} else if(state == Minesweeper.WON) {
			g2.drawImage(faceSunglasses, x, y, this);
		}	
	}
	
	private void drawTime(long elapsed) {
		int seconds = Math.min(999, (int)(elapsed/1e9));
		String[] out = String.format("%03d", seconds).split("");
		final int NUMBER_RIGHT_PAD = 15;
		for(int i = out.length-1; i >= 0; i--) {
			int n = Integer.parseInt(out[i]);
			int x = getWidth()-(NUMBER_RIGHT_PAD+NUMBER_WIDTH*(3-i));
			g2.drawImage(numDisp[n], x, 16, this);
		}
	}

	private void drawFlagsLeft(int bombs, int flags) {
		int left = bombs-flags;
		String[] out = String.format("%03d", Math.abs(left)).split("");
		final int NUMBER_LEFT_PAD = 17;
		
		for(int i = 0; i < out.length; i++) {
			int n = Integer.parseInt(out[i]);
			int x = NUMBER_LEFT_PAD+NUMBER_WIDTH*i;
			g2.drawImage(numDisp[n], x, 16, this);
		}

		if(left < 0) {
			g2.drawImage(negativeDisp, NUMBER_LEFT_PAD, 16, this);
		}
	}

	private void revealTile(int mX, int mY) {
		int RIGHT_BOUND = getWidth()-RIGHT_PAD-1;
		int BOT_BOUND = getHeight()-BOT_PAD-1;

		if(!withinBounds(mX, mY, LEFT_PAD, RIGHT_BOUND, TOP_PAD, BOT_BOUND))
			return;
		
		if(game.getState() != Minesweeper.ONGOING)
			return;

		if(game.revealedTiles.size() == 0)
			startTime = System.nanoTime();

		Coord c = mouseToCoord(mX, mY);
		game.revealTile(c.getX(), c.getY());
		return;
	}

	private void flagTile(int mX, int mY) {	
		int RIGHT_BOUND = getWidth()-RIGHT_PAD-1;
		int BOT_BOUND = getHeight()-BOT_PAD-1;

		if(!withinBounds(mX, mY, LEFT_PAD, RIGHT_BOUND, TOP_PAD, BOT_BOUND))
			return;
		
		if(game.getState() != Minesweeper.ONGOING)
			return;

		Coord c = mouseToCoord(mX, mY);
		game.flagTile(c.getX(), c.getY());
	}

	/* This is for the user clicking on the face */
	private void resetGame(int mx, int my) {
		final int BORDER_LEFT_PAD = borderStatLeft.getWidth();
		final int OFFSET = 4;
		int LEFT_BOUND = (int)(BORDER_LEFT_PAD+OFFSET+(game.SIZE_X-8)/2.0*TILE_SIZE);
		int RIGHT_BOUND  = LEFT_BOUND+FACE_SIZE;
		int TOP_BOUND = 17;
		int BOT_BOUND = TOP_BOUND+FACE_SIZE;
		if(withinBounds(mx, my, LEFT_BOUND, RIGHT_BOUND, TOP_BOUND, BOT_BOUND)) {
			game = new Minesweeper(gameWidth, gameHeight, gameBomb);
			setWindowSize(game.SIZE_X, game.SIZE_Y);
			elapsedTime = 0;
		}
	}

	/* This is for the MyFrame class for when the user changes the difficulty */
	public void resetGame() {
		game = new Minesweeper(gameWidth, gameHeight, gameBomb);
		setWindowSize(game.SIZE_X, game.SIZE_Y);
		elapsedTime=0;
	}

	private void setWindowSize(int sX, int sY) {
		int w = sX*TILE_SIZE+LEFT_PAD+RIGHT_PAD;
		int h = sY*TILE_SIZE+TOP_PAD+BOT_PAD+CHROME_PAD;
		frame.setSize(w, h);
		setPreferredSize(new Dimension(w, h));
	}

	public void setDifficulty(int sX, int sY, int nB) {
		gameWidth = sX;
		gameHeight = sY;
		gameBomb = nB;
	}

	private Coord mouseToCoord(int x, int y) {
		int cX = (x-LEFT_PAD)/TILE_SIZE;
		int cY = (y-TOP_PAD)/TILE_SIZE;
		return new Coord(cX, cY);
	}

	private boolean withinBounds(int x, int y, int xmin, int xmax, int ymin, int ymax) {
		return xmin <= x && x <= xmax && ymin <= y && y <= ymax;
	}
	
	private void setImages() {
		try {
			/* Numbers for Time */
			for(int i = 0; i < numDisp.length; i++) {
				numDisp[i] = ImageIO.read(new File("./Textures/numbers/"+i+".png"));
			}
			blankDisp = ImageIO.read(new File("./Textures/numbers/blank.png"));
			negativeDisp = ImageIO.read(new File("./Textures/numbers/negative.png"));

			/* Tiles */
			for(int i = 0; i < tiles.length; i++) {
				tiles[i] = ImageIO.read(new File("./Textures/tiles/"+i+".png"));
			}
			unrevealedTile = ImageIO.read(new File("./Textures/tiles/unrevealed.png"));
			flaggedTile = ImageIO.read(new File("./Textures/tiles/flagged.png"));
			wrongFlagTile = ImageIO.read(new File("./Textures/tiles/wrongflag.png"));
			
			/* Bombs */
			bomb = ImageIO.read(new File("./Textures/tiles/bomb.png"));
			revealedBomb = ImageIO.read(new File("./Textures/tiles/redbomb.png"));

			/* Borders */
			borderLeft  = ImageIO.read(new File("./Textures/borders/left.png"));
			borderRight = ImageIO.read(new File("./Textures/borders/right.png"));
			borderBot   = ImageIO.read(new File("./Textures/borders/bottom.png"));
			borderBotLeft  = ImageIO.read(new File("./Textures/borders/bottomleft.png"));
			borderBotRight = ImageIO.read(new File("./Textures/borders/bottomright.png"));

			/* Stat Borders */
			borderStatLeft  = ImageIO.read(new File("./Textures/borders/statleft.png"));
			borderStatRight = ImageIO.read(new File("./Textures/borders/statright.png"));
			borderStatMid  = ImageIO.read(new File("./Textures/borders/statmid.png"));
			borderStatFill = ImageIO.read(new File("./Textures/borders/statfill.png"));

			/* Faces */
			faceDead    = ImageIO.read(new File("./Textures/faces/dead.png"));
			faceShocked = ImageIO.read(new File("./Textures/faces/shocked.png"));
			faceSmile   = ImageIO.read(new File("./Textures/faces/smile.png"));
			faceSmilePressed = ImageIO.read(new File("./Textures/faces/smilePressed.png"));
			faceSunglasses = ImageIO.read(new File("./Textures/faces/sunglasses.png"));
		} catch(Exception e) {
			System.out.println(e);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if(game.revealedTiles.size() > 0 && game.getState() == Minesweeper.ONGOING)
			elapsedTime = System.nanoTime()-startTime;
		repaint();
	}

	public void mouseReleased(MouseEvent e) {
		mouseDown = false;
		mouseX = e.getX();
		mouseY = e.getY();

		if(mouseLeftClick) {
			revealTile(mouseX, mouseY);
			resetGame(mouseX, mouseY);
		}
	}

	public void mousePressed(MouseEvent e) {
		mouseDown = true;
		mouseLeftClick = SwingUtilities.isLeftMouseButton(e);
		mouseX = e.getX();
		mouseY = e.getY();
		
		if(!mouseLeftClick) {
			flagTile(mouseX, mouseY);
		}
	}

	public void mouseDragged(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	public void mouseExited(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
}
