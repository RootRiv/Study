package findcontours;

import java.util.ArrayList;

import org.opencv.core.Point;

public class Kmeans {
	final static int number = 3;
	Point[] center = new Point[number];

	public static int getDistance(Point p, Point q) {
		double distance = Math.sqrt((q.x - p.x) * (q.x - p.x) + (q.y - p.y) * (q.y - p.y));

		return (int)distance;
	}

	public static Point getCenter(ArrayList<Point> p){
		double sumx = 0;
		double sumy = 0;
		for(int i = 0;i < p.size();i++){
			sumx += p.get(i).x;
			sumy += p.get(i).y;
		}

		Point q = new Point();
		q.x = sumx / p.size();
		q.y = sumy / p.size();

		return q;
	}

	public static int getCluster(Point p, Point[] center){
		int minDis = Integer.MAX_VALUE;
		int distance;
		int cluster = 0;

		for(int i = 0;i < center.length;i++){
			distance = getDistance(p, center[i]);
			if(distance < minDis){
				minDis = distance;
				cluster = i;
			}
		}

		return cluster;
	}


}
