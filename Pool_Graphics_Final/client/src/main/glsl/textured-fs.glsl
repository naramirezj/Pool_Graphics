#version 300 es

precision highp float;

in vec4 texCoord;
in vec3 worldNormal;
in vec4 worldPosition;
in vec4 modelPosition;

uniform struct{
  vec3 position;
  mat4 viewProjMatrix; 
} camera;

uniform struct {
  sampler2D colorTexture; 
  samplerCube envTexture;
} material;

uniform struct {
  vec4 position;
  vec3 powerDensity;
} lights[8];

out vec4 fragmentColor;

vec3 noiseGrad(vec3 r) {
  uvec3 s = uvec3(
    0x1D4E1D4E,
    0x58F958F9,
    0x129F129F);
  vec3 f = vec3(0, 0, 0);
  for(int i=0; i<16; i++) {
    vec3 sf =
    vec3(s & uvec3(0xFFFF))
  / 65536.0 - vec3(0.5, 0.5, 0.5);
    
    f += cos(dot(sf, r)) * sf;
    s = s >> 1;
  }
  return f;
}

void main(void) {
  vec3 viewDir = normalize(camera.position - worldPosition.xyz);
  vec3 normal = normalize(worldNormal);
  //normal += noiseGrad(modelPosition.xyz * 50.0) * 0.05;
  //normal = normalize(normal);
  //vec3 reflDir = reflect(-viewDir, normal);
  //fragmentColor = texture(material.envTexture, reflDir);

  vec3 radiance = vec3(0, 0, 0);

  for(int iLight=0; iLight<2; iLight++){
    vec3 lightDiff =
      lights[iLight].position.xyz
         - worldPosition.xyz * lights[iLight].position.w;
    vec3 lightDir = normalize(lightDiff);
    float distanceSquared = dot(lightDiff, lightDiff);
    vec3 powerDensity =
      lights[iLight].powerDensity 
         / distanceSquared;


    //vec3 lightDir = lights[iLight].position.xyz;
    float cosa = clamp(
      dot(lightDir, normal),
           0.0,
           1.0);
    radiance += 
      powerDensity * // M
      cosa * // n*l
      texture(material.colorTexture, texCoord.xy).rgb // BRDF
      ;
    }

  fragmentColor = vec4(radiance, 1);
}