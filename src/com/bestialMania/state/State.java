package com.bestialMania.state;

public interface State {
    void update();
    void render();
    void removeObjects();
    void cleanUp();
}
