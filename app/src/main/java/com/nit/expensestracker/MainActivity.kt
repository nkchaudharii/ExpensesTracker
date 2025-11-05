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
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database helper
        dbHelper = DBHelper(this)

        // Initialize IdGenerator with max IDs from database
        IdGenerator.initializeFromDatabase(dbHelper)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ExpensesTrackerApp(dbHelper)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Database operations will be handled in composables through state
    }

    override fun onStop() {
        super.onStop()
        // Data is saved immediately when changes occur, no need to save here
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}

@Composable
fun ExpensesTrackerApp(dbHelper: DBHelper) {
    val context = LocalContext.current

    // Load sheets from database
    var allSheets by remember { mutableStateOf(dbHelper.getAllSheets()) }
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.List) }

    // Function to refresh sheets from database
    fun refreshSheets() {
        allSheets = dbHelper.getAllSheets()
    }

    when (val screen = currentScreen) {
        is AppScreen.List -> {
            SheetListScreen(
                sheets = allSheets,
                onSheetClick = { clickedSheet ->
                    val intent = Intent(context, MonthActivity::class.java).apply {
                        putExtra("SHEET_ID", clickedSheet.id)
                        putExtra("MONTH", clickedSheet.month)
                        putExtra("YEAR", clickedSheet.year)
                        putExtra("INCOME", clickedSheet.income)
                    }
                    context.startActivity(intent)
                },
                onCreateClick = {
                    currentScreen = AppScreen.Create
                },
                onChartClick = {
                    val sortedSheets = allSheets.sortedWith(
                        compareBy<ExpenseSheet> { it.year }.thenBy { it.month }
                    ).takeLast(4)

                    if (sortedSheets.isNotEmpty()) {
                        val months = sortedSheets.map { "${it.getMonthName().take(3)} ${it.year}" }.toTypedArray()
                        val incomeData = sortedSheets.map { it.income }.toDoubleArray()
                        val expenseData = sortedSheets.map { it.getTotalAmount() }.toDoubleArray()

                        val intent = Intent(context, ChartActivity::class.java).apply {
                            putExtra("MONTHS", months)
                            putExtra("INCOME_DATA", incomeData)
                            putExtra("EXPENSE_DATA", expenseData)
                        }
                        context.startActivity(intent)
                    }
                }
            )
        }

        is AppScreen.Create -> {
            CreateSheetScreen(
                onSheetCreated = { newSheet ->
                    // Insert sheet into database
                    val result = dbHelper.insertSheet(newSheet)
                    if (result > 0) {
                        // Refresh sheets from database
                        refreshSheets()
                        currentScreen = AppScreen.List
                    }
                },
                onBackClick = {
                    currentScreen = AppScreen.List
                }
            )
        }

        is AppScreen.Detail -> {
            MonthDetailScreen(
                sheet = screen.sheet,
                onBackClick = {
                    // Refresh sheets when going back
                    refreshSheets()
                    currentScreen = AppScreen.List
                },
                onIncomeUpdated = { newIncome ->
                    // Update income in database
                    dbHelper.updateSheetIncome(screen.sheet.id, newIncome)

                    // Refresh sheets from database
                    refreshSheets()

                    // Update current screen with refreshed sheet
                    val updatedSheet = allSheets.find { it.id == screen.sheet.id }
                    if (updatedSheet != null) {
                        currentScreen = AppScreen.Detail(updatedSheet)
                    }
                },
                onExpenseAdded = { description, amount, date ->
                    val newExpense = Expense(
                        id = IdGenerator.generateId(),
                        description = description,
                        amount = amount,
                        date = date
                    )

                    // Insert expense into database
                    dbHelper.insertExpense(screen.sheet.id, newExpense)

                    // Refresh sheets from database
                    refreshSheets()

                    // Update current screen with refreshed sheet
                    val updatedSheet = allSheets.find { it.id == screen.sheet.id }
                    if (updatedSheet != null) {
                        currentScreen = AppScreen.Detail(updatedSheet)
                    }
                }
            )
        }
    }
}

sealed class AppScreen {
    object List : AppScreen()
    object Create : AppScreen()
    data class Detail(val sheet: ExpenseSheet) : AppScreen()
}