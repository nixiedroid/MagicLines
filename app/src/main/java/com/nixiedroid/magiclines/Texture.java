package com.nixiedroid.magiclines;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;

public class Texture {
  private int id;
  
  private Texture(int id) {
    if (id == 0) throw new IllegalArgumentException("id=" + id);
    this.id = id;
  }
  
  public static Texture allocateDirect(int paramInt1, int paramInt2) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(paramInt1 * paramInt2 * 4);
    byteBuffer.limit(byteBuffer.capacity());
    byteBuffer.position(0);
    int[] textures = new int[1];
    GLES20.glGenTextures(1, textures, 0);
    GLES20.glBindTexture(3553, textures[0]);
    GLES20.glTexImage2D(3553, 0, 6407, paramInt1, paramInt2, 0, 6407, 5121, byteBuffer);
    GLES20.glTexParameteri(3553, 10241, 9729);
    GLES20.glTexParameteri(3553, 10240, 9729);
    WallpaperRenderer.checkGlError("Texture.allocateDirect");
    return new Texture(textures[0]);
  }
  
  public static Bitmap decodeBitmap(Context context, int resourceId, BitmapFactory.Options options) {
    return BitmapFactory.decodeResource(context.getResources(), resourceId, options);
  }
  
  public static Texture fromBitmap(Bitmap image) {
    if (image == null)
      throw new IllegalArgumentException("bitmap is null"); 
    int[] textures = new int[1];
    GLES20.glGenTextures(1, textures, 0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, image, 0);
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
    WallpaperRenderer.checkGlError("Texture.fromBitmap");
    return new Texture(textures[0]);
  }
  
  public void destroy() {
    GLES20.glDeleteTextures(1, new int[] { this.id}, 0);
    this.id = 0;
    WallpaperRenderer.checkGlError("Texture.destroy");
  }
  
  public int getId() {
    return this.id;
  }
}
