package demo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import map.*;

import java.util.*;

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
	private List<Meteor> meteors = new ArrayList<>();
	private List<Tunnel> tunnels=new ArrayList<>();
	private int width=0;
	private int height=0;
	private int vision=0;
	private List<OldMap> oldMaps = new ArrayList<OldMap>();
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
		this.vision=vision;
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
            tunnels.clear();
			JSONArray tunnelArray = map.getJSONArray("tunnel");
			for (int i = 0; i < tunnelArray.size(); i++) {
				JSONObject object = tunnelArray.getJSONObject(i);
				Tunnel tunnel = new Tunnel(object);
				this.tunnels.add(tunnel);
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
		if (roundId%30==0){
			for (int i=0;i<width;i++){
				OldMap oldMap=new OldMap();
				boolean[] y_height=new boolean[height];
				for(int j=0;j<height;j++){
					y_height[j]=false;
				}
				oldMap.setY_map(y_height);
				oldMaps.add(oldMap);
			}
		}
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
			String action="left";
			double grade=-999;
			double up_grade = getGradeByPlace(player.getX(), player.getY() - 1,"up",player);
			double down_grade = getGradeByPlace(player.getX(), player.getY() + 1,"down",player);
			double left_grade = getGradeByPlace(player.getX() - 1, player.getY(),"left",player);
			double right_grade = getGradeByPlace(player.getX() + 1, player.getY(),"right",player);
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
			actions.add(new Action(player.getTeam(), player.getId(), action));
			//增加已探索区域
			for(int i=-this.vision;i<=this.vision;i++){
				for (int j=-this.vision;j<=this.vision;j++){
					if (player.getX()+i>=0 && player.getX()+i<width && player.getY()+j>=0 && player.getY()+j<height) {
						oldMaps.get(player.getX() + i).getY_map()[player.getY() + j] = true;
					}
				}
			}
		}
		RoundAction roundAction = new RoundAction(this.roundId, actions);
		return roundAction;
	}


	private Boolean notBorder(int x,int y,String action){
	    if(meteors!=null && meteors.size()!=0) {
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
        if (tunnels!=null && tunnels.size()!=0) {
            //反向进入时空隧道
            for (Tunnel tunnel:tunnels){
                if (tunnel.getX()==x && tunnel.getY()==y){
                    if ((tunnel.getDirection().equals("down") && action.equals("up")) || (tunnel.getDirection().equals("up") && action.equals("down"))|| (tunnel.getDirection().equals("left") && action.equals("right"))|| (tunnel.getDirection().equals("right") && action.equals("left"))){
                        return false;
                    }
                }
            }

        }
		return true;
	}

	private double getGradeByPlace(int x,int y,String action,Player main_player){
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
			power_grade=100*Math.pow(miniPowerGrade,-1);
		}
		//遇到同伴分散开来
		double mini_play_dist=100;
		for (Player player_2 : players) {
			if(main_player.getId()!=player_2.getId()) {
				int play_dist = Math.abs(x - player_2.getX()) + Math.abs(y - player_2.getY());
				if (play_dist < mini_play_dist) {
					mini_play_dist = play_dist;
				}
				if(player_2.getX()==main_player.getX() && player_2.getY()==main_player.getY()){
					Random random=new Random();
					mini_play_dist=random.nextInt(5);
				}
			}
		}
		double playDistGrade=101*Math.pow(mini_play_dist,-1)*-1;

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
			enemyGrade = 130*Math.pow(mini_dist,-1)*-1;
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
			enemyGrade= 130 * Math.pow(mini_dist,-1)*1;
		}
		//未探索的区域得分
		double unsee_dist=0;
		for (int i=0;i<width;i++){
			OldMap y_height=oldMaps.get(i);
			for(int j=0;j<height;j++){
				if(!y_height.getY_map()[j]){
					//未探索的区域
					unsee_dist+=Math.abs(x-i)+Math.abs(y-j);
				}
			}
		}
		double unseeGrade=unsee_dist*-0.01;
		grade=power_grade+enemyGrade+unseeGrade+playDistGrade;
		//随机因子
		Random random=new Random();
//		grade+=Math.pow(random.nextInt(20),-1);
		if (!notBorder(x,y,action)){
			grade=-9999;
		}
		return grade;
	}

}
