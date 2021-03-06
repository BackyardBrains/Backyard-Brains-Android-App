package com.backyardbrains.drawing.gl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author Tihomir Leka <tihomir at backyardbrains.com>
 */
public class GlSpikes {

    // We can maximally handle 6 seconds of sample data and spike can appear max every 5 ms so 8000 is more then enough
    public static final int MAX_SPIKES = 8000;
    // We multiply by 2 because we have 2 vertices for every spike
    private static final int MAX_POINT_VERTICES = MAX_SPIKES * 2;
    // We multiply by 4 because we have 4 vertices for every color
    private static final int MAX_COLOR_VERTICES = MAX_SPIKES * 4;

    private static final float POINT_SIZE = 10f;

    private FloatBuffer spikesVFB;
    private FloatBuffer spikesColorVFB;

    private float[] spikesVertices = new float[MAX_POINT_VERTICES];
    private float[] spikesColors = new float[MAX_COLOR_VERTICES];

    public GlSpikes() {
        ByteBuffer spikesVBB = ByteBuffer.allocateDirect(MAX_POINT_VERTICES * 4);
        spikesVBB.order(ByteOrder.nativeOrder());
        spikesVFB = spikesVBB.asFloatBuffer();

        ByteBuffer spikeColorsVBB = ByteBuffer.allocateDirect(MAX_COLOR_VERTICES * 4);
        spikeColorsVBB.order(ByteOrder.nativeOrder());
        spikesColorVFB = spikeColorsVBB.asFloatBuffer();
    }

    public void draw(GL10 gl, float[] spikesVertices, float[] spikesColors, int verticesCount) {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        gl.glPointSize(POINT_SIZE);
        System.arraycopy(spikesVertices, 0, this.spikesVertices, 0, verticesCount);
        spikesVFB.put(this.spikesVertices);
        spikesVFB.position(0);
        System.arraycopy(spikesColors, 0, this.spikesColors, 0, verticesCount * 2);
        spikesColorVFB.put(this.spikesColors);
        spikesColorVFB.position(0);
        gl.glVertexPointer(2, GL10.GL_FLOAT, 0, spikesVFB);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, spikesColorVFB);
        gl.glDrawArrays(GL10.GL_POINTS, 0, (int) (verticesCount * .5));
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    }
}
