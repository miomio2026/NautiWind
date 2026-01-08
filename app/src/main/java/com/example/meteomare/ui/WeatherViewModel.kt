package com.example.meteomare.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meteomare.api.RetrofitClient
import com.example.meteomare.data.MarePunto
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val tuttiIPunti: List<MarePunto>) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class WeatherViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState

    private val _regioneSelezionata = MutableStateFlow<String?>(null)
    val regioneSelezionata = _regioneSelezionata.asStateFlow()

    private val _puntoSelezionato = MutableStateFlow<MarePunto?>(null)
    val puntoSelezionato = _puntoSelezionato.asStateFlow()

    private val databasePunti = listOf<MarePunto>(
        // --- PUNTI REGIONALI ---
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

        // --- LOCALITÀ DETTAGLIATE ---
        MarePunto("Sanremo", "Liguria", 43.75, 7.78, xOffset = -175, yOffset = 125, urlWebcam = "https://www.vedetta.org/webcam/liguria/imperia/sanremo/"),
        MarePunto("Genova", "Liguria", 44.35, 8.95, xOffset = 0, yOffset = -5, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/liguria/genova/genova-boccadasse.html"),
        MarePunto("La Spezia", "Liguria", 44.05, 9.85, xOffset = 160, yOffset = 70, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/liguria/la-spezia/lerici.html"),
        MarePunto("Viareggio", "Toscana", 43.86, 10.18, xOffset = -140, yOffset = -90, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/toscana/lucca/viareggio.html"),
        MarePunto("Elba", "Toscana", 42.82, 10.35, xOffset = -120, yOffset = 100, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/toscana/livorno/isola-elba.html"),
        MarePunto("Grosseto", "Toscana", 42.70, 10.95, xOffset = -5, yOffset = 165, urlWebcam = "https://www.vedetta.org/webcam/toscana/grosseto/marina-di-grosseto/"),
        MarePunto("Civitavecchia", "Lazio", 42.08, 11.75, xOffset = -150, yOffset = -20, urlWebcam = "https://myearthcam.com/darsenaromanacv"),
        MarePunto("Anzio", "Lazio", 41.42, 12.60, xOffset = -20, yOffset = 90, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/lazio/roma/anzio.html"),
        MarePunto("Latina", "Lazio", 41.38, 12.85, xOffset = 60, yOffset = 115, urlWebcam = "https://vedetta.org/webcam/italia/lazio/latina/lido-di-latina/"),
        MarePunto("Gaeta", "Lazio", 41.18, 13.55, xOffset = 140, yOffset = 140, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/lazio/latina/gaeta.html"),
        MarePunto("Mondragone", "Campania", 41.08, 13.85, xOffset = -140, yOffset = -80, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/campania/caserta/mondragone.html"),
        MarePunto("Ischitella", "Campania", 40.92, 13.98, xOffset = -130, yOffset = -40, urlWebcam = "https://vedetta.org/webcam/italia/campania/caserta/ischitella/"),
        MarePunto("Napoli", "Campania", 40.81, 14.28, xOffset = -100, yOffset = 30, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/campania/napoli/napoli-posillipo.html"),
        MarePunto("Salerno", "Campania", 40.65, 14.72, xOffset = 10, yOffset = 120, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/campania/salerno/porto-salerno.html"),
        MarePunto("Camerota", "Campania", 39.98, 15.25, xOffset = 130, yOffset = 200, urlWebcam = "https://vedetta.org/webcam/italia/campania/salerno/camerota-ovest/"),
        MarePunto("Vieste", "Puglia", 41.92, 16.20, xOffset = -45, yOffset = -100, urlWebcam = "https://www.vedetta.org/webcam/puglia/foggia/vieste-spiaggia-del-castello/"),
        MarePunto("Bari", "Puglia", 41.15, 16.92, xOffset = 15, yOffset = -25, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/puglia/bari/lungomare-bari.html"),
        MarePunto("Lecce (S.Cataldo)", "Puglia", 40.40, 18.32, xOffset = 160, yOffset = 50, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/puglia/lecce/marina-di-lecce.html"),
        MarePunto("Gallipoli", "Puglia", 40.05, 17.90, xOffset = 135, yOffset = 170, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/puglia/lecce/gallipoli.html"),
        MarePunto("Palermo", "Sicilia", 38.15, 13.35, xOffset = -30, yOffset = -60, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/sicilia/palermo/mondello.html"),
        MarePunto("San Vito Lo Capo", "Sicilia", 38.18, 12.73, xOffset = -120, yOffset = -60, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/sicilia/trapani/san-vito-lo-capo.html"),
        MarePunto("Mazara", "Sicilia", 37.62, 12.55, xOffset = -140, yOffset = 40, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/sicilia/trapani/mazara-del-vallo.html"),
        MarePunto("Agrigento (S.Leone)", "Sicilia", 37.25, 13.58, xOffset = -40, yOffset = 105, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/sicilia/agrigento/san-leone.html"),
        MarePunto("Marina di Ragusa", "Sicilia", 36.78, 14.55, xOffset = 80, yOffset = 160, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/sicilia/ragusa/marina-di-ragusa.html"),
        MarePunto("Catania", "Sicilia", 37.48, 15.15, xOffset = 160, yOffset = 40, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/sicilia/catania/piazza-duomo-catania.html"),
        MarePunto("Siracusa", "Sicilia", 37.02, 15.32, xOffset = 175, yOffset = 90, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/siracusa/ortigia.html"),
        MarePunto("Capo d'Orlando", "Sicilia", 38.16, 14.75, xOffset = 100, yOffset = -65, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/sicilia/messina/capo-dorlando.html"),
        MarePunto("Alghero", "Sardegna", 40.58, 8.22, xOffset = -160, yOffset = -130, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/sardegna/sassari/alghero.html"),
        MarePunto("Stintino", "Sardegna", 40.95, 8.20, xOffset = -160, yOffset = -200, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/sardegna/sassari/stintino.html"),
        MarePunto("Castelsardo", "Sardegna", 40.92, 8.70, xOffset = -70, yOffset = -220, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/sardegna/sassari/castelsardo.html"),
        MarePunto("Olbia", "Sardegna", 40.95, 9.60, xOffset = 150, yOffset = -210, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/sardegna/olbia-tempio/olbia.html"),
        MarePunto("Cala Gonone", "Sardegna", 40.28, 9.65, xOffset = 160, yOffset = 20, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/sardegna/nuoro/cala-gonone.html"),
        MarePunto("Villasimius", "Sardegna", 39.10, 9.55, xOffset = 140, yOffset = 270, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/sardegna/cagliari/villasimius.html"),
        MarePunto("Cagliari", "Sardegna", 39.18, 9.15, xOffset = 30, yOffset = 260, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/sardegna/cagliari/poetto.html"),
        MarePunto("Sant'Antioco", "Sardegna", 38.95, 8.35, xOffset = -50, yOffset = 320, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/sardegna/carbonia-iglesias/calasetta.html"),
        MarePunto("Oristano", "Sardegna", 39.90, 8.40, xOffset = -120, yOffset = 80, urlWebcam = "https://www.vedetta.org/webcam/sardegna/oristano/torre-grande/"),
        MarePunto("Pesaro", "Marche", 43.95, 12.95, xOffset = -10, yOffset = -200, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/marche/pesaro-urbino/pesaro.html"),
        MarePunto("Ancona", "Marche", 43.62, 13.55, xOffset = 110, yOffset = -100, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/marche/ancona/porto-ancona.html"),
        MarePunto("S.Benedetto", "Marche", 42.95, 13.92, xOffset = 180, yOffset = 125, urlWebcam = "https://www.whatsupcams.com/it/webcams/italia/marche/san-benedetto-del-tronto/san-benedetto-del-tronto-spiaggia/"),
        MarePunto("Pescara", "Abruzzo", 42.48, 14.25, xOffset = 80, yOffset = -50, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/abruzzo/pescara/pescara-beach.html"),
        MarePunto("Vasto", "Abruzzo", 42.12, 14.75, xOffset = 185, yOffset = 40, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/abruzzo/chieti/vasto-marina.html"),
        MarePunto("Giulianova", "Abruzzo", 42.75, 14.02, xOffset = 30, yOffset = -150, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/abruzzo/teramo/giulianova-lido.html"),
        MarePunto("Rimini", "Emilia", 44.08, 12.62, xOffset = 180, yOffset = 80, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/emilia-romagna/rimini/rimini-marina-centro.html"),
        MarePunto("Cesenatico", "Emilia", 44.22, 12.45, xOffset = 170, yOffset = 20, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/emilia-romagna/forli-cesena/cesenatico.html"),
        MarePunto("Comacchio", "Emilia", 44.68, 12.28, xOffset = 160, yOffset = -50, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/emilia-romagna/ferrara/lido-di-pomposa.html"),
        MarePunto("Venezia (Lido)", "Veneto", 45.38, 12.45, xOffset = 90, yOffset = 110, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/veneto/venezia/venezia-lido.html"),
        MarePunto("Jesolo", "Veneto", 45.48, 12.68, xOffset = 130, yOffset = 80, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/veneto/venezia/jesolo-lido.html"),
        MarePunto("Caorle", "Veneto", 45.55, 12.92, xOffset = 170, yOffset = 50, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/veneto/venezia/caorle.html"),
        MarePunto("Trieste", "Friuli V.G.", 45.62, 13.72, xOffset = 160, yOffset = 185, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/friuli-venezia-giulia/trieste/piazza-unita-italia.html"),
        MarePunto("Grado", "Friuli V.G.", 45.65, 13.38, xOffset = 75, yOffset = 170, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/friuli-venezia-giulia/gorizia/grado.html"),
        MarePunto("Lignano", "Friuli V.G.", 45.63, 13.15, xOffset = 10, yOffset = 170, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/friuli-venezia-giulia/udine/lignano-sabbiadoro.html"),
        MarePunto("Termoli", "Molise", 42.02, 15.05, xOffset = 110, yOffset = -135, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/molise/campobasso/termoli.html"),
        MarePunto("Campomarino", "Molise", 41.95, 15.08, xOffset = 160, yOffset = -110, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/molise/campobasso/campomarino-lido.html"),
        MarePunto("Maratea", "Basilicata", 39.98, 15.65, xOffset = -140, yOffset = 200, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/basilicata/potenza/maratea.html"),
        MarePunto("Scanzano", "Basilicata", 40.22, 16.75, xOffset = 180, yOffset = 100, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/basilicata/matera/lido-di-metaponto.html"),
        MarePunto("Policoro", "Basilicata", 40.15, 16.72, xOffset = 160, yOffset = 160, urlWebcam = "https://www.meteosystem.com/wlive/webcam.php?webcam=policoro"),
        MarePunto("Praia a Mare", "Calabria", 39.90, 15.72, xOffset = -160, yOffset = -210, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/calabria/cosenza/praia-a-mare.html"),
        MarePunto("Tropea", "Calabria", 38.68, 15.85, xOffset = -170, yOffset = 100, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/calabria/vibo-valentia/tropea.html"),
        MarePunto("Scilla", "Calabria", 38.26, 15.70, xOffset = -180, yOffset = 230, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/calabria/reggio-calabria/scilla.html"),
        MarePunto("Reggio C.", "Calabria", 38.12, 15.62, xOffset = -180, yOffset = 300, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/calabria/reggio-calabria/reggio-calabria-lungomare.html"),
        MarePunto("Catanzaro Lido", "Calabria", 38.82, 16.65, xOffset = 50, yOffset = 100, urlWebcam = "https://www.skylinewebcams.com/it/webcam/italia/calabria/catanzaro/catanzaro-lido.html"),
        MarePunto("Cirò Marina", "Calabria", 39.38, 17.15, xOffset = 160, yOffset = -100, urlWebcam = "https://www.meteosystem.com/wlive/webcam.php?webcam=ciromarina")
    )

    init {
        fetchTuttiIPunti()
    }

    fun selezionaRegione(nome: String?) {
        _regioneSelezionata.value = nome
        _puntoSelezionato.value = null
    }

    fun selezionaPunto(punto: MarePunto?) {
        _puntoSelezionato.value = punto
    }

    // SCARICA TUTTO ALL'AVVIO (Senza lag visivo)
    private fun fetchTuttiIPunti() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Limitiamo la banda a 4 richieste parallele per stabilità
                val semaphore = Semaphore(4)

                val taskRisultati = databasePunti.map { punto ->
                    async {
                        semaphore.withPermit {
                            try {
                                // Timeout di 10 secondi per singolo punto
                                withTimeout(10000) {
                                    val resM = RetrofitClient.marineApi.getMarineData(punto.lat, punto.lon)
                                    val resW = RetrofitClient.weatherApi.getWeatherData(punto.lat, punto.lon)
                                    punto.copy(
                                        previsioniOnde = resM.hourly?.waveHeight?.map { it ?: 0.0 } ?: emptyList(),
                                        direzioniVento = resW.hourly?.windDirection?.map { it ?: 0.0 } ?: emptyList()
                                    )
                                }
                            } catch (e: Exception) {
                                punto // In caso di errore, ritorna il punto senza dati
                            }
                        }
                    }
                }

                val risultatiFinali = taskRisultati.awaitAll()

                // Aggiorniamo la UI solo quando TUTTO è pronto (niente pallini che cambiano colore uno alla volta)
                withContext(Dispatchers.Main) {
                    _uiState.value = WeatherUiState.Success(risultatiFinali)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = WeatherUiState.Error("Connessione instabile")
                }
            }
        }
    }
}
