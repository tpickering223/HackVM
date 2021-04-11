import java.io.*;
/*
 * Project #8 from nand2tetris.
 * CodeWriter class, responsible for writing assembly code when given a VM command.
 * Can handle input file changes for static variables and bootstrap initialization.
 * Function/variables names correspond to the recommendations of the book "Elements of Computing Systems" and nand2tetris.
 * Other than the helper functions ^
 * 
 * 
 */

public class CodeWriter {
	private File out;
	private FileWriter toOut;
	private int labelCount;
	private String title;
	private int framepointer = 0;
	private File vmStat;
	
	// Initialization, sets CodeWriter to an input file and generates the proper .asm for the entire directory/file.
	public CodeWriter(File vm) {
		try {
			labelCount = 0;
			out = new File(vm.getAbsolutePath().replace(".vm", ".asm"));
			toOut = new FileWriter(out);
			title = vm.getName();
			vmStat = null;
			
		}
		catch (IOException e) {
			System.out.println("Error occurred in CodeWriter initialization.\n Debug info - Attempted File: " + vm.getAbsolutePath().replace(".vm", ".hack"));
			System.out.println("Exception Information:");
			System.out.println(e.getMessage());
		}
		
	}
	
	// Setter function to set the static file (e.g. like a class file) to allow for static differentiation.
	public void setStaticFile(File vmStatic) {
		vmStat = vmStatic;
	}
	
	// Set stack to 256, the value specified in the book.
	public void stackInit() throws IOException {
		toOut.write("// INITIALIZING VM-SPACE...\n");	
		toOut.write("@256\n");		// Stack size as mentioned in the book's RAM Addresses Specification
		toOut.write("D=A\n");
		toOut.write("@SP\n");
		toOut.write("M=D\n");
	}

	public void writeInit() {
		// Bootstrap code
		try {
			toOut.write("// INITIALIZING VM-SPACE...\n");	
			toOut.write("@256\n");		// Stack size as mentioned in the book's RAM Addresses Specification
			toOut.write("D=A\n");
			toOut.write("@SP\n");
			toOut.write("M=D\n");
			toOut.write("// Stack pointer set to address 256.\n");
			writeCall("Sys.init", 0);
		}
		catch (IOException e) {
			System.out.println("Error occurred in CodeWriter initialization.\n");
			System.out.println("Exception Information:");
			System.out.println(e.getMessage());
		}
	}
	
	// Set output file, setter function.
	public void setOutFile(String filename) {
		try {
			out = new File(filename);
			toOut = new FileWriter(out);
		}
		catch (IOException e) {
			System.out.println("Error occurred in (re)setting the output file!.\nDebug info - Attempted File: " + filename);
			System.out.println("Exception Information:");
			System.out.println(e.getMessage());
		}
	}
	/*
	 * Writes code for Arithmetic functions.
	 *  
	 */
	public void writeArithmetic(String command) throws IOException {
		Instruction switchkey = Instruction.valueOf(command.toUpperCase());
		switch(switchkey) {
			case ADD:
				// Left an example without using my helper methods to give a better picture of what's happening.
				toOut.write("\n// ADD_POP_1\n");	// Pop Stack
				toOut.write("@SP\n");			// A = 0
				toOut.write("M=M-1\n");			// Decrement SP
				toOut.write("A=M\n");			// A = SP_addr
				toOut.write("D=M\n");			// D = RAM[SP]
				toOut.write("// ADD_POP_2\n");	// Pop stack again
				toOut.write("@SP\n");
				toOut.write("M=M-1\n");
				toOut.write("A=M\n");		
				toOut.write("// Add Operation\n");
				toOut.write("M=M+D\n");			// ADD Operation x+y
				toOut.write("@SP\nM=M+1\t// ADD_FINISHED\n");
				break;
			case SUB:
				toOut.write("\n// SUB_POP_1\n");	
				decrementSP();
				toOut.write("D=M\n");			
				toOut.write("// SUB_POP_2\n");	
				decrementSP();
				toOut.write("// SUB Operation\n");
				toOut.write("M=M-D\n");			// SUB Operation x-y
				toOut.write("@SP\nM=M+1\t// SUB_FINISHED\n");
				break;
			case NEG:
				toOut.write("// NEGATION \n");
				decrementSP();
				toOut.write("M=-M\n");		// Negation Operation x=-x
				incrementSP();
				break;
			case NOT:
				toOut.write("// bitwise NOT \n");
				decrementSP();
				toOut.write("M=!M\n");	// x = !x
				incrementSP();
				break;
			case AND:
				toOut.write("// bitwise AND\n");
				decrementSP();
				toOut.write("D=M\n");
				decrementSP();
				toOut.write("M=M&D\n");
				incrementSP();
				break;
			case OR:
				toOut.write("// bitwise OR\n");
				decrementSP();
				toOut.write("D=M\n");
				decrementSP();
				toOut.write("M=M|D\n");
				incrementSP();
				break;
			case EQ:
				toOut.write("// EQ Comparison if(x==y)\n");
				decrementSP();
				toOut.write("D=M\n");
				decrementSP();
				toOut.write("D=D-M\n");
				toOut.write("M=-1\n");			// Assume true by setting M to true. This was originally 1 but apparently true is -1 in hack;
				toOut.write("@COMP_LABEL" + labelCount + "\n");
				toOut.write("D;JEQ\n");			
				toOut.write("@SP\n");
				toOut.write("A=M\n");			// Set M to 0 if false.
				toOut.write("M=0\n");
				toOut.write("(COMP_LABEL" + labelCount + ")\n");
				incrementSP();
				labelCount++;
				break;
			case LT:
				toOut.write("// LT Comparison if(x<y)\n");
				decrementSP();
				toOut.write("D=M\n");
				decrementSP();
				toOut.write("D=M-D\n");
				toOut.write("M=-1\n");			// Assume true by setting M to true;
				toOut.write("@COMP_LABEL" + labelCount + "\n");
				toOut.write("D;JLT\n");			
				toOut.write("@SP\n");
				toOut.write("A=M\n");			// Set M to 0 if false.
				toOut.write("M=0\n");
				toOut.write("(COMP_LABEL" + labelCount + ")\n");
				incrementSP();
				labelCount++;
				break;
			case GT:
				toOut.write("// GT Comparison if(x>y)\n");
				decrementSP();
				toOut.write("D=M\n");
				decrementSP();
				toOut.write("D=M-D\n");
				toOut.write("M=-1\n");			// Assume true by setting M to true;
				toOut.write("@COMP_LABEL" + labelCount + "\n");
				toOut.write("D;JGT\n");			
				toOut.write("@SP\n");
				toOut.write("A=M\n");			// Set M to 0 if false.
				toOut.write("M=0\n");
				toOut.write("(COMP_LABEL" + labelCount + ")\n");
				incrementSP();
				labelCount++;
				break;
		}
	}
	
	// Helper function to decrement the Stack Pointer.
	private void decrementSP() throws IOException {
		toOut.write("@SP\n");
		toOut.write("M=M-1\n");
		toOut.write("A=M\n");
	}
	
	// Helper function to increment the Stack Pointer.
	private void incrementSP() throws IOException {
		toOut.write("@SP\n");
		toOut.write("M=M+1\n");
	}
	
	// Function to generate assembly code for push and pop VM commands.
	public void writePushPop(CommandType instruction, String segment, int value) throws IOException {
		if(instruction.equals(CommandType.C_PUSH)) {
			toOut.write("// PUSH " + segment + " " + value + "\n");
			switch (Segment.valueOf(segment.toUpperCase())) {
				case LOCAL:
					toOut.write("@LCL\n");
					toOut.write("D=M\n");	// Get LOCAL base address segment
					toOut.write("@" + value + "\n");
					toOut.write("A=D+A\n");	// Calculate final address
					toOut.write("D=M\n");
					toOut.write("@SP\n");
					toOut.write("A=M\n");
					toOut.write("M=D\n");
					incrementSP();
					break;
				case ARGUMENT:
					toOut.write("@ARG\n");	
					toOut.write("D=M\n");	// Grab ARG base Address
					copyToStack(value);
					incrementSP();
					break;
				case THIS:
					toOut.write("@THIS\n");
					toOut.write("D=M\n");	// Get THIS base address segment
					copyToStack(value);
					incrementSP();
					break;
				case THAT:
					toOut.write("@THAT\n");
					toOut.write("D=M\n");	// Get THIS base address segment
					copyToStack(value);
					incrementSP();
					break;
				case CONSTANT:
					toOut.write("@" + value + "\n");
					toOut.write("D=A\n");
					toOut.write("@SP\n");
					toOut.write("A=M\n");
					toOut.write("M=D\n");	// Set top of stack to CONSTANT value
					incrementSP();
					break;
				case STATIC:
					title = vmStat.getName();
					toOut.write("@" + title + value + "\n");
					toOut.write("D=M\n");
					toOut.write("@SP\n");
					toOut.write("A=M\n");
					toOut.write("M=D\n");	// Set top of stack to CONSTANT value
					incrementSP();
					
					break;
				case POINTER:
					if(value == 0) {
						toOut.write("@THIS\n");
						toOut.write("D=M\n");
						toOut.write("@SP\n");
						toOut.write("A=M\n");
						toOut.write("M=D\n");
						incrementSP();
					}
					else {
						toOut.write("@THAT\n");
						toOut.write("D=M\n");
						toOut.write("@SP\n");
						toOut.write("A=M\n");
						toOut.write("M=D\n");
						incrementSP();
					}
					break;
				case TEMP:
					int totalIndex = value + 5;
					toOut.write("@" + totalIndex + "\n");
					toOut.write("D=M\n");
					toOut.write("@SP\n");
					toOut.write("A=M\n");
					toOut.write("M=D\n");
					incrementSP();
					break;
			}
		}
		else if (instruction.equals(CommandType.C_POP)) {
			toOut.write("// Pop " + segment + value + "\n");
			switch (Segment.valueOf(segment.toUpperCase())) {
				case LOCAL:
					toOut.write("@" + value + "\n");
					toOut.write("D=A\n");				// D = LOCAL address offset.
					toOut.write("@LCL\n");
					toOut.write("D=D+M\n");				// Get actual address
					toOut.write("@13\n");				// R13 according to book's specification
					toOut.write("M=D\n");				// Save address to temp register.
					toOut.write("@SP\n");
					toOut.write("M=M-1\n");
					toOut.write("A=M\n");
					toOut.write("D=M\n");				// Grab value from top of stack via popping. Will turn these 4 lines into a helper function.
					toOut.write("@13\n");
					toOut.write("A=M\n");
					toOut.write("M=D\n");				// Copy over popped value to its destination address in LOCAL.
					break;
				case ARGUMENT:
					toOut.write("@" + value + "\n");
					toOut.write("D=A\n");
					toOut.write("@ARG\n");
					toOut.write("A=M\n");
					toOut.write("D=D+A\n");
					toOut.write("@14\n");
					toOut.write("M=D\n");
					copyFromStack();
					toOut.write("@14\n");
					toOut.write("A=M\n");
					toOut.write("M=D\n");
					break;
				case THIS:
					toOut.write("@" + value + "\n");
					toOut.write("D=A\n");
					toOut.write("@THIS\n");
					toOut.write("A=M\n");
					toOut.write("D=D+A\n");
					toOut.write("@15\n");
					toOut.write("M=D\n");
					copyFromStack();
					toOut.write("@15\n");
					toOut.write("A=M\n");
					toOut.write("M=D\n");
					break;
				case THAT:
					toOut.write("@" + value + "\n");
					toOut.write("D=A\n");
					toOut.write("@THAT\n");
					toOut.write("A=M\n");
					toOut.write("D=D+A\n");
					toOut.write("@13\n");
					toOut.write("M=D\n");
					copyFromStack();
					toOut.write("@13\n");
					toOut.write("A=M\n");
					toOut.write("M=D\n");
					break;
				case STATIC:
					title = vmStat.getName();
					toOut.write("@" + title + value + "\n");
					toOut.write("D=A\n");
					toOut.write("@14\n");
					toOut.write("M=D\n");
					copyFromStack();
					toOut.write("@14\n");
					toOut.write("A=M\n");
					toOut.write("M=D\n");
					
					break;
				case TEMP:
					copyFromStack();
					int totalIndex = value + 5;
					toOut.write("@" + totalIndex + "\n");
					toOut.write("M=D\n");	// Assign Temp space stack variable.
					break;
				case POINTER:
					if(value == 0) {
						toOut.write("@THIS\n");
						toOut.write("D=A\n");
						toOut.write("@15\n");
						toOut.write("M=D\n");
						copyFromStack();
						toOut.write("@15\n");
						toOut.write("A=M\n");
						toOut.write("M=D\n");
						
					}
					else {
						toOut.write("@THAT\n");
						toOut.write("D=A\n");
						toOut.write("@13\n");
						toOut.write("M=D\n");
						copyFromStack();
						toOut.write("@13\n");
						toOut.write("A=M\n");
						toOut.write("M=D\n");
					}
			}
		}
	}
	
	// Special helper function, copies to Stack and then increments it.
	public void copyAndIncrSP() throws IOException {
		toOut.write("@SP\n");
		toOut.write("A=M\n");
		toOut.write("M=D\n");
		incrementSP();
	}
	
	private void copyToStack(int index) throws IOException {
		toOut.write("@" + index + "\n");
		toOut.write("A=D+A\n");	// Calculate final address
		toOut.write("D=M\n");
		toOut.write("@SP\n");
		toOut.write("A=M\n");
		toOut.write("M=D\n");
	}
	
	// Helper function that essentially just pops the stack and assigns the D register to the popped value.
	private void copyFromStack() throws IOException {
		toOut.write("@SP\n");
		toOut.write("M=M-1\n");
		toOut.write("A=M\n");
		toOut.write("D=M\n");
	}
	
	
	// Function for generating code for Calls
	public void writeCall(String functionLabel, int numArgs) {
		labelCount++;
		String returnLabel = "RETURN_ADDRESS_" + labelCount;
		try {
			toOut.write("@" + returnLabel + "\n");		// Push/Save return address
			toOut.write("D=A\n");
			toOut.write("@SP\n");
			toOut.write("A=M\n");
			toOut.write("M=D\n");
			incrementSP();
			toOut.write("@LCL\n");						// Push/Save LOCAL base address
			toOut.write("D=M\n");	
			copyAndIncrSP();
			
			toOut.write("@ARG\n");						// Push/Save ARG base address
			toOut.write("D=M\n");
			copyAndIncrSP();
			
			toOut.write("@THIS\n");						// Push/Save THIS base address
			toOut.write("D=M\n");
			copyAndIncrSP();
			
			toOut.write("@THAT\n");						// Push/Save THAT base address
			toOut.write("D=M\n");
			copyAndIncrSP();
			
			toOut.write("@SP\n");
			toOut.write("D=M\n");
			toOut.write("@5\n");
			toOut.write("D=D-A\n");
			toOut.write("@" + numArgs + "\n");
			toOut.write("D=D-A\n");
			toOut.write("@ARG\n");
			toOut.write("M=D\n");						// Calculate SP - n - 5 as per the book and set it to ARGs pointer.
			
			toOut.write("@SP\n");
			toOut.write("D=M\n");
			toOut.write("@LCL\n");
			toOut.write("M=D\n");						// LCL = SP
			
			toOut.write("@" + functionLabel + "\n");
			toOut.write("0;JMP\n");						// Goto function
			
			toOut.write("(" + returnLabel + ")\n");
		}
		catch (IOException err) {
			System.out.println("Error in function call code generation for return address: " + returnLabel);
			System.out.println(err.getMessage());
		}
	}
	
	// Function for generating return assembly code.
	public void writeReturn() {
		try {
			
			toOut.write("@LCL\n");
			toOut.write("D=M\n");
			toOut.write("@FRAME" + framepointer + "\n");
			toOut.write("M=D\n");		// FRAME = LCL
			
			toOut.write("@5\n");
			toOut.write("A=D-A\n");		//Calculate FRAME - 5
			toOut.write("D=M\n");
			toOut.write("@RET\n");
			toOut.write("M=D\n");		// Copy return address to RET as per book
			
			//writePushPop(CommandType.C_POP, "ARGUMENT", 0);	<-- Can't do this, this scenario requires special push syntax.
			toOut.write("@ARG\n");		// Pop ARG back to caller
			toOut.write("D=M\n");
			toOut.write("@" + "0\n");
			toOut.write("D=D+A\n");
			toOut.write("@13\n");
			toOut.write("M=D\n");
			toOut.write("@SP\n");
			toOut.write("AM=M-1\n");
			toOut.write("D=M\n");
			toOut.write("@13\n");
			toOut.write("A=M\n");
			toOut.write("M=D\n");
			
			toOut.write("@ARG\n");
			toOut.write("D=M\n");
			toOut.write("@SP\n");
			toOut.write("M=D+1\n");		// Restore SP to caller
			
			toOut.write("@FRAME" + framepointer + "\n");
			toOut.write("D=M-1\n");		// Get FRAME - 1
			toOut.write("AM=D\n");
			// toOut.write("A=D\n");
			toOut.write("D=M\n");
			toOut.write("@THAT\n");
			toOut.write("M=D\n");		// Restore THAT to caller
			
			toOut.write("@FRAME" + framepointer + "\n");
			toOut.write("D=M-1\n");		// Get FRAME - 1
			toOut.write("AM=D\n");
			toOut.write("D=M\n");
			toOut.write("@THIS\n");
			toOut.write("M=D\n");		// Restore THIS to caller
			
			toOut.write("@FRAME" + framepointer + "\n");
			toOut.write("D=M-1\n");		// Get FRAME - 1
			toOut.write("AM=D\n");
			toOut.write("D=M\n");
			toOut.write("@ARG\n");
			toOut.write("M=D\n");		// Restore ARG to caller
			
			toOut.write("@FRAME" + framepointer + "\n");
			toOut.write("D=M-1\n");		// Get FRAME - 1
			toOut.write("AM=D\n");
			toOut.write("D=M\n");
			toOut.write("@LCL\n");
			toOut.write("M=D\n");		// Restore THAT to caller
			
			toOut.write("@RET\n");
			toOut.write("A=M\n");
			toOut.write("0;JMP\n");		// goto return address
			framepointer++;
		}
		catch (IOException err) {
			System.out.println("Error in return code generation for return address!");
			System.out.println(err.getMessage());
		}
	}
	
	// Function to generate assembly code for function declarations
	public void writeFunction(String functionName, int localVars) {
		try { 
			toOut.write("(" + functionName + ")\n");
			for(int i = 0; i < localVars; i++) {
				writePushPop(CommandType.C_PUSH, "constant", 0);
			}
		}
		catch (IOException err) {
			System.out.println("Error in function declaration code generation for function address: " + functionName);
			System.out.println(err.getMessage());
		}
		
	}
	
	// Function to generate assembly code for labels.
	public void writeLabel(String label) throws IOException  {
		toOut.write("(" + label + ")\n");
	}
	
	// Function to generate assembly code for GOTO statements
	public void writeGoto(String goLabel) throws IOException  {
		toOut.write("@" + goLabel + "\n");
		toOut.write("D;JMP\n");
	}
	
	// Function to generate assembly code for if-goto statements
	public void writeIf(String cond_branch) throws IOException {
		copyFromStack();
		toOut.write("@" + cond_branch + "\n");
		toOut.write("D;JNE\n");
	}
	
	// Closes main file after generating program termination assembly code.
	public void terminate() {
		try {
			toOut.write("(END)\n");
			toOut.write("@END\n");
			toOut.write("0;JMP");	//Infinite loop to end program
			
			toOut.close();
		} catch (IOException e) {
			System.out.println("Unable to close FileWriter in CodeWriter! Error Info:\n");
			e.printStackTrace();
		}
	}
	
}
