package com.phpthinky.browseropen

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.nativephp.mobile.bridge.BridgeFunction

/**
 * Browser open functions for launching URLs in the device's default browser
 * Namespace: "BrowserOpen.*"
 */
object BrowserOpenFunctions {

    /**
     * Open a URL in the device's default browser
     *
     * Parameters:
     *   - url: string - The URL to open (must include scheme, e.g. https://)
     *
     * Returns:
     *   - success: boolean - Whether the browser was launched successfully
     *   - error: string (optional) - Error message if operation failed
     */
    class Open(private val activity: FragmentActivity) : BridgeFunction {
        override fun execute(parameters: Map<String, Any>): Map<String, Any> {
            val url = parameters["url"] as? String

            Log.d("BrowserOpen.Open", "Opening URL: $url")

            if (url.isNullOrEmpty()) {
                return mapOf("success" to false, "error" to "'url' parameter is required")
            }

            return try {
                val uri = Uri.parse(url)

                // Reject non-http(s) schemes for safety
                val scheme = uri.scheme?.lowercase()
                if (scheme != "http" && scheme != "https") {
                    return mapOf("success" to false, "error" to "Only http and https URLs are supported")
                }

                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                activity.startActivity(intent)

                Log.d("BrowserOpen.Open", "Browser launched successfully for: $url")
                mapOf("success" to true)

            } catch (e: ActivityNotFoundException) {
                Log.e("BrowserOpen.Open", "No browser app found: ${e.message}", e)
                mapOf("success" to false, "error" to "No browser application found on this device")
            } catch (e: Exception) {
                Log.e("BrowserOpen.Open", "Error opening URL: ${e.message}", e)
                mapOf("success" to false, "error" to (e.message ?: "Unknown error"))
            }
        }
    }
}
