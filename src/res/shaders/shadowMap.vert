#version 330 core

layout(location = 0) in vec4 VertexIn;

uniform mat4 depthProj = mat4(1.0);
uniform mat4 modelMatrix = mat4(1.0);

void main()
{
	gl_Position = depthProj * modelMatrix * VertexIn;
}