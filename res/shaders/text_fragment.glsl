#version 330

in vec2 frag_uv;

out vec4 out_color;

uniform sampler2D textureSampler;
uniform vec3 color;

void main() {
    out_color = vec4(color,texture(textureSampler,frag_uv).a);
}

