package net.sneak.discordTournamentBot.sql.queries;

import java.sql.SQLException;

import net.sneak.discordTournamentBot.sql.Args;
import net.sneak.discordTournamentBot.sql.Query;
import net.sneak.discordTournamentBot.sql.Sql;

public class Update extends Query {
	private String table;
	private String args;
	private String fieldsToChange;
	private int rowsUpdated;

	/**
	 * @param fieldsToChange <strong>NOTE: This field only accepts the 'EQUALS'
	 *                       operator and the first parameter is the field and the
	 *                       second is a value
	 */
	public Update(String table, Args[] fieldsToChange, Args[] args) {
		this.table = table;
		this.args = Args.ArrayToString(args);
		this.fieldsToChange = Args.ArrayToString(fieldsToChange);
		this.rowsUpdated = 0;
	}

	@Override
	public Query execute() throws SQLException {
		try {
			this.rowsUpdated = Sql.getInstance().getConnection().createStatement().executeUpdate("UPDATE " + table + " SET " + this.fieldsToChange + ((this.args != null)? " WHERE " + this.args:"") + ";");
		} catch (SQLException e) {
			super.success = false;
			if(super.onError != null)
				super.onError.accept(this);
			else
				throw e;
		}
		return this;
	}
	
	/**
	 * 
	 * @return number of rows updated by statement
	 */
	public int executeWithReturn() throws SQLException {
		this.execute();
		return this.rowsUpdated;
	}
}
