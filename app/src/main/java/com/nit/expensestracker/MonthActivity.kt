package com.nit.expensestracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

class MonthActivity : ComponentActivity()
{
    private lateinit var myydb: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        myydb = DBHelper(this)
        val sheet_Id = intent.getIntExtra("SHEET_ID", -1)

        setContent {
            ExpensesTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (sheet_Id != -1) {
                        MonthActivityContent(
                            sheetId = sheet_Id,
                            dbHelper = myydb,
                            onBackPressed = {
                                // ✅ Trigger refresh in MainActivity before finishing
                                MainActivity.refCallbackk?.invoke()
                                finish()
                            }
                        )
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            androidx.compose.material3.Text(
                                text = "Invalid sheet selected",
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        myydb.close()
    }
}

@Composable
fun MonthActivityContent(
    sheetId: Int,
    dbHelper: DBHelper,
    onBackPressed: () -> Unit
) {
    var active_Sheet by remember { mutableStateOf(dbHelper.getSheetById(sheetId)) }

    fun updateSheet() {
        active_Sheet = dbHelper.getSheetById(sheetId)
    }

    active_Sheet?.let { sheetinfo ->
        MonthDetailScreen(
            sheet = sheetinfo,
            onBackClick = onBackPressed,
            onIncomeUpdated = { newIncomee ->
                dbHelper.updateSheetIncome(sheetId, newIncomee)
                updateSheet()
            },
            onExpenseAdded = { descr, amt, datee ->
                val newExpensee = Expense(
                    id = IdGenerator.generateId(),
                    description = descr,
                    amount = amt,
                    date = datee
                )
                dbHelper.insertExpense(sheetId, newExpensee)
                updateSheet()
            },
            onExpenseUpdated = { updatedExpensee ->
                dbHelper.updateExpense(updatedExpensee)
                updateSheet()
            },
            onExpenseDeleted = { deletedExpensee ->
                dbHelper.deleteExpense(deletedExpensee.id)
               updateSheet()
            }
        )
    }
}