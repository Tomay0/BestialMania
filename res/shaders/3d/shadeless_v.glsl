#version 330 core

layout(location=0) in vec3 pos;
layout(location=1) in vec2 uv;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

out vec2 frag_uv;

void main() {
    frag_uv = uv;

    //matrices, position
    mat4 mv = viewMatrix * modelMatrix;
    vec4 viewSpace = mv * vec4(pos,1.0);
    gl_Position = projectionMatrix * viewSpace;
}