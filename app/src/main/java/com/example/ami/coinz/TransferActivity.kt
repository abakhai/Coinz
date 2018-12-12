package com.example.ami.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TransferActivity : AppCompatActivity() {

    val tag = "Trans Activity"

    //Variables for RecycleView
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    //Variables for Firestore
    val db = FirebaseFirestore.getInstance()
    //Variable for authentification
    private var mAuth: FirebaseAuth? = null

    //This object is to be able to transfer the list of coins and ids to other activities
    object DataTrans {
        var ListofCoins = ArrayList<Coin>()
        var listofId = ArrayList<String>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer)

        //This is clearing the list of Ids
        //Going through the coins in the wallet and adding
        //each Id of each coin the the listofId
        //This will be used to compare and remove the redundant ones in the recycler view
        DataTrans.listofId.clear()
        for (coin in MainActivity.Data.wallet) {
            DataTrans.listofId.add(coin.id)
        }

        //Setting the BottomNavBar
        val bottomNavigation : BottomNavigationView = findViewById(R.id.navTBar)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        val menu = bottomNavigation.getMenu()
        val menuItem = menu.getItem(4)
        menuItem.setChecked(true)

        //Getting the instance of the authentification
        mAuth = FirebaseAuth.getInstance()

        //This function fetches the coins
        //That were destined to get transferred from the wallet
        fromFirebaseTrans()


        //Populating the recycler View and setting it
        viewManager = LinearLayoutManager(this)
        val data = DataTrans.ListofCoins
        viewAdapter = MyAdapterTrans(data)
        viewAdapter.notifyDataSetChanged()

        recyclerView = findViewById<RecyclerView>(R.id.recyclerViewTrans).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            // use a linear layout manager
            layoutManager = viewManager
            // specify an viewAdapter (see also next example)
            adapter = viewAdapter }

        //Setting the TextView with the uid of the user
        val textView : TextView = findViewById(R.id.tvtrans)
        val uid = mAuth?.currentUser?.uid
        //Setting the text from the strings.xml file.
        textView.text = resources.getString(R.string.transuid, uid)


    }

    fun fromFirebaseTrans()   {

        val userId = mAuth?.currentUser?.uid.toString()
        //Fetching the the coins in Transfer collection from Firestore
        db.collection("User/$userId/Transfer/Coin/IDs")
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

                        if (!DataTrans.ListofCoins.contains(coin)) {
                            DataTrans.ListofCoins.add(coin)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(tag, "get failed with ", exception)
                }
    }



    //Populating and setting my BottomNavBar
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
                startActivity(intent4)
                return@OnNavigationItemSelectedListener true
            }
            R.id.transfernav -> {
                val intent5 = Intent(this, TransferActivity::class.java)
                //Stopping the Activity to overlay itself if user pressed on the activity they are on
                intent5.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent5)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

}
