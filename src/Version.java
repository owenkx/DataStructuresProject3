import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * A Version is an abstract class that allows operations over a set of US census data
 * It supports finding the population of a rectange within the USA
 * 
 * Via subclassing, it can implement many different algorithsm
 *
 */
public abstract class Version {
	
	// holds a representation of the USA, inclding boundaries and total population
	protected Rectangle usa;
	
	// the cutoff for parallel processing the US's corners
	public static final int CUTOFF = 2;
	
	// Holds the provided censusdata
	protected CensusData popData;
	
	// holds the provided number of rows and columns in the grid
	protected int columns;
	protected int rows;
	
	// used for parallelism
	protected ForkJoinPool fjPool = new ForkJoinPool();

	/**
	 * Returns the population and percentage of the USA within a certain grid
	 * @param w The western most column (0 based index) to consider
	 * @param s The southern most column (0 based index) to consider
	 * @param e The eastern most column (0 based index) to consider
	 * @param n The north most column (0 based index) to consider
	 * @return A pair holding the population within given rectangle, as well as that
	 * 	       as percentage of USA
	 */
	public abstract Pair<Integer, Float> singleInteraction(int w, int s, int e, int n);

	// Takes in a longitude and returns a the column within our chart that it belongs in
	protected int getXPos(float lon) {
		
		// first, get distance from west
		float distanceFromWest = lon - usa.left;
		
		// compute how wide each column is
		float columnWidth = (usa.right - usa.left) / this.columns;
		
		// divide distance from west edge of USA rectangle
		// by width of each column
		// and truncate to int
		int column = (int) (distanceFromWest / columnWidth);
		
		return (column != columns) ? column : column - 1;
	}

	//Takes in a latitude and returns the row in the grid in which it belongs
	protected int getYPos(float lat) {
		// first, get distance from west
		float distanceFromSouth = lat - usa.bottom;
		float rowLength = (usa.top - usa.bottom) / this.rows;
		int row = (int) (distanceFromSouth / rowLength);
		return (row != rows) ? row : row - 1;
	}

	// Fill the USA field, using a sequential algorithm
	// Results: this.USA is populated with boundaries and total population
	protected Rectangle getUsaSequential() {
		float top = -1000, bottom = 1000, left = 1000, right = -1000;
		int totalPop = 0;

		for (int i = 0; i < popData.data_size; i++) {
			CensusGroup oneGroup = popData.data[i];
			top = Math.max(oneGroup.latitude, top);
			bottom = Math.min(oneGroup.latitude, bottom);
			left = Math.min(oneGroup.longitude, left);
			right = Math.max(oneGroup.longitude, right);
			totalPop += oneGroup.population;
		}
		return new Rectangle(left, right, top, bottom, totalPop);
	}

	// Fills the USA field, using a parallel algorithm
	// Results: this.USA is populated with boundaries and a total population
	protected Rectangle getUsaParallel() {
		return fjPool.invoke(new CornerThread(0, popData.data_size));
	}
	
	// private thread class used for finding the corners of the census data
	private class CornerThread extends RecursiveTask<Rectangle> {

		private static final long serialVersionUID = 1L;
		private Rectangle subRectangle;
		private int lo;
		private int hi;

		public CornerThread(int lo, int hi){
			this.subRectangle = new Rectangle(1000, -1000, -1000, 1000);
			this.lo = lo;
			this.hi = hi;
		}

		// computes the smallest rectangle that holds all the coordinates
		@Override
		protected Rectangle compute() {

			//if the indices are close enough together, their corners are calculated
			if (hi - lo < CUTOFF) { 
				for (int i = lo; i < hi; i++) {
					CensusGroup oneGroup = popData.data[i];
					this.subRectangle.top = Math.max(oneGroup.latitude, this.subRectangle.top);
					this.subRectangle.bottom = Math.min(oneGroup.latitude, this.subRectangle.bottom);
					this.subRectangle.left = Math.min(oneGroup.longitude, this.subRectangle.left);
					this.subRectangle.right = Math.max(oneGroup.longitude, this.subRectangle.right);
					this.subRectangle.population += oneGroup.population;
				}
				return this.subRectangle;
			} else {
				// Make the threads do it!
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
	
}
