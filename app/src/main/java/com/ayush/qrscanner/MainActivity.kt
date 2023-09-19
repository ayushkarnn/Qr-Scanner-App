package com.ayush.qrscanner

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView

class MainActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 200
    }

    private lateinit var scannerView: ZXingScannerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scanButton = findViewById<Button>(R.id.scanButton)
        scanButton.setOnClickListener { startScanner() }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (::scannerView.isInitialized) {
                scannerView.setResultHandler(this)
                scannerView.startCamera()
            }
        } else {
            requestCameraPermission()
        }
    }

    override fun onPause() {
        super.onPause()
        scannerView.stopCamera()
    }

    override fun handleResult(rawResult: Result?) {
        val resultText = rawResult?.text ?: ""

        if (resultText.startsWith("http://") || resultText.startsWith("https://")) {

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resultText))
            startActivity(intent)
            Toast.makeText(this, "Opening URL: $resultText", Toast.LENGTH_SHORT).show()
        } else {

            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Scanned Text")
                .setMessage(resultText)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    scannerView.resumeCameraPreview(this)
                }
                .create()

            alertDialog.show()
        }
    }

    private fun startScanner() {
        scannerView = ZXingScannerView(this)
        scannerView.setResultHandler(this)

        setContentView(scannerView)
        scannerView.startCamera()
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanner()
            } else {
                Toast.makeText(this@MainActivity,"Permission Denied",Toast.LENGTH_SHORT).show()
            }
        }
    }
}
