// texture vertex shader
#version 330 core

layout(location = 0) in vec4 VertexIn;
layout(location = 1) in vec2 TexIn;

layout(location = 2) in vec3 NormalIn1;
layout(location = 3) in vec3 NormalIn2;
layout(location = 4) in vec3 NormalIn3;

layout(location = 5) in vec2 ShadeIn;

/*noperspective*/ out vec2 texCoord;
/*noperspective*/ out vec2 shadeCoord;
out vec3 vertexPos;
out mat3 normalMatrix;

uniform mat4 projectionMatrix = mat4(1.0);
uniform mat4 viewMatrix  = mat4(1.0);
uniform mat4 modelMatrix  = mat4(1.0);

void main()
{
	gl_Position = projectionMatrix * viewMatrix * modelMatrix * VertexIn;
	//gl_Position = floor(gl_Position*50.0)/50.0;

	texCoord = TexIn;
	shadeCoord = ShadeIn;

	// light location value is in model space, so vertexPos must be in model space
	vertexPos = vec3(modelMatrix * VertexIn);
	normalMatrix = mat3(NormalIn1,NormalIn2,NormalIn3);
}
