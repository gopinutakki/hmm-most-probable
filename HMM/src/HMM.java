import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;

public class HMM {

	static ArrayList<String> inputEvents = new ArrayList<String>();
	static ArrayList<String> outputStates = new ArrayList<String>();

	static ArrayList<String> randomStates = new ArrayList<String>();
	static ArrayList<String> predictedStates = new ArrayList<String>();

	static boolean useInputEvents = false;

	static String[] states = null;
	static String[] events = null;
	static LinkedHashMap<String, LinkedHashMap<String, Double>> transitionProbs = new LinkedHashMap<String, LinkedHashMap<String, Double>>();
	static LinkedHashMap<String, LinkedHashMap<String, Double>> emissionProbs = new LinkedHashMap<String, LinkedHashMap<String, Double>>();

	public static void main(String args[]) throws IOException {
		readConfig();
		if (!useInputEvents)
			getRandomState();
		// viterbi(inputEvents.toArray(), randomStates.toArray(),
		// transitionProbs.get("B"), transitionProbs, emissionProbs);
		viterbi(inputEvents.toArray(), states, transitionProbs.get("B"),
				transitionProbs, emissionProbs);

		printResults();
	}

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
		bw.write("\n\nFinal Predictions (Input Events & Corresponding Highest Probable Sequence of States):\n");
		for (int index = 0; index < inputEvents.size(); index++) {
			bw.write("\n" + inputEvents.get(index) + "\t->\t"
					+ outputStates.get(index));
		}
		bw.flush();
		bw.close();
	}

	private static void getRandomState() {
		inputEvents.clear();
		String newState = "B";
		int randomIndex = 0;
		while (!newState.equals("E")) {
			Object[] tkeys = transitionProbs.get(newState).keySet().toArray();
			Object[] tvalues = transitionProbs.get(newState).values().toArray();
			for (int index = 1; index < tvalues.length; index++) {
				tvalues[index] = ((Double) tvalues[index - 1])
						+ ((Double) tvalues[index]);
				// System.out.println(tvalues[index]);
			}
			randomIndex = getRandomNumber();

			for (int index = 0; index < tvalues.length; index++) {
				if (randomIndex < (Double) tvalues[index] * 100) {
					newState = (String) tkeys[index];
					if (!newState.endsWith("E")) {
						randomStates.add(newState);
						getRandomEvent(newState);
					}
					break;
				}
			}
		}
	}

	private static void getRandomEvent(String newState) {
		if (newState.equals("E"))
			return;

		int randomIndex = 0;
		Object[] ekeys = emissionProbs.get(newState).keySet().toArray();
		Object[] evalues = emissionProbs.get(newState).values().toArray();
		for (int index = 1; index < evalues.length; index++) {
			evalues[index] = ((Double) evalues[index - 1])
					+ ((Double) evalues[index]);
			// System.out.println(evalues[index]);
		}
		randomIndex = getRandomNumber();
		for (int index = 0; index < evalues.length; index++) {
			if (randomIndex < (Double) evalues[index] * 100) {
				newState = (String) ekeys[index];
				inputEvents.add(newState);
				break;
			}
		}
	}

	private static int getRandomNumber() {
		Random rand = new Random();
		return rand.nextInt(100);
	}

	private static void readConfig() {
		try {
			BufferedReader br = new BufferedReader(new FileReader("run.config"));
			String line = "";
			states = null;
			while ((line = br.readLine()) != null) {
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

				if ((line = br.readLine()).startsWith("input-sequence")) {
					String inputEventSeq[] = line.split(":");
					String str = inputEventSeq[1];
					for (int index = 0; index < str.length(); index++)
						inputEvents.add(str.substring(index, index + 1));
				} else {
					System.out
							.println("Illegal 'run.config' file, please re-check the format.");
				}

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

	public static void viterbi(Object[] obs, Object[] states,
			LinkedHashMap<String, Double> start_p,
			LinkedHashMap<String, LinkedHashMap<String, Double>> trans_p,
			LinkedHashMap<String, LinkedHashMap<String, Double>> emit_p) {

		LinkedHashMap<String, Object[]> T = new LinkedHashMap<String, Object[]>();
		for (Object state : states) {
			String temp = ((String) state).trim();
			T.put(temp,
					new Object[] { start_p.get(temp), temp, start_p.get(temp) });
		}

		for (Object output : obs) {
			String tempOutput = ((String) output).trim();
			LinkedHashMap<String, Object[]> U = new LinkedHashMap<String, Object[]>();
			for (Object next_state : states) {
				String tempState = ((String) next_state).trim();
				double total = 0;
				String argmax = "";
				double valmax = 0;

				double prob = 1;
				String v_path = "";
				double v_prob = 1;

				for (Object source_state : states) {
					String srcState = ((String) source_state).trim();
					if (srcState.equals("B") || srcState.equals("E"))
						continue;
					if (tempOutput.equals("B") || tempOutput.equals("E"))
						continue;
					if (tempState.equals("B") || tempState.equals("E"))
						continue;

					// System.out.println("**" + srcState + " " + tempOutput +
					// " " + tempState);
					Object[] objs = T.get((String) source_state);
					prob = ((Double) objs[0]).doubleValue();
					v_path = (String) objs[1];
					v_prob = ((Double) objs[2]).doubleValue();

					double p = (emit_p.get(srcState).get(tempOutput))
							* (trans_p.get(srcState).get(tempState));
					prob *= p;
					v_prob *= p;
					total += prob;
					if (v_prob > valmax) {
						argmax = v_path + "," + next_state;
						valmax = v_prob;
					}
				}
				U.put((String) next_state,
						new Object[] { total, argmax, valmax });
			}
			T = U;
		}

		String argmax = "";
		double valmax = 0;

		String v_path;
		double v_prob;

		for (Object state : states) {
			Object[] objs = T.get((String) state);
			v_path = (String) objs[1];
			v_prob = ((Double) objs[2]).doubleValue();
			if (v_prob > valmax) {
				argmax = v_path;
				valmax = v_prob;
			}
		}
		String argmaxVals[] = argmax.split(",");
		for (int index = 0; index < argmaxVals.length - 1; index++)
			outputStates.add(argmaxVals[index]);
	}
}
