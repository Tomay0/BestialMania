package com.bestialMania.state.game.map;

import com.bestialMania.collision.BoundingBox;
import com.bestialMania.collision.CollisionHandler;
import com.bestialMania.collision.CollisionLoader;
import com.bestialMania.state.game.Game;
import org.joml.Vector3f;

/**
 * GameMap class is used for loading assets specific to certain maps into the game.
 */
public abstract class MapData {
    protected BoundingBox boundingBox;
    public BoundingBox getBoundingBox() {return boundingBox;}

    //lighting and colour
    public abstract Vector3f getLightDirection();
    public abstract Vector3f getLightColor();
    public abstract Vector3f getAmbientLight();
    public abstract float getContrast();
    public abstract float getSaturation();
    public abstract float getBrightness();

    //resources
    public abstract String getSkyboxTexture();
    public abstract String getMusic();

    //collisions
    public abstract String getCollisions();

    //object loading
    public abstract void loadShaders(Game game);
    public abstract void loadObjects(Game game);

    //collision loader
    public abstract CollisionLoader loadCollisions();
}
