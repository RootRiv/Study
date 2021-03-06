package findcontours;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class FindContoursTest {
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ

		String path_in = "C:/Users/Tonegawa/Pictures/fig1.jpg";
		String path_gray_out = "C:/Users/Tonegawa/Pictures/Findcontours/sample_gray.jpg";
		String path_edge_out = "C:/Users/Tonegawa/Pictures/Findcontours/sample_edges.jpg";
		String path_rect_out = "C:/Users/Tonegawa/Pictures/Findcontours/sample_rect.jpg";
		String path_white = "C:/Users/Tonegawa/Pictures/Findcontours/white.jpg";
		String path_gousei = "C:/Users/Tonegawa/Pictures/Findcontours/gousei.jpg";
		String path_rin = "C:/Users/Tonegawa/Pictures/Findcontours/rin.jpg";
		String path_white_out = "C:/Users/Tonegawa/Pictures/Findcontours/white_out.jpg";
		String path_circle_out = "C:/Users/Tonegawa/Pictures/Findcontours/circle_out.jpg";

		Mat src = new Mat();
		Mat gray = new Mat();
		Mat edges = new Mat();
		Mat hierarchy = new Mat();
		Mat white_out = Highgui.imread(path_white_out);
		Mat circle_img = new Mat();

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>(100);

		src = Highgui.imread(path_in);						 // 入力画像の読み込み
		Mat src_copy = src.clone();
		Mat white = src.clone();
		Mat back = src.clone();
		Mat dst = src.clone();
		circle_img = src.clone();
		back.setTo(new Scalar(0, 0, 0));
		white.setTo(new Scalar(255, 255, 255));
		Highgui.imwrite(path_white, white);
		Mat rin = white.clone();

		Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY); // カラー画像をグレー画像に変換
		//Imgproc.threshold(gray, edges, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);					//　エッジ検出
		Imgproc.Canny(gray, edges, 100, 200);

		Highgui.imwrite(path_edge_out, edges);

		//輪郭検出
		Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		//輪郭描画
		Imgproc.drawContours(src,contours,-1,new Scalar(255,0,0),2);
		Imgproc.drawContours(rin,contours,-1,new Scalar(255,0,0),2);

		Highgui.imwrite(path_gray_out, src);						// 出力画像を保存
		Highgui.imwrite(path_rin, rin);

		Mat m = new Mat();
		Point pt1 = new Point();

		int count = 0;
		int high;
		int weight;
		Koma[] koma = new Koma[10];

		for(int i = 0;i < contours.size();i++){
			Point max_pt = new Point();
			Point min_pt = new Point();
			//輪郭の集合
			m = contours.get(i);
			//System.out.println(m.size());
			if(Imgproc.contourArea(m) > 30000){
				System.out.println(m.size());
				System.out.println(Imgproc.contourArea(m));

				max_pt.x = m.get(0,0)[0];
				max_pt.y = m.get(0,0)[1];
				min_pt.x = m.get(0,0)[0];
				min_pt.y = m.get(0,0)[1];

				//ひとつの輪郭に含まれる点を表示
				//最小点と最大点を求める
				for(int j = 0;j < m.rows();j++){
					pt1.x = m.get(j,0)[0];
					pt1.y = m.get(j,0)[1];
					/*
					 * 最新版　正しく動いている。
					 *
					 * 7月21日
					 */
					if(pt1.x > max_pt.x)
						max_pt.x = pt1.x;
					if(pt1.y > max_pt.y)
						max_pt.y = pt1.y;
					if(pt1.x < min_pt.x)
						min_pt.x = pt1.x;
					if(pt1.y < min_pt.y)
						min_pt.y = pt1.y;
				}

				System.out.print("min_pt = (" + min_pt.x + ", ");
				System.out.println(min_pt.y + ")");
				System.out.print("max_pt = (" + max_pt.x + ", ");
				System.out.println(max_pt.y + ")");
				high = (int) (max_pt.y - min_pt.y);
				weight = (int) (max_pt.x - min_pt.x);

				koma[count] = new Koma(src, max_pt, min_pt);

				//デバッグ
				System.out.println("count = " + count);
				System.out.println(koma[count].getMinPoint());

				if(count != 0){
					System.out.println(koma[count-1].getMinPoint());
				}
				count++;
			}
		}

		//デバッグ
		System.out.println();
		System.out.println(count);
		for(int i = 0;i < count;i++){
			//System.out.println("i = " + i);
			System.out.println(koma[i].getMinPoint());
		}

		//sort
		//Arrays.sort(koma, 0, count, new SampleComparator());

		//4koma sort
		Arrays.sort(koma, 0, count, new XYComparator());

		//デバッグ
		System.out.println("after sort");
		for(int i = 0;i < count;i++){
			//System.out.println("i = " + i);
			System.out.println(koma[i].getMinPoint());
		}

		Integer name = 0;
		for(int i = 0;i < count;i++){
			fncCutImageRect(src, koma[i].getMinPoint(), koma[i].getWidth(), koma[i].getHigh(), name);
			name++;
			Core.rectangle(white, koma[i].getMinPoint(), koma[i].getMaxPoint(), new Scalar(255,0,0), -1);
		}

		Point[] center = drwCenterPoint(circle_img, koma, count);

		ArrayList<Point> cluster1 = new ArrayList<Point>();
		ArrayList<Point> cluster2 = new ArrayList<Point>();
		ArrayList<Point> cluster3 = new ArrayList<Point>();

		fncSep(src_copy, center, cluster1, cluster2, cluster3);
		Point grSort[] = groupSort(cluster1, cluster2,cluster3);
		Koma[] g = PointCeter2Koma(grSort,koma,count);

		Integer num=0;
		for(int i = 0;i < count;i++){
			System.out.println( "i = " + i);
			fncCutImageRect(src, g[i].getMinPoint(), g[i].getWidth(), g[i].getHigh(), "GroupSort" + num.toString());
			num++;
		}


		Highgui.imwrite(path_rect_out, white);
		//Core.addWeighted(white_out, 0.5, white, 0.5, 0, dst);
		//Core.addWeighted(dst, 0.5, rin, 0.5, 0, dst);
		Highgui.imwrite(path_gousei, dst);
		Highgui.imwrite(path_circle_out, circle_img);
	}

	private static Point[] groupSort(ArrayList<Point> cluster1, ArrayList<Point> cluster2, ArrayList<Point> cluster3){
		Collections.sort(cluster1,new Point4Comparator());
		Collections.sort(cluster2,new Point4Comparator());
		Collections.sort(cluster3,new Point4Comparator());

		int len = cluster1.size() + cluster2.size() + cluster3.size();
		int pop = 0;
		Point p[] = new Point[len];
		for(int i = 0;i < cluster1.size();i++){
			p[i] = cluster1.get(i);
			pop++;
		}
		for(int i = 0;i < cluster2.size();i++){
			p[pop] = cluster2.get(i);
			pop++;
		}
		for(int i = 0;i < cluster3.size();i++){
			p[pop] = cluster3.get(i);
			pop++;
		}
		return p;
	}

	private static Koma[] PointCeter2Koma(Point[] p, Koma k[],int count){
		Koma rk[] = new Koma[count];
		for(int i = 0;i < count;i++){
			for(int j = 0;j < count;j++){
				if(p[i].equals(k[j].getCenterPoint())){
					rk[i] = k[j];
				}
			}
		}
		return rk;
	}

	//長方形切抜き
	private static void fncCutImageRect(Mat img, Point pt1, int w,int h,Integer count){
		Rect roi = new Rect((int)pt1.x, (int)pt1.y, w, h);
		Mat img2 = new Mat(img, roi);
		String s = new String("C:/Users/Tonegawa/Pictures/Findcontours/" + count.toString() + ".jpg");
		Highgui.imwrite(s,img2);
		System.out.println(count + ".jpg save success");
	}

	private static void fncCutImageRect(Mat img, Point pt1, int w,int h,String name){
		Rect roi = new Rect((int)pt1.x, (int)pt1.y, w, h);
		Mat img2 = new Mat(img, roi);
		String s = new String("C:/Users/Tonegawa/Pictures/Findcontours/" + name + ".jpg");
		Highgui.imwrite(s,img2);
		System.out.println(name + ".jpg save success");
	}

	private static Point[] drwCenterPoint(Mat img, Koma[] koma, int count){
		Point center  = new Point();
		Point[] r = new Point[count];
		for(int i = 0;i < count;i++){
			r[i] = new Point();
			center.x = (koma[i].getMinPointX() + koma[i].getMaxPointX()) / 2;
			center.y = (koma[i].getMinPointY() + koma[i].getMaxPointY()) / 2;
			r[i].x = center.x;
			r[i].y = center.y;
			Core.circle(img, center, 5, new Scalar(0,0,255), -1);
		}

		return r;
	}

	//kmeansによるグループ分け
	private static void fncSep(Mat src, Point[] p, ArrayList<Point> cluster1, ArrayList<Point> cluster2, ArrayList<Point> cluster3){
		/*
		ArrayList<Point> cluster1 = new ArrayList<Point>();
		ArrayList<Point> cluster2 = new ArrayList<Point>();
		ArrayList<Point> cluster3 = new ArrayList<Point>();
		 */
		String filename = "C:/Users/Tonegawa/Pictures/Findcontours/cluster_out.jpg";

		Point[] center = new Point[3];
		for(int i = 0;i < 3;i++){
			center[i] = new Point(src.width()/2, (src.height()*(i+1))/4);
		}

		System.out.println("center[0] = " + center[0]);
		System.out.println("center[1] = " + center[1]);
		System.out.println("center[2] = " + center[2]);

		int num;
		for(int i = 0;i < p.length;i++){
			num = Kmeans.getCluster(p[i], center);
			switch(num){
				case 0:
					cluster1.add(p[i]);
					break;
				case 1:
					cluster2.add(p[i]);
					break;
				case 2:
					cluster3.add(p[i]);
					break;
			}
		}

		boolean flag = true;
		int roop_num = 0;
		while(roop_num != 10){
			//debug
			roop_num++;
			System.out.println();
			System.out.println("ROOP NUM = " + roop_num);
			System.out.println("center[0] = " + center[0]);
			System.out.println("Kmeans.getCenter(cluster1) = " + Kmeans.getCenter(cluster1));
			System.out.println("center[1] = " + center[1]);
			System.out.println("Kmeans.getCenter(cluster2) = " + Kmeans.getCenter(cluster2));
			System.out.println("center[2] = " + center[2]);
			System.out.println("Kmeans.getCenter(cluster3) = " + Kmeans.getCenter(cluster3));

			if(center[0].equals(Kmeans.getCenter(cluster1)) && center[1].equals(Kmeans.getCenter(cluster2)) && center[2].equals(Kmeans.getCenter(cluster3))){
				flag = false;
				break;
			}
			center[0] = Kmeans.getCenter(cluster1);
			center[1] = Kmeans.getCenter(cluster2);
			center[2] = Kmeans.getCenter(cluster3);
			for(int i = 0;i < cluster1.size();i++){
				num = Kmeans.getCluster(cluster1.get(i), center);
				if(num != 0){
					switch(num){
						case 1:
							cluster2.add(cluster1.get(i));
							break;
						case 2:
							cluster3.add(cluster1.get(i));
							break;
					}
					cluster1.remove(i);
					i--;
				}
			}
			for(int i = 0;i < cluster2.size();i++){
				num = Kmeans.getCluster(cluster2.get(i), center);
				if(num != 1){
					switch(num){
						case 0:
							cluster1.add(cluster2.get(i));
							break;
						case 2:
							cluster3.add(cluster2.get(i));
							break;
					}
					cluster2.remove(i);
					i--;
				}
			}
			for(int i = 0;i < cluster3.size();i++){
				num = Kmeans.getCluster(cluster3.get(i), center);
				if(num != 2){
					switch(num){
						case 0:
							cluster1.add(cluster3.get(i));
							break;
						case 1:
							cluster2.add(cluster3.get(i));
							break;
					}
					cluster3.remove(i);
					i--;
				}
			}
		}

		for(int i = 0;i < cluster1.size();i++){
			Core.circle(src, cluster1.get(i), 5, new Scalar(0,0,255), -1);
		}
		for(int i = 0;i < cluster2.size();i++){
			Core.circle(src, cluster2.get(i), 5, new Scalar(0,255,0), -1);
		}
		for(int i = 0;i < cluster3.size();i++){
			Core.circle(src, cluster3.get(i), 5, new Scalar(255,0,0), -1);
		}
		Core.circle(src, center[0], 5, new Scalar(0,0,255), -1);
		Core.circle(src, center[1], 5, new Scalar(0,255,0), -1);
		Core.circle(src, center[2], 5, new Scalar(255,0,0), -1);
		System.out.println();
		System.out.println("center[0] = " + center[0]);
		System.out.println("center[1] = " + center[1]);
		System.out.println("center[2] = " + center[2]);

		Highgui.imwrite(filename, src);
	}


	//切り抜くコマの情報を一つにまとめたクラス
    public static class Koma{
        private Mat image;
        private Point max,min;
        private int high;
        private int width;
        Point center = new Point();

        //コンストラクタ
        public Koma(Mat src, Point getMax, Point getMin) {
            image = src;
            max = getMax;
            min = getMin;
			center.x = (int)(min.x + max.x) / 2;
			center.y = (int)(min.y + max.y) / 2;
            high = (int) (max.y - min.y);
            width = (int) (max.x - min.x);
        }

        public Point getMaxPoint() {
           return max;
       }
        public Point getMinPoint() {
           return min;
       }
        public Point getCenterPoint() {
            return center;
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

    //sortのための比較
    public static class SampleComparator implements Comparator<Koma>{
    	public int compare(Koma koma1, Koma koma2) {
    		int k = koma1.getMinPointY() - koma2.getMinPointY();
    		if(k <= 10 && k >= -10)
    			return  koma2.getMinPointX() - koma1.getMinPointX()  ;

    		return k;
    	}
    }

    //4コマ漫画用comparator
    public static class XYComparator implements Comparator<Koma>{
    	public int compare(Koma koma1, Koma koma2) {
    		int k = koma2.getMinPointX() - koma1.getMinPointX();
    		if(k <= 10 && k >= -10)
    			return   koma1.getMinPointY() - koma2.getMinPointY();

    		return k;
    	}
    }

    public static class Point4Comparator implements Comparator<Point>{
    	public int compare(Point p, Point q) {
    		int k = (int) (q.x - p.x);
    		if(k <= 10 && k >= -10)
    			return   (int) (p.y - q.y);

    		return k;
    	}
    }

}