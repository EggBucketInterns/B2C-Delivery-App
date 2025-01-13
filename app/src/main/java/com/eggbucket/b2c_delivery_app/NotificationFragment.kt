package com.eggbucket.b2c_delivery_app

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException


class NotificationFragment : Fragment() {

    private var columnCount = 1
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)

        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        // Load notifications from SharedPreferences
        val notificationList = loadNotifications()

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = MyNotificationRecyclerViewAdapter(notificationList.toMutableList(),
                    onAccept = { notification ->
                        acceptOrder("0987654321", notification)
                        Toast.makeText(context, "Accepted: ${notification.orderId}", Toast.LENGTH_SHORT).show()

                    },
                    onDelete = { notification ->
                        deleteNotification(notification)
                        Toast.makeText(context, "Deleted: ${notification.orderId}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
        return view
    }

    private fun loadNotifications(): List<NotificationEntity> {
        val gson = Gson()
        val notificationsJson = sharedPreferences.getString("notifications_list", null)
        val type = object : TypeToken<MutableList<NotificationEntity>>() {}.type
        return if (notificationsJson != null) {
            gson.fromJson(notificationsJson, type)
        } else {
            emptyList()
        }
    }
    private fun acceptOrder(phone: String, data:NotificationEntity) {



            // Build the URL dynamically
            val url = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/markorderdelivered/$phone/${data.orderId}"

            // Create OkHttpClient
            val client = OkHttpClient()

            // Build the request
            val request = Request.Builder()
                .url(url)
                .post(RequestBody.create(null, "")) // Empty body for POST request
                .build()

            // Make the API call
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle failure
                    println("API call failed: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    // Handle success
                    if (response.isSuccessful) {
                        println("API call successful: ${response.body?.string()}")
                        deleteNotification(data)

                    } else {
                        Toast.makeText(context, "${response.body.toString()}", Toast.LENGTH_SHORT).show()

                        Log.d("failed accepting order",response.body.toString())
                    }
                }
            })
        }


        private fun deleteNotification(notification: NotificationEntity) {
        val gson = Gson()
        val notificationsJson = sharedPreferences.getString("notifications_list", null)
        val type = object : TypeToken<MutableList<NotificationEntity>>() {}.type
        val notificationsList: MutableList<NotificationEntity> = if (notificationsJson != null) {
            gson.fromJson(notificationsJson, type)
        } else {
            mutableListOf()
        }

        notificationsList.remove(notification)

        // Save the updated list back to SharedPreferences
        val updatedNotificationsJson = gson.toJson(notificationsList)
        sharedPreferences.edit().putString("notifications_list", updatedNotificationsJson).apply()
    }

    companion object {
        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            NotificationFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}
