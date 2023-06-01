package com.example.stripepoc

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.stripepoc.databinding.FragmentBlankBinding
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

class BlankFragment : Fragment() {
  companion object {
    private const val TAG = "MainActivity"
    // JM Backend: https://1209-124-123-80-141.ngrok-free.app/api/v3/payments
    private const val BACKEND_URL = "https://e29f-103-211-19-233.in.ngrok.io"
  }

  private lateinit var paymentIntentClientSecret: String
  private lateinit var paymentSheet: PaymentSheet

  private lateinit var binding: FragmentBlankBinding

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    binding = FragmentBlankBinding.inflate(inflater, container, false)

    PaymentConfiguration.init(
      requireContext(),
      "<PUBLISHABLE_API_KEY>"
    )

//    val googlePayLauncher = GooglePayLauncher(
//      activity = requireActivity(),
//      config = GooglePayLauncher.Config(
//        environment = GooglePayEnvironment.Test,
//        merchantCountryCode = "US",
//        merchantName = "Widget Store"
//      ),
//      readyCallback = ::onGooglePayReady,
//      resultCallback = ::onGooglePayResult
//    )
//
//    binding.googlePayButton.setOnClickListener {
//      // launch `GooglePayLauncher` to confirm a Payment Intent
//      googlePayLauncher.presentForPaymentIntent(paymentIntentClientSecret)
//    }

    binding.btnAddDetails.setOnClickListener {
      onAddDetailsClicked()
    }

    // Hook up the pay button
    binding.btnPay.setOnClickListener(::onPayClicked)
    binding.btnPay.isEnabled = false

    paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

    return binding.root
  }

//  private fun onGooglePayReady(isReady: Boolean) {
//    // implemented below
//  }
//
//  private fun onGooglePayResult(result: GooglePayLauncher.Result) {
//    // implemented below
//  }

  private fun onAddDetailsClicked() {
    val itemId = binding.etItemId.text.toString().trim()
    val amount = binding.etItemAmount.text.toString().trim()

    if(itemId.isEmpty() || amount.isEmpty()) {
      Toast.makeText(requireContext(), "Please enter item details", Toast.LENGTH_SHORT).show()
    } else {
      fetchPaymentIntent(itemId.toInt(), amount.toInt())
    }
  }

  private fun fetchPaymentIntent(itemId: Int, amount: Int) {
    val backendUrl = binding.etServerUrl.text.toString().trim()
    val url = if (backendUrl.length > 5) "$backendUrl/create_payment_intent" else "$BACKEND_URL/create_payment_intent"

    // val shoppingCartContent = """{"amount": "$amount", "currency": "usd"}"""
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
            paymentIntentClientSecret = responseJson.getString("client_secret")
            activity?.runOnUiThread {
              binding.btnPay.isEnabled = true
              binding.etItemId.text?.clear()
              binding.etItemAmount.text?.clear()
//              binding.googlePayButton.isEnabled = true
            }
            Log.i(TAG, "Retrieved PaymentIntent")
          }
        }
      })
  }

  private fun showAlert(title: String, message: String? = null) {
    activity?.runOnUiThread {
      val builder = AlertDialog.Builder(requireContext())
        .setTitle(title)
        .setMessage(message)
      builder.setPositiveButton("Ok", null)
      builder.create().show()
    }
  }

  private fun showToast(message: String) {
    activity?.runOnUiThread {
      Toast.makeText(requireContext(),  message, Toast.LENGTH_LONG).show()
    }
  }

  private fun onPayClicked(view: View) {
    val googlePayConfiguration = PaymentSheet.GooglePayConfiguration(
      environment = PaymentSheet.GooglePayConfiguration.Environment.Test,
      countryCode = "US",
      currencyCode = "USD" // Required for Setup Intents, optional for Payment Intents
    )

    val configuration = PaymentSheet.Configuration(
      merchantDisplayName = "Rohan, Inc.",
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