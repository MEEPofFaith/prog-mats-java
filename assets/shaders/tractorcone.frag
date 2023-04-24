#define HIGHP

uniform sampler2D u_texture;

uniform vec2 u_texsize;
uniform float u_progress;
uniform float u_time;
uniform float u_dp;
uniform vec2 u_offset;
uniform float u_spacing;
uniform float u_thickness;
uniform float u_cx;
uniform float u_cy;

varying vec2 v_texCoords;

float dst(vec2 coords){
    float dx = coords.x / u_dp - u_cx;
    float dy = coords.y / u_dp - u_cy;

    return sqrt(dx * dx + dy * dy);
}

float fixMod(float x, float n){
    return mod(mod(x, n) + n, n);
}

void main(){
    vec2 T = v_texCoords.xy;
    vec2 coords = (T * u_texsize) + u_offset;
    float dst = fixMod(dst(coords) + u_time, u_spacing);

    vec4 color = texture2D(u_texture, T);

    if(dst > u_thickness){
        color.a *= 0.4;
    }

    gl_FragColor = color;
}
