#define HIGHP

uniform sampler2D u_texture;

uniform float u_alpha;

varying vec2 v_texCoords;

void main() {
    vec2 T = v_texCoords.xy;
    vec4 color = texture2D(u_texture, T);
    color.a *= u_alpha;

    gl_FragColor = color;
}
