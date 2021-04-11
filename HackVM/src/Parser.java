import java.util.Scanner;
import java.io.*;
/*
 * Parser class, takes input from VM stack file and separates it into its parts, while classifying them for the VMTranslator class. 
 *  Can detect comments, and can indicate to CodeWriter to generate comments based on the VM command.
 *  Also contains the enums for all of the keywords in the VM language.
 */
public class Parser {
	
	private Scanner p;
	private String[] parts;
	boolean commentFlag;
	CommandType currentType = null;
	
	public Parser(File input) {
		try {
			commentFlag = false; 
			p = new Scanner(input);
		} catch (FileNotFoundException e) {
			System.out.println("Parser Error in generating File Scanner! Attempted File: " + input.getName());
			System.out.println("Error info below:\n");
			e.printStackTrace();
		}
	}
	
	public boolean hasMoreCommands() {
		return p.hasNext();
	}
	
	
	public void advance() {
		commentFlag = false;
		String raw = p.nextLine().trim();
		raw = removeComments(raw).trim();
		if(raw.isBlank()) {
			commentFlag = true;
		}
		else {
			parts = raw.split(" ");		// Separate the input into parts.
			
		}
		
	}
	
	public boolean isComment() {
		return commentFlag;
	}
	
	public CommandType getCommandType() {
		if(commentFlag) {
			return null;
		}
		
		if(parts[0].equalsIgnoreCase("if-goto")) {		// Special case because hyphens can't be enum'd in java
			return CommandType.C_IF;
		}
		
		switch(Instruction.valueOf(parts[0].toUpperCase())) {
			case GOTO: 
				return CommandType.C_GOTO;
			case LABEL:
				return CommandType.C_LABEL;
			case RETURN:
				return CommandType.C_RETURN;
			case FUNCTION:
				return CommandType.C_FUNCTION;
			case CALL:
				return CommandType.C_CALL;
			case PUSH:
				return CommandType.C_PUSH;
			case POP:
				return CommandType.C_POP;
			case ADD:
			case SUB:
			case AND:
			case OR:
			case NOT:
			case EQ:
			case LT:
			case GT:
			case NEG:
				currentType = CommandType.C_ARITHMETIC;
				return CommandType.C_ARITHMETIC;
			default:
				return null;
		
		
		}
	}
	
	public String getArg1() {
		if(!(parts.length >  1)) {
			return parts[0];
		}
		else {
			return parts[1];
		}
	}
	
	public Integer getArg2() {
		if (parts.length > 2) {
			return Integer.parseInt(parts[2]);
		}
		else {
			return null;
		}
	}
	
	public String removeComments(String line) {
		if(line.contains("//")) {
			line = line.substring(0, line.indexOf('/'));
			return line;
		}
		else {
			return line;
		}
	}
	
}
enum CommandType {
	C_PUSH,
	C_POP,
	C_LABEL,
	C_GOTO,
	C_RETURN,
	C_IF,
	C_FUNCTION,
	C_CALL,
	C_ARITHMETIC
}
enum Instruction {
	ADD,
	SUB,
	AND,
	OR,
	NOT,
	NEG,
	EQ,
	LT,
	GT,
	PUSH,
	POP,
	FUNCTION,
	CALL,
	RETURN,
	GOTO,
	LABEL
}

enum Segment {
	POINTER,
	CONSTANT,
	STATIC,
	THIS,
	THAT,
	LOCAL,
	ARGUMENT,
	TEMP
}
