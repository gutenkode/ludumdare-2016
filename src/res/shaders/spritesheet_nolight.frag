// texture fragment shader
#version 330 core

in vec2 texCoord, texCoordEmissive;

out vec4 FragColor;

uniform sampler2D texture1;
uniform float emissiveMult = 0.0;
uniform vec4 colorMult = vec4(1.0);
uniform vec3 colorAdd = vec3(0.0);

void main() 
{
	FragColor = texture(texture1, texCoord);
	FragColor += vec4(texture(texture1, texCoordEmissive).xyz * emissiveMult, 0); // ignore alpha from emissive texture
	FragColor.xyz += colorAdd;
	FragColor *= colorMult;

	if (FragColor.a == 0.0)
		discard;
}