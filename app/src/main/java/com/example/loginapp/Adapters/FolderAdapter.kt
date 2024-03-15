    package com.example.loginapp.Adapters

    import android.annotation.SuppressLint
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.TextView
    import androidx.cardview.widget.CardView
    import androidx.recyclerview.widget.RecyclerView
    import com.example.loginapp.Listeners.FolderClickListener
    import com.example.loginapp.Models.Folder
    import com.example.loginapp.R
    import com.google.android.material.card.MaterialCardView

    public class FolderAdapter(private var folderList: MutableList<Folder>,
                               private var folderClickListener: FolderClickListener) :
        RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

        inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val folderCard : MaterialCardView = itemView.findViewById(R.id.folderCard)
            private val folderTitle: TextView = itemView.findViewById(R.id.folderTitle)
            private val folderItemsCount: TextView = itemView.findViewById(R.id.folderItemsCount)

            @SuppressLint("SetTextI18n")
            fun bind(folder: Folder) {
                folderTitle.text = folder.title
                folderItemsCount.text = "Items count: ${folder.itemCount}"
            }
        }

        fun updateData(newFolderList: MutableList<Folder>) {
            folderList = newFolderList
            notifyDataSetChanged()
        }

        fun removeFolder(position: Int) {
            folderList.removeAt(position)
            notifyItemRemoved(position)
        }

        fun updateFolderName(position: Int, newName: String) {
            folderList[position].title = newName
            notifyItemChanged(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.folder_item, parent, false)
            return FolderViewHolder(view)
        }

        override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
            holder.bind(folderList[position])

            holder.folderCard.setOnClickListener {
                folderClickListener.onClick(folderList.get(holder.adapterPosition))
            }

            holder.folderCard.setOnLongClickListener {
                folderClickListener.onLongClick(holder.folderCard,
                    folderList.get(holder.adapterPosition))
                true
            }
        }

        override fun getItemCount(): Int {
            return folderList.size
        }
    }