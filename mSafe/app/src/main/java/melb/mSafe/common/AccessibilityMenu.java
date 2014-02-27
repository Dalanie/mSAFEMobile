package melb.mSafe.common;

import melb.mSafe.model.Accessibility;

/**
 * Created by Daniel on 13.01.14.
 */
public class AccessibilityMenu {
    public int iconId;
    public Accessibility accessibility;

    public AccessibilityMenu(Accessibility accessibility, int iconId){
        this.accessibility = accessibility;
        this.iconId = iconId;
    }
}
