package com.nit.expensestracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

// Task 10: Separate activity for individual month management
class MonthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get month data passed from MainActivity via Intent
        val sheetId = intent.getIntExtra("SHEET_ID", -1)
        val month = intent.getIntExtra("MONTH", 1)
        val year = intent.getIntExtra("YEAR", 2025)
        val income = intent.getDoubleExtra("INCOME", 0.0)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MonthActivityContent(
                        initialSheet = ExpenseSheet(
                            id = sheetId,
                            month = month,
                            year = year,
                            income = income,
                            expenses = emptyList()
                        ),
                        onBackPressed = { finish() }
                    )
                }
            }
        }
    }
}

// Content composable for MonthActivity
@Composable
fun MonthActivityContent(
    initialSheet: ExpenseSheet,
    onBackPressed: () -> Unit
) {
    // State to hold current sheet data for this activity instance
    var currentSheet by remember { mutableStateOf(initialSheet) }

    MonthDetailScreen(
        sheet = currentSheet,
        onBackClick = onBackPressed,
        onIncomeUpdated = { newIncome ->
            // Update income in this activity instance
            currentSheet = currentSheet.copy(income = newIncome)
        },
        onExpenseAdded = { description, amount, date ->
            // Create new expense
            val newExpense = Expense(
                id = IdGenerator.generateId(),
                description = description,
                amount = amount,
                date = date
            )

            // Add expense to current sheet in this activity instance
            currentSheet = currentSheet.copy(
                expenses = currentSheet.expenses + newExpense
            )
        }
    )
}