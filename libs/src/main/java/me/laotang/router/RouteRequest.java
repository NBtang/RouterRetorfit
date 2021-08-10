package me.laotang.router;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;

public class RouteRequest {
    final String relativeUrl;
    final int mFlags;
    final boolean isGreenChannel;
    final Bundle mBundle;
    final WeakReference<Context> contextWeakReference;
    final WeakReference<Fragment> fragmentWeakReference;
    final boolean fromFragment;
    final int requestCode;
    final String action;

    RouteRequest(Builder builder, Context context) {
        this.relativeUrl = builder.relativeUrl;
        this.mFlags = builder.mFlags;
        this.isGreenChannel = builder.isGreenChannel;
        this.mBundle = builder.mBundle;
        this.contextWeakReference = new WeakReference<>(context);
        this.fragmentWeakReference = null;
        this.fromFragment = false;
        this.requestCode = builder.requestCode;
        this.action = builder.action;
    }

    RouteRequest(Builder builder, Fragment fragment) {
        this.relativeUrl = builder.relativeUrl;
        this.mFlags = builder.mFlags;
        this.isGreenChannel = builder.isGreenChannel;
        this.mBundle = builder.mBundle;
        this.contextWeakReference = null;
        this.fragmentWeakReference = new WeakReference<>(fragment);
        this.fromFragment = true;
        this.requestCode = builder.requestCode;
        this.action = builder.action;
    }

    public int getFlags() {
        return mFlags;
    }

    public String getRelativeUrl() {
        return relativeUrl;
    }

    public Bundle getExtras() {
        return mBundle;
    }

    public boolean isGreenChannel() {
        return isGreenChannel;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public String getAction() {
        return action;
    }

    public static class Builder {
        private String relativeUrl;
        private int mFlags;
        private boolean isGreenChannel;
        private Bundle mBundle;
        private int requestCode;
        private String action;

        public Builder() {
        }

        Builder(RouteRequest routeRequest) {
            this.relativeUrl = routeRequest.relativeUrl;
            this.mFlags = routeRequest.mFlags;
            this.mBundle = routeRequest.mBundle;
            this.isGreenChannel = routeRequest.isGreenChannel;
            this.requestCode = routeRequest.requestCode;
        }

        public Builder url(String url) {
            if (url == null) throw new NullPointerException("url == null");
            this.relativeUrl = url;
            return this;
        }

        public Builder flags(int flags) {
            this.mFlags = flags;
            return this;
        }

        public Builder extras(Bundle bundle) {
            this.mBundle = bundle;
            return this;
        }

        public Builder isGreenChannel(boolean isGreenChannel) {
            this.isGreenChannel = isGreenChannel;
            return this;
        }

        public Builder requestCode(int requestCode) {
            this.requestCode = requestCode;
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public RouteRequest build(Context context) {
            if (relativeUrl == null) throw new IllegalStateException("url == null");
            return new RouteRequest(this, context);
        }

        public RouteRequest build(Fragment fragment) {
            if (relativeUrl == null) throw new IllegalStateException("url == null");
            return new RouteRequest(this, fragment);
        }
    }
}
