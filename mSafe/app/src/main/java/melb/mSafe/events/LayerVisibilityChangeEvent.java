package melb.mSafe.events;

import melb.mSafe.opengl.drawable.Layer3DGL;

/**
 * Created by Daniel on 24.01.14.
 */
public class LayerVisibilityChangeEvent {
    public Layer3DGL layer;
    public boolean visible;

    public LayerVisibilityChangeEvent(Layer3DGL layer, boolean visible) {
        this.layer = layer;
        this.visible = visible;
    }
}
