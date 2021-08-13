package me.laotang.router.route;

import androidx.annotation.Nullable;

import me.laotang.router.RouteInfo;

public interface RouteProvider {
    Object adapt(RouteInfo routeInfo);

    abstract class Factory {
        public abstract @Nullable
        RouteProvider get(String routePath);
    }
}
