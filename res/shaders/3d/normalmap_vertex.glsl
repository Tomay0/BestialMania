#version 330 core

layout(location=0) in vec3 pos;
layout(location=1) in vec2 uv;
layout(location=2) in vec3 normal;
layout(location=3) in vec3 tangent;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

uniform vec3 lightDirection;

out vec2 frag_uv;
out vec3 frag_lightDir;
out vec3 frag_cameraVec;

void main() {
    frag_uv = uv;

    //matrices, position
    mat4 mv = viewMatrix * modelMatrix;
    vec4 viewSpace = mv * vec4(pos,1.0);
    gl_Position = projectionMatrix * viewSpace;

    vec3 eyeNormal = (mv * vec4(normal,0.0)).xyz;
    vec3 eyeTangent = (mv * vec4(tangent,0.0)).xyz;
    vec3 eyeBitangent = cross(eyeNormal,eyeTangent);

    mat3 tangentSpace = transpose(mat3(
    	eyeTangent,
    	eyeBitangent,
    	eyeNormal
    ));

    //lighting vectors
    frag_lightDir = tangentSpace * (viewMatrix * vec4(lightDirection,0.0)).xyz;
    frag_cameraVec = tangentSpace * -viewSpace.xyz;
}