package com.example.ami.coinz

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyAdapter(private val myDataset: ArrayList<Coin>) :
        RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    private val tag = "MyAdapter"

    //Varibales for firebase
    private val db = FirebaseFirestore.getInstance()
    private var mAuth: FirebaseAuth? = null
    //Hashmap needed to send info to firebase
    var hm = HashMap<String, Any>()
    var goldhm = HashMap<String, Any>()

    //The position variable is to get the position of the coin
    //Gold is the get the amount of gold that the coin has and add it to the bank gold
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

        //Variables for getting the information for the recycler view from firebase
        mAuth = FirebaseAuth.getInstance()
        val userId = mAuth?.currentUser?.uid as String
        val id = myDataset[position].id

        //Assigning the holder for the coin elements
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

        //If user clicks on Transfer button for the coin then it will send that coin
        //To a new location in the firebase
        holder.button1.setOnClickListener {
            hm["currency"] = myDataset[position].curr
            hm["value"] = ( myDataset[position].value).toFloat()
            hm["id"] =  myDataset[position].id
            hm["coord"] =  myDataset[position].coord
            db.document("User/$userId/Transfer/Coin/IDs/$id").set(hm)
        }


        if (WalletActivity.DataWallet.coinRemain > 0) {
            //If the remaining bank transfer is higher then zero
            //then the user can click on the bank button
            holder.button.setOnClickListener {
                //The remaining bank transfer will decrease
                WalletActivity.DataWallet.coinRemain = WalletActivity.DataWallet.coinRemain - 1
                //Putting the gold rate in a hashmap to store it in firebase
                hm["GOLD"] = rates.toString()
                db.document("User/$userId/Bank/Coin/IDs/$id").set(hm)
                //Delete the coin from the wallet
                db.collection("User/$userId/Wallet/Coin/IDs").document(id)
                        .delete()
                        .addOnSuccessListener { Log.d(tag, "check DocumentSnapshot successfully deleted!") }
                        .addOnFailureListener { e -> Log.w(tag, "check Error deleting document", e) }
                //Delete the coin from the transfer screen
                db.collection("User/$userId/Transfer/Coin/IDs").document(id)
                        .delete()
                        .addOnSuccessListener { Log.d(tag, "check DocumentSnapshot successfully deleted!") }
                        .addOnFailureListener { e -> Log.w(tag, "check Error deleting document", e) }

                //update the amount of gold and set it in firebase
                db.collection("User/$userId/Bank").document("Gold")
                        .delete()
                        .addOnSuccessListener { Log.d(tag, "check DocumentSnapshot successfully deleted!") }
                        .addOnFailureListener { e -> Log.w(tag, "check Error deleting document", e) }
                DataAdapt.gold = rates + DataAdapt.gold
                goldhm["GOLD"] = DataAdapt.gold
                BankActivity.DataBank.gold = DataAdapt.gold.toString()
                db.document("User/$userId/Bank/Gold").set(goldhm)

                //update the variable containing the coins availabe
                TransferActivity.DataTrans.ListofCoins.remove(myDataset[position])
                DataAdapt.position = holder.adapterPosition
                MainActivity.Data.wallet.removeAt(DataAdapt.position)
                notifyDataSetChanged()
            }
        }
        //Assigning the position of the coordinates to be zero so comparisons are easier
        myDataset[position].coord = listOf(0.0,0.0)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}