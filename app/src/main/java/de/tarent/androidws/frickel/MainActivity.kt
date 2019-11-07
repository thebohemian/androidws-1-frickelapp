package de.tarent.androidws.frickel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.component_activity_main)
    }

    companion object {

        val INTENT_ACTION_SCANNED_NAME = "scanned_name"

        val INTENT_EXTRA_NAME_KEY = "name"
    }
}
