package net.sneak.r3.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.sneak.r3.Main;

public class TerminalListener {
	private TerminalListener() {
		Listener.init();
		this.loop();
	}
	
	private void loop() {
		new Thread(() -> {
			try(BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
				while(true) {
					try {
						String command = br.readLine();
						if(command.equalsIgnoreCase("stop"))
							break;
						String[] parts = command.split(" ");
						if(parts.length == 0) {
							System.out.println("Not enough arguments!");
							continue;
						}
						try {
							Listener.getInstance().getCommands().get(parts[0]).terminalRun(command);
						} catch (NullPointerException e) {
							System.out.println("Unrecognised command!");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				Main.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}).start();
	}
	
	private static TerminalListener instance;
	
	public static void init() {
		instance = new TerminalListener();
	}
	
	public static TerminalListener getInstance() {
		return instance;
	}
}
