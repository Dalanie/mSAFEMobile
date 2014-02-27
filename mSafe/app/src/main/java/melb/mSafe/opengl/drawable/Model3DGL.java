package melb.mSafe.opengl.drawable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import melb.mSafe.R;
import melb.mSafe.common.ExtendedWay;
import melb.mSafe.common.RotationPoint;
import melb.mSafe.events.LayerVisibilityChangeEvent;
import melb.mSafe.events.RouteChangedEvent;
import melb.mSafe.events.RouteGraphChangedEvent;
import melb.mSafe.model.RouteGraph;
import melb.mSafe.model.RouteGraphNode;
import melb.mSafe.opengl.utilities.AttributeColorShaderProgram;
import melb.mSafe.model.Layer3D;
import melb.mSafe.model.Model3D;
import melb.mSafe.model.Vector3D;
import melb.mSafe.opengl.utilities.ShaderProgram;
import melb.mSafe.opengl.utilities.TextureHelper;
import melb.mSafe.utilities.BusProvider;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

public class Model3DGL {
    public static SparseIntArray resourceTextureMap = new SparseIntArray();
    public static SparseArray<ShaderProgram> shaderPrograms = new SparseArray<ShaderProgram>();
    public static final int ATTRIBUTE_COLOR_SHADER_PROGRAM = 42;
    public static final int TEXTURE_SHADER_PROGRAM = 13;
    private static float EPSILON = 5;

    private List<Layer3DGL> glLayers;
    private static final boolean linesVisible = true;
    private float modelRatio = 1.0f;
    private Model3D model;
    private PolygonGL linesWay;
    public Vector3D mUserPosition;
    private UserPositionGL glUserPosition;
    private WayGL glWay;
    private WayGL glTransparencyWay;
    private float mUserOrientation;
    private boolean isInitialized = false;
    private List<DestinationGL> destinationGLs;
    private ExtendedWay extendedWay;

    public Model3DGL(Model3D model, Vector3D userPosition) {
        initialize(model);
        mUserPosition = userPosition;
    }



    private void initUserPosition() {
        glUserPosition = new UserPositionGL(true, mUserPosition);
    }

    public Vector3D getUserPosition(){
        if (mUserPosition == null){
            mUserPosition = new Vector3D(0f,0f,0f);
        }
        return mUserPosition;
    }

    private void initLayers() {
        glLayers = new ArrayList<Layer3DGL>();
        for (Layer3D layer : model.layers) {
            glLayers.add(new Layer3DGL(layer));
        }
        linesWay = getLinesOfBuilding();
    }

    private PolygonGL getLinesOfBuilding() {
        float color[] = { 1.0f, 0.0f, 0.0f, 1.0f };
        float[] linesOfBuilding = { 0f, 0f, 0f, color[0], color[1], color[2],
                color[3], model.width, 0f, 0f, color[0], color[1], color[2],
                color[3], model.width, model.length, 0f, color[0], color[1],
                color[2], color[3], 0f, model.length, 0f, color[0], color[1],
                color[2], color[3], 0f, 0f, 0f, color[0], color[1], color[2],
                color[3] };
        return new PolygonGL(linesOfBuilding,
                GLES20.GL_LINE_STRIP, 0);
    }

    public void draw(float[] mvpMatrix) {
        ShaderProgram colorProgram = shaderPrograms.get(ATTRIBUTE_COLOR_SHADER_PROGRAM);
        if (isInitialized){
            if (glLayers != null) {
                synchronized (glLayers){
                    Iterator<Layer3DGL> i = glLayers.iterator(); // Must be in synchronized block
                    while (i.hasNext()){
                        i.next().draw(mvpMatrix);
                    }
                }
            }
            if (linesVisible) {
                if (linesWay != null) {
                    synchronized (linesWay){
                        GLES20.glLineWidth(5f);
                        linesWay.draw(mvpMatrix, colorProgram);
                    }
                }
            }
            if (glWay != null) {
                GLES20.glLineWidth(5f);
                glWay.draw(mvpMatrix, colorProgram);
            }
            if (glTransparencyWay != null){
                GLES20.glLineWidth(7f);
                GLES20.glDisable(GLES20.GL_DEPTH_TEST);
                glTransparencyWay.draw(mvpMatrix, colorProgram);
                GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            }
            if (glUserPosition != null) {
                synchronized (glUserPosition){
                    glUserPosition.draw(mvpMatrix, colorProgram);
                }
            }

            if (destinationGLs != null){
                synchronized (destinationGLs){
                    Iterator<DestinationGL> i = destinationGLs.iterator(); // Must be in synchronized block
                    while (i.hasNext()){
                        i.next().draw(mvpMatrix, colorProgram);
                    }
                }
            }
        }
    }

    public float getRatio() {
        if (model == null){
            return 1;
        }
        return modelRatio;
    }

    public float getWidth() {
        if (model == null){
            return 1;
        }
        return model.width;
    }

    public float getHeight() {
        if (model == null){
            return 1;
        }
        return model.height;
    }

    public float getLength() {
        if (model == null){
            return 1;
        }
        return model.length;
    }

    private void getModelRatio() {
        float highestValue = (model.width > model.length) ? model.width
                : model.length;
        modelRatio = 2f / highestValue;
    }

    public List<Layer3DGL> getLayers() {
        return glLayers;
    }

    public void setHeight(float height) {
        this.model.height = height;
    }

    public void setLength(float length) {
        this.model.length = length;
    }

    public void setWidth(float width) {
        this.model.width = width;
    }

    public void initWithGLContext(Context context) {
        shaderPrograms.put(ATTRIBUTE_COLOR_SHADER_PROGRAM, new AttributeColorShaderProgram(context));
        shaderPrograms.put(TEXTURE_SHADER_PROGRAM, new AttributeColorShaderProgram(context));
        boolean failure = false;
        //failure &= !loadTexture(context, R.drawable.floor_texture);
        //failure &= !loadTexture(context, R.drawable.wall_texture);
        if (failure){
            Log.e("Texture", "Error in loading textures");
        }
    }

    private boolean loadTexture(Context context, int drawableId){
        int textureId = TextureHelper.loadTexture(context, drawableId);
        if (textureId != 0){
            resourceTextureMap.put(drawableId, textureId);
            return true;
        }
        return false;
    }

    public void setUserPosition(Vector3D userPosition) {
        if (userPosition != null){
            mUserPosition = userPosition;
            updateUserPosition();
        }
    }

    public void setUserOrientation(RotationPoint userOrientation) {
        mUserOrientation = userOrientation.bearing;
        updateUserOrientation();
    }

    public void initialize(Model3D model){
        if (model != null){
            synchronized (model){
                this.model = model;
                getModelRatio();
                initLayers();
                initUserPosition();
                isInitialized = true;
            }
        }
    }

    private void updateUserPosition(){
        if (glUserPosition != null){
            glUserPosition.setUserPosition(mUserPosition);
        }
        hideUpperLayers();
    }

    private void hideUpperLayers() {
        float zValue = mUserPosition.getZ();
        if (glLayers != null){
            synchronized (glLayers){
                for (Layer3DGL layer : glLayers){
                    boolean visible = layer.getLayer().getLayerZ() <= zValue + EPSILON;
                    changeVisibilityOfLayer(layer, visible);
                }
            }
        }
    }

    private void changeVisibilityOfLayer(Layer3DGL layer, boolean isVisible){
        layer.setVisible(isVisible);
        BusProvider.getInstance().post(new LayerVisibilityChangeEvent(layer, isVisible));
    }

    private void updateUserOrientation(){
        if (glUserPosition != null){
            glUserPosition.setUserOrientation(mUserOrientation);
        }
    }

    public void updateWay(RouteChangedEvent event){
        if (event != null && event.extendedWay != null){
            glWay = new WayGL(event.extendedWay, true, 10000, false);
            glTransparencyWay = new WayGL(event.extendedWay, true, 10000, true);
            extendedWay = event.extendedWay;
        }
    }

    public void setRouteGraphChanged(RouteGraphChangedEvent event) {
        RouteGraph graph = event.routeGraph;
        if (graph != null){
            if (destinationGLs == null){
                destinationGLs = new ArrayList<DestinationGL>();
            }
            synchronized (destinationGLs){
                destinationGLs.clear();
                Collection<RouteGraphNode> nodes = graph.getGoals();
                for (RouteGraphNode node : nodes){
                    destinationGLs.add(new DestinationGL(true, node.position));
                }
            }
        }
    }

    public Vector3D getOrientationToNextTarget() {
        if (extendedWay != null){
            if (extendedWay.getCurrentNode() != null){
                float directionToNextTarget = extendedWay.getDirection(extendedWay.getCurrentNode());
                return new Vector3D(0, 0f, directionToNextTarget);
            }
        }
        return new Vector3D(0,0,0);
    }
}
