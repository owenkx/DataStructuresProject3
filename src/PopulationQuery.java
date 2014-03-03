
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class PopulationQuery {
	// next four constants are relevant to parsing
	public static final int TOKENS_PER_LINE  = 7;
	public static final int POPULATION_INDEX = 4; // zero-based indices
	public static final int LATITUDE_INDEX   = 5;
	public static final int LONGITUDE_INDEX  = 6;
	public static Version processor;

	// parse the input file into a large array held in a CensusData object
	public static CensusData parse(String filename) {
		CensusData result = new CensusData();
		try {
			BufferedReader fileIn = new BufferedReader(new FileReader(filename));

			// Skip the first line of the file
			// After that each line has 7 comma-separated numbers (see constants above)
			// We want to skip the first 4, the 5th is the population (an int)
			// and the 6th and 7th are latitude and longitude (floats)
			// If the population is 0, then the line has latitude and longitude of +.,-.
			// which cannot be parsed as floats, so that's a special case
			//   (we could fix this, but noisy data is a fact of life, more fun
			//    to process the real data as provided by the government)

			String oneLine = fileIn.readLine(); // skip the first line

			// read each subsequent line and add relevant data to a big array
			while ((oneLine = fileIn.readLine()) != null) {
				String[] tokens = oneLine.split(",");
				if(tokens.length != TOKENS_PER_LINE)
					throw new NumberFormatException();
				int population = Integer.parseInt(tokens[POPULATION_INDEX]);
				if(population != 0)
					result.add(population,
							Float.parseFloat(tokens[LATITUDE_INDEX]),
							Float.parseFloat(tokens[LONGITUDE_INDEX]));
			}

			fileIn.close();
		} catch(IOException ioe) {
			System.err.println("Error opening/reading/writing input or output file.");
			System.exit(1);
		} catch(NumberFormatException nfe) {
			System.err.println(nfe.toString());
			System.err.println("Error in file format");
			System.exit(1);
		}
		return result;
	}

	//Returns the total population and percentage of total population in 
	//the query area.
	public static Pair<Integer, Float> singleInteraction(int w, int s, int e,
			int n) {
		return processor.singleInteraction(w, s, e, n);
	}

	//Sets up the query processor based on argument
	public static void preprocess(String filename, int columns, int rows,
			int versionNum) {
		CensusData parsedData = parse(filename);
		if (versionNum == 1) {
			processor = new Version1(parsedData, columns, rows);
		} else if (versionNum == 2) {
			processor = new Version2(parsedData, columns, rows);
		}
	}


	// argument 1: file name for input data: pass this to parse
	// argument 2: number of x-dimension buckets
	// argument 3: number of y-dimension buckets
	// argument 4: -v1, -v2, -v3, -v4, or -v5
	public static void main(String[] args) {
		if (args.length != 4) {
			System.err.println("USAGE: --argument 1: file name for input data: pass this to parse \n "
					+ "--argument 2: number of x-dimension buckets "
					+ "--argument 3: number of y-dimension buckets "
					+ "--argument 4: -v1, -v2, -v3, -v4, or -v5");
			System.exit(1);
		}

		preprocess(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]),
				Integer.parseInt(Character.toString(args[3].charAt(2))));

		Scanner s = new Scanner(System.in);
		while (true) {
			System.out.println("Please give west, south, east, north coordinates of your query rectangle: ");

			// Get input
			String input = s.nextLine();

			// Check if of the form num num num num
			if (!input.matches("^\\d+\\s\\d+\\s\\d+\\s\\d+$")) {
				break;
			} 

			// Split input into constituent parts
			Scanner numSplitter = new Scanner(input);
			int west = numSplitter.nextInt();
			int south = numSplitter.nextInt();
			int east = numSplitter.nextInt();
			int north = numSplitter.nextInt();
			numSplitter.close();

			//If invalid input
			if (west < 1 || west > Integer.parseInt(args[1]) ||
					south < 1 || south > Integer.parseInt(args[2]) ||
					east < west || east > Integer.parseInt(args[1]) ||
					north < south || north > Integer.parseInt(args[2])) {
				System.out.println("fail");
				break;
			}

			// And finally, execute
			// 0 based indexing
			Pair<Integer, Float> results = singleInteraction(west - 1, south - 1, east - 1, north - 1);
			System.out.println("population of rectangle: " + results.getElementA());
			System.out.println("percent of total population: " + String.format("%.2f", results.getElementB()));
		}
	}
}
