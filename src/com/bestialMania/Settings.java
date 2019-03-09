package com.bestialMania;

/**
 * Class with all the display/graphics settings
 * TODO load/unload from a file which can be edited in-game in the future.
 */
public class Settings {
    public enum GraphicsSetting {LOW, MEDIUM, HIGH, ULTRA}

    //CONTROL SETTINGS
    public static float HORIZONTAL_MOUSE_SENSITIVITY = 0.007f;
    public static float VERTICAL_MOUSE_SENSITIVITY = 0.005f;
    public static float HORIZONTAL_CONTROLLER_CAMERA_SENSITIVITY = 0.05f;//>mouse sensitivity
    public static float VERTICAL_CONTROLLER_CAMERA_SENSITIVITY = 0.05f;//>mouse sensitivity
    public static boolean AUTOMATIC_RUNNING = true;//tap the run button to run instead of holding constantly to run

    //DISPLAY SETTINGS
    public static int WIDTH = 1600;//This will automatically be changed to your monitor's resolution if fullscreen is on
    public static int HEIGHT = 900;
    public static boolean FULLSCREEN = true;
    public static boolean VSYNC = false;
    public static float FOV = (float)Math.toRadians(60);

    //GRAPHICS SETTINGS
    public static int ANISOTROPIC_FILTERING = 2;//either off,2x,4x,8x,16x
    public static int ANTIALIASING = 4;//either off,2x,4x,8x
    public static GraphicsSetting TEXTURE_DETAIL = GraphicsSetting.HIGH;//LOW = no texture effects. HIGH = normal mapping TODO add more texture detail stuff
    public static GraphicsSetting SHADOW_RESOLUTION = GraphicsSetting.HIGH;//LOW/MEDIUM/HIGH.
    public static GraphicsSetting SHADOW_SOFTENING = GraphicsSetting.HIGH;//LOW/MEDIUM/HIGH/ULTRA. Affects performance the most
}
