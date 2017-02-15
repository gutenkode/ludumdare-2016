// texture fragment shader
#version 330 core

/*noperspective*/ in vec2 texCoord;
/*noperspective*/ in vec2 shadeCoord;

layout(location = 0) out vec4 FragColor;
layout(location = 1) out vec4 DOFValue;

uniform sampler2D tex_diffuse;
uniform sampler2D tex_shade;

uniform vec4 colorMult = vec4(1.0);
uniform vec4 colorAdd = vec4(0.0);

void main()
{
	// color texture component
	FragColor = texture(tex_diffuse, texCoord);
	// shade texture component
    FragColor.rgb *= texture(tex_shade, shadeCoord).rgb;

	DOFValue = vec4(0,0,0,0);

	FragColor = colorMult * (colorAdd + FragColor);
}
