package com.example.ami.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.util.Log
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BankActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()
    private var mAuth: FirebaseAuth? = null

   val tag = "Bank Activity"
    object DataBank {
        var gold = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank)

        mAuth = FirebaseAuth.getInstance()


        val bottomNavigation : BottomNavigationView = findViewById(R.id.navBBar)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        var menu = bottomNavigation.getMenu()
        var menuItem = menu.getItem(3)
        menuItem.setChecked(true)


        var textView : TextView = findViewById(R.id.banktv)

        //Setting the text from the strings.xml file.
        textView.text = resources.getString(R.string.bank, DataBank.gold)

    }

    fun FromFirebase() {

        var userId = mAuth?.currentUser?.uid

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


    }





    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.homenav -> {
                var intent1 = Intent(this, HomeActivity::class.java)
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
                intent4.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
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


    @SuppressWarnings("MissingPermission")
    override fun onStart() {
        super.onStart()

        if (mAuth?.currentUser == null) {
            var intentlog = Intent(this, FirebaseUIActivity::class.java)
            startActivity(intentlog)
        } else {

            Log.d(tag, "Someone is logged in" )
        }


    }

}
