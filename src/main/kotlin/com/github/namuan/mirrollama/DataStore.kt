package com.github.namuan.mirrollama

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

data class User(val id: Int, val name: String, val age: Int)

class DatabaseManager(databasePath: String) {
    private var connection: Connection? = null

    init {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:$databasePath")
        } catch (e: SQLException) {
            println(e.message)
        }
    }

    fun closeConnection() {
        try {
            connection?.close()
        } catch (e: SQLException) {
            println(e.message)
        }
    }

    // CRUD operations will be implemented here
    fun createTable() {
        val sql = """
           CREATE TABLE IF NOT EXISTS users (
               id INTEGER PRIMARY KEY,
               name TEXT NOT NULL,
               age INTEGER NOT NULL
           )
       """
        executeUpdate(sql)
    }

    fun insertUser(name: String, age: Int) {
        val sql = "INSERT INTO users(name, age) VALUES(?, ?)"
        executeUpdate(sql, name, age)
    }

    fun getUsers(): List<User> {
        val sql = "SELECT * FROM users"
        val users = mutableListOf<User>()
        executeQuery(sql) { resultSet ->
            while (resultSet.next()) {
                val id = resultSet.getInt("id")
                val name = resultSet.getString("name")
                val age = resultSet.getInt("age")
                users.add(User(id, name, age))
            }
        }
        return users
    }

    fun updateUser(id: Int, name: String, age: Int) {
        val sql = "UPDATE users SET name = ?, age = ? WHERE id = ?"
        executeUpdate(sql, name, age, id)
    }

    fun deleteUser(id: Int) {
        val sql = "DELETE FROM users WHERE id = ?"
        executeUpdate(sql, id)
    }

    private fun executeQuery(sql: String, action: (resultSet: ResultSet) -> Unit) {
        connection?.prepareStatement(sql)?.use { statement ->
            statement.executeQuery().use { resultSet ->
                action(resultSet)
            }
        }
    }

    private fun executeUpdate(sql: String, vararg params: Any) {
        connection?.prepareStatement(sql)?.use { statement ->
            params.forEachIndexed { index, param ->
                statement.setObject(index + 1, param)
            }
            statement.executeUpdate()
        }
    }
}

