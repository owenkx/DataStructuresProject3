
public class Version1 implements Version {

	private Rectangle usa;
	private CensusData popData;
	private int columns;
	private int rows;
	private int totalPop;
	
	public Version1(CensusData parsedData,
			int columns, int rows) {
		
		// longitudes and latitudes will never get to 1000 or negative 1000
		float top = -1000, bottom = 1000, left = 1000, right = -1000;

		totalPop = 0;
		for (int i = 0; i < parsedData.data_size; i++) {
			CensusGroup oneGroup = parsedData.data[i];
			top = Math.max(oneGroup.realLatitude, top);
			bottom = Math.min(oneGroup.realLatitude, bottom);
			left = Math.min(oneGroup.longitude, left);
			right = Math.max(oneGroup.longitude, right);
			totalPop += oneGroup.population;
		}

		this.columns = columns;
		this.rows = rows;
		this.popData = parsedData;
		this.usa = new Rectangle(left, right, top, bottom);

	}

	public Pair<Integer, Float> singleInteraction(int w, int s, int e, int n) {
		int queryPop = 0;
		for (int i = 0; i < popData.data_size; i++) {
			CensusGroup oneGroup = popData.data[i];
			int row = getYPos(oneGroup.realLatitude);
			int col = getXPos(oneGroup.longitude);
			if (row >= s && row <= n && col <= e && col >= w) {
				queryPop += oneGroup.population;
			}
		}
		return new Pair(queryPop, 100 * (float) queryPop / totalPop);
	}

	public int getXPos(float lon) {
		// first, get distance from west
		float distanceFromWest = lon - usa.left;
		float columnWidth = (usa.right - usa.left) / this.columns;
		int column = (int) (distanceFromWest / columnWidth);
		return Math.min(columns - 1, column);
	}

	public int getYPos(float lat) {

		// first, get distance from west
		float distanceFromSouth = lat - usa.bottom;
		float rowLength = (usa.top - usa.bottom) / this.rows;
		int row = (int) (distanceFromSouth / rowLength);
		return Math.min(rows - 1, row);
	}

	public String toString() {
		return "";
	}
}
