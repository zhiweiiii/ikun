package map;

import net.sf.json.JSONObject;

public class Power {
	private int x;
	private int y;
	private int point;
	public Power(JSONObject object) {
		this.x = object.getInt("x");
		this.y = object.getInt("y");
		this.point = object.getInt("point");
		System.out.printf("power x %d, y %d, point %d\n", this.x, this.y, this.point);
		
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

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}
}
