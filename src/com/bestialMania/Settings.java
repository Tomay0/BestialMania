package com.bestialMania;

/**
 * Class with all the display/graphics settings
 * TODO load/unload from a file which can be edited in-game in the future.
 */
public class Settings {
    public enum GraphicsSetting {LOW, MEDIUM, HIGH, ULTRA}


    //INVERT BY JUST MAKING THESE NEGATIVE
    public static float HORIZONTAL_MOUSE_SENSITIVITY = 0.007f;
    public static float VERTICAL_MOUSE_SENSITIVITY = 0.005f;
    public static float HORIZONTAL_CONTROLLER_CAMERA_SENSITIVITY = 0.05f;//>mouse sensitivity
    public static float VERTICAL_CONTROLLER_CAMERA_SENSITIVITY = 0.05f;//>mouse sensitivity

    public static int WIDTH = 1920;
    public static int HEIGHT = 1080;
    public static boolean FULLSCREEN = true;
    public static int ANISOTROPIC_FILTERING = 2;//either off,2x,4x,8x,16x
    public static boolean ANTIALIASING = true;
    public static GraphicsSetting TEXTURE_DETAIL = GraphicsSetting.HIGH;//LOW = no texture effects. HIGH = normal mapping TODO add more texture detail stuff
    public static GraphicsSetting SHADOW_QUALITY = GraphicsSetting.ULTRA;
    public static int SAMPLES = 4;
    public static boolean VSYNC = true;
    public static float FOV = (float)Math.toRadians(60);
}
