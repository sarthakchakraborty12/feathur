package com.platnumm.openevo.feathur.doc

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.platnumm.openevo.feathur.doc.data.DocumentHistoryRepository
import com.platnumm.openevo.feathur.doc.data.FeathurDatabase
import com.platnumm.openevo.feathur.doc.ui.FeathurApp
import com.platnumm.openevo.feathur.doc.ui.theme.MyApplicationTheme
import com.platnumm.openevo.feathur.doc.viewmodel.FeathurViewModel
import com.platnumm.openevo.feathur.doc.viewmodel.FeathurViewModelFactory

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {

    private val database by lazy { FeathurDatabase.getDatabase(applicationContext) }
    private val repository by lazy { DocumentHistoryRepository(database.historyDao()) }
    
    // Modern simple DI via ViewModelProvider Factory
    private val viewModel: FeathurViewModel by viewModels {
        FeathurViewModelFactory(repository, applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle incoming system file picker intents
        handleIntent(intent)

        setContent {
            val darkModeSetting by viewModel.darkModeSetting.collectAsState()
            val themeSetting by viewModel.themeSetting.collectAsState()
            MyApplicationTheme(
                darkModeSetting = darkModeSetting,
                themeSetting = themeSetting
            ) {
                FeathurApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val type = intent.type
        val data: Uri? = intent.data

        Log.d("MainActivity", "Received intent action: $action, type: $type, data: $data")

        if (Intent.ACTION_VIEW == action && data != null) {
            viewModel.openDocument(this, data)
        }
    }
}
