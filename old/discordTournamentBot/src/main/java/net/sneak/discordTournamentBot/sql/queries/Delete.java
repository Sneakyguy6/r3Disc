package net.sneak.discordTournamentBot.sql.queries;

import java.sql.SQLException;

import net.sneak.discordTournamentBot.sql.Args;
import net.sneak.discordTournamentBot.sql.Query;
import net.sneak.discordTournamentBot.sql.Sql;

public class Delete extends Query {
	private String table;
	private String args;
	
	public Delete(String table, Args[] args) {
		this.table = table;
		
		if(args != null) {
			StringBuilder sb = new StringBuilder();
			if(args.length != 0) {
				for(int i = 0; i < args.length; i++)
					sb.append(args[i].toString() + ", ");
				for(int i = 0; i < 2; i++)
					sb.deleteCharAt(sb.length() - 1);
				this.args = sb.toString();
			}
		}
	}
	
	@Override
	public Query execute() throws SQLException {
		try {
			Sql.getInstance().getConnection().createStatement().execute("DELETE FROM " + table + ((args != null)? " WHERE " + args:"") + ";");
		} catch (SQLException e) {
			super.success = false;
			if(super.onError != null)
				super.onError.accept(this);
			else
				throw e;
		}
		return this;
	}
}
