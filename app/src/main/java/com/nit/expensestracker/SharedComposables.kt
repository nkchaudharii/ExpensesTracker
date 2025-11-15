package com.nit.expensestracker

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import java.util.Calendar


// Simple data classes for monthly sheets and expenses
data class Expense(
    val id: Int,
    val amount: Double,
    val date: String,
    val description: String,

    )

// Each sheet records all of the expenses and income for a given month.
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

    // Determines the total amount spent in the sheet.
    fun getTotalAmount(): Double = expenses.sumOf { it.amount }
}

// This object makes sure that each new sheet or expense has a unique ID.
object IdGenerator {
    private var unique_Id = 0

    // Set IDs to the highest value already present in the database.
    fun initializeFromDatabase(dbHelper: DBHelper) {
        val maxSheet_Id = dbHelper.getMaxSheetId()
        val maxExpense_Id = dbHelper.getMaxExpenseId()
        unique_Id = maxOf(maxSheet_Id, maxExpense_Id)
    }

    // Provides the subsequent unique ID
    fun generateId(): Int {
        unique_Id += 1
        return unique_Id
    }
}

// A screen with add and chart buttons that shows every saved expenditure sheet
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
                    containerColor = Color(0xFF4CAF50),  //  GREEN TOP BAR
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
                // To create a new sheet, use the floating button
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

                // To view the chart, click the floating button.
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
            //When no sheets have been created yet, this is displayed.
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No expense sheets yet", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Tap + to create new sheet")
                }
            }
        } else {
            // Shows every created expense sheet
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
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

// A little card with the name of the month, the total amount spent, and the number of expenses
@Composable
fun SheetCard(
    sheet: ExpenseSheet,
    onClick: () -> Unit,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {}
) {
    var confirm_Delete by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)  // LIGHT GREEN BACKGROUND
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // The sheet's month and year
                Text(
                    text = "${sheet.getMonthName()} ${sheet.year}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Total entries for expenses
                Text(
                    "${sheet.expenses.size} items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 12.dp)
                )

                // Action button for editing and deleting
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF4CAF50))
                }
                IconButton(onClick = { confirm_Delete = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // The total amount spent on this sheet
            Text(
                text = "Total Expenses: €%.2f".format(sheet.getTotalAmount()),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF868686)
            )
        }
    }

    // Dialog conformation prior to sheet delection
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirm_Delete = false }) { Text("Cancel") }
            }
        )
    }
}

// The screen where a new monthly expense sheet can be added
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSheetScreen(
    existingSheets: List<ExpenseSheet> = emptyList(),
    onSheetCreated: (ExpenseSheet) -> Unit,
    onBackClick: () -> Unit
) {
    // Get the current date to display in the UI
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
            // Back navigation and a green top bar
            TopAppBar(
                title = { Text("Create New Sheet", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Provides the user with the current date.
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Current Date", style = MaterialTheme.typography.labelMedium)
                    Text(formattedDate, fontWeight = FontWeight.Bold)
                }
            }

            // The entry field for month
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

            // The entry filed for year
            Text("Enter Year", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = year_Input,
                onValueChange = { year_Input = it; error_Message = "" },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = error_Message.isNotEmpty(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Display message validation
            if (error_Message.isNotEmpty()) {
                Text(error_Message, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Add a button at the bottom
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

                    // Check for duplicate sheet
                    val duplicatee = existingSheets.any {
                        it.month == selected_Month && it.year == yearr
                    }

                    if (duplicatee) {
                        error_Message = "A sheet for ${ExpenseSheet(0, selected_Month, yearr).getMonthName()} $yearr already exists"
                        return@Button
                    }

                    onSheetCreated(ExpenseSheet(IdGenerator.generateId(), selected_Month, yearr))
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Create Sheet") }
        }
    }
}


// Editing screen for an existing expense sheet
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Summary card for the current sheet
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Current Sheet")
                    Text("${sheet.getMonthName()} ${sheet.year}", fontWeight = FontWeight.Bold)
                    Text("${sheet.expenses.size} expenses • €%.2f total".format(sheet.getTotalAmount()))
                }
            }

            // Month input field
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

            // Year input field
            Text("Enter Year", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = year_Input,
                onValueChange = { year_Input = it; error_Message = "" },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = error_Message.isNotEmpty(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (error_Message.isNotEmpty()) {
                Text(error_Message, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.weight(1f))

            // The button to store updated information
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

                    // Check if updating would create a duplicate (excluding current sheet)
                    val duplicate = existingSheets.any {
                        it.id != sheet.id &&
                                it.month == selected_Month &&
                                it.year == year
                    }

                    if (duplicate) {
                        error_Message = "A sheet for ${ExpenseSheet(0, selected_Month, year).getMonthName()} $year already exists"
                        return@Button
                    }

                    onSheetUpdated(sheet.copy(month = selected_Month, year = year))
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Update Sheet") }
        }
    }
}


// Simple dropdown menu that lets the user choose a month name
@Composable
fun MonthDropdown(selectedMonth: Int, onMonthSelected: (Int) -> Unit) {
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

        DropdownMenu(expanded = menu_expanded, onDismissRequest = { menu_expanded= false }) {
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

// A screen with all the information for a specific month (income, spending, balance)
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
            // The month name and a green header with a back button
            TopAppBar(
                title = { Text("${sheet.getMonthName()} ${sheet.year}", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        // To add a new expense, use the floating button.
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { show_AddExpenseDialog = true },
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White,
                icon = {
                    Icon(Icons.Default.Add, contentDescription = "Add expense", tint = Color.White)
                },
                text = {
                    Text("Add Expense", color = Color.White)
                }
            )
        }
    ) { paddingValues ->

        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // Income section: displays or modifies the user's monthly income
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Monthly Income")
                    Spacer(modifier = Modifier.height(8.dp))

                    if (is_Editing_Income) {
                        OutlinedTextField(
                            value = income_Input,
                            onValueChange = { income_Input = it; income_Error = "" },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = income_Error.isNotEmpty(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )

                        if (income_Error.isNotEmpty()) {
                            Text(income_Error, color = MaterialTheme.colorScheme.error)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // When editing income, click the save and cancel buttons.
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val newIncome = income_Input.toDoubleOrNull()
                                    if (income_Input.isEmpty()) {
                                        income_Error = "Income cannot be empty"; return@Button
                                    }
                                    if (newIncome == null || newIncome < 0) {
                                        income_Error = "Enter valid income"; return@Button
                                    }
                                    onIncomeUpdated(newIncome)
                                    is_Editing_Income = false
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("Save") }

                            OutlinedButton(
                                onClick = {
                                    income_Input = sheet.income.toString()
                                    is_Editing_Income = false
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("Cancel") }
                        }

                    } else {
                        // Shows the income amount while not modifying
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("€%.2f".format(sheet.income), fontWeight = FontWeight.Bold)
                            OutlinedButton(onClick = { is_Editing_Income = true }) {
                                Text("Edit")
                            }
                        }
                    }
                }
            }

            // Monthly summary of total expenses
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Total Spent")
                    Text("€%.2f".format(sheet.getTotalAmount()), fontWeight = FontWeight.Bold)
                    Text("${sheet.expenses.size} expenses")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            // Calculates whether the user saved or overspent
            val balance = sheet.income - sheet.getTotalAmount()

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (balance >= 0)
                        MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(if (balance >= 0) "Surplus" else "Deficit")
                    Text("€%.2f".format(kotlin.math.abs(balance)), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display an expenses list or a message if none are present.
            if (sheet.expenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

        //Adds a new expense entry by opening a dialogue.
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


// One expense entry is displayed on the reusable item card.
@Composable
fun ExpenseItem(
    expense: Expense,
    onEdit: (Expense) -> Unit,
    onDelete: (Expense) -> Unit
) {
    var show_DeleteDialog by remember { mutableStateOf(false) }
    var show_EditDialog by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.description, fontWeight = FontWeight.Bold)
                Text(expense.date, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text("€%.2f".format(expense.amount), fontWeight = FontWeight.Bold)

            Column {
                IconButton(onClick = { show_EditDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { show_DeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    // Before removing an expense, confirm
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { OutlinedButton(onClick = { show_DeleteDialog = false }) { Text("Cancel") } }
        )
    }

    // Clicking the edit icon opens the edit dialogue.
    if (show_EditDialog) {
        EditExpenseDialog(
            expense = expense,
            onExpenseUpdated = { onEdit(it); show_EditDialog = false },
            onDismiss = { show_EditDialog = false }
        )
    }
}

// Dialog window to update the specifics of an existing expenditure
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
                    onValueChange = { exp_description = it; descErr = "" },
                    label = { Text("Description") },
                    isError = descErr.isNotEmpty()
                )
                if (descErr.isNotEmpty())
                    Text(descErr, color = MaterialTheme.colorScheme.error)

                OutlinedTextField(
                    value = exp_amount,
                    onValueChange = { exp_amount = it; amtErr = "" },
                    label = { Text("Amount") },
                    isError = amtErr.isNotEmpty(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                if (amtErr.isNotEmpty())
                    Text(amtErr, color = MaterialTheme.colorScheme.error)

                OutlinedTextField(
                    value = exp_date,
                    onValueChange = { exp_date = it; dateErr = "" },
                    label = { Text("Date") },
                    isError = dateErr.isNotEmpty()
                )
                if (dateErr.isNotEmpty())
                    Text(dateErr, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (descErr.isEmpty()) { descErr = "Required"; return@Button }
                    val newAmount = exp_amount.toDoubleOrNull()
                    if (newAmount == null || newAmount <= 0) { amtErr = "Invalid"; return@Button }
                    if (exp_date.isEmpty()) { dateErr = "Required"; return@Button }

                    onExpenseUpdated(expense.copy(description = descErr, amount = newAmount, date = exp_date))
                }
            ) { Text("Update") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// Dialog to add a brand-new expense entry
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    onExpenseAdded: (String, Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = java.util.Calendar.getInstance()
    val defaultDate = "%02d/%02d/%04d".format(
        calendar.get(java.util.Calendar.DAY_OF_MONTH),
        calendar.get(java.util.Calendar.MONTH) + 1,
        calendar.get(java.util.Calendar.YEAR)
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
                    onValueChange = { exp_description = it; descErr = "" },
                    label = { Text("Description") },
                    isError = descErr.isNotEmpty()
                )

                OutlinedTextField(
                    value = exp_amount,
                    onValueChange = { exp_amount = it; amtErr = "" },
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
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}