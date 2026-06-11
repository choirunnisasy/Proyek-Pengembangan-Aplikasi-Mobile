package com.example.rewind.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.rewind.data.local.RewindDatabase
import com.example.rewind.data.local.entity.toMovie
import com.example.rewind.data.local.datastore.UserPreferences
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Worker untuk mengirim notifikasi reminder harian.
 *
 * Mengecek film dengan status WATCHING dari database,
 * kemudian mengirim notifikasi jika ada film yang sedang ditonton.
 * 
 * Worker ini hanya mengirim notifikasi jika setting notifikasi diaktifkan
 * oleh user di halaman Settings.
 */
class WatchReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val userPreferences: UserPreferences by inject()
    private val database: RewindDatabase by inject()

    companion object {
        const val WORK_NAME = "watch_reminder_worker"
    }

    override suspend fun doWork(): Result {
        return try {
            // Cek apakah notifikasi diaktifkan oleh user
            // Menggunakan first() agar hanya mengambil satu nilai dan langsung return,
            // bukan collect() yang akan suspend selamanya pada DataStore Flow.
            val isEnabled = userPreferences.notificationsEnabled.first()

            if (!isEnabled) {
                return Result.success()
            }

            // Query database untuk film dengan status WATCHING
            val watchingMovies = database.movieQueries
                .getMoviesByStatus("WATCHING")
                .executeAsList()
                .map { it.toMovie() }

            if (watchingMovies.isNotEmpty()) {
                val notificationHelper = NotificationHelper(context)

                val (title, body) = if (watchingMovies.size == 1) {
                    val movie = watchingMovies.first()
                    val progress = if (movie.totalEpisodes != null && movie.totalEpisodes > 0) {
                        " Kamu sudah di episode ${movie.watchedEpisodes}/${movie.totalEpisodes}."
                    } else {
                        ""
                    }
                    "Lanjut Nonton 🎬" to "Lanjut nonton ${movie.title}!$progress"
                } else {
                    "Lanjut Nonton 🎬" to "Kamu punya ${watchingMovies.size} film yang belum selesai. Yuk lanjut nonton!"
                }

                notificationHelper.showWatchReminderNotification(title, body)
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

