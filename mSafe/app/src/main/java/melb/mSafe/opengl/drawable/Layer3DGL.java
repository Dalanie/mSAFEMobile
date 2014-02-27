package melb.mSafe.opengl.drawable;

import android.opengl.GLES20;

import java.util.ArrayList;
import java.util.List;

import melb.mSafe.R;
import melb.mSafe.model.Line3D;
import melb.mSafe.opengl.utilities.ColorHelper;
import melb.mSafe.opengl.utilities.ShaderProgram;
import melb.mSafe.model.Element3D;
import melb.mSafe.model.Layer3D;
import melb.mSafe.model.Polygon3D;
import melb.mSafe.model.Triangle;

public class Layer3DGL {
    private Layer3D layer;
    private boolean visible = true;
    private float[] floorColor = ColorHelper.convert255ColorToGLColor(245f, 245f, 245f, 255f);
    private float[] wallColor = ColorHelper.convert255ColorToGLColor(203f, 203f, 203f, 255f);
    private float[] openColor = ColorHelper.convert255ColorToGLColor(0f, 0f, 0f, 0f);
    private float[] doorColor = ColorHelper.convert255ColorToGLColor(200f, 100f, 30f, 255f);
    private float[] contureColor = ColorHelper.convert255ColorToGLColor(0f,0f,0f,255f);
    private List<IDrawableObject> drawableObjects;
    private List<IDrawableObject> drawableContures;

    private List<IDrawableObject> drawableWalls;
    private List<IDrawableObject> drawableFloors;

    public Layer3DGL(Layer3D layer) {
        this.layer = layer;
        //initWithTextures();
        init();
    }

    private class TriangleWrapper {
        List<Triangle> triangles;
        String type;
    }


    //somehow bugged with textures!
    public void initWithTextures(){
        drawableFloors = new ArrayList<IDrawableObject>();
        drawableWalls = new ArrayList<IDrawableObject>();
        drawableContures = new ArrayList<IDrawableObject>();

        List<TriangleWrapper> triangleWallWrappers = new ArrayList<TriangleWrapper>();
        List<TriangleWrapper> triangleFloorWrappers = new ArrayList<TriangleWrapper>();
        List<Line3D> lineWrappers = new ArrayList<Line3D>();
        for (Polygon3D polygon : layer.polygons){
            for (Element3D el : polygon.outlines){
                TriangleWrapper wrapper = new TriangleWrapper();
                wrapper.triangles = el.triangles;
                wrapper.type = el.type;
                triangleWallWrappers.add(wrapper);
            }
            for (Element3D el : polygon.inlines){ //change polygon "out" and "in"
                TriangleWrapper wrapper = new TriangleWrapper();
                wrapper.triangles = el.triangles;
                wrapper.type = el.type;
                triangleFloorWrappers.add(wrapper);
            }
            for (Line3D line : polygon.lines){
                lineWrappers.add(line);
            }
        }

        List<Triangle> trianglesWall= new ArrayList<Triangle>();
        List<Triangle> trianglesFloor = new ArrayList<Triangle>();

        for (TriangleWrapper wrapper : triangleWallWrappers){
            for(Triangle triangle : wrapper.triangles){
                triangle.texture = new float[]{0f,1f};
                triangle.color = null;
                trianglesWall.add(triangle);
            }
        }
        for (TriangleWrapper wrapper : triangleFloorWrappers){
            for(Triangle triangle : wrapper.triangles){
                triangle.texture = new float[]{0f,1f};
                triangle.color = null;
                trianglesFloor.add(triangle);
            }
        }

        if (!lineWrappers.isEmpty()){
            drawableContures.add(new LineGL(lineWrappers, contureColor));
        }
        if (!trianglesWall.isEmpty()){
            drawableWalls.add(new PolygonGL(trianglesWall, R.drawable.wall_texture)); // alpha-elements
        }
        if (!trianglesFloor.isEmpty()){
            drawableFloors.add(new PolygonGL(trianglesFloor, R.drawable.floor_texture)); // default-elements
        }
    }

    public void init() {
        drawableObjects = new ArrayList<IDrawableObject>();
        drawableContures = new ArrayList<IDrawableObject>();
        List<TriangleWrapper> triangleWrappers = new ArrayList<TriangleWrapper>();
        List<Line3D> lineWrappers = new ArrayList<Line3D>();
        for (Polygon3D polygon : layer.polygons){
            for (Element3D el : polygon.outlines){
                TriangleWrapper wrapper = new TriangleWrapper();
                wrapper.triangles = el.triangles;
                wrapper.type = el.type;
                triangleWrappers.add(wrapper);
            }
            for (Element3D el : polygon.inlines){ //change polygon "out" and "in"
                TriangleWrapper wrapper = new TriangleWrapper();
                wrapper.triangles = el.triangles;
                wrapper.type = el.type;
                triangleWrappers.add(wrapper);
            }
            for (Line3D line : polygon.lines){
                lineWrappers.add(line);
            }
        }

        List<Triangle> trianglesWithAlpha = new ArrayList<Triangle>();
        List<Triangle> trianglesWithoutAlpha = new ArrayList<Triangle>();

        for (TriangleWrapper wrapper : triangleWrappers){
            float[] color = getColorByType(wrapper.type);
            for(Triangle triangle : wrapper.triangles){
                triangle.color = color;
            }
            if (color[3] > 0){ //ignore fully transparent objects
                if (color[3] < 1){
                    //if object has alpha, add triangles to the list of alpha-objects
                    trianglesWithAlpha.addAll(wrapper.triangles);
                }else{
                    trianglesWithoutAlpha.addAll(wrapper.triangles);
                }
            }
        }
        if (!lineWrappers.isEmpty()){
            drawableContures.add(new LineGL(lineWrappers, contureColor));
        }
        if (!trianglesWithAlpha.isEmpty()){
            drawableObjects.add(new PolygonGL(trianglesWithAlpha, true)); // alpha-elements
        }
        if (!trianglesWithoutAlpha.isEmpty()){
            drawableObjects.add(new PolygonGL(trianglesWithoutAlpha, false)); // default-elements
        }
    }

    private float[] getColorByType(String type){
        float[] color;
        if ("Wall".equalsIgnoreCase(type)){
            color = wallColor;
        }else if ("Door".equalsIgnoreCase(type)){
            color = doorColor;
        }else if ("Open".equalsIgnoreCase(type)){
            color = openColor;
        }else if ("Handrail".equalsIgnoreCase(type)){
            color = wallColor;
        }else{
            color = floorColor;
        }
        return color;
    }

    public void draw(float[] mvpMatrix) {
        ShaderProgram colorProgram = Model3DGL.shaderPrograms.get(Model3DGL.ATTRIBUTE_COLOR_SHADER_PROGRAM);
        ShaderProgram textureProgram = Model3DGL.shaderPrograms.get(Model3DGL.TEXTURE_SHADER_PROGRAM);
        if (visible) {
            if (drawableObjects != null){
                for (IDrawableObject drawableObject : drawableObjects) {
                      drawableObject.draw(mvpMatrix, colorProgram);
                }
            }
            if (drawableWalls != null){
                for (IDrawableObject drawableObject : drawableWalls) {
                    drawableObject.draw(mvpMatrix, textureProgram);
                }
            }
            if (drawableFloors != null){
                for (IDrawableObject drawableObject : drawableFloors) {
                    drawableObject.draw(mvpMatrix, textureProgram);
                }
            }
            if (drawableContures != null){
                GLES20.glLineWidth(1);
                for (IDrawableObject drawableObject : drawableContures) {
                    drawableObject.draw(mvpMatrix, colorProgram);
                }
            }
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }


    public Layer3D getLayer() {
        return layer;
    }
}
