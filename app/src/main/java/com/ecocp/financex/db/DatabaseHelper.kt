package com.ecocp.financex.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.ecocp.financex.data.Expense
import com.ecocp.financex.data.User

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "FinanceX.db", null, 3) { // bumped to version 3

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT UNIQUE, username TEXT, password TEXT)"
        )
        db.execSQL(
            "CREATE TABLE categories(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE)"
        )
        db.execSQL(
            "CREATE TABLE expenses(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, category TEXT, amount REAL, date TEXT, description TEXT)"
        )
        db.execSQL(
            "CREATE TABLE budgets(id INTEGER PRIMARY KEY AUTOINCREMENT, category TEXT, amount REAL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS categories")
        db.execSQL("DROP TABLE IF EXISTS expenses")
        db.execSQL("DROP TABLE IF EXISTS budgets")
        onCreate(db)
    }

    // ───── USER FUNCTIONS ─────
    fun insertUser(user: User): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", user.username)
            put("email", user.email)
            put("password", user.password)
        }
        val result = db.insert("users", null, values)
        return result != -1L
    }

    fun getUserByEmail(email: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE email = ?", arrayOf(email))
        return if (cursor.moveToFirst()) {
            val user = User(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                username = cursor.getString(cursor.getColumnIndexOrThrow("username")),
                password = cursor.getString(cursor.getColumnIndexOrThrow("password"))
            )
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    fun updateUser(user: User): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", user.username)
            put("password", user.password)
        }
        val result = db.update("users", values, "id = ?", arrayOf(user.id.toString()))
        return result > 0
    }

    // ───── CATEGORY FUNCTIONS ─────
    fun insertCategory(name: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
        }
        val result = db.insert("categories", null, values)
        android.util.Log.d("DatabaseHelper", "Insert category '$name': ${result != -1L}")
        return result != -1L
    }

    fun getAllCategories(): List<String> {
        val db = readableDatabase
        val list = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT name FROM categories", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        android.util.Log.d("DatabaseHelper", "Fetched categories: $list")
        return list
    }

    fun deleteCategory(name: String): Boolean {
        val db = writableDatabase
        return db.delete("categories", "name = ?", arrayOf(name)) > 0
    }

    // ───── EXPENSE FUNCTIONS ─────
    fun insertExpense(expense: Expense): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", expense.expenseName)
            put("category", expense.category)
            put("amount", expense.amount.toDouble()) // Ensure it's converted to Double
            put("date", expense.date)
            put("description", expense.description)
        }
        return db.insert("expenses", null, values) != -1L
    }

    fun getExpensesByCategory(category: String): List<Map<String, Any>> {
        val db = readableDatabase
        val list = mutableListOf<Map<String, Any>>()
        val cursor = db.rawQuery("SELECT * FROM expenses WHERE category = ?", arrayOf(category))
        if (cursor.moveToFirst()) {
            do {
                val expense = mapOf(
                    "id" to cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    "name" to cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    "category" to cursor.getString(cursor.getColumnIndexOrThrow("category")),
                    "amount" to cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                    "date" to cursor.getString(cursor.getColumnIndexOrThrow("date")),
                    "description" to cursor.getString(cursor.getColumnIndexOrThrow("description"))
                )
                list.add(expense)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getTotalExpenseForDate(date: String): Double {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT SUM(amount) FROM expenses WHERE date = ?", arrayOf(date))
        var total = 0.0
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }
        cursor.close()
        return total
    }

    fun updateExpense(id: Long, name: String, amount: Double, date: String, description: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("amount", amount)
            put("date", date)
            put("description", description)
        }
        try {
            val rowsAffected = db.update("expenses", values, "id = ?", arrayOf(id.toString()))
            android.util.Log.d("DatabaseHelper", "Update attempt for ID: $id, Rows affected: $rowsAffected")
            db.close()
            return rowsAffected > 0
        } catch (e: Exception) {
            android.util.Log.e("DatabaseHelper", "Update failed: ${e.message}")
            db.close()
            return false
        }
    }

    fun deleteExpense(id: Long): Boolean {
        val db = writableDatabase
        val rowsAffected = db.delete("expenses", "id = ?", arrayOf(id.toString()))
        db.close()
        return rowsAffected > 0
    }

    fun getExpenseId(name: String, category: String, amount: Double, date: String): Long? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id FROM expenses WHERE TRIM(name) = ? AND TRIM(category) = ? AND amount = ? AND TRIM(date) = ?",
            arrayOf(name.trim(), category.trim(), amount.toString(), date.trim())
        )
        var expenseId: Long? = null
        if (cursor.moveToFirst()) {
            expenseId = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
        }
        cursor.close()
        db.close()
        android.util.Log.d("DatabaseHelper", "getExpenseId result: $expenseId for name: $name, category: $category, amount: $amount, date: $date")
        return expenseId
    }

    // ───── BUDGET FUNCTIONS ─────
    fun insertBudget(category: String, amount: Double): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("category", category)
            put("amount", amount)
        }
        return db.insert("budgets", null, values) != -1L
    }

    fun getBudgetByCategory(category: String): Double {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT amount FROM budgets WHERE category = ?", arrayOf(category))
        var amount = 0.0
        if (cursor.moveToFirst()) {
            amount = cursor.getDouble(0)
        }
        cursor.close()
        return amount
    }

    fun updateBudget(category: String, newAmount: Double): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("amount", newAmount)
        }
        val result = db.update("budgets", values, "category = ?", arrayOf(category))
        return result > 0
    }

    fun getAllBudgets(): Map<String, Double> {
        val db = readableDatabase
        val map = mutableMapOf<String, Double>()
        val cursor = db.rawQuery("SELECT category, amount FROM budgets", null)
        if (cursor.moveToFirst()) {
            do {
                val category = cursor.getString(0)
                val amount = cursor.getDouble(1)
                map[category] = amount
            } while (cursor.moveToNext())
        }
        cursor.close()
        return map
    }
}