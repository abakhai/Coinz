package com.example.ami.coinz

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyAdapterTrans(private val myDataset: ArrayList<Coin>) :
        RecyclerView.Adapter<MyAdapterTrans.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    val tag = "MyAdapterTrans"

    //Variables for firebase
    val db = FirebaseFirestore.getInstance()
    private var mAuth: FirebaseAuth? = null
    var hm = HashMap<String, Any>()

    //List of coins
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

        //Variable to get the firebase info for the user
        mAuth = FirebaseAuth.getInstance()
        val userId = mAuth?.currentUser?.uid as String
        val id = myDataset[position].id
        //changing the id of the transferred coin so duplicate coin can be added
        val idTrans = myDataset[position].id + "tran"

        Log.d(tag, "mainactivity transfer 66 ${MainActivity.Data.wallet.contains(myDataset[position])}  ${myDataset[position]}")


        if (TransferActivity.DataTrans.listofId.contains(myDataset[position].id)) {
            //if the transferred coin is in the wallet then populate it with the correct info

        holder.text.text = myDataset[position].value
        holder.textcurr.text = myDataset[position].curr
        var rates = myDataset[position].value.toFloat() * MainActivity.Data.QUID
        if (myDataset[position].curr == "DOLR") {
            rates = myDataset[position].value.toFloat() * MainActivity.Data.DOLR
        } else if (myDataset[position].curr == "PENY") {
            rates = myDataset[position].value.toFloat() * MainActivity.Data.PENY
        } else if (myDataset[position].curr == "SHIL") {
            rates = myDataset[position].value.toFloat() * MainActivity.Data.SHIL
        }

        holder.textgold.text = rates.toString()

            //At Transfer button clicked, send the coin to the user written in the EditText
            //Remove the coin from the destined to be transferred lists and wallet
        holder.button1.setOnClickListener {
            hm["currency"] = myDataset[position].curr
            hm["value"] = (myDataset[position].value).toFloat()
            hm["id"] = myDataset[position].id
            hm["coord"] = myDataset[position].coord
            db.document("User/${holder.edit.text}/Wallet/Coin/IDs/$idTrans").set(hm)
            db.collection("User/$userId/Wallet/Coin/IDs").document(id)
                    .delete()
                    .addOnSuccessListener { Log.d(tag, "check DocumentSnapshot successfully deleted!") }
                    .addOnFailureListener { e -> Log.w(tag, "check Error deleting document", e) }
            db.collection("User/$userId/Transfer/Coin/IDs").document(id)
                    .delete()
                    .addOnSuccessListener { Log.d(tag, "check DocumentSnapshot successfully deleted!") }
                    .addOnFailureListener { e -> Log.w(tag, "check Error deleting document", e) }

            MainActivity.Data.wallet.remove(myDataset[position])
            TransferActivity.DataTrans.listofId.remove(myDataset[position].id)
            coinList.clear()

            for (coin in MainActivity.Data.wallet) {
                //Because the value of the coins varied, I had to only compare the coins via id
                //And remove the coin that is transferred from the wallter
                if (!TransferActivity.DataTrans.listofId.contains(coin.id)) {
                    coinList.add(coin)
                }
            }
            MainActivity.Data.wallet.removeAll(coinList)
            TransferActivity.DataTrans.ListofCoins.removeAt(holder.adapterPosition)
            notifyDataSetChanged()
        }


        } else {
            //If the coin in the destined to be transferred list is not in the wallet
            // -> the coin was banked so display the coin is banked
            holder.textgold.text = ""
            holder.text.text = ""
            holder.textcurr.text = "You have banked this coin"
            //The coin should disappear from the Transfer screen, if not, if the user clicks the Transfer button the coin will disappear
            //So will it from the firebase
            holder.button1.setOnClickListener {
                db.collection("User/$userId/Transfer/Coin/IDs").document(id)
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