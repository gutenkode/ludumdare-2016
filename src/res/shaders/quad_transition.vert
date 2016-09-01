// non-transformed quad texture vertex shader
#version 330 core

layout(location = 0) in vec4 VertexIn;
layout(location = 2) in vec2 TexIn;

out vec2 texCoord;

uniform mat4 modelMatrix  = mat4(1.0);

void main()
{
	gl_Position = modelMatrix * VertexIn;
	texCoord = TexIn;
}