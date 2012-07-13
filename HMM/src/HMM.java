import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;

/**
 * Class: HMM
 * 
 * Class definition for the HMM to generate random states, events and find the
 * maximum probable path.
 *
 * Viterbi algorithm adopted from: Biological Sequence Analysis, Durbin et.al.
 * and Wikipedia: http://en.wikipedia.org/wiki/Viterbi_algorithm
 * @author gopi
 * 
 */
public class HMM {

	// Input event sequences. Also randomly generate sequences.
	static ArrayList<String> inputEvents = new ArrayList<String>();
	// Most probable output sequence of states.
	static ArrayList<String> outputStates = new ArrayList<String>();
	// Randomly generated state sequence.
	static ArrayList<String> randomStates = new ArrayList<String>();

	// Flag to use the user input or generate random events, states.
	static boolean useInputEvents = false;

	// All the state symbols.
	static String[] states = null;
	// All the event symbols.
	static String[] events = null;
	// All the transition probabilities
	static LinkedHashMap<String, LinkedHashMap<String, Double>> transitionProbs = new LinkedHashMap<String, LinkedHashMap<String, Double>>();
	// All the emission probabilities
	static LinkedHashMap<String, LinkedHashMap<String, Double>> emissionProbs = new LinkedHashMap<String, LinkedHashMap<String, Double>>();

	/*
	 * Method: main
	 * 
	 * Execution begins here...
	 */
	public static void main(String args[]) throws IOException {
		// Read the run.conf file.
		readConfig();
		// If the user chose to use a random sequence, generate one using HMM.
		if (!useInputEvents)
			generateSequence();
		// Calculate the most probable path using the Viterbi algorithm.
		viterbiPath(inputEvents.toArray(), states, transitionProbs.get("B"),
				transitionProbs, emissionProbs);

		// Print the most probable state sequence.
		printResults();
		System.out.println("DONE! Output written to 'output.txt' file.");
	}

	/*
	 * Method: printResults
	 * 
	 * Method to print the input sequences and output sequences.
	 */
	private static void printResults() throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter("output.txt"));
		if (!useInputEvents) {
			bw.write("Randomly Generated Events Sequence:\n");
			for (int index = 0; index < inputEvents.size(); index++) {
				bw.write(inputEvents.get(index));
			}
			bw.write("\n\nRandomly Generated State Sequence (Corresponding To The Above Random Events Sequence):\n");
			for (int index = 0; index < randomStates.size(); index++) {
				bw.write(randomStates.get(index));
			}
		} else {
			bw.write("\nUser Provided Events Sequence:\n");
			for (int index = 0; index < inputEvents.size(); index++) {
				bw.write(inputEvents.get(index));
			}
		}
		bw.write("\n\nFinal Predictions (Input Events -> Corresponding Highest Probable Sequence of States):\n");
		for (int index = 0; index < inputEvents.size(); index++) {
			bw.write("\n" + inputEvents.get(index) + "\t->\t"
					+ outputStates.get(index));
		}
		bw.flush();
		bw.close();
	}

	/*
	 * Method: generateSequence
	 * 
	 * Generates a random sequence of events using the HMM (transition and
	 * emission probabilities).
	 */
	private static void generateSequence() {
		// Clear the user input, as a random sequence will be used.
		inputEvents.clear();
		// Begin state.
		String newState = "B";
		// Random number to traverse the HMM.
		int randomIndex = 0;
		// Loop till the HMM reaches End state
		while (!newState.equals("E")) {
			// Get transition probabilities and state symbols.
			Object[] tkeys = transitionProbs.get(newState).keySet().toArray();
			Object[] tvalues = transitionProbs.get(newState).values().toArray();
			// Create the range using probabilities
			for (int index = 1; index < tvalues.length; index++) {
				tvalues[index] = ((Double) tvalues[index - 1])
						+ ((Double) tvalues[index]);
			}
			// Generate a random number that will be used to select the state.
			randomIndex = getRandomNumber();

			// Check random number and the range it falls into.
			for (int index = 0; index < tvalues.length; index++) {
				// Check the range, if the random number falls in the range,
				// choose the representing state.
				if (randomIndex < (Double) tvalues[index] * 100) {
					// Select the State
					newState = (String) tkeys[index];
					if (!newState.endsWith("E")) {
						randomStates.add(newState);
						// For the selected State, generate random event.
						getRandomEvent(newState);
					}
					break;
				}
			}
		}
	}

	/*
	 * Method: getRandomEvent
	 * 
	 * Generates a random event for a given state.
	 */
	private static void getRandomEvent(String newState) {
		// If the state is End, nothing to do.
		if (newState.equals("E"))
			return;

		// Random number
		int randomIndex = 0;
		// Get emission probabilities.
		Object[] ekeys = emissionProbs.get(newState).keySet().toArray();
		Object[] evalues = emissionProbs.get(newState).values().toArray();
		// Calculate the ranges.
		for (int index = 1; index < evalues.length; index++) {
			evalues[index] = ((Double) evalues[index - 1])
					+ ((Double) evalues[index]);
		}
		// Get the random number.
		randomIndex = getRandomNumber();
		// Check the range the random number falls into and obtain the random
		// event.
		for (int index = 0; index < evalues.length; index++) {
			if (randomIndex < (Double) evalues[index] * 100) {
				newState = (String) ekeys[index];
				// Store the random event.
				inputEvents.add(newState);
				break;
			}
		}
	}

	/*
	 * Method: getRandomNumber
	 * 
	 * Generates a random number range 0 inclusive to 100 exclusive, replicating
	 * the probability of 0 to 1.
	 */
	private static int getRandomNumber() {
		Random rand = new Random();
		return rand.nextInt(100);
	}

	/*
	 * Method: readConfig
	 * 
	 * Reads 'run.config' configuration file.
	 */
	private static void readConfig() {
		try {
			BufferedReader br = new BufferedReader(new FileReader("run.config"));
			String line = "";
			states = null;
			while ((line = br.readLine()) != null) {
				// Read transition probabilities.
				if (line.startsWith("#transition")) {
					states = br.readLine().split(" ");

					for (int index = 0; index < states.length; index++) {
						LinkedHashMap<String, Double> tempTrans = new LinkedHashMap<String, Double>();
						String[] tProbs = br.readLine().split(" ");
						for (int index2 = 1; index2 < tProbs.length; index2++) {
							tempTrans.put(states[index2 - 1],
									Double.parseDouble(tProbs[index2]));
						}
						transitionProbs.put(states[index], tempTrans);
					}
				} else {
					System.out
							.println("Illegal 'run.config' file, please re-check the format.");
				}

				// Read emission probabilities.
				if (br.readLine().startsWith("#emission")) {
					events = br.readLine().split(" ");
					for (int index = 1; index < states.length - 1; index++) {
						LinkedHashMap<String, Double> tempTrans = new LinkedHashMap<String, Double>();
						String[] eProbs = br.readLine().split(" ");
						for (int index2 = 1; index2 < eProbs.length; index2++) {
							tempTrans.put(events[index2 - 1],
									Double.parseDouble(eProbs[index2]));
						}
						emissionProbs.put(states[index], tempTrans);
					}
				} else {
					System.out
							.println("Illegal 'run.config' file, please re-check the format.");
				}

				// Read user input events sequence.
				if ((line = br.readLine()).startsWith("input-sequence")) {
					String inputEventSeq[] = line.split(":");
					String str = inputEventSeq[1];
					for (int index = 0; index < str.length(); index++)
						inputEvents.add(str.substring(index, index + 1));
				} else {
					System.out
							.println("Illegal 'run.config' file, please re-check the format.");
				}

				// Read the flag, whether to use the user input sequence or not.
				if ((line = br.readLine()).startsWith("use-input-sequence")) {
					String useInputEventSeq[] = line.split(":");
					if (useInputEventSeq[1].trim().startsWith("y")
							|| useInputEventSeq[1].trim().startsWith("Y"))
						useInputEvents = true;
				} else {
					System.out
							.println("Illegal 'run.config' file, please re-check the format.");
				}

				br.close();
				return;
			}
		} catch (Exception e) {
			System.out.println("Problem using, 'run.config' file!");
		}
	}

	/*
	 * Method: viterbiPath
	 * 
	 * Calculates the most probable state sequence for a given events sequence
	 * using the Viterbi algorithm.
	 */
	public static void viterbiPath(
			Object[] eventsObs,
			Object[] states,
			LinkedHashMap<String, Double> beginProbs,
			LinkedHashMap<String, LinkedHashMap<String, Double>> transitionProbs,
			LinkedHashMap<String, LinkedHashMap<String, Double>> emissionProbs) {

		// Create the initial setup, using the starting probabilities.
		LinkedHashMap<String, Object[]> initial = new LinkedHashMap<String, Object[]>();
		for (Object state : states) {
			String temp = ((String) state).trim();
			initial.put(temp, new Object[] { beginProbs.get(temp), temp,
					beginProbs.get(temp) });
		}

		// Viterbi algorithm, step to calculate the most probable path.
		for (Object output : eventsObs) {
			String tempOutput = ((String) output).trim();
			LinkedHashMap<String, Object[]> mostProbable = new LinkedHashMap<String, Object[]>();
			for (Object nextState : states) {
				String tempState = ((String) nextState).trim();
				double total = 0.0, maxValue = 0.0, prob = 1, probViterbi = 1;
				String maxSeq = "", pathViterbi = "";
				for (Object source : states) {
					String srcState = ((String) source).trim();
					// Cannot go back to Begin, cannot exit End state.
					if (srcState.equals("B") || srcState.equals("E"))
						continue;
					if (tempOutput.equals("B") || tempOutput.equals("E"))
						continue;
					if (tempState.equals("B") || tempState.equals("E"))
						continue;

					// System.out.println("**" + srcState + " " + tempOutput +
					// " " + tempState);
					Object[] objs = initial.get((String) source);
					prob = ((Double) objs[0]).doubleValue();
					pathViterbi = (String) objs[1];
					probViterbi = ((Double) objs[2]).doubleValue();

					double p = (emissionProbs.get(srcState).get(tempOutput))
							* (transitionProbs.get(srcState).get(tempState));
					prob *= p;
					probViterbi *= p;
					total += prob;
					if (probViterbi > maxValue) {
						maxSeq = pathViterbi + "," + nextState;
						maxValue = probViterbi;
					}
				}
				mostProbable.put((String) nextState, new Object[] { total,
						maxSeq, maxValue });
			}
			initial = mostProbable;
		}

		// Check for the best path.
		String maxSeq = "", pathViterbi = "";
		double maxValue = 0.0, probViterbi = 0.0;
		for (Object state : states) {
			Object[] objs = initial.get((String) state);
			pathViterbi = (String) objs[1];
			probViterbi = ((Double) objs[2]).doubleValue();
			if (probViterbi > maxValue) {
				maxSeq = pathViterbi;
				maxValue = probViterbi;
			}
		}
		String argmaxVals[] = maxSeq.split(",");
		for (int index = 0; index < argmaxVals.length - 1; index++)
			outputStates.add(argmaxVals[index]);
	}
}
