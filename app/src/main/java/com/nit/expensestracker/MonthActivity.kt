package com.nit.expensestracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

class MonthActivity : ComponentActivity() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbHelper = DBHelper(this)

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

@Composable
fun MonthActivityContent(
    sheetId: Int,
    dbHelper: DBHelper,
    onBackPressed: () -> Unit
) {
    var currentSheet by remember { mutableStateOf(dbHelper.getSheetById(sheetId)) }

    fun refreshSheet() {
        currentSheet = dbHelper.getSheetById(sheetId)
    }

    currentSheet?.let { sheet ->

        MonthDetailScreen(
            sheet = sheet,
            onBackClick = onBackPressed,
            onIncomeUpdated = { newIncome ->
                dbHelper.updateSheetIncome(sheetId, newIncome)
                refreshSheet()
            },
            onExpenseAdded = { description, amount, date ->
                val newExpense = Expense(
                    id = IdGenerator.generateId(),
                    description = description,
                    amount = amount,
                    date = date
                )
                dbHelper.insertExpense(sheetId, newExpense)
                refreshSheet()
            },
            onExpenseUpdated = { updatedExpense ->
                dbHelper.updateExpense(updatedExpense)
                refreshSheet()
            },
            onExpenseDeleted = { deletedExpense ->
                dbHelper.deleteExpense(deletedExpense.id)
                refreshSheet()
            }
        )
    }
}
