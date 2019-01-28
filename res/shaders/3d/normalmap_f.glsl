#version 330 core

in vec2 frag_uv;
in vec3 frag_lightDir;
in vec3 frag_cameraVec;

out vec4 color;

uniform sampler2D textureSampler;
uniform sampler2D normalSampler;

uniform vec3 lightColor;
uniform float reflectivity;
uniform float shineDamper;
const vec3 ambient = vec3(0.4);

void main() {
    vec4 textureColor = texture(textureSampler,frag_uv);
	vec3 normal = normalize(texture(normalSampler, frag_uv).rgb*2.0 - 1.0);

    //diffuse lighting
    float cosTheta = dot(normal,-normalize(frag_lightDir));
    cosTheta = clamp(cosTheta,0,1);
    vec3 diffuse = ambient + cosTheta * lightColor;

    //specular lighting
	vec3 reflected = reflect(normalize(frag_lightDir),normal);
	float cosAlpha = dot(normalize(frag_cameraVec),reflected);
	cosAlpha = clamp(cosAlpha,0,1);
	vec3 specular = clamp(reflectivity * pow(cosAlpha,shineDamper),0,0.8) * lightColor;

    color = textureColor * vec4(diffuse,1.0) + vec4(specular,1.0);
}