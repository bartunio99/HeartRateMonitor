import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.mobileapp.database.PulseData
import com.mobileapp.database.Session
import com.mobileapp.database.pulseDatabase
import java.time.Instant
import java.time.LocalDate

class databaseManager(private val context: Context) {

    // Metoda dodająca sesję i dane pulsu
    @RequiresApi(Build.VERSION_CODES.O)
        val db = pulseDatabase.getInstance(context)  // Pobranie instancji bazy

    @RequiresApi(Build.VERSION_CODES.O)
        val sessionDao = db.sessionDao()  // DAO dla sesji

    @RequiresApi(Build.VERSION_CODES.O)
        val pulseDataDao = db.pulseDataDao()  // DAO dla danych pulsu

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addSessionAndPulseData(startTime: Instant, startDate: LocalDate, pulseList: MutableList<Int>) {

        // Tworzymy nową sesję
        val newSession = Session(
            date = startDate,
            start_time = startTime,
            end_time = Instant.now()
        )

        // Uruchamiamy operacje w tle (coroutine)
            // Wstawiamy sesję i pobieramy jej ID
            val sessionId = sessionDao.insertSession(newSession)

            // Tworzymy dane pulsu przypisane do sesji
            var timeMs: Long = 0

            // Wstawiamy dane pulsu do bazy
            for (item in pulseList){
                pulseDataDao.insertPulseData(PulseData(session_id = sessionId.toInt(), time = timeMs, pulse = item))
                timeMs += 50
            }
            println("Sesja i dane pulsu zostały dodane do bazy!")
        }
    }

