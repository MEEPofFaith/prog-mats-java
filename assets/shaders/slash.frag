#define HIGHP

#define PI 3.1415926535897932384626433832795
#define PI2 PI * 2.0
#define HALFPI PI / 2.0

uniform sampler2D u_texture;

uniform vec2 u_campos;
uniform vec2 u_resolution;

uniform int u_slashescount;
uniform vec4 u_slashes[MAX_COUNT]; //x, y, angle, offset

varying vec2 v_texCoords;

bool invert(float a1, float a2){
    float fd = abs(a1 - a2);
    float bd = PI2 - fd;
    return a1 > a2 == bd > fd;
}

void main() {
    vec2 c = v_texCoords.xy;
    vec2 coords = (c * u_resolution) + u_campos;

    vec2 offset = vec2(0.0);
    for(int i = 0; i < u_slashescount; ++i){
        vec4 slash = u_slashes[i];
        float sX = slash.r;
        float sY = slash.g;
        float sA = slash.b; // [0, 2pi]
        float sO = slash.a;

        float angleTo = atan(coords.x - sX, coords.y - sY); // [-pi, pi]
        if(angleTo < 0.0) angleTo += PI2; // Convert to [0, 2pi]

        vec2 off = vec2(cos(HALFPI - sA) * sO, sin(HALFPI - sA) * sO);
        if(invert(angleTo, sA)) off = -off; //Below the slash, invert

        offset += off;
    }

    coords += offset;
    gl_FragColor = texture2D(u_texture, (coords - u_campos) / u_resolution);
}
