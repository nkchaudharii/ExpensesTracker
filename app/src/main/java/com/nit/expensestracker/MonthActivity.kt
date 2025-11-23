package com.nit.expensestracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

// Month Activity
// This is used to give a complete listing of the financial information related to a user-selected ledger of expenditure per month.
class MonthActivity : ComponentActivity() {
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
                                MainActivity.reffCallbackk?.invoke()
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