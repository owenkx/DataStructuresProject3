import java.util.concurrent.RecursiveTask;

/** 
 * Version2 is a subclass of Version1 that uses naive but parallel algorithms 
 *
 */
public class Version2 extends Version {
	
	/** Create a new Version2
	 * @param parsedData the CensusData to analyze
	 * @param columns the number of columns to model
	 * @param rows the number of rows to model
	 */
	public Version2(CensusData parsedData, int columns, int rows) {
		this.columns = columns;
		this.rows = rows;
		this.popData = parsedData;
		this.usa = getUsaParallel();
	}

	/** {@inheritDoc} */
	@Override
	public Pair<Integer, Float> singleInteraction(int w, int s, int e, int n) {
		int queryPop = fjPool.invoke(new QueryThread(0, popData.data_size, w, s, e, n));
		return new Pair<Integer, Float>(queryPop, 100 * (float) queryPop / usa.population);
	}
	
	//Used to sum the populations in the query block, returns the population in the block
	private class QueryThread extends RecursiveTask<Integer> {

		private static final long serialVersionUID = 1L;
		private final int CUTOFF = 100;
		private int lo;
		private int hi;
		private int w;
		private int s;
		private int e;
		private int n;
		
		public QueryThread(int lo, int hi, int w, int s, int e, int n) {
			this.lo = lo; this.hi = hi; this.w = w; this.s = s; this.e = e; this.n = n;

		}
		
		//Computes the population in the specified block
		@Override
		protected Integer compute() {
			//if the indices are close enough, the corresponding census group
			//populations are summed up if the query is within the bounds of query block.
			if (hi - lo < CUTOFF) {
				int queryPop = 0;
				for (int i = lo; i < hi; i++) {
					CensusGroup oneGroup = popData.data[i];
					int row = getYPos(oneGroup.latitude);
					int col = getXPos(oneGroup.longitude);
					if (row >= s && row <= n && col <= e && col >= w) {
						queryPop += oneGroup.population;
					}
				}
				return queryPop;

			//sums up the running total
			} else {
				QueryThread leftThread = new QueryThread(lo, (lo + hi) / 2, w, s, e, n);
				QueryThread rightThread = new QueryThread((lo + hi) / 2, hi, w, s, e, n);
				leftThread.fork();
				Integer rightPop = rightThread.compute();
				Integer leftPop = leftThread.join();
				return leftPop + rightPop;
			}
		}
	}
}
