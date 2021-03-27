package net.sneak.r3.roleBot;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.sneak.r3.Main;

public class Roles extends ListenerAdapter implements AutoCloseable {
	private Map<String, Role[]> roles;
	private File jsonFile;
	
	private Roles() {
		this.roles = new HashMap<String, Role[]>();
		this.jsonFile = new File(System.getProperty("user.dir") + "/roles.json");
	}
	
	public void initRoles(Guild g) throws IOException {
		if(this.jsonFile.exists()) {
			JsonObject root = JsonParser.parseReader(new FileReader(this.jsonFile)).getAsJsonObject();
			for(Iterator<String> i = root.keySet().iterator(); i.hasNext();) {
				String group = i.next();
				List<Role> temp = new ArrayList<Role>();
				for(Iterator<Entry<String, JsonElement>> j = root.get(group).getAsJsonObject().entrySet().iterator(); j.hasNext();)
					temp.add(g.getRoleById(j.next().getValue().getAsLong()));
				this.roles.put(group, temp.toArray(new Role[] {}));
			}
		} else {
			this.jsonFile.createNewFile();
			new Thread(() -> {
				Color[] colours = {Color.WHITE, Color.CYAN, Color.GREEN, Color.ORANGE, Color.RED};
				Role[] genRoles = new Role[5];
				for(int i = 0; i < genRoles.length - 1; i++)
					genRoles[i] = g.createRole().setName("g" + (i * 20) + "-" + (i * 20 + 19)).setColor(colours[i]).complete();
				genRoles[genRoles.length - 1] = g.createRole().setName("g80-100").setColor(colours[4]).complete();
				this.roles.put("genRoles", genRoles);
			}).start();
			new Thread(() -> {
				this.roles.put("R3Ranks", new Role[] {
					g.createRole().setName("Grunt").setColor(Color.WHITE).complete(),
					g.createRole().setName("Spectre").setColor(Color.CYAN).complete(),
					g.createRole().setName("Reaper").setColor(Color.YELLOW).complete(),
					g.createRole().setName("Pilot").setColor(Color.ORANGE).complete(),
					g.createRole().setName("Titan").setColor(Color.RED).complete()
				});
			}).start();
			new Thread(() -> {
				this.roles.put("R3Staff", new Role[] {
					g.createRole().setName("Admin").setColor(Color.ORANGE).complete(),
				});
			}).start();
			new Thread(() -> {
				this.roles.put("otherRoles", new Role[] {
						g.createRole().setName("Streamer").setColor(Color.YELLOW).complete()	
				});
			}).start();
		}
		System.out.println("ROLES:");
		for(Iterator<Entry<String, Role[]>> i = this.roles.entrySet().iterator(); i.hasNext();) {
			Entry<String, Role[]> e = i.next();
			System.out.println(e.getKey());
			for(Role j : e.getValue())
				System.out.println("\t" + j.getName() + ": " + j.getId());
		}
	}
	
	public Role assignGenRole(Member m, int gen) {
		if(gen == 100)
			gen -= 1;
		Role r = this.roles.get("genRoles")[gen / 20];
		m.getGuild().modifyMemberRoles(m, null, Arrays.asList(this.roles.get("genRoles"))).queue((e) -> 
			m.getGuild().addRoleToMember(m.getIdLong(), r).queue());
		return r;
	}
	
	public void assignRanks() {
		List<Member> members = this.sort(Main.getBot().getGuildById(Main.IDs.get("R3")).getMembers());
		for(int i = 0; i < members.size() / 10; i++)
			members.get(i).getGuild().addRoleToMember(members.get(i), this.roles.get("R3Ranks")[3]);
	}
	
	private List<Member> sort(List<Member> numbers) {
		if(numbers.size() <= 1) return numbers;
		List<Member> result = new LinkedList<Member>();
		List<Member> left = new LinkedList<Member>();
		List<Member> right = new LinkedList<Member>();
		for(int i = 0; i < numbers.size(); i++)
		{
			if(i < (numbers.size()/2)) left.add(numbers.get(i));
			else right.add(numbers.get(i));
		}
		
		left = sort(left);
		right = sort(right);
		
		while((left.size() > 0) && (right.size() > 0))
		{
			if(left.get(0).getTimeJoined().getDayOfYear() < right.get(0).getTimeJoined().getDayOfYear()) {
				result.add(left.get(0));
				left.remove(0);
			}else {
				result.add(right.get(0));
				right.remove(0);
			}
		}
		result.addAll(left);
		result.addAll(right);
		return result;
	}
	
	/**Adds a role that already exists on the server to the list of roles that the bot can control. If the role is already in the list, nothing changes.
	 * @param id the ID of the role to be controlled by the bot
	 * @throws NullPointerException if the ID is invalid/the bot could not find a role with that ID
	 */
	public void addExistingRole(long id) {
		Role temp = Main.getBot().getRoleById(id);
		if(temp == null)
				throw new NullPointerException("No role was found with that name");
		else {
			for(Iterator<Role[]> i = this.roles.values().iterator(); i.hasNext();)
				for(Role j : i.next())
					if(j.equals(temp))
						return;
			List<Role> tempArr = Arrays.asList(this.roles.get("otherRoles"));
			tempArr.add(temp);
			this.roles.put("otherRoles", tempArr.toArray(new Role[] {}));
		}
	}
	
	/**Creates a new role in the guild that the bot automatically has control over. You can change permissions and the colour in the client
	 * @param name The name of the new role
	 */
	public void createNewRole(String name) {
		Main.getBot().getGuildById(Main.IDs.get("R3")).createRole().setName(name).queue((r) -> {
			List<Role> tempArr = Arrays.asList(this.roles.get("otherRoles"));
			tempArr.add(r);
			this.roles.put("otherRoles", tempArr.toArray(new Role[] {}));
		});
	}
	
	/**Removes the role from the list of roles that the bot should manage
	 * @param id the ID of the role
	 */
	public void removeRole(long id) {
		for(Iterator<Role[]> i = this.roles.values().iterator(); i.hasNext();) {
			Role[] temp = i.next();
			for(int j = 0; j < temp.length; j++) {
				if(temp[j].getIdLong() == id) {
					List<Role> tempArr = Arrays.asList(this.roles.get("otherRoles"));
					tempArr.remove(j);
					this.roles.put("otherRoles", tempArr.toArray(new Role[] {}));
					return;
				}
			}
		}
	}
	
	/**
	 * @deprecated This method cannot be guaranteed to work in all cases (mainly to do with roles that have the same name)
	 */
	@Deprecated
	public void setRoleIdByName(String name, long id) {
		for(Iterator<Role[]> i = this.roles.values().iterator(); i.hasNext();) {
			Role[] temp = i.next();
			for(int j = 0; j < temp.length; j++) {
				if(temp[j].getName().equals(name)) {
					temp[j] = Main.getBot().getRoleById(id);
					return;
				}
			}
		}
		throw new IllegalArgumentException("The does not have control over any roles named '" + name + "'");
	}
	
	@Override
	public void onRoleDelete(RoleDeleteEvent event) {
		for(Iterator<Role[]> i = this.roles.values().iterator(); i.hasNext();) {
			Role[] temp = i.next();
			for(int j = 0; j < temp.length; j++) {
				if(temp[j].equals(event.getRole())) {
					List<Role> tempList = Arrays.asList(temp);
					tempList.remove(j);
					temp = tempList.toArray(new Role[] {});
					return;
				}
			}
		}
	}
	
	@Override
	public void close() throws Exception {
		this.jsonFile.delete();
		this.jsonFile.createNewFile();
		JsonWriter w = new JsonWriter(new FileWriter(this.jsonFile));
		w.beginObject();
		for(Iterator<Entry<String, Role[]>> i = this.roles.entrySet().iterator(); i.hasNext();) {
			Entry<String, Role[]> e = i.next();
			w.name(e.getKey());
			w.beginObject();
			for(Role j : e.getValue()) {
				w.name(j.getName());
				w.value(j.getIdLong());
			}
			w.endObject();
		}
		w.endObject();
		w.close();
	}
	
	private static Roles instance;
	
	public static void init() throws IOException {
		instance = new Roles();
	}
	
	public static Roles getInstance() {
		return instance;
	}
}
