#version 330 core

in vec2 texCoord_out;
uniform sampler2D texture1;

void main()
{
	if (texture(texture1, texCoord_out).a == 0.0)
		discard;

    // writes to depth buffer automagically~
}
