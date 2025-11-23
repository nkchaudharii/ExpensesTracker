package com.nit.expensestracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier


//This is my Data Models
// This part outlines the schema of a single expense record and the cumulative expense record which is depicted on a monthly basis.
data class Expense(
    val id: Int,
    val amount: Double,
    val date: String,
    val description: String,
)


data class ExpenseSheet(
    val id: Int,
    val month: Int,
    val year: Int,
    val income: Double = 0.0,
    val expenses: List<Expense> = emptyList()
)
{
    fun getMonthName(): String = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )[month - 1]

    fun getTotalAmount(): Double = expenses.sumOf { it.amount }
}

// Id Generator
// This element ensures that sheets and expenses are uniquely identified by reference to the maximum identifiers that are already available in the database.
object IdGenerator {
    private var unique_Id = 0

    fun initializeFromDatabase(dbHelper: DBHelper) {
        val maxSheet_Id = dbHelper.getMaxSheetId()
        val maxExpense_Id = dbHelper.getMaxExpenseId()
        unique_Id = maxOf(maxSheet_Id, maxExpense_Id)
    }

    fun generateId(): Int {
        unique_Id += 1
        return unique_Id
    }
}

// App Screen
// This is a sealed type of enumeration that gives out the navigation destinations of the application.
sealed class AppScreen {
    object List : AppScreen()
    object Create : AppScreen()
    data class Edit(val sheet: ExpenseSheet) : AppScreen()
    data class Detail(val sheet: ExpenseSheet) : AppScreen()
}


// MainActivity apps entry point
// The main module is a coordinator of the database setup and unique identifiers configuration, as well as the presentation of the Compose UI.
class MainActivity : ComponentActivity() {

    private lateinit var myydb: DBHelper

    companion object {
        var reffCallbackk: (() -> Unit)? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        myydb = DBHelper(this)
        IdGenerator.initializeFromDatabase(myydb)

        setContent {
            ExpensesTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ExpensesMainUi(myydb)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        reffCallbackk = null
        myydb.close()
    }
}