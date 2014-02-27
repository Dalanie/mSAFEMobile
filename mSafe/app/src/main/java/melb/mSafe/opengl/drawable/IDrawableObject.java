package melb.mSafe.opengl.drawable;


import melb.mSafe.opengl.utilities.ShaderProgram;

public interface IDrawableObject {
	void draw(float[] mvpMatrix, ShaderProgram program);

	boolean isVisible();

	void setVisible(boolean visible);
}
