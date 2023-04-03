package com.nixiedroid.magiclines.color;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import com.nixiedroid.magiclines.R;

public class Color implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Context context;
    private SharedPreferences preferences;
    private int primary;
    private int accent;
    public float[][] currentColor;
    public boolean isDarkMode = true;

    public boolean isBloomDisabled = false;
    private boolean autoDark;
    private boolean forceDark;


    public Color(Context context)  {
            this.context = context;
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
            preferences.registerOnSharedPreferenceChangeListener(this);
            loadFromSettings();
    }

    private boolean checkDarkMode(){
        int nightModeFlags =
                context.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        return !(nightModeFlags == Configuration.UI_MODE_NIGHT_NO);
    }
    private static float asFloat(int paramInt) {
        return paramInt / 255.0F;
    }

    public static long defineColor(int primaryColor, int accentColor) {
        return (long) primaryColor << 32L | accentColor & 0xFFFFFFFFL;
    }
    public static float[][] extract(int upper, int lower) {
        return new float[][] { rgbIntToFloatArray(upper), rgbIntToFloatArray(lower) };
    }
    public static float[][] extract(long paramLong) {
        return new float[][] { rgbIntToFloatArray((int)(paramLong >> 32L & 0xFFFFFFFFL)), rgbIntToFloatArray((int)(paramLong & 0xFFFFFFFFL)) };
    }
    private static float[] rgbIntToFloatArray(int paramInt) {
        return new float[] { asFloat(paramInt >> 16 & 0xFF), asFloat(paramInt >> 8 & 0xFF), asFloat(paramInt & 0xFF) };
    }

    private float[] fromString(String value){
        return rgbIntToFloatArray(Integer.decode("0x"+value));
    }
    private boolean validate(String s){
        if (s.length()!=6) return false;
        return s.matches("-?[0-9a-fA-F]+");
    }
    @SuppressWarnings("deprecation")
    public void loadFromSettings(){
        if (preferences.getBoolean("customColors",false)) {
            String primaryColor = preferences.getString("primaryColor", "004161");
            String accentColor = preferences.getString("accentColor", "004161");
            if (validate(primaryColor)&&validate(accentColor)) currentColor = new float[][] {fromString(primaryColor),fromString(accentColor)};
        } else {
            loadPresetColor();
        }
        if (preferences.getBoolean("autoDark",true)){
            isDarkMode = checkDarkMode();
        } else {
            isDarkMode = preferences.getBoolean("alwaysDark",true);
        }

       if (preferences.getBoolean("autoDark",true)){
           isDarkMode = checkDarkMode();
       } else {
           isDarkMode = preferences.getBoolean("alwaysDark",true);
       }

    }

    private void loadPresetColor(){
        String switcher = preferences.getString("waveStyle","1");
        switch (switcher ){
            case "Emerald":
                currentColor = extract(ColorInfo.Emerald.getColor());
            break;
            case "Sapphire":
                currentColor = extract(ColorInfo.Sapphire.getColor());
                break;
            case "Gold":
                currentColor = extract(ColorInfo.Gold.getColor());
                break;
            case "Ruby":
                currentColor = extract(ColorInfo.Ruby.getColor());
                break;
            case "Amethyst":
                currentColor = extract(ColorInfo.Amethyst.getColor());
                break;
            case "Amber":
                currentColor = extract(ColorInfo.Amber.getColor());
                break;
            //case "Monet":
            default:
                int primaryColor, accentColor;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    primaryColor = context.getColor(R.color.primary); //For light theme we need to use different colors
                    accentColor =  context.getColor(R.color.accent);
                } else {
                    primaryColor = context.getResources().getColor(R.color.primary);
                    accentColor = context.getResources().getColor(R.color.accent);
                }
                currentColor = extract(primaryColor, accentColor);
                break;
        }
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        loadFromSettings();
    }
}
