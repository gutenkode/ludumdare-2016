// combine fragment shader
// blur 3D scene with DOF, and put UI on top
#version 330 core

in vec2 texCoord;

out vec4 FragColor;

uniform sampler2D tex_scene;
uniform sampler2D tex_ui;
uniform sampler2D tex_dof;
uniform sampler2D tex_post_values;
uniform float dofCoef = 0.0;

void main()
{
	// blend 3D scene with blurred DOF scene
	vec4 scene_pixel = texture(tex_scene, texCoord);
	vec4 dof_pixel = texture(tex_dof, texCoord);
	float dof_value = texture(tex_post_values, texCoord).r + dofCoef; // r component
	// mix the scene with dof, based on the blur value
	FragColor = mix(scene_pixel, dof_pixel, smoothstep(0,1,dof_value));
	FragColor.a = 1;

	// put the non-blurred UI over the scene, mix based on alpha of the UI texture
	vec4 ui_pixel = texture(tex_ui, texCoord); // the UI texture
	FragColor.rgb = mix(FragColor.rgb, ui_pixel.rgb, smoothstep(0,1,ui_pixel.a));
	//FragColor = vec4(vec3(dof_value),1);
}
