package com.example.flowride.utils

import android.content.Context
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

object ScannerUtils {
    /**
     * Real scanner using Google Code Scanner API via ML Kit.
     * This opens the camera and returns the QR content.
     */
    fun startScanner(context: Context, onResult: (String?) -> Unit) {
        val options = GmsBarcodeScannerOptions.Builder()
            .build()

        val scanner = GmsBarcodeScanning.getClient(context, options)

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                onResult(barcode.rawValue)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
}