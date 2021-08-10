package me.laotang.router

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment

class MainFragment : Fragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val routerRetrofit = RouterRetrofit.Builder()
            .build(requireContext())
        val routerService = routerRetrofit.create(RouterService::class.java)
        routerService.startTestUri(requireContext(),"app://test.uri.activity",22,Intent.ACTION_VIEW)
//        routerService.startTestUri(this, "app://test.uri.activity/fragment",22,Intent.ACTION_VIEW)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}