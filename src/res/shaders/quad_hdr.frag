// texture fragment shader
#version 330 core

in vec2 texCoord;

out vec4 FragColor;

uniform sampler2D texture1;

float sum(vec3 vec) {
	return vec.r + vec.g + vec.b;
}

void main()
{
	FragColor = texture(texture1, texCoord);
	// luminance is the "perceived" brightness of an image by the human eye
	float luminance = sum(FragColor.rgb * vec3(0.299, 0.587, 0.114));
	// to prevent pure white scenes from being washed out, the total brightness is cut for very luminant fragments
	// less luminant fragments have relatively little brightness cut, giving a better range
	FragColor = (FragColor-.666)*3.0*(1-luminance*.5);
}
