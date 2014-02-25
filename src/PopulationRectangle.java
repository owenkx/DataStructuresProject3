import java.awt.Point;


public class PopulationRectangle {
	public float top;
	public float bottom;
	public float right;
	public float left;
	
	public PopulationRectangle(float xleft, float ytop, float xright, float ybottom) {
		this.top = ytop;
		this.bottom = ybottom;
		this.left = xleft;
		this.right = xright;
	}

}
