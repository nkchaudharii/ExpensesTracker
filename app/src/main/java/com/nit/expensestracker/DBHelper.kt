package com.nit.expensestracker

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ExpensesTracker.db"
        private const val DATABASE_VERSION = 1

        // Table for expense sheets
        private const val TABLE_SHEETS = "expense_sheets"
        private const val COLUMN_SHEET_ID = "id"
        private const val COLUMN_MONTH = "month"
        private const val COLUMN_YEAR = "year"
        private const val COLUMN_INCOME = "income"

        // Table for individual expenses
        private const val TABLE_EXPENSES = "expenses"
        private const val COLUMN_EXPENSE_ID = "id"
        private const val COLUMN_SHEET_ID_FK = "sheet_id"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_DATE = "date"
    }

    override fun onCreate(mydb: SQLiteDatabase?) {
        // Create expense sheets table
        val create_Sheets_Table = """
            CREATE TABLE $TABLE_SHEETS (
                $COLUMN_SHEET_ID INTEGER PRIMARY KEY,
                $COLUMN_MONTH INTEGER NOT NULL,
                $COLUMN_YEAR INTEGER NOT NULL,
                $COLUMN_INCOME REAL DEFAULT 0.0
            )
        """.trimIndent()

        // Create expenses table with foreign key to sheets
        val create_Expenses_Table = """
            CREATE TABLE $TABLE_EXPENSES (
                $COLUMN_EXPENSE_ID INTEGER PRIMARY KEY,
                $COLUMN_SHEET_ID_FK INTEGER NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL,
                $COLUMN_AMOUNT REAL NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                FOREIGN KEY($COLUMN_SHEET_ID_FK) REFERENCES $TABLE_SHEETS($COLUMN_SHEET_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        mydb?.execSQL(create_Sheets_Table)
        mydb?.execSQL(create_Expenses_Table)
    }

    override fun onUpgrade(mydb: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        mydb?.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        mydb?.execSQL("DROP TABLE IF EXISTS $TABLE_SHEETS")
        onCreate(mydb)
    }

    // Enable foreign key constraints
    override fun onConfigure(mydb: SQLiteDatabase?) {
        super.onConfigure(mydb)
        mydb?.setForeignKeyConstraintsEnabled(true)
    }

    // ========== EXPENSE SHEET OPERATIONS ==========

    // Insert a new expense sheet
    fun insertSheet(sheet: ExpenseSheet): Long {
        val mydb = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SHEET_ID, sheet.id)
            put(COLUMN_MONTH, sheet.month)
            put(COLUMN_YEAR, sheet.year)
            put(COLUMN_INCOME, sheet.income)
        }
        return mydb.insert(TABLE_SHEETS, null, values)
    }

    // Update an existing expense sheet
    fun updateSheet(sheet: ExpenseSheet): Int {
        val mydb = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MONTH, sheet.month)
            put(COLUMN_YEAR, sheet.year)
            put(COLUMN_INCOME, sheet.income)
        }
        return mydb.update(
            TABLE_SHEETS,
            values,
            "$COLUMN_SHEET_ID = ?",
            arrayOf(sheet.id.toString())
        )
    }

    // Update only the income for a sheet
    fun updateSheetIncome(sheetId: Int, income: Double): Int {
        val mydb = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_INCOME, income)
        }
        return mydb.update(
            TABLE_SHEETS,
            values,
            "$COLUMN_SHEET_ID = ?",
            arrayOf(sheetId.toString())
        )
    }

    // Delete an expense sheet (will also delete associated expenses due to CASCADE)
    fun deleteSheet(sheetId: Int): Int {
        val mydb = writableDatabase
        return mydb.delete(
            TABLE_SHEETS,
            "$COLUMN_SHEET_ID = ?",
            arrayOf(sheetId.toString())
        )
    }

    // Get all expense sheets
    fun getAllSheets(): List<ExpenseSheet> {
        val all_sheets = mutableListOf<ExpenseSheet>()
        val mydb = readableDatabase
        val cursorr: Cursor = mydb.query(
            TABLE_SHEETS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_YEAR ASC, $COLUMN_MONTH ASC"
        )

        with(cursorr) {
            while (moveToNext()) {
                val sheet_Id = getInt(getColumnIndexOrThrow(COLUMN_SHEET_ID))
                val monthh = getInt(getColumnIndexOrThrow(COLUMN_MONTH))
                val yearr = getInt(getColumnIndexOrThrow(COLUMN_YEAR))
                val incomee = getDouble(getColumnIndexOrThrow(COLUMN_INCOME))

                // Get expenses for this sheet
                val expense_List = getExpensesForSheet(sheet_Id)

                all_sheets.add(
                    ExpenseSheet(
                        id = sheet_Id,
                        month = monthh,
                        year = yearr,
                        income = incomee,
                        expenses = expense_List
                    )
                )
            }
        }
        cursorr.close()
        return all_sheets
    }

    // Get a single sheet by ID
    fun getSheetById(sheetId: Int): ExpenseSheet? {
        val mydb = readableDatabase
        val cursorr: Cursor = mydb.query(
            TABLE_SHEETS,
            null,
            "$COLUMN_SHEET_ID = ?",
            arrayOf(sheetId.toString()),
            null,
            null,
            null
        )

        var all_sheet: ExpenseSheet? = null
        with(cursorr) {
            if (moveToFirst()) {
                val idd = getInt(getColumnIndexOrThrow(COLUMN_SHEET_ID))
                val monthh = getInt(getColumnIndexOrThrow(COLUMN_MONTH))
                val yearr = getInt(getColumnIndexOrThrow(COLUMN_YEAR))
                val incomee = getDouble(getColumnIndexOrThrow(COLUMN_INCOME))

                // Get expenses for this sheet
                val expense_List = getExpensesForSheet(idd)

                all_sheet = ExpenseSheet(
                    id = idd,
                    month = monthh,
                    year = yearr,
                    income = incomee,
                    expenses = expense_List
                )
            }
        }
        cursorr.close()
        return all_sheet
    }

    // ========== EXPENSE OPERATIONS ==========

    // Insert a new expense
    fun insertExpense(sheetId: Int, expense: Expense): Long {
        val mydb = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EXPENSE_ID, expense.id)
            put(COLUMN_SHEET_ID_FK, sheetId)
            put(COLUMN_DESCRIPTION, expense.description)
            put(COLUMN_AMOUNT, expense.amount)
            put(COLUMN_DATE, expense.date)
        }
        return mydb.insert(TABLE_EXPENSES, null, values)
    }

    // Update an existing expense
    fun updateExpense(expense: Expense): Int {
        val mydb = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DESCRIPTION, expense.description)
            put(COLUMN_AMOUNT, expense.amount)
            put(COLUMN_DATE, expense.date)
        }
        return mydb.update(
            TABLE_EXPENSES,
            values,
            "$COLUMN_EXPENSE_ID = ?",
            arrayOf(expense.id.toString())
        )
    }

    // Delete an expense
    fun deleteExpense(expenseId: Int): Int {
        val mydb = writableDatabase
        return mydb.delete(
            TABLE_EXPENSES,
            "$COLUMN_EXPENSE_ID = ?",
            arrayOf(expenseId.toString())
        )
    }

    // Get all expenses for a specific sheet
    fun getExpensesForSheet(sheetId: Int): List<Expense> {
        val expense_List = mutableListOf<Expense>()
        val mydb = readableDatabase
        val cursorr: Cursor = mydb.query(
            TABLE_EXPENSES,
            null,
            "$COLUMN_SHEET_ID_FK = ?",
            arrayOf(sheetId.toString()),
            null,
            null,
            "$COLUMN_EXPENSE_ID ASC"
        )

        with(cursorr) {
            while (moveToNext()) {
                val idd = getInt(getColumnIndexOrThrow(COLUMN_EXPENSE_ID))
                val descriptionn = getString(getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                val amountt = getDouble(getColumnIndexOrThrow(COLUMN_AMOUNT))
                val datee = getString(getColumnIndexOrThrow(COLUMN_DATE))

                expense_List.add(
                    Expense(
                        id = idd,
                        description = descriptionn,
                        amount = amountt,
                        date = datee
                    )
                )
            }
        }
        cursorr.close()
        return expense_List
    }

    // Get a single expense by ID
    fun getExpenseById(expenseId: Int): Expense? {
        val mydb = readableDatabase
        val cursorr: Cursor = mydb.query(
            TABLE_EXPENSES,
            null,
            "$COLUMN_EXPENSE_ID = ?",
            arrayOf(expenseId.toString()),
            null,
            null,
            null
        )

        var expensee: Expense? = null
        with(cursorr) {
            if (moveToFirst()) {
                val idd = getInt(getColumnIndexOrThrow(COLUMN_EXPENSE_ID))
                val descr = getString(getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                val amtt = getDouble(getColumnIndexOrThrow(COLUMN_AMOUNT))
                val datee = getString(getColumnIndexOrThrow(COLUMN_DATE))

                expensee = Expense(
                    id = idd,
                    description = descr,
                    amount = amtt,
                    date = datee
                )
            }
        }
        cursorr.close()
        return expensee
    }

    // ========== UTILITY OPERATIONS ==========

    // Get the maximum ID used for sheets (for ID generation)
    fun getMaxSheetId(): Int {
        val mydb = readableDatabase
        val cursorr = mydb.rawQuery("SELECT MAX($COLUMN_SHEET_ID) FROM $TABLE_SHEETS", null)
        var maxxId = 0
        if (cursorr.moveToFirst()) {
            maxxId = cursorr.getInt(0)
        }
        cursorr.close()
        return maxxId
    }

    // Get the maximum ID used for expenses (for ID generation)
    fun getMaxExpenseId(): Int {
        val mydb = readableDatabase
        val cursorr = mydb.rawQuery("SELECT MAX($COLUMN_EXPENSE_ID) FROM $TABLE_EXPENSES", null)
        var maxxId = 0
        if (cursorr.moveToFirst()) {
            maxxId = cursorr.getInt(0)
        }
        cursorr.close()
        return maxxId
    }
}