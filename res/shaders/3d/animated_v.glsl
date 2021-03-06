#version 330 core

const int MAX_JOINTS = 100;
const int N_SHADOWMAPS = 3;

layout(location=0) in vec3 pos;
layout(location=1) in vec2 uv;
layout(location=2) in vec3 normal;
layout(location=4) in ivec3 jointIds;
layout(location=5) in vec3 weights;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

uniform vec3 lightDirection;
uniform mat4 shadowMatrix[N_SHADOWMAPS];
uniform mat4[MAX_JOINTS] jointTransforms;

out vec2 frag_uv;
out vec3 frag_normal;
out vec3 frag_lightDir;
out vec3 frag_cameraVec;

out vec3 shadowSpace[N_SHADOWMAPS];
out float dist;

void main() {
    frag_uv = uv;

    vec4 localPos = vec4(0.0);
    vec4 localNormal = vec4(0.0);

    for(int i = 0;i<3;i++) {
        mat4 jointTransform = jointTransforms[jointIds[i]];
        localPos += weights[i] * (jointTransform * vec4(pos,1.0));
        localNormal += weights[i] * (jointTransform * vec4(normal,0.0));
    }

    //matrices, position
    mat4 mv = viewMatrix * modelMatrix;
    vec4 modelSpace = modelMatrix *localPos;
    vec4 viewSpace = mv * localPos;
    dist = -viewSpace.z;
    gl_Position = projectionMatrix * viewSpace;

    //lighting vectors
    frag_normal = (mv * localNormal).xyz;
    frag_lightDir = (viewMatrix * vec4(lightDirection,0.0)).xyz;
    frag_cameraVec = -viewSpace.xyz;
    //shadow matrices
	for(int i = 0;i<N_SHADOWMAPS;i++) {
		shadowSpace[i] = (shadowMatrix[i] * modelSpace).xyz;
	}
}