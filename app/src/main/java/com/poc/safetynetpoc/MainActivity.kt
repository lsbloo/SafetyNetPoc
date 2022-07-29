package com.poc.safetynetpoc

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafetyNet
import com.poc.safetynetpoc.Config.Companion.TAG
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.SecureRandom
import java.util.*
import android.util.Base64
import com.google.gson.Gson

class MainActivity : FragmentActivity() {


    private lateinit var btnCertificateGenerate: Button
    private lateinit var linearSharedCertificate: LinearLayout
    private lateinit var progressBar: ProgressBar
    private var result: String? = null

    private val mRandom: Random = SecureRandom()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnCertificateGenerate = findViewById(R.id.btn_certificate_generate)
        linearSharedCertificate = findViewById(R.id.linear_share_certificate)
        progressBar = findViewById(R.id.progressBar)

        btnCertificateGenerate.setBackgroundColor(Color.BLUE)

        btnCertificateGenerate.setOnClickListener {
            generateCertificate()
            progressBar.visibility = View.VISIBLE
        }

        linearSharedCertificate.setOnClickListener {
            shareCertificate()
        }
    }

    private fun extractJwsData(jws: String?): ByteArray? {
        // The format of a JWS is:
        // <Base64url encoded header>.<Base64url encoded JSON data>.<Base64url encoded signature>
        // Split the JWS into the 3 parts and return the JSON data part.
        val parts = jws?.split("[.]".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
        if (parts?.size != 3) {
            System.err.println(
                "Failure: Illegal JWS signature format. The JWS consists of "
                        + parts?.size + " parts instead of 3."
            )
            return null
        }
        return Base64.decode(parts[1], Base64.DEFAULT)
    }

    private fun shareCertificate() {
        result?.let { certificate ->

            val processedJws = String(extractJwsData(certificate)!!)
            val model = Gson().fromJson(processedJws, SafetyNetApiModel::class.java)
            Log.d("Result: ", "" + model.toString())
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, model.toString())
                type = "text/plain"
            }

            val sharedIntent = Intent.createChooser(intent, null)
            startActivity(sharedIntent)
        }

    }

    private fun generateCertificate() {
        if (GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS
        ) {
            sendSafetyNetRequest()
        } else {
            Toast.makeText(this, "Google Play Service don't available", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun sendSafetyNetRequest() {
        Log.i(TAG, "Sending SafetyNet API request.")

        val nonceData = "Safety Net Sample: " + System.currentTimeMillis()
        val nonce = getRequestNonce(nonceData)

        nonce?.let { data ->
            val safetyNetClient = SafetyNet.getClient(this)
            val taskRequest = safetyNetClient.attest(data, Config.API_KEY)

            taskRequest.addOnSuccessListener(this) { onSuccess ->
                Log.d(TAG, "Success! SafetyNet result:\n" + onSuccess.jwsResult + "\n")
                btnCertificateGenerate.setBackgroundColor(Color.GREEN)
                btnCertificateGenerate.text = "Certificado Gerado!"
                linearSharedCertificate.visibility = View.VISIBLE
                btnCertificateGenerate.setTextColor(Color.BLACK)
                progressBar.visibility = View.GONE
                result = onSuccess.jwsResult
            }

            taskRequest.addOnFailureListener(this) { onFailure ->

                if (onFailure is ApiException) {
                    (onFailure).let {
                        Log.d(
                            TAG, "Error: " +
                                    CommonStatusCodes.getStatusCodeString(it.statusCode) + ": " +
                                    it.message
                        )
                    }
                    progressBar.visibility = View.GONE

                } else {
                    Log.d(TAG, "ERROR! " + onFailure.message)
                    progressBar.visibility = View.GONE
                }
            }
        }


    }


    private fun getRequestNonce(data: String): ByteArray? {
        val byteStream = ByteArrayOutputStream()
        val bytes = ByteArray(24)
        mRandom.nextBytes(bytes)
        try {
            byteStream.write(bytes)
            byteStream.write(data.toByteArray())
        } catch (e: IOException) {
            return null
        }
        return byteStream.toByteArray()
    }

}