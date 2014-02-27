package melb.mSafe.events;

import melb.mSafe.model.Accessibility;

/**
 * Created by Daniel on 13.01.14.
 */
public class AccessibilityChangedEvent {

    public Accessibility accessibility;
    public Boolean elevatorAllowed;
    public Boolean stairsAllowed;

    public AccessibilityChangedEvent(Accessibility accessibility, Boolean elevatorAllowed, Boolean stairsAllowed){
        this.accessibility = accessibility;
        this.elevatorAllowed = elevatorAllowed;
        this.stairsAllowed = stairsAllowed;
    }
}
