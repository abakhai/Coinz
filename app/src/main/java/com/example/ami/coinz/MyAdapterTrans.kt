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
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class MyAdapterTrans(private val myDataset: ArrayList<Coin>) :
        RecyclerView.Adapter<MyAdapterTrans.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    val tag = "MyAdapterTrans"
    val db = FirebaseFirestore.getInstance()
    private var mAuth: FirebaseAuth? = null
    var hm = HashMap<String, Any>()
    private var coinList = ArrayList<Coin>()

    // Replace the contents of a view (invoked by the layout manager)


    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        val text = itemView.findViewById<TextView>(R.id.textView)
        val textcurr = itemView.findViewById<TextView>(R.id.textViewCurr)
        val textgold = itemView.findViewById<TextView>(R.id.textViewGold)
        val button1 = itemView.findViewById<Button>(R.id.buttonTrans)
        val edit = itemView.findViewById<EditText>(R.id.edit)


    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.listtrans_item, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        mAuth = FirebaseAuth.getInstance()
        var userId = mAuth?.currentUser?.uid as String
        var id = myDataset[position].id
        var idTrans = myDataset[position].id + "tran"

        Log.d(tag, "mainactivity transfer 66 ${MainActivity.Data.wallet.contains(myDataset[position])}  ${myDataset[position]}")


        if (TransferActivity.DataTrans.listofId.contains(myDataset[position].id)) {


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


        holder.button1.setOnClickListener {
            hm["currency"] = myDataset[position].curr
            hm["value"] = (myDataset[position].value).toFloat()
            hm["id"] = myDataset[position].id
            hm["coord"] = myDataset[position].coord
            db.document("User/${holder.edit.text.toString()}/Wallet/Coin/IDs/$idTrans").set(hm)
            db.collection("User/$userId/Wallet/Coin/IDs").document("$id")
                    .delete()
                    .addOnSuccessListener { Log.d(tag, "check DocumentSnapshot successfully deleted!") }
                    .addOnFailureListener { e -> Log.w(tag, "check Error deleting document", e) }
            db.collection("User/$userId/Transfer/Coin/IDs").document("$id")
                    .delete()
                    .addOnSuccessListener { Log.d(tag, "check DocumentSnapshot successfully deleted!") }
                    .addOnFailureListener { e -> Log.w(tag, "check Error deleting document", e) }

            MainActivity.Data.wallet.remove(myDataset[position])

            Log.d(tag, "listofIds transfer 107 ${TransferActivity.DataTrans.listofId}  ${myDataset[position].id}")
            TransferActivity.DataTrans.listofId.remove(myDataset[position].id)

            Log.d(tag, "listofIds transfer 108 ${TransferActivity.DataTrans.listofId}  ${myDataset[position].id}")

            coinList.clear()

            for (coin in MainActivity.Data.wallet) {

                Log.d(tag, "listofIds transfer 111 list of ids ${TransferActivity.DataTrans.listofId}  this coin id ${myDataset[position].id} the coin $coin the coin id ${coin.id}")
                if (!TransferActivity.DataTrans.listofId.contains(coin.id)) {
                    Log.d(tag, "mainactivity transfer before rem ${MainActivity.Data.wallet}  ${myDataset[position]}")
                    coinList.add(coin)
                    Log.d(tag, "mainactivity transfer after rem ${MainActivity.Data.wallet}  ${myDataset[position]}")
                }
                Log.d(tag, "listofIds transfer 114 ${TransferActivity.DataTrans.listofId}  ${myDataset[position].id}")
            }


            MainActivity.Data.wallet.removeAll(coinList)
            Log.d(tag, "mainactivity transfer 118 ${MainActivity.Data.wallet.contains(myDataset[position])}  ${myDataset[position]}")
            TransferActivity.DataTrans.ListofCoins.removeAt(holder.adapterPosition)
            notifyDataSetChanged()
        }


        } else {


            holder.textgold.text = ""
            holder.text.text = ""
            holder.textcurr.text = "You banked this coin"
            holder.button1.setOnClickListener {
                db.collection("User/$userId/Transfer/Coin/IDs").document("$id")
                        .delete()
                        .addOnSuccessListener { Log.d(tag, "check DocumentSnapshot successfully deleted!") }
                        .addOnFailureListener { e -> Log.w(tag, "check Error deleting document", e) }

                TransferActivity.DataTrans.ListofCoins.removeAt(holder.adapterPosition)
                notifyDataSetChanged()

            }

        }
    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}