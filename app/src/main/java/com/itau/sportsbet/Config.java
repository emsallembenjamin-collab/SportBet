package com.itau.sportsbet;


import com.googlecode.tesseract.android.TessBaseAPI;

public class Config {

	//. define some global data types.
	enum StrCompMethod {
		e_PermitIncluding,	// 0
		e_IncludedBehind,
		e_BriefType,
		e_ExactEqual;

		public static StrCompMethod fromInteger(int x) {
			switch(x) {
				case 0:
					return e_PermitIncluding;
				case 1:
					return e_IncludedBehind;
				case 2:
					return e_BriefType;
				case 3:
					return e_ExactEqual;
			}
			return null;
		}

		public static int toInteger(StrCompMethod eType) {
			switch(eType) {
				case e_PermitIncluding:
					return 0;
				case e_IncludedBehind:
					return 1;
				case e_BriefType:
					return 2;
				case e_ExactEqual:
					return 3;
			}
			return -1;
		}
	};

	enum StrPreprocessMethod {
		e_removeSpace,	// 0
		e_removeNonLetters,
		e_removeNonAlphanumeric,
		e_caseNumberic,
		e_noneProc;

		public static StrPreprocessMethod fromInteger(int x) {
			switch(x) {
				case 0:
					return e_removeSpace;
				case 1:
					return e_removeNonLetters;
				case 2:
					return e_removeNonAlphanumeric;
				case 3:
					return e_caseNumberic;
				case 4:
					return e_noneProc;
			}
			return null;
		}

		public static int toInteger(StrPreprocessMethod eType) {
			switch(eType) {
				case e_removeSpace:
					return 0;
				case e_removeNonLetters:
					return 1;
				case e_removeNonAlphanumeric:
					return 2;
				case e_caseNumberic:
					return 3;
				case e_noneProc:
					return 4;
			}
			return -1;
		}

	};

	enum OcrPattern {
		e_NormalPattern,	// 0
		e_DigitOnly;

		public static OcrPattern fromInteger(int x) {
			switch(x) {
				case 0:
					return e_NormalPattern;
				case 1:
					return e_DigitOnly;
			}
			return null;
		}

		public static int toInteger(OcrPattern eType) {
			switch(eType) {
				case e_NormalPattern:
					return 0;
				case e_DigitOnly:
					return 1;
			}
			return -1;
		}
	};

	enum TextDetMode {
		e_NormalTxtDet,	// 0
		e_HasMarkFront;

		public static TextDetMode fromInteger(int x) {
			switch(x) {
				case 0:
					return e_NormalTxtDet;
				case 1:
					return e_HasMarkFront;
			}
			return null;
		}

		public static int toInteger(TextDetMode eType) {
			switch(eType) {
				case e_NormalTxtDet:
					return 0;
				case e_HasMarkFront:
					return 1;
			}
			return -1;
		}
	};

	enum IgnorePartMode {
		e_NormalIgnore,	// 0
		e_IgnoreMode1;

		public static IgnorePartMode fromInteger(int x) {
			switch(x) {
				case 0:
					return e_NormalIgnore;
				case 1:
					return e_IgnoreMode1;
			}
			return null;
		}

		public static int toInteger(IgnorePartMode eType) {
			switch(eType) {
				case e_NormalIgnore:
					return 0;
				case e_IgnoreMode1:
					return 1;
			}
			return -1;
		}
	};



	enum DoConfirmMode {
		e_Before,			// 0
		e_BeforeValidator,	// 1
		e_AfterDone;		// 2

		public static DoConfirmMode fromInteger(int x) {
			switch(x) {
				case 0:
					return e_Before;
				case 1:
					return e_BeforeValidator;
				case 2:
					return e_AfterDone;
			}
			return null;
		}

		public static int toInteger(DoConfirmMode eType) {
			switch(eType) {
				case e_Before:
					return 0;
				case e_BeforeValidator:
					return 1;
				case e_AfterDone:
					return 2;
			}
			return -1;
		}
	};

	enum NeighborCond2Targets {
		e_UpDownDenseNeighborCond,			// 0
		e_FarHorizNeighborCond,				// 1
		e_Merge2TargetNeighborCond,			// 2
		e_FarVerticalNeighborCond,			// 3
		e_TableTypeNeighborCond;			// 4

		public static NeighborCond2Targets fromInteger(int x) {
			switch(x) {
				case 0:
					return e_UpDownDenseNeighborCond;
				case 1:
					return e_FarHorizNeighborCond;
				case 2:
					return e_Merge2TargetNeighborCond;
				case 3:
					return e_FarVerticalNeighborCond;
				case 4:
					return e_TableTypeNeighborCond;
			}
			return null;
		}

		public static int toInteger(NeighborCond2Targets eType) {
			switch(eType) {
				case e_UpDownDenseNeighborCond:
					return 0;
				case e_FarHorizNeighborCond:
					return 1;
				case e_Merge2TargetNeighborCond:
					return 2;
				case e_FarVerticalNeighborCond:
					return 3;
				case e_TableTypeNeighborCond:
					return 4;
			}
			return -1;
		}
	};




	//. for Image Processing.
	public static int Screen_Width = 0;
	public static int Screen_Height = 0;
	public static int IMAGE_WIDTH = 540;
	public static int IMAGE_HEIGHT = 960;
	public static float resizeXRatio = 0;
	public static float resizeYRatio = 0;

	public static int   default_limitTime = 90000;	// ms unit...

	public static int vscroll_unit = 600;
	public static String menu_more_countries  = "Thêm"; // "Thêm nhiều quốc gia"  // "Them nhieu quoc gia";
	public static String menu_fewer_countries = "quốc" ; // "Ít các quốc gia";  // "It cac quoc gia";

	public static int bet_item_height = 85;
	public static String input_unit = "K VND";
	public static String bet_btn = "Đặt cược";
	public static String sports_viet = "Thể thao";

	//pgh public static float tesseractDetaultCharHeight = 70.0f;
	public static int max_userid_password_len = 10;

	public static String betType_Handicap = "Keo chap";
	public static String betType_TaiXiu = "Tai/Xiu";
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
