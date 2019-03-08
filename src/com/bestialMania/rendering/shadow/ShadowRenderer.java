package com.bestialMania.rendering.shadow;

import com.bestialMania.MemoryManager;
import com.bestialMania.collision.BoundingBox;
import com.bestialMania.object.AnimatedObject;
import com.bestialMania.object.Object3D;
import com.bestialMania.rendering.*;
import com.bestialMania.rendering.shader.Shader;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.GL_FRONT;

public class ShadowRenderer {
    //close shadows
    private Renderer[] closeRenderers;
    private Renderer[] closeAnimatedRenderers;
    private Texture[] closeTextures;
    private ShadowBox[] closeBoxes;
    //mid shadows
    private Renderer[] midRenderers;
    private Renderer[] midAnimatedRenderers;
    private Texture[] midTextures;
    private ShadowBox[] midBoxes;
    //far shadows
    private Renderer farRenderer;
    private Renderer farAnimatedRenderer;
    private Texture farTexture;
    private ShadowBoundingBox farBox;//doesn't update

    private int nPlayers;
    private MasterRenderer masterRenderer;
    private MemoryManager memoryManager;
    private int resolution;
    private Vector3f lightDir;
    private Shader depthShader, animatedDepthShader;

    public ShadowRenderer(MemoryManager mm, MasterRenderer masterRenderer, Shader depthShader, Shader animatedDepthShader, Vector3f lightDir, BoundingBox boundingBox, int nPlayers, int resolution) {
        this.memoryManager = mm;
        this.resolution = resolution;
        this.masterRenderer = masterRenderer;
        this.depthShader = depthShader;
        this.animatedDepthShader = animatedDepthShader;
        this.nPlayers = nPlayers;
        this.lightDir = lightDir;

        closeRenderers = new Renderer[nPlayers];
        closeAnimatedRenderers = new Renderer[nPlayers];
        closeTextures = new Texture[nPlayers];
        closeBoxes = new ShadowBox[nPlayers];
        midRenderers = new Renderer[nPlayers];
        midAnimatedRenderers = new Renderer[nPlayers];
        midTextures = new Texture[nPlayers];
        midBoxes = new ShadowBox[nPlayers];

        //far box
        Framebuffer farFbo = Framebuffer.createDepthFramebuffer2D(memoryManager,resolution,resolution);
        masterRenderer.addFramebuffer(farFbo);
        farRenderer = farFbo.createRenderer(depthShader);
        farRenderer.setCull(GL_FRONT);
        farAnimatedRenderer = farFbo.createRenderer(animatedDepthShader);
        farAnimatedRenderer.setCull(GL_FRONT);
        farTexture = farFbo.getTexture(0);

        farBox = new ShadowBoundingBox(boundingBox, lightDir);
        farBox.linkToDepthRenderer(farRenderer);
        farBox.linkToDepthRenderer(farAnimatedRenderer);

    }

    /**
     * Create renderers for the close/mid shadow boxes for a player ID
     */
    public void createRenderers(int playerID) {
        //close
        Framebuffer closeFbo = Framebuffer.createDepthFramebuffer2D(memoryManager,resolution,resolution);
        masterRenderer.addFramebuffer(closeFbo);
        closeRenderers[playerID] = closeFbo.createRenderer(depthShader);
        closeRenderers[playerID].setCull(GL_FRONT);
        closeAnimatedRenderers[playerID] = closeFbo.createRenderer(animatedDepthShader);
        closeAnimatedRenderers[playerID].setCull(GL_FRONT);
        closeTextures[playerID] = closeFbo.getTexture(0);

        //mid
        Framebuffer midFbo = Framebuffer.createDepthFramebuffer2D(memoryManager,resolution,resolution);
        masterRenderer.addFramebuffer(midFbo);
        midRenderers[playerID] = midFbo.createRenderer(depthShader);
        midRenderers[playerID].setCull(GL_FRONT);
        midAnimatedRenderers[playerID] = midFbo.createRenderer(animatedDepthShader);
        midAnimatedRenderers[playerID].setCull(GL_FRONT);
        midTextures[playerID] = midFbo.getTexture(0);
    }


    /**
     * Load the shadow boxes for a player ID
     */
    public void loadShadowBoxes(int playerID, Matrix4f viewMatrix, float closeMin, float closeMax, float midMin, float midMax, float aspectRatio) {
        //close
        closeBoxes[playerID] = new ShadowBox(aspectRatio,viewMatrix,lightDir,closeMin,closeMax);
        closeBoxes[playerID].linkToDepthRenderer(closeRenderers[playerID]);
        closeBoxes[playerID].linkToDepthRenderer(closeAnimatedRenderers[playerID]);

        //mid
        midBoxes[playerID] = new ShadowBox(aspectRatio,viewMatrix,lightDir,midMin,midMax);
        midBoxes[playerID].linkToDepthRenderer(midRenderers[playerID]);
        midBoxes[playerID].linkToDepthRenderer(midAnimatedRenderers[playerID]);

    }

    public void linkToRenderer(Renderer renderer, int playerID) {
        closeBoxes[playerID].linkToRenderer(renderer,0);
        midBoxes[playerID].linkToRenderer(renderer,1);
        farBox.linkToRenderer(renderer,2);
        renderer.addTexture(2,closeTextures[playerID]);
        renderer.addTexture(3,midTextures[playerID]);
        renderer.addTexture(4,farTexture);
    }

    /**
     * Create shadow casting object
     */
    public void createShadowCastingObject(Object3D object) {
        for(Renderer renderer : closeRenderers) {
            object.createShaderObject(renderer);
        }
        for(Renderer renderer : midRenderers) {
            object.createShaderObject(renderer);
        }
        object.createShaderObject(farRenderer);
    }

    /**
     * Create shadow casting object that is animated
     */
    public void createShadowCastingAnimatedObject(AnimatedObject object) {
        for(Renderer renderer : closeAnimatedRenderers) {
            ShaderObject so = object.createShaderObject(renderer);
            object.linkTransformsToShaderObject(so);
        }
        for(Renderer renderer : midAnimatedRenderers) {
            ShaderObject so = object.createShaderObject(renderer);
            object.linkTransformsToShaderObject(so);
        }
        ShaderObject so = object.createShaderObject(farAnimatedRenderer);
        object.linkTransformsToShaderObject(so);

    }

    public void update() {
        for(ShadowBox sb : closeBoxes) sb.update();
        for(ShadowBox sb : midBoxes) sb.update();
    }
}
