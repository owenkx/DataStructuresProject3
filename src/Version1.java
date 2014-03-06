public class Version1 extends Version {
	
	public Version1(CensusData parsedData,
			int columns, int rows) {
		
		//stores a copy of the grid's size specification
		this.columns = columns;
		this.rows = rows;
		this.popData = parsedData;
		this.usa = getUsaSequential();
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
		return new Pair(queryPop, 100 * (float) queryPop / usa.population);
	}
}
