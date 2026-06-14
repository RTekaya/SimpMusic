package com.maxrave.simpmusic.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.maxrave.simpmusic.wear.ui.WearApp

class WearMainActivity : ComponentActivity() {

    private val viewModel: WearMediaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp(viewModel = viewModel)
        }
    }
}
