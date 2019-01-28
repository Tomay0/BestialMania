#version 330 core

out vec4 color;

in vec3 frag_uv;

uniform samplerCube skybox;

void main() {
	color = texture(skybox, frag_uv);
}