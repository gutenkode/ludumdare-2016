// texture fragment shader
#version 330 core

/*noperspective*/ in vec2 texCoord;
/*noperspective*/ in vec2 shadeCoord;
in vec3 vertexPos;
in mat3 normalMatrix;

layout(location = 0) out vec4 FragColor;
layout(location = 1) out vec4 DOFValue;

uniform vec3 lightPos;
uniform vec3 ambient = vec3(0.0);
uniform float flashlightAmbient = 0.3,
			  shadowNearPlane = 0.0,
			  shadowFarPlane = 0.0;

uniform sampler2D tex_diffuse;
uniform sampler2D tex_shade;
uniform sampler2D tex_bump;
uniform samplerCubeShadow shadowCubeMap;

uniform vec4 colorMult = vec4(1.0);
uniform vec4 colorAdd = vec4(0.0);
uniform vec3 flashlightAngle = vec3(1,0,0);
uniform vec2 mapSize = vec2(10.0);

uniform vec3[16] eLightPos;
uniform vec3[16] eLightColor;

float shadowCalculation(vec3 Vec)
{
    vec3 AbsVec = abs(Vec);
    float LocalZcomp = max(AbsVec.x, max(AbsVec.y, AbsVec.z));

    float f = shadowFarPlane;
    float n = shadowNearPlane;

    float NormZComp = (f+n) / (f-n) - (2*f*n)/(f-n)/LocalZcomp;
    return (NormZComp + 1.0) * 0.5;
}

void fadeEdges()
{
	const vec3 fade = vec3(0.0);
	FragColor.rgb = mix(fade, FragColor.rgb, smoothstep(0,.8, vertexPos.x));
	FragColor.rgb = mix(fade, FragColor.rgb, smoothstep(0,.8, vertexPos.y));
	FragColor.rgb = mix(fade, FragColor.rgb, smoothstep(0,.8, mapSize.x-vertexPos.x));
	FragColor.rgb = mix(fade, FragColor.rgb, smoothstep(0,.25, mapSize.y-vertexPos.y));
}

vec3 findBumpNormal()
{
	// calcuate the normal for the surface based on its bumpmap
	vec3 bumpNormal = vec3(texture(tex_bump, texCoord));
    bumpNormal = bumpNormal*2.0-1.0;
    bumpNormal = normalMatrix * bumpNormal;
    normalize(bumpNormal);
    //bumpNormal = normalMatrix * vec3(0,0,1); // debug, disable bumpmapping
	return bumpNormal;
}

void writeDOF()
{
	// depth of field interpolation value
	float dofLength = 0;//length(lightPos.y-vertexPos.y);
	//dofLength = 1.0-1.0/pow(dofLength,3.0);
	DOFValue = vec4(dofLength,0,0,1);
}

void main()
{
	FragColor = texture(tex_diffuse, texCoord); // color texture component
    FragColor.rgb *= texture(tex_shade, shadeCoord).rgb; // shade texture component

    // diffuse lighting calculation
	vec3 bumpNormal = findBumpNormal();
    float lengthVal = length(lightPos-vertexPos); // distance from this fragment to the light source
	vec3 L = normalize(lightPos - vertexPos);
    float diffuseCoef = dot(bumpNormal,L);
    float coneDiffuseCoef = diffuseCoef * pow(clamp(dot(L,flashlightAngle), 0,1),2) * 2; // flashlight cone
    float lightDistCoef = 2.0/lengthVal; // light attenuation, farther away = less light

	// shadow rendering!
    // bias is used to reduce weird artifacts in shadow, e.g. "shadow acne"
    float bias = 0.003*tan(acos(max(dot(bumpNormal,L),0)));
    bias = clamp(bias, 0, 0.01);
	vec3 lightVec = vertexPos - lightPos;
	float LightDepth = shadowCalculation(lightVec*vec3(-1,1,1));
	float shadow = texture(shadowCubeMap,vec4(L*vec3(-1,1,1),LightDepth-bias));
	shadow = shadow*.9+.1;

    // apply lighting to fragment, cannot be brighter than the diffuse texture
    vec3 light = max(ambient, vec3(clamp(
        lightDistCoef*diffuseCoef*flashlightAmbient + // dark ambient circle around player
        lightDistCoef*coneDiffuseCoef*shadow, // flashlight and shadow map
        0.0, 1.0)));

	// entity lights
	for (int i = 0; i < 10; i++)
	{
		vec3 l1 = normalize(eLightPos[i] - vertexPos);
		float l2 = length(eLightPos[i]-vertexPos);
		lightDistCoef = 1.0 / (1.0 + 0.01*l2 + 1.8*l2*l2);
		vec3 thisLight = dot(bumpNormal,l1)*eLightColor[i]*lightDistCoef*clamp(shadow,.5,1);
	    light = max(light,thisLight);
	}
	// actually apply lighting
	light = clamp(light,vec3(0),vec3(1));
	FragColor.rgb *= light;

	fadeEdges();

    // final global color adjustement, colorMult takes precedence over colorAdd
	FragColor = colorMult * (colorAdd + FragColor);
	writeDOF();
}
