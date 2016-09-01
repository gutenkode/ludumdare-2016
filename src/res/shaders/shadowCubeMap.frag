#version 330 core

//layout(location = 0) out float fragmentdepth;

in vec4 FragPos;

uniform vec3 lightPos;
uniform float far_plane = 25.0;

void main()
{
    // get distance between fragment and light source
    float lightDistance = length(lightPos - FragPos.xyz);
    
    // map to [0:1] range by dividing by far_plane
    //lightDistance /= far_plane;
    
    // Write this as modified depth
    gl_FragDepth = lightDistance;
    //fragmentdepth = lightDistance;
}

/*
#version 330 core

layout(location = 0) out float fragmentdepth;

void main() {
	fragmentdepth = gl_FragCoord.z;
}
*/