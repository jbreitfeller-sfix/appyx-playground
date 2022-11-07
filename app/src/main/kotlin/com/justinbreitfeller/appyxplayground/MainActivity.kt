package com.justinbreitfeller.appyxplayground

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.integration.NodeHost
import com.bumble.appyx.core.integrationpoint.ActivityIntegrationPoint
import com.bumble.appyx.core.integrationpoint.IntegrationPointProvider
import com.justinbreitfeller.appyxplayground.ui.theme.AppyxPlaygroundTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity(), IntegrationPointProvider {
    override lateinit var appyxIntegrationPoint: ActivityIntegrationPoint
        private set

    private fun createIntegrationPoint(savedInstanceState: Bundle?) =
        ActivityIntegrationPoint(
            activity = this,
            savedInstanceState = savedInstanceState
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appyxIntegrationPoint = createIntegrationPoint(savedInstanceState = savedInstanceState)
        setContent {
            AppyxPlaygroundTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    TabbedNavigationApp(appyxIntegrationPoint)
                }
            }
        }
    }

    @Composable
    fun TabbedNavigationApp(appyxIntegrationPoint: ActivityIntegrationPoint) {
        NodeHost(integrationPoint = appyxIntegrationPoint) {
            TabbedNavigationNode(
                buildContext = it,
                oneNodePerTab = true,
                application = applicationContext as Application?
            )
        }
    }
}
