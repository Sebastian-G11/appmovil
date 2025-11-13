package com.example.componentestest.Componentes.Firebase

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebase_testapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLogin(onLoginSuccess: () -> Unit) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D141A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {

            //Ícono superior
            Image(
                painter = painterResource(id = R.drawable.house_2), // Reemplaza por tu ícono real
                contentDescription = "Icono casa",
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )

            // Texto de bienvenida
            Text(
                text = "Bienvenido de nuevo",
                fontSize = 22.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Inicia sesión para controlar tu techo",
                fontSize = 14.sp,
                color = Color(0xFFB0B8C1),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp, top = 4.dp)
            )

            // Campo de usuario
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de Usuario", color = Color(0xFFB0B8C1)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                shape = RoundedCornerShape(10.dp),

                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF23A8F2),
                    unfocusedBorderColor = Color(0xFF263238),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF23A8F2)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Campo de contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña", color = Color(0xFFB0B8C1)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF23A8F2),
                    unfocusedBorderColor = Color(0xFF263238),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF23A8F2)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )

            // Botón Iniciar Sesión
            Button(
                onClick = {
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        println("Intentando iniciar sesión con: $username / $password")
                        onLoginSuccess()
                    } else {
                        mensaje = "Por favor, completa todos los campos."
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF23A8F2),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Iniciar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            if (mensaje.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(mensaje, color = Color(0xFFFF5252), fontSize = 13.sp)
            }
        }
    }
}
