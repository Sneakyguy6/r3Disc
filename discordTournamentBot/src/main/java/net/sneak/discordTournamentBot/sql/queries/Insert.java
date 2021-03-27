package net.sneak.discordTournamentBot.sql.queries;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sneak.discordTournamentBot.sql.Query;
import net.sneak.discordTournamentBot.sql.Sql;

public class Insert extends Query {
	private String table;
	private Object[] values;
	private String fields;
	
	public Insert(String table, String[] fields, Object[] data) {
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
		this.values = data;
	}
	
	@Override
	public Query execute() throws SQLException {
		if(this.fields.equals("*")) {
			this.fields = "";
			try {
				ResultSet rs = Sql.getInstance().getConnection().createStatement().executeQuery("select * from " + this.table + " limit 1;");
				for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++)
					this.fields += rs.getMetaData().getColumnName(i) + ",";
				this.fields = new StringBuilder(this.fields).deleteCharAt(this.fields.length() - 1).toString();
			} catch (SQLException e) {
				e.printStackTrace();
				super.success = false;
				return this;
			}
		}
		try {
			PreparedStatement p = Sql.getInstance().getConnection().prepareStatement("INSERT INTO " + table + " (" + fields + ")  VALUES (" + this.questionMarks() + ");");
			for(int i = 0; i < this.values.length; i++)
				p.setObject(i+1, this.values[i]);
			p.execute();
		} catch (SQLException e) {
			super.success = false;
			if(super.onError != null)
				super.onError.accept(this);
			else
				throw e;
		}
		return this;
	}
	
	private String questionMarks() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < this.values.length; i++)
			sb.append("?,");
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
}
