package com.example.loginapp.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.loginapp.Listeners.FolderFileClickListener
import com.example.loginapp.Models.FolderFile
import com.example.loginapp.R

class FileAdapter(private var fileList: List<FolderFile>,
                  private var fileClickListener: FolderFileClickListener) :
    RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileCard : CardView = itemView.findViewById(R.id.fileCard)
        val fileNameTextView: TextView = itemView.findViewById(R.id.fileName)
        val fileTypeTextVIew: TextView = itemView.findViewById(R.id.fileType)
        val fileTypeImageView : ImageView = itemView.findViewById(R.id.fileTypeImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.file_item, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val currentFile = fileList.get(position)

        holder.fileNameTextView.text = currentFile.fileName
        holder.fileTypeTextVIew.text = currentFile.fileType

        holder.fileCard.setOnClickListener {
            fileClickListener.onClick(fileList.get(holder.adapterPosition))
        }

        holder.fileCard.setOnLongClickListener {
            fileClickListener.onLongClick(holder.fileCard,
                fileList.get(holder.adapterPosition))
            true
        }

        when (currentFile.fileType) {
            "Image" -> {
                holder.fileTypeImageView.setImageResource(R.drawable.ic_image);
            }
            "Document" -> {
                holder.fileTypeImageView.setImageResource(R.drawable.ic_document);
            }
            "Video" -> {
                holder.fileTypeImageView.setImageResource(R.drawable.ic_video);
            }
            "Audio" -> {
                holder.fileTypeImageView.setImageResource(R.drawable.ic_audio);
            }
            else -> {
                holder.fileTypeImageView.setImageResource(R.drawable.ic_default);
            }
        }
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    fun updateData(newFileList: List<FolderFile>) {
        fileList = newFileList
        notifyDataSetChanged()
    }
}