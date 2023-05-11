package com.example.stripepoc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.stripepoc.databinding.ActivityMainBinding
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
    private const val BACKEND_URL = "https://852b-103-211-19-38.ngrok.io"
  }

  private lateinit var paymentIntentClientSecret: String
  private lateinit var paymentSheet: PaymentSheet

  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    PaymentConfiguration.init(
      applicationContext,
      "pk_test_51N5r1uSHE8F93FeUlysWOJuwo276VkTvLBy4DoQmDq4uRhRD3jP4745MzLOIykQfgaK5yNdEBLHwW5BzZT4ZVnW700pUD6xclu"
    )

    binding.btnAddDetails.setOnClickListener {
      onAddDetailsClicked()
    }

    // Hook up the pay button
    binding.btnPay.setOnClickListener(::onPayClicked)
    binding.btnPay.isEnabled = false

    paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
  }

  private fun onAddDetailsClicked() {
    val itemId = binding.etItemId.text.toString().trim()
    val amount = binding.etItemAmount.text.toString().trim()

    if(itemId.isEmpty() || amount.isEmpty()) {
      Toast.makeText(this, "Please enter item details", Toast.LENGTH_SHORT).show()
    } else {
      fetchPaymentIntent(itemId.toInt(), amount.toInt())
    }
  }

  private fun fetchPaymentIntent(itemId: Int, amount: Int) {
    val backendUrl = binding.etServerUrl.text.toString().trim()
    val url = if (backendUrl.length > 5) "$backendUrl/create-payment-intent" else "$BACKEND_URL/create-payment-intent"

    val shoppingCartContent = """
            {
                "items": [
                    {"id":$itemId, "amount": $amount}
                ]
            }
        """

    val mediaType = "application/json; charset=utf-8".toMediaType()

    val body = shoppingCartContent.toRequestBody(mediaType)
    Log.d(TAG, "Request body: $body")
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
            runOnUiThread {
              binding.btnPay.isEnabled = true
              binding.etItemId.text?.clear()
              binding.etItemAmount.text?.clear()
            }
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
    val googlePayConfiguration = PaymentSheet.GooglePayConfiguration(
      environment = PaymentSheet.GooglePayConfiguration.Environment.Test,
      countryCode = "US",
    )

    val appearance = PaymentSheet.Appearance(
      colorsDark = PaymentSheet.Colors.defaultDark,
      shapes = PaymentSheet.Shapes.default
    )

    val configuration = PaymentSheet.Configuration(
      merchantDisplayName = "Rohan, Inc.",
      appearance = appearance,
      googlePay = googlePayConfiguration
    )

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