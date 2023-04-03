package com.nixiedroid.magiclines;

import javax.microedition.khronos.egl.EGL10;

public enum GLConfig {
        PRIMARY(new int[]{
            EGL10.EGL_RED_SIZE, 8, 8,
            EGL10.EGL_GREEN_SIZE, 8, 8,
            EGL10.EGL_BLUE_SIZE, 8, 8,
            EGL10.EGL_ALPHA_SIZE, 8, 8,
            EGL10.EGL_DEPTH_SIZE, 0, 24,
            EGL10.EGL_STENCIL_SIZE, 0, 8,
            EGL10.EGL_SAMPLE_BUFFERS, 0, 1,
            EGL10.EGL_SAMPLES, 2, 2,
            EGL10.EGL_RENDERABLE_TYPE, 0, 7}),
    FALLBACK(new int[]{
            EGL10.EGL_RED_SIZE, 8, 8,
            EGL10.EGL_GREEN_SIZE, 8, 8,
            EGL10.EGL_BLUE_SIZE, 8, 8,
            EGL10.EGL_ALPHA_SIZE, 8, 8,
            EGL10.EGL_DEPTH_SIZE, 0, 24,
            EGL10.EGL_STENCIL_SIZE, 0, 8,
            EGL10.EGL_SAMPLE_BUFFERS, 0, 1,
            EGL10.EGL_SAMPLES, 0, 4,
            EGL10.EGL_RENDERABLE_TYPE, 0, 7});


   private final int[] config;

    GLConfig(int[] config)
    {
        this.config = config;
    }

    public int[] getConfig() {
        return config;
    }
}
