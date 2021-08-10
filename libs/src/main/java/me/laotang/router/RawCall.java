package me.laotang.router;

public interface RawCall {
    RouteInfo execute();
    interface Factory {
        RawCall newCall(RouteRequest routeRequest);
    }
}
