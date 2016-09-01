// non-transformed quad texture fragment shader
#version 330 core

in vec2 texCoord;

out vec4 FragColor;

uniform sampler2D texture1;
uniform vec2 screenSize = vec2(4.0);

mat4 ditherMat = mat4(1,9,3,11, 13,5,15,7, 4,12,2,10, 16,8,14,6) * (1.0/17.0) * .25;

void main() 
{
	FragColor = texture(texture1, texCoord);

	int pixX = int(screenSize.x*texCoord.x);
	int pixY = int(screenSize.y*texCoord.y);
	FragColor = FragColor + (FragColor * ditherMat[int(mod(pixX,4))][int(mod(pixY,4))]);
	//float ditherVal = mod(pixX+pixY, 2);
	FragColor = floor(FragColor*12.0)/12.0;
}