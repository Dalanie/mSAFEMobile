package melb.mSafe;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

/**
 * Created by Daniel on 08.01.14.
 */
public class AudioManager implements
        TextToSpeech.OnInitListener{
    private static AudioManager _instance;
    private TextToSpeech tts;
    private int result=0;

    private AudioManager(Context context){
        tts = new TextToSpeech(context, this);
    }

    public static AudioManager getInstance(){
        return _instance;
    }

    public static void init(Context context){
        _instance = new AudioManager(context);
    }

    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
    //called when text to speech start
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            //set Language
            result = tts.setLanguage(Locale.US);
            // tts.setPitch(5); // set pitch level
            // tts.setSpeechRate(2); // set speech speed rate
        } else {
            Log.e("TTS", "Initilization Failed");
        }
    }

    public void speak(String text) {
        if(result==tts.setLanguage(Locale.US))
        {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    /**
     * Converts "distance" to speech
     * "~1,5 -> one and a half meters
     * ~0,5 -> half a meter
     * ~ 2 -> two meters
     * @param distance you wish to have in spoken language
     * @return result-string
     */
    public static String distanceToSpeech(double distance){
        int meter = (int) distance;
        int centimeter = (int) ((distance % 1) * 100);

        String centimeterString = "";
        boolean isHalf = false;
        boolean hasCentimeter = false;
        boolean hasMeter;

        if ((centimeter > 30) && (centimeter < 75)){
            //nearly half a meter
            centimeterString = "half";
            isHalf = true;
            hasCentimeter = true;
        }else if (centimeter < 30){
            centimeterString = "";
            hasCentimeter = false;
        }else if (centimeter > 75){
            meter++;
            centimeterString = "";
            hasCentimeter = false;
        }
        String resultString = "";
        if (meter > 0){
            hasMeter = true;
            if (hasCentimeter){
                  resultString = String.format("%s and a %s", meter, centimeterString);
            }else{
                resultString = meter + "";
            }
        }else{
            hasMeter = false;
            resultString = centimeterString;
        }
        String unit;
        if (hasMeter){
            unit = "meter";
            if (meter > 1 || hasCentimeter){
                unit = "meters";
            }
        }else{
            if (!hasCentimeter){
                return null;
            }
            unit = "a meter";
        }
        return String.format("%s %s", resultString, unit);
    }
}
