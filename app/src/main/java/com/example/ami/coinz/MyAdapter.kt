package com.example.ami.coinz

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class MyAdapter(private val myDataset: ArrayList<Coin>) :
        RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    val tag = "MyAdapter"


    // Replace the contents of a view (invoked by the layout manager)


    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        val text = itemView.findViewById<TextView>(R.id.textView)
        val textcurr = itemView.findViewById<TextView>(R.id.textViewCurr)
        val textgold = itemView.findViewById<TextView>(R.id.textViewGold)
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
    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}