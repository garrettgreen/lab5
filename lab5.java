// Logan Schwarz
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.List;
import java.text.DecimalFormat;

public class lab5 {

    public static int[] datamemory = new int[8192];
    public static int pc = 0;
    public static int totalPredictions = 0;
    public static int correctPredictions = 0;
    public static int[] GHR = new int[8];
    public static int[] countArray = new int[256];
   // Map<Integer, String> stringMap = new HashMap<>();

    // This function finds and maps labels in the code 
    public static HashMap<String, Integer> mapLabels(String fname) {

        // Initialize
        File infile = new File(fname);
        if (!infile.isFile()) {
            System.out.println(fname + " is not a file!");
            return null;
        }
    
        HashMap<String, Integer> labelMap = new HashMap<>();
        int lineCount = 0;
    
        // First pass to find labels, save name and line number into table
        try {
            Scanner scanner = new Scanner(infile);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim(); // Remove whitespace
    
                if (line.startsWith("#")) {
                    continue;
                }
    
                String[] parts = line.split(":"); // Split line by colon
    
                if (parts.length == 2) { // Line contains both label and instruction
                    String label = parts[0].trim();
                    labelMap.put(label, lineCount);
                    line = parts[1].trim();
                }
    
                int commentIndex = line.indexOf("#");
                if (commentIndex >= 0) {
                    line = line.substring(0, commentIndex).trim(); // Remove any text after the comment
                }
    
                if (!line.isEmpty()) { // Check if line is not empty
                    lineCount++;
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error.");
            e.printStackTrace();
        }
    
        return labelMap;
    }
    
    // This function converts asm file into a usable array of asm lines
    public static String[] readASM(String fname){
        // Initialize
        File infile = new File(fname);
        if (!infile.isFile()) {
            System.out.println(fname + " is not a file!");
            return null;
        }
        StringBuilder output = new StringBuilder();
        
        // second pass 
        try {
            Scanner scanner = new Scanner(infile); 

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (line.startsWith("#")){
                    continue;
                }

                boolean inlinecomment = false;
                boolean leadingwhitespace = true;

                int colonIndex = line.indexOf(":");
                if (colonIndex >= 0) {
                    // Skip the label in the code
                    line = line.substring(colonIndex + 1).trim();
                }
                // If the line is a comment, dont include
                for (int i = 0; i < line.length(); i++){
                    char c = line.charAt(i);
                    if (c == '#') {
                        inlinecomment = true;
                    }
                    if (Character.isWhitespace(c)) {
                        if (leadingwhitespace){
                            // ignore whitespace
                        }
                        else {
                            output.append(' ');
                            leadingwhitespace = true;
                        }
                    } else if (c == ',' || c == '(' || c == ')') {
                        output.append(' ');
                    } else if (c == '$' && !inlinecomment) {
                            output.append(' ');
                            output.append(c);
                            leadingwhitespace = false;
                    } else if (!inlinecomment) {
                        output.append(c);
                        leadingwhitespace = false;
                    }
                }
                // Newline
                output.append('\n');
            }
            scanner.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("Error.");
            e.printStackTrace();
        }

        // building and prepping the output array
        String[] outputArray = output.toString().split("\\r?\\n");
        outputArray = Arrays.stream(outputArray).filter(s -> !s.trim().isEmpty()).toArray(String[]::new);
        return outputArray;
    }

    // This function initializes our registers hashmap
    public static HashMap<String, Integer> createRegistersMap(){
        HashMap<String, Integer> registers = new HashMap<String, Integer>();

        registers.put("$0", 0);
        registers.put("$v0", 0);
        registers.put("$v1", 0);
        registers.put("$a0", 0);
        registers.put("$a1", 0);
        registers.put("$a2", 0);
        registers.put("$a3", 0);
        registers.put("$t0", 0);
        registers.put("$t1", 0);
        registers.put("$t2", 0);
        registers.put("$t3", 0);
        registers.put("$t4", 0);
        registers.put("$t5", 0);
        registers.put("$t6", 0);
        registers.put("$t7", 0);
        registers.put("$s0", 0);
        registers.put("$s1", 0);
        registers.put("$s2", 0);
        registers.put("$s3", 0);
        registers.put("$s4", 0);
        registers.put("$s5", 0);
        registers.put("$s6", 0);
        registers.put("$s7", 0);
        registers.put("$t8", 0);
        registers.put("$t9", 0);
        registers.put("$sp", 0);
        registers.put("$ra", 0);  
        
        return registers;
    }

    public static HashMap<String, Integer> createCounterStringMap(){
        HashMap<String, Integer> stringMap = new HashMap<String, Integer>();

        // Loop to generate all possible 8-bit binary strings and their values
        for (int i = 0; i < 256; i++) {
            String binaryString = String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');
            stringMap.put(binaryString, i);
        }

        return stringMap;
    }
    
    public static void main(String[] args) throws FileNotFoundException, IOException{

        //System.out.println(args.length);

        String[] asmarray = readASM(args[0]);

        /*
        for (String line : asmarray){
            System.out.println(line);
        }
        */

        HashMap<String, Integer> labelMap = mapLabels(args[0]);

        /* 
        System.out.println("Label Table:");
        for (Entry<String, Integer> entry : labelMap.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
        */

        HashMap<String, Integer> registers = createRegistersMap();
        HashMap<String, Integer> stringMap = createCounterStringMap();

        // Print the stringMap
        for (HashMap.Entry<String, Integer> entry : stringMap.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        for (int i = 0; i < GHR.length; i++) {
            GHR[i] = 0;  // Setting each element to 0
        }
        for (int i = 0; i < countArray.length; i++) {
            countArray[i] = 0;  // Setting each element to 0
        }
        

        // registers are null going into this 
        if (args.length == 2 && (args[1].equals("2") || args[1].equals("4") || args[1].equals("8"))) {
            // interactive
            int bitCount = Integer.parseInt(args[1]);
            Scanner scanner = new Scanner(System.in);
            System.out.println(bitCount);
            if (!(bitCount == 2 || bitCount == 4 || bitCount == 8)){
                System.out.println("Invalid GHR bit count.");
                System.exit(0);
            }

            while (true) {
                System.out.print("mips> ");
                String input = scanner.nextLine();
                String[] userIn = input.split(" ");
                
                if (userIn.length == 1){
                    if (userIn[0].equals("h")){
                        // help
                        System.out.println("List of commands:\n" +
                                           "h = show help\n" + 
                                           "d = dump register state\n" +
                                           "s = single step through the program (i.e. execute 1 instruction and stop) s num = step through num instructions of the program\n" +
                                           "r = run until the program ends\n" +
                                           "m num1 num2 = display data memory from location num1 to num2\n" +
                                           "c = clear all registers, memory, and the program counter to 0\n" +
                                           "q = exit the program");
                    }
                    else if (userIn[0].equals("d")) {
                        // dump registers
                        System.out.println("pc = " + pc);
                    
                        // Create a list of register names in the desired order
                        List<String> registerNames = new ArrayList<>(Arrays.asList(
                             "$v0", "$v1", "$a0", "$a1", "$a2", "$a3", "$t0", "$t1", "$t2", "$t3",
                            "$t4", "$t5", "$t6", "$t7", "$s0", "$s1", "$s2", "$s3", "$s4", "$s5",
                            "$s6", "$s7", "$t8", "$t9", "$sp", "$ra"
                        ));
                    
                        int count = 0;
                        for (String registerName : registerNames) {
                            System.out.printf("%-4s = %-3d    ", registerName, registers.get(registerName));
                            count++;
                            if (count % 4 == 0) {
                                System.out.println();
                            }
                        }
                        System.out.println();
                    }
                    
                    else if (userIn[0].equals("s")){
                        if (pc < asmarray.length){
                            instrOp operation = new instrOp(asmarray[pc], labelMap, registers, stringMap, bitCount);
                            System.out.println("The instruction about to be executed is: " + asmarray[pc]);

                            registers = operation.execute_instruction();
                            
                            // single step through instructions
                            pc++;
                            System.out.println("1 instruction completed.");
                        }
                        else {
                            System.out.println("Program Completed. Please clear.");
                        }
                    }
                    else if (userIn[0].equals("r")){
                        // run whole program
                        int count = 0;
                        while (pc < asmarray.length) {
                            instrOp operation = new instrOp(asmarray[pc], labelMap, registers, stringMap, bitCount);

                            registers = operation.execute_instruction();
                            pc++;
                            count++;
                        }
                        System.out.println("Program Completed. Please clear.");
                        System.out.println(count + " instruction(s) completed.");            
                    }

                    else if (userIn[0].equals("c")){
                        // clear registers, memory (pc == 0)
                        for (Entry<String, Integer> entry : registers.entrySet()) {
                            entry.setValue(0);
                        }
                        for (int i = 0; i < datamemory.length; i++) {
                            datamemory[i] = 0;
                        }
                        pc = 0;
                    }
                    else if (userIn[0].equals("q")){
                        // exit program 
                        System.exit(0);
                    }
                    else if (userIn[0].equals("b")){
                        double battingPercentage = (float)correctPredictions/(float)totalPredictions;
                        battingPercentage = battingPercentage * 100;
                        DecimalFormat df = new DecimalFormat("##.##");
                        String fbattingPercentage = df.format(battingPercentage);
                        System.out.println(fbattingPercentage + "% (" + correctPredictions + " correct Predictions, " + totalPredictions + " predictions)");
                    }

                } else if (userIn.length == 2 && userIn[0].equals("s")){
                    // code for s num
                    int count = 0;
                    while (count < Integer.parseInt(userIn[1])){
                        if (pc < asmarray.length){ 
                            instrOp operation = new instrOp(asmarray[pc], labelMap, registers, stringMap, bitCount);
                            registers = operation.execute_instruction();
                            pc++;
                            count++;
                        }
                        else {
                            System.out.println("Reached the end of program. Please clear.");
                            count = Integer.parseInt(userIn[1]);
                        }       
                    }
                    System.out.println(userIn[1] + " instruction(s) completed.");
                } else if (userIn.length == 3 && userIn[0].equals("m")){
                    // code for m num1 num2
                    int lower = Integer.parseInt(userIn[1]);
                    int upper = Integer.parseInt(userIn[2]);
                    for (int i = lower; i <= upper; i++) {
                        System.out.println("[" + i + "] = " + datamemory[i]);
                    }
                }
            }
        } else if (args.length == 2) {
                // script
                String filename = args[1];
                File script = new File(filename);
    
                if (!script.isFile() || !script.exists()){
                    System.out.print("Unable to open given file\n");
                    return;
                }
                ArrayList<ArrayList<String>> lines = new ArrayList<>(); 
                
                int bitCount = 2;
                
                try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] splitLine = line.split(" "); 
                        ArrayList<String> words = new ArrayList<>(); // initialize ArrayList to hold words
                        for (String word : splitLine) {
                            words.add(word); // add each word to ArrayList
                        }
                        lines.add(words); // add ArrayList of words to ArrayList of lines
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
    
                for (int i = 0; i < lines.size(); i++){
                    if (lines.get(i).size() == 1){
                        String first = lines.get(i).get(0);
                        if (first.equals("h")){
                            System.out.println("List of commands:\n" +
                                               "h = show help\n" + 
                                               "d = dump register state\n" +
                                               "s = single step through the program (i.e. execute 1 instruction and stop) s num = step through num instructions of the program\n" +
                                               "r = run until the program ends\n" +
                                               "m num1 num2 = display data memory from location num1 to num2\n" +
                                               "c = clear all registers, memory, and the program counter to 0\n" +
                                               "b = show the branch prediction accuracy once the program is complete\n" +
                                               "q = exit the program");
    
                        }
                        else if (first.equals("d")){
                            // dump registers
                            System.out.println("pc = " + pc);
                                                
                            // Create a list of register names in the desired order
                            List<String> registerNames = new ArrayList<>(Arrays.asList(
                                "$v0", "$v1", "$a0", "$a1", "$a2", "$a3", "$t0", "$t1", "$t2", "$t3",
                                "$t4", "$t5", "$t6", "$t7", "$s0", "$s1", "$s2", "$s3", "$s4", "$s5",
                                "$s6", "$s7", "$t8", "$t9", "$sp", "$ra"
                            ));

                            int count = 0;
                            for (String registerName : registerNames) {
                                System.out.printf("%-4s = %-3d    ", registerName, registers.get(registerName));
                                count++;
                                if (count % 4 == 0) {
                                    System.out.println();
                                }
                            }
                            System.out.println();
                        }
                        else if (first.equals("s")){
                            if (pc < asmarray.length){
                                instrOp operation = new instrOp(asmarray[pc], labelMap, registers, stringMap, bitCount);
                                registers = operation.execute_instruction();
                                
                                // single step through instructions
                                pc++;
                                System.out.println("1 instruction completed.");
                            }
                            else {
                                System.out.println("Program Completed. Please clear.");
                            }
                        }
                        else if (first.equals("r")){
                            int count = 0;
                            while (pc < asmarray.length) {
                                instrOp operation = new instrOp(asmarray[pc], labelMap, registers, stringMap, bitCount);
                                registers = operation.execute_instruction();
                                pc++;
                                count++;
                            }
                            System.out.println("Program Completed. Please clear.");
                            System.out.println(count + " instruction(s) completed.");
                        }
                        else if(first.equals("c")){
                            // clear registers, memory (pc == 0)
                            System.out.println("yo\n");
                            for (Entry<String, Integer> entry : registers.entrySet()) {
                                entry.setValue(0);
                            }
                            for (int j = 0; j < datamemory.length; j++) {
                                datamemory[j] = 0;
                            }
                            pc = 0;
                        }
                        else if(first.equals("b")){
                            double battingPercentage = (float)correctPredictions/(float)totalPredictions;
                            battingPercentage = battingPercentage * 100;
                            DecimalFormat df = new DecimalFormat("##.##");
                            String fbattingPercentage = df.format(battingPercentage);
                            System.out.println(fbattingPercentage + "% (" + correctPredictions + " correct Predictions, " + totalPredictions + " predictions)");
                        }
                        else if (first.equals("q")){
                            // exit program 
                            System.exit(0);
                        }
                    }
                    else if (lines.get(i).size() == 2 && lines.get(i).get(0).equals("s")){
                        int instrNum = Integer.parseInt(lines.get(i).get(1));
                        int count = 0;
                        while (count < instrNum){
                            if (pc < asmarray.length){ 
                                instrOp operation = new instrOp(asmarray[pc], labelMap, registers, stringMap, bitCount);
                                registers = operation.execute_instruction();
                                pc++;
                                count++;
                            }
                            else {
                                System.out.println("Reached the end of program. Please clear.");
                                count = instrNum;
                            }       
                        }
                        System.out.println(instrNum + " instruction(s) completed.");
                    }
                    else if(lines.get(i).size() == 3 && lines.get(i).get(0).equals("m")){
                        int lower = Integer.parseInt(lines.get(i).get(1));
                        int upper = Integer.parseInt(lines.get(i).get(2));
                        for (int k = lower; k <= upper; k++) {
                            System.out.println("[" + k + "] = " + datamemory[k]);
                        }
                    }
                }
    
        } else if (args.length == 3 && (args[2].equals("2") || args[2].equals("4") || args[2].equals("8"))) {
            // script
            String filename = args[1];
            File script = new File(filename);

            if (!script.isFile() || !script.exists()){
                System.out.print("Unable to open given file\n");
                return;
            }
            ArrayList<ArrayList<String>> lines = new ArrayList<>(); 
            
            int bitCount = Integer.parseInt(args[2]);
            
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] splitLine = line.split(" "); 
                    ArrayList<String> words = new ArrayList<>(); // initialize ArrayList to hold words
                    for (String word : splitLine) {
                        words.add(word); // add each word to ArrayList
                    }
                    lines.add(words); // add ArrayList of words to ArrayList of lines
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < lines.size(); i++){
                if (lines.get(i).size() == 1){
                    String first = lines.get(i).get(0);
                    if (first.equals("h")){
                        System.out.println("List of commands:\n" +
                                           "h = show help\n" + 
                                           "d = dump register state\n" +
                                           "s = single step through the program (i.e. execute 1 instruction and stop) s num = step through num instructions of the program\n" +
                                           "r = run until the program ends\n" +
                                           "m num1 num2 = display data memory from location num1 to num2\n" +
                                           "c = clear all registers, memory, and the program counter to 0\n" +
                                           "b = show the branch prediction accuracy once the program is complete\n" +
                                           "q = exit the program");

                    }
                    else if (first.equals("d")){
                        // dump registers
                        System.out.println("pc = " + pc);
                                            
                        // Create a list of register names in the desired order
                        List<String> registerNames = new ArrayList<>(Arrays.asList(
                            "$v0", "$v1", "$a0", "$a1", "$a2", "$a3", "$t0", "$t1", "$t2", "$t3",
                            "$t4", "$t5", "$t6", "$t7", "$s0", "$s1", "$s2", "$s3", "$s4", "$s5",
                            "$s6", "$s7", "$t8", "$t9", "$sp", "$ra"
                        ));

                        int count = 0;
                        for (String registerName : registerNames) {
                            System.out.printf("%-4s = %-3d    ", registerName, registers.get(registerName));
                            count++;
                            if (count % 4 == 0) {
                                System.out.println();
                            }
                        }
                        System.out.println();
                    }
                    else if (first.equals("s")){
                        if (pc < asmarray.length){
                            instrOp operation = new instrOp(asmarray[pc], labelMap, registers, stringMap, bitCount);
                            registers = operation.execute_instruction();
                            
                            // single step through instructions
                            pc++;
                            System.out.println("1 instruction completed.");
                        }
                        else {
                            System.out.println("Program Completed. Please clear.");
                        }
                    }
                    else if (first.equals("r")){
                        int count = 0;
                        while (pc < asmarray.length) {
                            instrOp operation = new instrOp(asmarray[pc], labelMap, registers, stringMap, bitCount);
                            registers = operation.execute_instruction();
                            pc++;
                            count++;
                        }
                        System.out.println("Program Completed. Please clear.");
                        System.out.println(count + " instruction(s) completed.");
                    }
                    else if(first.equals("c")){
                        // clear registers, memory (pc == 0)
                        System.out.println("yo\n");
                        for (Entry<String, Integer> entry : registers.entrySet()) {
                            entry.setValue(0);
                        }
                        for (int j = 0; j < datamemory.length; j++) {
                            datamemory[j] = 0;
                        }
                        pc = 0;
                    }
                    else if(first.equals("b")){
                        double battingPercentage = (float)correctPredictions/(float)totalPredictions;
                        battingPercentage = battingPercentage * 100;
                        DecimalFormat df = new DecimalFormat("##.##");
                        String fbattingPercentage = df.format(battingPercentage);
                        System.out.println(fbattingPercentage + "% (" + correctPredictions + " correct Predictions, " + totalPredictions + " predictions)");
                    }
                    else if (first.equals("q")){
                        // exit program 
                        System.exit(0);
                    }
                }
                else if (lines.get(i).size() == 2 && lines.get(i).get(0).equals("s")){
                    int instrNum = Integer.parseInt(lines.get(i).get(1));
                    int count = 0;
                    while (count < instrNum){
                        if (pc < asmarray.length){ 
                            instrOp operation = new instrOp(asmarray[pc], labelMap, registers, stringMap, bitCount);
                            registers = operation.execute_instruction();
                            pc++;
                            count++;
                        }
                        else {
                            System.out.println("Reached the end of program. Please clear.");
                            count = instrNum;
                        }       
                    }
                    System.out.println(instrNum + " instruction(s) completed.");
                }
                else if(lines.get(i).size() == 3 && lines.get(i).get(0).equals("m")){
                    int lower = Integer.parseInt(lines.get(i).get(1));
                    int upper = Integer.parseInt(lines.get(i).get(2));
                    for (int k = lower; k <= upper; k++) {
                        System.out.println("[" + k + "] = " + datamemory[k]);
                    }
                }
            }

        } else {
            System.out.println("Invalid arguments: Please enter: \nlab5.java asm script -or-\nlab5.java asm GHRbitCount -or-\nlab5.java asm script GHRbitCount\n");
            System.exit(0);
        }

    }

}
