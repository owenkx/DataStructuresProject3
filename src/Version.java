
public abstract class Version {
	public Rectangle usa;
	public CensusData popData;
	public int columns;
	public int rows;
	
	public abstract Pair<Integer, Float> singleInteraction(int w, int s, int e, int n);
	
	
	//Takes in a longitude and returns a x coordinate in the grid
	public int getXPos(float lon) {
		// first, get distance from west
		float distanceFromWest = lon - usa.left;
		float columnWidth = (usa.right - usa.left) / this.columns;
		int column = (int) (distanceFromWest / columnWidth);
		return Math.min(columns - 1, column);
	}

	//Takes in a latitude and returns a y coordinate in the grid
	public int getYPos(float lat) {
		// first, get distance from west
		float distanceFromSouth = lat - usa.bottom;
		float rowLength = (usa.top - usa.bottom) / this.rows;
		int row = (int) (distanceFromSouth / rowLength);
		return Math.min(rows - 1, row);
	}
	
}
