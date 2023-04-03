package com.nixiedroid.magiclines;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.view.MotionEvent;
import com.nixiedroid.magiclines.color.Color;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class WallpaperRenderer implements GLSurfaceView.Renderer {
  private final Context context;
  private final Color color;


  private static final float[] primaryColor = new float[3];
  private static final float[] secondaryColor = new float[3];


  private Bitmap backgroundImage;
  private String backgroundFragmentShader;
  private String backgroundVertexShader;
  protected ShaderProgram backgroundShaders;
  protected Texture backgroundTexture;
  protected VertexBufferObject backgroundVBO;


  private Bitmap foregroundImage;
  private String foregroundFragmentShader;
  private String foregroundVertexShader;
  private ShaderProgram foregroundShaders;
  private Texture foregroundTexture;
  protected VertexBufferObject foregroundVBO;

  private final FrameSkipper frameSkipper;


  private boolean isFirstFrame;
  private boolean isTouched;
  private boolean isDestroyed;
  private boolean isWhiteThemed;

  private float animationSpeedFactor;
  private float animationTimeMillis;
  private float animationTimeSeconds;

  private float lastAnimationTimeRefresh;
  private float lastFrameTime;
  private float lastTouchTime;
  private float frameDeltaTime;
  private float timeTouchBonus;

  private float delta;
  private float noiseScale;
  private float screenOffset;


  private final float[] identityMatrix;
  private final float[] MVPMatrix;
  private final float[] perspectiveMatrix;
  private final float[] viewMatrix;

  private void createShaders() {
    if (this.foregroundVertexShader == null) {
     // this.foregroundVertexShader = ShaderFactory.loadText(R.raw.fg_vp, this.context);
      this.foregroundVertexShader = Shaders.FOREGROUND_VERTEX.getData();
    }
    if (this.foregroundFragmentShader == null) {
      //this.foregroundFragmentShader = ShaderFactory.loadText(R.raw.fg_fp, this.context);
      this.foregroundFragmentShader = Shaders.FOREGROUND_FRAGMENT.getData();
    }
    if (this.foregroundShaders != null) this.foregroundShaders.destroy();
    this.foregroundShaders = ShaderFactory.createProgramFromString(this.foregroundVertexShader, this.foregroundFragmentShader);


    if (this.backgroundVertexShader == null) {
     //this.backgroundVertexShader = ShaderFactory.loadText(R.raw.bg_vp, this.context);
      this.backgroundVertexShader = Shaders.BACKGROUND_VERTEX.getData();
    }
    if (this.backgroundFragmentShader == null) {
      //this.backgroundFragmentShader = ShaderFactory.loadText(R.raw.bg_fp, this.context);
      this.backgroundFragmentShader = Shaders.BACKGROUND_FRAGMENT.getData();
    }
    if (this.backgroundShaders != null) this.backgroundShaders.destroy();
    this.backgroundShaders = ShaderFactory.createProgramFromString(this.backgroundVertexShader, this.backgroundFragmentShader);


    GLES20.glReleaseShaderCompiler();
  }
  
  private void createTextures() {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inDither = false;
    int foregroundResID = R.drawable.flow_greyscale;
    if (this.foregroundImage == null)
      this.foregroundImage = Texture.decodeBitmap(this.context, foregroundResID, options);

    if (this.foregroundTexture != null)
      this.foregroundTexture.destroy();
    this.foregroundTexture = Texture.fromBitmap(this.foregroundImage);


    int backgroundResID = R.drawable.bg_grey;
    if (this.backgroundImage == null)
      this.backgroundImage = Texture.decodeBitmap(this.context, backgroundResID, null);

    if (this.backgroundTexture != null)
      this.backgroundTexture.destroy();
    this.backgroundTexture = Texture.fromBitmap(this.backgroundImage);
  }


  public WallpaperRenderer(final Context context,final Color color) {
    super();
    this.context = context;
    this.color = color;
    this.foregroundImage = null;
    this.backgroundImage = null;
    this.animationTimeSeconds = 0.0f;
    this.animationTimeMillis = 0.0f;
    this.lastFrameTime = Float.MAX_VALUE;
    this.lastAnimationTimeRefresh = Float.MAX_VALUE;
    this.frameDeltaTime = 50.0f;
    this.isFirstFrame = false;
    this.MVPMatrix = new float[16];
    this.perspectiveMatrix = new float[16];
    this.identityMatrix = new float[16];
    this.viewMatrix = new float[16];
    this.animationSpeedFactor = 0.0f;
    this.frameSkipper = new FrameSkipper();

  }

  private void accelerateOnTouch() {
    this.timeTouchBonus = this.animationTimeMillis + 3000.0f;
    this.frameDeltaTime = 40f;
    this.lastTouchTime = SystemClock.uptimeMillis();
    this.animationSpeedFactor = 1.0f;
    this.frameSkipper.wake();
  }

  public static void checkGlError(final String str) {
    final int glGetError = GLES20.glGetError();
    if (glGetError != 0) {
      throw new RuntimeException(str + ": glError " + glGetError);
    }
  }

  private void matrixSetUp() {
    Matrix.setIdentityM(this.identityMatrix, 0);
    Matrix.rotateM(this.identityMatrix, 0, 90.0f, 1.0f, 0.0f, 0.0f);
    Matrix.rotateM(this.identityMatrix, 0, 0.0f, 0.0f, 0.0f, 1.0f);
    Matrix.scaleM(this.identityMatrix, 0, 6.0f, 4.0f, 7.0f);
    Matrix.translateM(this.identityMatrix, 0, -0.47f + this.screenOffset, -0.5f, 0.0f);
    Matrix.multiplyMM(this.MVPMatrix, 0, this.viewMatrix, 0, this.identityMatrix, 0);
    Matrix.multiplyMM(this.MVPMatrix, 0, this.perspectiveMatrix, 0, this.MVPMatrix, 0);
    checkGlError("WallpaperRenderer.matrixSetUp");
  }


  private static final float[] OFFSET_STEPS = new float[] { 0.0f, 0.125f, 0.25f };
  private void render(final GL10 gl10) {
    this.delta = Float.MAX_VALUE;
    for (float offsetStep : OFFSET_STEPS) {
      final float abs = Math.abs(offsetStep - Math.abs(this.screenOffset));
      if (abs < this.delta) {
        this.delta = abs;
      }
    }
    this.noiseScale = Math.min(Math.max(8.0f, this.delta / 0.125f * 2.0f + 8.0f), 10.0f);
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    if (color.isDarkMode) this.renderBackground(this.backgroundShaders, this.backgroundTexture, this.backgroundVBO);
    this.renderForeground();
    checkGlError("WallpaperRenderer.onDrawFrame");
  }

  private void renderBackground(final ShaderProgram shaderProgram, final Texture texture, final VertexBufferObject vertexBufferObject) {
    shaderProgram.use();
    final int textureCoords = shaderProgram.getAttributeHandle("v_textureCoords");
    final int position = shaderProgram.getAttributeHandle("v_Position");
    final int primaryColor = shaderProgram.getUniformHandle("u_PrimaryColor");
    final int time_height_color_xOffset = shaderProgram.getUniformHandle("u_in_time_height_color_xOffset");

    GLES20.glUniform4f(time_height_color_xOffset,
                       this.animationTimeSeconds * 0.6f, //x
                       1.0f / (this.noiseScale / 8.0f),     //y
                       0,                                   //z
                       this.screenOffset);                  //w

    GLES20.glUniform3f(primaryColor,
                       WallpaperRenderer.primaryColor[0], //R
                       WallpaperRenderer.primaryColor[1], //G
                       WallpaperRenderer.primaryColor[2]); //B

    GLES20.glEnableVertexAttribArray(textureCoords);
    GLES20.glEnableVertexAttribArray(position);
    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getId());

    GLES20.glVertexAttribPointer(textureCoords, 2, GLES20.GL_FLOAT, false, 0, vertexBufferObject.getTextureBuffer());
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferObject.getVertexBuffer());

    GLES20.glVertexAttribPointer(position, 3, GLES20.GL_FLOAT, false, 0, 0);
    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vertexBufferObject.getIndexBuffer());

    GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, vertexBufferObject.getIndexSize(), GLES20.GL_UNSIGNED_SHORT, 0);

    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    checkGlError("WallpaperRenderer.renderBackground");
  }

  private void renderForeground() {
    this.foregroundShaders.use();

    final int position = this.foregroundShaders.getAttributeHandle("aPosition");
    final int MVPMatrix = this.foregroundShaders.getUniformHandle("uMVPMatrix");
    final int time_noiseScale_color = this.foregroundShaders.getUniformHandle("u_Time_NoiseScale_Color");
    final int noise = this.foregroundShaders.getUniformHandle("u_Noise");
    final int primaryColor = this.foregroundShaders.getUniformHandle("u_PrimaryColor");
    final int secondaryColor = this.foregroundShaders.getUniformHandle("u_SecondaryColor");
    GLES20.glUniform3f(time_noiseScale_color,
                       this.animationTimeSeconds * 0.6f, //time
                       this.noiseScale,                     //noiseScale
                       0);                                  //color
    GLES20.glUniformMatrix4fv(MVPMatrix, 1, false, this.MVPMatrix, 0);


    final float n = (float)Math.cos(this.animationTimeSeconds * 0.02 * 0.6);
    final float n2 = (float)Math.sin(this.animationTimeSeconds * 0.02 * 0.6);
    final float n3 = (float)Math.sin(this.animationTimeSeconds * 0.01 * 0.6);
    final float n4 = (float)Math.cos(this.animationTimeSeconds * 0.01 * 0.6);
    final float n5 = (float)Math.sin(this.animationTimeSeconds * 0.005 * 0.6);
    final float n6 = (float)Math.cos(this.animationTimeSeconds * 0.005 * 0.6);
        GLES20.glUniform4f(noise,
            0.45f + 0.7f * (0.32f * n + 0.1f * n3 + 0.07f * n5),
            0.5f + 0.7f * (0.3f * n2 + 0.1f * n4 + 0.07f * n6),
            0.0f,
            0.0f);

    GLES20.glUniform3f(primaryColor,
                       WallpaperRenderer.primaryColor[0], //R
                       WallpaperRenderer.primaryColor[1], //G
                       WallpaperRenderer.primaryColor[2]); //B
    GLES20.glUniform3f(secondaryColor,
                       WallpaperRenderer.secondaryColor[0], //R
                       WallpaperRenderer.secondaryColor[1], //G
                       WallpaperRenderer.secondaryColor[2]); //B

    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.foregroundTexture.getId());
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.foregroundVBO.getVertexBuffer());
    GLES20.glVertexAttribPointer(position, 3, GLES20.GL_FLOAT, false, 0, 0);
    GLES20.glEnableVertexAttribArray(position);
    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, this.foregroundVBO.getIndexBuffer());
    GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, this.foregroundVBO.getIndexSize(), GLES20.GL_UNSIGNED_SHORT, 0);
    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    checkGlError("WallpaperRenderer.renderForeground");
  }



  private void updateSpeed() {
    final float n = SystemClock.uptimeMillis() - this.lastTouchTime;
    if (n > 10000) {
      this.animationSpeedFactor = 0.5f;
    }
    else if (n > 6000) {
      this.animationSpeedFactor =
              (float)((Math.cos(
                      (n - 6000.0f) / 1000.0f / 4.0f * 3.14
              ) + 1.0) / 2.0) * 0.5f + 0.5f;
    }
    else {
      this.animationSpeedFactor = 1.0f;
    }
    if (n > 9000.0f) {
      this.frameDeltaTime = 83.333336f;
    }
    else if (n > 3000.0f) {
      this.frameDeltaTime = 50.0f;
    }
    else {
      this.frameDeltaTime = 33.333332f;
    }
  }

  public void destroy() {
    this.isDestroyed = true;
    if (this.backgroundShaders != null) {
      this.backgroundShaders.destroy();
      this.backgroundShaders = null;
    }
    if (this.foregroundShaders != null) {
      this.foregroundShaders.destroy();
      this.foregroundShaders = null;
    }
    if (this.foregroundTexture != null) {
      this.foregroundTexture.destroy();
      this.foregroundTexture = null;
    }
    if (this.backgroundTexture != null) {
      this.backgroundTexture.destroy();
      this.backgroundTexture = null;
    }
    if (this.backgroundVBO != null) {
      this.backgroundVBO.destroy();
      this.backgroundVBO = null;
    }
    if (this.foregroundVBO != null) {
      this.foregroundVBO.destroy();
      this.foregroundVBO = null;
    }
    if (this.backgroundImage != null) {
      this.backgroundImage.recycle();
    }
    if (this.foregroundImage != null) {
      this.foregroundImage.recycle();
    }
  }

  public void onDrawFrame(final GL10 gl10) {
    if (!this.isDestroyed) {
      if (this.timeTouchBonus > this.animationTimeMillis) {
        this.animationTimeMillis += 0.078f * (this.timeTouchBonus - this.animationTimeMillis) % Float.MAX_VALUE;
      }
      final float mLastAnimationTimeUpdate = (float)SystemClock.uptimeMillis();
      final float n = (mLastAnimationTimeUpdate - this.lastAnimationTimeRefresh) * this.animationSpeedFactor;
      this.lastAnimationTimeRefresh = mLastAnimationTimeUpdate;
      if (n > 0.0f && !this.isFirstFrame) {
        float n2 = Math.min(n, 83.333336f);
        this.animationTimeMillis += n2;
        this.animationTimeMillis %= Float.MAX_VALUE;
      }
      this.animationTimeSeconds = this.animationTimeMillis / 1000.0f;
      this.updateSpeed();
      final float n3 = SystemClock.uptimeMillis() - this.lastFrameTime;
      float mCurrentFrameDeltaTime;
      if (n3 > 0.0f) {
        mCurrentFrameDeltaTime = this.frameDeltaTime - n3;
      }
      else {
        mCurrentFrameDeltaTime = this.frameDeltaTime;
      }
      if (mCurrentFrameDeltaTime > 0.0f && !this.isFirstFrame) {
        this.frameSkipper.sleep((long)mCurrentFrameDeltaTime);
      }
      this.isFirstFrame = false;
      this.lastFrameTime = (float)SystemClock.uptimeMillis();
      this.render(gl10);
    }
  }

  public void onOffsetChanged(final float offset) {
    this.screenOffset = (offset - 0.5f) * 0.5f;
    this.matrixSetUp();
    if (!this.isTouched && this.delta > 0.0f) {
      this.accelerateOnTouch();
    }
  }

  public void onSurfaceChanged(final GL10 gl10, final int mViewportWidth, final int mViewportHeight) {
    GLES20.glViewport(0, 0, mViewportWidth,mViewportHeight);
    GLES20.glEnable(GL10.GL_MULTISAMPLE); //GL_MULTISAMPLE
    GLES20.glGetError();//Don't touch. Resets glError if GL_MULTISAMPLE is not supported
    float ratio;
    if (mViewportWidth <= mViewportHeight) {
     ratio = mViewportWidth / (float) mViewportHeight;
      Matrix.frustumM(this.perspectiveMatrix, 0, -ratio, ratio, -1.0f, 1.0f, 2.5f, 10.0f);
    }
    else {
      ratio = mViewportHeight / (float) mViewportWidth;
      Matrix.frustumM(this.perspectiveMatrix, 0, -0.7f, 0.7f, -ratio, ratio, 2.5f, 10.0f);
    }
    Matrix.setLookAtM(this.viewMatrix, 0, 0.0f, 0.0f, -5.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    this.matrixSetUp();
    this.lastFrameTime = (float)SystemClock.uptimeMillis();
    checkGlError("WallpaperRenderer.onSurfaceChanged");
  }



  public void onSurfaceCreated(final GL10 gl10, final EGLConfig eglConfig) {
    GLES20.glEnable(GLES20.GL_BLEND);
    GLES20.glDisable(GLES20.GL_DITHER);
    updateColorPickerColors();

    if (!color.isDarkMode){
      GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_SRC_COLOR);
      GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

    } else {
      GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);
      GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    this.createTextures();
    this.createShaders();
    if (this.backgroundVBO != null) {
      this.backgroundVBO.destroy();
    }
    this.backgroundVBO = new VertexBufferObject(MeshPrimitive.newSquareMeshPrimitive(1.0f, 1.0f));
    if (this.foregroundVBO != null) {
      this.foregroundVBO.destroy();
    }
    this.foregroundVBO = new VertexBufferObject(MeshPrimitive.newForegroundPrimitive((short)100));
    this.matrixSetUp();
    checkGlError("WallpaperRenderer.onSurfaceCreated");
    this.isDestroyed = false;


  }

  public void onTap(final int n, final int n2) {
    this.accelerateOnTouch();
  }


  public void onTouchEvent(final MotionEvent motionEvent) {
    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
      this.isTouched = true;
     // accelerateOnTouch();
    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
      this.isTouched = false;
    }
  }

  public void onVisibilityChanged(final boolean isVisible, final boolean b2) {
    if (isVisible) {

      updateColorPickerColors();
      this.lastFrameTime = (float)SystemClock.uptimeMillis();
      this.frameSkipper.wake();
      this.isFirstFrame = true;
    }
  }
  private void updateColorPickerColors(){

    color.loadFromSettings();
    this.setPrimaryColor(color.currentColor[0]);
    this.setSecondaryColor(color.currentColor[1]);
  }

  public void setPrimaryColor(final float[] array) {
    WallpaperRenderer.primaryColor[0] = array[0];
    WallpaperRenderer.primaryColor[1] = array[1];
    WallpaperRenderer.primaryColor[2] = array[2];
  }

  public void setSecondaryColor(final float[] array) {
    WallpaperRenderer.secondaryColor[0] = array[0];
    WallpaperRenderer.secondaryColor[1] = array[1];
    WallpaperRenderer.secondaryColor[2] = array[2];
  }

  public void release() {
    destroy();
  }

  static class FrameSkipper {
    private boolean isSkipping = false;

    public void sleep(float sleepTime) {
      synchronized (this) {
        if (sleepTime > 0f) {
          this.isSkipping = true;
          try {
            wait((long) sleepTime);
          } catch (InterruptedException ignored) {
          }
          this.isSkipping = false;
        }
      }
    }

    public void wake() {
      synchronized (this) {
        if (this.isSkipping) {
          this.notifyAll();
        }
      }
    }
  }
}

