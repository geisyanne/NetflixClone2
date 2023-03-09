package co.geisyanne.netflixclone

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.geisyanne.netflixclone.model.Movie
import co.geisyanne.netflixclone.model.MovieDetail
import co.geisyanne.netflixclone.util.DownloadImageTask
import co.geisyanne.netflixclone.util.MovieTask

class MovieActivity : AppCompatActivity(), MovieTask.Callback {

    private lateinit var txtTitle: TextView
    private lateinit var txtDesc: TextView
    private lateinit var txtCast: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: MovieAdapter
    private val movies = mutableListOf<Movie>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie)

        txtTitle = findViewById(R.id.movie_txt_title)
        txtDesc = findViewById(R.id.movie_txt_desc)
        txtCast = findViewById(R.id.movie_txt_cast)
        progressBar = findViewById(R.id.movie_progress)
        val rvSimilar: RecyclerView = findViewById(R.id.movie_rv_similar)

        val id = intent?.getIntExtra("id", 0)
            ?: throw IllegalStateException("ID não foi encontrado!")
        val url =
            "https://api.tiagoaguiar.co/netflixapp/movie/$id?apiKey=52a4df31-b492-4a21-bf1f-2436dca1cf65"

        MovieTask(this).execute(url)

        adapter = MovieAdapter(movies, R.layout.movie_item_similar)
        rvSimilar.layoutManager = GridLayoutManager(this, 3)
        rvSimilar.adapter = adapter

        val toolbar: Toolbar = findViewById(R.id.movie_toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onPreExecute() {
        progressBar.visibility = View.VISIBLE
    }

    override fun onFailure(message: String) {
        progressBar.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResult(movieDetail: MovieDetail) {
        progressBar.visibility = View.GONE

        txtTitle.text = movieDetail.movie.title
        txtDesc.text = movieDetail.movie.desc
        txtCast.text = getString(R.string.cast, movieDetail.movie.cast)

        movies.clear()
        movies.addAll(movieDetail.similars)
        adapter.notifyDataSetChanged()

        DownloadImageTask(object : DownloadImageTask.Callback {
            override fun onResult(bitmap: Bitmap) {

                val layerDrawable: LayerDrawable = ContextCompat.getDrawable(
                    this@MovieActivity,
                    R.drawable.shadows
                ) as LayerDrawable
                val movieCover = BitmapDrawable(resources, bitmap)
                layerDrawable.setDrawableByLayerId(R.id.cover_drawable, movieCover)
                val coverImg: ImageView = findViewById(R.id.movie_img)
                coverImg.setImageDrawable(layerDrawable)
            }
        }).execute(movieDetail.movie.coverUrl)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}