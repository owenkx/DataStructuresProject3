/**
 * Version1 is a subclass of Version that relies on naive and sequential algorithms 
 *
 */

public class Version1 extends Version {
	
	/** Create a new Version1
	 * @param parsedData the CensusData to analyze
	 * @param columns the number of columns to model
	 * @param rows the number of rows to model
	 */
	public Version1(CensusData parsedData,
			int columns, int rows) {
		
		//stores a copy of the grid's size specification
		this.columns = columns;
		this.rows = rows;
		this.popData = parsedData;
		this.usa = getUsaSequential();
	}

	/** {@inheritDoc} */
	@Override
	public Pair<Integer, Float> singleInteraction(int w, int s, int e, int n) {
		int queryPop = 0;
		
		// Iterate for each member of population data
		for (int i = 0; i < popData.data_size; i++) {
			
			// Extract the current population
			CensusGroup oneGroup = popData.data[i];
			
			// Obtain the row and column in which the location is
			int row = getYPos(oneGroup.latitude);
			int col = getXPos(oneGroup.longitude);
			
			// If the row and column for oneGroup is within the provided rectangle
			// (Borders inclusive), add to our running population)
			if (row >= s && row <= n && col <= e && col >= w) {

				queryPop += oneGroup.population;
			}
		}
		return new Pair<Integer, Float>(queryPop, 100 * (float) queryPop / usa.population);
	}
}
