
import java.util.BitSet;
import java.util.HashMap;

public class instrOp {
    
    private HashMap<String, Integer> registers;
    private HashMap<String, Integer> labelMap;
    private HashMap<String, Integer> stringMap;
    private String instruction;
    private int pc;
    private boolean takenFlag;
    private boolean prediction;
    private int bitCount;

    // private int datamemory[];

    public instrOp(String instruction, HashMap<String, Integer> labelMap, HashMap<String, Integer> registers, HashMap<String, Integer> stringMap, int bitCount){
        this.labelMap = labelMap;
        this.instruction = instruction;
        this.registers = registers;
        this.stringMap = stringMap;
        this.pc = 0;
        this.takenFlag = false;
        this.prediction = true;
        this.bitCount = bitCount;
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
            predict();
            if (reg1 != reg2){
                lab5.pc = labelMap.get(arr[3]) - 1; // -1 because completing this instruction pc++
                System.out.println("Taken");
                this.takenFlag = true;
            } else {
                System.out.println("Not Taken");
                this.takenFlag = false;
            }
        }
        else if (instName.equals("beq")){
            int reg1 = get_register(arr[1]);
            int reg2 = get_register(arr[2]);
            predict();
            if (reg1 == reg2){
                System.out.println("Taken");
                this.takenFlag = true;
                lab5.pc = labelMap.get(arr[3]) - 1; // -1 because completing this instruction pc++
            } else {
                System.out.println("Not Taken");
                this.takenFlag = false;
            }
            
        }
        else if (instName.equals("j")){
            int address = labelMap.get(arr[1]);
            lab5.pc = address - 1; // -1 because completing this instruction pc++
        }
        else if (instName.equals("jr")){
            int address = get_register(arr[1]);
            lab5.pc = address; // -1 because completing this instruction pc++
        }
        else if (instName.equals("jal")){
            int address = labelMap.get(arr[1]);
            // set pc to $ra first, then set pc
            set_register("$ra", lab5.pc);
            lab5.pc = address - 1; // -1 because completing this instruction pc++
        }

        if (takenFlag && (instName.equals("beq") || instName.equals("bne"))){
            System.out.println("Branch was taken.");
            StringBuilder sb = new StringBuilder();
            if (bitCount == 2) {
                sb.append("0");
                sb.append("0");
                sb.append("0");
                sb.append("0");
                sb.append("0");
                sb.append("0");
                for (int i= 0; i < 2; i++){
                    sb.append(lab5.GHR[i]);
                }
            } else if (bitCount == 4){
                sb.append("0");
                sb.append("0");
                sb.append("0");
                sb.append("0");
                for (int i= 0; i < 4; i++){
                    sb.append(lab5.GHR[i]);
                }
            } else if (bitCount == 8){
                for (int i= 0; i < 8; i++){
                    sb.append(lab5.GHR[i]);
                }
            }

            String result = sb.toString();
            System.out.println("result is: " + result);
            int index = stringMap.get(result);
            System.out.println("index is :" + index);
            System.out.println("value at that index is : " + lab5.countArray[index]);
            if (lab5.countArray[index] < 3){
                lab5.countArray[index] = lab5.countArray[index] + 1;
            }
            
            if (this.prediction == true){
                lab5.correctPredictions++;
            }
            
            if (bitCount == 2){
                lab5.GHR[1] = lab5.GHR[0];
                lab5.GHR[0] = 1;
            } else if (bitCount == 4){
                lab5.GHR[3] = lab5.GHR[2];
                lab5.GHR[2] = lab5.GHR[1];
                lab5.GHR[1] = lab5.GHR[0];
                lab5.GHR[0] = 1;
            } else if (bitCount == 8){
                lab5.GHR[7] = lab5.GHR[6];
                lab5.GHR[6] = lab5.GHR[5];
                lab5.GHR[5] = lab5.GHR[4];
                lab5.GHR[4] = lab5.GHR[3];
                lab5.GHR[3] = lab5.GHR[2];
                lab5.GHR[2] = lab5.GHR[1];
                lab5.GHR[1] = lab5.GHR[0];
                lab5.GHR[0] = 1;
            }

        } else if (!takenFlag && (instName.equals("beq") || instName.equals("bne"))){
            System.out.println("Branch was not taken.");
            StringBuilder sb = new StringBuilder();
            if (bitCount == 2) {
                sb.append("0");
                sb.append("0");
                sb.append("0");
                sb.append("0");
                sb.append("0");
                sb.append("0");
                for (int i= 0; i < 2; i++){
                    sb.append(lab5.GHR[i]);
                }
            } else if (bitCount == 4){
                sb.append("0");
                sb.append("0");
                sb.append("0");
                sb.append("0");
                for (int i= 0; i < 4; i++){
                    sb.append(lab5.GHR[i]);
                }
            } else if (bitCount == 8){
                for (int i= 0; i < 8; i++){
                    sb.append(lab5.GHR[i]);
                }
            }

            String result = sb.toString();
            System.out.println("result is: " + result);
            int index = stringMap.get(result);
            System.out.println("index is : " + index);
            System.out.println("value at that index is : " + lab5.countArray[index]);
            if (lab5.countArray[index] > 0){
                lab5.countArray[index] = lab5.countArray[index] - 1;
            }
            if (this.prediction == false){
                lab5.correctPredictions++;
            }
            
            if (bitCount == 2){
                lab5.GHR[1] = lab5.GHR[0];
                lab5.GHR[0] = 0;
            } else if (bitCount == 4){
                lab5.GHR[3] = lab5.GHR[2];
                lab5.GHR[2] = lab5.GHR[1];
                lab5.GHR[1] = lab5.GHR[0];
                lab5.GHR[0] = 0;
            } else if (bitCount == 8){
                lab5.GHR[7] = lab5.GHR[6];
                lab5.GHR[6] = lab5.GHR[5];
                lab5.GHR[5] = lab5.GHR[4];
                lab5.GHR[4] = lab5.GHR[3];
                lab5.GHR[3] = lab5.GHR[2];
                lab5.GHR[2] = lab5.GHR[1];
                lab5.GHR[1] = lab5.GHR[0];
                lab5.GHR[0] = 0;
            }
            

        }
        return registers;

    }

    private void predict(){
        //boolean prediction;
        StringBuilder sb = new StringBuilder();

        if (bitCount == 2) {
            sb.append("0");
            sb.append("0");
            sb.append("0");
            sb.append("0");
            sb.append("0");
            sb.append("0");
            for (int i= 0; i < 2; i++){
                sb.append(lab5.GHR[i]);
            }
        } else if (bitCount == 4){
            sb.append("0");
            sb.append("0");
            sb.append("0");
            sb.append("0");
            for (int i= 0; i < 4; i++){
                sb.append(lab5.GHR[i]);
            }
        } else if (bitCount == 8){
            for (int i= 0; i < 8; i++){
                sb.append(lab5.GHR[i]);
            }
        }

        String result = sb.toString();
        int index = stringMap.get(result);
        if (lab5.countArray[index] > 1){
            System.out.println("Prediction: T");
            this.prediction = true;
        } else if (lab5.countArray[index] < 2) {
            System.out.println("Prediction: NT");
            this.prediction = false;
        }
        lab5.totalPredictions++;

        /* True = T False = NT */
       //return prediction;
    }

    /*
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
    */
}
