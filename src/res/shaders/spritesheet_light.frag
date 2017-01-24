// texture fragment shader
#version 330 core

in vec2 texCoord, texCoordEmissive;
in vec3 vertexPos;
in vec3 normal;
in vec4 shadowCoord;

layout(location = 0) out vec4 FragColor;
layout(location = 1) out vec4 DOFValue;

uniform vec3 lightPos;
uniform vec3 ambient = vec3(.1f);
uniform float flashlightAmbient = .2;

uniform float emissiveMult = 0.0;
uniform vec4 colorMult = vec4(1.0);
uniform vec4 colorAdd = vec4(0.0);

uniform sampler2D texture1;
uniform sampler2DShadow shadowMap; // shadow depth texture
uniform vec3 flashlightAngle = vec3(1,0,0);

uniform vec3[10] eLightPos;
uniform vec3[10] eLightColor;

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
    diffuseCoef *= pow(clamp(dot(L,flashlightAngle), 0,1),2)*2;

    // distance from this fragment to the light source
    float lengthVal = length(lightPos-vertexPos);

    // light attenuation, farther away = less light
    float lightDistCoef = 2.0/lengthVal;

	// depth of field interpolation value
	float dofLength = length(lightPos.y-vertexPos.y);
	DOFValue = vec4(1.0);//vec4(1.0-1.0/pow(dofLength,3.0),0,0,1);

    // bias is used to reduce weird artifacts in shadow
    float bias = 0.005*tan(acos(max(dot(normal,L),0)));
    bias = clamp(bias, 0, 0.01);
    float shadow = textureProj(shadowMap, shadowCoord+vec4(0,0,-bias,0));
		shadow = shadow*.9+.1;

    // apply lighting to fragment, cannot be brighter than the diffuse texture
    vec3 light = max(ambient, vec3(clamp(lightDistCoef*flashlightAmbient+lightDistCoef*diffuseCoef*shadow, 0.0, 1.0)));

	// entity lights
	for (int i = 0; i < 10; i++)
	{
		vec3 l1 = normalize(eLightPos[i] - vertexPos);
		float l2 = length(eLightPos[i]-vertexPos);
		lightDistCoef = 1.0 / (1.0 + 0.01*l2 + 1.8*l2*l2);
		vec3 thisLight = dot(normal,l1)*eLightColor[i]*lightDistCoef;
	    light = max(light,thisLight);
	}

	light = clamp(light,vec3(0),vec3(2));
	FragColor.rgb *= light;

	// emissive texture ignores lighting, does not affect alpha
    FragColor.rgb += texture(texture1, texCoordEmissive).rgb * emissiveMult;

    // final global color adjustement, colorMult takes precedence over colorAdd
	FragColor = colorMult * (colorAdd + FragColor);
}
