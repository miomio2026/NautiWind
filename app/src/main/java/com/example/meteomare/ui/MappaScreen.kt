package com.example.meteomare.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F7FA)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HEADER CON SCRITTA NAUTIWIND GRANDE ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF01579B), // Blu più scuro e profondo
                            Color(0xFF00B8D4)
                        )
                    )
                )
                .padding(top = 32.dp, bottom = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // IL BRAND NAUTIWIND
                Text(
                    text = "NAUTIWIND",
                    fontSize = 42.sp, // Dimensione molto grande
                    fontWeight = FontWeight.Black, // Super grassetto
                    letterSpacing = 6.sp, // Lettere distanziate per uno stile moderno
                    color = Color.White,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.4f),
                            offset = Offset(3f, 3f),
                            blurRadius = 8f
                        )
                    )
                )

                // Sottotitolo dinamico
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

                // PANNELLO PREVISIONI
                puntoSelezionato?.let { punto ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                        elevation = CardDefaults.cardElevation(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Previsioni: ${punto.nome}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF01579B))
                                TextButton(onClick = { viewModel.selezionaPunto(null) }) {
                                    Text("CHIUDI", fontWeight = FontWeight.Bold, color = Color.Gray)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                for (i in 0 until 48) {
                                    val oreTotali = oraAttuale + i
                                    val giorniDaAggiungere = oreTotali / 24
                                    val oraVisualizzata = oreTotali % 24

                                    val dataRiferimento = dataAttuale.plusDays(giorniDaAggiungere.toLong())
                                    val labelGiorno = dataRiferimento.format(formatterGiorno)
                                    val labelOra = String.format("%02d:00", oraVisualizzata)

                                    val dataIndex = oreTotali
                                    val altezza = punto.previsioniOnde.getOrNull(dataIndex) ?: 0.0
                                    val dirVento = punto.direzioniVento.getOrNull(dataIndex) ?: 0.0

                                    val colorePrevisione = when {
                                        altezza < 0.5 -> Color(0xFF00BCD4)
                                        altezza < 1.2 -> Color(0xFF1976D2)
                                        else -> Color(0xFFD32F2F)
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    ) {
                                        Text(
                                            text = labelGiorno,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (giorniDaAggiungere == 0) Color.Gray else Color(0xFF388E3C)
                                        )
                                        Text(labelOra, fontSize = 10.sp, color = Color.LightGray)
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowUp,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp).rotate(dirVento.toFloat()),
                                            tint = Color.DarkGray
                                        )
                                        Text(
                                            text = "${altezza}m",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp,
                                            color = colorePrevisione
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottone Torna Indietro
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

    val testoADestra = punto.nome in listOf("Emilia", "Marche", "Abruzzo", "Molise")

    if (testoADestra) {
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(34.dp).background(colore, CircleShape))
                Icon(Icons.Default.KeyboardArrowUp, null, tint = Color.White, modifier = Modifier.size(22.dp).rotate(direzioneVento.toFloat()))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text("${altezzaAttuale}m", fontSize = 10.sp, fontWeight = FontWeight.Black,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.8f), CircleShape).padding(horizontal = 4.dp))
                Text(punto.nome, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black,
                    modifier = Modifier.padding(top = 1.dp).background(Color.White, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp))
            }
        }
    } else {
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(34.dp).background(colore, CircleShape))
                Icon(Icons.Default.KeyboardArrowUp, null, tint = Color.White, modifier = Modifier.size(22.dp).rotate(direzioneVento.toFloat()))
            }
            Text("${altezzaAttuale}m", fontSize = 10.sp, fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 2.dp).background(Color.White.copy(alpha = 0.8f), CircleShape).padding(horizontal = 4.dp))
            Text(punto.nome, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black,
                modifier = Modifier.padding(top = 1.dp).background(Color.White, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp))
        }
    }
}
