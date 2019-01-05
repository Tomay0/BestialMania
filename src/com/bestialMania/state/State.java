package com.bestialMania.state;

public interface State {
    /**
     * Update all objects within the current state
     */
    void update();

    /**
     * Render everything within the current state
     */
    void render();

    /**
     * Remove necessary objects/memory to change to another state (may not necessarily clear everything)
     */
    void removeObjects();

    /**
     * Deletes all memory associated with the current state
     */
    void cleanUp();
}
