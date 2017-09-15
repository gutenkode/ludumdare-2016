// final mix fragment shader
#version 330 core

in vec2 texCoord;

out vec4 FragColor;

uniform sampler2D tex_scene;
uniform sampler2D tex_ui;
uniform sampler2D tex_bloom;
uniform sampler2D tex_dof;
uniform sampler2D tex_dofvalue;
//uniform sampler2D tex_noise;
uniform sampler2D tex_vignette;
uniform sampler2D tex_scanlines;
uniform float //aspectRatio = 16.0/9.0,
			  dofCoef = 0.0,
			  bloomCoef = 0.5;
uniform vec2 texSize = vec2(256.0);
uniform vec3 colorMult = vec3(1.0);

vec4 quilezTexture(sampler2D tex, vec2 p)
{
	// pulled from:
	// iquilezles.org/www/articles/texture/texture.htm
    p = p*texSize + 0.5;

    vec2 i = floor(p);
    vec2 f = p - i;
	//f = smoothstep(0,1,f);
    f = f*f*f*(f*(f*6.0-15.0)+10.0); // smoothstep
    p = i + f;

    p = (p - 0.5)/texSize;
    return texture(tex, p);
}

vec4 crtTexture(sampler2D tex, vec2 p)
{
	vec4 result = quilezTexture(tex, p) * .45;
	result += quilezTexture(tex, p+vec2(0.5/texSize.x,0)) * .25;
	result += quilezTexture(tex, p-vec2(0.5/texSize.x,0)) * .25;
	result += quilezTexture(tex, p+vec2(1.5/texSize.x,0)) * .15;
	result += quilezTexture(tex, p-vec2(1.5/texSize.x,0)) * .15;
	//result += quilezTexture(tex, p+vec2(2.5/texSize.x,0)) * .10;
	//result += quilezTexture(tex, p-vec2(2.5/texSize.x,0)) * .10;

	float blue = quilezTexture(tex, p-vec2(2/texSize.x,0)).b * .5;
	result.b = max(result.b, blue);
	return result;
}

void main()
{
	// the UI texture
	vec4 ui_pixel = crtTexture(tex_ui, texCoord);

	// blend 3D scene with blurred DOF scene
	vec4 scene_pixel = crtTexture(tex_scene, texCoord);

	vec4 dof_pixel = texture(tex_dof, texCoord);

	float dof_value = texture(tex_dofvalue, texCoord).r + dofCoef;
	dof_value = clamp(dof_value, 0.0,1.0); // 0 = full blur, 1 = no blur
	// mix the scene with dof, based on the blur value
	FragColor = mix(scene_pixel, dof_pixel, smoothstep(0,1,dof_value));

	// put the non-blurred UI over the scene, mix based on alpha of the UI texture
	FragColor = mix(FragColor, ui_pixel, smoothstep(0,1,ui_pixel.a));

	// vignette, noise, scanlines, etc.
	vec2 scanlineCoord = texCoord * vec2(1,texSize.y);
	FragColor *= texture(tex_scanlines, scanlineCoord);
	FragColor *= texture(tex_vignette, texCoord);
	//FragColor.xyz *= mod(texCoord.y*texSize.y, 1.0); // mathematical scanlines

	// bloom
	FragColor += texture(tex_bloom, texCoord) * bloomCoef;

	FragColor.rgb *= colorMult; // final global multiply, used for fading in/out

	// gamma correction, 2.2 is normal
    //float gamma = 2.0;
    //FragColor.rgb = pow(FragColor.rgb, vec3(1.0/gamma));
}
