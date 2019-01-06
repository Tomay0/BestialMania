package com.bestialMania;

/**
 * Class with all the display/graphics settings
 * TODO load/unload from a file which can be edited in-game in the future.
 */
public class DisplaySettings {
    //INVERT BY JUST MAKING THESE NEGATIVE
    public static float HORIZONTAL_MOUSE_SENSITIVITY = 0.007f;
    public static float VERTICAL_MOUSE_SENSITIVITY = 0.005f;
    public static float HORIZONTAL_CONTROLLER_CAMERA_SENSITIVITY = 0.05f;//>mouse sensitivity
    public static float VERTICAL_CONTROLLER_CAMERA_SENSITIVITY = 0.05f;//>mouse sensitivity
    public static int WIDTH = 1920/2;
    public static int HEIGHT = 1080/2;
    public static boolean FULLSCREEN = false;
    public static boolean ANISOTROPIC_FILTERING = true;
    public static boolean ANTIALIASING = true;
    public static int SAMPLES = 4;
}
