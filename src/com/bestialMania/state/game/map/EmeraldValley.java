package com.bestialMania.state.game.map;

import com.bestialMania.collision.BoundingBox;
import com.bestialMania.collision.TriangleLoader;
import com.bestialMania.object.Object3D;
import com.bestialMania.object.StaticObject;
import com.bestialMania.rendering.Texture;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.rendering.model.loader.Loader;
import com.bestialMania.rendering.shader.Shader;
import com.bestialMania.collision.Floor;
import com.bestialMania.state.game.Game;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Arrays;

public class EmeraldValley extends MapData{
    public EmeraldValley() {
        boundingBox = new BoundingBox(-32,-100,-32,32,100,32);
    }


    @Override
    public Vector3f getLightDirection() {
        return new Vector3f(-1.4f, -0.5f, 2.5f).normalize();
    }

    @Override
    public Vector3f getLightColor() {
        return new Vector3f(1.0f, 1.0f, 1.0f);
    }

    @Override
    public String getSkyboxTexture() {
        return "res/textures/skyboxes/test/desertsky";
    }

    @Override
    public String getMusic() {
        return "res/sound/pumped_up_kicks.wav";
    }

    /**
     * Load shaders into the game
     * 2 = regular renderer
     * 3 = normalmap renderer
     */
    @Override
    public void loadShaders(Game game) {
        //regular shader
        Shader testShader = new Shader("res/shaders/3d/test_v.glsl","res/shaders/3d/test_f.glsl");
        testShader.bindTextureUnits(Arrays.asList("textureSampler"));
        game.loadShader(testShader,true,true,false,false);

        //normalmap shader
        if(game.usesNormalMapping()) {
            Shader normalmapShader = new Shader("res/shaders/3d/normalmap_v.glsl","res/shaders/3d/normalmap_f.glsl");
            normalmapShader.bindTextureUnits(Arrays.asList("textureSampler","normalSampler","shadowSampler[0]","shadowSampler[1]","shadowSampler[2]"));
            game.loadShader(normalmapShader,true,true,true,false);
        }

    }

    /**
     * Load objects into the game
     */
    @Override
    public void loadObjects(Game game) {
        //SOME POLE OBJECT
        Model poleModel = Loader.loadOBJ(game.getMemoryManager(),"res/models/pole.obj");
        Texture poleTexture = Texture.loadImageTexture3D(game.getMemoryManager(),"res/textures/concrete.png");
        Matrix4f testObjectMatrix = new Matrix4f();
        testObjectMatrix.translate(2.0f,0,0.5f);
        testObjectMatrix.scale(3.0f,3.0f,3.0f);
        if(game.usesNormalMapping()) {
            Texture poleNormalmap = Texture.loadImageTexture3D(game.getMemoryManager(), "res/textures/concrete_normal.png");
            Object3D object = new StaticObject(game, poleModel,testObjectMatrix,poleTexture, poleNormalmap,0.5f,10.0f);
            game.createObject(object,3,true);
        }else{
            Object3D object = new StaticObject(game, poleModel,testObjectMatrix,poleTexture,0.5f,10.0f);
            game.createObject(object,2,true);
        }

        //SOME FLOOR OBJECT
        Model planeModel = Loader.loadOBJ(game.getMemoryManager(),"res/models/plane.obj");
        Matrix4f planeMatrix = new Matrix4f();
        Texture planeTexture = Texture.loadImageTexture3D(game.getMemoryManager(),"res/textures/rocky.png");
        if(game.usesNormalMapping()) {
            Texture planeNormalmap = Texture.loadImageTexture3D(game.getMemoryManager(), "res/textures/rocky_normal.png");
            Object3D object = new StaticObject(game, planeModel,planeMatrix,planeTexture, planeNormalmap,0.1f,4.0f);
            game.createObject(object,3,true);
        }
        else{
            Object3D object = new StaticObject(game, planeModel,planeMatrix,planeTexture,0.1f,4.0f);
            game.createObject(object,2,true);
        }

    }

    /**
     * TODO load actual floor collisions
     */
    @Override
    public Floor loadFloor() {
        Floor floor = new Floor(boundingBox);
        floor.addTriangles(TriangleLoader.loadTrianglesOBJ("res/models/plane.obj"));

        return floor;
    }
}
