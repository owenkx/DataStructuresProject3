
public class Version1 {


	public static Rectangle preprocess(CensusData parsedData,
			int columns, int rows) {
		
		// longitudes and latitudes will never get to 1000 or negative 1000
		float top = -1000, bottom = 1000, left = 1000, right = -1000;
		
		for (int i = 0; i < parsedData.data_size; i++) {
			CensusGroup oneGroup = parsedData.data[i];
			top = Math.max(oneGroup.latitude, top);
			bottom = Math.min(oneGroup.latitude, bottom);
			left = Math.max(oneGroup.longitude, left);
			right = Math.min(oneGroup.longitude, right);
		}
		
		return new Rectangle(left, right, top, bottom);
	}
	
}
