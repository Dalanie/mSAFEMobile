package melb.mSafe.opengl.drawable;

import java.util.List;

import melb.mSafe.opengl.utilities.AttributeColorShaderProgram;
import melb.mSafe.opengl.utilities.Constants;
import melb.mSafe.opengl.utilities.FloatBufferHelper;
import melb.mSafe.opengl.utilities.ShaderProgram;
import melb.mSafe.opengl.utilities.TextureShaderProgram;
import melb.mSafe.opengl.utilities.VertexArray;
import melb.mSafe.model.Triangle;
import android.opengl.GLES20;

public class PolygonGL implements IDrawableObject {
    private boolean visible = true;

    protected VertexArray vertexArray;
    private Integer glType = null;
    private int offset = 0;
    private int vertexCount;
    private boolean hasAlpha = false;
    private static final int verticeSize = Constants.POSITION_COMPONENT_COUNT + Constants.COLOR_COMPONENT_COUNT;
    private static final int verticeTextureSize = Constants.POSITION_COMPONENT_COUNT + Constants.TEXTURE_COORDINATES_COMPONENT_COUNT;
    private int textureDrawableId;


    public PolygonGL(List<Triangle> triangles) {
        float[] vertices = FloatBufferHelper.createPolygon(triangles);
        vertexArray = new VertexArray(vertices);
        vertexCount = vertices.length / verticeSize;
    }

    public PolygonGL(List<Triangle> triangles, int textureDrawableId) {
        float[] vertices = FloatBufferHelper.createPolygon(triangles);
        vertexArray = new VertexArray(vertices);
        vertexCount = vertices.length / verticeTextureSize;
        this.textureDrawableId = textureDrawableId;
    }

    public PolygonGL(List<Triangle> triangles, boolean hasAlpha) {
        float[] vertices = FloatBufferHelper.createPolygon(triangles);
        vertexArray = new VertexArray(vertices);
        vertexCount = vertices.length / verticeSize;
        this.hasAlpha = hasAlpha;
    }

    public PolygonGL(float[] vertices) {
        vertexArray = new VertexArray(vertices);
        vertexCount = vertices.length / verticeSize;
    }

    public PolygonGL(float[] vertices, boolean hasAlpha) {
        vertexArray = new VertexArray(vertices);
        vertexCount = vertices.length / verticeSize;
        this.hasAlpha = hasAlpha;
    }

    public PolygonGL(float[] vertices, Integer glType, int offset) {
        vertexArray = new VertexArray(vertices);
        vertexCount = vertices.length / verticeSize;
        this.offset = offset;
        this.glType = glType;
    }

    public PolygonGL(float[] vertices, Integer glType, int offset, boolean hasAlpha) {
        vertexArray = new VertexArray(vertices);
        vertexCount = vertices.length / verticeSize;
        this.offset = offset;
        this.glType = glType;
        this.hasAlpha = hasAlpha;
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix
     *            - The Model View Project matrix in which to draw this shape.
     */
    public void draw(float[] mvpMatrix, ShaderProgram program) {
        if (visible) {
            program.useProgram();

            if (program instanceof AttributeColorShaderProgram) {
                AttributeColorShaderProgram shaderProgram = (AttributeColorShaderProgram) program;
                shaderProgram.setUniformMatrix(mvpMatrix);
            }else if (program instanceof TextureShaderProgram){
                TextureShaderProgram shaderProgram =(TextureShaderProgram) program;
                shaderProgram.setUniforms(mvpMatrix, Model3DGL.resourceTextureMap.get(textureDrawableId));
            }
            bindData(program);

            if (hasAlpha){
                GLES20.glEnable(GLES20.GL_BLEND);
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            }
            // Draw the triangles
            if (glType != null) {
                GLES20.glDrawArrays(glType, offset, vertexCount);
            } else {
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
            }
            if (hasAlpha){
                GLES20.glDisable(GLES20.GL_BLEND);
            }
        }
    }


    protected void bindData(ShaderProgram program) {
        if (program instanceof AttributeColorShaderProgram) {
            AttributeColorShaderProgram colorProgram = (AttributeColorShaderProgram) program;
            vertexArray.setVertexAttribPointer(0,
                    colorProgram.getPositionAttributeLocation(),
                    Constants.POSITION_COMPONENT_COUNT,
                    Constants.POSITION_COLOR_STRIDE);
            vertexArray.setVertexAttribPointer(
                    Constants.POSITION_COMPONENT_COUNT,
                    colorProgram.getColorAttributeLocation(),
                    Constants.COLOR_COMPONENT_COUNT,
                    Constants.POSITION_COLOR_STRIDE);
        } else if (program instanceof TextureShaderProgram) {
            TextureShaderProgram textureProgram = (TextureShaderProgram) program;
            vertexArray.setVertexAttribPointer(0,
                    textureProgram.getPositionAttributeLocation(),
                    Constants.POSITION_COMPONENT_COUNT,
                    Constants.POSITION_TEXTURE_STRIDE);
            vertexArray.setVertexAttribPointer(
                    Constants.POSITION_COMPONENT_COUNT,
                    textureProgram.getTextureCoordinatesAttributeLocation(),
                    Constants.TEXTURE_COORDINATES_COMPONENT_COUNT,
                    Constants.POSITION_TEXTURE_STRIDE);
        }
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

}
