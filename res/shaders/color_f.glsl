#version 330 core

in vec2 frag_uv;

out vec4 color;

uniform sampler2D textureSampler;
//uniform sampler2D glowSampler;

uniform float contrast;
uniform float brightness;
uniform float saturation;
//uniform float glow;

const vec3 luminanceWeights = vec3(0.299, 0.587, 0.114);

void main() {
	//vec4 sceneColor = texture(textureSampler,frag_uv);
	//vec4 blurColor = texture(glowSampler, frag_uv);

	//vec4 texColor = sceneColor + blurColor * glow;
	vec4 texColor = texture(textureSampler,frag_uv);

	float luminance = dot(texColor.rgb, luminanceWeights);
	texColor = mix(vec4(luminance), texColor, saturation);
	texColor.rgb = (texColor.rgb - 0.5) * (1.0 + contrast) + 0.5;
	texColor.rgb = texColor.rgb * brightness;

	color = texColor;
}