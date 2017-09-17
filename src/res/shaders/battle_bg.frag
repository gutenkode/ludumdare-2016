// texture fragment shader
#version 330 core

in vec2 texCoord;

layout(location = 0) out vec4 FragColor;
layout(location = 1) out vec4 DOFValue;

uniform sampler2D texture1;

uniform float cycle = 0.0;

void main()
{
	vec2 t = texCoord;
	t.y += .1f*sin(cycle*4+texCoord.x*6.0f);
	t.x += .1f*sin(cycle*4+texCoord.y*6.0f);

	//FragColor = texture(texture1, t);
	FragColor = texture(texture1, t+vec2(-cycle,cycle/4.0)); // normal shifting effect
	FragColor += .5 * texture(texture1, vec2(.5)+t+vec2(-cycle*2.0,cycle/2.0)); // darker "echo" texture

	//FragColor = texture(texture1, texCoord+vec2(-cycle,cycle/4.0)); // normal shifting effect
	//FragColor += .5 * texture(texture1, texCoord+vec2(-cycle*2.0,cycle/2.0)); // darker "echo" texture

	// prevent DOF rendering
	DOFValue = vec4(0,0,0,1);
}
