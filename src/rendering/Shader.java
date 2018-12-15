package rendering;

import java.io.BufferedReader;
import java.io.FileReader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class Shader {

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
}
