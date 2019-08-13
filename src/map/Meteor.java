package map;

import net.sf.json.JSONObject;

public class Meteor {
	private int x;
	private int y;
	public Meteor(JSONObject object) {
		this.x = object.getInt("x");
		this.y = object.getInt("y");
		System.out.printf("meteor x %d, y %d\n", this.x, this.y);
		
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
}
