package me.laotang.router;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

public class RouteInfo {
    private String relativeUrl;
    private int mFlags;
    private boolean isGreenChannel;
    private Bundle mBundle;
    private Context context;
    private Fragment fragment;
    private int requestCode;
    private String action;

    public String getRelativeUrl() {
        return relativeUrl;
    }

    public int getFlags() {
        return mFlags;
    }

    public Bundle getBundle() {
        return mBundle;
    }

    public boolean isGreenChannel() {
        return isGreenChannel;
    }

    public void setGreenChannel(boolean greenChannel) {
        isGreenChannel = greenChannel;
    }

    public Context getContext() {
        return context;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public boolean isFromFragment() {
        return fragment != null;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public String getAction() {
        return action;
    }

    public void setRelativeUrl(String relativeUrl) {
        this.relativeUrl = relativeUrl;
    }

    public void setFlags(int mFlags) {
        this.mFlags = mFlags;
    }

    public void setBundle(Bundle bundle) {
        this.mBundle = bundle;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
