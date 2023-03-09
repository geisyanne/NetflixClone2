package co.geisyanne.netflixclone.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import co.geisyanne.netflixclone.model.Movie
import co.geisyanne.netflixclone.model.MovieDetail
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class MovieTask(private val callback: Callback) {

    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()

    interface Callback {
        fun onPreExecute()
        fun onResult(movieDetail: MovieDetail)
        fun onFailure(message: String)
    }

    fun execute(url: String) {
        callback.onPreExecute()

        executor.execute {
            var urlConnection: HttpsURLConnection? = null
            var stream: InputStream? = null
            var buffer: BufferedInputStream? = null

            try {
                val requestURL = URL(url)
                urlConnection = requestURL.openConnection() as HttpsURLConnection
                urlConnection.readTimeout = 2000
                urlConnection.connectTimeout = 2000

                val statusCode: Int = urlConnection.responseCode

                if (statusCode == 400) {
                    stream = urlConnection.errorStream
                    buffer = BufferedInputStream(stream)
                    val jsonAsString = toString(buffer)

                    val json = JSONObject(jsonAsString)
                    val message = json.getString("message")
                    throw IOException(message)


                } else if (statusCode > 400) {
                    throw IOException("Erro na comunicação com o servidor!")
                }

                stream = urlConnection.inputStream

                buffer = BufferedInputStream(stream)
                val jsonAsString = toString(buffer)

                val movieDetail = toMovieDetail(jsonAsString)

                handler.post {
                    callback.onResult(movieDetail)
                }

            } catch (e: IOException) {
                val message = e.message ?: "Erro desconhecido"
                Log.e("Teste", message, e)

                handler.post {
                    callback.onFailure(message)
                }

            } finally {
                urlConnection?.disconnect()
                stream?.close()
                buffer?.close()
            }
        }
    }

    private fun toMovieDetail(jsonAsString: String): MovieDetail {

        val jsonRoot = JSONObject(jsonAsString)
        val id = jsonRoot.getInt("id")
        val title = jsonRoot.getString("title")
        val desc = jsonRoot.getString("desc")
        val cast = jsonRoot.getString("cast")
        val coverUrl = jsonRoot.getString("cover_url")
        val jsonMovies = jsonRoot.getJSONArray("movie")

        val moviesSimilar = mutableListOf<Movie>()
        for (i in 0 until jsonMovies.length()) {
            val jsonMovieSimilar = jsonMovies.getJSONObject(i)
            val idSimilar = jsonMovieSimilar.getInt("id")
            val coverUrlSimilar = jsonMovieSimilar.getString("cover_url")

            moviesSimilar.add(Movie(idSimilar, coverUrlSimilar))
        }

        val movie = Movie(id, coverUrl, title, desc, cast)
        return MovieDetail(movie, moviesSimilar)
    }

    private fun toString(stream: InputStream): String {
        val bytes = ByteArray(1024)
        val baos = ByteArrayOutputStream()
        var read: Int
        while (true) {
            read = stream.read(bytes)
            if (read <= 0) {
                break
            }
            baos.write(bytes, 0, read)
        }
        return String(baos.toByteArray())
    }

}