package com.example.ami.coinz

import android.content.Context
import android.content.Intent
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.NavUtils
import android.support.v4.app.TaskStackBuilder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    // Access a Cloud Firestore instance from your Activity
    val db = FirebaseFirestore.getInstance()
    private var mAuth: FirebaseAuth? = null
    private var tag = "HomeActivity"
    private val preferencesFile = "MyPrefsFile" // for storing preferences
    private var firestore: FirebaseFirestore? = null
    private var firestoreUser: DocumentReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        mAuth = FirebaseAuth.getInstance()


        val bottomNavigation : BottomNavigationView = findViewById(R.id.navHBar)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        var menu = bottomNavigation.getMenu()
        var menuItem = menu.getItem(0)
        menuItem.setChecked(true)


       // var textView : TextView = findViewById(R.id.tv)
        //var textView1 : TextView = findViewById(R.id.tv1)
        //var textView2 : TextView = findViewById(R.id.tv2)
        //var textView3: TextView = findViewById(R.id.tv3)
        //var textView4 : TextView = findViewById(R.id.tv4)
        //var textView5 : TextView = findViewById(R.id.tv5)

        //Setting the text from the strings.xml file.
        //textView.text = resources.getString(R.string.wallet, MainActivity.Data.wallet.size)
        //textView1.text = resources.getString(R.string.bank, 0)
        //textView2.text = resources.getString(R.string.colorD)
        //textView3.text = resources.getString(R.string.colorS)
        //textView4.text = resources.getString(R.string.colorP)
        //textView5.text = resources.getString(R.string.colorQ)








    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.homenav -> {
                var intent1 = Intent(this, HomeActivity::class.java)
                intent1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent1)
                return@OnNavigationItemSelectedListener true
            }
            R.id.mapnav -> {
                var intent2 = Intent(this, MainActivity::class.java)
                startActivity(intent2)
                return@OnNavigationItemSelectedListener true
            }
            R.id.walletnav -> {
                var intent3 = Intent(this, WalletActivity::class.java)
                startActivity(intent3)
                return@OnNavigationItemSelectedListener true
            }
            R.id.banknav -> {
                var intent4 = Intent(this, BankActivity::class.java)
                startActivity(intent4)
                return@OnNavigationItemSelectedListener true
            }
            R.id.transfernav -> {
                var intent5 = Intent(this, TransferActivity::class.java)
                startActivity(intent5)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem):  Boolean{
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.logout -> {
                signOut()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun signOut() {
        AuthUI.getInstance().signOut(this)
        var intentfire = Intent(this, FirebaseUIActivity::class.java)
        startActivity(intentfire)
    }

    override fun onStart() {
        super.onStart()
        //Restore preferences
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        if (mAuth?.currentUser == null) {
            var intentlog = Intent(this, FirebaseUIActivity::class.java)
            startActivity(intentlog)
        } else {

            Log.d(tag, "Someone is logged in" )
        }

        val gson = Gson()
        val jsonwallet = settings.getString("Wallet", "")
        val type = object : TypeToken<List<Coin>>() {}.type
        if (!jsonwallet.isEmpty()) {
            var walet: List<Coin> = gson.fromJson<List<Coin>>(jsonwallet, type)
            MainActivity.Data.wallet = ArrayList(walet)
        }
        MainActivity.Data.SHIL  = settings.getFloat("SHIL", 1f)
        MainActivity.Data.DOLR  = settings.getFloat("DOLR", 1f)
        MainActivity.Data.PENY  = settings.getFloat("PENY", 1f)
        MainActivity.Data.QUID  = settings.getFloat("QUID", 1f)

    }



}
