
public class Query1 {
	
	public void process(CensusData data, int w, int s, int e, int n) {
		float top = - Float.MAX_VALUE;
		float bot = Float.MAX_VALUE;
		float left = - Float.MAX_VALUE;
		float right = Float.MAX_VALUE;
		
		int totalPop = 0;
		for (int i = 0; i < data.data_size; i++) {
			CensusGroup group = data.data[i];
			top = Math.max(n, group.latitude);
			bot = Math.min(s, group.latitude);
			left = Math.max(w, group.longitude);
			right = Math.min(e, group.longitude);
			totalPop += group.population;
		}
		int boundPop = 0;
		for (int i = 0; i < data.data_size; i++) {
			CensusGroup group = data.data[i];
			if (group.longitude < e && group.longitude > w
					&& group.latitude > s && group.latitude < n) {
				
			}
				
		}
	}
}
