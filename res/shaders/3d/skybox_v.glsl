#version 330 core

layout (location = 0) in vec3 pos;

out vec3 frag_uv;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main() {
	frag_uv = pos;
	vec4 position = projectionMatrix * viewMatrix * vec4(pos,1.0);
	gl_Position = position.xyww;
}