package me.laotang.router.impl

import android.app.Activity
import android.content.Intent
import me.laotang.router.Call
import me.laotang.router.CallAdapter
import me.laotang.router.RouteInfo
import me.laotang.router.RouterRetrofit
import java.lang.reflect.Type

open class NavigationCallAdapter(protected val retrofit: RouterRetrofit) :
    CallAdapter<RouteInfo, Any> {

    override fun responseType(): Type {
        return Void::class.java
    }

    override fun adapt(call: Call<RouteInfo>): Any {
        val routeInfo = call.execute()
        val routeProvider = retrofit.routeProvider(routeInfo.relativeUrl)
        val intent = routeProvider.adapt(routeInfo)
        if (intent != null && intent is Intent) {
            navigation(routeInfo, intent)
            return Unit
        }
        throw RuntimeException("not found intent by routePath:" + routeInfo.relativeUrl)
    }

    protected fun navigation(routeInfo: RouteInfo, intent: Intent) {
        if (routeInfo.isFromFragment) {
            val requestCode = routeInfo.requestCode
            if (requestCode > 0) {
                routeInfo.fragment.startActivityForResult(intent, requestCode)
            } else {
                routeInfo.fragment.startActivity(intent)
            }
        } else {
            val context = routeInfo.context
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                val requestCode = routeInfo.requestCode
                if (requestCode > 0) {
                    context.startActivityForResult(intent, requestCode)
                } else {
                    context.startActivity(intent)
                }
            }
        }
    }
}