package com.bestialMania.rendering.blur;

import com.bestialMania.MemoryManager;
import com.bestialMania.rendering.Framebuffer;
import com.bestialMania.rendering.MasterRenderer;
import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.ShaderObject;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.rendering.model.Rect2D;
import com.bestialMania.rendering.shader.Shader;
import com.bestialMania.rendering.shader.UniformFloat;
import com.bestialMania.rendering.shader.UniformMatrix4;
import com.bestialMania.rendering.texture.Texture;
import org.joml.Matrix4f;

public class BlurRenderer {
    private Texture finalTexture;
    private UniformFloat hblurAmount;
    private UniformFloat vblurAmount;

    public BlurRenderer(MasterRenderer masterRenderer, MemoryManager mm, Texture inTexture, Shader hBlurShader, Shader vBlurShader, int width, int height) {
        //the model
        Model model = new Rect2D(mm,-1,-1,1,1);

        //horizontal blur
        Framebuffer hblurFbo = Framebuffer.createFramebuffer2D(mm,width,height);
        masterRenderer.addFramebuffer(hblurFbo);
        Renderer hblurRenderer = hblurFbo.createRenderer(hBlurShader);
        ShaderObject hblurObject = hblurRenderer.createObject(model);
        hblurObject.addTexture(0,inTexture);
        hblurAmount = new UniformFloat(hBlurShader,"blur",0);
        hblurObject.addUniform(hblurAmount);

        //vertical blur
        Framebuffer vblurFbo = Framebuffer.createFramebuffer2D(mm,width,height);
        masterRenderer.addFramebuffer(vblurFbo);
        Renderer vblurRenderer = vblurFbo.createRenderer(vBlurShader);

        ShaderObject vblurObject = vblurRenderer.createObject(model);
        vblurObject.addTexture(0,hblurFbo.getTexture(0));
        vblurAmount = new UniformFloat(vBlurShader,"blur",0);
        vblurObject.addUniform(vblurAmount);

        finalTexture = vblurFbo.getTexture(0);
    }
    public Texture getTexture() {
        return finalTexture;
    }

    public void setBlur(float blurX, float blurY) {
        hblurAmount.setValue(blurX);
        vblurAmount.setValue(blurY);
    }

}
