package com.example.meteomare.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meteomare.R
import com.example.meteomare.data.MarePunto
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MappaScreen(viewModel: WeatherViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val regioneAttiva by viewModel.regioneSelezionata.collectAsState()
    val puntoSelezionato by viewModel.puntoSelezionato.collectAsState()
    val context = LocalContext.current

    val oraAttuale = remember { LocalTime.now().hour }
    val dataAttuale = remember { LocalDate.now() }
    val formatterGiorno = remember { DateTimeFormatter.ofPattern("EEE d", Locale.ITALIAN) }

    // --- LOGICA TASTO INDIETRO ---
    BackHandler(enabled = regioneAttiva != null) {
        if (puntoSelezionato != null) {
            viewModel.selezionaPunto(null)
        } else {
            viewModel.selezionaRegione(null)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F7FA)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF01579B), Color(0xFF00B8D4))
                    )
                )
                .padding(top = 32.dp, bottom = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "NAUTIWIND",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp,
                    color = Color.White,
                    style = TextStyle(
                        shadow = Shadow(Color.Black.copy(alpha = 0.4f), Offset(3f, 3f), 8f)
                    )
                )
                Text(
                    text = if (regioneAttiva == null) "SITUAZIONE MARI ITALIA" else "DETTAGLIO $regioneAttiva",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        when (val state = uiState) {
            is WeatherUiState.Loading -> {
                Box(Modifier.fillMaxSize()) { CircularProgressIndicator(Modifier.align(Alignment.Center)) }
            }
            is WeatherUiState.Success -> {
                Box(modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(), contentAlignment = Alignment.Center) {
                    val immagineRes = if (regioneAttiva == null) {
                        R.drawable.mappa_italia
                    } else {
                        val nomeFile = "mappa_${regioneAttiva!!.lowercase().replace(" ", "").replace(".", "")}"
                        val resId = context.resources.getIdentifier(nomeFile, "drawable", context.packageName)
                        if (resId != 0) resId else R.drawable.mappa_italia
                    }

                    Image(
                        painter = painterResource(id = immagineRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(0.95f),
                        contentScale = ContentScale.Fit
                    )

                    val puntiDaMostrare = if (regioneAttiva == null) {
                        state.tuttiIPunti.filter { it.nome == it.regione }
                    } else {
                        state.tuttiIPunti.filter { it.regione == regioneAttiva && it.nome != it.regione }
                    }

                    puntiDaMostrare.forEach { punto ->
                        SimboloOndaInternal(
                            punto = punto,
                            oraIndex = oraAttuale,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(punto.xOffset.dp, punto.yOffset.dp)
                                .clickable {
                                    if (regioneAttiva == null) {
                                        viewModel.selezionaRegione(punto.regione)
                                    } else {
                                        viewModel.selezionaPunto(punto)
                                    }
                                }
                        )
                    }
                }

                // --- PANNELLO PREVISIONI ---
                puntoSelezionato?.let { punto ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                            .zIndex(10f),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFFFFFFF).copy(alpha = 0.95f),
                                            Color(0xFFE3F2FD).copy(alpha = 0.98f)
                                        )
                                    )
                                )
                                .border(
                                    BorderStroke(1.dp, Color.White),
                                    RoundedCornerShape(28.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("PREVISIONI", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0277BD), letterSpacing = 2.sp)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(punto.nome, fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color(0xFF01579B))

                                            if (!punto.urlWebcam.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    color = Color(0xFFD32F2F),
                                                    shape = RoundedCornerShape(50),
                                                    modifier = Modifier.clickable {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(punto.urlWebcam))
                                                        context.startActivity(intent)
                                                    }
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Icon(Icons.Default.Videocam, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("LIVE CAM", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    IconButton(
                                        onClick = { viewModel.selezionaPunto(null) },
                                        modifier = Modifier.background(Color(0xFF0277BD).copy(alpha = 0.1f), CircleShape)
                                    ) {
                                        Text("✕", fontWeight = FontWeight.Bold, color = Color(0xFF0277BD))
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                    for (i in 0 until 48) {
                                        val oreTotali = oraAttuale + i
                                        val giorniDaAggiungere = oreTotali / 24
                                        val oraVisualizzata = oreTotali % 24
                                        val labelGiorno = dataAttuale.plusDays(giorniDaAggiungere.toLong()).format(formatterGiorno)
                                        val labelOra = String.format("%02d:00", oraVisualizzata)
                                        val altezza = punto.previsioniOnde.getOrNull(oreTotali) ?: 0.0
                                        val dirVento = punto.direzioniVento.getOrNull(oreTotali) ?: 0.0
                                        val velVento = punto.velocitaVento.getOrNull(oreTotali) ?: 0.0
                                        val tempAcqua = punto.temperaturaAcqua.getOrNull(oreTotali) ?: 0.0

                                        val colorePrevisione = when {
                                            altezza < 0.5 -> Color(0xFF00BCD4)
                                            altezza < 1.2 -> Color(0xFF1976D2)
                                            else -> Color(0xFFD32F2F)
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .padding(horizontal = 10.dp)
                                                .background(
                                                    if (i == 0) Color(0xFF0277BD).copy(alpha = 0.05f) else Color.Transparent,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .padding(8.dp)
                                        ) {
                                            Text(labelGiorno.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Black, color = if (giorniDaAggiungere == 0) Color.Gray else Color(0xFF388E3C))
                                            Text(labelOra, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowUp,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .rotate(dirVento.toFloat() + 180f),
                                                tint = Color(0xFF455A64)
                                            )
                                            Text("${velVento.toInt()} km/h", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF546E7A))
                                            Text("${altezza}m", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = colorePrevisione)
                                            Text("${tempAcqua.toInt()}°C", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0288D1))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (regioneAttiva != null) {
                    Button(
                        onClick = { viewModel.selezionaRegione(null) },
                        modifier = Modifier.padding(bottom = 24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD)),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Torna alla Mappa Italia", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            is WeatherUiState.Error -> Text("Errore: ${state.message}", color = Color.Red, modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun SimboloOndaInternal(punto: MarePunto, oraIndex: Int, modifier: Modifier) {
    val altezzaAttuale = punto.previsioniOnde.getOrNull(oraIndex) ?: 0.0
    val direzioneVento = punto.direzioniVento.getOrNull(oraIndex) ?: 0.0
    val colore = when {
        altezzaAttuale < 0.5 -> Color(0xFF00BCD4)
        altezzaAttuale < 1.2 -> Color(0xFF1976D2)
        else -> Color(0xFFD32F2F)
    }
    val testoADestra = punto.nome in listOf("Emilia", "Marche", "Abruzzo", "Molise", "Sardegna", "Castel Volturno", "Ischitella", "Sant'Antioco")

    if (testoADestra) {
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier
                    .size(34.dp)
                    .background(colore, CircleShape))
                Icon(Icons.Default.KeyboardArrowUp, null, tint = Color.White, modifier = Modifier
                    .size(22.dp)
                    .rotate(direzioneVento.toFloat() + 180f))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text("${altezzaAttuale}m", fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier
                    .background(Color.White.copy(alpha = 0.8f), CircleShape)
                    .padding(horizontal = 4.dp))
                Text(punto.nome, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier
                    .padding(top = 1.dp)
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp))
            }
        }
    } else {
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier
                    .size(34.dp)
                    .background(colore, CircleShape))
                Icon(Icons.Default.KeyboardArrowUp, null, tint = Color.White, modifier = Modifier
                    .size(22.dp)
                    .rotate(direzioneVento.toFloat() + 180f))
            }
            Text("${altezzaAttuale}m", fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier
                .padding(top = 2.dp)
                .background(Color.White.copy(alpha = 0.8f), CircleShape)
                .padding(horizontal = 4.dp))
            Text(punto.nome, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier
                .padding(top = 1.dp)
                .background(Color.White, RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp))
        }
    }
}
