package net.sneak.discordTournamentBot.sql;

public class Args {
	
	private String field;
	private Operations operation;
	private Object arg;
	public Args(String field, Operations operation, Object arg) {
		this.arg = arg;
		this.field = field;
		this.operation = operation;
	}
	
	public static enum Operations {
		EQUALS("="),
		NOT_EQUAL_TO("<>"),
		LESS_THAN_OR_EQUAL_TO("<="),
		GREATER_THAN_OR_EQUAL_TO(">="),
		LESS_THAN("<"),
		GREATER_THAN(">");
		
		private final String stringRep;
		private Operations(String stringRep) {
			this.stringRep = stringRep;
		}
		
		@Override
		public String toString() {
			return this.stringRep;
		}
	}
	
	@Override
	public String toString() {
		return this.field + " " + this.operation.toString() + ((this.arg instanceof String)? " '" + this.arg + "'":this.arg);
	}
	
	public static String ArrayToString(Args[] args) {
		if(args == null)
			return null;
		if(args.length == 0)
			return null;
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < args.length; i++)
			sb.append(args[i].toString() + " AND ");
		for(int i = 0; i < 5; i++)
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
}
