#version 330 core

layout(location=0)out vec4 color;
layout(location=1)out vec4 glowColor;
in vec3 frag_uv;

uniform samplerCube skybox;

void main() {
	color = texture(skybox, frag_uv);
	glowColor = vec4(0,0,0,1);
}