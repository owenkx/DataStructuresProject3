/**
 * SmartVersion is a subclss of Version that holds code used for dealing with "smart grids":
 * grids where each square holds the total population of everything above and to the left of
 * that cell 
 */

public abstract class SmartVersion extends Version {
	
	/** Holds the smart grid */
	protected int[][] grid;

	/**  takes in a grid and makes it a "smart grid", allowing population access in O(1)
	 * @param grid The grid to load the smart grid into
	 */
	public void makeSmartGrid(int[][] grid) {
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

	/**
	 * Returns the population and percentage of the USA within a certain grid
	 * Uses a smart grid
	 * @param w The western most column (0 based index) to consider
	 * @param s The southern most column (0 based index) to consider
	 * @param e The eastern most column (0 based index) to consider
	 * @param n The north most column (0 based index) to consider
	 * @param grid the smart grid to consider
	 * @return A pair holding the population within given rectangle, as well as that
	 * 	       as percentage of USA
	 */
	public Pair<Integer, Float> singleInteractionSmart(int w, int s, int e, int n, int[][] grid) {
		// swap north and south because coordinates
		// and array indices are inverted
		int temp = s;
		s = n;
		n = temp;

		int queryPop = grid[e][s];
		if (inGrid(e, n - 1)) { queryPop -= grid[e][n - 1]; }
		if (inGrid(w - 1, s)) { queryPop -= grid[w - 1][s]; }
		if (inGrid(w - 1, n - 1)) { queryPop += grid[w - 1][n - 1]; }

		return new Pair<Integer, Float>(queryPop, (float) queryPop / usa.population * 100);
	}

	// returns true iff. the given coordinate is within the grid
	private boolean inGrid(int x, int y) {
		return x < columns && x >= 0 && y < rows && y >= 1; 
	}

}
