package matchTemp;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;

public class matchTemp {
	
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		Mat src = new Mat();
		String filename = "C:/Users/Tonegawa/Pictures/side_sample.jpg";
		String outpath = "C:/Users/Tonegawa/Pictures/match/out.jpg";
		String debugpath = "C:/Users/Tonegawa/Pictures/match/debug.jpg";
		
		src = Highgui.imread(filename);
		Mat white = new Mat(50, src.cols(), CvType.CV_8UC3);
		white.setTo(new Scalar(255,255,255));
		Mat result = new Mat(src.rows() - white.rows() + 1, src.cols() - white.cols() + 1, CvType.CV_8UC3);
		
		//Imgproc.matchTemplate(src, white, result, Imgproc.TM_SQDIFF_NORMED);
	
		System.out.println("resukt size = " + result.size());
		
		/*
		//結果から相関係数がしきい値以下を削除（０にする）
		Imgproc.threshold(result, result, 0.9, 1.0, Imgproc.THRESH_TOZERO); //しきい値=0.8
		for (int i=0;i<result.rows();i++) {
			for (int j=0;j<result.cols();j++) {
				if (result.get(i, j)[0] > 0) {
					System.out.println("i = " + i);
					System.out.println("j = " + j);
					Core.rectangle(src, new Point(j, i), new Point(j + white.cols(), i + white.rows()), new Scalar(0, 0, 255), 2);
				}
			}
		}*/
		
		/*
		 一つだけ見つける関数
		for(int i = 0;i < 10;i++){
			Imgproc.matchTemplate(src, white, result, Imgproc.TM_SQDIFF_NORMED);
			Core.MinMaxLocResult minr = Core.minMaxLoc(result);
			System.out.println("maxVal = " + minr.maxVal);
			System.out.println("maxLoc = " + minr.maxLoc);
			System.out.println("minVal = " + minr.minVal);
			System.out.println("minLoc = " + minr.minLoc);
			if(minr.maxVal < 1.0)break;
			Point maxp = minr.maxLoc;
			Point pt2 = new Point(maxp.x + white.width(), maxp.y + white.height());
			Core.rectangle(src, maxp, pt2, new Scalar(255,0,0), -1);
		}*/
		
		int[] white_p = getWhitePlace(src);
		
		for(int i = 0;i < white_p.length;i++){
			System.out.println("WHITE_P = " + white_p[i]);
			Core.line(src, new Point(0, white_p[i]), new Point(src.width(), white_p[i]), new Scalar(255,0,0), 1);
		}
		
		Highgui.imwrite(outpath, src);
		Highgui.imwrite(debugpath, white);
	}
	
	public static int[] getWhitePlace(Mat src){
		boolean flag = false;
		int[] whitePlace = new int[10];
		int whiteCount = 0;
		
		for(int i = 0;i < src.rows();i++){
			flag = false;
			for(int j = 0;j < src.cols();j++){
				double[] data =  src.get(i, j);
				if(data[0] == 255 && data[1] == 255 && data[2] == 255){
					flag = true;
				}
				if(!flag){
					break;
				}
			}
			if(flag){
				whitePlace[whiteCount] = i;
				whiteCount++;
				i += 100;
			}
		}
		
		int[] r = new int[whiteCount];
		for(int i = 0;i < whiteCount;i++){
			r[i] = whitePlace[i];
		}
		
		return r;
	}

}
