#version 330

layout(location=0) in vec2 pos;
layout(location=1) in vec2 uv;

uniform mat4 modelMatrix;

out vec2 frag_uv;

void main() {
    gl_Position = modelMatrix * vec4(pos,0.0,1.0);
    frag_uv = uv;
}
