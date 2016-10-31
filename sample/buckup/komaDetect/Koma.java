package komaDetect;

import org.opencv.core.Mat;
import org.opencv.core.Point;

public class Koma {
	private Mat image;
    private Point max,min;
    private int high;
    private int width;

    //コンストラクタ
    public Koma(Mat src, Point getMax, Point getMin) {
        image = src;
        max = getMax;
        min = getMin;
        high = (int) (max.y - min.y);
        width = (int) (max.x - min.x);
    }

    public Point getMaxPoint() {
       return max;
   }
    public Point getMinPoint() {
       return min;
   }
    public int getMaxPointX() { return (int)max.x;}
    public int getMaxPointY() { return (int)max.y;}
    public int getMinPointX() { return (int)min.x;}
    public int getMinPointY() { return (int)min.y;}
    public int getHigh() {
        high = (int) (max.y - min.y);
        return high;
    }
    public int getWidth() {
        width = (int) (max.x - min.x);
        return width;
    }
    public Mat getImage(){
        return image;
    }

    public void setImage(Mat src) {
    	image = src;
    }
    public void setMaxPointX(int x) {
    	max.x = x;
    }
    public void setMinPointX(int x) {
       min.x = x;
    }
    public void setMaxPointY(int y) {
    	max.y = y;
    }
    public void setMinPointY(int y) {
    	min.y = y;
    }
    public void setMaxPoint(Point setMax){
    	max = setMax;
    }
    public void setMinPoint(Point setMin){
    	min = setMin;
    }
}
