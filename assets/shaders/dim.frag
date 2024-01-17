
uniform sampler2D u_texture;
uniform float u_alpha;

varying vec2 v_texCoords;

void main() {
    float a = texture2D(u_texture, v_texCoords).a;
    gl_FragColor = vec4(0, 0, 0, 1 * u_alpha * (1.0 - a));
}
