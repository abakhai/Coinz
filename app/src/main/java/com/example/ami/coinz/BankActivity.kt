package com.example.ami.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.util.Log
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber


class BankActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    private val tag = "Bank Activity"
    //Variable holding the amount of Gold the user has got
    object DataBank {
        var gold = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank)

        //Getting the Auth instance
        mAuth = FirebaseAuth.getInstance()

        //Setting my BottomNavBar
        val bottomNavigation : BottomNavigationView = findViewById(R.id.navBBar)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        val menu = bottomNavigation.getMenu()
        val menuItem = menu.getItem(3)
        menuItem.setChecked(true)

        //Seeting the TextView
        val textView : TextView = findViewById(R.id.banktv)
        //Setting the text from the strings.xml file.
        textView.text = resources.getString(R.string.bank, DataBank.gold)
    }

    //Populating the BottomNavVar
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
                startActivity(intent3)
                return@OnNavigationItemSelectedListener true
            }
            R.id.banknav -> {
                val intent4 = Intent(this, BankActivity::class.java)
                //Stopping the Activity to overlay itself if user pressed on the activity they are on
                intent4.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
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
        if (mAuth?.currentUser == null) {
            val intentlog = Intent(this, FirebaseUIActivity::class.java)
            startActivity(intentlog)
        } else {

            Log.d(tag, "Someone is logged in" )
        }
    }

}
