package com.android.gps

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat


class MainActivity2 : AppCompatActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isSendPermissionGranted = false
    private var isCallPermissionGranted = false
    private var isLocationPermissionGranted = false
    private var isReadContactsPermissionGranted = false
    private var isForegroundPermissionGranted = false
    private var isInternetPermissionGranted = false





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)



        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

                isInternetPermissionGranted = permissions[Manifest.permission.INTERNET]
                    ?: isInternetPermissionGranted
                isCallPermissionGranted = permissions[Manifest.permission.CALL_PHONE]
                    ?: isInternetPermissionGranted
                isForegroundPermissionGranted = permissions[Manifest.permission.FOREGROUND_SERVICE]
                    ?: isForegroundPermissionGranted
                isSendPermissionGranted = permissions[Manifest.permission.SEND_SMS]
                    ?: isSendPermissionGranted
                isLocationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION]
                    ?: isLocationPermissionGranted
                isReadContactsPermissionGranted =
                    permissions[Manifest.permission.READ_CONTACTS] ?: isReadContactsPermissionGranted

            }

        requestPermission()

        val mail = findViewById<Button>(R.id.mail)
        mail.setOnClickListener {
            val recipient = "kshitj79@gmail.com"
            val subject = "Regarding My Safety"
            val text = "Emergency I need Help from you, Please help me   "
            sendEmail(this, recipient, subject, text)
        }

        val locate = findViewById<Button>(R.id.locate)
        locate.setOnClickListener {

            sendEmergencySMS(this,"+918726099839")
            Toast.makeText(this, "Sending SMS to Beneficiary", Toast.LENGTH_SHORT).show()

        }

        val call = findViewById<Button>(R.id.call)
        call.setOnClickListener {
            val emergencyNumber = "+918726099839" // Replace with your region's emergency number
            callEmergencyNumber(this, emergencyNumber)

            Toast.makeText(this, "Calling your Phone", Toast.LENGTH_SHORT).show()

        }


    }

    fun callEmergencyNumber(context: Context, emergencyNumber: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$emergencyNumber"))
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "No app installed to handle calls", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Handle permission not granted case (already shown in previous response)
        }
    }

    fun sendEmail(context: Context, recipient: String, subject: String, text: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822" // MIME type for email
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient)) // Recipient email address
        intent.putExtra(Intent.EXTRA_SUBJECT, subject) // Email subject
        intent.putExtra(Intent.EXTRA_TEXT, text) // Email body text
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasLocationPermission) {
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                val locationText =
                    "My current location: https://www.google.com/maps/@$latitude,$longitude,15z\n"
                val newText = locationText + text // Prepend location text to existing text
                intent.putExtra(Intent.EXTRA_TEXT, newText) // Update email body with location
            }


            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "No app installed to send emails", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }




    private fun requestPermission() {

        isForegroundPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.FOREGROUND_SERVICE
        ) == PackageManager.PERMISSION_GRANTED

        isCallPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        isInternetPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.INTERNET
        ) == PackageManager.PERMISSION_GRANTED

        isSendPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED


        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED


        isReadContactsPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        val permissionRequest: MutableList<String> = ArrayList()


        if (!isSendPermissionGranted) {
            permissionRequest.add(Manifest.permission.SEND_SMS)
        }

        if (!isCallPermissionGranted) {
            permissionRequest.add(Manifest.permission.CALL_PHONE)
        }

        if (!isForegroundPermissionGranted) {
            permissionRequest.add(Manifest.permission.FOREGROUND_SERVICE)
        }

        if (!isInternetPermissionGranted) {
            permissionRequest.add(Manifest.permission.INTERNET)
        }

        if (!isLocationPermissionGranted) {
            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (!isReadContactsPermissionGranted) {
            permissionRequest.add(Manifest.permission.READ_CONTACTS)
        }

        if (permissionRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }

    }



fun sendEmergencySMS(context: Context, phoneNumber: String) {
    // Check if the necessary permissions are granted


    // Get the location
    val locationManager = context.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
    var location: Location? = null

    // Try to get the last known location from GPS or network provider
    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    }

    // Prepare the SMS message
    val message = "Emergency! My current location is: https://www.google.com/maps/@${location?.latitude},${location?.longitude},15z"
    //https://www.google.com/maps/@${latLng.latitude},${latLng.longitude},15z

    // Send the SMS
    val smsManager = SmsManager.getDefault()
    smsManager.sendTextMessage(phoneNumber, null, message, null, null)
}
}