import java.util.*;
import java.lang.IllegalArgumentException;

public class Minesweeper {
	public final int SIZE_X;
	public final int SIZE_Y;
	public final int NUM_BOMBS;

	public final Map<Coord, Integer> tiles;
	public final Set<Coord> revealedTiles;
	private Coord wrongBomb;
	final static int LOST = -1;
	final static int WON = 1;
	final static int ONGOING = 0;
	private int gameState = 0;

	public Minesweeper(int width, int height, int bombs) {
		if(width <= 3 && height <=3) throw new IllegalArgumentException("Please use an area larger than 3x3");
		SIZE_X = width;
		SIZE_Y = height;
		NUM_BOMBS = bombs;
		revealedTiles = new HashSet<Coord>();
		tiles = new HashMap<Coord, Integer>();
	}

	public Minesweeper() {
		this(9, 8, 10);
	}

	/* Have to set up basic game loop in main
	 * This is for users wanting to play on the terminal!!
	 */
	public static void main(String[] args) {
		Minesweeper game = new Minesweeper();
		while(game.getState() == Minesweeper.ONGOING) {
			System.out.println(game);
			game.play();
		}
		System.out.println(game);

		/* Custom Win/Lose messages */
		if(game.getState() == Minesweeper.WON) System.out.println("Congratulations!! You've Won!!");
		else if(game.getState() == Minesweeper.LOST) System.out.println("You're such a loser!! You lost!!");
	}

	/* This is only used for playing on the terminal!!!
	 * Use the reveal tile when using the GUI!!!
	 */
	public void play() {
		Scanner s = new Scanner(System.in);
		int x, y;
		while(true) {
			try{
				System.out.print("Enter x coordinate to reveal: ");
				x = Integer.parseInt(s.nextLine()) - 1;
				if(x < 0 || x >= SIZE_X) {
					System.out.println("Please enter a valid x-coordinate.");
					continue;
				}

				System.out.print("Enter y coordinate to reveal: ");
				y = Integer.parseInt(s.nextLine()) - 1;
				if(y < 0 || y >= SIZE_Y) {
					System.out.println("Please enter a valid y-coordinate.");
					continue;
				}
				break;
			} catch(Exception e) {
				System.out.println("Please enter an integer.");
			}
		}
		revealTile(x, y);
	}

	public int getState() {return gameState;}
	public Coord getWrongBomb() {return wrongBomb;}

	/* Game Methods */
	public void revealTile(int x, int y) {
		if(tiles.isEmpty()) setBombs(x, y, NUM_BOMBS);
		sweep(x, y);
		checkWin();
	}

	/* Horrible hacky code to properly align the grid and coordinates that can handle any size */
	@Override
	public String toString() {
		int xDigitLength = String.valueOf(SIZE_X).length();
		int yDigitLength = String.valueOf(SIZE_Y).length();
		String out = "\033\143\n";
		
		/* x coordinates */
		out += String.format("%-"+(yDigitLength+3)+"s ", " ");
		for(int x = 0; x < SIZE_X; x++) out += String.format("%-"+(xDigitLength+1)+"d", (x+1));
		out +="\n";
		out += String.format("%-"+(yDigitLength+2)+"s", " ");
		out += "+";
		
		/* Top Border */
		for(int i = 0; i < SIZE_X; i++) out += String.format("%-"+(xDigitLength+1)+"s", "-").replace(' ', '-');
		out = out.substring(0, out.length()-(xDigitLength-1))+"-";
		out += "+\n";

		for(int y = 0; y < SIZE_Y; y++) {
			/* y coordinates and Left Border */
			out += String.format("%"+(yDigitLength+1)+"d | ", (y+1));

			/* Board */
			for(int x = 0; x < SIZE_X; x++) {
				Coord c = new Coord(x, y);
				if(revealedTiles.contains(c)) {
					if(tiles.get(c) > 0) out += String.format("%-"+(xDigitLength+1)+"d", tiles.get(c)); 
					else if(tiles.get(c) == -1) out += String.format("%-"+(xDigitLength+1)+"s", "b");
					else out += String.format("%-"+(xDigitLength+1)+"s", ".");
				} else {
					out += String.format("%-"+(xDigitLength+1)+"s", "x");
				}
			}
			
			/* Right Border */
			out = out.substring(0,out.length()-(xDigitLength-1))+"|\n";
		}

		/* Bottom Border */
		out += String.format("%-"+(yDigitLength+2)+"s", " ");
		out += "+";
		for(int i = 0; i < SIZE_X; i++) out += String.format("%-"+(xDigitLength+1)+"s", "-").replace(' ', '-');
		out = out.substring(0, out.length()-(xDigitLength-1))+"-";
		out += "+\n";
		
		return out;
	}
	
	/* Sets Bombs on the board once player clicks on the board.
	 * Bombs cannot spawn adjacent to where player clicked. 
	 */
	private void setBombs(int x, int y, int n) {
		Random r = new Random();
		Coord c = new Coord(x, y);
		Set<Coord> invalidCoords = new HashSet<Coord>(Arrays.asList(getEdges(c)));
		invalidCoords.add(c);

		for(int i = 0; i < n; i++) {
			int oCX = (int)(r.nextDouble()*SIZE_X);
			int oCY = (int)(r.nextDouble()*SIZE_Y);
			Coord oC = new Coord(oCX, oCY);
			while(invalidCoords.contains(oC) || tiles.containsKey(oC)) {
				oCX = (int)(r.nextDouble()*SIZE_X);
				oCY = (int)(r.nextDouble()*SIZE_Y);
				oC = new Coord(oCX, oCY);
			}

			tiles.put(oC, -1);
		}
	}

	private void checkWin() {
		if(revealedTiles.size() == SIZE_X*SIZE_Y-NUM_BOMBS) {
			for(Coord c : tiles.keySet()) revealedTiles.add(c);
			gameState = WON;
		}
	}

	/* Call this method after user clicks/inputs tile to remove */
	private void sweep(int x, int y) {
		Coord c = new Coord(x, y);
		setState(c);
		/* Check if it is bomb */
		if(tiles.containsKey(c) && tiles.get(c) == -1) {
			gameState = LOST;
			wrongBomb = c;
			for(Coord oC : tiles.keySet()) {
				if(oC.equals(wrongBomb))
					continue;
				revealedTiles.add(oC);
			}
			return;
		}

		/* Check if it's already been revealed */
		if(revealedTiles.contains(c)) {
			return;
		}

		/* Check if it is adjacent to a bomb */
		if(tiles.get(c) > 0) {
			revealedTiles.add(c);
			return;
		}

		/* Sweep using BFS if it's empty */
		Set<Coord> explored = new HashSet<Coord>();
		ArrayDeque<Coord> queue = new ArrayDeque<Coord>();

		explored.add(c);
		queue.add(c);
		revealedTiles.add(c);
		while(!queue.isEmpty()) {
			Coord currCoord = (Coord)queue.pop();
	
			Coord[] edges = getEdges(currCoord);
			for(Coord edge : edges) {
				/* Skip if it's already been explored */
				if(explored.contains(edge)) continue;

				/* Skip if it's out of bounds */	
				int edgeX = edge.getX();
				int edgeY = edge.getY();
				if(!(edgeX >= 0 && edgeX < SIZE_X) || !(edgeY >= 0 && edgeY < SIZE_Y)) continue;

				explored.add(edge);

				/* If it's a bomb, don't do anything else */
				if(tiles.containsKey(edge) && tiles.get(edge) == -1) continue;

				/* If it's adjacent to a bomb, add to revealed but don't add to queue */
				setState(edge);
				revealedTiles.add(edge);
				if(tiles.get(edge) > 0) continue;

				queue.add(edge);
			}
		}
	}

	private void setState(Coord c) {
		if(tiles.containsKey(c)) return; // Don't need to set state again if it's already been explored.
										 
		int state = 0;
		Coord[] neighbors = getEdges(c);
		for(Coord oC : neighbors) {
			if(tiles.containsKey(oC) && tiles.get(oC) == -1) state++;
		}

		tiles.put(c, state);
	}

	private Coord[] getEdges(Coord c) {
		int x = c.getX();
		int y = c.getY();
		return new Coord[] {new Coord(x-1, y-1), new Coord(x  , y-1), new Coord(x+1, y-1),
			            	new Coord(x-1, y  ), /*    This Coord  */ new Coord(x+1, y  ),
							new Coord(x-1, y+1), new Coord(x  , y+1), new Coord(x+1, y+1)};
	}

}
