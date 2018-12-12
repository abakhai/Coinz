package com.example.ami.coinz

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeActivity : AppCompatActivity(){

    private var tag = "HomeActivity"

    // Access a Cloud Firestore instance from your Activity
    private val db = FirebaseFirestore.getInstance()
    private var mAuth: FirebaseAuth? = null

    //Variable to use with json download
    private val baseUrl = "http://homepages.inf.ed.ac.uk/stg/coinz/"
    lateinit var updatedURL : String
    private val endOfUrl: String = "/coinzmap.geojson"

    //Variable for remembering the values
    private var downloadDate = ""
    private val preferencesFile = "MyPrefsFile" // for storing preferences

    //This variable is to set the mode of the game
    //Part of the bonus feature
    //That changes the distance the user has to be to the coin
    //Depending on the mode
    //This variable is an object as it needs to be passed to MainActivity
    object DataHome {
        var mode = "EASY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Getting the unique authenticator of the user
        mAuth = FirebaseAuth.getInstance()

        //Getting the current date
        val current = LocalDateTime.now()
        val formattedDate = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        val now = current.format(formattedDate)

        if (downloadDate != now) {
            //If the remembered download date is not the current date
            //then:

            //Let the remain coin, needed in WalletActivity, be 25
            WalletActivity.DataWallet.coinRemain = 25f
            //Set the download date to be the current date to remember
            downloadDate = now

            //Updating the geojson link
            val dateString = downloadDate
            updatedURL = baseUrl + dateString + endOfUrl
            //Getting the json
            val json = DownloadFileTask(DownloadCompleteRunner).execute(updatedURL).get()

            //Fetching the rate element of the Json
            val jsonObject = JSONObject(json)
            val ratesObject = jsonObject.getJSONObject("rates")
            MainActivity.Data.DOLR = ratesObject.getString("DOLR").toFloat()
            MainActivity.Data.SHIL = ratesObject.getString("SHIL").toFloat()
            MainActivity.Data.PENY = ratesObject.getString("PENY").toFloat()
            MainActivity.Data.QUID = ratesObject.getString("QUID").toFloat()
        }

        //Setting the BottomNavBar
        val bottomNavigation : BottomNavigationView = findViewById(R.id.navHBar)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        val menu = bottomNavigation.getMenu()
        val menuItem = menu.getItem(0)
        menuItem.setChecked(true)

        // get reference to button
        val btnHard : Button = findViewById(R.id.button_hard)
        // set on-click listener
        btnHard.setOnClickListener {
            Toast.makeText(this, "You clicked HARD", Toast.LENGTH_SHORT).show()
            DataHome.mode = "HARD"
        }
        // get reference to button
        val btnMedium : Button = findViewById(R.id.button_medium)
        // set on-click listener
        btnMedium.setOnClickListener {
            Toast.makeText(this, "You clicked MEDIUM", Toast.LENGTH_SHORT).show()
            DataHome.mode = "MEDIUM"
        }
        // get reference to button
        val btnEasy : Button = findViewById(R.id.button_easy)
        // set on-click listener
        btnEasy.setOnClickListener {
        Toast.makeText(this, "You clicked EASY", Toast.LENGTH_SHORT).show()
            DataHome.mode = "EASY"
        }


        val textView : TextView = findViewById(R.id.tv)
        //Setting the text from the strings.xml file for the welcome
        textView.text = resources.getString(R.string.welcome, DataHome.mode)

        val textv : TextView = findViewById(R.id.stepsLbl)
        //Setting the text from the strings.xml file for the steps
        textv.text = resources.getString(R.string.Steps, MainActivity.Data.steps)

        //fromFirebase() function fetches three things from the user's Firestore
        //The coins in the wallet
        //The coins that are destined to be transferred
        //The amount of Gold
        //And updating them for their respective activity and use
        fromFirebase()

    }



    private fun fromFirebase()   {

        MainActivity.Data.wallet.clear()
        //It makes sure that there is a unique user logged in
        if (mAuth?.currentUser == null) {
            val intentlog = Intent(this, FirebaseUIActivity::class.java)
            startActivity(intentlog)
        } else {
            Log.d(tag, "Someone is logged in" )
        }
        val userId = mAuth?.currentUser?.uid

        //Getting the wallet
        db.collection("User/$userId/Wallet/Coin/IDs")
                .get()
                .addOnSuccessListener { documents ->

                    for (document in documents) {
                        if (document != null) {
                            Log.d(tag, "DocumentSnapshot data: ${document.data}")
                        } else {
                            Log.d(tag, "No such document")
                        }
                        val value = document.get("value").toString()
                        val currency = document.get("currency").toString()
                        val id = document.get("id").toString()
                        val coord : List<Double> = listOf(0.0,0.0)

                        val coin = Coin(id, coord,currency,value)

                        MainActivity.Data.wallet.add(coin)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(tag, "get failed with ", exception)
                }
        //Getting the amount of Gold for the user
        db.collection("User/$userId/Bank").document("Gold")
                .get()
                .addOnSuccessListener { document ->

                        if (document != null) {
                            Log.d(tag, "DocumentSnapshot data: ${document.data}")
                        } else {
                            Log.d(tag, "No such document")
                        }

                        BankActivity.DataBank.gold = document.get("GOLD").toString()
                        Log.d(tag, "HomeActivity the gold ${BankActivity.DataBank.gold}")
                    }
                .addOnFailureListener { exception ->
                    Log.d(tag, "get failed with ", exception)
                }

        //Getting the coins that are suppose to be destined to be transferred
        TransferActivity.DataTrans.ListofCoins.clear()
        db.collection("User/$userId/Transfer/Coin/IDs")
                .get()
                .addOnSuccessListener { documents ->

                    for (document in documents) {
                        if (document != null) {
                            Timber.d(tag, "DocumentSnapshot data: ${document.data}")
                        } else {
                            Timber.d(tag, "No such document")
                        }
                        val value = document.get("value").toString()
                        val currency = document.get("currency").toString()
                        val id = document.get("id").toString()
                        val coord : List<Double> = listOf(0.0,0.0)

                        val coin = Coin(id, coord,currency,value)

                        TransferActivity.DataTrans.ListofCoins.add(coin)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d( tag,"get failed with ", exception)
                }

    }

    //Populating the BottomNavBar
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.homenav -> {
                val intent1 = Intent(this, HomeActivity::class.java)
                //Stopping the Activity to overlay itself if user pressed on the activity they are on
                intent1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent1)
                return@OnNavigationItemSelectedListener true
            }
            R.id.mapnav -> {
                val intent2 = Intent(this, MainActivity::class.java)
                startActivity(intent2)
                return@OnNavigationItemSelectedListener true
            }
            R.id.walletnav -> {
                val intent3 = Intent(this, WalletActivity::class.java)
                startActivity(intent3)
                return@OnNavigationItemSelectedListener true
            }
            R.id.banknav -> {
                val intent4 = Intent(this, BankActivity::class.java)
                startActivity(intent4)
                return@OnNavigationItemSelectedListener true
            }
            R.id.transfernav -> {
                val intent5 = Intent(this, TransferActivity::class.java)
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
        //When user clicks on "Log Out" they will be un-authenticated and brought to the sign in screen
        AuthUI.getInstance().signOut(this)
        val intentfire = Intent(this, FirebaseUIActivity::class.java)
        startActivity(intentfire)
    }

    override fun onStart() {
        super.onStart()

        //Restore preferences
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        //Checking if there is a user logged in, if not then go to the sign in screen
        if (mAuth?.currentUser == null) {
           val intentlog = Intent(this, FirebaseUIActivity::class.java)
            startActivity(intentlog)
        } else {
            Log.d(tag, "Someone is logged in" )
        }
        //Getting the saved download date
        downloadDate = settings.getString("lastDownloadDate", "")
        //Getting the rates from share preferences
        MainActivity.Data.SHIL  = settings.getFloat("SHIL", 1f)
        MainActivity.Data.DOLR  = settings.getFloat("DOLR", 1f)
        MainActivity.Data.PENY  = settings.getFloat("PENY", 1f)
        MainActivity.Data.QUID  = settings.getFloat("QUID", 1f)
    }

    override fun onStop() {
        super.onStop()
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        //We need an Editor object to make preferences changes
        val editor = settings.edit()
        //Remembering the download date to not download the json again
        editor.putString("lastDownloadDate", downloadDate)
        editor.apply()
    }




}



