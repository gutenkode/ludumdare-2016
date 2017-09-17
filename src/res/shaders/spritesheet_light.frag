// texture fragment shader
#version 330 core

in vec2 texCoord, texCoordEmissive;
in vec3 vertexPos;
in vec3 normal;

layout(location = 0) out vec4 FragColor;
layout(location = 1) out vec4 DOFValue;

uniform vec3 lightPos;
uniform vec3 ambient = vec3(0.0);
uniform float flashlightAmbient = 0.3,
			  shadowNearPlane = 0.0,
			  shadowFarPlane = 0.0;

uniform float emissiveMult = 0.0;
uniform vec4 colorMult = vec4(1.0);
uniform vec4 colorAdd = vec4(0.0);

uniform sampler2D texture1;
uniform samplerCubeShadow shadowCubeMap;
uniform vec3 flashlightAngle = vec3(1,0,0);

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

void writeDOF(float bloom)
{
	// depth of field interpolation value
	float dofLength = 0;//length(lightPos.y-vertexPos.y);
	//dofLength = 1.0-1.0/pow(dofLength,3.0);
	DOFValue = vec4(dofLength,bloom,0,1);
}

float sum(vec3 vec) {
	return vec.r + vec.g + vec.b;
}

void main()
{
	// color texture component
	FragColor = texture(texture1, texCoord);

	if (FragColor.a == 0.0)
		discard;

    // typical diffuse lighting calculation
    vec3 L = normalize(lightPos - vertexPos);
    float diffuseCoef = dot(normal,L) +0.25;

    // flashlight cone
    float coneDiffuseCoef = diffuseCoef * pow(clamp(dot(L,flashlightAngle), 0,1),2) * 2;

    // distance from this fragment to the light source
    float lengthVal = length(lightPos-vertexPos);

    // light attenuation, farther away = less light
    float lightDistCoef = 2.0/lengthVal;

	// shadow rendering!
    // bias is used to reduce weird artifacts in shadow, "shadow acne"
    float bias = 0.003*tan(acos(max(dot(normal,L),0)));
    bias = clamp(bias, 0, 0.01);
	vec3 lightVec = vertexPos - lightPos;
	float LightDepth = shadowCalculation(lightVec*vec3(-1,1,1));
	float shadow = texture(shadowCubeMap,vec4(L*vec3(-1,1,1),LightDepth-bias));
	shadow = shadow*.9+.1;

    // apply lighting to fragment, cannot be brighter than the diffuse texture
    vec3 light = max(ambient, vec3(clamp(
		lightDistCoef*diffuseCoef*flashlightAmbient+
		lightDistCoef*coneDiffuseCoef*shadow,
		0.0, 1.0)));

	// entity lights
	for (int i = 0; i < 10; i++)
	{
		vec3 l1 = normalize(eLightPos[i] - vertexPos);
		float l2 = length(eLightPos[i]-vertexPos);
		lightDistCoef = 1.0 / (1.0 + 0.01*l2 + 1.8*l2*l2);
		vec3 thisLight = dot(normal,l1)*eLightColor[i]*lightDistCoef*clamp(shadow,.5,1);
	    light = max(light,thisLight);
	}
	// actually apply lighting
	light = clamp(light,vec3(0),vec3(1));
	FragColor.rgb *= light;

	// emissive texture ignores lighting, does not affect alpha
    vec3 emissive = texture(texture1, texCoordEmissive).rgb * emissiveMult;
	FragColor.rgb += emissive;
	writeDOF(sum(emissive) > 0 ? 1 : 0); // if there is emissive color, enable bloom here
	// final global color adjustement, colorMult takes precedence over colorAdd
	FragColor = colorMult * (colorAdd + FragColor);
}
