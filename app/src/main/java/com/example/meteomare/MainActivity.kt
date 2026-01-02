package com.example.meteomare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image // Added
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale // Added
import androidx.compose.ui.res.painterResource // Added
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meteomare.data.MarePunto
import com.example.meteomare.ui.MappaScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen // Importa questo

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFE0F7FA)) {
                // If you wanted to show the image as a background,
                // you would call a Composable here.
                MappaScreen()
            }
        }
    }
}

@Composable
fun BackgroundImage() {
    Image(
        painter = painterResource(id = R.drawable.mappa_italia),
        contentDescription = "Mappa Italia",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun SimboloOnda(punto: MarePunto, modifier: Modifier) {
    val altezzaAttuale = punto.previsioniOnde.firstOrNull() ?: 0.0
    val direzioneVento = punto.direzioniVento.firstOrNull() ?: 0.0

    val colore = when {
        altezzaAttuale < 0.5 -> Color(0xFF00BCD4)
        altezzaAttuale < 1.2 -> Color(0xFF1976D2)
        else -> Color(0xFFD32F2F)
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(32.dp).background(colore, CircleShape))

            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,tint = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(direzioneVento.toFloat()) // Se vedi 180 gradi nel log, la freccia punterà in giù
            )


        }
        Text(
            text = "${altezzaAttuale}m",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 2.dp).background(Color.White.copy(alpha = 0.7f), CircleShape).padding(horizontal = 4.dp)
        )
        Text(text = punto.nome, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}
