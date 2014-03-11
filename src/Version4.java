import java.util.concurrent.RecursiveTask;

/**
 * Version4 is a subclass of SmartVersion (and thus, Version) that relies on smart and parallel algoirthsms 
 *
 */
public class Version4 extends SmartVersion {

	/** Create a new Version4
	 * @param parsedData the CensusData to analyze
	 * @param columns the number of columns to model
	 * @param rows the number of rows to model
	 */
	public Version4(CensusData parsedData, int columns, int rows) {
		this.columns = columns;
		this.rows = rows;
		this.popData = parsedData;
		this.usa = getUsaParallel();
		grid = fjPool.invoke(new GridThread(0, popData.data_size));

		makeSmartGrid(grid);
	}

	/** {@inheritDoc} */
	@Override
	public Pair<Integer, Float> singleInteraction(int w, int s, int e, int n) {
		return singleInteractionSmart(w, s, e, n, grid);
	}

	// Private class for recursively computing the smart grid
	private class GridThread extends RecursiveTask<int[][]> {
		private static final long serialVersionUID = 1L;
		int hi, lo;
		int[][] grid;
		private final int CUTOFF = 1000;


		// Takes the smallest and highest values of censudGroups to analyze
		public GridThread(int lo, int hi){
			this.lo = lo;
			this.hi = hi;
			this.grid = new int[columns][rows];
		}

		@Override
		protected int[][] compute() {
			if (hi - lo < CUTOFF) {
				// If close together, manually fill the grid
				for (int i = lo; i < hi; i++) {
					CensusGroup oneGroup = popData.data[i];
					int col = getXPos(oneGroup.longitude); 
					int row = getYPos(oneGroup.latitude);
					int pop = oneGroup.population;
					
					grid[col][row] += pop;
				}
				return this.grid;
			} else {
				// Otherwise, compute the grid in two threads, combine and return
				GridThread leftThread = new GridThread(lo, (lo + hi) / 2);
				GridThread rightThread = new GridThread((lo + hi) / 2, hi);
				leftThread.fork();
				int[][] rightGrid = rightThread.compute();
				int[][] leftGrid = leftThread.join();
				return fjPool.invoke(new sumThread(leftGrid, rightGrid, 0, columns));
			}
		}

		// Private class: uses for summing togther grids
		private class sumThread extends RecursiveTask<int[][]> {
			private static final long serialVersionUID = 1L;
			int hi, lo;
			int[][] grid1, grid2;
			private final int CUTOFF = 50;

			public sumThread(int[][] grid1, int[][] grid2, int lo, int hi){
				this.lo = lo;
				this.hi = hi;
				this.grid1 = grid1;
				this.grid2 = grid2;
			}

			@Override
			protected int[][] compute() {
				if (hi - lo < CUTOFF) {
					for (int i = lo; i < hi; i++) {
						for (int j = 0; j < rows; j++) {
							grid1[i][j] += grid2[i][j];
						}
					}
					return grid1;
				} else {
					sumThread leftThread = new sumThread(grid1, grid2, lo, (lo + hi) / 2);
					sumThread rightThread = new sumThread(grid1, grid2, (lo + hi) / 2, hi);
					leftThread.fork();
					int[][] rightGrid = rightThread.compute();
					int[][] leftGrid = leftThread.join();
					for (int i = (lo + hi) / 2; i < hi; i++) {
						leftGrid[i] = rightGrid[i];
					}
					return leftGrid;
				}
			}
		}
	}
}
