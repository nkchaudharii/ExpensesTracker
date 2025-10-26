package com.nit.expensestracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ExpensesTrackerApp()
                }
            }
        }
    }
}

@Composable
fun ExpensesTrackerApp() {
    // Step 17: Shared state for all sheets
    var sheetsList by remember { mutableStateOf(listOf<ExpenseSheet>()) }
    var showCreateScreen by remember { mutableStateOf(false) }

    if (showCreateScreen) {
        // Show create screen
        CreateSheetScreen(
            onSheetCreated = { newSheet ->
                sheetsList = sheetsList + newSheet
                showCreateScreen = false
            }
        )
    } else {
        // Show list screen with FAB
        Box(modifier = Modifier.fillMaxSize()) {
            SheetListScreen(
                sheets = sheetsList,
                onSheetClick = { sheet ->
                    // Will be implemented in Task 4
                    // For now just logs to console
                }
            )

            // Floating action button to create new sheet
            FloatingActionButton(
                onClick = { showCreateScreen = true },
                modifier = Modifier
                    .padding(16.dp)
                    .align(androidx.compose.ui.Alignment.BottomEnd)
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}
