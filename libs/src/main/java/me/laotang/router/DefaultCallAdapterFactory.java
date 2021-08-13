package me.laotang.router;

import androidx.fragment.app.Fragment;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import kotlinx.coroutines.flow.Flow;
import me.laotang.router.impl.NavigationCallAdapter;
import me.laotang.router.impl.FlowActivityResultCallAdapter;
import me.laotang.router.impl.SuspendActivityResultCallAdapter;
import me.laotang.router.impl.SuspendCallAdapter;
import me.laotang.router.result.ActivityResult;
import me.laotang.router.route.RouteProvider;

final class DefaultCallAdapterFactory extends CallAdapter.Factory {
    static final CallAdapter.Factory INSTANCE = new DefaultCallAdapterFactory();

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, RouterRetrofit retrofit) {
        Class<?> rawType = getRawType(returnType);
        if (rawType == Call.class) {
            final Type responseType = Utils.getCallResponseType(returnType);
            if (responseType == RouteInfo.class) {
                return new CallAdapter<RouteInfo, Call<RouteInfo>>() {
                    @Override
                    public Type responseType() {
                        return responseType;
                    }

                    @Override
                    public Call<RouteInfo> adapt(Call<RouteInfo> call) {
                        return call;
                    }
                };
            }
        }
        if (rawType == void.class || rawType == Void.class || rawType == kotlin.Unit.class) {
            return new NavigationCallAdapter(retrofit);
        }
        if (rawType == SuspendCall.class) {
            final Type responseType = Utils.getCallResponseType(returnType);
            if (responseType == void.class || responseType == Void.class || responseType == kotlin.Unit.class) {
                return new SuspendCallAdapter(responseType, retrofit);
            }
            if (responseType == ActivityResult.class) {
                return new SuspendActivityResultCallAdapter(retrofit);
            }
        }
        if (rawType == Flow.class) {
            final Type responseType = Utils.getCallResponseType(returnType);
            if (responseType == ActivityResult.class) {
                return new FlowActivityResultCallAdapter(retrofit);
            }
        }
        if (rawType == Fragment.class) {
            return new CallAdapter<RouteInfo, Fragment>() {
                @Override
                public Type responseType() {
                    return Fragment.class;
                }

                @Override
                public Fragment adapt(Call<RouteInfo> call) {
                    RouteInfo routeInfo = call.execute();
                    RouteProvider routeProvider = retrofit.routeProvider(routeInfo.getRelativeUrl());
                    if (routeProvider != null) {
                        Object fragment = routeProvider.adapt(routeInfo);
                        if (fragment instanceof Fragment) {
                            return (Fragment) fragment;
                        }
                    }
                    return null;
                }
            };
        }
        return null;
    }
}
