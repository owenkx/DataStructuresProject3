import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public abstract class Version {
	protected Rectangle usa;
	protected CensusData popData;
	protected int columns;
	protected int rows;
	protected ForkJoinPool fjPool = new ForkJoinPool();

	public abstract Pair<Integer, Float> singleInteraction(int w, int s, int e, int n);

	//Takes in a longitude and returns a x coordinate in the grid
	protected int getXPos(float lon) {
		// first, get distance from west
		float distanceFromWest = lon - usa.left;
		float columnWidth = (usa.right - usa.left) / this.columns;
		int column = (int) (distanceFromWest / columnWidth);
		return Math.min(columns - 1, column);
	}

	//Takes in a latitude and returns a y coordinate in the grid
	protected int getYPos(float lat) {
		// first, get distance from west
		float distanceFromSouth = lat - usa.bottom;
		float rowLength = (usa.top - usa.bottom) / this.rows;
		int row = (int) (distanceFromSouth / rowLength);
		return Math.min(rows - 1, row);
	}

	protected Rectangle getUsaSequential() {
		float top = -1000, bottom = 1000, left = 1000, right = -1000;
		int totalPop = 0;

		for (int i = 0; i < popData.data_size; i++) {
			CensusGroup oneGroup = popData.data[i];
			top = Math.max(oneGroup.realLatitude, top);
			bottom = Math.min(oneGroup.realLatitude, bottom);
			left = Math.min(oneGroup.longitude, left);
			right = Math.max(oneGroup.longitude, right);
			totalPop += oneGroup.population;
		}
		return new Rectangle(left, right, top, bottom, totalPop);
	}

	protected Rectangle getUsaParallel() {
		return fjPool.invoke(new CornerThread(0, popData.data_size));
	}
	
	//thread class used for finding the corners of the census data
	private class CornerThread extends RecursiveTask<Rectangle> {
		private final int CUTOFF = 1000;
		private Rectangle subRectangle;
		private int lo;
		private int hi;

		public CornerThread(int lo, int hi){
			this.subRectangle = new Rectangle(1000, -1000, -1000, 1000);
			this.lo = lo;
			this.hi = hi;
		}

		//computes the smallest rectangle that holds all the coordinates
		@Override
		protected Rectangle compute() {

			//if the indices are close enough together, their corners are calculated
			if (hi - lo < CUTOFF) { 
				for (int i = lo; i < hi; i++) {
					CensusGroup oneGroup = popData.data[i];
					this.subRectangle.top = Math.max(oneGroup.realLatitude, this.subRectangle.top);
					this.subRectangle.bottom = Math.min(oneGroup.realLatitude, this.subRectangle.bottom);
					this.subRectangle.left = Math.min(oneGroup.longitude, this.subRectangle.left);
					this.subRectangle.right = Math.max(oneGroup.longitude, this.subRectangle.right);
					this.subRectangle.population += oneGroup.population;
				}
				return this.subRectangle;
			} else {
				CornerThread leftThread = new CornerThread(lo, (lo + hi) / 2);
				CornerThread rightThread = new CornerThread((lo + hi) / 2, hi);
				leftThread.fork();
				Rectangle rightRect = rightThread.compute();
				Rectangle leftRect = leftThread.join();

				//Calculates the biggest rectangle that can fit both rectangle edges
				return new Rectangle(Math.min(leftRect.left, rightRect.left),
						Math.max(leftRect.right, rightRect.right),
						Math.max(leftRect.top, rightRect.top),
						Math.min(leftRect.bottom, rightRect.bottom),
						leftRect.population + rightRect.population);
			}
		}
	}

	public Pair<Integer, Float> singleInteractionSmart(int w, int s, int e, int n, int[][] grid) {

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
