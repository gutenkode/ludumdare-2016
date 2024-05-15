// texture fragment shader
#version 330 core

/*noperspective*/ in vec2 texCoord;
/*noperspective*/ in vec2 shadeCoord;
in vec3 vertexPos;

layout(location = 0) out vec4 FragColor;
layout(location = 1) out vec4 DOFValue;

uniform sampler2D tex_diffuse;
uniform sampler2D tex_shade;

uniform vec4 colorMult = vec4(1.0);
uniform vec4 colorAdd = vec4(0.0);
uniform vec2 mapSize = vec2(3.0);

void fadeEdges()
{
	const vec3 fade = vec3(0.4, 0.0, 0.0);
	FragColor.rgb = mix(fade, FragColor.rgb, smoothstep(0,.8, vertexPos.x));
	FragColor.rgb = mix(fade, FragColor.rgb, smoothstep(0,.8, vertexPos.y));
	FragColor.rgb = mix(fade, FragColor.rgb, smoothstep(0,.8, mapSize.x-vertexPos.x));
	FragColor.rgb = mix(fade, FragColor.rgb, smoothstep(0,.25, mapSize.y-vertexPos.y));
}

void main()
{
	FragColor = texture(tex_diffuse, texCoord); // color texture component
    FragColor.rgb *= texture(tex_shade, shadeCoord).rgb; // shade texture component

	fadeEdges();

	DOFValue = vec4(0,0,0,1);
	FragColor = colorMult * (colorAdd + FragColor);
}
