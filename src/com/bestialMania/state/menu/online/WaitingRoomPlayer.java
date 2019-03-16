package com.bestialMania.state.menu.online;

import com.bestialMania.MemoryManager;
import com.bestialMania.Settings;
import com.bestialMania.gui.text.Font;
import com.bestialMania.gui.text.Text;
import com.bestialMania.rendering.Renderer;
import org.joml.Vector3f;

public class WaitingRoomPlayer {
    private int id;
    private MemoryManager memoryManager;
    private Text label,ready;
    private Renderer renderer;

    /**
     * Create a row on the online waiting menu representing a player
     * @param id the id of the player
     * @param font font to use for text
     * @param y y coordinate of the row
     * @param renderer text renderer
     */
    public WaitingRoomPlayer(int id, Font font, int y, Renderer renderer) {
        memoryManager = new MemoryManager();
        this.renderer = renderer;
        this.id = id;
        label = new Text(memoryManager,"Player"+id,font,40, Settings.WIDTH/2-400,y,Text.TextAlign.LEFT,new Vector3f(1,1,0));
        label.addToRenderer(renderer);
        ready = new Text(memoryManager,"Waiting",font,40,Settings.WIDTH/2+300,y,Text.TextAlign.RIGHT,new Vector3f(1,1,0));
        ready.addToRenderer(renderer);
    }

    /**
     * Change the y value of the text
     */
    public void setY(int y) {
        label.setPosition(Settings.WIDTH/2-400,y);
        ready.setPosition(Settings.WIDTH/2+300,y);
    }

    /**
     * Change if a waiting player has pressed the ready button
     */
    public void setReady(boolean ready) {
        if(ready) this.ready.setText("Ready");
        else this.ready.setText("Waiting");
    }

    /**
     * Get the id
     */
    public int getId() {return id;}

    /**
     * Delete from renderers and memory
     */
    public void cleanUp() {
        label.removeFromRenderer(renderer);
        ready.removeFromRenderer(renderer);
        memoryManager.cleanUp();
    }
}
