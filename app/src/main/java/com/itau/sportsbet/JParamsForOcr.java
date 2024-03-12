package com.itau.sportsbet;


import static com.itau.sportsbet.Config.IgnorePartMode.e_IgnoreMode1;
import static com.itau.sportsbet.Config.IgnorePartMode.e_NormalIgnore;
import static com.itau.sportsbet.Config.OcrPattern.e_DigitOnly;
import static com.itau.sportsbet.Config.OcrPattern.e_NormalPattern;
import static com.itau.sportsbet.Config.StrCompMethod.e_ExactEqual;
import static com.itau.sportsbet.Config.StrCompMethod.e_IncludedBehind;
import static com.itau.sportsbet.Config.StrCompMethod.e_PermitIncluding;
import static com.itau.sportsbet.Config.StrPreprocessMethod.e_caseNumberic;
import static com.itau.sportsbet.Config.StrPreprocessMethod.e_removeNonAlphanumeric;
import static com.itau.sportsbet.Config.StrPreprocessMethod.e_removeSpace;
import static com.itau.sportsbet.Config.TextDetMode.e_NormalTxtDet;

//.*=======================================================================
//.class: JParamsForTextDet
//.desc:
//.
class JParamsForTextDet {
    public Config.TextDetMode textDetMode = e_NormalTxtDet;
    public Config.IgnorePartMode ignorePartMode = e_NormalIgnore;

    public static JParamsForTextDet fromInteger(int x) {
        JParamsForTextDet retMode = null;
        switch(x) {
            case 0:
                retMode = new JParamsForTextDet();
                break;
            case 1:
                retMode = new JParamsForTextDet();
                retMode.ignorePartMode = e_IgnoreMode1;
                break;
        }
        return retMode;
    }

}


//.*=======================================================================
//.class: JParamsForOcr
//.desc:
//.
public class JParamsForOcr {

    public Config.OcrPattern ocrPattern             = e_NormalPattern;
    public Config.StrCompMethod strCompMethod       = e_PermitIncluding;
    public Config.StrPreprocessMethod strPreprocessMethod = e_removeSpace;
    public float                fResizeRate         = 2.0f;
    public boolean              bNeedMoreContrast   = false;
    public float                alphaForMoreContrast = 2.0f;


    public static JParamsForOcr fromInteger(int x) {
        JParamsForOcr retMode = null;
        switch(x) {
            case 0:
                retMode = new JParamsForOcr();
                break;
            case 1:
                retMode = new JParamsForOcr();
                retMode.strCompMethod = e_PermitIncluding;
                break;
            case 2:
                retMode = new JParamsForOcr();
                retMode.strCompMethod = e_IncludedBehind;
                break;
            case 3:
                retMode = new JParamsForOcr();
                retMode.strCompMethod = e_ExactEqual;
                retMode.strPreprocessMethod = e_removeNonAlphanumeric;
                break;
            case 4:
                retMode = new JParamsForOcr();
                retMode.strCompMethod = e_ExactEqual;
                retMode.strPreprocessMethod = e_removeNonAlphanumeric;
                break;
            case 5:     //. for number pannel...
                retMode = new JParamsForOcr();
                retMode.ocrPattern = e_DigitOnly;
                retMode.strCompMethod = e_ExactEqual;
                retMode.strPreprocessMethod = e_caseNumberic;
                retMode.fResizeRate = 2.0f;
                retMode.bNeedMoreContrast = true;
                break;
        }
        return retMode;
    }
}
