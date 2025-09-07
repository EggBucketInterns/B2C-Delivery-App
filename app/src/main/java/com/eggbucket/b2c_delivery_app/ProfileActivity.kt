package com.eggbucket.b2c_delivery_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileActivity : AppCompatActivity() {

    private val db by lazy { Firebase.firestore }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.your_profile_layout) // replace with actual XML name

        val name    = findViewById<TextView>(R.id.tvPartnerName)
        val idView  = findViewById<TextView>(R.id.tvPartnerId)
        val phoneV  = findViewById<TextView>(R.id.tvPhoneValue)
        val dlV     = findViewById<TextView>(R.id.tvDlValue)
        val aadV    = findViewById<TextView>(R.id.tvAadhaarValue)

        // Open Edit page
        findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        // Back button (if needed)
        findViewById<ImageView>(R.id.aadharBackButton)?.setOnClickListener { finish() }

        // Live updates from Firestore (auto refresh on change)
        ensureSignedIn { user ->
            idView.text = "ID: ${user.uid.take(6)}"  // or show full UID
            db.collection("profiles").document(user.uid)
                .addSnapshotListener { snap, err ->
                    if (err != null || snap == null || !snap.exists()) return@addSnapshotListener
                    val p = snap.toObject(Profile::class.java) ?: return@addSnapshotListener
                    name.text  = listOf(p.firstName, p.lastName).filter { it.isNotBlank() }.joinToString(" ")
                    phoneV.text = p.phone
                    dlV.text    = p.drivingLicence
                    aadV.text   = p.aadhaar
                }
        }
    }
}
