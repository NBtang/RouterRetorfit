package me.laotang.router.impl

import android.content.Intent
import me.laotang.router.Call
import me.laotang.router.RouteInfo
import me.laotang.router.RouterRetrofit
import me.laotang.router.SuspendCall
import me.laotang.router.route.RouteProvider
import java.lang.Exception
import java.lang.reflect.Type
import kotlin.coroutines.resumeWithException

class SuspendCallAdapter(private val responseType: Type, retrofit: RouterRetrofit) :
    NavigationCallAdapter(retrofit) {

    override fun responseType(): Type {
        return responseType
    }

    override fun adapt(call: Call<RouteInfo>): Any {
        return SuspendCall<Any> { continuation ->
            val routeInfo = call.execute()
            val routeProvider: RouteProvider = retrofit.routeProvider(routeInfo.relativeUrl)
            val intent = routeProvider.adapt(routeInfo)
            if (intent != null && intent is Intent) {
                try {
                    navigation(routeInfo, intent)
                    continuation.resumeWith(Result.success(Unit))
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            } else {
                continuation.resumeWithException(Throwable("not found intent by routePath:${routeInfo.relativeUrl}"))
            }
        }
    }
}