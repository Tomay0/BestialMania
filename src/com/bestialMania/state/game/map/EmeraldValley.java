package com.bestialMania.state.game.map;

import com.bestialMania.collision.BoundingBox;
import com.bestialMania.collision.CollisionLoader;
import com.bestialMania.object.Object3D;
import com.bestialMania.object.StaticObject;
import com.bestialMania.rendering.texture.Texture;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.rendering.model.loader.ModelLoader;
import com.bestialMania.rendering.shader.Shader;
import com.bestialMania.state.game.Game;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Arrays;

public class EmeraldValley extends MapData{
    private Matrix4f cubeMatrix,cubeMatrix2, poleMatrix,stairMatrix,poolMatrix;


    public EmeraldValley() {
        boundingBox = new BoundingBox(-32,-10,-32,32,20,32);
        poleMatrix = new Matrix4f();
        poleMatrix.translate(2.0f,0,0.5f);
        poleMatrix.scale(3.0f,3.0f,3.0f);
        stairMatrix = new Matrix4f();
        stairMatrix.translate(-10,0,0);
        stairMatrix.scale(5,2,5);
        cubeMatrix = new Matrix4f();
        cubeMatrix.translate(-5,2.5f,8);
        cubeMatrix2 = new Matrix4f();
        cubeMatrix2.translate(0,3.8f,8);
        poolMatrix = new Matrix4f();
        poolMatrix.translate(-37,0,5);
    }


    @Override
    public Vector3f getLightDirection() {
        return new Vector3f(-1.4f, -2.5f, 2.5f).normalize();
    }

    @Override
    public Vector3f getLightColor() {
        return new Vector3f(1.0f, 1.0f, 0.8f);
    }

    @Override
    public Vector3f getAmbientLight() {return new Vector3f(0.5f,0.5f,0.5f);}

    @Override
    public float getBrightness() {return 1.35f;}
    @Override
    public float getContrast() {return 0.15f;}
    @Override
    public float getSaturation() {return 1.15f;}


    @Override
    public String getSkyboxTexture() {
        return "res/textures/skyboxes/desertsky/desertsky";
    }

    @Override
    public String getMusic() {
        return "res/sound/vsauce.wav";
    }

    @Override
    public String getCollisions() {
        return "res/collisions/emeraldValley.bmc";

    }

    /**
     * Load shaders into the game
     * 2 = regular renderer
     * 3 = normalmap renderer
     */
    @Override
    public void loadShaders(Game game) {
        //regular shader
        Shader generalShader = new Shader("res/shaders/3d/general_v.glsl","res/shaders/3d/general_f.glsl");
        generalShader.bindTextureUnits(Arrays.asList("textureSampler"));
        game.loadShader(generalShader,true,true,false,false);

        Shader normalmapShader = new Shader("res/shaders/3d/normalmap_v.glsl","res/shaders/3d/normalmap_f.glsl");
        normalmapShader.bindTextureUnits(Arrays.asList("textureSampler","normalSampler","shadowSampler0","shadowSampler1","shadowSampler2"));
        game.loadShader(normalmapShader,true,true,true,false);

        Shader generalShader2 = new Shader("res/shaders/3d/general_shadow_v.glsl","res/shaders/3d/general_shadow_f.glsl");
        generalShader2.bindTextureUnits(Arrays.asList("textureSampler","textureSampler2","shadowSampler0","shadowSampler1","shadowSampler2"));
        game.loadShader(generalShader2,true,true,true,false);

        Shader bloomShader = new Shader("res/shaders/3d/shadeless_v.glsl","res/shaders/3d/bloom_f.glsl");
        bloomShader.bindTextureUnits(Arrays.asList("textureSampler"));
        game.loadShader(bloomShader,false,true,false,false);

    }

    /**
     * Load objects into the game
     */
    @Override
    public void loadObjects(Game game) {
        //SOME POLE OBJECT
        Model poleModel = ModelLoader.loadModel(game.getMemoryManager(),"res/models/pole.bmm");
        Texture poleTexture = Texture.loadImageTexture3D(game.getMemoryManager(),"res/textures/concrete.bmt");
        if(game.usesNormalMapping()) {
            Texture poleNormalmap = Texture.loadImageTexture3D(game.getMemoryManager(), "res/textures/concrete_normal.bmt");
            Object3D object = new StaticObject(game, poleModel,poleMatrix,poleTexture, poleNormalmap,0.5f,10.0f);
            game.createObject(object,3,true);
        }else{
            Object3D object = new StaticObject(game, poleModel,poleMatrix,poleTexture,0.5f,10.0f);
            game.createObject(object,2,true);
        }

        //SOME FLOOR OBJECT
        Model planeModel = ModelLoader.loadModel(game.getMemoryManager(),"res/models/plane.bmm");
        Model planeModel2 = ModelLoader.loadModel(game.getMemoryManager(), "res/models/plane2.bmm");
        Matrix4f planeMatrix = new Matrix4f();
        Texture planeTexture = Texture.loadImageTexture3D(game.getMemoryManager(),"res/textures/rocky.bmt");
        if(game.usesNormalMapping()) {
            Texture planeNormalmap = Texture.loadImageTexture3D(game.getMemoryManager(), "res/textures/rocky_normal.bmt");
            Object3D object = new StaticObject(game, planeModel,planeMatrix,planeTexture, planeNormalmap,0.1f,4.0f);
            Object3D object2 = new StaticObject(game, planeModel2,planeMatrix,planeTexture, planeNormalmap,0.1f,4.0f);
            game.createObject(object,3,true);
            game.createObject(object2,3,true);
        }
        else{
            Object3D object1 = new StaticObject(game, planeModel,planeMatrix,planeTexture,0.1f,4.0f);
            Object3D object2 = new StaticObject(game, planeModel2,planeMatrix,planeTexture,0.1f,4.0f);
            game.createObject(object1,2,true);
            game.createObject(object2,2,true);
        }

        //SOME WALL OBJECT
        Model wallModel = ModelLoader.loadModel(game.getMemoryManager(), "res/models/wall.bmm");
        Texture wallTexture = Texture.loadImageTexture3D(game.getMemoryManager(),"res/textures/sexy.bmt");
        Object3D wallObject = new StaticObject(game,wallModel,new Matrix4f(),wallTexture,0.5f,10.0f);
        game.createObject(wallObject,2,true);
        //CEILING
        //Model ceilModel = ModelLoader.loadModel(game.getMemoryManager(), "res/models/ceil.bmm");
        //Object3D ceilObject = new StaticObject(game,ceilModel,new Matrix4f(),wallTexture,0.5f,10.0f);
        //game.createObject(ceilObject,2,true);

        //STAIRS
        Model stairsModel = ModelLoader.loadModel(game.getMemoryManager(),"res/models/stairs.bmm");
        if(game.usesNormalMapping()) {
            Texture stairsNormalmap = Texture.loadImageTexture3D(game.getMemoryManager(), "res/textures/concrete_normal.bmt");
            Object3D object = new StaticObject(game, stairsModel,stairMatrix,poleTexture, stairsNormalmap,0.1f,4.0f);
            game.createObject(object,3,true);
        }
        else{
            Object3D object = new StaticObject(game, stairsModel,stairMatrix,poleTexture,0.1f,4.0f);
            game.createObject(object,2,true);
        }
        //cube
        Texture pinkTex = Texture.loadImageTexture3D(game.getMemoryManager(),"res/textures/pink.bmt");
        Model cubeModel = ModelLoader.loadModel(game.getMemoryManager(),"res/models/cube.bmm");
        Object3D cube = new StaticObject(game,cubeModel,cubeMatrix,pinkTex,1.0f);
        game.createObject(cube,5,false);
        Object3D cube2 = new StaticObject(game,cubeModel,cubeMatrix2,pinkTex,1.0f);
        game.createObject(cube2,5,false);

        //pool
        Model poolModel = ModelLoader.loadModel(game.getMemoryManager(),"res/models/pool.bmm");
        if(game.usesNormalMapping()) {
            Texture poolNormalmap = Texture.loadImageTexture3D(game.getMemoryManager(), "res/textures/concrete_normal.bmt");
            Object3D object = new StaticObject(game, poolModel,poolMatrix,poleTexture, poolNormalmap,0.1f,4.0f);
            game.createObject(object,3,true);
        }
        else{
            Object3D object = new StaticObject(game, poolModel,poolMatrix,poleTexture,0.1f,4.0f);
            game.createObject(object,2,true);
        }

        //water IN DEVELOPMENT
        Model waterModel = ModelLoader.loadModel(game.getMemoryManager(),"res/models/water.bmm");
        Object3D water = new StaticObject(game,waterModel,poolMatrix,pinkTex,0.4f);
        game.createObject(water,5,false);

    }

    /**
     * Create a collision loader with several OBJ files
     * This collision loader can be used to
     */
    @Override
    public CollisionLoader loadCollisions() {
        CollisionLoader collisionLoader = new CollisionLoader();
        collisionLoader.loadFloors("toConvert/collisions/plane.obj", new Matrix4f());
        collisionLoader.loadFloors("toConvert/collisions/plane2.obj", new Matrix4f());
        collisionLoader.loadWalls("toConvert/collisions/wall.obj", new Matrix4f());
        //collisionLoader.loadCeilings("toConvert/collisions/ceil.obj", new Matrix4f());
        collisionLoader.loadFloors("toConvert/collisions/stairFloor.obj",stairMatrix);
        collisionLoader.loadWalls("toConvert/collisions/stairWalls.obj",stairMatrix);
        collisionLoader.autoLoadTriangleCollisionsOBJ("toConvert/collisions/pole.obj",poleMatrix,0.1f);
        collisionLoader.autoLoadTriangleCollisionsOBJ("toConvert/collisions/cube.obj",cubeMatrix,0.1f);
        collisionLoader.autoLoadTriangleCollisionsOBJ("toConvert/collisions/cube.obj",cubeMatrix2,0.1f);
        collisionLoader.autoLoadTriangleCollisionsOBJ("toConvert/collisions/pool.obj",poolMatrix,0.1f);
        return collisionLoader;
    }

}
