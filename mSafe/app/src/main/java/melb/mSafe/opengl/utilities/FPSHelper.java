package melb.mSafe.opengl.utilities;

/**
 * Created by Daniel on 24.01.14.
 */
public class FPSHelper {
    private static int mFPS;
    private static int lastMFPS;
    private static long mLastTime;
    private static int FPS_DEFAULT = 25;
    public static int maxFps = 25;

    public static final boolean SHOW_FPS = false;

    public static void calculateFPS() {
        if (SHOW_FPS) {
            System.out.println("FPS: " + lastMFPS);
            long currentTime = System.currentTimeMillis();
            mFPS++;
            if (currentTime - mLastTime >= 1000) {
                lastMFPS = mFPS;
                mFPS = 0;
                mLastTime = currentTime;
            }
        }
    }

    private static long startTime = System.currentTimeMillis();

    public static void limitFPS() {
        calculateFPS();
        long endTime = System.currentTimeMillis();
        long dt = endTime - startTime;
        if (dt < 33){
            try {
                Thread.sleep((1000/maxFps)- dt);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        startTime = System.currentTimeMillis();
    }

    public static void pauseFPS() {
       maxFps = 1;
    }
    public static void resumeFPS(){
        maxFps = FPS_DEFAULT;
    }
}
