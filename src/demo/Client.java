package demo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import map.*;

import java.util.*;
import java.util.stream.Collectors;

import demo.Team;
import cmd.Action;
import cmd.RoundAction;

public class Client {
	private int team_id = 0;
	private String team_name = "";
	private Team self = null;
	private Team enemy = null;
	private int roundId = 0;
	private String mode = null;
	private int moveIdx = 0;
	private List<String> moves = new ArrayList<String>();
	private List<Player> players = new ArrayList<Player>();
	private List<Player> enemy_players=new ArrayList<>();
	private List<Power> powers=new ArrayList<>();
	private List<Meteor> meteors = null;
	private int width=0;
	private int height=0;
	private List<oldMap> oldMaps = new ArrayList<oldMap>();
	private Map<Integer,String> actions_old = new HashMap<>();
	private Map<Integer,Integer> xy_old = new HashMap<>();

	public Client(int team_id, String team_name) {
		this.team_id = team_id;
		this.team_name = team_name;
		moves.add("up");
		moves.add("right");
		moves.add("down");
		moves.add("left");
	}

	public void legStart(JSONObject data) {
		System.out.println("leg start");

		JSONObject map = data.getJSONObject("map");

		int width = map.getInt("width");
		this.width=width;
		int height = map.getInt("height");
		this.height=height;
		int vision = map.getInt("vision");
		System.out.printf("map width:%d, map height %d, map vision %d\n", width, height, vision);

		try {
			this.meteors.clear();
			JSONArray meteorArray  = map.getJSONArray("meteor");
			for (int i = 0; i < meteorArray.size(); i++) {
				JSONObject object = meteorArray.getJSONObject(i);
				Meteor meteor = new Meteor(object);
				this.meteors.add(meteor);
			}
		}
		catch (Exception e) {
			System.out.printf("donot get meteor");
		}
		
		
		try {
		JSONArray cloudArray = map.getJSONArray("cloud");
		for (int i = 0; i < cloudArray.size(); i++) {
			JSONObject object = cloudArray.getJSONObject(i);
			Cloud cloud = new Cloud(object);
		}
		}
		catch (Exception e) {
			System.out.printf("donot get cloud");
		}
		
		try {		
			JSONArray tunnelArray = map.getJSONArray("tunnel");
			for (int i = 0; i < tunnelArray.size(); i++) {
				JSONObject object = tunnelArray.getJSONObject(i);
				Tunnel tunnel = new Tunnel(object);
			}
		}
		catch (Exception e) {
			System.out.printf("donot get tunnel");
		}
		
		try {		
			JSONArray wormholeArray = map.getJSONArray("wormhole");
			for (int i = 0; i < wormholeArray.size(); i++) {
				JSONObject object = wormholeArray.getJSONObject(i);
				Wormhole wormhole = new Wormhole(object);
			}
		}
		catch (Exception e) {
			System.out.printf("donot get wormhole");
		}

		try {		
			JSONArray teams = data.getJSONArray("teams");
	
			for (int i = 0; i < 2; i++) {
				JSONObject team = teams.getJSONObject(i);
				int team_id = team.getInt("id");
				if (this.team_id == team_id) {
					System.out.println("self team");
					this.self = new Team(team);
				} else {
					System.out.println("enemy team");
					this.enemy = new Team(team);
				}
			}
		}
		catch (Exception e) {
			System.out.printf("donot get teams");
		}
	}

	public void legEnd(JSONObject data) {
		System.out.println("leg end");

		try {
			JSONArray results = data.getJSONArray("teams");
			for (int i = 0; i < results.size(); i++) {
				Result result = new Result(results.getJSONObject(i));
			}
		}
		catch (Exception e) {
			System.out.printf("donot get legEnd teams");
		}
	}

	public void round(JSONObject data) {
		this.roundId = data.getInt("round_id");
		this.mode = data.getString("mode");
		System.out.printf("round %d, mode %s\n", this.roundId, this.mode);

		try {
			this.powers.clear();
			JSONArray powerArray = data.getJSONArray("power");
			for (int i = 0; i < powerArray.size(); i++) {
				JSONObject object = powerArray.getJSONObject(i);
				this.powers.add(new Power(object));
			}
		}
		catch (Exception e) {
			System.out.printf("donot get round power");
		}

		try {
			this.players.clear();
			this.enemy_players.clear();
			JSONArray players = data.getJSONArray("players");
			for (int i = 0; i < players.size(); i++) {
				JSONObject object = players.getJSONObject(i);
				Player player = new Player(object);
				if (player.getTeam() == this.team_id) {
					this.players.add(player);
				}else{
					this.enemy_players.add(player);
				}
			}
		}
		catch (Exception e) {
			System.out.printf("donot get round players");
		}

		try {
			JSONArray scores = data.getJSONArray("teams");
			for (int i = 0; i < scores.size(); i++) {
				JSONObject object = scores.getJSONObject(i);
				Score score = new Score(object);
			}
		}
		catch (Exception e) {
			System.out.printf("donot get round teams");
		}
	}

	private int getIdx()
	{
		int idx = moveIdx % 4;
		moveIdx ++;
		
		return idx;
	}
	public RoundAction act() {
		List<Action> actions = new ArrayList<Action>();
		int idx = getIdx();
		for(Player player : this.players)
		{
			String action="up";
			double grade=0;
			double up_grade = getGradeByPlace(player.getX(), player.getY() - 1,"up");
			double down_grade = getGradeByPlace(player.getX(), player.getY() + 1,"down");
			double left_grade = getGradeByPlace(player.getX() - 1, player.getY(),"left");
			double right_grade = getGradeByPlace(player.getX() + 1, player.getY(),"right");
			if (up_grade>grade){
				action="up";
				grade=up_grade;
			}
			if (down_grade>grade){
				action="down";
				grade=down_grade;
			}
			if (left_grade>grade){
				action="left";
				grade=left_grade;
			}
			if (right_grade > grade) {
				action = "right";
				grade = right_grade;
			}
//			if (actions_old.get(player.getId())!=null){
//
//			}
			actions.add(new Action(player.getTeam(), player.getId(), action));
//			actions_old.put(player.getId(),action);
//			xy_old.put(player.getId(),player.getX()*1000+player.getY());
		}
		RoundAction roundAction = new RoundAction(this.roundId, actions);
		return roundAction;
	}

//	private String getBestWay(int x,int y){
//		int left=Math.abs(x-1-width)+Math.abs(y-height);
//		int right=Math.abs(x+1-width)+Math.abs(y-height);
//		int up=Math.abs(x-width)+Math.abs(y-1-height);
//		int down=Math.abs(x-width)+Math.abs(y+1-height);
//		int[] arr=new int[] {left,right,up,down};
//		String[] Index=new String[]{"left","right","up","down"};
//		for(int i=0;i<arr.length;i++)
//		{
//			for(int j=0;j<arr.length-i-1;j++)
//			{
//				if(arr[j]<arr[j+1])
//				{
//					int temp = arr[j];
//					arr[j] = arr[j+1];
//					arr[j+1] = temp;
//
//					String index=Index[j];
//					Index[j] = Index[j+1];
//					Index[j+1] = index;
//				}
//			}
//		}
//
//        return Index[0];
//	}

	private Boolean notBorder(int x,int y){
		if (meteors!=null &&meteors.size()!=0) {
			//遇到陨石
			for (Meteor meteor : meteors) {
				if (x == meteor.getX() && y == meteor.getY()) {
					return false;
				}
			}
		}
		if (x<0 || x>=width ||y<0 || y>=height){
			//遇到边界
			return false;
		}
		return true;
	}

	private double getGradeByPlace(int x,int y,String action){
		double grade=0;
		double miniPowerGrade=100;
		double power_grade=0;
		double enemyGrade=0;
		if(powers!=null&& powers.size()!=0) {
		//计算最近的得分点
			for (Power power : powers) {
				int powerGrade = Math.abs(power.getX() - x) + Math.abs(power.getY() - y);
				if (powerGrade < miniPowerGrade) {
					miniPowerGrade = powerGrade;
				}
			}
            power_grade=Math.pow(miniPowerGrade,-1);
		}

		if (!mode.equals(self.getForce())) {
			//处于劣势方时避让
			int mini_dist = 100;
			for (Player player : enemy_players) {
				//遇见敌人紧急回避
				int enemy_dist = Math.abs(x - player.getX()) + Math.abs(y - player.getY());
				if (enemy_dist < mini_dist) {
					mini_dist = enemy_dist;
				}
			}
			enemyGrade = Math.pow(mini_dist,-1)*-1;
		}
		else{
			//处于优势方时主动出击
			int mini_dist=100;
			for (Player player:enemy_players){
				int enemy_dist=Math.abs(x-player.getX())+Math.abs(y-player.getY());
				if (enemy_dist<mini_dist){
					mini_dist=enemy_dist;
				}
			}
			enemyGrade= Math.pow(mini_dist,-1)*1;
		}


		grade=power_grade+enemyGrade;
		Random random=new Random();
//		if (action=="up" && y<height/3) {
//			grade += random.nextInt(3);
//		}
//		else if (action=="down" && y>(2*height)/3) {
//			grade += random.nextInt(3);
//		}
//		else if (action=="left" && x<width/3) {
//			grade += random.nextInt(3);
//		}
//		else if (action=="right" && x>(2*width)/3) {
//			grade += random.nextInt(3);
//		}else{
		grade+=Math.pow(random.nextInt(20),-1);
//		}
		if (!notBorder(x,y)){
			grade=-99999;
		}
		return grade;
	}

}
