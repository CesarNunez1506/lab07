package com.example.datossinmvvm

import com.example.mydatabase.User
import com.example.mydatabase.UserDao
import com.example.mydatabase.UserDatabase
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenUser() {
    val context = LocalContext.current
    val db = remember { crearDatabase(context) }
    val dao = db.userDao()
    val coroutineScope = rememberCoroutineScope()

    var id by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var dataUser by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Management") },
                actions = {
                    IconButton(onClick = {
                        val user = User(0, firstName, lastName)
                        coroutineScope.launch {
                            AgregarUsuario(user, dao)
                            firstName = ""
                            lastName = ""
                            dataUser = getUsers(dao)
                        }
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Agregar Usuario")
                    }
                    IconButton(onClick = {
                        coroutineScope.launch {
                            dataUser = getUsers(dao)
                        }
                    }) {
                        Icon(Icons.Filled.List, contentDescription = "Listar Usuarios")
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Spacer(Modifier.height(50.dp))
                TextField(
                    value = id,
                    onValueChange = { id = it },
                    label = { Text("ID (solo lectura)") },
                    readOnly = true,
                    singleLine = true
                )
                TextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name: ") },
                    singleLine = true
                )
                TextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name:") },
                    singleLine = true
                )
                Button(
                    onClick = {
                        coroutineScope.launch {
                            dao.deleteLastUser()
                            dataUser = getUsers(dao)
                        }
                    }
                ) {
                    Text("Eliminar Último Usuario", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = dataUser, fontSize = 20.sp
                )
            }
        }
    )
}

fun crearDatabase(context: Context): UserDatabase {
    return Room.databaseBuilder(
        context,
        UserDatabase::class.java,
        "user_db"
    ).build()
}

suspend fun getUsers(dao: UserDao): String {
    return try {
        val users = dao.getAll()
        users.joinToString(separator = "\n") { user ->
            "${user.firstName} - ${user.lastName}"
        }
    } catch (e: Exception) {
        Log.e("User", "Error: ${e.message}")
        ""
    }
}

suspend fun AgregarUsuario(user: User, dao: UserDao) {
    try {
        dao.insert(user)
    } catch (e: Exception) {
        Log.e("User", "Error: insert: ${e.message}")
    }
}
