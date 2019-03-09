#version 330 core

in vec2 frag_uv;
in vec3 frag_normal;
in vec3 frag_lightDir;
in vec3 frag_cameraVec;

in vec3 shadowSpace[3];
in float dist;

layout(location=0)out vec4 color;
layout(location=1)out vec4 glowColor;

uniform sampler2D textureSampler;
uniform sampler2D shadowSampler0;
uniform sampler2D shadowSampler1;
uniform sampler2D shadowSampler2;
//uniform float pxSize;
uniform int pcfCount;
uniform float pcfSpread;
uniform float pcfIncrAmount;
uniform float shadowDist[4];

uniform vec3 lightColor;
uniform vec3 ambient;
uniform float reflectivity;
uniform float shineDamper;
uniform float alpha;

/**
    Get the shadow amount for a specific shadow map
*/
float getShadow(int shadowMap,float spreadScale) {
    //low settings
    if(pcfCount==0) {
        float shadowDepth;
        if(shadowMap==0) shadowDepth = texture(shadowSampler0,shadowSpace[shadowMap].xy).r;
        else if(shadowMap==1) shadowDepth = texture(shadowSampler1,shadowSpace[shadowMap].xy).r;
        else shadowDepth = texture(shadowSampler2,shadowSpace[shadowMap].xy).r;

        if(shadowSpace[shadowMap].z > shadowDepth ) {
            return 1;
        } else {
            return 0;
        }
    }

    //antialiasing
    float shadow = 0;
    float spread = pcfSpread*spreadScale/1024.0;
    for(int x = -pcfCount;x<pcfCount;x++) {
        for(int y = -pcfCount;y<pcfCount;y++) {
            vec2 offset = vec2(x,y)*spread;
            float shadowDepth;
            if(shadowMap==0) shadowDepth = texture(shadowSampler0,shadowSpace[shadowMap].xy + offset).r;
            else if(shadowMap==1) shadowDepth = texture(shadowSampler1,shadowSpace[shadowMap].xy + offset).r;
            else shadowDepth = texture(shadowSampler2,shadowSpace[shadowMap].xy + offset).r;

            if(shadowSpace[shadowMap].z > shadowDepth) {
                shadow += pcfIncrAmount;
            }
        }
    }
    return shadow;
}

/**
    Calculates the shadow amount by interpolating between all rendered shadow maps
*/
float getShadow() {
    //close shadow box
	if(dist<shadowDist[0]) {
	    return getShadow(0,1.5);
	}
	//close-mid transition
	else if(dist<shadowDist[1]) {
        float t = (dist-shadowDist[0])/(shadowDist[1]-shadowDist[0]);

        return (1-t) * getShadow(0,1.5) + t * getShadow(1,0.7);
	}
	//mid shadow box
	else if(dist<shadowDist[2]) {
	    return getShadow(1,0.7);

	}
	//mid-far transition
	else if(dist<shadowDist[3]) {
        float t = (dist-shadowDist[1])/(shadowDist[2]-shadowDist[1]);

        return (1-t) * getShadow(1,0.7) + t * getShadow(2,0.7);
	}
	//far shadow box
	else {
        return getShadow(2,0.7);
	}
}

void main() {
    float shadow = getShadow(0,1.5);

    vec4 textureColor = texture(textureSampler,frag_uv);

    //diffuse lighting
    float cosTheta = dot(normalize(frag_normal),-normalize(frag_lightDir));
    cosTheta = clamp(cosTheta-shadow,0,1);
    vec3 diffuse = ambient + cosTheta * lightColor;

    //specular lighting
	vec3 reflected = reflect(normalize(frag_lightDir),normalize(frag_normal));
	float cosAlpha = dot(normalize(frag_cameraVec),reflected);
	cosAlpha = clamp(cosAlpha-shadow,0,1);
	vec3 specular = clamp(reflectivity * pow(cosAlpha,shineDamper),0,0.8) * lightColor;

    color = textureColor * vec4(diffuse,textureColor.a) + vec4(specular,textureColor.a);
    color.a*=alpha;
    glowColor = vec4(0,0,0,1);
}