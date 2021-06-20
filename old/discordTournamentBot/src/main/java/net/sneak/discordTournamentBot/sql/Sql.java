package net.sneak.discordTournamentBot.sql;

import java.sql.Connection;
import java.sql.DriverManager;

public class Sql implements AutoCloseable {
	private Connection con;

	private static Sql instance;

	public static Sql getInstance() {
		return instance;
	}

	public static void init() {
		instance = new Sql();
	}

	private Sql() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			this.con = DriverManager.getConnection("jdbc:mysql://192.168.1.20:3306/tf2Tournament", "remote", "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		return this.con;
	}

	@Override
	public void close() throws Exception {
		this.con.close();
	}
}
