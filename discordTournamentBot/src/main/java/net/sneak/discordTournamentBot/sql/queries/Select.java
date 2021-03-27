package net.sneak.discordTournamentBot.sql.queries;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.sneak.discordTournamentBot.sql.Args;
import net.sneak.discordTournamentBot.sql.Query;
import net.sneak.discordTournamentBot.sql.Sql;
import net.sneak.discordTournamentBot.sql.queries.format.InnerJoin;

public class Select extends Query
{
	private String table;
	private String args;
	private String fields;
	private ResultSet result;
	private String innerJoin;
	
	/**
	 * @param fields passing null to this will cause the fields section in the query to be '*' (i.e. pass null to get all fields in the table)
	 */
	public Select(String table, String[] fields, Args[] args) {
		this.table = table;
		if(fields == null)
			this.fields = "*";
		else {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < fields.length; i++)
				sb.append(fields[i] + ", ");
			for(int i = 0; i < 2; i++)
				sb.deleteCharAt(sb.length() - 1);
			this.fields = sb.toString();
		}
		this.args = Args.ArrayToString(args);
	}
	
	public Select(String table, String[] fields, Args[] args, InnerJoin[] links) {
		this.table = table;
		if(fields == null)
			this.fields = "*";
		else {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < fields.length; i++)
				sb.append(fields[i] + ", ");
			for(int i = 0; i < 2; i++)
				sb.deleteCharAt(sb.length() - 1);
			this.fields = sb.toString();
		}
		this.args = Args.ArrayToString(args);
		this.innerJoin = InnerJoin.ArrayToString(links);
	}
	
	@Override
	public Query execute() throws SQLException {
		try {
			this.result = Sql.getInstance().getConnection()
					.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
					.executeQuery("SELECT " + this.fields + " FROM " + this.table + ((this.innerJoin != null)? " " + this.innerJoin:"") + ((this.args != null)? " WHERE " + this.args:"") + ";");
		} catch (SQLException e) {
			super.success = false;
			if(super.onError != null)
				super.onError.accept(this);
			else
				throw e;
		}
		return this;
	}
	
	public ResultSet executeWithReturn() throws SQLException {
		this.execute();
		return this.result;
	}
	
	public ResultSet getResult() {
		return this.result;
	}
}
