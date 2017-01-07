// final mix fragment shader
#version 330 core

in vec2 texCoord;

out vec4 FragColor;

//uniform sampler2D tex_scene;
uniform sampler2D tex_ui;
uniform sampler2D tex_bloom;
uniform sampler2D tex_dof;
uniform sampler2D tex_dofvalue;
uniform sampler2D tex_noise;
uniform sampler2D tex_vignette;
//uniform sampler2D tex_scanlines;
uniform float bloomCoef = 1.0,
			  aspectRatio = 16.0/9.0,
			  dofCoef = 0.0;
uniform vec2 rand;
uniform vec3 colorMult = vec3(1.0);

void main()
{
	// the UI texture
	vec4 ui = texture(tex_ui, texCoord);

	// blend 3D scene with blurred DOF scene
	/*
	vec4 v1 = texture(tex_scene, texCoord);
	vec4 v2 = texture(tex_dof, texCoord);
	float dofvalue = texture(tex_dofvalue, texCoord).r + dofCoef;
	dofvalue *=2;
	dofvalue = clamp(dofvalue, 0,1);
	dofvalue = min(dofvalue, 1-ceil(ui.a)); // if there is UI here, use the full blurred texture
	//FragColor = v1*(texCoord.y) + v2*(1-texCoord.y);
	//dofvalue = 0.0; // 0 = blur, 1 = solid
	FragColor = mix(v2, v1, dofvalue);
	*/
	FragColor = texture(tex_dof, texCoord);

	// put the non-blurred UI over all of it
	FragColor = ui*(ui.a) + FragColor*(1-ui.a);

	// bloom
	FragColor += /*texture(tex_scanlines, texCoord) */ texture(tex_bloom, texCoord) * .7 * bloomCoef;

	// noise and vignette
	vec2 noiseCoord = (texCoord + rand) * vec2(aspectRatio,1);
	FragColor *= texture(tex_noise, noiseCoord*2);
	FragColor *= texture(tex_vignette, texCoord);
	//FragColor *= texture(tex_scanlines, texCoord*vec2(1,128));

	FragColor.xyz *= colorMult; // used for fading in/out
	FragColor.a = 1;

	//FragColor = texture(tex_dof, texCoord);
	//FragColor = vec4(vec3(dofvalue),1);
}
