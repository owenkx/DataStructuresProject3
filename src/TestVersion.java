import static org.junit.Assert.*;
import org.junit.*;

public class TestVersion {
	private static final int TIMEOUT = 2000; // 2000ms = 2sec
	private static final int NUM_VERSIONS = 5;
	private Version[] versions;
	private CensusData popData;
	
	@Before
	public void setup() {
		popData = PopulationQuery.parse("CenPop2010.txt");
		versions = new Version[NUM_VERSIONS];
		versions[0] = new Version1(popData, 100, 500);
		versions[1] = new Version2(popData, 100, 500);
		versions[2] = new Version3(popData, 100, 500);
		versions[3] = new Version4(popData, 100, 500);
		versions[4] = new Version5(popData, 100, 500);
	}
	
	@Test
	public void testNoPopulation() {
		compareResults(1, 1, 1, 1);
	}
	
	@Test
	public void testAllPopulation() {
		compareResults(1, 1, 100, 500);
	}
	
	@Test
	public void testBeastCoast() {
		compareResults(50, 1, 100, 500);
	}
	
	@Test
	public void testBestCoast() {
		compareResults(1, 1, 50, 500);
	}
	
	@Test
	public void testTopHalf() {
		compareResults(1, 250, 100, 500);
	}

	@Test
	public void testBottomHalf() {
		compareResults(1, 1, 100, 250);
	}
	
	@Test
	public void testArea1() {
		compareResults(30, 250, 50, 500);
	}
	
	@Test
	public void testArea2() {
		compareResults(1, 100, 80, 250);
	}
	
	private void compareResults(int w, int s, int e, int n) {
		w--;
		s--;
		e--;
		n--;
		Pair<Integer, Float> current = versions[0].singleInteraction(w, s, e, n);
		for (int j = 1; j < NUM_VERSIONS; j++) {
			Pair<Integer, Float> comparison = versions[j].singleInteraction(w, s, e, n);
			assertEquals(current.getElementA(), comparison.getElementA());
		}
	}
}
