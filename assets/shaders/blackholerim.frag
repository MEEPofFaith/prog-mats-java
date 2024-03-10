#define HIGHP

#define MUL 4.0

uniform sampler2D u_texture;

uniform vec2 u_campos;
uniform vec2 u_resolution;

uniform int u_blackholecount;
uniform vec4 u_blackholes[MAX_COUNT];
uniform vec4 u_colors[MAX_COUNT];

varying vec2 v_texCoords;

//https://stackoverflow.com/a/72973369
vec4 blendOver(vec4 a, vec4 b) {
    float newAlpha = mix(b.w, 1.0, a.w);
    vec3 newColor = mix(b.w * b.xyz, a.xyz, a.w);
    float divideFactor = (newAlpha > 0.001 ? (1.0 / newAlpha) : 1.0);
    return vec4(newColor * divideFactor, newAlpha);
}

//Made by MEEPofFaith
void main() {
    vec2 c = v_texCoords.xy;
    vec2 coords = (c * u_resolution) + u_campos;
    vec4 col = vec4(0.0);

    for(int i = 0; i < u_blackholecount; ++i){
        vec4 blackhole = u_blackholes[i];
        float cX = blackhole.r;
        float cY = blackhole.g;
        float iR = blackhole.b;

        float dst = distance(blackhole.xy, coords);
        if(dst < iR){ //Inside, black
            gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
            return;
        }else if(dst > iR * MUL){ //Outside, skip
            continue;
        }else{ //Add color
            float p = 1.0 - (dst - iR) / (iR * MUL - iR);
            vec4 c1 = u_colors[i];
            c1.a = p;

            col = blendOver(col, c1);
        }
    }

    gl_FragColor = col;
}
