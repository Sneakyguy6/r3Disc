package net.sneak.discordTournamentBot.sql;

import java.sql.SQLException;
import java.util.function.Consumer;

public abstract class Query {
	protected Consumer<Query> onError;
	protected boolean success;
	
	protected Query() {
		this.success = true;
	}
	
	public abstract Query execute() throws SQLException;
	
	public boolean wasSuccessful() {
		return this.success;
	}
	
	public Query onError(Consumer<Query> err) {
		this.onError = err;
		return this;
	}
}
