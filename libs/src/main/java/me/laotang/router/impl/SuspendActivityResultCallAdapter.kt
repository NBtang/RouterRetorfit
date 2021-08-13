package me.laotang.router.impl

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import me.laotang.router.Call
import me.laotang.router.RouteInfo
import me.laotang.router.RouterRetrofit
import me.laotang.router.SuspendCall
import me.laotang.router.result.ActivityResult
import me.laotang.router.result.ActivityResultLauncher
import me.laotang.router.route.RouteProvider
import java.lang.reflect.Type
import kotlin.coroutines.resumeWithException

class SuspendActivityResultCallAdapter(retrofit: RouterRetrofit) :
    NavigationCallAdapter(retrofit) {

    override fun responseType(): Type {
        return ActivityResult::class.java
    }

    override fun adapt(call: Call<RouteInfo>): Any {
        return SuspendCall<Any> { continuation ->
            val routeInfo = call.execute()
            val routeProvider: RouteProvider = retrofit.routeProvider(routeInfo.relativeUrl)
            val intent = routeProvider.adapt(routeInfo)
            if (intent != null && intent is Intent) {
                try {
                    val launcher = if (routeInfo.isFromFragment) {
                        ActivityResultLauncher(routeInfo.fragment)
                    } else {
                        ActivityResultLauncher(routeInfo.context as FragmentActivity)
                    }
                    launcher.startActivityForResult(intent, routeInfo.requestCode) {
                        continuation.resumeWith(Result.success(it))
                    }
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            } else {
                continuation.resumeWithException(Throwable("not found intent by routePath:${routeInfo.relativeUrl}"))
            }
        }
    }
}