package com.nixiedroid.magiclines;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class MeshPrimitive {
  private static final int FLOAT_SIZE = 4;
  
  private static final int SHORT_SIZE = 2;
  

  private final boolean isTextured;
  
  private final FloatBuffer textureBuffer;

  
  private final FloatBuffer vertexBuffer;
  
  private final int vertexBytes;

  private final ShortBuffer indexBuffer;
  private final int indices;
  private final int indicesBytes;
  
  private MeshPrimitive(FloatBuffer vertices, ShortBuffer indices, FloatBuffer texture) {
    this.vertexBuffer = vertices;
    this.indexBuffer = indices;
    this.textureBuffer = texture;
    this.indicesBytes = indices.capacity() * SHORT_SIZE;
    this.vertexBytes = vertices.capacity() * FLOAT_SIZE;
    this.isTextured = texture != null;
    this.indices = this.indicesBytes / 2;
  }
  
  public static MeshPrimitive newForegroundPrimitive(short length) {
    return new ForegroundMeshPrimitive(length);
  }
  
  public static MeshPrimitive newSquareMeshPrimitive(float lenX, float lenY) {
    return new SquareMeshPrimitive(lenX, lenY);
  }
  
  public ShortBuffer getIndexBuffer() {
    return this.indexBuffer;
  }
  
  public int getIndexBufferSize() {
    return this.indicesBytes;
  }
  
  public int getNumIndices() {
    return this.indices;
  }
  
  public FloatBuffer getTextureBuffer() {
    if (!this.isTextured)
      throw new RuntimeException("meshPrimitive is not textured!"); 
    return this.textureBuffer;
  }
  
  public FloatBuffer getVertexBuffer() {
    return this.vertexBuffer;
  }
  
  public int getVertexBufferSize() {
    return this.vertexBytes;
  }
  
  public boolean isTextured() {
    return this.isTextured;
  }
  
  private static class ForegroundMeshPrimitive extends MeshPrimitive {
    public ForegroundMeshPrimitive(short length) {
      super(vertices(length), indices(length), null);
    }
    
    private static ShortBuffer indices(short length) {
      short size = (short)(length * 2 + 2);
      short halfSize = (short)((length - 1) * size);
      short j;
      short[] indicesArray = new short[halfSize];
      for (short i = 0; i < length - 1; i++) {
        short pointer = (short) (i * size);
        for (j = 0; j < length; j++) {
          indicesArray[pointer] = (short)(i * length + j);
          indicesArray[pointer +1] = (short)((i + 1) * length + j);
          pointer +=2;
        }
        indicesArray[pointer] = (short)((i + 1) * length + length - 1);
        indicesArray[pointer + 1] = (short)((i + 1) * length);
      } 
      ShortBuffer shortBuffer =
              ByteBuffer.allocateDirect(halfSize * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
      shortBuffer.put(indicesArray);
      shortBuffer.position(0);
      return shortBuffer;
    }
    
    private static FloatBuffer vertices(int length) {
      int l2 = length * length;
      float[] arrayOfFloat = new float[l2 * 3];
      for (int i = 0; i < length; i++) {
        int j = i * length * 3;
        byte b1 = 0;
        while (b1 < length) {
          arrayOfFloat[j] = b1 * 1.0F / length;
          arrayOfFloat[j + 1] = i * 0.7F / length;
          arrayOfFloat[j + 2] = 0.0F;
          b1++;
          j += 3;
        } 
      } 
      FloatBuffer floatBuffer = ByteBuffer.allocateDirect(l2 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
      floatBuffer.put(arrayOfFloat);
      floatBuffer.position(0);
      return floatBuffer;
    }
  }
  
  private static class SquareMeshPrimitive extends MeshPrimitive {
    public SquareMeshPrimitive(float lenX, float lenY) {
      super(vertices(lenX, lenY), indices(), texture());
    }
    
    private static ShortBuffer indices() {
      short[] array = new short[6];
      array[0] = 0;
      array[1] = 1;
      array[2] = 2;
      array[3] = 1;
      array[4] = 2;
      array[5] = 3;
      ShortBuffer shortBuffer = ByteBuffer.allocateDirect(array.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
      shortBuffer.put(array);
      shortBuffer.position(0);
      return shortBuffer;
    }
    
    private static FloatBuffer texture() {
      float[] array = new float[8];
      array[0] = 0.0F;
      array[1] = 0.0F;
      array[2] = 1.0F;
      array[3] = 0.0F;
      array[4] = 0.0F;
      array[5] = 1.0F;
      array[6] = 1.0F;
      array[7] = 1.0F;
      FloatBuffer floatBuffer = ByteBuffer.allocateDirect(array.length * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
      floatBuffer.put(array);
      floatBuffer.position(0);
      return floatBuffer;
    }
    
    private static FloatBuffer vertices(float lenX, float lenY) {
      float[] array = new float[12];
      array[0] = -lenX;
      array[1] = lenY;
      array[2] = 0.02F;
      array[3] = lenX;
      array[4] = lenY;
      array[5] = 0.02F;
      array[6] = -lenX;
      array[7] = -lenY;
      array[8] = 0.02F;
      array[9] = lenX;
      array[10] = -lenY;
      array[11] = 0.02F;
      FloatBuffer floatBuffer =
              ByteBuffer.allocateDirect(array.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
      floatBuffer.put(array);
      floatBuffer.position(0);
      return floatBuffer;
    }
  }
}
