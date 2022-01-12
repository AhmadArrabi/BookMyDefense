package com.example.bookmydefense

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.*
import java.util.*
import android.widget.ArrayAdapter

class MainActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //ADD A NEW DEFENSE
        val buttonAddDefense: ImageButton = findViewById(R.id.btn_add_defense)
        buttonAddDefense.setOnClickListener { view ->
            //call new activity to add defense to database
            val intent = Intent(this, AddDefenseActivity::class.java)
            startActivity(intent)
        }

        val buttonCalendar: ImageButton = findViewById(R.id.btn_cancel_calendar)
        buttonCalendar.setOnClickListener{
            getDateTimeCalendar()
            DatePickerDialog(this, this, year, month, day).show()
        }

        val buttonTime: ImageButton = findViewById(R.id.btn_time2)
        buttonTime.setOnClickListener{
            pickTime()
        }

        val buttonCancel : ImageButton = findViewById(R.id.btn_delete_defense)
        buttonCancel.setOnClickListener{
            //call new activity to add defense to database
            val intent = Intent(this, CancelDefense::class.java)
            startActivity(intent)
        }
    }

    /*----------------------------------------------------------------------------------------------------------*/
    //DISPLAY BOOKED AND AVAILABLE ROOMS (QUERY)
    private fun showRooms() {

        var bookedDate: String = "$inputDay/$inputMonth/$inputYear"

        val cols = listOf<String>(DefensesProvider._ID, DefensesProvider.ROOM, DefensesProvider.DATE, DefensesProvider.TIME).toTypedArray()
        val bookedList: ListView = findViewById(R.id.booked_list)
        val availableList : ListView = findViewById(R.id.available_list)

        var rs = contentResolver.query(
            DefensesProvider.CONTENT_URI,
            cols,
            "${cols[2]} LIKE ? AND ${cols[3]} LIKE ?",
            listOf<String>("$bookedDate", "$bookedTime").toTypedArray(),
            null
        )

        var from = listOf<String>(cols[1]).toTypedArray()
        var to = intArrayOf(android.R.id.text1)

        var adapter = SimpleCursorAdapter(this,
            android.R.layout.simple_list_item_1,
            rs,
            from,
            to,
            0)

        bookedList.adapter = adapter

        val bookedRooms = generateSequence { if (rs!!.moveToNext()) rs else null }
            .map { it.getString(1) }
            .toList()

        var allRooms = listOf<String>("IT201",
                                        "IT202",
                                        "IT203",
                                        "IT204",
                                        "IT205",
                                        "IT206",
                                        "IT207",
                                        "IT208",
                                        "IT301",
                                        "IT302",
                                        "IT303")

        var availableRooms = allRooms subtract bookedRooms

        val adapter2: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, availableRooms.toTypedArray())

        availableList.adapter = adapter2
    }

    /*----------------------------------------------------------------------------------------------------------*/
    //MANAGING CALENDAR DIALOG
    var day = 0
    var month = 0
    var year = 0

    var inputDay = 0
    var inputMonth = 0
    var inputYear = 0

    /*----------------------------------------------------------------------------------------------------------*/
    private fun getDateTimeCalendar(){
        val cal= Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
    }

    /*----------------------------------------------------------------------------------------------------------*/
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        inputDay = dayOfMonth
        inputMonth = month+1
        inputYear = year

        getDateTimeCalendar()

        //DATE OUTPUT
        val displayDate : TextView = findViewById(R.id.out_date)
        displayDate.text = "$inputDay/$inputMonth/$inputYear"
        showRooms()
    }

    /*----------------------------------------------------------------------------------------------------------*/
    var hour = 0
    var inputHour = 0
    var bookedTime:String=""

    /*----------------------------------------------------------------------------------------------------------*/
    private fun pickTime(){
        val cal= Calendar.getInstance()
        hour = cal.get(Calendar.HOUR_OF_DAY)
        TimePickerDialog(this, this, hour, 0, true).show()

    }

    /*----------------------------------------------------------------------------------------------------------*/
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        inputHour = hourOfDay
        bookedTime = "$inputHour"

        //TIME OUTPUT
        val displayTime : TextView = findViewById(R.id.out_time)
        displayTime.text = "$bookedTime:00"

        showRooms()
    }
}