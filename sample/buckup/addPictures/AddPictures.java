package addPictures;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.highgui.Highgui;

public class AddPictures {
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		Mat m1 = new Mat();
		Mat m2 = new Mat();

		String path_in = "C:/Users/Tonegawa/Pictures/side_sample.jpg";
		String path_in2 = "C:/Users/Tonegawa/Pictures/sample001.jpg";
		String path_out = "C:/Users/Tonegawa/Pictures/combined_img.jpg";

		m1 = Highgui.imread(path_in);
		//m2 = m1.clone();
		m2 = Highgui.imread(path_in2);

		ArrayList<Mat> src = new ArrayList<Mat>();
		src.add(m1);
		src.add(m2);

		int maxWidth = (m1.width() > m2.width())?m1.width():m2.width();
		int totalHeight = m1.height() + m2.height();

		System.out.println("dim = " + m1.dims());

		//メインテスト
		//ロジックは完成
		Mat combined_img = new Mat(totalHeight, maxWidth, CvType.CV_8UC3);
		Rect roi = new Rect();
		roi.width = m1.cols();
		roi.height = m1.rows();
		Mat mRoi = new Mat(combined_img, roi);
		m1.copyTo(mRoi);
		roi.y += m1.rows();
		roi.width = m2.cols();
		roi.height = m2.rows();
		mRoi = new Mat(combined_img, roi);
		m2.copyTo(mRoi);
	     
		//これだと横幅がおんばじサイズの画像しか結合できない
		//Core.vconcat(src, combined_img);

		Highgui.imwrite(path_out, combined_img);
	}

}
