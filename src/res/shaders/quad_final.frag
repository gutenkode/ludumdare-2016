// final mix fragment shader
#version 330 core

in vec2 texCoord;

out vec4 FragColor;

uniform sampler2D tex_scene;
uniform sampler2D tex_ui;
uniform sampler2D tex_bloom;
uniform sampler2D tex_dof;
uniform sampler2D tex_dofvalue;
uniform sampler2D tex_noise;
uniform sampler2D tex_vignette;
uniform float bloomCoef = 1.0,
			  aspectRatio = 16.0/9.0,
			  dofCoef = 0.0;
uniform vec2 rand;
uniform vec3 colorMult = vec3(1.0);

void main() 
{
	// blend 3D scene with blurred DOF scene
	vec4 v1 = texture(tex_scene, texCoord);
	vec4 v2 = texture(tex_dof, texCoord);
	float dofvalue = texture(tex_dofvalue, texCoord).r + dofCoef;
	dofvalue = clamp(dofvalue, 0,1);
	//FragColor = v1*(texCoord.y) + v2*(1-texCoord.y);
	//dofvalue = 1.0; // temporary
	FragColor = mix(v2, v1, dofvalue);

	// put the non-blurred UI over all of it
	vec4 ui = texture(tex_ui, texCoord);
	FragColor = ui*(ui.a) + FragColor*(1-ui.a);

	// bloom
	FragColor += texture(tex_bloom, texCoord) * bloomCoef;

	// noise and vignette
	vec2 noiseCoord = (texCoord + rand) * vec2(aspectRatio,1);
	FragColor *= texture(tex_noise, noiseCoord);
	FragColor *= texture(tex_vignette, texCoord);

	FragColor.xyz *= colorMult; // used for fading in/out
}