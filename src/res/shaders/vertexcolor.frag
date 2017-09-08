// color fragment shader
#version 330 core

in vec3 color;

out vec4 FragColor;

uniform vec4 colorMult = vec4(1.0);
uniform vec3 colorAdd = vec3(0.0);

void main()
{
	// colorMult takes precedence over colorAdd
	FragColor = colorMult * (vec4(colorAdd,0) + vec4(color,1));
}
