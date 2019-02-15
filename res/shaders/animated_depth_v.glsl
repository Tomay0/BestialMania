#version 330 core
const int MAX_JOINTS = 100;

layout(location = 0) in vec3 pos;
layout(location=4) in ivec3 jointIds;
layout(location=5) in vec3 weights;

uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform mat4[MAX_JOINTS] jointTransforms;

void main() {
    vec4 localPos = vec4(0.0);

    for(int i = 0;i<3;i++) {
        mat4 jointTransform = jointTransforms[jointIds[i]];
        localPos += weights[i] * (jointTransform * vec4(pos,1.0));
    }
	gl_Position = viewMatrix * modelMatrix * localPos;
}