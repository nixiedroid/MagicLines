package com.nixiedroid.magiclines;

import android.opengl.GLES20;

import java.util.HashMap;

public class ShaderProgram {
    private int fragmentShaderId;
    private final HashMap<String, Integer> cache;
    private int programId;
    private int vertexShaderId;

    public ShaderProgram(final String vertexShader, final String fragmentShader) {
        this.cache = new HashMap<>();
        this.vertexShaderId = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        this.fragmentShaderId = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
        this.programId = createProgram(this.vertexShaderId, this.fragmentShaderId);
    }

    private static int createProgram(final int vertexShader, final int fragmentShader) {
        final int programId = GLES20.glCreateProgram();
        if (programId == 0) {
            throw new RuntimeException("Could not create shader program.");
        }
        GLES20.glAttachShader(programId, vertexShader);
        GLES20.glAttachShader(programId, fragmentShader);
        GLES20.glLinkProgram(programId);
        final int[] linkStatusResult = { 0 };
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatusResult, 0);
        if (linkStatusResult[0] != 1) {
            GLES20.glDeleteProgram(programId);
            throw new RuntimeException("Could not link program: " + GLES20.glGetProgramInfoLog(programId));
        }
        WallpaperRenderer.checkGlError("ShaderProgram.createProgram");
        return programId;
    }

    private static int loadShader(final int glShaderType, final String shaderData) {
        if (shaderData == null || shaderData.length() == 0) {
            throw new RuntimeException("Could not create " + glShaderType + " shader. Invalid input");
        }
        final int shaderId = GLES20.glCreateShader(glShaderType);
        WallpaperRenderer.checkGlError("ShaderProgram.loadShader - glCreateShader");
        if (shaderId == 0) {
            throw new RuntimeException("Could not create " + glShaderType + " shader due to GL error.");
        }
        GLES20.glShaderSource(shaderId, shaderData);
        GLES20.glCompileShader(shaderId);
        final int[] array = { 0 };
        GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, array, 0);
        if (array[0] == 0) {
            GLES20.glDeleteShader(shaderId);
            throw new RuntimeException("Could not compile shader: " + GLES20.glGetShaderInfoLog(shaderId) + "\n" + shaderData);
        }
        WallpaperRenderer.checkGlError("ShaderProgram.loadShader");
        return shaderId;
    }

    public void destroy() {
        GLES20.glDeleteProgram(this.programId);
        GLES20.glDeleteShader(this.vertexShaderId);
        GLES20.glDeleteShader(this.fragmentShaderId);
        this.programId = 0;
        this.vertexShaderId = 0;
        this.fragmentShaderId = 0;
        WallpaperRenderer.checkGlError("ShaderProgram.destroy");
    }

    public int getAttributeHandle(final String s) {
        Integer value;
        if ((value = this.cache.get(s)) == null) {
            final int glGetAttribLocation = GLES20.glGetAttribLocation(this.programId, s);
            WallpaperRenderer.checkGlError("ShaderProgram.getAttributeHandle");
            this.cache.put(s, glGetAttribLocation);
            value = glGetAttribLocation;
        }
        return value;
    }

    public int getProgramHandle() {
        return this.programId;
    }

    public int getUniformHandle(final String s) {
        Integer value;
        if ((value = this.cache.get(s)) == null) {
            final int glGetUniformLocation = GLES20.glGetUniformLocation(this.programId, s);
            WallpaperRenderer.checkGlError("ShaderProgram.getUniformHandle");
            this.cache.put(s, glGetUniformLocation);
            value = glGetUniformLocation;
        }
        return value;
    }

    public void use() {
        GLES20.glUseProgram(this.programId);
        WallpaperRenderer.checkGlError("ShaderProgram.use");
    }
}
