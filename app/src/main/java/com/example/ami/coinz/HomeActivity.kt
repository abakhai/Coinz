package com.example.ami.coinz

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.NavUtils
import android.support.v4.app.TaskStackBuilder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlinx.android.synthetic.main.activity_home.*
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeActivity : AppCompatActivity(){

    // Access a Cloud Firestore instance from your Activity
    val db = FirebaseFirestore.getInstance()
    private var mAuth: FirebaseAuth? = null
    private var tag = "HomeActivity"
    private var downloadDate = ""
    private val BASE_URL = "http://homepages.inf.ed.ac.uk/stg/coinz/"
    lateinit var updatedURL : String
    private val endOfUrl: String = "/coinzmap.geojson"
    private val preferencesFile = "MyPrefsFile" // for storing preferences
    var count = 0

    object DataHome {
        var mode = "EASY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        var current = LocalDateTime.now()
        var formattedDate = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        var now = current.format(formattedDate)

        if (downloadDate != now) {

            WalletActivity.DataWallet.coinRemain = 25f
            downloadDate = now
            var dateString = now

            updatedURL = BASE_URL + dateString + endOfUrl
            var json = DownloadFileTask(DownloadCompleteRunner).execute(updatedURL).get()

            var FeatureCollection = FeatureCollection.fromJson(json)
            var FeatureList = FeatureCollection.features()
            var jsonObject = JSONObject(json)
            var ratesObject = jsonObject.getJSONObject("rates")
            MainActivity.Data.DOLR = ratesObject.getString("DOLR").toFloat()
            MainActivity.Data.SHIL = ratesObject.getString("SHIL").toFloat()
            MainActivity.Data.PENY = ratesObject.getString("PENY").toFloat()
            MainActivity.Data.QUID = ratesObject.getString("QUID").toFloat()

        }

        mAuth = FirebaseAuth.getInstance()

        count = 1

        val bottomNavigation : BottomNavigationView = findViewById(R.id.navHBar)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        var menu = bottomNavigation.getMenu()
        var menuItem = menu.getItem(0)
        menuItem.setChecked(true)

        val btn_click : Button = findViewById(R.id.button3)
        // set on-click listener
        btn_click.setOnClickListener {
            Toast.makeText(this, "You clicked HARD", Toast.LENGTH_SHORT).show()
            DataHome.mode = "HARD"
        }

        // get reference to button
        val btn_click_me : Button = findViewById(R.id.button)
        // set on-click listener
        btn_click_me.setOnClickListener {
            Toast.makeText(this, "You clicked MEDIUM", Toast.LENGTH_SHORT).show()
            DataHome.mode = "MEDIUM"
        }

        val btn : Button = findViewById(R.id.button2)
        // set on-click listener
        btn.setOnClickListener {
        Toast.makeText(this, "You clicked EASY", Toast.LENGTH_SHORT).show()
            DataHome.mode = "EASY"
        }


       var textView : TextView = findViewById(R.id.tv)

        //Setting the text from the strings.xml file.
        textView.text = resources.getString(R.string.welcome, DataHome.mode)


        var textv : TextView = findViewById(R.id.stepsLbl)

        //Setting the text from the strings.xml file.
        textv.text = resources.getString(R.string.Steps, MainActivity.Data.steps)

        FromFirebase()

    }



    fun FromFirebase()   {

        MainActivity.Data.wallet.clear()

        if (mAuth?.currentUser == null) {
            var intentlog = Intent(this, FirebaseUIActivity::class.java)
            startActivity(intentlog)
        } else {

            Log.d(tag, "Someone is logged in" )
        }

        var userId = mAuth?.currentUser?.uid.toString()

        db.collection("User/$userId/Wallet/Coin/IDs")
                .get()
                .addOnSuccessListener { documents ->

                    for (document in documents) {
                        if (document != null) {
                            Log.d(tag, "DocumentSnapshot data: ${document.data}")
                        } else {
                            Log.d(tag, "No such document")
                        }
                        var value = document.get("value").toString()
                        var currency = document.get("currency").toString()
                        var id = document.get("id").toString()
                        var coord : List<Double> = listOf(0.0,0.0)

                        var Coin = Coin(id, coord,currency,value)

                        MainActivity.Data.wallet.add(Coin)

                    }


                }
                .addOnFailureListener { exception ->
                    Log.d(tag, "get failed with ", exception)

                }




        db.collection("User/$userId/Bank").document("Gold")
                .get()
                .addOnSuccessListener { document ->


                        if (document != null) {
                            Log.d(tag, "DocumentSnapshot data: ${document.data}")
                        } else {
                            Log.d(tag, "No such document")
                        }

                        BankActivity.DataBank.gold = document.get("GOLD").toString()
                        Log.d(tag, "HomeActivity l 200 the gold ${BankActivity.DataBank.gold}")
                    }


                .addOnFailureListener { exception ->
                    Log.d(tag, "get failed with ", exception)

                }
        TransferActivity.DataTrans.ListofCoins.clear()
        db.collection("User/$userId/Transfer/Coin/IDs")
                .get()
                .addOnSuccessListener { documents ->

                    for (document in documents) {
                        if (document != null) {
                            Log.d(tag, "DocumentSnapshot data: ${document.data}")
                        } else {
                            Log.d(tag, "No such document")
                        }
                        var value = document.get("value").toString()
                        var currency = document.get("currency").toString()
                        var id = document.get("id").toString()
                        var coord : List<Double> = listOf(0.0,0.0)

                        var Coin = Coin(id, coord,currency,value)

                        TransferActivity.DataTrans.ListofCoins.add(Coin)

                    }


                }
                .addOnFailureListener { exception ->
                    Log.d(tag, "get failed with ", exception)

                }

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

        downloadDate = settings.getString("lastDownloadDate", "")

        MainActivity.Data.SHIL  = settings.getFloat("SHIL", 1f)
        MainActivity.Data.DOLR  = settings.getFloat("DOLR", 1f)
        MainActivity.Data.PENY  = settings.getFloat("PENY", 1f)
        MainActivity.Data.QUID  = settings.getFloat("QUID", 1f)

    }

    override fun onStop() {
        super.onStop()


        //All objects are from android.context.Context

        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)

        //We need an Editor object to make preferences changes
        val editor = settings.edit()
        editor.putString("lastDownloadDate", downloadDate)

    }




}



