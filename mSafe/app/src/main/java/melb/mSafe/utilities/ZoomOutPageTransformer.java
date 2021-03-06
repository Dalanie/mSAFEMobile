package melb.mSafe.utilities;

import android.support.v4.view.ViewPager;
import android.view.View;

public class ZoomOutPageTransformer implements ViewPager.PageTransformer {

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();
        float tempPosition = position - 0.4f;

        if (tempPosition < -2) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);

        } else if (tempPosition <= 1) { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            float MIN_SCALE = 0.6f;
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(tempPosition));
            float vertMargin = pageHeight * (1 - scaleFactor) / 2;
            float horzMargin = pageWidth * (1 - scaleFactor) / 2;
            if (tempPosition < 0) {
                view.setTranslationX(horzMargin - vertMargin / 2);
            } else {
                view.setTranslationX(-horzMargin + vertMargin / 2);
            }

            // Scale the page down (between MIN_SCALE and 1)
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

            // Fade the page relative to its size.
            float MIN_ALPHA = 0.4f;
            view.setAlpha(MIN_ALPHA +
                    (scaleFactor - MIN_SCALE) /
                            (1 - MIN_SCALE) * (1 - MIN_ALPHA));

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }
}