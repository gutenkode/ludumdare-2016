// texture fragment shader
#version 330 core

in vec2 texCoord;

layout(location = 0) out vec4 FragColor;
layout(location = 1) out vec4 DOFValue;

uniform sampler2D texture1;
uniform vec4 colorMult = vec4(1.0);
uniform vec3 colorAdd = vec3(0.0);

float sum(vec3 v) {
	return dot(vec3(1.0),v);
}

void main()
{
	// colorMult takes precedence over colorAdd
	FragColor = colorMult * (vec4(colorAdd,0) + texture(texture1, texCoord));
	if (FragColor.a == 0)
		discard;
	float luminance = sum(FragColor.rgb * vec3(0.299, 0.587, 0.114));
	DOFValue = vec4(1,luminance * .5,0,1); // R = blur, G = bloom
}
