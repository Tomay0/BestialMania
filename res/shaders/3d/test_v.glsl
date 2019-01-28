#version 330 core

layout(location=0) in vec3 pos;
layout(location=1) in vec2 uv;
layout(location=2) in vec3 normal;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

uniform vec3 lightDirection;

out vec2 frag_uv;
out vec3 frag_normal;
out vec3 frag_lightDir;
out vec3 frag_cameraVec;

void main() {
    frag_uv = uv;

    //matrices, position
    mat4 mv = viewMatrix * modelMatrix;
    vec4 viewSpace = mv * vec4(pos,1.0);
    gl_Position = projectionMatrix * viewSpace;

    //lighting vectors
    frag_normal = (mv * vec4(normal,0.0)).xyz;
    frag_lightDir = (viewMatrix * vec4(lightDirection,0.0)).xyz;
    frag_cameraVec = -viewSpace.xyz;
}