package com.example.tabsdemo.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tabsdemo.R
import com.example.tabsdemo.Weather
import com.example.tabsdemo.databinding.FragmentMainBinding
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.io.InputStream
import java.net.URL
import java.util.*

// TODO: создать фрагмент со сведениями о погоде
class PlaceholderFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentMainBinding? = null
    private var position: Int = 1

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
        position = arguments?.getInt(ARG_SECTION_NUMBER) ?: 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        val root = binding.root

        val temp: TextView = binding.temp
        val type: TextView = binding.type
        val wSpeed: TextView = binding.wSpeed
        val windDir: ImageView = binding.windDir
        val typeImg: ImageView = binding.typeImg
        var weather: Weather
        Log.d("mytag", position.toString())
        GlobalScope.launch (Dispatchers.IO) {
            weather = context?.resources?.getStringArray(R.array.cities)?.get(position-1)?.let {
                loadWeather(
                    it
                )
            }!!

            GlobalScope.launch (Dispatchers.Main) {
                temp.text = weather.temp.toString();
                type.text = weather.type;
                wSpeed.text = weather.wSpeed.toString();
                typeImg.setImageResource(weather.typeImg)
                windDir.setImageResource(weather.wDirImg)
            }
        }

        return root
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun loadWeather(city: String): Weather {

        val API_KEY = resources.getString(R.string.API_KEY)
        val weatherURL = "https://api.openweathermap.org/data/2.5/weather?q=${city}&appid=${API_KEY}&units=metric";
        var newWeather: Weather
        val stream: InputStream

        try {
            stream = URL(weatherURL).content as InputStream
        } catch (e: Exception){
            Log.d("mytag",e.toString())
            Log.d("mytag", weatherURL)
            newWeather = Weather("Something went wrong", 0f,getString(R.string.weather_clear),0f,0,R.drawable.sun,R.drawable.north)
            return newWeather
        }


        // JSON отдаётся одной строкой,
        val data = Scanner(stream).nextLine()
        Log.d("mytag", data)

        val parser = JsonParser.parseString(data).asJsonObject

        val type = parser.get("weather").asJsonArray[0].asJsonObject.get("main").asString
        val temp = parser.get("main").asJsonObject.get("temp").toString().toFloat()
        val wSpeed = parser.get("wind").asJsonObject.get("speed").toString().toFloat()
        val wDir = parser.get("wind").asJsonObject.get("deg").toString().toInt()
        var typeString: Int = 0
        var typeImg: Int = 0
        var wDirImg: Int = 0

        when (type) {
            "Clouds" -> {
                typeImg = R.drawable.cloudy
                typeString = R.string.weather_clouds
            }
            "Clear" -> {
                typeImg = R.drawable.sun
                typeString = R.string.weather_clear
            }
            "Rain" -> {
                typeImg = R.drawable.rainy
                typeString = R.string.weather_rain
            }
            "Snow" -> {
                typeImg = R.drawable.snowy
                typeString = R.string.weather_snow
            }
        }

        when (wDir / 90) {
            0 -> wDirImg = R.drawable.north
            1 -> wDirImg = R.drawable.east
            2 -> wDirImg = R.drawable.south
            3 -> wDirImg = R.drawable.west
        }

        newWeather = Weather(city, temp, getString(typeString), wSpeed, wDir, typeImg, wDirImg)

        return newWeather
    }
}