package com.example.bookmydefense

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import android.widget.EditText
import android.widget.Toast
import java.util.*

class AddDefenseActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_defense_activity)

        /*----------------------------------------------------------------------------------------------------------*/
        //GET USER INPUT
        val studentName : EditText = findViewById(R.id.in_std_name)
        val thesisTitle : EditText = findViewById(R.id.in_thesis)
        val comName1 : EditText = findViewById(R.id.in_com1)
        val comName2 : EditText = findViewById(R.id.in_com2)
        val comName3 : EditText = findViewById(R.id.in_com3)
        val time : ImageButton = findViewById(R.id.btn_time)
        val userEmail: EditText = findViewById(R.id.in_email)

        pickDate()

        time.setOnClickListener{
            pickTime()
        }

        val room : Spinner = findViewById(R.id.spinner)

        var spinnerFlag : String =""
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

        /*----------------------------------------------------------------------------------------------------------*/
        //WHEN THE GREEN BUTTON IS CLICKED SAVE TO DATABASE
        val proceed : ImageButton = findViewById(R.id.btn_add_defense)
        val cancel : ImageButton = findViewById(R.id.btn_cancel)

        proceed.setOnClickListener{view->
            val cols = listOf<String>(DefensesProvider._ID, DefensesProvider.ROOM, DefensesProvider.DATE, DefensesProvider.TIME).toTypedArray()

            var rs = contentResolver.query(
                DefensesProvider.CONTENT_URI,
                cols,
                "${cols[2]} = ? AND ${cols[3]} = ?",
                listOf<String>("$regDate", "$regHour").toTypedArray(),
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

            /*----------------------------------------------------------------------------------------------------------*/
            //GENERATE A LIST OF BOOKED ROOMS
            val bookedRooms = generateSequence { if (rs!!.moveToNext()) rs else null }
                .map { it.getString(1) }
                .toList()

            //CHECK IF THE ROOM IS ALREADY BOOKED
            val intersection = bookedRooms intersect listOf<String>(spinnerFlag)

            if(intersection.isNotEmpty()){
                Toast.makeText(this, "ROOM NOT AVAILABLE ON THIS TIME", Toast.LENGTH_LONG).show()
            }else {
                val values = ContentValues()
                values.put(DefensesProvider.STUDENT_NAME, studentName.text.toString())
                values.put(DefensesProvider.THESIS, thesisTitle.text.toString())
                values.put(DefensesProvider.COM_NAME_1, comName1.text.toString())
                values.put(DefensesProvider.COM_NAME_2, comName2.text.toString())
                values.put(DefensesProvider.COM_NAME_3, comName3.text.toString())
                values.put(DefensesProvider.ROOM, spinnerFlag)
                values.put(DefensesProvider.DATE, regDate)
                values.put(DefensesProvider.TIME, regHour)

                contentResolver.insert(DefensesProvider.CONTENT_URI, values)

            /*----------------------------------------------------------------------------------------------------------*/
                composeEmail(Array<String>(1){userEmail.text.toString()}, "Thesis Defense Confirmation", "" +
                        "Dear Mr. ${studentName.text.toString()},\n" +
                        "We hope this email finds you well,\n" +
                        "This is an automated email to confirm your reservation for the classroom $spinnerFlag " +
                        "On $regDate at $regHour for the defense of the thesis titled '${thesisTitle.text.toString()}'\n" +
                        "We wish you the best of luck, \n regards.")

                Toast.makeText(this, "ROOM BOOKED SUCCESSFULLY", Toast.LENGTH_LONG).show()
            }
            finish()
        }

        /*----------------------------------------------------------------------------------------------------------*/
        //WHEN THE RED BUTTON IS CLICKED RETURN TO MAIN
        cancel.setOnClickListener{
            finish()
        }
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
    private fun pickDate(){
        val buttonCalendar: ImageButton = findViewById(R.id.btn_calendar)
        buttonCalendar.setOnClickListener{
            getDateTimeCalendar()
            DatePickerDialog(this, this, year, month, day).show()
        }
    }

    var regDate:String = ""

    /*----------------------------------------------------------------------------------------------------------*/
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        inputDay = dayOfMonth
        inputMonth = month+1
        inputYear = year

        getDateTimeCalendar()

        //DATE OUTPUT
        val displayDate : TextView = findViewById(R.id.out_date2)
        displayDate.text = "$inputDay/$inputMonth/$inputYear"
        regDate = "$inputDay/$inputMonth/$inputYear"
    }

    /*----------------------------------------------------------------------------------------------------------*/
    var hour = 0
    var inputHour = 0
    var regHour:String=""

    /*----------------------------------------------------------------------------------------------------------*/
    private fun pickTime(){
        val cal= Calendar.getInstance()
        hour = cal.get(Calendar.HOUR_OF_DAY)
        TimePickerDialog(this, this, hour, 0, true).show()

    }

    /*----------------------------------------------------------------------------------------------------------*/
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        inputHour = hourOfDay
        regHour = "$inputHour"

        //TIME OUTPUT
        val displayTime : TextView = findViewById(R.id.out_time)
        displayTime.text = "$regHour:00"
    }

    /*----------------------------------------------------------------------------------------------------------*/
    private fun composeEmail(addresses: Array<String>, subject: String, body: String) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_EMAIL, addresses)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
    }
}
