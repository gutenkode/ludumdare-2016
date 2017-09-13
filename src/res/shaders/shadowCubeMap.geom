#version 330 core

layout (triangles) in;
layout (triangle_strip, max_vertices=18) out;

uniform mat4 depthProj[6]; // projection matrix for a cubemap
uniform mat4 viewMatrix = mat4(1.0);
uniform mat4 modelMatrix = mat4(1.0);

in vec2[3] texCoord_in;
out vec2 texCoord_out;

void main()
{
    for (int face = 0; face < 6; face++)
    {
        gl_Layer = face; // built-in variable that specifies which face we render to
        for (int i = 0; i < 3; i++) // for each triangle's vertices
        {
            gl_Position = depthProj[face] * viewMatrix * modelMatrix * gl_in[i].gl_Position;
            texCoord_out = texCoord_in[i]; // just pass them through
            EmitVertex();
        }
        EndPrimitive();
    }
}
