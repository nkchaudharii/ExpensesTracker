package com.nit.expensestracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbHelper = DBHelper(this)
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

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}

@Composable
fun ExpensesTrackerApp(dbHelper: DBHelper) {
    val context = LocalContext.current

    var allSheets by remember { mutableStateOf(dbHelper.getAllSheets()) }
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.List) }

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
                    }
                    context.startActivity(intent)
                },
                onCreateClick = {
                    currentScreen = AppScreen.Create
                },
                onChartClick = {
                    context.startActivity(Intent(context, ChartActivity::class.java))
                }
            )
        }

        is AppScreen.Create -> {
            CreateSheetScreen(
                onSheetCreated = { newSheet ->
                    val result = dbHelper.insertSheet(newSheet)
                    if (result > 0) {
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
            // ✅ FIX: use MonthActivityContent instead of MonthDetailScreen
            MonthActivityContent(
                sheetId = screen.sheet.id,
                dbHelper = dbHelper,
                onBackPressed = {
                    refreshSheets()
                    currentScreen = AppScreen.List
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
