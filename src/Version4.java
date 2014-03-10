import java.util.Arrays;
import java.util.concurrent.RecursiveTask;

public class Version4 extends Version {
	public int[][] grid;

	public Version4(CensusData parsedData, int columns, int rows) {
		this.columns = columns;
		this.rows = rows;
		this.popData = parsedData;
		this.usa = getUsaParallel();
		grid = fjPool.invoke(new GridThread(0, popData.data_size));

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


	private class GridThread extends RecursiveTask<int[][]> {
		int hi, lo;
		int[][] grid;
		private final int CUTOFF = 1000;


		public GridThread(int lo, int hi){
			this.lo = lo;
			this.hi = hi;
			this.grid = new int[columns][rows];
		}

		@Override
		protected int[][] compute() {
			if (hi - lo < CUTOFF) {
				for (int i = lo; i < hi; i++) {
					CensusGroup oneGroup = popData.data[i];
					int col = getXPos(oneGroup.longitude); 
					int row = getYPos(oneGroup.realLatitude);
					int pop = oneGroup.population;
					
					grid[col][row] += pop;
				}
				return this.grid;
			} else {
				GridThread leftThread = new GridThread(lo, (lo + hi) / 2);
				GridThread rightThread = new GridThread((lo + hi) / 2, hi);
				leftThread.fork();
				int[][] rightGrid = rightThread.compute();
				int[][] leftGrid = leftThread.join();
				return fjPool.invoke(new sumThread(leftGrid, rightGrid, 0, columns));
			}
		}

		private class sumThread extends RecursiveTask<int[][]> {
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
