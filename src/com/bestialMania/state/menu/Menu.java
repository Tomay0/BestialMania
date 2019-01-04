package com.bestialMania.state.menu;

import com.bestialMania.InputHandler;
import com.bestialMania.Main;
import com.bestialMania.object.gui.text.Font;
import com.bestialMania.object.gui.text.Text;
import com.bestialMania.rendering.MasterRenderer;
import com.bestialMania.rendering.MemoryManager;
import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.shader.Shader;
import org.lwjgl.system.CallbackI;

/**
 * Not a state itself but handles everything in the menu and switching between each individual menu state.
 * 1 master renderer is shared between all states
 */
public class Menu {
    public enum MenuState {PLAYER_SELECT,MAIN_MENU};

    private MasterRenderer renderer;//the renderer
    private MemoryManager memoryManager;//memory manager
    private InputHandler inputHandler;
    private Main main;//for swapping states

    //rendering objects to be used by submenus
    private Renderer guiRender, textRender;
    private Shader guiShader, textShader;
    private Font font;

    /**
     * Initialize the menu
     *
     */
    public Menu(Main main, InputHandler inputHandler) {
        this.main = main;
        this.inputHandler = inputHandler;
        //load all menu textures and fonts
        renderer = new MasterRenderer();
        memoryManager = new MemoryManager();

        //shaders
        guiShader = new Shader("res/shaders/gui_vertex.glsl","res/shaders/gui_fragment.glsl");
        textShader = new Shader("res/shaders/gui_vertex.glsl", "res/shaders/text_fragment.glsl");

        guiRender = renderer.getWindowFramebuffer().createRenderer(guiShader);
        textRender = renderer.getWindowFramebuffer().createRenderer(textShader);

        font = new Font(memoryManager,"res/fonts/test.fnt","res/fonts/test.png");
    }
    /**
     * Set the menu state
     */
    public void setCurrentState(MenuState state) {
        switch(state) {
            case PLAYER_SELECT: {
                main.setCurrentState(new PlayerSelect(this,inputHandler,renderer));
                break;
            }
            case MAIN_MENU: {
                main.quit();//TODO add more menu states
                break;
            }
            default: break;
        }
    }

    public Renderer getGuiRender() {
        return guiRender;
    }

    public Renderer getTextRender() {
        return textRender;
    }

    public Shader getGuiShader() {
        return guiShader;
    }

    public Shader getTextShader() {
        return textShader;
    }

    public Font getFont() {
        return font;
    }

    public void cleanUp() {
        memoryManager.cleanUp();
    }
}
