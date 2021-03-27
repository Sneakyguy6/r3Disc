package net.sneak.r3.roleBot;

import net.sneak.r3.commands.Command;

public class CommandSetRoleId extends Command {

	public CommandSetRoleId() {
		super("setRoleId");
	}
	
	/*@Override
	public String terminalRun(String command) {
		try {//setRoleId [name] [ID]
			String[] parts = command.split(" ");
			Roles.getInstance().setRoleIdByName(parts[1], Long.parseLong(parts[2]));
		} catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("Not enough arguments");
		} catch(NumberFormatException e) {
			System.out.println("That is not a number");
		} catch(NullPointerException e) {
			System.out.println("The specified guild could not be found");
		}
		return "Role ID set!";
	}*/
}
