// renders the background for the title screen, and sets DOF value to max
#version 330 core

in vec2 texCoord;

layout(location = 0) out vec4 FragColor;
layout(location = 1) out vec4 DOFValue;

uniform sampler2D texture1;
uniform vec3 colorMult = vec3(1.0);
uniform float ratio = 1.0;
uniform float offset = 0.0;

void main()
{
	FragColor = texture(texture1, texCoord*vec2(1,ratio)+vec2(offset,-offset*.25));
	FragColor.xyz *= colorMult;
	DOFValue = vec4(0,.2,0,1);
}
