// color fragment shader
#version 330 core

in float depth;

layout(location = 0) out vec4 FragColor;
layout(location = 1) out vec4 DOFValue;

uniform vec4 colorMult = vec4(1,1,1,1);
uniform vec4 colorAdd = vec4(0,0,0,0);

void main()
{
	FragColor = colorMult * (colorAdd + vec4((depth+1.5)/2));
	//FragColor *= depth/2.0+0.5;
	//DOFValue = vec4(vec3(1-depth-.5f,.2,0),1);//-(depth+1)*3);
	DOFValue = vec4(0,.2,0,1);
}
