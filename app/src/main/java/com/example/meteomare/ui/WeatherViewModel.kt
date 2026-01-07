package com.example.meteomare.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meteomare.api.RetrofitClient
import com.example.meteomare.data.MarePunto
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val tuttiIPunti: List<MarePunto>) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class WeatherViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState

    // Stato per la regione (null = Italia)
    private val _regioneSelezionata = MutableStateFlow<String?>(null)
    val regioneSelezionata = _regioneSelezionata.asStateFlow()

    // NUOVO: Stato per il punto specifico selezionato per il dettaglio 48h
    private val _puntoSelezionato = MutableStateFlow<MarePunto?>(null)
    val puntoSelezionato = _puntoSelezionato.asStateFlow()

    init { fetchTuttiIPunti() }

    fun selezionaRegione(nome: String?) {
        _regioneSelezionata.value = nome
        _puntoSelezionato.value = null // Resetta il dettaglio quando cambi regione
    }

    fun selezionaPunto(punto: MarePunto?) {
        _puntoSelezionato.value = punto
    }

    fun fetchTuttiIPunti() {
        viewModelScope.launch {
            try {
                val listaPunti = listOf<MarePunto>(
                    // --- PUNTI REGIONALI (Mappa Italia) ---
                    MarePunto("Liguria", "Liguria", 44.1, 9.3, xOffset = -125, yOffset = -85),
                    MarePunto("Toscana", "Toscana", 42.7, 10.2, xOffset = -90, yOffset = -50),
                    MarePunto("Lazio", "Lazio", 41.5, 12.0, xOffset = -30, yOffset = 40),
                    MarePunto("Campania", "Campania", 40.5, 14.1, xOffset = 45, yOffset = 95),
                    MarePunto("Friuli V.G.", "Friuli V.G.", 45.6, 13.5, xOffset = 60, yOffset = -175),
                    MarePunto("Veneto", "Veneto", 45.1, 12.5, xOffset = 5, yOffset = -160),
                    MarePunto("Emilia", "Emilia", 44.4, 12.4, xOffset = 35, yOffset = -115),
                    MarePunto("Marche", "Marche", 43.5, 13.8, xOffset = 70, yOffset = -80),
                    MarePunto("Abruzzo", "Abruzzo", 42.4, 14.5, xOffset = 90, yOffset = -35),
                    MarePunto("Molise", "Molise", 42.0, 15.1, xOffset = 110, yOffset = -5),
                    MarePunto("Puglia", "Puglia", 41.2, 16.8, xOffset = 155, yOffset = 30),
                    MarePunto("Basilicata", "Basilicata", 40.0, 15.6, xOffset = 155, yOffset = 105),
                    MarePunto("Calabria", "Calabria", 38.8, 15.8, xOffset = 90, yOffset = 150),
                    MarePunto("Sardegna", "Sardegna", 40.0, 8.2, xOffset = -140, yOffset = 80),
                    MarePunto("Sicilia", "Sicilia", 36.8, 13.7, xOffset = 10, yOffset = 250),

                    // --- LOCALITÀ DETTAGLIATE (Mappe Regionali) ---
                    // LIGURIA
                    MarePunto("Sanremo", "Liguria", 43.8, 7.7, xOffset = -120, yOffset = 60,
                        urlWebcam = "https://www.vedetta.org/webcam/liguria/imperia/sanremo/"),
                    MarePunto("Genova", "Liguria", 44.4, 8.9, xOffset = 0, yOffset = -40,
                        // urlWebcam = ""
                    ),
                    MarePunto("La Spezia", "Liguria", 44.1, 9.8, xOffset = 110, yOffset = 30,
                        // urlWebcam = ""
                    ),

                    // TOSCANA
                    MarePunto("Viareggio", "Toscana", 43.8, 10.2, xOffset = -40, yOffset = -100,
                        // urlWebcam = ""
                    ),
                    MarePunto("Elba", "Toscana", 42.7, 10.2, xOffset = -90, yOffset = 80,
                        // urlWebcam = ""
                    ),
                    MarePunto("Grosseto", "Toscana", 42.7, 11.0, xOffset = 70, yOffset = 110,
                        // urlWebcam = ""
                    ),

                    // LAZIO
                    MarePunto("Civitavecchia", "Lazio", 42.1, 11.7, xOffset = -150, yOffset = -20,
                        urlWebcam = "https://myearthcam.com/darsenaromanacv"
                    ),
                    MarePunto("Anzio", "Lazio", 41.4, 12.6, xOffset = -20, yOffset = 90,
                        urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/lazio/roma/anzio.html"),
                    MarePunto("Latina", "Lazio", 41.4, 12.9, xOffset = 60, yOffset = 115,
                        urlWebcam = "https://vedetta.org/webcam/italia/lazio/latina/lido-di-latina/"),
                    MarePunto("Gaeta", "Lazio", 41.2, 13.5, xOffset = 140, yOffset = 140,
                        urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/lazio/latina/gaeta.html"),

                    // CAMPANIA
                    MarePunto("Mondragone", "Campania", 41.1, 13.8, xOffset = -140, yOffset = -80,
                        // urlWebcam = ""
                    ),
                    MarePunto("Ischitella", "Campania", 40.9, 14.0, xOffset = -130, yOffset = -40,
                        urlWebcam = "https://vedetta.org/webcam/italia/campania/caserta/ischitella/"),
                    MarePunto("Napoli", "Campania", 40.8, 14.2, xOffset = -100, yOffset = 30,
                        urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/campania/napoli/napoli-posillipo.html"),
                    MarePunto("Salerno", "Campania", 40.6, 14.7, xOffset = 10, yOffset = 120,
                        // urlWebcam = ""
                    ),
                    MarePunto("Camerota", "Campania", 40.0, 15.2, xOffset = 130, yOffset = 200,
                        urlWebcam = "https://vedetta.org/webcam/italia/campania/salerno/camerota-ovest/"
                    ),

                    // PUGLIA
                    MarePunto("Vieste", "Puglia", 41.8, 16.1, xOffset = -40, yOffset = -140,
                        // urlWebcam = ""
                    ),
                    MarePunto("Bari", "Puglia", 41.1, 16.8, xOffset = 0, yOffset = 0,
                        // urlWebcam = ""
                    ),
                    MarePunto("Gallipoli", "Puglia", 40.0, 17.9, xOffset = 90, yOffset = 180,
                        // urlWebcam = ""
                    ),

                    // SICILIA
                    MarePunto("Palermo", "Sicilia", 38.1, 13.3, xOffset = -90, yOffset = -90,
                        // urlWebcam = ""
                    ),
                    MarePunto("Catania", "Sicilia", 37.5, 15.1, xOffset = 130, yOffset = 40,
                        // urlWebcam = ""
                    ),
                    MarePunto("Mazara", "Sicilia", 37.6, 12.5, xOffset = -140, yOffset = 100,
                        // urlWebcam = ""
                    ),

                    // SARDEGNA
                    MarePunto("Alghero", "Sardegna", 40.5, 8.3, xOffset = -130, yOffset = -40,
                        // urlWebcam = ""
                    ),
                    MarePunto("Olbia", "Sardegna", 40.9, 9.5, xOffset = 130, yOffset = -70,
                        // urlWebcam = ""
                    ),
                    MarePunto("Cagliari", "Sardegna", 39.2, 9.1, xOffset = 0, yOffset = 180,
                        urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/sardegna/cagliari/poetto.html"),

                    // MARCHE
                    MarePunto("Pesaro", "Marche", 43.9, 12.9, xOffset = -80, yOffset = -120,
                        // urlWebcam = ""
                    ),
                    MarePunto("Ancona", "Marche", 43.6, 13.5, xOffset = 0, yOffset = 0,
                        // urlWebcam = ""
                    ),
                    MarePunto("S.Benedetto", "Marche", 42.9, 13.8, xOffset = 50, yOffset = 150,
                        // urlWebcam = ""
                    ),

                    // ABRUZZO
                    MarePunto("Pescara", "Abruzzo", 42.4, 14.2, xOffset = -50, yOffset = -50,
                        // urlWebcam = ""
                    ),
                    MarePunto("Vasto", "Abruzzo", 42.1, 14.7, xOffset = 50, yOffset = 50,
                        // urlWebcam = ""
                    ),
                    MarePunto("Giulianova", "Abruzzo", 42.7, 14.0, xOffset = -80, yOffset = -130,
                        // urlWebcam = ""
                    ),

                    // EMILIA
                    MarePunto("Rimini", "Emilia", 44.0, 12.5, xOffset = 0, yOffset = 0,
                        // urlWebcam = ""
                    ),
                    MarePunto("Cesenatico", "Emilia", 44.2, 12.4, xOffset = -60, yOffset = -80,
                        // urlWebcam = ""
                    ),
                    MarePunto("Comacchio", "Emilia", 44.7, 12.2, xOffset = 40, yOffset = -150,
                        // urlWebcam = ""
                    ),

                    // VENETO
                    MarePunto("Venezia", "Veneto", 45.4, 12.3, xOffset = 0, yOffset = 0,
                        // urlWebcam = ""
                    ),
                    MarePunto("Caorle", "Veneto", 45.6, 12.8, xOffset = 80, yOffset = -60,
                        // urlWebcam = ""
                    ),
                    MarePunto("Jesolo", "Veneto", 45.5, 12.6, xOffset = -80, yOffset = -40,
                        // urlWebcam = ""
                    ),

                    // FRIULI V.G.
                    MarePunto("Trieste", "Friuli V.G.", 45.6, 13.7, xOffset = 0, yOffset = 0,
                        // urlWebcam = ""
                    ),
                    MarePunto("Grado", "Friuli V.G.", 45.6, 13.3, xOffset = -100, yOffset = 20,
                        // urlWebcam = ""
                    ),
                    MarePunto("Lignano", "Friuli V.G.", 45.6, 13.1, xOffset = -150, yOffset = -30,
                        // urlWebcam = ""
                    ),

                    // MOLISE
                    MarePunto("Termoli", "Molise", 42.0, 15.0, xOffset = 0, yOffset = 0,
                        // urlWebcam = ""
                    ),
                    MarePunto("Campomarino", "Molise", 41.9, 15.0, xOffset = 0, yOffset = 80,
                        // urlWebcam = ""
                    ),

                    // BASILICATA
                    MarePunto("Maratea", "Basilicata", 39.9, 15.7, xOffset = 0, yOffset = 0,
                        // urlWebcam = ""
                    ),
                    MarePunto("Scanzano", "Basilicata", 40.2, 16.7, xOffset = 100, yOffset = -100,
                        // urlWebcam = ""
                    ),
                    MarePunto("Policoro", "Basilicata", 40.1, 16.7, xOffset = 120, yOffset = 50,
                        // urlWebcam = ""
                    ),

                    // CALABRIA
                    MarePunto("Tropea", "Calabria", 38.6, 15.8, xOffset = -80, yOffset = 50,
                        // urlWebcam = ""
                    ),
                    MarePunto("Reggio C.", "Calabria", 38.1, 15.6, xOffset = 0, yOffset = 200,
                        // urlWebcam = ""
                    ),
                    MarePunto("Catanzaro", "Calabria", 38.8, 16.6, xOffset = 120, yOffset = 80,
                        // urlWebcam = ""
                    )
                )

                val taskRisultati = listaPunti.map { punto ->
                    async {
                        try {
                            val resM = RetrofitClient.marineApi.getMarineData(punto.lat, punto.lon)
                            val resW = RetrofitClient.weatherApi.getWeatherData(punto.lat, punto.lon)
                            punto.copy(
                                previsioniOnde = resM.hourly?.waveHeight?.map { it ?: 0.0 } ?: emptyList(),
                                direzioniVento = resW.hourly?.windDirection?.map { it ?: 0.0 } ?: emptyList()
                            )
                        } catch (e: Exception) { punto }
                    }
                }
                _uiState.value = WeatherUiState.Success(taskRisultati.map { it.await() })
            } catch (e: Exception) { _uiState.value = WeatherUiState.Error("Errore") }
        }
    }
}
