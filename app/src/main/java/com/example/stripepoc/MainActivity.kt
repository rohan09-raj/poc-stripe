package com.example.stripepoc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
  companion object {
    private const val TAG = "MainActivity"
    private const val BACKEND_URL = "https://18b9-103-211-19-38.ngrok.io"
  }

  private lateinit var paymentIntentClientSecret: String
  private lateinit var paymentSheet: PaymentSheet

  private lateinit var payButton: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    PaymentConfiguration.init(
      applicationContext,
      "TODO: API_KEY_HERE"
    )

    // Hook up the pay button
    payButton = findViewById(R.id.pay_button)
    payButton.setOnClickListener(::onPayClicked)
    payButton.isEnabled = false

    paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

    fetchPaymentIntent()
  }

  private fun fetchPaymentIntent() {
    val url = "$BACKEND_URL/create-payment-intent"

    val shoppingCartContent = """
            {
                "items": [
                    {"id":"xl-tshirt"}
                ]
            }
        """

    val mediaType = "application/json; charset=utf-8".toMediaType()

    val body = shoppingCartContent.toRequestBody(mediaType)
    val request = Request.Builder()
      .url(url)
      .post(body)
      .build()

    OkHttpClient()
      .newCall(request)
      .enqueue(object: Callback {
        override fun onFailure(call: Call, e: IOException) {
          showAlert("Failed to load data", "Error: $e")
        }

        override fun onResponse(call: Call, response: Response) {
          if (!response.isSuccessful) {
            showAlert("Failed to load page", "Error: $response")
          } else {
            val responseData = response.body?.string()
            val responseJson = responseData?.let { JSONObject(it) } ?: JSONObject()
            paymentIntentClientSecret = responseJson.getString("clientSecret")
            runOnUiThread { payButton.isEnabled = true }
            Log.i(TAG, "Retrieved PaymentIntent")
          }
        }
      })
  }

  private fun showAlert(title: String, message: String? = null) {
    runOnUiThread {
      val builder = AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
      builder.setPositiveButton("Ok", null)
      builder.create().show()
    }
  }

  private fun showToast(message: String) {
    runOnUiThread {
      Toast.makeText(this,  message, Toast.LENGTH_LONG).show()
    }
  }

  private fun onPayClicked(view: View) {
    val configuration = PaymentSheet.Configuration("Example, Inc.")

    // Present Payment Sheet
    paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration)
  }

  private fun onPaymentSheetResult(paymentResult: PaymentSheetResult) {
    when (paymentResult) {
      is PaymentSheetResult.Completed -> {
        showToast("Payment complete!")
      }

      is PaymentSheetResult.Canceled -> {
        Log.i(TAG, "Payment canceled!")
      }

      is PaymentSheetResult.Failed -> {
        showAlert("Payment failed", paymentResult.error.localizedMessage)
      }
    }
  }
}