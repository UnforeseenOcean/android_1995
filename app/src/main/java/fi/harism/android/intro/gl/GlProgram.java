package fi.harism.android.intro.gl;

import android.opengl.GLES30;
import android.util.Log;

import java.lang.reflect.Field;

public class GlProgram {

    private int program;

    public GlProgram(String vertSource, String fragSource) throws Exception {
        int vertShader = 0, fragShader = 0;
        try {
            vertShader = vertSource != null ? compileShader(vertSource, GLES30.GL_VERTEX_SHADER) : 0;
            fragShader = fragSource != null ? compileShader(fragSource, GLES30.GL_FRAGMENT_SHADER) : 0;

            program = GLES30.glCreateProgram();
            if (program != 0 &&
                    (vertSource == null || vertShader != 0) &&
                    (fragSource == null || fragShader != 0)) {
                final int[] linkStatus = {GLES30.GL_FALSE};
                if (vertShader != 0) GLES30.glAttachShader(program, vertShader);
                if (fragShader != 0) GLES30.glAttachShader(program, fragShader);
                GLES30.glLinkProgram(program);
                GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);
                if (linkStatus[0] != GLES30.GL_TRUE) {
                    String infoLog = GLES30.glGetProgramInfoLog(program);
                    throw new Exception(infoLog.isEmpty() ? "Link program failed empty." : infoLog);
                }
            }
        } catch (Exception ex) {
            GLES30.glDeleteProgram(program);
            program = 0;
            throw ex;
        } finally {
            GLES30.glDeleteShader(vertShader);
            GLES30.glDeleteShader(fragShader);
            GlUtils.checkGLErrors();
        }
    }

    public GlProgram useProgram() {
        GLES30.glUseProgram(program);
        GlUtils.checkGLErrors();
        return this;
    }

    public int getAttribLocation(String name) {
        int location = GLES30.glGetAttribLocation(program, name);
        GlUtils.checkGLErrors();
        return location;
    }

    public int getUniformLocation(String name) {
        int location = GLES30.glGetUniformLocation(program, name);
        GlUtils.checkGLErrors();
        if (location == -1) {
            Log.d("GlProgram", "Uniform '" + name + "' not found.");
        }
        return location;
    }

    public GlProgram getUniformIndices(String names[], int indices[]) {
        GLES30.glGetUniformIndices(program, names, indices, 0);
        GlUtils.checkGLErrors();
        for (int i = 0; i < names.length; ++i) {
            if (indices[i] == -1) {
                Log.d("GlProgram", "Uniform '" + names[i] + "' not found.");
            }
        }
        return this;
    }

    public GlProgram getUniformIndices(Object obj) throws Exception {
        for (Field field : obj.getClass().getFields()) {
            field.setInt(obj, getUniformLocation(field.getName()));
        }
        return this;
    }

    private int compileShader(String shaderSource, int shaderType) throws Exception {
        int shader = GLES30.glCreateShader(shaderType);
        try {
            if (shader != 0) {
                final int[] compileStatus = {GLES30.GL_FALSE};
                GLES30.glShaderSource(shader, shaderSource);
                GLES30.glCompileShader(shader);
                GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0);
                if (compileStatus[0] != GLES30.GL_TRUE) {
                    String infoLog = GLES30.glGetShaderInfoLog(shader);
                    throw new Exception(infoLog.isEmpty() ? "compileShader failed empty." : infoLog);
                }
            }
            return shader;
        } catch (Exception ex) {
            GLES30.glDeleteShader(shader);
            throw ex;
        } finally {
            GlUtils.checkGLErrors();
        }
    }

}
