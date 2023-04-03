package com.nixiedroid.magiclines;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import com.nixiedroid.magiclines.color.Color;
//import android.opengl.EGLConfig;
//import android.opengl.EGLContext;

import javax.microedition.khronos.egl.EGL10;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

public class Wallpaper extends GLWallpaperService {

public Wallpaper() {
    super();
}

    @Override
    public Engine onCreateEngine() {
        return new WallpaperEngine();
    }

    static class WallpaperEGLConfigChooser implements GLSurfaceView.EGLConfigChooser
    {
        private final int[] fallbackConfig;
        private final int[] primaryConfig;

        public WallpaperEGLConfigChooser(final int[] primaryConfig, final int[] fallbackConfig) {
            super();
            this.primaryConfig = primaryConfig;
            this.fallbackConfig = fallbackConfig;
        }

        @Override
        public EGLConfig chooseConfig(final EGL10 egl10, final EGLDisplay eglDisplay) {
            int[] selectedConfig = this.findBestConfigSpec(egl10, eglDisplay, this.primaryConfig);
            if (!this.hasMultiSampleSupport(selectedConfig)) {
                selectedConfig = this.findBestConfigSpec(egl10, eglDisplay, this.fallbackConfig);
            }
            final int[] eglChooseConfigResult = { 0 };
            if (!egl10.eglChooseConfig(eglDisplay, selectedConfig, null, 0, eglChooseConfigResult)) {
                throw new IllegalArgumentException("eglChooseConfig failed");
            }
            final int configSize = eglChooseConfigResult[0];
            if (configSize <= 0) {
                throw new IllegalArgumentException("No configs match configSpec");
            }
            final EGLConfig[] configs = new EGLConfig[configSize];
            if (!egl10.eglChooseConfig(eglDisplay, selectedConfig, configs, configSize, eglChooseConfigResult)) {
                throw new IllegalArgumentException("eglChooseConfig failed");
            }
            return configs[0];
        }

        public int[] findBestConfigSpec(final EGL10 egl10, final EGLDisplay eglDisplay, final int[] protoConfig) {
            final int configResultSize = protoConfig.length / 3 * 2;
            final int[] bestConfig = new int[configResultSize + 1];
            bestConfig[configResultSize] = EGL10.EGL_NONE;
            for (int i = 0, n2 = 0; i < configResultSize; i += 2, n2 += 3) {
                bestConfig[i] = protoConfig[n2];
                bestConfig[i + 1] = protoConfig[n2 + 1];
            }
            final int[] truncatedConfig = new int[configResultSize];
            System.arraycopy(bestConfig, 0, truncatedConfig, 0, configResultSize);
            final EGLConfig[] supportedConfigs = new EGLConfig[100];
            final int[] array5 = new int[100];
            egl10.eglGetConfigs(eglDisplay, supportedConfigs, array5.length, array5);
            final int[] eglConfigValue = { 0 };
            for (EGLConfig eglConfig : supportedConfigs) {
                if (eglConfig != null) {
                    int truncatedConfigPointer = 0;
                    int protoConfigPointer = 0;
                    int copyDirection;
                    while (true) {
                        copyDirection = 1;
                        if (truncatedConfigPointer >= configResultSize) {
                            break;
                        }
                        egl10.eglGetConfigAttrib(eglDisplay, eglConfig, truncatedConfig[truncatedConfigPointer], eglConfigValue);
                        if (eglConfigValue[0] < truncatedConfig[truncatedConfigPointer + 1] || eglConfigValue[0] > protoConfig[protoConfigPointer + 2]) {
                            copyDirection = 0;
                            break;
                        }
                        truncatedConfig[truncatedConfigPointer + 1] = eglConfigValue[0];
                        truncatedConfigPointer += 2;
                        protoConfigPointer += 3;
                    }
                    if (copyDirection != 0) {
                        System.arraycopy(truncatedConfig, 0, bestConfig, 0, configResultSize);
                    } else {
                        System.arraycopy(bestConfig, 0, truncatedConfig, 0, configResultSize);
                    }
                }
            }
            return bestConfig;
        }

        public boolean hasMultiSampleSupport(final int[] array) {
            boolean answer = false;
            for (int i = 0; i < array.length; ++i) {
                if (array[i] == GL10.GL_MULTISAMPLE) {
                    if (array[i + 1] > 0) {
                        answer = true;
                    }
                }
            }
            return answer;
        }
    }

    class WallpaperEngine extends GLEngine
    {
        private Color color = new Color(getApplicationContext());
        private WallpaperRenderer renderer;
        private final WallpaperEGLConfigChooser configChooser;


        public WallpaperEngine() {
            super();

            this.renderer = new WallpaperRenderer(Wallpaper.this,color);
            setEGLConfigChooser(this.configChooser = new WallpaperEGLConfigChooser(GLConfig.PRIMARY.getConfig(), GLConfig.FALLBACK.getConfig()));
            setEGLContextFactory(new GLSurfaceView.EGLContextFactory() {
                private final int EGL_CONTEXT_CLIENT_VERSION = 12440; //From EGL.14

                @Override
                public EGLContext createContext(final EGL10 egl10, final EGLDisplay eglDisplay, final EGLConfig eglConfig) {
                    return egl10.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, new int[] { this.EGL_CONTEXT_CLIENT_VERSION, 2, 12344 });
                }

                @Override
                public void destroyContext(final EGL10 egl10, final EGLDisplay eglDisplay, final EGLContext eglContext) {
                    if (renderer != null) {
                        renderer.destroy();
                        renderer = null;
                    }
                    egl10.eglDestroyContext(eglDisplay, eglContext);
                }
            });
            this.setRenderer(this.renderer);
            this.setTouchEventsEnabled(false);
        }

        public GLSurfaceView.Renderer getRenderer() {
            return renderer;
        }

        public WallpaperEGLConfigChooser getWallpaperEGLConfigChooser() {
            return this.configChooser;
        }

        public Bundle onCommand(final String s, final int n, final int n2, final int n3, final Bundle bundle, final boolean b) {
            if (this.renderer != null && ("android.wallpaper.tap".equals(s) )) {
                this.renderer.onTap(n, n2);
            }
            return super.onCommand(s, n, n2, n3, bundle, b);
        }

//        @Override
//        public void onDestroy() {
//            super.onDestroy();
//        }
        public void onOffsetsChanged(final float xOffset, final float yOffset, final float xOffsetStep, final float yOffsetStep, final int xPixels, final int yPixels) {
            if (this.renderer != null) {
                //System.out.println("OFFSET is " + xOffset + ", " + yOffset + ", " + xOffsetStep + ", " + yOffsetStep + ", " + xPixels + ", " + yPixels   );
                this.renderer.onOffsetChanged(xOffset);
            }
        }

        public void onTouchEvent(final MotionEvent motionEvent) {
            if (this.renderer != null) {
                this.renderer.onTouchEvent(motionEvent);
            }
        }

        @Override
        public void onVisibilityChanged(final boolean isVisible) {
            super.onVisibilityChanged(isVisible);
            this.renderer.onVisibilityChanged(isVisible, this.isPreview());
        }
    }
}
