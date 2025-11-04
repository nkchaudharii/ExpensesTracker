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
    val context = LocalContext.current
    var allSheets by remember { mutableStateOf<List<ExpenseSheet>>(emptyList()) }
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.List) }

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
                    allSheets = allSheets + newSheet
                    currentScreen = AppScreen.List
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
                    currentScreen = AppScreen.List
                },
                onIncomeUpdated = { newIncome ->
                    allSheets = allSheets.map { existingSheet ->
                        if (existingSheet.id == screen.sheet.id) {
                            existingSheet.copy(income = newIncome)
                        } else {
                            existingSheet
                        }
                    }

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

                    allSheets = allSheets.map { existingSheet ->
                        if (existingSheet.id == screen.sheet.id) {
                            existingSheet.copy(
                                expenses = existingSheet.expenses + newExpense
                            )
                        } else {
                            existingSheet
                        }
                    }

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