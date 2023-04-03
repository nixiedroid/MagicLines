package com.nixiedroid.magiclines;

import android.content.Context;

import java.io.InputStream;

public class ShaderFactory {
    private ShaderFactory() {
    }

    public static ShaderProgram createProgramFromString(final String vertexShader, final String fragmentShader) {
        return new ShaderProgram(vertexShader, fragmentShader);
    }

    public static String loadText(final int id, final Context context) {
        try(InputStream shader = context.getResources().openRawResource(id)) {
            int size = shader.available();
            byte[] b = new byte[size];
            int checkSize = shader.read(b);
            if (size!=checkSize) throw new Exception("Read and expected length mismatch!");
            return new String(b);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
