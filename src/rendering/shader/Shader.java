package rendering.shader;

import org.joml.*;
import org.lwjgl.BufferUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private static FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    private int program;//program ID

    /**
     * Creates a shader program
     * */
    public Shader(String vertex, String fragment) {
        program = glCreateProgram();

        int vshader = loadShader(vertex,GL_VERTEX_SHADER);
        int fshader = loadShader(fragment,GL_FRAGMENT_SHADER);

        glLinkProgram(program);
        glValidateProgram(program);

        //clean up
        glDetachShader(program,vshader);
        glDetachShader(program,fshader);
        glDeleteShader(vshader);
        glDeleteShader(fshader);
    }

    /**
     * Binds to this shader
     */
    public void bind() {
        glUseProgram(program);
    }

    /**
     * Gets the program ID
     */
    public int getProgram() {return program;}

    /**
     * Loads a shader from a file and return the ID
     */
    private int loadShader(String path, int type) {
        int shader = glCreateShader(type);

        //get source code
        StringBuilder shaderSource = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while((line = reader.readLine())!=null) {
                shaderSource.append(line).append("\n");
            }
            reader.close();
        }catch(Exception e) {
            System.err.println("Could not load shader: " + path);
            e.printStackTrace();
            System.exit(-1);
        }

        //compile
        glShaderSource(shader,shaderSource);
        glCompileShader(shader);

        //check for errors
        if(glGetShaderi(shader,GL_COMPILE_STATUS)==GL_FALSE) {
            System.err.println("Error loading shader: " + path);
            System.err.println(glGetShaderInfoLog(shader, 500));
            System.exit(-1);
        }


        //attach to program
        glAttachShader(program,shader);

        System.out.println("Compiled shader " + path);

        return shader;
    }

    /**
     * Get the location of a uniform
     */
    public int getUniformLocation(String uniform) {return glGetUniformLocation(program,uniform);}


    /**
     * Set an int uniform
     */
    public void setUniformInt(int location, int value) {
        glUniform1i(location,value);
    }
    /**
     * Set a float uniform
     */
    public void setUniformFloat(int location, float value) {
        glUniform1f(location,value);
    }
    /**
     * Set a vector2f uniform
     */
    public void setUniformVector2f(int location, Vector2f value) {
        glUniform2f(location,value.x,value.y);
    }
    /**
     * Set a vector3f uniform
     */
    public void setUniformVector3f(int location, Vector3f value) {
        glUniform3f(location,value.x,value.y,value.z);
    }
    /**
     * Set a vector4f uniform
     */
    public void setUniformVector4f(int location, Vector4f value) {
        glUniform4f(location,value.x,value.y,value.z,value.w);
    }

    /**
     * Set a vector2i uniform
     */
    public void setUniformVector2i(int location, Vector2i value) {
        glUniform2i(location,value.x,value.y);
    }
    /**
     * Set a vector3i uniform
     */
    public void setUniformVector3i(int location, Vector3i value) {
        glUniform3i(location,value.x,value.y,value.z);
    }
    /**
     * Set a vector4i uniform
     */
    public void setUniformVector4i(int location, Vector4i value) {
        glUniform4i(location,value.x,value.y,value.z,value.w);
    }

    /**
     * Set a 4x4 matrix uniform
     */
    public void setUniformMatrix4(int location,Matrix4f value) {
        value.get(matrixBuffer);
        glUniformMatrix4fv(location, false, matrixBuffer);
    }

}
