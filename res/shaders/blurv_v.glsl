#version 330 core

layout(location = 0) in vec2 pos;

out vec2 textureCoords[21];

uniform float pxHeight;
uniform float blur;

void main() {
	gl_Position = vec4(pos,0.0,1.0);
	vec2 center = pos * 0.5 + 0.5;

	for(int i = -10;i<=10;i++){
		textureCoords[i+10] = center + vec2(0.0, pxHeight * i * blur);
	}
}