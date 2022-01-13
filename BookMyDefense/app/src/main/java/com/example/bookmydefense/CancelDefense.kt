package com.example.bookmydefense

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.database.CursorIndexOutOfBoundsException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import java.util.*

class CancelDefense : AppCompatActivity() , DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cancel_defense)

        //GET USER INPUT//
        //ROOM
        val room : Spinner = findViewById(R.id.spinner_cancel)

        var rooms = arrayOf("IT201",
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
        room.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, rooms)

        room.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                spinnerFlag = rooms.get(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                spinnerFlag = spinnerFlag
            }
        }

        //DATE
        pickDate()

        //TIME
        val time : ImageButton = findViewById(R.id.btn_cancel_time)
        time.setOnClickListener {
            pickTime()
        }

        //SHOW INFO OF SEARCHED DEFENSE
        findViewById<Button>(R.id.btn_cancel_search).setOnClickListener{
            showInfo()
        }

        //CANCEL CANCELLATION
        findViewById<ImageButton>(R.id.btn_cancel_cancellation).setOnClickListener{
            finish()
        }

        //CONFIRM CANCELLATION
        findViewById<ImageButton>(R.id.btn_confirm_cancellation).setOnClickListener{
            val x:Int = contentResolver.delete(DefensesProvider.CONTENT_URI,
                         "DATE = ? AND TIME = ? AND ROOM = ?",
                                listOf<String>(regDate, regHour, spinnerFlag).toTypedArray())
            finish()
        }
    }

    var spinnerFlag : String =""

    /*--------------------------------------------------------------------------------------------*/
    private fun showInfo() {
        val cols = listOf<String>(  DefensesProvider._ID,
                                    DefensesProvider.THESIS,
                                    DefensesProvider.STUDENT_NAME,
                                    DefensesProvider.DATE,
                                    DefensesProvider.TIME,
                                    DefensesProvider.ROOM).toTypedArray()

        var rs = contentResolver.query(
            DefensesProvider.CONTENT_URI,
            cols,
            "${cols[3]} LIKE ? AND ${cols[4]} LIKE ? AND ${cols[5]} LIKE ?",
            listOf<String>(regDate, regHour, spinnerFlag).toTypedArray(),
            null
        )

        //THESIS TITLE AND STUDENT NAME OUTPUT
        val displayThesis: TextView = findViewById(R.id.cancel_thesis_title)
        val displayName: TextView = findViewById(R.id.cancel_student_name)
        try {
            if(rs?.moveToNext()!!){
                displayThesis.text = rs?.getString(1).toString()
                displayName.text = rs?.getString(2).toString()
            }
        } catch (e: CursorIndexOutOfBoundsException){
            displayThesis.text = " "
            displayName.text = " "
        }

    }

    //MANAGING CALENDAR DIALOG
    var day = 0
    var month = 0
    var year = 0

    var inputDay = 0
    var inputMonth = 0
    var inputYear = 0

    /*--------------------------------------------------------------------------------------------*/
    private fun getDateTimeCalendar() {
        val cal = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
    }

    /*--------------------------------------------------------------------------------------------*/
    private fun pickDate() {
        val buttonCalendar: ImageButton = findViewById(R.id.btn_cancel_calendar)
        buttonCalendar.setOnClickListener {
            getDateTimeCalendar()
            DatePickerDialog(this, this, year, month, day).show()
        }
    }

    var regDate: String = ""

    /*--------------------------------------------------------------------------------------------*/
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        inputDay = dayOfMonth
        inputMonth = month + 1
        inputYear = year

        getDateTimeCalendar()
        regDate = "$inputDay/$inputMonth/$inputYear"
    }

    var hour = 0
    var inputHour = 0
    var regHour: String = ""

    /*--------------------------------------------------------------------------------------------*/
    private fun pickTime() {
        val cal = Calendar.getInstance()
        hour = cal.get(Calendar.HOUR_OF_DAY)
        TimePickerDialog(this, this, hour, 0, true).show()
    }

    /*--------------------------------------------------------------------------------------------*/
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        inputHour = hourOfDay
        regHour = "$inputHour"
    }
}