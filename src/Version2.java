import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;


public class Version2 implements Version {
	public ForkJoinPool fjPool = new ForkJoinPool();
	private Rectangle usa;
	private CensusData popData;
	private int columns;
	private int rows;
	
	public Version2(CensusData parsedData, int columns, int rows) {
		this.usa = fjPool.invoke(new CornerThread(parsedData, 0, parsedData.data_size));
		this.columns = columns;
		this.rows = rows;
		this.popData = parsedData;
	}

	@Override
	public Pair<Integer, Float> singleInteraction(int w, int s, int e, int n) {
		int queryPop = fjPool.invoke(new QueryThread(popData, 0, popData.data_size, w, s, e, n));
		return new Pair(queryPop, 100 * (float) queryPop / usa.population);
	}
	
	public int getXPos(float lon) {
		// first, get distance from west
		float distanceFromWest = lon - usa.left;
		float columnWidth = (usa.right - usa.left) / this.columns;
		int column = (int) (distanceFromWest / columnWidth);
		return Math.min(columns - 1, column);
	}

	public int getYPos(float lat) {

		// first, get distance from west
		float distanceFromSouth = lat - usa.bottom;
		float rowLength = (usa.top - usa.bottom) / this.rows;
		int row = (int) (distanceFromSouth / rowLength);
		return Math.min(rows - 1, row);
	}

	private class CornerThread extends RecursiveTask<Rectangle> {

		private final int CUTOFF = 1000;
		private CensusData popData;
		private Rectangle subRectangle;
		private int lo;
		private int hi;

		public CornerThread(CensusData censusData, int lo, int hi){
			this.popData = censusData;
			this.subRectangle = new Rectangle(1000, -1000, -1000, 1000);
			this.lo = lo;
			this.hi = hi;
		}

		@Override
		protected Rectangle compute() {
			if (hi - lo < CUTOFF) { 
				for (int i = lo; i < hi; i++) {
					CensusGroup oneGroup = this.popData.data[i];
					this.subRectangle.top = Math.max(oneGroup.realLatitude, this.subRectangle.top);
					this.subRectangle.bottom = Math.min(oneGroup.realLatitude, this.subRectangle.bottom);
					this.subRectangle.left = Math.min(oneGroup.longitude, this.subRectangle.left);
					this.subRectangle.right = Math.max(oneGroup.longitude, this.subRectangle.right);
					this.subRectangle.population += oneGroup.population;
				}
				return this.subRectangle;
			} else {
				CornerThread leftThread = new CornerThread(this.popData, lo, (lo + hi) / 2);
				CornerThread rightThread = new CornerThread(this.popData, (lo + hi) / 2, hi);
				leftThread.fork();
				Rectangle rightRect = rightThread.compute();
				Rectangle leftRect = leftThread.join();
				
				return new Rectangle(Math.min(leftRect.left, rightRect.left),
						Math.max(leftRect.right, rightRect.right),
						Math.max(leftRect.top, rightRect.top),
						Math.min(leftRect.bottom, rightRect.bottom),
						leftRect.population + rightRect.population);
			}
		}
	}
	
	private class QueryThread extends RecursiveTask<Integer> {
		private final int CUTOFF = 100;
		private CensusData popData;
		private int lo;
		private int hi;
		private int w;
		private int s;
		private int e;
		private int n;
		
		public QueryThread(CensusData data, int lo, int hi, int w, int s, int e, int n) {
			this.popData = data;
			this.lo = lo; this.hi = hi; this.w = w; this.s = s; this.e = e; this.n = n;

		}
		
		@Override
		protected Integer compute() {
			if (hi - lo < CUTOFF) {
				int queryPop = 0;
				for (int i = lo; i < hi; i++) {
					CensusGroup oneGroup = popData.data[i];
					int row = getYPos(oneGroup.realLatitude);
					int col = getXPos(oneGroup.longitude);
					if (row >= s && row <= n && col <= e && col >= w) {
						queryPop += oneGroup.population;
					}
				}
				return queryPop;
			} else {
				QueryThread leftThread = new QueryThread(this.popData, lo, (lo + hi) / 2, w, s, e, n);
				QueryThread rightThread = new QueryThread(this.popData, (lo + hi) / 2, hi, w, s, e, n);
				leftThread.fork();
				Integer rightPop = rightThread.compute();
				Integer leftPop = leftThread.join();
				return leftPop + rightPop;
			}
		}
		
	
	}

}
