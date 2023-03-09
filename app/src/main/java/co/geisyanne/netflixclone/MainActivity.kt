package co.geisyanne.netflixclone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.geisyanne.netflixclone.model.Category
import co.geisyanne.netflixclone.util.CategoryTask

class MainActivity : AppCompatActivity(), CategoryTask.Callback {

    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: CategoryAdapter
    private val categories = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progress_main)

        adapter = CategoryAdapter(categories) { id ->
            val intent = Intent(this@MainActivity, MovieActivity::class.java)
            intent.putExtra("id", id)
            startActivity(intent)
        }
        val rvMain: RecyclerView = findViewById(R.id.rv_main)
        rvMain.layoutManager = LinearLayoutManager(this)
        rvMain.adapter = adapter

        CategoryTask(this).execute("https://api.tiagoaguiar.co/netflixapp/home?apiKey=52a4df31-b492-4a21-bf1f-2436dca1cf65")
    }

    override fun onPreExecute() {
        progressBar.visibility = View.VISIBLE
    }

    override fun onResult(categories: List<Category>) {
        Log.i("Teste", categories.toString())
        this.categories.clear()
        this.categories.addAll(categories)
        adapter.notifyDataSetChanged()
        progressBar.visibility = View.GONE
    }

    override fun onFailure(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        progressBar.visibility = View.GONE
    }
}