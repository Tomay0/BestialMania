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


    public abstract Vector3f getLightDirection();
    public abstract Vector3f getLightColor();
    public abstract String getSkyboxTexture();
    public abstract String getMusic();
    public abstract String getCollisions();

    public abstract void loadShaders(Game game);
    public abstract void loadObjects(Game game);

    public abstract CollisionLoader loadCollisions();
}
