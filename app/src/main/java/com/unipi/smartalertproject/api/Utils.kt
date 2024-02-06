package com.unipi.smartalertproject.api

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.unipi.smartalertproject.R

class Utils {
    fun showSuccessMessage(text: String, duration: Int, context: Context){
        val toast = Toast.makeText(context, text, duration)
        toast.show()
    }

    fun showMessage(title: String, message: String, context: Context){
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        // TODO:  for alert icon
        val alert = alertDialog.create()
        alert.setCanceledOnTouchOutside(true)
        alert.show()
    }

    inline fun <reified T> convertStringToObject(jsonString: String): T? {
        return try {
            Gson().fromJson(jsonString, T::class.java)
        } catch (e: Exception) {
            // Handle exceptions, e.g., JSON parsing error
            e.printStackTrace()
            null
        }
    }

    fun mapToString(map :Map<String,List<String>>): String{
        var stringToReturn: StringBuilder = java.lang.StringBuilder()
        map.keys.forEach {
            stringToReturn.append("\n" + it + "\n\n")
            map[it]?.forEach { error -> stringToReturn.append("• $error\n") }
        }

        return stringToReturn.toString()
    }

    fun showScrollableDialog(context: Context, title: String, message: String){
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(
            R.layout.scrollable_dialog_content,
            null)

        view.findViewById<TextView>(R.id.textViewScrollableContent).text = message

        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)
        alertDialog.setTitle(title)
        alertDialog.setView(view)

        val alert: AlertDialog = alertDialog.create()
        alert.show()
    }

}