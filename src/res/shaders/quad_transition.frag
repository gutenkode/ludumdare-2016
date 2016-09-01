// non-transformed quad texture fragment shader
#version 330 core

in vec2 texCoord;

out vec4 FragColor;

uniform sampler2D texture1;

void main() 
{
	float v = 1.0/64.0;
	FragColor = vec4(0,0,0,1);
	//FragColor = texture(texture1, texCoord)*.25;
	FragColor += texture(texture1, texCoord+ vec2( v,0) )*.25;
	FragColor += texture(texture1, texCoord+ vec2(-v,0) )*.25;
	FragColor += texture(texture1, texCoord+ vec2(0, v) )*.25;
	FragColor += texture(texture1, texCoord+ vec2(0,-v) )*.25;

	//FragColor *= 0.3;
	FragColor = floor(FragColor*10)/8;

	FragColor = (FragColor*.85+.075);
	//FragColor.a = .5;
}