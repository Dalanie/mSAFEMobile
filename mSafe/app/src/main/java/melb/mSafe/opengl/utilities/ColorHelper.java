package melb.mSafe.opengl.utilities;

/**
 * Created by Daniel on 23.01.14.
 */
public class ColorHelper {
    public static float[] convert255ColorToGLColor(float... color){
        if (color != null){
            for (int i = 0; i < color.length; i++){
                color[i] = color[i] / 255f;
            }
        }
        return color;
    }
}
