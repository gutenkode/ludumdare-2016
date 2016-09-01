// texture vertex shader
#version 330 core

layout(location = 0) in vec4 VertexIn;
layout(location = 2) in vec2 TexIn;

out vec2 texCoord, texCoordEmissive;

uniform mat4 projectionMatrix; 	// defines the visible area on the screen
uniform mat4 viewMatrix;	// represents camera transformations
uniform mat4 modelMatrix;	// represents model transformations

// contains information about the sprite being drawn:
// number of tiles horizontally,
// number of tiles vertically,
// sprite index to draw
uniform vec3 spriteInfo;
uniform vec3 spriteInfoEmissive;

vec2 getSpriteCoords(vec3 info) {
	float posX = info.z;
	float posY = floor(info.z / info.x);
	float width = 1.0/info.x;
	float height = 1.0/info.y;

	vec2 tex = TexIn;
	tex *= vec2(width,height);
	tex += vec2(width*posX,height*posY);
	return tex;
}

void main()
{
	gl_Position = projectionMatrix * viewMatrix * modelMatrix * VertexIn;

	texCoord = getSpriteCoords(spriteInfo);
	texCoordEmissive = getSpriteCoords(spriteInfoEmissive);
}