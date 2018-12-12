package com.example.ami.coinz

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth

class WalletActivity : AppCompatActivity() {

    private val tag = "Wallet Activity"

    //Instantiating the recycler view
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    // Variable for Firebase auth
    private var mAuth: FirebaseAuth? = null

    //Variable storing the 25 coin limit for bank transfers
    //And to be able to pass it through to another class
    object DataWallet {
        var coinRemain = 25f
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        //Getting the firebase user auth instance
        mAuth = FirebaseAuth.getInstance()

        //Setting the BottomNavBar
        val bottomNavigation : BottomNavigationView = findViewById(R.id.navWBar)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        val menu = bottomNavigation.getMenu()
        val menuItem = menu.getItem(2)
        menuItem.setChecked(true)


        //Setting and populating the recyclerView
        //with the data from the wallet from MainActivity
        viewManager = LinearLayoutManager(this)
        val data = MainActivity.Data.wallet
        viewAdapter = MyAdapter(data)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter }

        //Setting an info button on the screen
        //Which shows the remaining bank transfer possible for the day
        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener{ view ->
            Snackbar.make(view, getString(R.string.remaining, DataWallet.coinRemain.toInt()), Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show()
        }
    }

    //Populating the BottomNavBar
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.homenav -> {
                val intent1 = Intent(this, HomeActivity::class.java)
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
                //Stopping the Activity to overlay itself if user pressed on the activity they are on
                intent3.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
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



    @SuppressWarnings("MissingPermission")
    override fun onStart() {
        super.onStart()
        //Making sure that there is a unique user auth identification
        if (mAuth?.currentUser == null) {
            val intentlog = Intent(this, FirebaseUIActivity::class.java)
            startActivity(intentlog)
        } else {
            Log.d(tag, "Someone is logged in" )
        }
        val settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE)
        //Remembered amount of bank transfers
        DataWallet.coinRemain = settings.getFloat("Remain", 25f)
        //Remembered amount of Golf in the bank
        MyAdapter.DataAdapt.gold = settings.getFloat("GOLD", 0f)
    }

    override fun onStop() {
        super.onStop()

        val settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE)

        val editor = settings.edit()
        //Remembering the remain amount of bank transfers
        editor.putFloat("Remain", DataWallet.coinRemain)
        //Remembering the amount of Gold in the bank
        editor.putFloat("GOLD",MyAdapter.DataAdapt.gold)
        editor.apply()
    }

}


