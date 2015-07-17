package com.antama.abc2015s.omikuji;

import android.view.View;

public class TensionMeter {
    private View mTarget;
    private boolean mTripped;
    private float mTension;

    public TensionMeter(final View target) {
        mTarget = target;
        reset();

        mTarget.setOnClickListener(new TapAction());
    }

    public void setTension(final float t) {
        mTension = t;
        update();
    }

    private void update() {

    }

    public void reset() {
        mTension = 0.0f;
        mTripped = false;
    }

    public void trip() {
        mTension = 1.0f;
        mTripped = true;
    }

    public boolean isTripped() {
        return mTripped;
    }

    private class TapAction implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            if (mTripped) {
                reset();
            }
        }
    };
}
