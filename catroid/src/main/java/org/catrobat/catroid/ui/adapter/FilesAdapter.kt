package org.catrobat.catroid.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.danvexteam.lunoscript_annotations.LunoClass
import org.catrobat.catroid.R
import org.catrobat.catroid.content.Project
import java.io.File

@LunoClass
class FilesAdapter(
    private val project: Project?,
    private val files: List<File>,
    private val onDelete: (File) -> Unit,
    private val onCopy: (File) -> Unit,
    private val onOpen: (File) -> Unit
) : RecyclerView.Adapter<FilesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileName: TextView = view.findViewById(R.id.file_name)
        val fileSize: TextView = view.findViewById(R.id.file_size)
        val fileIcon: ImageView = view.findViewById(R.id.file_icon)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]
        holder.fileName.text = file.name

        if (file.isDirectory) {
            holder.fileIcon.setImageResource(R.drawable.folder_copy_24px)

            if (file.name == "..") {
                holder.deleteButton.visibility = View.GONE
                holder.fileSize.text = ""
            } else {
                holder.deleteButton.visibility = View.VISIBLE
                val itemsCount = file.listFiles()?.size ?: 0
                holder.fileSize.text = "Elements: $itemsCount"
            }
        } else {
            holder.deleteButton.visibility = View.VISIBLE

            val extension = file.extension.lowercase()
            val iconRes = when (extension) {
                "py", "lua", "js", "java", "kt", "xml", "json", "txt", "md" -> R.drawable.code_24px
                "png", "jpg", "jpeg", "webp", "gif" -> R.drawable.ic_draw_image
                "mp3", "wav", "ogg", "m4a" -> R.drawable.ic_music_library
                "rscene", "glb", "obj" -> R.drawable.deployed_code_24px
                else -> R.drawable.file_present_24px
            }
            holder.fileIcon.setImageResource(iconRes)

            if (file.exists()) {
                holder.fileSize.text = formatFileSize(file.length())
            } else {
                holder.fileSize.text = "File not found"
            }
        }

        holder.deleteButton.setOnClickListener {
            onDelete(file)
        }

        holder.itemView.setOnClickListener {
            onOpen(file)
        }

        holder.itemView.setOnLongClickListener {
            onCopy(file)
            true
        }
    }

    fun formatFileSize(size: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var sizeInUnits = size.toDouble()
        var index = 0

        while (sizeInUnits >= 1024 && index < units.size - 1) {
            sizeInUnits /= 1024
            index++
        }

        return String.format("%.1f %s", sizeInUnits, units[index])
    }

    override fun getItemCount() = files.size
}
