#version 330 core

layout(location = 0) out float fragmentdepth;

in vec2 texCoord;

uniform sampler2D texture1;

void main() {
	if (texture(texture1, texCoord).a == 0.0)
		discard;

	fragmentdepth = gl_FragCoord.z;
}