package com.bestialMania.object.beast;

import com.bestialMania.object.animation.AnimatedObject;
import com.bestialMania.object.animation.Animation;
import com.bestialMania.rendering.Renderer;
import com.bestialMania.rendering.ShaderObject;
import com.bestialMania.rendering.Texture;
import com.bestialMania.rendering.model.Model;
import com.bestialMania.rendering.shader.UniformFloat;
import com.bestialMania.rendering.shader.UniformMatrix4;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Beast {
    private static final float TURN_SPEED = 0.1f;

    private Vector3f position, positionInterpolate;
    private Vector2f movementDirection;//direction they are facing
    private float angle, angleTarget;
    private Matrix4f modelMatrix;
    private float speed;//current speed

    private AnimatedObject object;
    private Texture texture;
    private ShaderObject shaderObject;

    /**
     * Create a beast
     */
    public Beast(AnimatedObject object, Texture texture){
        position = new Vector3f(0,0,0);//TODO have some sort of spawn point
        angle = 0;

        positionInterpolate = new Vector3f(0,0,0);
        movementDirection = new Vector2f((float)Math.sin(angle),(float)Math.cos(angle));
        modelMatrix = new Matrix4f();
        angleTarget = angle;
        speed = 0;
        this.object = object;

        //TEST ANIMATION
        Animation animation = new Animation(object.getArmature(),object.getPose(0),true);
        int i = 0;
        float time = 0;

        float songBPM = 128;//PUMPED UP KICKS = 128. Running in the 90s = 159. Spaceghostpurp = 150 Change to make jimmy sync up to the song you pick


        float beatDuration = 60.0f/songBPM;


        while(i<12) {
            i++;
            time+=0.85*beatDuration;
            animation.addKeyFrame(time,object.getPose(i));
            i++;
            time+=0.15*beatDuration;
            animation.addKeyFrame(time,object.getPose(i));

        }
        animation.setCurrentTime(time+beatDuration*0.1f);
        object.setAnimation(animation);

        this.texture = texture;
    }

    /**
     * Set a new direction
     */
    public void setDirection(Vector2f direction) {
        angleTarget = (float)Math.atan2(direction.x,direction.y);
        this.movementDirection = direction;
    }

    /**
     * Set a new speed
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * Get position
     */
    public Vector3f getPosition() {return position;}

    /**
     * Update position and orientation
     */
    public void update() {
        position.x+=movementDirection.x*speed;
        position.z+=movementDirection.y*speed;

        //turn to face direction of movement
        if(Math.abs(angle-angleTarget)<TURN_SPEED) angle = angleTarget;
        else if(angle>angleTarget) {
            if(angle-angleTarget>Math.PI) angle+=TURN_SPEED;
            else angle-=TURN_SPEED;
        }
        else {
            if(angleTarget-angle>Math.PI) angle-=TURN_SPEED;
            else angle+=TURN_SPEED;
        }

        if(angle>Math.PI) angle-=2*Math.PI;
        if(angle<-Math.PI) angle+=2*Math.PI;
        object.update();
    }

    /**
     * Update matrices using the frame interpolation amount
     *
     * return the interpolated position
     */
    public Vector3f interpolate(float frameInterpolation) {
        object.interpolate(frameInterpolation);
        //interpolate position
        positionInterpolate.x = position.x+movementDirection.x*speed*frameInterpolation;
        positionInterpolate.z = position.z+movementDirection.y*speed*frameInterpolation;
        //System.out.println(positionInterpolate.x + "," + positionInterpolate.y + "," + positionInterpolate.z);

        //interpolate direction
        float angleInterpolate = angle;
        float turnAmount = TURN_SPEED*frameInterpolation;
        if(Math.abs(angle-angleTarget)<turnAmount) angleInterpolate = angleTarget;
        else if(angle>angleTarget) {
            if(angle-angleTarget>Math.PI) angleInterpolate+=turnAmount;
            else angleInterpolate-=turnAmount;
        }
        else {
            if(angleTarget-angle>Math.PI) angleInterpolate-=turnAmount;
            else angleInterpolate+=turnAmount;
        }

        //recalculate matrix
        modelMatrix.identity();

        //translation
        modelMatrix.translate(positionInterpolate);
        //rotation
        modelMatrix.rotateY(angleInterpolate);

        //scale
        modelMatrix.scale(0.1f,0.1f,0.1f);


        return positionInterpolate;
    }

    /**
     * Add the model to the renderer
     */
    public void linkToRenderer(Renderer renderer) {
        shaderObject = object.createObject(renderer);
        shaderObject.addTexture(0,texture);
        shaderObject.addUniform(new UniformFloat(renderer.getShader(),"reflectivity",0.2f));
        shaderObject.addUniform(new UniformFloat(renderer.getShader(),"shineDamper",5.0f));
        shaderObject.addUniform(new UniformMatrix4(renderer.getShader(),"modelMatrix",modelMatrix));
    }


    public Model getModel() {return object.getModel();}
    public AnimatedObject getAnimatedObject() {return object;}
    public Matrix4f getMatrix() {
        return modelMatrix;
    }
}
