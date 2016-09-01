// texture fragment shader
#version 330 core

in vec2 texCoord;

out vec4 FragColor;

uniform sampler2D texture1;

void main()
{
	FragColor = (texture(texture1, texCoord)-.5)*2.0;//1.177;
}