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
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database helper
        dbHelper = DBHelper(this)

        // Get month data passed from MainActivity via Intent
        val sheetId = intent.getIntExtra("SHEET_ID", -1)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (sheetId != -1) {
                        MonthActivityContent(
                            sheetId = sheetId,
                            dbHelper = dbHelper,
                            onBackPressed = { finish() }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}

// Content composable for MonthActivity
@Composable
fun MonthActivityContent(
    sheetId: Int,
    dbHelper: DBHelper,
    onBackPressed: () -> Unit
) {
    // Load sheet from database
    var currentSheet by remember { mutableStateOf(dbHelper.getSheetById(sheetId)) }

    // Function to refresh sheet from database
    fun refreshSheet() {
        currentSheet = dbHelper.getSheetById(sheetId)
    }

    // Only display if sheet exists
    currentSheet?.let { sheet ->
        MonthDetailScreen(
            sheet = sheet,
            onBackClick = onBackPressed,
            onIncomeUpdated = { newIncome ->
                // Update income in database
                dbHelper.updateSheetIncome(sheetId, newIncome)

                // Refresh sheet from database
                refreshSheet()
            },
            onExpenseAdded = { description, amount, date ->
                // Create new expense
                val newExpense = Expense(
                    id = IdGenerator.generateId(),
                    description = description,
                    amount = amount,
                    date = date
                )

                // Add expense to database
                dbHelper.insertExpense(sheetId, newExpense)

                // Refresh sheet from database
                refreshSheet()
            },
            // Task 14: Add expense update handler
            onExpenseUpdated = { updatedExpense ->
                // Update expense in database
                dbHelper.updateExpense(updatedExpense)

                // Refresh sheet from database
                refreshSheet()
            },
            // Task 14: Add expense delete handler
            onExpenseDeleted = { expenseToDelete ->
                // Delete expense from database
                dbHelper.deleteExpense(expenseToDelete.id)

                // Refresh sheet from database
                refreshSheet()
            }
        )
    }
}