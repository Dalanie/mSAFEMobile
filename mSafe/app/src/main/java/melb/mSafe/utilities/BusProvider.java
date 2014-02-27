package melb.mSafe.utilities;

import com.squareup.otto.Bus;

import melb.mSafe.ui.AndroidBus;

/**
 * Created by Daniel on 26.12.13.
 */
public class BusProvider {
    private static final Bus BUS = new AndroidBus();

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {
        // No instances.
    }
}
