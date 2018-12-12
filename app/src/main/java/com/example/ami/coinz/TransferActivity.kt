package com.example.ami.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TransferActivity : AppCompatActivity() {



    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    val db = FirebaseFirestore.getInstance()
    val tag = "Trans Activity"
    private var mAuth: FirebaseAuth? = null
    var hm = HashMap<String, Any>()
    var dataset = ArrayList<Coin>()

    object DataTrans {
        var transfer = "TRANSFER"
        var ListofCoins = ArrayList<Coin>()
        var listofId = ArrayList<String>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(tag, "Boooo 34" )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer)

        DataTrans.listofId.clear()
        for (coin in MainActivity.Data.wallet) {
            DataTrans.listofId.add(coin.id)
        }

        val bottomNavigation : BottomNavigationView = findViewById(R.id.navTBar)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        var menu = bottomNavigation.getMenu()
        var menuItem = menu.getItem(4)
        menuItem.setChecked(true)

        mAuth = FirebaseAuth.getInstance()

        Log.d(tag, "Boooo 43" )
        FromFirebaseTrans()
        Log.d(tag, "Boooo 45" )
        viewManager = LinearLayoutManager(this)


        var data = DataTrans.ListofCoins
        Log.d(tag, "Boooo 49 ${data.size}" )
        viewAdapter = MyAdapterTrans(data)
        viewAdapter.notifyDataSetChanged()

        Log.d(tag, "Boooo 52" )

        recyclerView = findViewById<RecyclerView>(R.id.recyclerViewTrans).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter }

        Log.d(tag, "Boooo 64" )

        var textView : TextView = findViewById(R.id.tvtrans)
        var uid = mAuth?.currentUser?.uid
        //Setting the text from the strings.xml file.
        textView.text = resources.getString(R.string.transuid, uid)


    }

    fun FromFirebaseTrans()   {



        var userId = mAuth?.currentUser?.uid.toString()

        db.collection("User/$userId/Transfer/Coin/IDs")
                .get()
                .addOnSuccessListener { documents ->
                    Log.d(tag, "Boooo 77" )
                    Log.d(tag, "Boooo 78 ${documents.isEmpty}" )
                    for (document in documents) {
                        if (document != null) {
                            Log.d(tag, "DocumentSnapshot data: ${document.data}")
                        } else {
                            Log.d(tag, "No such document")
                        }
                        Log.d(tag, "Boooo 84" )
                        var value = document.get("value").toString()
                        var currency = document.get("currency").toString()
                        var id = document.get("id").toString()
                        var coord : List<Double> = listOf(0.0,0.0)

                        var Coin = Coin(id, coord,currency,value)

                        if (!DataTrans.ListofCoins.contains(Coin)) {
                            DataTrans.ListofCoins.add(Coin)
                        }
                        Log.d(tag, "Boooo 93 ${DataTrans.ListofCoins}" )
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
                intent5.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent5)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

}
