package komaDetect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import findcontours.FindContoursTest.Koma;

public class KomaDetect {
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	static int komaCount = 0;

	public static void main(String[] args){
		//検出したコマをここに入れる。
		//添え字はグローバル変数に記載
		Koma[] koma = new Koma[20];

		String path_in = "C:/Users/Tonegawa/Pictures/fig1.jpg";
		String path_edge_out = "C:/Users/Tonegawa/Pictures/KomaDetect/edges_out.jpg";
		String path_gray_out = "C:/Users/Tonegawa/Pictures/KomaDetect/gray_out.jpg";
		String path_matContours_out = "C:/Users/Tonegawa/Pictures/KomaDetect/contours_out.jpg";
		String path_line_out = "C:/Users/Tonegawa/Pictures/KomaDetect/line_out.jpg";
		String path_line_out2 = "C:/Users/Tonegawa/Pictures/KomaDetect/line_out2.jpg";
		String path_rect_out = "C:/Users/Tonegawa/Pictures/KomaDetect/rect_out.jpg";
		String path_combined_image_out = "C:/Users/Tonegawa/Pictures/KomaDetect/combined_image.jpg";

		String path_debug_out = "C:/Users/Tonegawa/Pictures/KomaDetect/debug.jpg";

		Mat src = new Mat();
		Mat gray = new Mat();
		Mat edges = new Mat();
		Mat lines = new Mat();			//線分検出の入れ子, 画像としては使わない
		Mat matContours = new Mat();	//輪郭検出画像
		Mat hierarchy = new Mat();
		Mat white = new Mat();			//線分を描画するためのオブジェクト
		Mat white2 = new Mat();			//伸ばした線分を描画するためのオブジェクト
		Mat rect = new Mat();			//Rectangle drown

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>(100);

		//線分検出法で用いる輪郭情報
		double[][] doubleContours;

		src = Highgui.imread(path_in);
		matContours = src.clone();
		white = src.clone();
		white.setTo(new Scalar(255, 255, 255));
		white2 = white.clone();
		rect = white.clone();
		Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY); // カラー画像をグレー画像に変換
		Imgproc.Canny(gray, edges, 100, 200);				//エッジ検出
		Mat edges2 = edges.clone();

		//輪郭検出
		Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		//輪郭描画
		Imgproc.drawContours(matContours,contours,-1,new Scalar(255,0,0),1);

		//コマ検出(輪郭検出法), 検出されたコマは引数3のkoma配列に格納される。
		mangaDetect(src, contours, koma);

		Imgproc.HoughLinesP(edges2, lines, 1, Math.PI / 180, 100, 100, 5);	//直線検出
		fncDrwLine(lines, white);									//直線描画
		System.out.println("lines.size() = " + lines.size());
		doubleContours = fncDrwLine(lines, white);

		int[] size = endLineSize(src, doubleContours);
		double[][] x0EndLine =  findLineX0End(src, doubleContours);
		double[][] y0EndLine = findLineY0End(src, doubleContours);
		double[][] xMaxEndLine = findLineXMAXEnd(src, doubleContours);
		double[][] yMaxEndLine = findLineYMAXEnd(src, doubleContours);			//各辺の端点情報

		//線分における縦と横がまっすぐな線のみを抽出して描画する
		drwLineEnd(white2, x0EndLine, xMaxEndLine, y0EndLine, yMaxEndLine);

		//rectは白い画像であることを前提とする
		//rectに輪郭検出法で検出したコマをぬりつぶす
		for(int i = 0;i < komaCount;i++){
			Core.rectangle(rect, koma[i].getMinPoint(), koma[i].getMaxPoint(), new Scalar(255,0,0), -1);
		}

		//線分検出手法によるコマ抽出
		//rectには塗りつぶされた画像を格納してある
		cutEndLineRect(rect ,src, koma, x0EndLine, xMaxEndLine, y0EndLine, yMaxEndLine);

		Highgui.imwrite(path_gray_out, gray);				// 出力画像を保存
		Highgui.imwrite(path_edge_out, edges);				//エッジ画像の保存
		Highgui.imwrite(path_matContours_out, matContours);
		Highgui.imwrite(path_line_out, white);
		Highgui.imwrite(path_line_out2, white2);
		Highgui.imwrite(path_rect_out, rect);

		//コマのソート

		//コマの画像の保存
		Integer name = 0;
		String filename;
    	for(int i = 0;i < komaCount;i++){
    		//debug
    		System.out.println("koma[i].getMinPoint() = " + koma[i].getMinPoint());
    		System.out.println("koma[i].getWidth() = " +  koma[i].getWidth());
    		System.out.println("koma[i].getHigh() = " +  koma[i].getHigh());

    		filename = "DetectPicture" + name.toString();
    		fncCutImageRect(src, koma[i].getMinPoint(), koma[i].getWidth(), koma[i].getHigh(), filename);
    		name++;
    	}
    	
    	//debug for powerpoint
		for(int i = 0;i < komaCount;i++){
			Core.rectangle(rect, koma[i].getMinPoint(), koma[i].getMaxPoint(), new Scalar(255,0,0), -1);
		}
		Highgui.imwrite(path_rect_out, rect);

    	//画像の結合
    	//画像サイズの設定
    	/*int maxWidth = 0;
		int totalHeight = 0;
		for(int i = 0;i < komaCount;i++){
			totalHeight += koma[i].getHigh();
			if(maxWidth  < koma[i].getWidth()){
				maxWidth = koma[i].getWidth();
			}
		}
		//縦に結合
		Mat combined_img = new Mat(totalHeight, maxWidth, CvType.CV_8UC3);
		Rect roi = new Rect();
		for(int i = 0;i < komaCount;i++){
			roi.width = koma[i].getWidth();
			roi.height = koma[i].getHigh();
			//このfor文内で画像を切り抜いている
			//KomaクラスのgetImageではうまくいかなかった
			Rect sroi = new Rect(koma[i].getMinPointX(), koma[i].getMinPointY(), koma[i].getWidth(), koma[i].getHigh());
			Mat img2 = new Mat(src, sroi);		//切り抜いた画像（コマ）
			Mat mRoi = new Mat(combined_img, roi);
			img2.copyTo(mRoi);
			roi.y += koma[i].getHigh();
		}
		*/
		Mat combine = new Mat();
		combine = pictureCombine(src, koma);

		//debug
		Highgui.imwrite(path_debug_out, koma[1].getImage());
		//main
		Highgui.imwrite(path_combined_image_out, combine);
	}

	//縦と横の線のみを描画するメソッド
	//同時に縦と横の線をdouble型の二次元配列で返す
	private static double[][] fncDrwLine(Mat line, Mat img) {
		double[] data;
		double[][] contours = new double[line.cols()][4];
		Point pt1 = new Point();
		Point pt2 = new Point();
		int count = 0;
		System.out.println(line.cols());
		for (int i = 0; i < line.cols(); i++){
			data = line.get(0, i);
			pt1.x = data[0];
			pt1.y = data[1];
			pt2.x = data[2];
			pt2.y = data[3];

			if(pt1.x == pt2.x || pt1.y == pt2.y){
				Core.line(img, pt1, pt2, new Scalar(255,0,0), 1);
				contours[count][0]  = pt1.x;
				contours[count][1]  = pt1.y;
				contours[count][2]  = pt2.x;
				contours[count][3]  = pt2.y;
				//debug
				System.out.print("(" + contours[count][0] + ",");
				System.out.print(contours[count][1] + "), ");
				System.out.print("(" + contours[count][2] + ",");
				System.out.println(contours[count][3] + ")");
				count++;
			}
		}
		System.out.println(contours.length);
		System.out.println(count);
		System.out.println();
		double[][] returnContours = new double[count][4];

		for(int i = 0;i < count;i++){
			for(int j = 0;j < 4;j++){
				returnContours[i][j] = contours[i][j];
			}
		}

		for(int i = 0;i < count;i++){
			System.out.print("(" + returnContours[i][0] + ",");
			System.out.print(returnContours[i][1] + "), ");
			System.out.print("(" + returnContours[i][2] + ",");
			System.out.println(returnContours[i][3] + ")");
		}

		return returnContours;
	}

	//線分の本数を数えるメソッド
	public static int[] endLineSize(Mat src, double contours[][]){
		int[] size = {0, 0, 0, 0};
		int XMAX = src.width();
		int YMAX = src.height();

		for(int i = 0;i  < contours.length;i++){
			if(contours[i][0] <= 5){
				size[0]++;
			}
			if(contours[i][3] <= 5){
				size[1]++;
			}
			if(contours[i][2] >= XMAX-5 ){
				size[2]++;
			}
			if(YMAX-5 <= contours[i][1]){
				size[3]++;
			}
		}
		return size;
	}

    //コマ検出
    //FindContoursにより輪郭を検出する
	//引数1には切り出す画像(漫画の画像)を入力
    public static int mangaDetect(Mat src, List<MatOfPoint> contours, Koma[] koma){
    	Mat m = new Mat();
    	Point pt1 = new Point();
    	Integer count = komaCount;
    	int high;
    	int width;

    	for(int i = 0;i < contours.size();i++){
    		Point max_pt = new Point();
    		Point min_pt = new Point();
    		//輪郭の集合
    		m = contours.get(i);
    		//System.out.println(m.size());
    		if(Imgproc.contourArea(m) > 30000){
    			max_pt.x = m.get(0,0)[0];
    			max_pt.y = m.get(0,0)[1];
    			min_pt.x = m.get(0,0)[0];
    			min_pt.y = m.get(0,0)[1];

    			//ひとつの輪郭に含まれる点を表示
    			//最小点と最大点を求める
    			for(int j = 0;j < m.rows();j++){
    				pt1.x = m.get(j,0)[0];
    				pt1.y = m.get(j,0)[1];

    				//ROIの点を見つける。
    				if(pt1.x > max_pt.x)
    					max_pt.x = pt1.x;
    				if(pt1.y > max_pt.y)
    					max_pt.y = pt1.y;
    				if(pt1.x < min_pt.x)
    					min_pt.x = pt1.x;
    				if(pt1.y < min_pt.y)
    					min_pt.y = pt1.y;
    			}
    			//デバッグ用
    			System.out.print("min_pt = (" + min_pt.x + ", ");
    			System.out.println(min_pt.y + ")");
    			System.out.print("max_pt = (" + max_pt.x + ", ");
    			System.out.println(max_pt.y + ")");

    			high = (int) (max_pt.y - min_pt.y);
    			width = (int) (max_pt.x - min_pt.x);

    			koma[count] = new Koma(src, max_pt, min_pt);
    			count++;
    			komaCount++;
    		}
    	}
    	//sort
    	Arrays.sort(koma, 0, count, new SampleComparator());

    	//4koma sort
    	//Arrays.sort(koma, 0, count, new XYComparator());

    	//デバッグ
    	System.out.println("after sort");
    	for(int i = 0;i < count;i++){
    		//System.out.println("i = " + i);
    		System.out.print("MinPoint = " + koma[i].getMinPoint());
    		System.out.println("  MaxPoint = " + koma[i].getMaxPoint());
    	}

    	Integer name = 0;
    	for(int i = 0;i < count;i++){
    		fncCutImageRect(src, koma[i].getMinPoint(), koma[i].getWidth(), koma[i].getHigh(), name.toString());
    		name++;
    	}

    	System.out.println("Count = " + count);
    	return count;
    }

	public static double[][] findLineX0End(Mat src, double contours[][]){
		int[] size = endLineSize(src, contours);
		double[][] x0EndPointfull = new double[size[0]][4];

		int XMAX = src.width();
		int YMAX = src.height();
		int x0count = 0;

		for(int i = 0;i  < contours.length;i++){
			//x0部
			if(contours[i][0] <= 5){
				//線分がかぶっているかどうかの処理
				//よさげ
				boolean x0flag = false;
				for(int k = 0;k < x0count;k++){
					//かぶっているかどうか
					x0flag = (contours[i][1] <= x0EndPointfull[k][1] + 5 && contours[i][1] >= x0EndPointfull[k][1] - 5);
					if(x0flag) break;
				}
				//かぶっていない場合の処理
				//追加する
				if(!x0flag){
					for(int j = 0;j < 4;j++){
						x0EndPointfull[x0count][j] = contours[i][j];
					}
					x0count++;
				}
			}
		}

		double[][] x0EndPoint = new double[x0count][4];
		for(int i = 0;i < x0count;i++){
			x0EndPoint[i] = x0EndPointfull[i];
		}

		return x0EndPoint;
	}

	public static double[][] findLineY0End(Mat src, double contours[][]){
		int[] size = endLineSize(src, contours);
		double[][] y0EndPointfull = new double[size[1]][4];

		int XMAX = src.width();
		int YMAX = src.height();
		int y0count = 0;

		for(int i = 0;i  < contours.length;i++){
			//y0
			if(contours[i][3] <= 5){
				//線分がかぶっているかどうかの処理
				//よさげ
				boolean y0flag = false;
				for(int k = 0;k < y0count;k++){
					//かぶっているかどうか
					y0flag = (contours[i][0] <= y0EndPointfull[k][0] + 5 && contours[i][0] >= y0EndPointfull[k][0] - 5);
					if(y0flag) break;
				}
				//かぶっていない場合の処理
				//追加する
				if(!y0flag){
					for(int j = 0;j < 4;j++){
						y0EndPointfull[y0count][j] = contours[i][j];
					}
					y0count++;
				}
			}
		}

		double[][] y0EndPoint = new double[y0count][4];
		for(int i = 0;i < y0count;i++){
			y0EndPoint[i] = y0EndPointfull[i];
		}

		return y0EndPoint;
	}

	public static double[][] findLineXMAXEnd(Mat src, double contours[][]){
		int[] size = endLineSize(src, contours);
		double[][] xMaxEndPointfull = new double[size[2]][4];

		int XMAX = src.width();
		int YMAX = src.height();
		int xMaxcount = 0;

		for(int i = 0;i  < contours.length;i++){
			//xMax
			if(contours[i][2] >= XMAX-5 ){
				//線分がかぶっているかどうかの処理
				//よさげ
				boolean xMaxflag = false;
				for(int k = 0;k < xMaxcount;k++){
					//かぶっているかどうか
					xMaxflag = (contours[i][1] <= xMaxEndPointfull[k][1] + 5 && contours[i][1] >= xMaxEndPointfull[k][1] - 5);
					if(xMaxflag) break;
				}
				//かぶっていない場合の処理
				//追加する
				if(!xMaxflag){
					for(int j = 0;j < 4;j++){
						xMaxEndPointfull[xMaxcount][j] = contours[i][j];
					}
					xMaxcount++;
				}
			}
		}

		double[][] xMaxEndPoint = new double[xMaxcount][4];
		for(int i = 0;i < xMaxcount;i++){
			xMaxEndPoint[i] = xMaxEndPointfull[i];
		}

		return xMaxEndPoint;
	}

	public static double[][] findLineYMAXEnd(Mat src, double contours[][]){
		int[] size = endLineSize(src, contours);
		double[][] yMaxEndPointfull = new double[size[3]][4];

		int XMAX = src.width();
		int YMAX = src.height();
		int yMaxcount = 0;

		for(int i = 0;i  < contours.length;i++){
			//yMax
			if(YMAX-5 <= contours[i][1]){
				//線分がかぶっているかどうかの処理
				//よさげ
				boolean yMaxflag = false;
				for(int k = 0;k < yMaxcount;k++){
					//かぶっているかどうか
					yMaxflag = (contours[i][0] <= yMaxEndPointfull[k][0] + 5 && contours[i][0] >= yMaxEndPointfull[k][0] - 5);
					if(yMaxflag) break;
				}
				//かぶっていない場合の処理
				//追加する
				if(!yMaxflag){
					for(int j = 0;j < 4;j++){
						yMaxEndPointfull[yMaxcount][j] = contours[i][j];
					}
					yMaxcount++;
				}
			}
		}

		double[][] yMaxEndPoint = new double[yMaxcount][4];
		for(int i = 0;i < yMaxcount;i++){
			yMaxEndPoint[i] = yMaxEndPointfull[i];
		}

		return yMaxEndPoint;
	}

	//引数1は塗りつぶし画像
	//引数2は切り出す画像, 漫画の画像で良い.
	public static void cutEndLineRect(Mat src, Mat img, Koma[] koma, double[][] x0EndPoint, double[][] xMaxEndPoint, double[][] y0EndPoint, double[][] yMaxEndPoint){
		int XMAX = src.width();
		int YMAX = src.height();
		double[] data = new double[3];
		boolean colorFlag = true;
		int colorCount  = 0;

		Mat img_rect;

		//Koma[] koma  =new Koma[10];

		Point left_up = new Point(0,0);
		Point left_down = new Point(0,YMAX);
		Point right_up = new Point(XMAX,0);
		Point right_down = new Point(XMAX,YMAX);

		// leftup
		// bug無し
		// このまま他のも実装
		String fileSpaceName = "LeftUP_Space";
		String filename;
		for(Integer i = 0;i < x0EndPoint.length;i++){
			for(Integer j = 0;j < y0EndPoint.length;j++){
				filename = fileSpaceName + i.toString() + j.toString();
				img_rect = fncCutImageRect(src, left_up, (int)y0EndPoint[j][0], (int)x0EndPoint[i][1]);

				//color find
				for(int h = 10;h < img_rect.rows() -10;h++){
					for(int k = 10;k < img_rect.cols() -10;k++){
						data = img_rect.get(h, k);

						if(data[0] >= 250 && data[1] == 0 && data[2] == 0){
							colorCount++;
						}
						if(colorCount == 100){
							colorFlag = false;
							break;
						}
					}
					if(!colorFlag) break;
				}
				if(colorFlag){
					fncCutImageRect(img, left_up, (int)y0EndPoint[j][0], (int)x0EndPoint[i][1], filename);
					koma[komaCount] = new Koma(img, new Point(left_up.x + y0EndPoint[j][0], left_up.y + x0EndPoint[i][1]), left_up);
					komaCount++;
				}
				colorFlag = true;
				colorCount = 0;
			}
		}

		// leftdown
		// 線分検出に改善の余地あり
		fileSpaceName = "LeftDown_Space";
		Point left_down_start;
		colorFlag = true;
		for(Integer i = 0;i < yMaxEndPoint.length;i++){
			for(Integer j = 0;j < x0EndPoint.length;j++){
				filename = fileSpaceName + i.toString() + j.toString();
				left_down_start = new Point(left_down.x, x0EndPoint[j][1]);
				img_rect = fncCutImageRect(src, left_down_start, (int)yMaxEndPoint[i][0], YMAX - (int)x0EndPoint[j][1]);

				//color find
				for(int h = 10;h < img_rect.rows()-10;h++){
					for(int k = 10;k < img_rect.cols()-10;k++){
						data = img_rect.get(h, k);

						//青色があるときの処理
						if(data[0] >= 250 && data[1] == 0 && data[2] == 0)
							colorCount++;
						if(colorCount == 100){
							colorFlag = false;
							break;
						}

					}
					if(!colorFlag) break;
				}
				if(colorFlag){
					fncCutImageRect(img, left_down_start, (int)yMaxEndPoint[i][0], YMAX - (int)x0EndPoint[j][1], filename);
					koma[komaCount] = new Koma(img, new Point(left_down_start.x + yMaxEndPoint[i][0], left_down_start.y + YMAX - (int)x0EndPoint[j][1]), left_down_start);
					komaCount++;
				}
				colorFlag = true;
				colorCount = 0;
			}
		}

		// Right_up
		fileSpaceName = "RightUP_Space";
		Point right_up_start;
		colorFlag = true;
		for(Integer i = 0;i < xMaxEndPoint.length;i++){
			for(Integer j = 0;j < y0EndPoint.length;j++){
				filename = fileSpaceName + i.toString() + j.toString();
				right_up_start = new Point(y0EndPoint[j][0], 0);
				img_rect = fncCutImageRect(src, right_up_start, XMAX - (int)y0EndPoint[j][0], (int)xMaxEndPoint[i][1]);

				//color find
				for(int h = 10;h < img_rect.rows() -10;h++){
					for(int k = 10;k < img_rect.cols() -10;k++){
						data = img_rect.get(h, k);

						if(data[0] >= 250 && data[1] == 0 && data[2] == 0){
							colorCount++;
						}
						if(colorCount == 100){
							colorFlag = false;
							break;
						}
					}
					if(!colorFlag) break;
				}
				if(colorFlag){
					fncCutImageRect(img, right_up_start, XMAX - (int)y0EndPoint[j][0], (int)xMaxEndPoint[i][1], filename);
					koma[komaCount] = new Koma(img, new Point(right_up_start.x + XMAX - (int)y0EndPoint[j][0], right_up_start.y + (int)xMaxEndPoint[j][1]), right_up_start);
					komaCount++;
				}
				colorFlag = true;
				colorCount = 0;
			}
		}

		//Right_down
		fileSpaceName = "RightDown_Space";
		Point right_down_start;
		colorFlag = true;
		for(Integer i = 0;i < xMaxEndPoint.length;i++){
			for(Integer j = 0;j < yMaxEndPoint.length;j++){
				filename = fileSpaceName + i.toString() + j.toString();
				right_down_start = new Point(yMaxEndPoint[j][0], xMaxEndPoint[i][1]);
				img_rect = fncCutImageRect(src, right_down_start, XMAX - (int)yMaxEndPoint[j][0], YMAX - (int)xMaxEndPoint[i][1]);

				//color find
				for(int h = 10;h < img_rect.rows() -10;h++){
					for(int k = 10;k < img_rect.cols() -10;k++){
						data = img_rect.get(h, k);

						if(data[0] >= 250 && data[1] == 0 && data[2] == 0){
							colorCount++;
						}
						if(colorCount == 100){
							colorFlag = false;
							break;
						}
					}
					if(!colorFlag) break;
				}
				if(colorFlag){
					fncCutImageRect(img, right_down_start, XMAX - (int)yMaxEndPoint[j][0], YMAX - (int)xMaxEndPoint[i][1], filename);
					koma[komaCount] = new Koma(img, new Point(right_down_start.x + XMAX - (int)yMaxEndPoint[j][0], right_down_start.y + YMAX - (int)xMaxEndPoint[i][1]), right_down_start);
					komaCount++;
				}
				colorFlag = true;
				colorCount = 0;
			}
		}
	}

	//線分を伸ばして描画する関数
	public static void drwLineEnd(Mat src, double[][] x0EndPoint, double[][] xMaxEndPoint, double[][] y0EndPoint, double[][] yMaxEndPoint){
		int YMAX = src.height();
		int XMAX = src.width();

		System.out.println("x0EndPoint.length =  " + x0EndPoint.length);

		for(int i = 0;i < x0EndPoint.length;i++){
				Core.line(src, new Point(0, x0EndPoint[i][1]), new Point(XMAX,  x0EndPoint[i][3]), new Scalar(255,0,0), 1);
		}
		for(int i = 0;i < xMaxEndPoint.length;i++){
			Core.line(src, new Point(0, xMaxEndPoint[i][1]), new Point(XMAX,  xMaxEndPoint[i][3]), new Scalar(255,0,0), 1);
		}
		for(int i = 0;i < y0EndPoint.length;i++){
			Core.line(src, new Point(y0EndPoint[i][0],0), new Point(y0EndPoint[i][2], YMAX), new Scalar(255,0,0), 1);
		}
		for(int i = 0;i < yMaxEndPoint.length;i++){
			System.out.println();
			Core.line(src, new Point(yMaxEndPoint[i][0],0), new Point(yMaxEndPoint[i][0], YMAX), new Scalar(255,0,0), 1);
		}
	}

	//長方形切抜き
	private static Mat fncCutImageRect(Mat img, Point pt1, int w,int h,String filename){
		Rect roi = new Rect((int)pt1.x, (int)pt1.y, w, h);
		Mat img2 = new Mat(img, roi);
		String s = new String("C:/Users/Tonegawa/Pictures/KomaDetect/" + filename + ".jpg");
		Highgui.imwrite(s,img2);
		System.out.println(filename + ".jpg save success");
		return img2;
	}

	private static Mat fncCutImageRect(Mat img, Point pt1, int w,int h){
		Rect roi = new Rect((int)pt1.x, (int)pt1.y, w, h);
		Mat img2 = new Mat(img, roi);
		//String s = new String("C:/Users/Tonegawa/Pictures/manga/" + filename + ".jpg");
		//Highgui.imwrite(s,img2);
		//System.out.println(filename + ".jpg save success");
		return img2;
	}
	
	private static Mat pictureCombine(Mat src, Koma[] koma){
    	//画像の結合
    	//画像サイズの設定
    	int maxWidth = 0;
		int totalHeight = 0;
		for(int i = 0;i < komaCount;i++){
			totalHeight += koma[i].getHigh();
			if(maxWidth  < koma[i].getWidth()){
				maxWidth = koma[i].getWidth();
			}
		}
		//縦に結合
		Mat combined_img = new Mat(totalHeight, maxWidth, CvType.CV_8UC3);
		Rect roi = new Rect();
		for(int i = 0;i < komaCount;i++){
			roi.width = koma[i].getWidth();
			roi.height = koma[i].getHigh();
			//このfor文内で画像を切り抜いている
			//KomaクラスのgetImageではうまくいかなかった
			Rect sroi = new Rect(koma[i].getMinPointX(), koma[i].getMinPointY(), koma[i].getWidth(), koma[i].getHigh());
			Mat img2 = new Mat(src, sroi);		//切り抜いた画像（コマ）
			Mat mRoi = new Mat(combined_img, roi);
			img2.copyTo(mRoi);
			roi.y += koma[i].getHigh();
		}
		
		return combined_img;
	}
    //sortのための比較
    public static class SampleComparator implements Comparator<Koma>{
    	public int compare(Koma koma1, Koma koma2) {
    		int k = koma1.getMinPointY() - koma2.getMinPointY();
    		if(k <= 20 && k >= -20)
    			return  koma2.getMinPointX() - koma1.getMinPointX();

    		return k;
    	}
    }

    //4コマ漫画用comparator
    public static class XYComparator implements Comparator<Koma>{
    	public int compare(Koma koma1, Koma koma2) {
    		int k = koma2.getMinPointX() - koma1.getMinPointX();
    		if(k <= 20 && k >= -20)
    			return   koma1.getMinPointY() - koma2.getMinPointY();

    		return k;
    	}
    }

}