package net.sneak.discordTournamentBot.commands;

import net.sneak.discordTournamentBot.commands.team.Team;
import net.sneak.discordTournamentBot.commands.tournament.BuildTournament;
import net.sneak.discordTournamentBot.commands.tournament.StartTournament;

public enum Commands {
	INTRODUCE("introduce", "What is tournament bot?", new Description()),
	HELP("help", "Get help", new Help()),
	TEAM("team", "Manage teams with this. For more details, do 'team help'", new Team()),
	STARTTOURNAMENT("startTournament", null, new StartTournament()),
	BUILDTOURNAMENT("buildTournament", null, new BuildTournament()),
	UPLOADFOOTAGE("footage", "Use this to upload footage after a game. When using this command, you must add a youtube link at the end", new UploadFootage()),
	VERIFY("verify", null, new Verify());
	
	private final ICommand executor;
	private final String alias;
	private final String description;
	private Commands(String a, String d, ICommand e) {
		this.alias = a;
		this.description = d;
		this.executor = e;
	}
	
	public ICommand getExecutor() {
		return this.executor;
	}
	
	public String getAlias() {
		return this.alias;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public static Commands getCommandFromString(String s) {
		for(Commands i : values())
			if(i.alias.toString().equalsIgnoreCase(s))
				return i;
		return null;
	}
}
