#version 330 core

layout(location = 0) in vec4 VertexIn;
layout(location = 2) in vec2 TexIn;

out vec2 texCoord_in;

// contains information about the sprite being drawn:
// number of tiles horizontally,
// number of tiles vertically,
// sprite index to draw
uniform vec3 spriteInfo;

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
	gl_Position = VertexIn;
	texCoord_in = getSpriteCoords(spriteInfo);
}
