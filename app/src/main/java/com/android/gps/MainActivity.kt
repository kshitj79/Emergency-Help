package com.android.gps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var sharedPreferences: SharedPreferences
    private var isSendPermissionGranted = false
    private var isCallPermissionGranted = false
    private var isLocationPermissionGranted = false
    private var isReadContactsPermissionGranted = false
    private var isForegroundPermissionGranted = false
    private var isInternetPermissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nextPageButton = findViewById<Button>(R.id.nextpage)
        nextPageButton.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }


        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isInternetPermissionGranted = permissions[Manifest.permission.INTERNET]
                    ?: isInternetPermissionGranted
                isCallPermissionGranted = permissions[Manifest.permission.CALL_PHONE]
                    ?: isCallPermissionGranted
                isForegroundPermissionGranted = permissions[Manifest.permission.FOREGROUND_SERVICE]
                    ?: isForegroundPermissionGranted
                isSendPermissionGranted = permissions[Manifest.permission.SEND_SMS]
                    ?: isSendPermissionGranted
                isLocationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION]
                    ?: isLocationPermissionGranted
                isReadContactsPermissionGranted =
                    permissions[Manifest.permission.READ_CONTACTS]
                        ?: isReadContactsPermissionGranted
            }

        requestPermission()

        val mailButton = findViewById<Button>(R.id.mail)
        mailButton.setOnClickListener {
            val recipient = "kshitj79@gmail.com"
            val subject = "Regarding My Safety"
            val text = "Emergency! I need help."
            sendEmail(this, recipient, subject, text)
        }

        val smsButton = findViewById<Button>(R.id.sms)
        smsButton.setOnClickListener {
            val phoneNumbers = loadPhoneNumbers()
            Toast.makeText(this, "Fetching Location", Toast.LENGTH_SHORT).show()
            CoroutineScope(Dispatchers.Main).launch {
                val location = getLocationAsync().await()
                location?.let { loc ->
                    phoneNumbers.forEach { phoneNumber ->
                        Toast.makeText(
                            this@MainActivity,
                            "Sending emergency SMS to: $phoneNumber",
                            Toast.LENGTH_SHORT
                        ).show()
                        sendMessageWithLocation(this@MainActivity, phoneNumber, loc)
                    }
                    Toast.makeText(
                        this@MainActivity,
                        "Sent emergency SMS to all beneficiaries",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadPhoneNumbers(): Set<String> {
        return sharedPreferences.getStringSet("phoneNumbers", emptySet()) ?: emptySet()
    }

    private suspend fun getLocationAsync(): Deferred<Location?> = coroutineScope {
        async(Dispatchers.IO) { getLocation(this@MainActivity) }
    }

    private fun getLocation(context: Context): Location? {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return when {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ->
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ->
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                else -> null
            }
        }
        return null
    }

    private fun sendMessageWithLocation(context: Context, phoneNumber: String, location: Location) {
        val message = "Emergency! My current location is: https://www.google.com/maps/@${location.latitude},${location.longitude},15z"
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
    }

    private fun requestPermission() {
        val permissions = arrayOf(
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.INTERNET,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CONTACTS
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun sendEmail(context: Context, recipient: String, subject: String, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { location ->
                val locationText = "My current location: https://www.google.com/maps/@${location.latitude},${location.longitude},15z\n"
                intent.putExtra(Intent.EXTRA_TEXT, locationText + text)
            }
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "No app installed to send emails", Toast.LENGTH_SHORT).show()
        }
    }
}
