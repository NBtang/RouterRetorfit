package me.laotang.router.route;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import me.laotang.router.RouteInfo;

public class FragmentProvider implements RouteProvider {

    private final Class<?> clazz;

    public FragmentProvider(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object adapt(RouteInfo routeInfo) {
        Fragment fragment;
        try {
            fragment = (Fragment) clazz.newInstance();
            Bundle bundle = routeInfo.getBundle();
            if (bundle != null) {
                fragment.setArguments(bundle);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fragment = null;
        }
        return fragment;
    }
}
