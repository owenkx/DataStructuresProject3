/**
 * A Version3 is a subclass of Version (and by extension, Version) that uses smart but sequential algorithms
 *
 */
public class Version3 extends SmartVersion{
	
	/** Create a new Version3
	 * @param parsedData the CensusData to analyze
	 * @param columns the number of columns to model
	 * @param rows the number of rows to model
	 */
	public Version3(CensusData parsedData, int columns, int rows) {
		this.popData = parsedData;
		grid = new int[columns][rows];
		this.usa = getUsaSequential();
		this.columns = columns;
		this.rows = rows;

		for (int i = 0; i < parsedData.data_size; i++) {
			CensusGroup oneGroup = parsedData.data[i];

			grid[getXPos(oneGroup.longitude)][getYPos(oneGroup.latitude)]
					+= oneGroup.population;
		}
		//make the grid a smart grid
		makeSmartGrid(grid);
	}
	
	/** {@inheritDoc} */
	@Override
	public Pair<Integer, Float> singleInteraction(int w, int s, int e, int n) {
		return singleInteractionSmart(w, s, e, n, grid);
	}
}
