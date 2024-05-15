// texture fragment shader
#version 330 core

in vec2 texCoord;

out vec4 FragColor;

uniform sampler2D texture1;
uniform sampler2D tex_bloomvalue;

void main()
{
	FragColor = texture(texture1, texCoord);

	float bloom_value = texture(tex_bloomvalue, texCoord).g; // G component = bloom coefficient
	FragColor.rgb *= bloom_value;
}
