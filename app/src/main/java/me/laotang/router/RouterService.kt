package me.laotang.router

import android.content.Context
import androidx.fragment.app.Fragment
import me.laotang.router.annotation.Action
import me.laotang.router.annotation.RequestCode
import me.laotang.router.annotation.Url

interface RouterService {
    fun startTestUri(context: Context, @Url url: String, @RequestCode requestCode:Int, @Action action:String)
    fun startTestUri(fragment: Fragment, @Url url: String, @RequestCode requestCode:Int, @Action action:String)

    suspend fun startTestUriSuspend(context: Context, @Url url: String)
}