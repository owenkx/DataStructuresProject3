public class Version5 extends SmartVersion {

	private static final int NUM_THREADS = 4;

	// stores the population of each rectangle
	private Object[][] locks;


	public Version5(CensusData parsedData, int columns, int rows) {
		// store passed info
		this.columns = columns;
		this.rows = rows;
		this.popData = parsedData;

		// calculate USA boundaries
		this.usa = getUsaParallel();

		// build the grid's locks
		this.grid = new int[columns][rows];
		this.locks = new Object[columns][rows];
		for (int i = 0; i < columns; i++) {
			for (int j = 0; j < rows; j++) {
				locks[i][j] = new Object();
			}
		}

		// build our threads
		BuildGridThread[] threadArr = new BuildGridThread[NUM_THREADS];
		for (int i = 0; i < NUM_THREADS; i++) {
			// TODO: Off by one error?
			threadArr[i] = new BuildGridThread(i * (this.popData.data_size / NUM_THREADS),
					(i + 1) * (this.popData.data_size / NUM_THREADS), locks);
			threadArr[i].run();
		}

		for (int i = 0; i < NUM_THREADS; i++) {
			try {
				threadArr[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// make the grid a smart grid
		makeSmartGrid(grid);
	}

	@Override
	public Pair<Integer, Float> singleInteraction(int w, int s, int e, int n) {
		return singleInteractionSmart(w, s, e, n, grid);
	}

	private class BuildGridThread extends Thread {

		private int lo, hi;
		private Object[][] lockGrid;
		
		public BuildGridThread(int lo, int hi, Object[][] lockGrid) {
			this.lo = lo;
			this.hi = hi;
			this.lockGrid = lockGrid;
		}

		public void run() {
			for (int i = lo; i < hi; i++) {

				// Get the current censusdata
				CensusGroup oneGroup = popData.data[i];
				int col = getXPos(oneGroup.longitude);
				int row = getYPos(oneGroup.realLatitude);
				
				synchronized (lockGrid[col][row]) {
					grid[col][row] += oneGroup.population;
				}
			}
		}
	}

}
