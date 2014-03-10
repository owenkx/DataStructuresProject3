
public class Version5 extends Version {

	private static final int NUM_THREADS = 4;

	// stores the population of each rectangle
	private int[][] grid;
	public Object[][] locks;


	public Version5(CensusData parsedData, int columns, int rows) {
		// store passed info
		this.columns = columns;
		this.rows = rows;
		this.popData = parsedData;

		// calculate USA boundaries
		this.usa = getUsaParallel();

		// build grid
		this.grid = new int[columns][rows];
		this.locks = new Object[columns][rows];
		for (int i = 0; i < columns; i++) {
			for (int j = 0; j < columns; j++) {
				locks[i][j] = new Object();
			}
		}

		// build our threads
		BuildGridThread[] threadArr = new BuildGridThread[NUM_THREADS];
		for (int i = 0; i < NUM_THREADS; i++) {
			// TODO: Off by one error?
			threadArr[i] = new BuildGridThread(i * (this.popData.data_size / NUM_THREADS),
					(i + 1) * (this.popData.data_size / NUM_THREADS));
			threadArr[i].run();
		}

		for (int i = 0; i < NUM_THREADS; i++) {
			try {
				threadArr[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// TODO: move
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
		return singleInteractionSmart(w, s, e, n, grid);
	}

	// TODO: THIS
	private class BuildGridThread extends Thread {

		private int lo, hi;

		public BuildGridThread(int lo, int hi) {
			this.lo = lo;
			this.hi = hi;
		}

		public void run() {
			for (int i = lo; i < hi; i++) {

				// Get the current censusdata
				CensusGroup oneGroup = popData.data[i];
				int col = getXPos(oneGroup.longitude);
				int row = getYPos(oneGroup.realLatitude);
				
				assert(locks[col][row] != null);
				synchronized (locks[col][row]) {
					grid[col][row] += oneGroup.population;
				}
			}
		}
	}

}
