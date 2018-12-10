package com.example.ami.coinz

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_firebase_ui.detail
import kotlinx.android.synthetic.main.activity_firebase_ui.signInButton
import kotlinx.android.synthetic.main.activity_firebase_ui.signOutButton
import kotlinx.android.synthetic.main.activity_firebase_ui.status

/**
 * Demonstrate authentication using the FirebaseUI-Android library. This activity demonstrates
 * using FirebaseUI for basic email/password sign in.
 *
 * For more information, visit https://github.com/firebase/firebaseui-android
 */
class FirebaseUIActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth
    private var tag = "FBActivity"
    // Access a Cloud Firestore instance from your Activity
    val db = FirebaseFirestore.getInstance()
    private var firestore: FirebaseFirestore? = null
    private var firestoreUser: DocumentReference? = null

    val hm = HashMap<String, Any>()

   // var userId = auth.uid.toString()
   // lateinit var users : ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firebase_ui)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()


        signInButton.setOnClickListener(this)
        signOutButton.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        updateUI(auth.currentUser)
    }


    private fun startSignIn() {
        // Build FirebaseUI sign in intent. For documentation on this operation and all
        // possible customization see: https://github.com/firebase/firebaseui-android
        val intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                .setAvailableProviders(listOf(AuthUI.IdpConfig.EmailBuilder().build()))
                .setLogo(R.mipmap.ic_launcher)
                .build()

        startActivityForResult(intent, RC_SIGN_IN)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // Signed in
            status.text = getString(R.string.firebaseui_status_fmt, user.email)
            detail.text = getString(R.string.id_fmt, user.uid)

            signInButton.visibility = View.GONE
            signOutButton.visibility = View.VISIBLE


            var intenthome = Intent(this, HomeActivity::class.java)
            startActivity(intenthome)

        } else {
            // Signed out
            status.setText(R.string.signed_out)
            detail.text = null

            signInButton.visibility = View.VISIBLE
            signOutButton.visibility = View.GONE
        }
    }

    private fun signOut() {
        AuthUI.getInstance().signOut(this)
        updateUI(null)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.signInButton -> startSignIn()
            R.id.signOutButton -> signOut()
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}