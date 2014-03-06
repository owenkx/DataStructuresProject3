
public class Version3 extends Version{
	private int[][] grid;

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

		// compute the smart grid
		// first, we set the top row
		int workingCount = 0;
		for (int i = 0; i < this.columns; i++) {
			workingCount += grid[i][0];
			grid[i][0] = workingCount;
		}

		// Next, we compute the first column
		workingCount = 0;
		for (int i = 0; i < this.rows; i++) {
			workingCount += grid[0][i];
			grid[0][i] = workingCount;
		}

		// Now we compute the rest of the smart array
		// We iterate first by row, then within each row by column
		for (int row = 1; row < this.rows; row++) {
			for (int col = 1; col < this.columns; col++) {

				// The value in this grid spot is the sum of the two adjacent
				// (top, left) and original, minus the value above and to the left
				grid[col][row] = grid[col][row] + grid[col - 1][row] + 
						grid[col][row - 1] - grid[col-1][row - 1];
			}
		}

		// Boom! Grid now is a populated smart grid
	}

	@Override
	public Pair<Integer, Float> singleInteraction(int w, int s, int e, int n) {
		
		// swap north and south because coordinates
		// and array indices are inverted
		int temp = s;
		s = n;
		n = temp;
		
		int queryPop = grid[e][s];
		if (inGrid(e, n - 1)) { queryPop -= grid[e][n + 1]; }
		if (inGrid(w - 1, s)) { queryPop -= grid[w - 1][s]; }
		if (inGrid(w - 1, n - 1)) { queryPop += grid[w - 1][n + 1]; }
		
		return new Pair<Integer, Float>(queryPop, (float) queryPop / usa.population * 100);
	}

	private boolean inGrid(int x, int y) {
		return x < columns && x >= 0 && y < rows && y >= 1; 
	}
}
