public class Version1 extends Version {
	private int totalPop;
	
	public Version1(CensusData parsedData,
			int columns, int rows) {
		
		// longitudes and latitudes will never get to 1000 or negative 1000
		float top = -1000, bottom = 1000, left = 1000, right = -1000;

		totalPop = 0;
		
		//get the boundaries of the map and sum the total population
		for (int i = 0; i < parsedData.data_size; i++) {
			CensusGroup oneGroup = parsedData.data[i];
			top = Math.max(oneGroup.realLatitude, top);
			bottom = Math.min(oneGroup.realLatitude, bottom);
			left = Math.min(oneGroup.longitude, left);
			right = Math.max(oneGroup.longitude, right);
			totalPop += oneGroup.population;
		}

		//stores a copy of the grid's size specification
		this.columns = columns;
		this.rows = rows;
		
		this.popData = parsedData;
		this.usa = new Rectangle(left, right, top, bottom);
	}

	//Takes in a set of coordinates and returns the total population in the block as well as the
	//percentage of the total population that exists in the query block.
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
}
