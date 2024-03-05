#define HIGHP

#define HALFPI 3.1415926535897932384626433832795 / 2.0

uniform sampler2D u_texture;

uniform vec2 u_campos;
uniform vec2 u_resolution;

uniform vec2 u_center;
uniform vec2 u_radii;

varying vec2 v_texCoords;

float interp(float a){
    return 1.0 - cos(a * HALFPI);
}

void main() {
    vec2 c = v_texCoords.xy;
    vec2 coords = (c * u_resolution) + u_campos;
    float dst = distance(u_center, coords);

    if(dst < u_radii.x){
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
    }else if(dst > u_radii.y){
        gl_FragColor = texture2D(u_texture, c);
    }else{
        float p = (dst - u_radii.x) / (u_radii.y - u_radii.x);
        p = interp(p);
        float a = atan(coords.x - u_center.x, coords.y - u_center.y) + HALFPI;
        vec2 pos = u_center + vec2(-u_radii.y * cos(a) * p, u_radii.y * sin(a) * p);
        gl_FragColor = texture2D(u_texture, (pos - u_campos) / u_resolution);
    }
}
