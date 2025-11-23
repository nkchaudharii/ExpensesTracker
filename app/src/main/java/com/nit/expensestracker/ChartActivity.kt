package com.nit.expensestracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

// Chart Activity
// The entry point of the interface, creating the underlying persistence model and showing the visualisation of the chart correspondingly.
class ChartActivity : ComponentActivity() {
    private lateinit var db_Access: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db_Access = DBHelper(this)

        setContent {
            ExpensesTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChartScreenWithSwipe(
                        dbHelper = db_Access,
                        onBackPressed = { finish() }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        db_Access.close()
    }
}