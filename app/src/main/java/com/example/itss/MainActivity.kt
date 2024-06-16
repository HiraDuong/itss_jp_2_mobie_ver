package com.example.itss

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    lateinit var d9Info: TextView
    lateinit var c7Info: TextView
    lateinit var d3Info: TextView
    lateinit var redBtn: Button
    lateinit var yellowBtn: Button
    lateinit var greenBtn: Button
    var hPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val spinner: Spinner = findViewById(R.id.spinner)

        val options = arrayOf("D9", "C7", "D3")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                Toast.makeText(this@MainActivity, "Selected: $selectedItem", Toast.LENGTH_SHORT).show()
                Log.d("item", "Selected: $selectedItem")
                hPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // button listener
        redBtn = findViewById(R.id.red_btn)
        greenBtn = findViewById(R.id.gr_btn)
        yellowBtn = findViewById(R.id.yellow_btn)
        d9Info = findViewById(R.id.d9_info)
        d3Info = findViewById(R.id.d3_info)
        c7Info = findViewById(R.id.c7_info)

        redBtn.setOnClickListener {
            sendPostRequest(hPosition, "R")
            updateTrafficInfo(hPosition, "Đang tắc")
        }

        greenBtn.setOnClickListener {
            sendPostRequest(hPosition, "G")
            updateTrafficInfo(hPosition, "Không tắc")
        }

        yellowBtn.setOnClickListener {
            sendPostRequest(hPosition, "Y")
            updateTrafficInfo(hPosition, "Có thể tắc")
        }
    }

    private fun updateTrafficInfo(position: Int, status: String) {
        val color = when (status) {
            "Không tắc" -> Color.parseColor("#FF4CAF50")
            "Đang tắc" -> Color.RED
            "Có thể tắc" -> Color.parseColor("#C9DA21")
            else -> Color.BLACK
        }
        when (position) {
            0 -> {
                d9Info.text = status
                d9Info.setTextColor(color)
            }
            1 -> {
                c7Info.text = status
                c7Info.setTextColor(color)
            }
            2 -> {
                d3Info.text = status
                d3Info.setTextColor(color)
            }
        }
    }

    val JSON: MediaType = "application/json".toMediaTypeOrNull()!!

    val client = OkHttpClient()

    private fun sendPostRequest(p: Int, s: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val jsonObject = JSONObject()
                jsonObject.put("message", p.toString() + s)
                val json = jsonObject.toString()
                val body = RequestBody.create(JSON, json)
                val request = Request.Builder()
                    .url("https://itss-jp-2-hiraduong-hiraduongs-projects.vercel.app/api/mqtt")
                    .post(body)
                    .build()
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        Log.d("HTTP Response", responseBody)
                    } else {
                        throw IOException("Unexpected empty response body")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Request failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
