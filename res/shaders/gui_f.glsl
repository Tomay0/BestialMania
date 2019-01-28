#version 330

in vec2 frag_uv;

out vec4 color;

uniform sampler2D textureSampler;

void main() {
    color = texture(textureSampler,frag_uv);
}

