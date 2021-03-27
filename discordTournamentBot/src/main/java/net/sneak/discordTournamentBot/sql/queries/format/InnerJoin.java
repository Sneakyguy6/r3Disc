package net.sneak.discordTournamentBot.sql.queries.format;

public class InnerJoin {
	private String tableA;
	private String tableB;
	private String key;
	private String foreignColumn;

	public InnerJoin(String tableA, String tableB, String key, String foreignColumn) {
		this.tableA = tableA;
		this.tableB = tableB;
		this.key = key;
		this.foreignColumn = foreignColumn;
	}

	public String format() {
		return ("INNER JOIN `" + this.tableB + "` ON " + this.tableA + "." + this.key + "=" + this.tableB + "." + this.foreignColumn);
	}
	
	public static String ArrayToString(InnerJoin[] array) {
		StringBuilder sb = new StringBuilder();
		for(InnerJoin i : array)
			sb.append(i.format() + " ");
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
}
