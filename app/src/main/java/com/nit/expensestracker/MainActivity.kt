package com.nit.expensestracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Using Surface container for the app
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
    // State variable to hold all expense sheets
    var allSheets by remember { mutableStateOf<List<ExpenseSheet>>(emptyList()) }

    // State variable to track which screen to show
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.List) }

    // Decide which screen to display based on current state
    when (val screen = currentScreen) {
        // Show list of all sheets
        is AppScreen.List -> {
            SheetListScreen(
                sheets = allSheets,
                onSheetClick = { clickedSheet ->
                    // Navigate to detail screen when sheet is clicked
                    currentScreen = AppScreen.Detail(clickedSheet)
                },
                onCreateClick = {
                    // Navigate to create screen when FAB is clicked
                    currentScreen = AppScreen.Create
                }
            )
        }

        // Show form to create new sheet
        is AppScreen.Create -> {
            CreateSheetScreen(
                onSheetCreated = { newSheet ->
                    // Add new sheet to the list
                    allSheets = allSheets + newSheet
                    // Go back to list screen
                    currentScreen = AppScreen.List
                },
                onBackClick = {
                    // Cancel and go back to list screen
                    currentScreen = AppScreen.List
                }
            )
        }

        // Show details of a specific sheet
        is AppScreen.Detail -> {
            MonthDetailScreen(
                sheet = screen.sheet,
                onBackClick = {
                    // Go back to list screen
                    currentScreen = AppScreen.List
                }
            )
        }
    }
}

// Sealed class to represent different screens in the app
// This helps with type-safe navigation
sealed class AppScreen {
    object List : AppScreen()  // List of all sheets
    object Create : AppScreen()  // Create new sheet form
    data class Detail(val sheet: ExpenseSheet) : AppScreen()  // Details of one sheet
}