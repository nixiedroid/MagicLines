package com.nixiedroid.magiclines.color;

import android.content.SharedPreferences;
import com.nixiedroid.magiclines.R;

public enum ColorInfo {
  Emerald ("Emerald", Color.defineColor(0x146532,0xd6ffb1)),
  Sapphire ("Sapphire", Color.defineColor(0x2559b3, 0xa1f7ff)),
  Gold ("Gold", Color.defineColor(0xa57f24, 0xa2ffb3)),
  Ruby("Ruby", Color.defineColor(0x740f30, 0xffacdd)),
  Amethyst("Amethyst", Color.defineColor(0x760687, 0xffffe4)),
  Amber("Amber", Color.defineColor(0xf08f34, 0xfdc5c5));

  private final long color;

  private final String name;

    public long getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    ColorInfo(String name, long paramLong) {
    this.name = name;
    this.color = paramLong;
  }

}


