/*
 *  Name: Roman Joska
 *  Class: CS4700
 *  Description: Make code that can make a DFA or NFA, and print output files for every machine and the final log
 */

//imports
import java.io.File;
import java.util.Scanner;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Math;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;

//note this code could be 100 lines if not more shorter, but I have some stuff in here im scared to delete / rework at this late in the lifecycle
public class CS4700PJ1 {

	public static void main(String[] args) throws IOException{
		
		//need to make input files and outFile for log
		BufferedWriter writer = new BufferedWriter(new FileWriter("fa.log"));
		
		//call machines while we have machines to make
		File machinesFolder = new File("machines/");
		File[] machineFiles = machinesFolder.listFiles();
		//go through each .fa file
		for (File machineFile : machineFiles) {
		    if (machineFile.isFile() && machineFile.getName().endsWith(".fa")) {
		    	//make the name and then make the machine
		        String machineName = machineFile.getName().replace(".fa", "");
		        //print to test
		        //System.out.printf("we got one called %s\n", machineName);
		        logFileType logOutput = processMachineFile(machineFile, machineName);
		        String outputString = machineName + "," + logOutput.getType() + "," + logOutput.getStates() + "," + logOutput.getAlpha() + "," + logOutput.getStrings() + "\n";
		        writer.write(outputString);
		    }
		}
		//close the log writer
		writer.close();
	}

//process machine file (DFA or NFA)
public static logFileType processMachineFile(File machineFile, String machineName) {
	//inputs needed
	int acceptSize = 0;
	int numStates = 0;
    int numAlphabet = 0;
    int acceptStrings = 0;
    String machineType = "DFA";
    boolean hasEpsi = false;
    boolean hasAccept = true;
	
    //get all the machine information and make the machine
	Map<Integer, Map<Character, Integer>> transitions = new HashMap<>();
	Set<inputs> machineInputs = new HashSet<>(); //added to try set route (both are needed)
    try (BufferedReader reader = new BufferedReader(new FileReader(machineFile))) {
    	//read the first line and then read the rest
    	String firstLine = reader.readLine();
        String acceptStatesStr = firstLine.substring(firstLine.indexOf("{") + 1, firstLine.indexOf("}"));
        String[] acceptStatesArray = acceptStatesStr.split(",");
        int[] acceptStates = new int[acceptStatesArray.length];
        //read the first line and convert to an array of ints
        for (int i = 0; i < acceptStatesArray.length; i++) {
        	if(acceptStatesStr.isEmpty()) {
        		i = acceptStatesArray.length;
        		hasAccept = false;
        	}
        	else {
        		acceptStates[i] = Integer.parseInt(acceptStatesArray[i].trim());
                //print to test
                //System.out.printf("Accept state #%d, is %d \n", i, acceptStates[i]);
        	}
        }
        
        //read the rest of the file
        String line;
        boolean zeroTo255 = true;
        while ((line = reader.readLine()) != null) {
            // process line
            String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            int currentState = Integer.parseInt(tokens[0].trim());
            String inputSymbol = tokens[1];
            int nextState = Integer.parseInt(tokens[2].trim());
            if(nextState < 0 | nextState >= 256 | currentState < 0 | currentState >= 256) {
            	zeroTo255 = false;
            }
            //added this set as it is easier to use but the map was kept to save time
            inputs input1 = new inputs(currentState,inputSymbol,nextState);
            machineInputs.add(input1);
            
            if (!transitions.containsKey(currentState)) {
                transitions.put(currentState, new HashMap<>());
            }
            //check to see if there is an epsilon transistion
            if(inputSymbol.isEmpty()) {
            	//System.out.printf("This machine is an NFA\n");
            	//this was part of the print test
            	transitions.get(currentState).put('	', nextState);
            	//System.out.printf("Transistion %d, 	, %d\n", currentState, nextState);
            	machineType = "NFA";
            	hasEpsi = true;
            }
            else {
            	transitions.get(currentState).put(inputSymbol.charAt(0), nextState);
                //print to test
                //System.out.printf("Transistion %d, %c, %d\n", currentState, inputSymbol.charAt(0), nextState);
            }

        }
        
        // Create a set to hold all unique states and alphabet
        Set<Character> alphabet = new HashSet<>();

        // Loop over all transitions and add the start and end states to the set
        for (Map<Character, Integer> innerMap : transitions.values()) {
        	alphabet.addAll(innerMap.keySet());
        }
        
        //make the sets and count how many states there are
        Set<Integer> states = new HashSet<>();
        for (Integer startState : transitions.keySet()) {
            Map<Character, Integer> innerMap = transitions.get(startState);
            states.add(startState);
            states.addAll(innerMap.values());
        }

        // The get number of states and alphabet
        numStates = states.size();
        //System.out.println("Machine numstates = " + states.size());
        numAlphabet = alphabet.size();
        if(hasEpsi) {
        	numAlphabet--;
        }
        acceptSize = acceptStatesArray.length;
        //System.out.printf("Number of states %d, alphabet %d, and accept states %d\n", numStates, numAlphabet, acceptSize);
        
        //looking for a duplicate value
        int output = 0;
        List<inputs> values = new ArrayList<>(machineInputs);
        for (int i = 0; i < values.size(); i++) {
        	inputs value1 = values.get(i);
            for (int j = i + 1; j < values.size(); j++) {
            	inputs value2 = values.get(j);
                output += value1.findDupe(value2);
            }
        }
        //classify it as an NFA if it is a dupe
        if(output > 0) {
        	//System.out.println("We have a dupe");
        	machineType = "NFA";
        }
        //this is a check for printing characters
        int printable = 0;
        for (inputs value : machineInputs) {
            	printable += value.printChars();
        }
        //checks for invalid
        if(printable > 0) {
        	System.out.println("It has a non-printable");
        	machineType = "INV";
        }
        
        if(!zeroTo255) {
        	System.out.println("Inv machine");
        	machineType = "INV";
        }
        //put 246 as an accept state if the machine has no accept states, this is after the INV checks so it will not break
        if(!hasAccept) {
        	for(int i = 0; i < acceptStates.length; i++) {
        		acceptStates[i] = 256;
        		acceptSize--;
        	}
        }
        
        //Make the machine
        if(!machineType.equals("INV")) {
        	machine thisOne = new machine(acceptStates, numStates, numAlphabet, acceptSize, machineName, machineType, machineInputs);
            acceptStrings = thisOne.readStrings();
            System.out.println("Num accepted by " + machineName + " is " + acceptStrings + " " + machineType);
        }
        
    } 
    catch (IOException e) {
        e.printStackTrace();
    }
    
    //make the log file return type note that this could of just been a machine type, but I was struggling for too long and made this and it works
    logFileType returnOne = new logFileType(machineType, numStates, numAlphabet, acceptStrings);
    if(machineType.equals("INV")) {//invalid is made here
    	returnOne = new logFileType(machineType, -1, -1, -1);
    }
    return returnOne;
    
    }

}




//classes
class machine{
	private int[] acceptStates;
	private int numStates;
	private int numAlphbet;
	private int numAccept;
	private String machineName;
	private String machineType;
	private Set<inputs> tranistions;
	
	//constructor
	public machine(int[] accept, int states, int alphabet, int acceptNum, String name, String type, Set<inputs> information) {
		acceptStates = accept;
		numStates = states;
		numAlphbet = alphabet;
		numAccept = acceptNum;
		machineName = name;
		machineType = type;	
		tranistions = information;
	}
	//read input and make output functions
	public int readStrings() {
		int numAccepted = 0;
		//start reading the files and make the output folder
		String directoryPath = "./outputs";
		File directory = new File(directoryPath);
		directory.mkdirs(); // create the directory if it doesn't exist
		String fileName = machineName + ".txt";
    	File outputFile = new File(directory, fileName);
    	
       /* for (int i = 0; i < acceptStates.length; i++) {
            //print to test
            System.out.printf("Accept state #%d, is %d \n", i, acceptStates[i]);
        }*/
		//make the machine if it is a DFA
		if(machineType.equals("DFA")) {
		try (BufferedReader br = new BufferedReader(new FileReader("machines/strings.txt"))) {
            String line;
            int currentState = 0;
        	try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            while ((line = br.readLine()) != null) {
                // loop through each character in the line
            	currentState = 0;
            	//System.out.printf("reset\n");
                for (int i = 0; i < line.length(); i++) {
                    char nextInput = line.charAt(i);
                    boolean found = false;
                    // check current state and then do the transition also make sure you only take 1 tranistion per character
                    boolean notDidOneAlready = true;
                    for (inputs input : tranistions) {
                        if (input.getStart() == currentState && input.getChar().charAt(0) == nextInput && notDidOneAlready) {
                        	//System.out.printf("Current state %d, transition %c to state %d on i = %d\n", currentState, nextInput, input.getEnd(), i);
                        	currentState = input.getEnd();
                        	found = true;
                        	notDidOneAlready = false;
                        }
                    }//send to trap state and kill it if the input is not in the alphabet
                    if(!found) {
                    	i = line.length();
                    	currentState = 255;
                    	//System.out.printf("trap card\n");
                    }
                }// end of for
                
                //System.out.printf("Current state #%d\n", currentState);
                
                //see if in an accept state
                boolean accept = false;
                for (int i = 0; i < acceptStates.length; i++) {
                    if (acceptStates[i] == currentState) {
                    	accept = true;
                    }
                }
                //if it is copy the string
                if (accept) {
                	String copyStrng = line + "\n";
                	writer.write(copyStrng);
                	numAccepted += 1;
                }  
                
            }//end of while
            
        	} catch (IOException e) {
        	    e.printStackTrace();
        	}//end of write
        	
        } catch (IOException e) {//error line just in case
            System.err.println("Error reading file: " + e.getMessage());
        }
		}//end of the if DFA now code for NFA that is similar
		else if(machineType.equals("NFA")){
			//read through each string
			try (BufferedReader br = new BufferedReader(new FileReader("machines/strings.txt"))) {
	            String line;
	            Set<Integer> currentState = new HashSet<>();
	            Set<Integer> nextStates = new HashSet<>();
	            currentState.add(0);
	        	try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
	            while ((line = br.readLine()) != null) {
	                // loop through each character in the line
	            	nextStates.clear();
	            	currentState.clear();
	            	currentState.add(0);
	            	//System.out.printf("reset\n");
	                //added to catch empty NFA
	                if(line.isEmpty()) {
	                	nextStates.add(0);
	                }
	                for (int i = 0; i < line.length(); i++) {
	                    char nextInput = line.charAt(i);
	                    nextStates.clear();
		                
	                    //first we take any and all epsilons
	                    for (inputs input : tranistions) {
	                    	if(currentState.contains(input.getStart()) && input.getChar().isEmpty()) {
	                    		currentState.add(input.getEnd());
	                    	}
	                    }
	                    //this handles for another epsilon transition and then anything for the character
	                    boolean found = false;
	                    for (inputs input : tranistions) {
	                    	if(currentState.contains(input.getStart()) && input.getChar().isEmpty()) {
	                    		currentState.add(input.getEnd());
	                    	}
	                    	else if (currentState.contains(input.getStart()) && input.getChar().charAt(0) == nextInput) {
	                        	//System.out.printf("Current state %d, transition %c to state %d on i = %d\n", input.getStart(), nextInput, input.getEnd(), i);
	                        	nextStates.add(input.getEnd());
	                        	found = true;
	                        	//System.out.println("Added to " + input.getEnd() + " on " + input.getStart() + " for " + input.getChar().charAt(0));
	                        }
	                    }//get any epsilons at the end	                    
	                    if(!found) {
	                    	nextStates.add(255);
	                    	//System.out.printf("trap card\n");
	                    }
	                    /*
	                    for (int element : currentState) {
	                        System.out.println("Before Clear " + element);
	                    }
	                    for (int element : nextStates) {
	                        System.out.println("Next Before Clear " + element);
	                    }*/
	                    //check for last epsilons
	                    
	                    //catching epsilons at the end
	                    int j = line.length();
	                    j--;
	                    
	                    if(i == j) {
	                    	 for (inputs input : tranistions) {
	 	                    	if(nextStates.contains(input.getStart()) && input.getChar().isEmpty()) {
	 	                    		nextStates.add(input.getEnd());
	 	                    	}
	 	                    }
	                    }
	                    
	                    //move the next states into current and clear current before to allow for proper NFA behavior
	                    currentState.clear();
	                    currentState.addAll(nextStates);
	                }// end of for
	                
	                //System.out.printf("Current state #%d\n", currentState);
	                
	                //do epsilon on a NFA empty string
	                if(line.isEmpty()) {
	                	for (inputs input : tranistions) {
 	                    	if(nextStates.contains(input.getStart()) && input.getChar().isEmpty()) {
 	                    		nextStates.add(input.getEnd());
 	                    	}
 	                    }
	                }
	                
	                //see if in an accept state
	                boolean accept = false;
	                for (int i = 0; i < acceptStates.length; i++) {
	                    if (nextStates.contains(acceptStates[i])) {
	                    	accept = true;
	                    }
	               	}//end of i for
	                //check to see if accepted
	                if (accept) {
	                	String copyStrng = line + "\n";
	                	writer.write(copyStrng);
	                	numAccepted += 1;
	                }  
	                
	            }//end of while
	            
	        	} catch (IOException e) {
	        	    e.printStackTrace();
	        	}//end of write
		} catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
		
	}
	else {
		numAccepted = -1;
		System.out.printf("An error occuered neither DFA or NFA\n");
	}
		return numAccepted;
	}// end of read strings
}//end of machine class

class inputs{
	private int startS;
	private String transChar;
	private int endS;
	
	//constructor
	public inputs(int startState, String transistion, int endState) {
		startS = startState;
		transChar = transistion;
		endS = endState;
	}
	
	//getters
	public int getStart() {
		return startS;
	}
	public int getEnd() {
		return endS;
	}
	public String getChar() {
		return transChar;
	}
	
	//find dupes
	public int findDupe(inputs machine) {
		int returnInt = 0;
        	if(machine.getStart() == startS) {
        		if(machine.getChar().isEmpty()) {
        			returnInt += 0;
        		}
        		else if(transChar.isEmpty()) {
        			returnInt += 0;
        		}
        		else {//only check the character if one is there
        			if(machine.getChar().charAt(0) == transChar.charAt(0)) {
        				returnInt += 1;
        				//System.out.println("Dupe on state " + startS + " with transistion " + transChar.charAt(0));
        			}
        		}
        	}
        	else {
        		returnInt += 0;
        	}
        	
		return returnInt;
	}
	
	//check if char == valid
	public int printChars() {
		if(transChar.isEmpty()) {
			return 0;
		}
		else {
			boolean yesNo = !Character.isISOControl(transChar.charAt(0)) && transChar.charAt(0) != KeyEvent.CHAR_UNDEFINED;
			if(yesNo == true) {
				return 0;
			}
			else {
				return 1;
			}
		}
	}
	
}

class logFileType {
	private String type;
	private int states;
	private int alphabet;
	private int strings;
	
	//constructor
	public logFileType(String machineType, int numStates, int numAlpha, int numStrings) {
		type = machineType;
		states = numStates;
		alphabet = numAlpha;
		strings = numStrings;
	}
	
	//getters
	public int getStrings() {
		return strings;
	}
	public int getAlpha() {
		return alphabet;
	}
	public int getStates() {
		return states;
	}
	public String getType() {
		return type;
	}
	
}