package com.mobileapp.activities

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.mobileapp.R
import com.mobileapp.database.PulseData
import com.mobileapp.database.pulseDatabase
import kotlinx.coroutines.launch


class StatViewActivity(): ComponentActivity() {
    private var sessionId = 0
    private lateinit var chart: LineChart
    private val pulseValues = ArrayList<Entry>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stat_view_layout)

        sessionId = intent.getIntExtra("session_id", 0)

        val db = pulseDatabase.getInstance(this)
        val pulseDataDao = db.pulseDataDao()
        var pulseList: List<PulseData>
        val pulseVals: MutableList<Int> = mutableListOf()

        val undoButton: Button = findViewById(R.id.undoBtn)
        val maxNum: TextView = findViewById(R.id.maxNum)
        val minNum: TextView = findViewById(R.id.minNum)
        val avgNum: TextView = findViewById(R.id.avgNum)


        chart = findViewById(R.id.lineChart)


        lifecycleScope.launch{
            pulseList = pulseDataDao.getPulseDataForSession(sessionId)

            for(item in pulseList){
                pulseValues.add(Entry(item.time.toFloat(),item.pulse.toFloat()))
                pulseVals.add(item.pulse)
            }

            val max = pulseVals.max()
            val min = pulseVals.min()
            val avg = pulseVals.average()

            maxNum.text = max.toString()
            minNum.text = min.toString()
            avgNum.text = avg.toString()

            //wykres
            val dataSet = LineDataSet(pulseValues, "Czas [s]")
            dataSet.color = resources.getColor(R.color.blue) // Ustawienie koloru linii
            dataSet.valueTextColor = resources.getColor(R.color.black)
            dataSet.lineWidth = 2f  // Grubość linii


            val description = Description()
            description.text = "Puls"
            description.textColor = Color.RED  // Kolor tekstu
            description.textSize = 14f        // Rozmiar czcionki
            chart.description = description

            val xAxis: XAxis = chart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM // Ustawienie pozycji osi (np. na dole)
            xAxis.setDrawLabels(true) // Włącza etykiety osi X
            xAxis.setDrawAxisLine(true) // Rysuje linię osi X
            xAxis.setDrawGridLines(false) // Wyłącza linie siatki
            xAxis.axisMinimum = 0f // Minimalna wartość osi X (opcjonalnie)
            xAxis.labelCount = 5 // Liczba etykiet na osi X
            xAxis.textSize = 12f // Rozmiar czcionki etykiet
            xAxis.textColor = Color.BLACK // Kolor tekstu etykiet
            xAxis.granularity = 1f // Granularność etykiet na osi X

            // Ustawienie osi Y
            val leftAxis: YAxis = chart.axisLeft
            leftAxis.setDrawLabels(true) // Włącza etykiety osi Y
            leftAxis.setDrawAxisLine(true) // Rysuje linię osi Y
            leftAxis.setDrawGridLines(true) // Włącza linie siatki na osi Y
            leftAxis.axisMinimum = 0f // Minimalna wartość osi Y
            leftAxis.textSize = 12f // Rozmiar czcionki etykiet
            leftAxis.textColor = Color.BLACK // Kolor tekstu etykiet

            val rightAxis: YAxis = chart.axisRight
            rightAxis.isEnabled = false // Można wyłączyć prawą oś, jeśli nie jest potrzebna




            //osie
            val lineData = LineData(dataSet)
            chart.data = lineData
            chart.invalidate()


        }

        undoButton.setOnClickListener{
            finish()
        }
    }
}