#version 330 core

const int MAX_JOINTS = 100;

layout(location=0) in vec3 pos;
layout(location=1) in vec2 uv;
layout(location=2) in vec3 normal;
layout(location=4) in ivec3 jointIds;
layout(location=5) in vec3 weights;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

uniform vec3 lightDirection;
uniform mat4[MAX_JOINTS] jointTransforms;

out vec2 frag_uv;
out vec3 frag_normal;
out vec3 frag_lightDir;
out vec3 frag_cameraVec;

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
    vec4 viewSpace = mv * localPos;
    gl_Position = projectionMatrix * viewSpace;

    //lighting vectors
    frag_normal = (mv * localNormal).xyz;
    frag_lightDir = (viewMatrix * vec4(lightDirection,0.0)).xyz;
    frag_cameraVec = -viewSpace.xyz;
}