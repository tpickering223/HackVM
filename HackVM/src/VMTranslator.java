import java.io.*;

public class VMTranslator {
	public static void main(String[] args) throws IOException {
		
		File vmFile = null;
		File[] vmDirectory = null;
		
			if(args.length == 0) {
				System.out.println("No Args[] detected, enter a filepath after the program name in the following format: VMTranslator.java filepath");
				return;
			}
			else {
				vmFile = new File(args[0]);
				if(vmFile.isDirectory()) {
					vmDirectory = vmFile.listFiles();
					int numVM = 0;
					File temp = null;
					for(File input : vmDirectory) {
						String name = input.getName();
						
						if(name.endsWith(".vm")) {
							numVM++;
							temp = input;
						}
					}
					if(numVM == 1) {
						CodeWriter output = new CodeWriter(temp);
						output.stackInit();
						translate(output, temp); 
						output.terminate();
					}
					else {
						
						String vmPathName = vmFile.getName();
						File out = new File(vmFile + ".asm");
						CodeWriter output = new CodeWriter(out);
						output.writeInit();
						for(File input : vmDirectory) {
							String name = input.getName();
							
							if(name.endsWith(".vm")) {
								translate(output, input);
							}
							
						}
						output.terminate();
					}
			}
				else {
					CodeWriter output = new CodeWriter(vmFile);
					translate(output, vmFile);
					output.terminate();
				}
			}
			
		
		/*
		catch (ArrayIndexOutOfBoundsException a) {
			System.out.println("Error in obtaining Virtual Machine (.vm) file! Attempted filepath: args[0] = " + args[0]);
			System.out.println(a.getMessage());
		}
		*/
		
		
		System.out.println("VM Translation completed!");
	}
	
	
	public static void translate(CodeWriter output, File vmSource) {
		int linenumber = 0;
		try {
			Parser p = new Parser(vmSource);
			
			CommandType CType = null;
			while(p.hasMoreCommands()) {
				linenumber++;
				p.advance();
				if(p.isComment()) {
					System.out.println("Encountered comment at line " + linenumber);
				}
				else {
					CType = p.getCommandType();
					if (CType == CommandType.C_PUSH || CType == CommandType.C_POP) {
						if(p.getArg1().equalsIgnoreCase("STATIC")) {
							output.setStaticFile(vmSource);
							output.writePushPop(CType, p.getArg1(), p.getArg2());
							output.setStaticFile(null);
						}
						else {
						output.writePushPop(CType, p.getArg1(), p.getArg2());
						}
					}
					else if (CType == CommandType.C_ARITHMETIC) {
						output.writeArithmetic(p.getArg1());
					}
					else if (CType == CommandType.C_FUNCTION) {
						output.writeFunction(p.getArg1(),  p.getArg2());
					}
					else if (CType ==  CommandType.C_CALL) {
						output.writeCall(p.getArg1(), p.getArg2());
					}
					else if (CType == CommandType.C_RETURN){
						output.writeReturn();
					}
					else if (CType ==  CommandType.C_GOTO) {
						output.writeGoto(p.getArg1());
					}
					else if (CType == CommandType.C_LABEL) {
						output.writeLabel(p.getArg1());
					}
					else if (CType == CommandType.C_IF) {
						output.writeIf(p.getArg1());
					}
				}
				
			}
			
		}
		catch (IOException b) {
			System.out.println("Error in parsing Virtual Machine (.vm) file! Attempted filepath: args[0] = " + vmSource.getAbsolutePath());
			System.out.println(b.getMessage());
		}
		
	}
	
	
}
