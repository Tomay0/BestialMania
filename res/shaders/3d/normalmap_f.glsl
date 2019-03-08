#version 330 core

in vec2 frag_uv;
in vec3 frag_lightDir;
in vec3 frag_cameraVec;

in vec3 shadowSpace[3];
in float dist;

out vec4 color;

uniform sampler2D textureSampler;
uniform sampler2D normalSampler;
uniform sampler2D shadowSampler0;
uniform sampler2D shadowSampler1;
uniform sampler2D shadowSampler2;
uniform float pxSize;
uniform int pcfCount;
uniform float pcfSpread;
uniform float pcfIncrAmount;
uniform float shadowDist[4];

uniform vec3 lightColor;
uniform float reflectivity;
uniform float shineDamper;
const vec3 ambient = vec3(0.4);
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
    float spread = pcfSpread*spreadScale;
    for(float x = -pcfCount * spread; x <= pcfCount * spread;x+= spread) {
        for(float y = -pcfCount * spread; y <= pcfCount * spread;y+= spread) {
            float shadowDepth;
            if(shadowMap==0) shadowDepth = texture(shadowSampler0,shadowSpace[shadowMap].xy + vec2(x,y) * pxSize).r;
            else if(shadowMap==1) shadowDepth = texture(shadowSampler1,shadowSpace[shadowMap].xy + vec2(x,y) * pxSize).r;
            else shadowDepth = texture(shadowSampler2,shadowSpace[shadowMap].xy + vec2(x,y) * pxSize).r;

            if(shadowSpace[shadowMap].z > shadowDepth ) {
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

        return (1-t) * getShadow(1,0.7) + t * getShadow(2,0.5);
	}
	//far shadow box
	else {
        return getShadow(2,0.5);
	}
}


void main() {
    float shadow = getShadow();

    vec4 textureColor = texture(textureSampler,frag_uv);
	vec3 normal = normalize(texture(normalSampler, frag_uv).rgb*2.0 - 1.0);

    //diffuse lighting
    float cosTheta = dot(normal,-normalize(frag_lightDir));
    cosTheta = clamp(cosTheta-shadow,0,1);
    vec3 diffuse = ambient + cosTheta * lightColor;

    //specular lighting
	vec3 reflected = reflect(normalize(frag_lightDir),normal);
	float cosAlpha = dot(normalize(frag_cameraVec),reflected);
	cosAlpha = clamp(cosAlpha-shadow,0,1);
	vec3 specular = clamp(reflectivity * pow(cosAlpha,shineDamper),0,0.8) * lightColor;


    color = textureColor * vec4(diffuse,1.0) + vec4(specular,1.0);
}