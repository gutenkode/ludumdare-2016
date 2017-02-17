// texture vertex shader
#version 330 core

layout(location = 0) in vec4 VertexIn;
layout(location = 2) in vec2 TexIn;
layout(location = 3) in vec3 NormalIn;

out vec2 texCoord,
	     texCoordEmissive;
out vec3 vertexPos;
out vec3 normal;

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
	//gl_Position = floor(gl_Position*50.0)/50.0;

	// light location value is in model space, so vertexPos must be in model space
	vertexPos = vec3(modelMatrix * VertexIn);
	normal = mat3(modelMatrix) * NormalIn;

	texCoord = getSpriteCoords(spriteInfo);
	texCoordEmissive = getSpriteCoords(spriteInfoEmissive);
}
