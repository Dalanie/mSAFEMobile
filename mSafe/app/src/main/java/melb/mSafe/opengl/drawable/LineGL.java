package melb.mSafe.opengl.drawable;

import android.opengl.GLES20;

import java.util.ArrayList;
import java.util.List;

import melb.mSafe.model.Line3D;
import melb.mSafe.model.Triangle;
import melb.mSafe.model.Vector3D;
import melb.mSafe.opengl.utilities.ShaderProgram;

public class LineGL implements IDrawableObject {
    private IDrawableObject glLine;
    private boolean isVisible = true;
    private static final int VERTEX_COUNT = 3;
    private static final int COLOR_COUNT = 4;
    private static final int OFFSET = VERTEX_COUNT + COLOR_COUNT;

    public LineGL(List<Vector3D> nodes, float[] color, float lineWidth){
        glLine = getLinesByVectors(nodes, color, lineWidth);
    }

    //better performance for single lines with the same color
    public LineGL(List<Line3D> lines, float[] color){
        float[] linesOfWay = getSingleLines(lines, color);
        glLine = new PolygonGL(linesOfWay, GLES20.GL_LINES, 0);
    }

    private float[] getSingleLines(List<Line3D> lines, float[] color) {
        int counter = 0;
        for (Line3D line : lines){
            counter+= line.points.size()*2;
        }
        float[] linesOfWay = new float[counter*OFFSET];
        int newCounter = 0;
        for(Line3D line : lines){
            for(int i = 0; i < line.points.size()-1; i++){
                Vector3D point = line.points.get(i);
                Vector3D nextPoint = line.points.get(i+1);
                linesOfWay[newCounter++] = point.getX();
                linesOfWay[newCounter++] = point.getY();
                linesOfWay[newCounter++] = point.getZ();
                //r,g,b,a
                linesOfWay[newCounter++] = color[0];
                linesOfWay[newCounter++] = color[1];
                linesOfWay[newCounter++] = color[2];
                linesOfWay[newCounter++] = color[3];

                linesOfWay[newCounter++] = nextPoint.getX();
                linesOfWay[newCounter++] = nextPoint.getY();
                linesOfWay[newCounter++] = nextPoint.getZ();
                //r,g,b,a
                linesOfWay[newCounter++] = color[0];
                linesOfWay[newCounter++] = color[1];
                linesOfWay[newCounter++] = color[2];
                linesOfWay[newCounter++] = color[3];
            }
        }
        return linesOfWay;
    }

    public LineGL(Line3D line, float[] contureColor, float lineWidth) {
        glLine = getLinesByVectors(line.points, contureColor, lineWidth);
    }

    private float[] getLinesByVectors(List<Vector3D> nodes, float[] color){
        float[] linesOfWay = new float[nodes.size()*OFFSET];
        for (int i = 0; i < nodes.size(); i++){
            int counter = 0;
            //x,y,z
            linesOfWay[(i * OFFSET) + counter++] = nodes.get(i).getX();
            linesOfWay[(i * OFFSET) + counter++] = nodes.get(i).getY();
            linesOfWay[(i * OFFSET) + counter++] = nodes.get(i).getZ();
            //r,g,b,a
            linesOfWay[(i * OFFSET) + counter++] = color[0];
            linesOfWay[(i * OFFSET) + counter++] = color[1];
            linesOfWay[(i * OFFSET) + counter++] = color[2];
            linesOfWay[(i * OFFSET) + counter++] = color[3];
        }
        return linesOfWay;
    }

    private IDrawableObject getLinesByVectors(List<Vector3D> nodes, float[] color, float lineWidth){
        List<Triangle> triangles = new ArrayList<Triangle>();
        for (int i = 0; i < nodes.size()-1; i++){
            Vector3D node = nodes.get(i);
            Vector3D nextNode = nodes.get(i+1);

            float dx = nextNode.getX() - node.getX();
            float dy = nextNode.getY() - node.getY();

            Vector3D normalVector = Vector3D.getNormalized(new Vector3D(dx,dy,0));
            //half length
            normalVector = new Vector3D(normalVector.getX()*(lineWidth/2f), normalVector.getY()*(lineWidth/2f), normalVector.getZ()*(lineWidth/2f));

            Vector3D a1 = node.add(normalVector);
            Vector3D a2 = Vector3D.subtract(node, normalVector);
            Vector3D b1 = nextNode.add(normalVector);
            Vector3D b2 = Vector3D.subtract(nextNode, normalVector);

            Triangle triangle = getTriangleByVectors(a1,a2,b1, color);
            Triangle triangle2 = getTriangleByVectors(b1, b2, a2, color);
            triangles.add(triangle);
            triangles.add(triangle2);
        }

        return new PolygonGL(triangles);
    }

    private Triangle getTriangleByVectors(Vector3D a, Vector3D b, Vector3D c, float[] color) {
        float[] vertices = new float[9];
        vertices[0] = a.getX();
        vertices[1] = a.getY();
        vertices[2] = a.getZ();

        vertices[3] = b.getX();
        vertices[4] = b.getY();
        vertices[5] = b.getZ();

        vertices[6] = c.getX();
        vertices[7] = c.getY();
        vertices[8] = c.getZ();

        return new Triangle(vertices, color, true);
    }

    @Override
    public void draw(float[] mvpMatrix, ShaderProgram program) {
        if (isVisible) {
            glLine.draw(mvpMatrix, program);
        }
    }

    @Override
    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

}
