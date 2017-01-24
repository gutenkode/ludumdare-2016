// non-transformed quad texture fragment shader
#version 330 core

in vec2 texCoord;

out vec4 FragColor;

uniform sampler2D texture1;

void main()
{
	FragColor = texture(texture1, texCoord);

	float v = 1-1.0/32.0;
	vec4 blur;
	vec2 coord = ((texCoord-.5)*vec2(v,v))+.5;
	blur = texture(texture1, coord);
	/*
	blur += texture(texture1, texCoord* vec2( v,1) )*.25;
	blur += texture(texture1, texCoord* vec2(-v,1) )*.25;
	blur += texture(texture1, texCoord* vec2(1, v) )*.25;
	blur += texture(texture1, texCoord* vec2(1,-v) )*.25;*/

	FragColor = FragColor*.71 + blur*.29;
	FragColor *= vec4(1.02, 1, 0.99, 1);

	//FragColor *= 0.5;
	//FragColor = floor(FragColor*10)/8;

	//FragColor = (FragColor*.85+.075);
	//FragColor.a = .5;
}
