package com.nit.expensestracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

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
    // Get context for starting new activities
    val context = LocalContext.current

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
                    // Task 10: Launch separate MonthActivity for each month
                    val intent = Intent(context, MonthActivity::class.java).apply {
                        putExtra("SHEET_ID", clickedSheet.id)
                        putExtra("MONTH", clickedSheet.month)
                        putExtra("YEAR", clickedSheet.year)
                        putExtra("INCOME", clickedSheet.income)
                    }
                    context.startActivity(intent)
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

        // Detail screen kept for backward compatibility
        // Task 10: MonthActivity now handles detail view
        is AppScreen.Detail -> {
            MonthDetailScreen(
                sheet = screen.sheet,
                onBackClick = {
                    // Go back to list screen
                    currentScreen = AppScreen.List
                },
                onIncomeUpdated = { newIncome ->
                    // Update the income for this sheet
                    allSheets = allSheets.map { existingSheet ->
                        if (existingSheet.id == screen.sheet.id) {
                            // Update this sheet with new income value
                            existingSheet.copy(income = newIncome)
                        } else {
                            // Keep other sheets unchanged
                            existingSheet
                        }
                    }

                    // Update current screen with the modified sheet
                    val updatedSheet = allSheets.find { it.id == screen.sheet.id }
                    if (updatedSheet != null) {
                        currentScreen = AppScreen.Detail(updatedSheet)
                    }
                },
                onExpenseAdded = { description, amount, date ->
                    // Create new expense with unique ID
                    val newExpense = Expense(
                        id = IdGenerator.generateId(),
                        description = description,
                        amount = amount,
                        date = date
                    )

                    // Update the sheet with the new expense
                    allSheets = allSheets.map { existingSheet ->
                        if (existingSheet.id == screen.sheet.id) {
                            // Add expense to this sheet
                            existingSheet.copy(
                                expenses = existingSheet.expenses + newExpense
                            )
                        } else {
                            // Keep other sheets unchanged
                            existingSheet
                        }
                    }

                    // Update current screen with the modified sheet
                    val updatedSheet = allSheets.find { it.id == screen.sheet.id }
                    if (updatedSheet != null) {
                        currentScreen = AppScreen.Detail(updatedSheet)
                    }
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