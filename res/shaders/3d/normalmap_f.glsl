#version 330 core

const int N_SHADOWMAPS = 3;
const float SEAM_RATIO = 0.3;


in vec2 frag_uv;
in vec3 frag_lightDir;
in vec3 frag_cameraVec;

in vec3 shadowSpace[N_SHADOWMAPS];
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
uniform float shadowDist[N_SHADOWMAPS];

uniform vec3 lightColor;
uniform float reflectivity;
uniform float shineDamper;
const vec3 ambient = vec3(0.4);


/**
    Get the shadow amount for a specific shadow map
*/
float getShadow(int shadowMap, float incrAmount) {

    float shadow = 0;
    for(float x = -pcfCount * pcfSpread; x <= pcfCount * pcfSpread;x+= pcfSpread) {
        for(float y = -pcfCount * pcfSpread; y <= pcfCount * pcfSpread;y+= pcfSpread) {
            float shadowDepth;
            if(shadowMap==0) shadowDepth = texture(shadowSampler0,shadowSpace[shadowMap].xy + vec2(x,y) * pxSize).r;
            else if(shadowMap==1) shadowDepth = texture(shadowSampler1,shadowSpace[shadowMap].xy + vec2(x,y) * pxSize).r;
            else shadowDepth = texture(shadowSampler2,shadowSpace[shadowMap].xy + vec2(x,y) * pxSize).r;

            if(shadowSpace[shadowMap].z > shadowDepth ) {
                shadow += incrAmount;
            }
        }
    }
    return shadow;
}

/**
    Calculates the shadow amount by interpolating between all rendered shadow maps
*/
float getShadow() {
	float shadow = 0;
	float incrAmount = 1.0/float((pcfCount*2.0 + 1.0)*(pcfCount*2.0 + 1.0));
	for(int i = 0;i<N_SHADOWMAPS;i++) {
		if(i<N_SHADOWMAPS-1) {
			float boxLength = shadowDist[i];
			if(i>0) boxLength -= shadowDist[i-1];
			float seam = boxLength * SEAM_RATIO;

			//is an interpolation of 2 shadow maps
			float seamInterpolate = (shadowDist[i]-dist)/seam;
			if (seamInterpolate >0 && seamInterpolate < 1) {
				float shadow1 = getShadow(i, incrAmount);
				float shadow2 = getShadow(i+1, incrAmount);
				shadow = shadow2 * (1-seamInterpolate) + shadow1 * seamInterpolate;
				break;
			}
		}
		//no interpolation
		if(dist < shadowDist[i]) {
			shadow = getShadow(i, incrAmount);
			break;
		}
	}

	return shadow;
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