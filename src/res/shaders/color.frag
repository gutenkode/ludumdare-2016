// color fragment shader
#version 330 core

out vec4 FragColor;

uniform vec4 colorMult = vec4(1,1,1,1);
uniform vec4 colorAdd = vec4(0,0,0,0);

void main()
{
	FragColor = colorMult * (colorAdd + vec4(1));
}