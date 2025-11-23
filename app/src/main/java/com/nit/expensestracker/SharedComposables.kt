package com.nit.expensestracker

import android.content.Intent
import android.graphics.Paint
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Calendar
import kotlin.math.abs


// Main UI & Navigation
/* Provides application wide navigation within disparate screens, listings, creation,
 editing and detailed views */
@Composable
fun ExpensesMainUi(myydb: DBHelper) {
    val context = LocalContext.current

    var rec_Sheet by remember { mutableStateOf(myydb.getAllSheets()) }
    var signal_Update by remember { mutableStateOf(0) }
    var active_Screen by remember { mutableStateOf<AppScreen>(AppScreen.List) }

    fun ref_Sheets() {
        rec_Sheet = myydb.getAllSheets()
        signal_Update++
    }

    LaunchedEffect(Unit) {
        MainActivity.reffCallbackk = {
            ref_Sheets()
        }
    }

    LaunchedEffect(signal_Update) {
        rec_Sheet = myydb.getAllSheets()
    }

    AnimatedContent(
        targetState = active_Screen,
        label = "screenFlowAnimation"
    ) { screen ->
        when (screen) {
            is AppScreen.List -> {
                SheetListScreen(
                    sheets = rec_Sheet,
                    onSheetClick = { selected ->
                        val jumpIntent = Intent(context, MonthActivity::class.java).apply {
                            putExtra("SHEET_ID", selected.id)
                        }
                        context.startActivity(jumpIntent)
                    },
                    onCreateClick = {
                        active_Screen = AppScreen.Create
                    },
                    onChartClick = {
                        context.startActivity(Intent(context, ChartActivity::class.java))
                    },
                    onSheetDeleted = { sheet ->
                        myydb.deleteSheet(sheet.id)
                        ref_Sheets()
                    },
                    onSheetEdited = { sheet ->
                        active_Screen = AppScreen.Edit(sheet)
                    }
                )
            }

            is AppScreen.Create -> {
                CreateSheetScreen(
                    existingSheets = rec_Sheet,
                    onSheetCreated = { newSheet ->
                        val duplicate = rec_Sheet.any {
                            it.month == newSheet.month && it.year == newSheet.year
                        }
                        if (!duplicate) {
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

            is AppScreen.Edit -> {
                EditSheetScreen(
                    sheet = screen.sheet,
                    existingSheets = rec_Sheet,
                    onSheetUpdated = { updated ->
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
                    },
                    onBackClick = {
                        active_Screen = AppScreen.List
                    }
                )
            }

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


// Sheet List Screen
/* Displays a summary of all persisted monthly sheets and is supplemented with floating action
buttons which allow the addition of new entries or viewing of aggregate visualisations */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetListScreen(
    sheets: List<ExpenseSheet>,
    onSheetClick: (ExpenseSheet) -> Unit,
    onCreateClick: () -> Unit,
    onChartClick: () -> Unit,
    onSheetDeleted: (ExpenseSheet) -> Unit = {},
    onSheetEdited: (ExpenseSheet) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Expenses Tracker",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = onCreateClick,
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White,
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Create New Sheet",
                            tint = Color.White
                        )
                    },
                    text = {
                        Text("New Sheet", color = Color.White)
                    }
                )

                ExtendedFloatingActionButton(
                    onClick = onChartClick,
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White,
                    icon = {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "View Chart",
                            tint = Color.White
                        )
                    },
                    text = {
                        Text("Income/Expenses", color = Color.White)
                    }
                )
            }
        }
    ) { paddingValues ->
        if (sheets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No expense sheets yet",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Tap + to create new sheet")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sheets) { sheet ->
                    SheetCard(
                        sheet = sheet,
                        onClick = { onSheetClick(sheet) },
                        onDelete = { onSheetDeleted(sheet) },
                        onEdit = { onSheetEdited(sheet) }
                    )
                }
            }
        }
    }
}


// Sheet Card
/* Represents a single sheet in a card element listing by month, year, cumulative expenses
and the display of the total number of constituent elements */
@Composable
fun SheetCard(
    sheet: ExpenseSheet,
    onClick: () -> Unit,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {}
) {
    var confirm_Delete by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${sheet.getMonthName()} ${sheet.year}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    "${sheet.expenses.size} items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 12.dp)
                )

                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF4CAF50)
                    )
                }

                IconButton(onClick = { confirm_Delete = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFE53935)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Total Expenses: €%.2f".format(sheet.getTotalAmount()),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF868686)
            )
        }
    }

    if (confirm_Delete) {
        AlertDialog(
            onDismissRequest = { confirm_Delete = false },
            title = { Text("Delete Expense Sheet") },
            text = { Text("Are you sure? All expenses in this sheet will be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        confirm_Delete = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirm_Delete = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


// Create Sheet Screen
/* The creation screen has the form interface in which the user makes a
monthly sheet by choosing a month and giving the right year */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSheetScreen(
    existingSheets: List<ExpenseSheet> = emptyList(),
    onSheetCreated: (ExpenseSheet) -> Unit,
    onBackClick: () -> Unit
) {
    val calendarr = Calendar.getInstance()
    val current_Day = calendarr.get(Calendar.DAY_OF_MONTH)
    val current_Month = calendarr.get(Calendar.MONTH) + 1
    val current_Year = calendarr.get(Calendar.YEAR)

    val week_day = java.text.SimpleDateFormat("EEEE").format(calendarr.time)
    val month_Name = ExpenseSheet(0, current_Month, current_Year).getMonthName()
    val formattedDate = "$week_day, %02d $month_Name $current_Year".format(current_Day)

    var selected_Month by remember { mutableStateOf(current_Month) }
    var year_Input by remember { mutableStateOf(current_Year.toString()) }
    var error_Message by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Sheet", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Current Date", style = MaterialTheme.typography.labelMedium)
                    Text(formattedDate, fontWeight = FontWeight.Bold)
                }
            }

            Text("Enter Month (1-12)", fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = if (selected_Month == 0) "" else selected_Month.toString(),
                onValueChange = {
                    val month = it.toIntOrNull()
                    if (month != null && month in 1..12) {
                        selected_Month = month
                        error_Message = ""
                    } else if (it.isEmpty()) {
                        selected_Month = 0
                        error_Message = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Enter month (1-12)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Text("Enter Year", fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = year_Input,
                onValueChange = {
                    year_Input = it
                    error_Message = ""
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = error_Message.isNotEmpty(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (error_Message.isNotEmpty()) {
                Text(error_Message, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (selected_Month == 0 || selected_Month !in 1..12) {
                        error_Message = "Enter a valid month (1-12)"
                        return@Button
                    }

                    val yearr = year_Input.toIntOrNull()
                    if (yearr == null) {
                        error_Message = "Enter a valid year"
                        return@Button
                    }

                    val duplicatee = existingSheets.any {
                        it.month == selected_Month && it.year == yearr
                    }

                    if (duplicatee) {
                        error_Message = "A sheet for ${
                            ExpenseSheet(0, selected_Month, yearr).getMonthName()
                        } $yearr already exists"
                        return@Button
                    }

                    onSheetCreated(ExpenseSheet(IdGenerator.generateId(), selected_Month, yearr))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Create Sheet")
            }
        }
    }
}


// Edit Sheet Screen
/* This screen is committed to adjustment of an existing sheet where the user
can edit the month and year of the selected sheet with the duplication protection
to avoid data corruption */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSheetScreen(
    sheet: ExpenseSheet,
    existingSheets: List<ExpenseSheet> = emptyList(),
    onSheetUpdated: (ExpenseSheet) -> Unit,
    onBackClick: () -> Unit
) {
    var selected_Month by remember { mutableStateOf(sheet.month) }
    var year_Input by remember { mutableStateOf(sheet.year.toString()) }
    var error_Message by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Sheet", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Current Sheet")
                    Text("${sheet.getMonthName()} ${sheet.year}", fontWeight = FontWeight.Bold)
                    Text("${sheet.expenses.size} expenses • €%.2f total".format(sheet.getTotalAmount()))
                }
            }

            Text("Enter Month (1-12)", fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = if (selected_Month == 0) "" else selected_Month.toString(),
                onValueChange = {
                    val month = it.toIntOrNull()
                    if (month != null && month in 1..12) {
                        selected_Month = month
                        error_Message = ""
                    } else if (it.isEmpty()) {
                        selected_Month = 0
                        error_Message = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Enter month (1-12)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Text("Enter Year", fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = year_Input,
                onValueChange = {
                    year_Input = it
                    error_Message = ""
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = error_Message.isNotEmpty(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (error_Message.isNotEmpty()) {
                Text(error_Message, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (selected_Month == 0 || selected_Month !in 1..12) {
                        error_Message = "Enter a valid month (1-12)"
                        return@Button
                    }

                    val year = year_Input.toIntOrNull()
                    if (year == null) {
                        error_Message = "Enter a valid year"
                        return@Button
                    }

                    val duplicate = existingSheets.any {
                        it.id != sheet.id &&
                                it.month == selected_Month &&
                                it.year == year
                    }

                    if (duplicate) {
                        error_Message = "A sheet for ${
                            ExpenseSheet(0, selected_Month, year).getMonthName()
                        } $year already exists"
                        return@Button
                    }

                    onSheetUpdated(sheet.copy(month = selected_Month, year = year))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Update Sheet")
            }
        }
    }
}


// Month Dropdown
/* It is a simple dropdown user-interface component assign users with a predetermined
list of months to choose a month instead of typing numbers manually */
@Composable
fun MonthDropdown(
    selectedMonth: Int,
    onMonthSelected: (Int) -> Unit
) {
    var menu_expanded by remember { mutableStateOf(false) }

    val all_months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { menu_expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(all_months[selectedMonth - 1], color = Color.Black)
                Text("▼", color = Color.Black)
            }
        }

        DropdownMenu(
            expanded = menu_expanded,
            onDismissRequest = { menu_expanded = false }
        ) {
            all_months.forEachIndexed { index, month ->
                DropdownMenuItem(
                    text = { Text(month) },
                    onClick = {
                        menu_expanded = false
                        onMonthSelected(index + 1)
                    }
                )
            }
        }
    }
}


// Month Details Screen
/* This is a view that shows all the information on a sheet of the month
such as the income the balance and a very long list of all expenses recorded */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthDetailScreen(
    sheet: ExpenseSheet,
    onBackClick: () -> Unit,
    onIncomeUpdated: (Double) -> Unit,
    onExpenseAdded: (String, Double, String) -> Unit,
    onExpenseUpdated: (Expense) -> Unit,
    onExpenseDeleted: (Expense) -> Unit
) {
    var is_Editing_Income by remember { mutableStateOf(false) }
    var income_Input by remember { mutableStateOf(sheet.income.toString()) }
    var income_Error by remember { mutableStateOf("") }
    var show_AddExpenseDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${sheet.getMonthName()} ${sheet.year}", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { show_AddExpenseDialog = true },
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White,
                icon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add expense",
                        tint = Color.White
                    )
                },
                text = {
                    Text("Add Expense", color = Color.White)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Monthly Income")
                    Spacer(modifier = Modifier.height(8.dp))

                    if (is_Editing_Income) {
                        OutlinedTextField(
                            value = income_Input,
                            onValueChange = {
                                income_Input = it
                                income_Error = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = income_Error.isNotEmpty(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )

                        if (income_Error.isNotEmpty()) {
                            Text(income_Error, color = MaterialTheme.colorScheme.error)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val newIncome = income_Input.toDoubleOrNull()

                                    if (income_Input.isEmpty()) {
                                        income_Error = "Income cannot be empty"
                                        return@Button
                                    }

                                    if (newIncome == null || newIncome < 0) {
                                        income_Error = "Enter valid income"
                                        return@Button
                                    }

                                    onIncomeUpdated(newIncome)
                                    is_Editing_Income = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save")
                            }

                            OutlinedButton(
                                onClick = {
                                    income_Input = sheet.income.toString()
                                    is_Editing_Income = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "€%.2f".format(sheet.income),
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedButton(onClick = { is_Editing_Income = true }) {
                                Text("Edit")
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Total Spent")
                    Text(
                        "€%.2f".format(sheet.getTotalAmount()),
                        fontWeight = FontWeight.Bold
                    )
                    Text("${sheet.expenses.size} expenses")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val balance = sheet.income - sheet.getTotalAmount()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (balance >= 0)
                        MaterialTheme.colorScheme.tertiaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(if (balance >= 0) "Surplus" else "Deficit")
                    Text(
                        "€%.2f".format(kotlin.math.abs(balance)),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (sheet.expenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No expenses yet")
                        Text("Click + to add an expense")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sheet.expenses) { expense ->
                        ExpenseItem(
                            expense = expense,
                            onEdit = { onExpenseUpdated(it) },
                            onDelete = { onExpenseDeleted(it) }
                        )
                    }
                }
            }
        }

        if (show_AddExpenseDialog) {
            AddExpenseDialog(
                onExpenseAdded = { description, amount, date ->
                    onExpenseAdded(description, amount, date)
                    show_AddExpenseDialog = false
                },
                onDismiss = { show_AddExpenseDialog = false }
            )
        }
    }
}


// Expense Item
/* The expense item component gives a graphical view of a single expense with an
option of editing or deleting the relevant entry */
@Composable
fun ExpenseItem(
    expense: Expense,
    onEdit: (Expense) -> Unit,
    onDelete: (Expense) -> Unit
) {
    var show_DeleteDialog by remember { mutableStateOf(false) }
    var show_EditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.description, fontWeight = FontWeight.Bold)
                Text(expense.date, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text(
                "€%.2f".format(expense.amount),
                fontWeight = FontWeight.Bold
            )

            Column {
                IconButton(onClick = { show_EditDialog = true }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { show_DeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (show_DeleteDialog) {
        AlertDialog(
            onDismissRequest = { show_DeleteDialog = false },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete '${expense.description}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(expense)
                        show_DeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { show_DeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (show_EditDialog) {
        EditExpenseDialog(
            expense = expense,
            onExpenseUpdated = {
                onEdit(it)
                show_EditDialog = false
            },
            onDismiss = { show_EditDialog = false }
        )
    }
}


// Add Expense Dialog
/* A pop up box requesting the user to enter a description, financial value,
and date of a new expense. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    onExpenseAdded: (String, Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance()
    val defaultDate = "%02d/%02d/%04d".format(
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.YEAR)
    )

    var exp_description by remember { mutableStateOf("") }
    var exp_amount by remember { mutableStateOf("") }
    var exp_date by remember { mutableStateOf(defaultDate) }

    var descErr by remember { mutableStateOf("") }
    var amtErr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = exp_description,
                    onValueChange = {
                        exp_description = it
                        descErr = ""
                    },
                    label = { Text("Description") },
                    isError = descErr.isNotEmpty()
                )

                OutlinedTextField(
                    value = exp_amount,
                    onValueChange = {
                        exp_amount = it
                        amtErr = ""
                    },
                    label = { Text("Amount") },
                    isError = amtErr.isNotEmpty(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                OutlinedTextField(
                    value = exp_date,
                    onValueChange = { exp_date = it },
                    label = { Text("Date") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (exp_description.isEmpty()) {
                        descErr = "Required"
                        return@Button
                    }

                    val amountValue = exp_amount.toDoubleOrNull()
                    if (amountValue == null || amountValue <= 0) {
                        amtErr = "Invalid"
                        return@Button
                    }

                    onExpenseAdded(exp_description, amountValue, exp_date)
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


// Edit Expense Dialog
/* This dialog allows updating the details of a selected expense including
the changes in the amount and date */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseDialog(
    expense: Expense,
    onExpenseUpdated: (Expense) -> Unit,
    onDismiss: () -> Unit
) {
    var exp_description by remember { mutableStateOf(expense.description) }
    var exp_amount by remember { mutableStateOf(expense.amount.toString()) }
    var exp_date by remember { mutableStateOf(expense.date) }

    var descErr by remember { mutableStateOf("") }
    var amtErr by remember { mutableStateOf("") }
    var dateErr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = exp_description,
                    onValueChange = {
                        exp_description = it
                        descErr = ""
                    },
                    label = { Text("Description") },
                    isError = descErr.isNotEmpty()
                )
                if (descErr.isNotEmpty()) {
                    Text(descErr, color = MaterialTheme.colorScheme.error)
                }

                OutlinedTextField(
                    value = exp_amount,
                    onValueChange = {
                        exp_amount = it
                        amtErr = ""
                    },
                    label = { Text("Amount") },
                    isError = amtErr.isNotEmpty(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                if (amtErr.isNotEmpty()) {
                    Text(amtErr, color = MaterialTheme.colorScheme.error)
                }

                OutlinedTextField(
                    value = exp_date,
                    onValueChange = {
                        exp_date = it
                        dateErr = ""
                    },
                    label = { Text("Date") },
                    isError = dateErr.isNotEmpty()
                )
                if (dateErr.isNotEmpty()) {
                    Text(dateErr, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (exp_description.isEmpty()) {
                        descErr = "Required"
                        return@Button
                    }

                    val newAmount = exp_amount.toDoubleOrNull()
                    if (newAmount == null || newAmount <= 0) {
                        amtErr = "Invalid"
                        return@Button
                    }

                    if (exp_date.isEmpty()) {
                        dateErr = "Required"
                        return@Button
                    }

                    onExpenseUpdated(
                        expense.copy(
                            description = exp_description,
                            amount = newAmount,
                            date = exp_date
                        )
                    )
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


// Month Activity Screen
/* The month activity content module contains the logic of the
month detail screen, which handles to update its data with a database */
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


// Chart Screen with Swipe
/* Chart screen shows a scrollable line chart which illustrates the correlation
of income and expenses over a chosen time period */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreenWithSwipe(
    dbHelper: DBHelper,
    onBackPressed: () -> Unit
) {
    val all_Sheets = remember {
        dbHelper.getAllSheets().sortedWith(
            compareBy<ExpenseSheet> { it.year }.thenBy { it.month }
        )
    }

    var current_Index by remember { mutableStateOf(maxOf(0, all_Sheets.size - 4)) }

    val display_Sheets = remember(current_Index, all_Sheets) {
        if (all_Sheets.isEmpty()) {
            emptyList()
        } else {
            val start_Index = current_Index.coerceIn(0, maxOf(0, all_Sheets.size - 1))
            val end_Index = minOf(start_Index + 4, all_Sheets.size)
            all_Sheets.subList(start_Index, end_Index)
        }
    }

    val all_months = display_Sheets.map { "${it.getMonthName().take(3)} ${it.year}" }
    val income_Data = display_Sheets.map { it.income }
    val expense_Data = display_Sheets.map { it.getTotalAmount() }

    val canSwipe_Left = current_Index > 0
    val canSwipe_Right = current_Index < maxOf(0, all_Sheets.size - 4)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Income/Expenses Chart", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Income/Expenses",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Swipe left/right to navigate",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (canSwipe_Left) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Swipe left",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (canSwipe_Right) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Swipe right",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (all_Sheets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available. Create expense sheets first.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                SwipeableChart(
                    months = all_months,
                    incomeData = income_Data,
                    expenseData = expense_Data,
                    onSwipeLeft = {
                        if (canSwipe_Right) {
                            current_Index = minOf(current_Index + 1, all_Sheets.size - 4)
                        }
                    },
                    onSwipeRight = {
                        if (canSwipe_Left) {
                            current_Index = maxOf(current_Index - 1, 0)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Showing ${display_Sheets.size} of ${all_Sheets.size} months",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            if (canSwipe_Left) {
                                current_Index = maxOf(current_Index - 1, 0)
                            }
                        },
                        enabled = canSwipe_Left,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }

                    Button(
                        onClick = {
                            if (canSwipe_Right) {
                                current_Index = minOf(current_Index + 1, all_Sheets.size - 4)
                            }
                        },
                        enabled = canSwipe_Right,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(color = Color(0xFF008F82), label = "Income")
                    LegendItem(color = Color(0xFFE05780), label = "Expenses")
                }
            }
        }
    }
}


// Swipeable Chart
/* A container directly interacting with forward direction swipe gestures touch-sensitive
gadgets on the gadget are packed and the chart is displayed on the gadget */
@Composable
fun SwipeableChart(
    months: List<String>,
    incomeData: List<Double>,
    expenseData: List<Double>,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    var drag_Offset by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (abs(drag_Offset) > 100f) {
                            if (drag_Offset > 0) {
                                onSwipeRight()
                            } else {
                                onSwipeLeft()
                            }
                        }
                        drag_Offset = 0f
                    },
                    onDragCancel = {
                        drag_Offset = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        drag_Offset += dragAmount
                    }
                )
            }
    ) {
        CustomLineChart(
            months = months,
            incomeData = incomeData,
            expenseData = expenseData
        )
    }
}


// Custom Line Chart
/* This item is that component that makes the actual line chart, which is the representation
of income and expense data with the help of APIs that draw a graphic on a canvas */
@Composable
fun CustomLineChart(
    months: List<String>,
    incomeData: List<Double>,
    expenseData: List<Double>
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp)
    ) {
        val canvas_Width = size.width
        val canvas_Height = size.height

        val left_Margin = 65f
        val right_Margin = 35f
        val top_Margin = 45f
        val bottom_Margin = 65f

        val chart_AreaWidth = canvas_Width - left_Margin - right_Margin
        val chart_AreaHeight = canvas_Height - top_Margin - bottom_Margin

        val max_Income = incomeData.maxOrNull() ?: 0.0
        val max_Expense = expenseData.maxOrNull() ?: 0.0
        val max_Value = maxOf(max_Income, max_Expense)

        val yAxisMax = if (max_Value > 0) {
            val calculated = max_Value * 1.25
            if (calculated < 100) 100.0 else calculated
        } else {
            1000.0
        }

        drawLine(
            color = Color(0xFF37474F),
            start = Offset(left_Margin, top_Margin),
            end = Offset(left_Margin, canvas_Height - bottom_Margin),
            strokeWidth = 3.5f,
            cap = StrokeCap.Round
        )

        drawLine(
            color = Color(0xFF37474F),
            start = Offset(left_Margin, canvas_Height - bottom_Margin),
            end = Offset(canvas_Width - right_Margin, canvas_Height - bottom_Margin),
            strokeWidth = 3.5f,
            cap = StrokeCap.Round
        )

        val y_Steps = 5
        for (i in 0..y_Steps) {
            val y_Value = (yAxisMax / y_Steps) * i
            val y_Pos = canvas_Height - bottom_Margin - (chart_AreaHeight / y_Steps * i)

            drawLine(
                color = Color.LightGray.copy(alpha = 0.6f),
                start = Offset(left_Margin, y_Pos),
                end = Offset(canvas_Width - right_Margin, y_Pos),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )

            drawIntoCanvas { canvas ->
                val paintt = Paint().apply {
                    color = android.graphics.Color.parseColor("#1976D2")
                    textSize = 30f
                    textAlign = Paint.Align.RIGHT
                    isFakeBoldText = true
                }
                canvas.nativeCanvas.drawText(
                    "€${y_Value.toInt()}",
                    left_Margin - 12f,
                    y_Pos + 8f,
                    paintt
                )
            }
        }

        val x_Spacing = if (months.size > 1) {
            chart_AreaWidth / (months.size - 1)
        } else {
            chart_AreaWidth / 2
        }

        months.forEachIndexed { index, month ->
            val x_Pos = left_Margin + (x_Spacing * index)

            drawLine(
                color = Color(0xFF37474F),
                start = Offset(x_Pos, canvas_Height - bottom_Margin),
                end = Offset(x_Pos, canvas_Height - bottom_Margin + 8f),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )

            drawIntoCanvas { canvas ->
                val paintt = Paint().apply {
                    color = android.graphics.Color.parseColor("#424242")
                    textSize = 29f
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                }
                canvas.nativeCanvas.drawText(
                    month,
                    x_Pos,
                    canvas_Height - bottom_Margin + 42f,
                    paintt
                )
            }
        }

        if (incomeData.size > 1) {
            val income_Path = Path()

            incomeData.forEachIndexed { index, value ->
                val x_Pos = left_Margin + (x_Spacing * index)
                val y_Pos = canvas_Height - bottom_Margin -
                        ((value / yAxisMax) * chart_AreaHeight).toFloat()

                if (index == 0) {
                    income_Path.moveTo(x_Pos, y_Pos)
                } else {
                    income_Path.lineTo(x_Pos, y_Pos)
                }

                drawCircle(
                    color = Color.White,
                    radius = 10f,
                    center = Offset(x_Pos, y_Pos)
                )
                drawCircle(
                    color = Color(0xFF008F82),
                    radius = 7f,
                    center = Offset(x_Pos, y_Pos)
                )
            }

            drawPath(
                path = income_Path,
                color = Color(0xFF00B8A9),
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )
        }

        if (expenseData.size > 1) {
            val expense_Path = Path()

            expenseData.forEachIndexed { index, value ->
                val x_Pos = left_Margin + (x_Spacing * index)
                val y_Pos = canvas_Height - bottom_Margin -
                        ((value / yAxisMax) * chart_AreaHeight).toFloat()

                if (index == 0) {
                    expense_Path.moveTo(x_Pos, y_Pos)
                } else {
                    expense_Path.lineTo(x_Pos, y_Pos)
                }

                drawCircle(
                    color = Color.White,
                    radius = 10f,
                    center = Offset(x_Pos, y_Pos)
                )
                drawCircle(
                    color = Color(0xFFE05780),
                    radius = 7f,
                    center = Offset(x_Pos, y_Pos)
                )
            }

            drawPath(
                path = expense_Path,
                color = Color(0xFFFF6B9D),
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )
        }

        if (incomeData.size == 1) {
            val x_Pos = left_Margin + chart_AreaWidth / 2
            val y_Pos = canvas_Height - bottom_Margin -
                    ((incomeData[0] / yAxisMax) * chart_AreaHeight).toFloat()

            drawCircle(
                color = Color.White,
                radius = 12f,
                center = Offset(x_Pos, y_Pos)
            )
            drawCircle(
                color = Color(0xFF00B8A9),
                radius = 9f,
                center = Offset(x_Pos, y_Pos)
            )
        }

        if (expenseData.size == 1) {
            val x_Pos = left_Margin + chart_AreaWidth / 2
            val y_Pos = canvas_Height - bottom_Margin -
                    ((expenseData[0] / yAxisMax) * chart_AreaHeight).toFloat()

            drawCircle(
                color = Color.White,
                radius = 12f,
                center = Offset(x_Pos, y_Pos)
            )
            drawCircle(
                color = Color(0xFFFF6B9D),
                radius = 9f,
                center = Offset(x_Pos, y_Pos)
            )
        }
    }
}


// Legend Item
/*  A color-coded label which points to the meaning of each line used in the chart */
@Composable
fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            drawCircle(color = color)
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}