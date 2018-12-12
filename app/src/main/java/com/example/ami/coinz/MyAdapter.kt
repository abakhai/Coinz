package com.example.ami.coinz

import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class MyAdapter(private val myDataset: ArrayList<Coin>) :
        RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    val tag = "MyAdapter"
    val db = FirebaseFirestore.getInstance()
    private var mAuth: FirebaseAuth? = null
    var hm = HashMap<String, Any>()
    var goldhm = HashMap<String, Any>()
    object DataAdapt {
        var position = -1
        var gold = 0f
    }

    // Replace the contents of a view (invoked by the layout manager)


    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        val text = itemView.findViewById<TextView>(R.id.textView)
        val textcurr = itemView.findViewById<TextView>(R.id.textViewCurr)
        val textgold = itemView.findViewById<TextView>(R.id.textViewGold)


        val button = itemView.findViewById<Button>(R.id.buttonBank)
        val button1 = itemView.findViewById<Button>(R.id.buttonTrans)


    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        mAuth = FirebaseAuth.getInstance()
        var userId = mAuth?.currentUser?.uid as String
        var id = myDataset[position].id


        holder.text.text = myDataset[position].value
        holder.textcurr.text = myDataset[position].curr
        var rates = myDataset[position].value.toFloat() * MainActivity.Data.QUID
        var c = MainActivity.Data.QUID
        if (myDataset[position].curr == "DOLR") {
            rates = myDataset[position].value.toFloat() * MainActivity.Data.DOLR
            c = MainActivity.Data.DOLR
        } else if (myDataset[position].curr == "PENY") {
            rates = myDataset[position].value.toFloat() * MainActivity.Data.PENY
            c = MainActivity.Data.PENY
        } else if (myDataset[position].curr == "SHIL") {
            rates = myDataset[position].value.toFloat() * MainActivity.Data.SHIL
            c = MainActivity.Data.SHIL
        }

        holder.textgold.text = rates.toString()
        var check = myDataset[position].value
        var ch = myDataset[position].curr
        Log.d(tag, "[onBindViewHolder] check value $check curr $ch rate $c ")

        holder.button1.setOnClickListener {
            Log.d(tag, "mainactivity transfer 94 ${MainActivity.Data.wallet.contains(myDataset[position])}")

            hm["currency"] = myDataset[position].curr
            hm["value"] = ( myDataset[position].value).toFloat()
            hm["id"] =  myDataset[position].id
            hm["coord"] =  myDataset[position].coord
            db.document("User/$userId/Transfer/Coin/IDs/$id").set(hm)
            Log.d(tag, "mainactivity transfer 101 ${MainActivity.Data.wallet.contains(myDataset[position])}")

        }

        if (WalletActivity.DataWallet.coinRemain > 0) {

            holder.button.setOnClickListener {
                WalletActivity.DataWallet.coinRemain = WalletActivity.DataWallet.coinRemain - 1

                hm["GOLD"] = rates.toString()
                db.document("User/$userId/Bank/Coin/IDs/$id").set(hm)

                db.collection("User/$userId/Wallet/Coin/IDs").document("$id")
                        .delete()
                        .addOnSuccessListener { Log.d(tag, "check DocumentSnapshot successfully deleted!") }
                        .addOnFailureListener { e -> Log.w(tag, "check Error deleting document", e) }


                db.collection("User/$userId/Transfer/Coin/IDs").document("$id")
                        .delete()
                        .addOnSuccessListener { Log.d(tag, "check DocumentSnapshot successfully deleted!") }
                        .addOnFailureListener { e -> Log.w(tag, "check Error deleting document", e) }



                db.collection("User/$userId/Bank").document("Gold")
                        .delete()
                        .addOnSuccessListener { Log.d(tag, "check DocumentSnapshot successfully deleted!") }
                        .addOnFailureListener { e -> Log.w(tag, "check Error deleting document", e) }

                DataAdapt.gold = rates + DataAdapt.gold
                goldhm["GOLD"] = DataAdapt.gold
                BankActivity.DataBank.gold = DataAdapt.gold.toString()
                db.document("User/$userId/Bank/Gold").set(goldhm)

                TransferActivity.DataTrans.ListofCoins.remove(myDataset[position])
                DataAdapt.position = holder.adapterPosition
                MainActivity.Data.wallet.removeAt(DataAdapt.position)
                notifyDataSetChanged()


            }
        }


        myDataset[position].coord = listOf(0.0,0.0)
        Log.d(tag, "mainactivity transfer 144 ${MainActivity.Data.wallet.contains(myDataset[position])} ${myDataset[position]}")

    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}