package com.example.potholedetection_v1.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.potholedetection_v1.R
import com.example.potholedetection_v1.navigation.AppRoutes

@Composable
fun WelcomeScreen(navController: NavController,
                  onWelcomeCompleted: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    val welcomePages = listOf(
        WelcomePage(
            title = "Bienvenido a Pothole Detector",
            description = "Ayúdanos a mejorar la infraestructura vial detectando baches automáticamente",
            imageRes = R.drawable.auto_ciudad
        ),
        WelcomePage(
            title = "Detección Automática",
            description = "Usa los sensores de tu smartphone para identificar baches mientras conduces",
            imageRes = R.drawable.auto_ciudad_bache
        ),
        WelcomePage(
            title = "Contribuye a tu Comunidad",
            description = "Tus reportes ayudan a las autoridades a priorizar reparaciones de calles",
            imageRes = R.drawable.auto_ciudad_bache_registro
        )
    )

    // Solicitud de permisos
    val permissionsRequested = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Contenido de la página actual
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = welcomePages[currentPage].imageRes),
                contentDescription = "Welcome Illustration",
                modifier = Modifier
                    .size(250.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = welcomePages[currentPage].title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = welcomePages[currentPage].description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }

        // Indicadores de página y botones de navegación
        Column {
            // Indicadores de página
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                welcomePages.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (index == currentPage) MaterialTheme.colorScheme.primary
                                else Color.Gray.copy(alpha = 0.3f),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 4.dp)
                    )
                }
            }

            // Botones de navegación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Botón Anterior
                if (currentPage > 0) {
                    OutlinedButton(
                        onClick = { currentPage-- },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Anterior")
                    }
                }

                // Espaciado
                Spacer(modifier = Modifier.width(16.dp))

                // Botón Siguiente/Comenzar
                Button(
                    onClick = {
                        if (currentPage < welcomePages.size - 1) {
                            currentPage++
                        } else {
                            // Llama al callback antes de navegar
                            onWelcomeCompleted()
                            navController.navigate(AppRoutes.MAIN) {
                                popUpTo(AppRoutes.WELCOME) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (currentPage < welcomePages.size - 1) "Siguiente"
                        else "Comenzar"
                    )
                }
            }
        }
    }
}

// Clase de datos para las páginas de bienvenida
data class WelcomePage(
    val title: String,
    val description: String,
    val imageRes: Int
)