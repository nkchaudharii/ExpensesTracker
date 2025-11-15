package com.nit.expensestracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.ui.platform.LocalContext


// =============****================*** MAIN ACTIVITY ***===============****========================

// The app's entry point, which manages screen navigation and initial setup
class MainActivity : ComponentActivity() {

    // Database helper instance to manage data operations
    private lateinit var myydb: DBHelper

    companion object {
        // ✅ Static variable to trigger refresh from outside
        var refCallbackk: (() -> Unit)? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up the ID generator and database.
        myydb = DBHelper(this)
        IdGenerator.initializeFromDatabase(myydb)

        // Set up the UI using Jetpack Compose
        setContent {
            ExpensesTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Launch main app composable
                    ExpensesMainUi(myydb)
                }
            }
        }
    }

    // When the activity is terminated, terminate the database connection.
    override fun onDestroy() {
        super.onDestroy()
        refCallbackk = null // ✅ Clean up callback
        myydb.close()
    }
}


// ==============****=============*** MAIN UI & NAVIGATION ***=============****=====================
@Composable
// The root composable function that manages screen state and navigation
fun ExpensesMainUi(myydb: DBHelper) {
    val context = LocalContext.current

    // Open the database and load every expense sheet that is available.
    var rec_Sheet by remember { mutableStateOf(myydb.getAllSheets()) }

    // ✅ Trigger for forcing recomposition
    var signal_Update by remember { mutableStateOf(0) }

    // Keep track of which screen the user is currently on
    var active_Screen by remember { mutableStateOf<AppScreen>(AppScreen.List) }

    // After updates, manually reload the list of sheets.
    fun ref_Sheets() {
        rec_Sheet = myydb.getAllSheets()
        signal_Update++ // ✅ Force UI update
    }

    // ✅ Set up the callback so MonthActivity can trigger refresh
    LaunchedEffect(Unit) {
        MainActivity.refCallbackk = {
            ref_Sheets()
        }
    }

    // ✅ Watch for refresh trigger changes
    LaunchedEffect(signal_Update) {
        rec_Sheet = myydb.getAllSheets()
    }

    // Apply animation effects to transitions between screens to make navigation smoother
    AnimatedContent(
        targetState = active_Screen,
        label = "screenFlowAnimation") { screen ->
        when (screen) {

            // Show all the different expenses sheets.
            is AppScreen.List -> {
                SheetListScreen(
                    sheets = rec_Sheet,
                    onSheetClick = { selected ->
                        // When a sheet is selected, display the month's activity
                        val jumpIntent = Intent(context, MonthActivity::class.java).apply {
                            putExtra("SHEET_ID", selected.id)
                        }
                        context.startActivity(jumpIntent)
                    },
                    // Click 'Add New' to navigate to the Create screen.
                    onCreateClick = {
                        active_Screen = AppScreen.Create
                    },
                    // To see a visual summary, navigate to the chart activity.
                    onChartClick = {
                        context.startActivity(Intent(context, ChartActivity::class.java))
                    },
                    // Remove the chosen sheet and reload the list.
                    onSheetDeleted = { sheet ->
                        myydb.deleteSheet(sheet.id)
                        ref_Sheets()
                    },
                    // Open the chosen sheet's Edit screen.
                    onSheetEdited = { sheet ->
                        active_Screen = AppScreen.Edit(sheet)
                    }
                )
            }

            // The screen where a new expense sheet is created
            is AppScreen.Create -> {
                CreateSheetScreen(
                    existingSheets = rec_Sheet,
                    onSheetCreated = { newSheet ->
                        // Check if a sheet with the same month and year already exists
                        val duplicate = rec_Sheet.any {
                            it.month == newSheet.month && it.year == newSheet.year
                        }

                        if (duplicate) {
                            // Show error - handled in CreateSheetScreen
                            // Error message will be displayed to user
                        } else {
                            val insertResult = myydb.insertSheet(newSheet)
                            if (insertResult > 0) {
                                ref_Sheets()
                                active_Screen = AppScreen.List
                            }
                        }
                    },
                    onBackClick = {
                        active_Screen = AppScreen.List
                    }
                )
            }

            // Editing an existing expense sheet screen
            is AppScreen.Edit -> {
                EditSheetScreen(
                    sheet = screen.sheet,
                    existingSheets = rec_Sheet,
                    onSheetUpdated = { updated ->
                        // Check if updating would create a duplicate (excluding the current sheet)
                        val duplicate = rec_Sheet.any {
                            it.id != updated.id &&
                                    it.month == updated.month &&
                                    it.year == updated.year
                        }

                        if (!duplicate) {
                            myydb.updateSheet(updated)
                            ref_Sheets()
                            active_Screen = AppScreen.List
                        }
                        // Error message will be displayed in EditSheetScreen if duplicate
                    },
                    onBackClick = {
                        active_Screen = AppScreen.List
                    }
                )
            }

            // The screen where monthly spending information is displayed
            is AppScreen.Detail -> {
                MonthActivityContent(
                    sheetId = screen.sheet.id,
                    dbHelper = myydb,
                    onBackPressed = {
                        ref_Sheets()
                        active_Screen = AppScreen.List
                    }
                )
            }
        }
    }
}

/* ==============****===============*** SCREEN STATES ***================****======================= */

// Indicates various app states and screens.
sealed class AppScreen {
    object List : AppScreen() // The home screen with all of the sheets
    object Create : AppScreen() // Create a new sheet using the screen
    data class Edit(val sheet: ExpenseSheet) : AppScreen() // Screen to modify an already-existing sheet
    data class Detail(val sheet: ExpenseSheet) : AppScreen() // Monthly view in detail
}