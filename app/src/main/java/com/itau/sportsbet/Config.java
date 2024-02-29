package com.itau.sportsbet;


import com.googlecode.tesseract.android.TessBaseAPI;

public class Config {

	//. define some global data types.


	//. for Image Processing.
	public static int Screen_Width = 0;
	public static int Screen_Height = 0;
	public static int IMAGE_WIDTH = 540;
	public static int IMAGE_HEIGHT = 960;
	public static float resizeXRatio = 0;
	public static float resizeYRatio = 0;

	public static int vscroll_unit = 600;
	public static String menu_more_countries  = "Thêm"; // "Thêm nhiều quốc gia"  // "Them nhieu quoc gia";
	public static String menu_fewer_countries = "quốc" ; // "Ít các quốc gia";  // "It cac quoc gia";

	public static int bet_item_height = 85;
	public static String input_unit = "K VND";
	public static String bet_btn = "Đat cuoc";
	public static String sports_viet = "Thể thao";

	public static float tesseractDetaultCharHeight = 40.0f;

	public static String betType_Handicap = "Handicap";
	public static String betType_TaiXiu = "TaiXiu";
	public static String betType_Europe1X2 = "Europe1X2";

	public static int CalcYSize(int ySize){
		return (int) (ySize / resizeYRatio);
	}
	public static int CalcXSize(int xSize){
		return (int) (xSize / resizeXRatio);
	}


	//. for tesseract ocr engine.
	public static int TESS_OCR_MODE = TessBaseAPI.OEM_LSTM_ONLY;
	public static String OCR_LANG = "vie";
	public static String OCR_MODEL_NAME = "vie.traineddata";
	public static String phoneId = "";

	public static String db_name = "SportBet";

}
