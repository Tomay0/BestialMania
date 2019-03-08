package com.bestialMania.state.game.map;

import com.bestialMania.collision.BoundingBox;
import com.bestialMania.collision.CollisionHandler;
import com.bestialMania.collision.TriangleLoader;
import com.bestialMania.object.Object3D;
import com.bestialMania.object.StaticObject;
import com.bestialMania.rendering.Texture;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.rendering.model.loader.Loader;
import com.bestialMania.rendering.shader.Shader;
import com.bestialMania.state.game.Game;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Arrays;

public class EmeraldValley extends MapData{
    public EmeraldValley() {
        boundingBox = new BoundingBox(-32,-10,-32,32,20,32);
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
        return "res/sound/vsauce.wav";
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
            normalmapShader.bindTextureUnits(Arrays.asList("textureSampler","normalSampler","shadowSampler0","shadowSampler1","shadowSampler2"));
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
        Model planeModel2 = Loader.loadOBJ(game.getMemoryManager(), "res/models/plane2.obj");
        Matrix4f planeMatrix = new Matrix4f();
        Texture planeTexture = Texture.loadImageTexture3D(game.getMemoryManager(),"res/textures/rocky.png");
        if(game.usesNormalMapping()) {
            Texture planeNormalmap = Texture.loadImageTexture3D(game.getMemoryManager(), "res/textures/rocky_normal.png");
            Object3D object = new StaticObject(game, planeModel,planeMatrix,planeTexture, planeNormalmap,0.1f,4.0f);
            Object3D object2 = new StaticObject(game, planeModel2,planeMatrix,planeTexture, planeNormalmap,0.1f,4.0f);
            game.createObject(object,3,true);
            game.createObject(object2,3,true);
        }
        else{
            Object3D object = new StaticObject(game, planeModel,planeMatrix,planeTexture,0.1f,4.0f);
            Object3D object2 = new StaticObject(game, planeModel2,planeMatrix,planeTexture,0.1f,4.0f);
            game.createObject(object,2,true);
            game.createObject(object2,2,true);
        }

        //SOME WALL OBJECT
        Model wallModel = Loader.loadOBJ(game.getMemoryManager(), "res/models/wall.obj");
        Texture wallTexture = Texture.loadImageTexture3D(game.getMemoryManager(),"res/textures/sexy.png");
        Object3D wallObject = new StaticObject(game,wallModel,new Matrix4f(),wallTexture,0.5f,10.0f);
        game.createObject(wallObject,2,true);
        //CEILING
        Model ceilModel = Loader.loadOBJ(game.getMemoryManager(), "res/models/ceil.obj");
        Object3D ceilObject = new StaticObject(game,ceilModel,new Matrix4f(),wallTexture,0.5f,10.0f);
        game.createObject(ceilObject,2,true);

        /*Model wallModel2 = Loader.loadOBJ(game.getMemoryManager(), "res/models/wall2.obj");
        Object3D wallObject2 = new StaticObject(game,wallModel2,new Matrix4f(),wallTexture,0.5f,10.0f);
        game.createObject(wallObject2,2,true);
        Matrix4f wallMatrix = new Matrix4f();
        wallMatrix.translate(-10,1.2f,-20);
        Model wallModel3 = Loader.loadOBJ(game.getMemoryManager(), "res/models/wall3.obj");
        Object3D wallObject3 = new StaticObject(game,wallModel3,wallMatrix,wallTexture,0.5f,10.0f);
        game.createObject(wallObject3,2,true);*/

        //STAIRS
        Model stairsModel = Loader.loadOBJ(game.getMemoryManager(),"res/models/stairs.obj");
        Matrix4f stairMatrix = new Matrix4f();
        stairMatrix.translate(-10,0,0);
        stairMatrix.scale(5,2,5);
        if(game.usesNormalMapping()) {
            Texture stairsNormalmap = Texture.loadImageTexture3D(game.getMemoryManager(), "res/textures/concrete_normal.png");
            Object3D object = new StaticObject(game, stairsModel,stairMatrix,poleTexture, stairsNormalmap,0.1f,4.0f);
            game.createObject(object,3,true);
        }
        else{
            Object3D object = new StaticObject(game, stairsModel,stairMatrix,poleTexture,0.1f,4.0f);
            game.createObject(object,2,true);
        }
    }

    /**
     * Loads the floor and wall collisions
     */
    @Override
    public CollisionHandler loadCollisions() {
        CollisionHandler collisionHandler = new CollisionHandler(boundingBox);
        collisionHandler.addFloor(TriangleLoader.loadTrianglesOBJ("res/models/plane.obj"));
        collisionHandler.addFloor(TriangleLoader.loadTrianglesOBJ("res/models/plane2.obj"));
        collisionHandler.addWalls(TriangleLoader.loadTrianglesOBJ("res/models/wall.obj"));
        collisionHandler.addCeiling(TriangleLoader.loadTrianglesOBJ("res/models/ceil.obj"));
        //collisionHandler.addWalls(TriangleLoader.loadTrianglesOBJ("res/models/wall2.obj"));
        Matrix4f testObjectMatrix = new Matrix4f();
        testObjectMatrix.translate(2.0f,0,0.5f);
        testObjectMatrix.scale(3.0f,3.0f,3.0f);
        collisionHandler.addWalls(TriangleLoader.loadTrianglesOBJ("res/models/poleWall.obj",testObjectMatrix));

        Matrix4f stairMatrix = new Matrix4f();
        stairMatrix.translate(-10,0,0);
        stairMatrix.scale(5,2,5);
        collisionHandler.addFloor(TriangleLoader.loadTrianglesOBJ("res/models/stairFloor.obj",stairMatrix));
        collisionHandler.addWalls(TriangleLoader.loadTrianglesOBJ("res/models/stairWalls.obj",stairMatrix));
        //Matrix4f wallMatrix = new Matrix4f();
        //wallMatrix.translate(-10,1.2f,-20);
        //collisionHandler.addWalls(TriangleLoader.loadTrianglesOBJ("res/models/wall3.obj",wallMatrix));
        return collisionHandler;
    }
}
