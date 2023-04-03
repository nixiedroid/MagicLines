package com.nixiedroid.magiclines;

import android.opengl.GLES20;
import java.nio.FloatBuffer;

public class VertexBufferObject {
  private int indexBuffer;
  private final int size;
  private final FloatBuffer textureBuffer;
  private int vertexBuffer;

  public VertexBufferObject(MeshPrimitive mesh) {
    if (mesh.isTextured()) {
      this.textureBuffer = mesh.getTextureBuffer();
    } else {
      this.textureBuffer = null;
    }

    this.size = mesh.getNumIndices();
    int[] buffers = new int[]{-1, -1};
    GLES20.glGenBuffers(2, buffers, 0);
    if (buffers[0] >= 0 && buffers[1] >= 0) {
      this.vertexBuffer = buffers[0];
      this.indexBuffer = buffers[1];
      GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.vertexBuffer);
      GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mesh.getVertexBufferSize(), mesh.getVertexBuffer(), GLES20.GL_STATIC_DRAW);
      GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, this.indexBuffer);
      GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, mesh.getIndexBufferSize(), mesh.getIndexBuffer(), GLES20.GL_STATIC_DRAW);
      WallpaperRenderer.checkGlError("VertexBufferObject glBufferData");
      if (!GLES20.glIsBuffer(this.indexBuffer) || !GLES20.glIsBuffer(this.vertexBuffer)) {
        throw new RuntimeException("VertexBufferObject:Index VBO or Vertex VBO did not bind! Maybe GL surface is not yet created?");
      }

      GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
      GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    } else {
      WallpaperRenderer.checkGlError("VertexBufferObject glGenBuffers");
    }

  }

  public void destroy() {
    GLES20.glDeleteBuffers(2, new int[]{this.vertexBuffer, this.indexBuffer}, 0);
    this.vertexBuffer = 0;
    this.indexBuffer = 0;
    WallpaperRenderer.checkGlError("VertexBufferObject.destroy buffers");
  }

  public int getIndexBuffer() {
    return this.indexBuffer;
  }

  public int getIndexSize() {
    return this.size;
  }

  public FloatBuffer getTextureBuffer() {
    return this.textureBuffer;
  }

  public int getVertexBuffer() {
    return this.vertexBuffer;
  }
}
