package com.nixiedroid.magiclines;

public enum Shaders {
    BACKGROUND_VERTEX("attribute vec4 v_Position; attribute vec2 v_textureCoords;" +
            "   uniform vec3 u_PrimaryColor;  uniform float u_xoffset;" +
            "  uniform vec4 u_in_time_height_color_xOffset;   " +
            " float u_Time; float u_HeightScale; float u_XOffset;" +
            "  varying vec3 v_Color;   varying vec2 v_textureCoordinate; " +
            " \n void main() {     u_Time = u_in_time_height_color_xOffset.x;  " +
            "   u_HeightScale = u_in_time_height_color_xOffset.y;    " +
            " u_XOffset = u_in_time_height_color_xOffset.w;    " +
            "  gl_Position = vec4(1.0, u_HeightScale, 1.0, 1.0) *" +
            " (v_Position + vec4(0.0, -0.2 * sin(v_Position.y + " +
            "u_XOffset * 2.0 + u_Time + v_Position.x), 0.0, 0.0)); " +
            "    v_Color = u_PrimaryColor;     v_textureCoordinate " +
            "= vec2(v_textureCoords.x, v_textureCoords.y); } "),
    BACKGROUND_FRAGMENT("precision mediump float; uniform " +
            "sampler2D sTexture; varying vec3 v_Color; varying" +
            " vec2 v_textureCoordinate; void main(void){ " +
            " gl_FragColor = texture2D(sTexture, vec2(v_textureCoordinate.x," +
            " v_textureCoordinate.y)); gl_FragColor = vec4(gl_FragColor.xyz " +
            "* v_Color, gl_FragColor.a);}"),
    FOREGROUND_VERTEX("attribute vec4 aPosition; " +
            " uniform mat4 uMVPMatrix;  uniform vec3 " +
            "u_Time_NoiseScale_Color;  float u_NoiseScale;" +
            " float u_Time; uniform vec4 u_Noise; varying vec4" +
            " vPosition; varying vec4 v_deltaX; varying vec4 " +
            "v_deltaY; const float NOISE_LO = 0.03; const float" +
            " NOISE_HI = 0.05; const float TURB = 3.5; " +
            "const float SPEED = 0.2; const float ZERO = 0.0;" +
            " const float PI_TWICE = 3.141 * 2.0; const vec4 " +
            "NOISE_SCALE = vec4(0.7, 1.3, 0.7, 1.0);" +
            "float wave(float x, vec3 params){    float waveParam =" +
            " x * params.x + params.y + params.z;     return " +
            "sin(mod(waveParam, PI_TWICE)); }float addNoiseForAxis(float" +
            " val1, float val2, float val3, float val4){    float ret" +
            " = ZERO;     ret += NOISE_LO * val1 * val2;     ret +=" +
            " NOISE_HI * val3 * val4;     ret += NOISE_LO * val3 * " +
            "val2;     return ret; }vec4 addNoise0(vec4 pos){    ve" +
            "c2 texPos0 = pos.xy + u_Noise.xy * 3.0;     float x = " +
            "texPos0.x;     float y = texPos0.y;     float timeSpee" +
            "dHi = u_Time * SPEED;     float ret = ZERO;     float w" +
            "ave1 = wave(x, vec3((TURB * 5.34), 9.13, timeSpeedHi));" +
            "     float wave2 = wave(y, vec3((TURB * 7.54), 3.33, -t" +
            "imeSpeedHi));     float wave3 = wave(x, vec3(TURB * 3.5" +
            "4, 6.13, -timeSpeedHi));     float wave4 = wave(y, vec3(" +
            "TURB * 5.24, 4.33, timeSpeedHi));     float wave5 = wav" +
            "e(x, vec3(TURB * 7.12, 5.14, timeSpeedHi));     float w" +
            "ave6 = wave(y, vec3(TURB * 1.375, 7.43, -timeSpeedHi));" +
            "     float wave7 = wave(x, vec3(TURB * 3.54, 3.43, -time" +
            "SpeedHi));     float wave8 = wave(x, vec3(TURB * 2.23, 4" +
            ".44, timeSpeedHi));     float wave9 = wave(y, vec3(TURB" +
            " * 4.199, 1.84, -timeSpeedHi));     float wave10 = wave" +
            "(x, vec3(TURB * 3.54, 5.13, -timeSpeedHi));     float nx" +
            " = addNoiseForAxis(wave1, wave2, wave3, wave4);     float " +
            "nz = addNoiseForAxis(wave8, wave9, wave10, wave1);     fl" +
            "oat ny = nx;     return vec4(pos.x + nx, pos.y + ny, ZERO" +
            " + nz, 1.0); }vec4 delta(vec4 pos, float dx, float dy, vec" +
            "4 newPos){    return uMVPMatrix * addNoise0(pos + vec4(dx" +
            ", dy, ZERO, ZERO)) * NOISE_SCALE - newPos; }void main() {" +
            "u_Time = u_Time_NoiseScale_Color.x;     u_NoiseScale = u_" +
            "Time_NoiseScale_Color.y;     gl_Position = uMVPMatrix * a" +
            "ddNoise0(aPosition) * NOISE_SCALE; " +
            "    v_deltaX = delta(aPosition, " +
            "0.045, ZERO, gl_Position);     v_deltaY = delta(aPosition," +
            " ZERO, 0.045, gl_Position);     vPosition = aPosition; }"),
    FOREGROUND_FRAGMENT("precision mediump float; uniform vec3 u_PrimaryColor;" +
            " uniform vec3 u_SecondaryColor; varying vec4 vPosition; varying vec4 " +
            "v_deltaX; varying vec4 v_deltaY; varying vec4 vColor; uniform sampler2D" +
            " sTexture; void main() {     float wave = texture2D(sTexture, vPosition.xy).a;" +
            "     float normalAlpha = (1.1 - (length(cross(v_deltaX.xyz, v_deltaY.xyz))));" +
            "     normalAlpha = clamp(normalAlpha, 0.0, 1.0);     float alpha = normalAlpha" +
            " * (vPosition.y + 0.2);     alpha = pow(alpha, 2.3);      gl_FragColor =" +
            " vec4(u_PrimaryColor + wave * u_SecondaryColor, alpha);  } ");

    final String data;

    Shaders(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }
}
