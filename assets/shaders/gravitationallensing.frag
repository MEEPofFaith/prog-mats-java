#define HIGHP

#define HALFPI 3.1415926535897932384626433832795 / 2.0

uniform sampler2D u_texture;

uniform vec2 u_campos;
uniform vec2 u_resolution;

uniform int u_blackholecount;
uniform vec4 u_blackholes[MAX_COUNT];

varying vec2 v_texCoords;

float interp(float a){
    return 1.0 - cos(a * HALFPI);
}

void main() {
    vec2 c = v_texCoords.xy;
    vec2 coords = (c * u_resolution) + u_campos;

    vec2 offset = vec2(0.0);
    for(int i = 0; i < u_blackholecount; ++i){
        vec4 blackhole = u_blackholes[i];
        float cX = blackhole.r;
        float cY = blackhole.g;
        float iR = blackhole.b;
        float oR = blackhole.a;

        float dst = distance(blackhole.xy, coords);
        if(dst < iR){ //Inside black hole, set to black
            gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
            return;
        }else if(dst > oR){ //Outside black hole, skip
            continue;
        }else{ //Influence position
            float p = (dst - iR) / (oR - iR);
            p = interp(p);
            float a = atan(coords.x - cX, coords.y - cY) + HALFPI;
            vec2 pos = vec2(cX - oR * cos(a) * p, cY + oR * sin(a) * p);

            offset += pos - coords;
        }
    }

    coords += offset;
    gl_FragColor = texture2D(u_texture, (coords - u_campos) / u_resolution);
}
