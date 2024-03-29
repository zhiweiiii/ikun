package demo;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Team {
	private int id;
	private int[] players;
	private String force;

	public Team(JSONObject team) {
		this.id = team.getInt("id");
		System.out.printf("team id %d\n", this.id);
		
		
		JSONArray array = team.getJSONArray("players");
		this.players = new int[array.size()];
		for (int i = 0; i < players.length; i++) {
			players[i] = array.getInt(i);
			System.out.printf("player id %d\n", players[i]);
		}
		this.force = team.getString("force");
		System.out.printf("team force %s\n", this.force);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int[] getPlayers() {
		return players;
	}

	public void setPlayers(int[] players) {
		this.players = players;
	}

	public String getForce() {
		return force;
	}

	public void setForce(String force) {
		this.force = force;
	}
}
