// color vertex shader
#version 330 core

layout(location = 0) in vec4 VertexIn;
layout(location = 1) in vec3 ColorIn;

out vec3 color;

uniform mat4 projectionMatrix = mat4(1.0);
uniform mat4 viewMatrix  = mat4(1.0);
uniform mat4 modelMatrix  = mat4(1.0);

void main()
{
	gl_Position = projectionMatrix * viewMatrix * modelMatrix * VertexIn;

	color = ColorIn;
}
