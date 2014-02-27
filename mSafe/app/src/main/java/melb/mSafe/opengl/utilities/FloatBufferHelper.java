package melb.mSafe.opengl.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import melb.mSafe.model.Triangle;

public class FloatBufferHelper {

	/**
	 * draw circle contours (skip center vertex at start of buffer)
	 * glDrawArrays(GL_LINE_LOOP, 2, outerVertexCount);
	 * 
	 * draw circle as filled shape glDrawArrays(GL_TRIANGLE_FAN, 0,
	 * vertexCount);
	 * 
	 * http://stackoverflow.com/questions/18140117/how-to-draw-basic
	 * -circle-in-opengl-es-2-0-android
	 * 
	 * @param circleColor
	 */

	public static float[] createCircle(int vertexCount, float radius,
			float center_x, float center_y, float center_z, float[] circleColor, float[] centerCircleColor) {
		/*
		 * create a buffer for vertex data
		 */
		float buffer[] = new float[vertexCount
				* Constants.POSITION_COLOR_STRIDE];
		int idx = 0;

		/*
		 * center vertex for triangle fan
		 */
		buffer[idx++] = center_x;
		buffer[idx++] = center_y;
		buffer[idx++] = center_z;
		buffer[idx++] = circleColor[0];
		buffer[idx++] = circleColor[1];
		buffer[idx++] = circleColor[2];
		buffer[idx++] = circleColor[3];

		/*
		 * outer vertices of the circle
		 */
		int outerVertexCount = vertexCount - 1;

		for (int i = 0; i < outerVertexCount; ++i) {
			float percent = (i / (float) (outerVertexCount - 1));
			float rad = (float) (percent * 2 * Math.PI);

			/*
			 * vertex position
			 */
			float outer_x = (float) (center_x + radius * Math.cos(rad));
			float outer_y = (float) (center_y + radius * Math.sin(rad));

			buffer[idx++] = outer_x;
			buffer[idx++] = outer_y;
			buffer[idx++] = center_z;
			buffer[idx++] = centerCircleColor[0];
			buffer[idx++] = centerCircleColor[1];
			buffer[idx++] = centerCircleColor[2];
			buffer[idx++] = centerCircleColor[3];
		}
		return buffer;
	}

	public static float[] createArrow(float size, float[] color) {
		List<Triangle> triangles = new ArrayList<Triangle>();
		/*
		 * first part of arrow
		 */
		triangles.add(new Triangle(new float[] {
                0.5f * size, 0f, 0f,
               -0.17f * size, 0f, 0f,
               -0.5f * size,-0.5f * size, 0f,
				}, new float[] {
                color[0], color[1], color[2], color[3] }, true));
		/*
		 * second part of arrow
		 */
		triangles.add(new Triangle(new float[] {
                0.5f * size, 0f, 0f,
               -0.5f * size, 0.5f * size, 0f,
			   -0.17f * size, 0f, 0f }, new float[] {color[0], color[1], color[2], color[3]}, true));
		return createPolygon(triangles);
	}

	public static float[] createPolygon(List<Triangle> triangles) {
		float[] vertices;
        float[][] allTrianglesSorted = new float[triangles.size()][];
        int counter = 0;
        for (Triangle triangle : triangles){
            allTrianglesSorted[counter++] = triangle.getTrianglesSorted();
        }
        vertices = concatAllFloat(allTrianglesSorted);
		return vertices;
	}

	/**
	 * Concatenates a list of float arrays into a single array.
	 * 
	 * @param arrays
	 *            The arrays.
	 * @return The concatenated array.
	 * 
	 * @see {@link //stackoverflow.com/questions/80476/how-to-concatenate-two
	 *      -arrays-in-java}
	 */
	public static float[] concatAllFloat(float[]... arrays) {
		int totalLength = 0;
		final int subArrayCount = arrays.length;
		for (int i = 0; i < subArrayCount; ++i) {
			if (arrays[i] != null) {
				totalLength += arrays[i].length;
			}
		}
		float[] result = Arrays.copyOf(arrays[0], totalLength);
		int offset = arrays[0].length;
		for (int i = 1; i < subArrayCount; ++i) {
			System.arraycopy(arrays[i], 0, result, offset, arrays[i].length);
			offset += arrays[i].length;
		}
		return result;
	}

}
