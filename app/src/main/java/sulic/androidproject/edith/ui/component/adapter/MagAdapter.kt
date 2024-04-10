package sulic.androidproject.edithandroidapp2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import sulic.androidproject.edith.R
import sulic.androidproject.edith.ui.component.StreamTextView

class Msg(val content: String, val type: Int) {
    val stamp = System.currentTimeMillis()
    companion object {
        const val TYPE_RECEIVED: Int = 0
        const val TYPE_SEND: Int = 1
    }
}

class MsgAdapter(private val list: List<Msg>) : RecyclerView.Adapter<MsgAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var leftLayout: LinearLayout = view.findViewById(R.id.left_layout)
        var leftMsg: StreamTextView = view.findViewById(R.id.left_msg)
        var rightLayout: LinearLayout = view.findViewById(R.id.right_layout)
        var rightMsg: StreamTextView = view.findViewById(R.id.right_msg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.msg_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val msg: Msg = list[position]
        if (msg.type == Msg.TYPE_RECEIVED) {
            holder.leftLayout.visibility = View.VISIBLE
            if(msg.stamp > System.currentTimeMillis() - 2000)
                holder.leftMsg.display(msg.content)
            else
                holder.leftMsg.text = msg.content
            holder.rightLayout.visibility = View.GONE
        } else if (msg.type == Msg.TYPE_SEND) {
            holder.rightLayout.visibility = View.VISIBLE
            holder.rightMsg.text = msg.content
            holder.leftLayout.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}