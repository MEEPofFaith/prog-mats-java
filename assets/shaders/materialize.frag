uniform sampler2D u_texture;

uniform float u_time;
uniform float u_progress;
uniform float u_offset;
uniform float u_width;
uniform float u_shadow;
uniform vec4 u_color;
uniform vec2 u_uv;
uniform vec2 u_uv2;
uniform vec2 u_texsize;

varying vec2 v_texCoords;

float absin(float t){
    return (sin(t) + 1.0) / 2.0;
}

void main(){
    vec2 coords = (v_texCoords.xy - u_uv) / (u_uv2 - u_uv);
    vec2 v = vec2(1.0 / u_texsize.x, 1.0 / u_texsize.y);

    float w = absin(coords.x * u_width + u_time) * u_offset / 4.0;
    float p = u_progress * (1.0 + u_offset) + w;
    float top = clamp(p, 0.0, 1.0);
    float bottom = clamp(p - u_offset, 0.0, 1.0);

    float y = 1.0 - coords.y;
    vec4 c = texture2D(u_texture, v_texCoords.xy);
    if(y > top){
        c.a = 0.0;
    }else if(y > bottom){
        c.rgb = u_color.rgb;
    }
    if(u_shadow == 1.0){
        c.rgb = vec3(0, 0, 0);
        c.a *= 0.22;
    }

    gl_FragColor = c;
}
