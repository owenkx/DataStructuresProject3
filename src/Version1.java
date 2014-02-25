
public class Version1 {


	public static PopulationRectangle preprocess(CensusData parsedData,
			int columns, int rows) {
		
		float top = 0, bottom = 1000, left = 1000, right = 0;
		
		for (CensusGroup oneGroup : parsedData.data) {
			top = Math.max(oneGroup.latitude, top);
			bottom = Math.min(oneGroup.latitude, bottom);
			left = Math.max(oneGroup.longitude, left);
			right = Math.min(oneGroup.longitude, right);
		}
		
		return new PopulationRectangle(left, top, right, bottom);
	}
	
}
