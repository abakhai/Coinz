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
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mapbox.android.core.permissions.PermissionsManager

class WalletActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    val db = FirebaseFirestore.getInstance()
    val tag = "Wallet Activity"
    private var mAuth: FirebaseAuth? = null
    var hm = HashMap<String, Any>()
    var dataset = ArrayList<Coin>()

    object DataWallet {
        var coinRemain = 25f
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)
        mAuth = FirebaseAuth.getInstance()

        val bottomNavigation : BottomNavigationView = findViewById(R.id.navWBar)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        var menu = bottomNavigation.getMenu()
        var menuItem = menu.getItem(2)
        menuItem.setChecked(true)



        viewManager = LinearLayoutManager(this)



        var data = MainActivity.Data.wallet
        Log.d(tag," check 48 ${dataset}")
        Log.d(tag," check 49 ${data}")
        viewAdapter = MyAdapter(data)
        //viewAdapter.notifyDataSetChanged()


        recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter }


        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener{ view ->
            Snackbar.make(view, getString(R.string.remaining, DataWallet.coinRemain.toInt()), Snackbar.LENGTH_LONG)
                    .setAction("tAction", null)
                    .show()
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
                intent3.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
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



    @SuppressWarnings("MissingPermission")
    override fun onStart() {
        super.onStart()

        if (mAuth?.currentUser == null) {
            var intentlog = Intent(this, FirebaseUIActivity::class.java)
            startActivity(intentlog)
        } else {

            Log.d(tag, "Someone is logged in" )
        }
        val settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE)
        DataWallet.coinRemain = settings.getFloat("Remain", 25f)
        MyAdapter.DataAdapt.gold = settings.getFloat("GOLD", 0f)


    }

    override fun onStop() {
        super.onStop()

        //All objects are from android.context.Context

        val settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE)

        //We need an Editor object to make preferences changes
        val editor = settings.edit()
        editor.putFloat("Remain", DataWallet.coinRemain)
        editor.putFloat("GOLD",MyAdapter.DataAdapt.gold)


        editor.apply()

    }




}


