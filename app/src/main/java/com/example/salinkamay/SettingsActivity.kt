package com.example.salinkamay

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.content.Intent


class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.settings_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        val howToUseOption = findViewById<LinearLayout>(R.id.how_to_use_option)
        howToUseOption.setOnClickListener {
            val intent = Intent(this, HowToUseActivity::class.java)
            startActivity(intent)
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val aboutAppOption = findViewById<LinearLayout>(R.id.about_salinkamay_option)
        val creatorsOption = findViewById<LinearLayout>(R.id.creators_option)


        aboutAppOption.setOnClickListener {
            showAboutDialog()
        }

        creatorsOption.setOnClickListener {
            showCreatorsDialog()
        }

    }

    private fun showAboutDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_about_us, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showCreatorsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_creators, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()
        dialog.show()
    }

    private fun refreshCamera() {

        Toast.makeText(this, "Camera Refreshed", Toast.LENGTH_SHORT).show()
    }

    private fun shutdownSystem() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Shutdown")
            .setMessage("Are you sure you want to shutdown?")
            .setPositiveButton("Yes") { _, _ ->
                Toast.makeText(this, "Shutting Down...", Toast.LENGTH_SHORT).show()

            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
