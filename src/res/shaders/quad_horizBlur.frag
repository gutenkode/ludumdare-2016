// bloom horizontal blur texture
#version 330 core

in vec2 texCoord;

out vec4 FragColor;

uniform sampler2D texture1; // the texture with the scene you want to blur
uniform float blurSize = 1.0/64.0;
uniform float rampCoef = 1.0;
 
void main(void)
{
   vec4 sum = vec4(0.0);
 
   // blur in y (vertical)
   // take nine samples, with the distance blurSize between them
   sum += texture(texture1, vec2(texCoord.x - 4.0*blurSize, texCoord.y)) * 0.05;
   sum += texture(texture1, vec2(texCoord.x - 3.0*blurSize, texCoord.y)) * 0.09;
   sum += texture(texture1, vec2(texCoord.x - 2.0*blurSize, texCoord.y)) * 0.12;
   sum += texture(texture1, vec2(texCoord.x - blurSize, 	   texCoord.y)) * 0.15;
   sum += texture(texture1, vec2(texCoord.x, 			      texCoord.y)) * 0.16;
   sum += texture(texture1, vec2(texCoord.x + blurSize, 	   texCoord.y)) * 0.15;
   sum += texture(texture1, vec2(texCoord.x + 2.0*blurSize, texCoord.y)) * 0.12;
   sum += texture(texture1, vec2(texCoord.x + 3.0*blurSize, texCoord.y)) * 0.09;
   sum += texture(texture1, vec2(texCoord.x + 4.0*blurSize, texCoord.y)) * 0.05;
 
   FragColor = sum*rampCoef;
   FragColor.a = 1.0;
}