#version 330 core

in vec2 textureCoords[21];

out vec4 color;

uniform sampler2D textureSampler;

void main() {
	color = vec4(0.0);
	color += texture(textureSampler, textureCoords[0]) * 0.011254;
    color += texture(textureSampler, textureCoords[1]) * 0.016436;
    color += texture(textureSampler, textureCoords[2]) * 0.023066;
    color += texture(textureSampler, textureCoords[3]) * 0.031105;
    color += texture(textureSampler, textureCoords[4]) * 0.040306;
    color += texture(textureSampler, textureCoords[5]) * 0.050187;
    color += texture(textureSampler, textureCoords[6]) * 0.060049;
    color += texture(textureSampler, textureCoords[7]) * 0.069041;
    color += texture(textureSampler, textureCoords[8]) * 0.076276;
    color += texture(textureSampler, textureCoords[9]) * 0.080977;
    color += texture(textureSampler, textureCoords[10]) * 0.082607;
    color += texture(textureSampler, textureCoords[11]) * 0.080977;
    color += texture(textureSampler, textureCoords[12]) * 0.076276;
    color += texture(textureSampler, textureCoords[13]) * 0.069041;
    color += texture(textureSampler, textureCoords[14]) * 0.060049;
    color += texture(textureSampler, textureCoords[15]) * 0.050187;
    color += texture(textureSampler, textureCoords[16]) * 0.040306;
    color += texture(textureSampler, textureCoords[17]) * 0.031105;
    color += texture(textureSampler, textureCoords[18]) * 0.023066;
    color += texture(textureSampler, textureCoords[19]) * 0.016436;
	color += texture(textureSampler, textureCoords[20]) * 0.011254;
}