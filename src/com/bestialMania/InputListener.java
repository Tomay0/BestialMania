package com.bestialMania;

public interface InputListener {
    void keyEvent(boolean pressed, int key);
    void mouseEvent(boolean pressed, int button);
    void controllerEvent(int controller, boolean pressed, int button);
}
