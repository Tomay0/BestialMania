#version 330 core

in vec2 frag_uv;

layout(location=0)out vec4 color;
layout(location=1)out vec4 glowColor;

uniform sampler2D textureSampler;

void main() {
    vec4 textureColor = texture(textureSampler,frag_uv);
    color = textureColor;
    glowColor = vec4(0,0,0,1);
}