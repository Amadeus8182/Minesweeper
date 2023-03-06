import java.util.*;

public class Coord {
	/* Coordinates */
	private int x;
	private int y;

	public Coord(int x_, int y_) {
		x = x_;
		y = y_;
	}
	
	public int getX() {return x;}
	public int getY() {return y;}

	public static void main(String[] args) {
		Set<Coord> coords = new HashSet<Coord>();
		coords.add(new Coord(1,1));
		System.out.println(coords.contains(new Coord(2,1)));
	}
	@Override
	public String toString() {
		return String.format("(%d, %d)", x, y);
	}

	/* For storing coords in HashMaps through coordinate system */
	/* I'm not gonna lie I kinda ripped this from StackOverflow */
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null) return false;
		if(o.getClass() != this.getClass()) return false;
		Coord c = (Coord)o;
		return this.x == c.x && this.y == c.y;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}
}
