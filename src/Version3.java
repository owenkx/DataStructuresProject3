
public class Version3 extends SmartVersion{
	
	public Version3(CensusData parsedData, int columns, int rows) {
		this.popData = parsedData;
		grid = new int[columns][rows];
		this.usa = getUsaSequential();
		this.columns = columns;
		this.rows = rows;

		for (int i = 0; i < parsedData.data_size; i++) {
			CensusGroup oneGroup = parsedData.data[i];

			grid[getXPos(oneGroup.longitude)][getYPos(oneGroup.realLatitude)]
					+= oneGroup.population;
		}
		//make the grid a smart grid
		makeSmartGrid(grid);
	}

	@Override
	public Pair<Integer, Float> singleInteraction(int w, int s, int e, int n) {
		return singleInteractionSmart(w, s, e, n, grid);
	}
}
