
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class instrOp {
    
    private HashMap<String, Integer> registers;
    private HashMap<String, Integer> labelMap;
    private String instruction;
    private int pc;
    private int[] globalHistoryRegister;
    private int correctPredictions;
    private int totalPredictions;
    // private int datamemory[];

    public instrOp(String instruction, HashMap<String, Integer> labelMap, HashMap<String, Integer> registers){
        this.labelMap = labelMap;
        this.instruction = instruction;
        this.registers = registers;
        this.pc = 0;
        this.globalHistoryRegister = new int[2];
        this.correctPredictions = 0;
        this.totalPredictions = 0;
        //this.datamemory = datamemory;
    }

    public int get_register(String name){
        return registers.get(name);
    }

    public void set_register(String name, int value){
        registers.put(name, value);
    }

    public int get_pc(){
        return this.pc;
    }

    public void set_pc(int value){
        this.pc = value;
    }

    public HashMap<String, Integer> execute_instruction(){
        String arr[] = instruction.trim().split("\\s+");
        String instName = arr[0];

        if (instName.equals("add")){
            String destination = arr[1];
            int reg1 = get_register(arr[2]);
            int reg2 = get_register(arr[3]);
            int finnal = reg1 + reg2;
            set_register(destination, finnal);
        }

        else if (instName.equals("addi")){
            String destination = arr[1];
            int reg1 = get_register(arr[2]);
            String immediate = arr[3];
            int intimm = Integer.parseInt(immediate);
            int finnal = reg1 + intimm;
            set_register(destination, finnal);
        }
        
        else if (instName.equals("sub")){
            String destination = arr[1];
            int reg1 = get_register(arr[2]);
            int reg2 = get_register(arr[3]);
            int finnal = reg1 - reg2;
            set_register(destination, finnal);
        }

        else if (instName.equals("and")){
            String destination = arr[1];
            int reg1 = get_register(arr[2]);
            int reg2 = get_register(arr[3]);
            int finnal = reg1 & reg2;
            set_register(destination, finnal);
        }

        else if (instName.equals("or")){
            String destination = arr[1];
            int reg1 = get_register(arr[2]);
            int reg2 = get_register(arr[3]);
            int finnal = reg1 | reg2;
            set_register(destination, finnal);
        }

        else if (instName.equals("sll")){
            String destination = arr[1];
            int reg1 = get_register(arr[2]);
            int imm = Integer.parseInt(arr[3]);
            int finnal = reg1 << imm;
            set_register(destination, finnal);
        }
        else if (instName.equals("slt")){
            String destination = arr[1];
            int reg1 = get_register(arr[2]);
            int reg2 = get_register(arr[3]);
            int finnal;
            if (reg1 < reg2){
                finnal = 1;
            }
            else{
                finnal = 0;
            }
            set_register(destination, finnal);
        }
        else if (instName.equals("sw")){
            int value = get_register(arr[1]);
            int offset = Integer.parseInt(arr[2]);
            String reg1 = arr[3];
            int memoryLoc = get_register(reg1) + offset;
            lab5.datamemory[memoryLoc] = value; 
        }
        else if (instName.equals("lw")){
            String destination = arr[1];
            int offset = Integer.parseInt(arr[2]);
            String reg1 = arr[3];
            int memoryLoc = get_register(reg1) + offset;
            int value = lab5.datamemory[memoryLoc];
            set_register(destination, value);
        }
        else if (instName.equals("bne")){
            int reg1 = get_register(arr[1]);
            int reg2 = get_register(arr[2]);

            if (reg1 != reg2){
                lab5.pc = labelMap.get(arr[3]) - 1; // -1 because completing this instruction pc++
            }

        }
        else if (instName.equals("beq")){
            int reg1 = get_register(arr[1]);
            int reg2 = get_register(arr[2]);
            if (reg1 == reg2){
                lab5.pc = labelMap.get(arr[3]) - 1; // -1 because completing this instruction pc++
            }

        }
        else if (instName.equals("j")){
            int address = labelMap.get(arr[1]);
            lab5.pc = address - 1; // -1 because completing this instruction pc++
        }
        else if (instName.equals("jr")){
            int address = get_register(arr[1]);
            lab5.pc = address - 1; // -1 because completing this instruction pc++
        }
        else if (instName.equals("jal")){
            int address = get_register(arr[1]);
            // set pc to $ra first, then set pc
            set_register("$ra", lab5.pc);
            lab5.pc = address - 1; // -1 because completing this instruction pc++
        }
        if (branchPredict(instName)){
            totalPredictions++;
        }
        
        return registers;

    }

    private boolean branchPredict(String instName){
        if (instName.equals("beq") || instName.equals("bne") ||
        instName.equals("j") || instName.equals("jr") || instName.equals("jal")){
            totalPredictions ++;
            int prediction = globalHistoryRegister[pc % globalHistoryRegister.length];
            boolean shouldPredict = prediction >= 2; // predict if prediction is ST or T
            boolean branchTaken = instName.equals("beq") || instName.equals("bne");
            if (shouldPredict == branchTaken){
                correctPredictions++;
            }
            return shouldPredict;
        }
        return false;
    }

    public void updateGHR(String instName, boolean branchTaken){
        if (instName.equals("beq") || instName.equals("bne") ||
        instName.equals("j") || instName.equals("jr") || instName.equals("jal")){
            int prediction = globalHistoryRegister[pc % globalHistoryRegister.length];
            if (branchTaken){
                if (prediction < 3){
                    prediction++;
                }
            }
            else{
                if (prediction > 0){
                    prediction--;
                }
            }
            globalHistoryRegister[pc % globalHistoryRegister.length] = prediction;
        }
    }

    public void printBranchPrediction(){
        double accuracy = (double) correctPredictions / totalPredictions * 100;
        System.out.printf("Accuracy: %.2f%% (%d correct predictions, %d predictions)%n",
                accuracy, correctPredictions, totalPredictions);
    }

}
