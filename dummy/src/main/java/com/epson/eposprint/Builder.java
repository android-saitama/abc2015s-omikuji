package com.epson.eposprint;

public class Builder {
    public static final int MODEL_JAPANESE = 1;
    public static final int LANG_JA = 1;
    public static final int TRUE = 1;
    public static final int FONT_A = 1;
    public static final int CUT_FEED = 1;

    public Builder(final String name, final int model) {
    }

    public void addTextLang(final int lang) {
    }
    public void addTextSmooth(final int val) {
    }
    public void addTextFont(final int val) {
    }
    public void addTextSize(final int x, final int y) {
    }
    public void addText(final String text) {
    }
    public void addCut(final int mode) {
    }

    public void clearCommandBuffer() {
    }
}
