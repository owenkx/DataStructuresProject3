public class Problem6 {
	private final static int NUM_TEST = 20;
	private final static int NUM_WARMUP = 3;

	public static void main (String[] args) {
		System.out.println("Runtime of V1 " +  getAverageRuntime(args));
	}

	private static double getAverageRuntime(String[] args) {
		double totalTime = 0;
		for(int i=0; i<NUM_TEST; i++) {
			long startTime = System.currentTimeMillis();
			PopulationQuery.preprocess("CenPop2010.txt", 100, 500, 1);
			long endTime = System.currentTimeMillis();
			if(NUM_WARMUP <= i) {                    // Throw away first NUM_WARMUP runs to encounter JVM warmup
				totalTime += (endTime - startTime);
			}
		}
		return totalTime / (NUM_TEST-NUM_WARMUP);  // Return average runtime.
	}
}
