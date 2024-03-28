package com.example.p7menus

import android.content.Context
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var notesRecyclerView: RecyclerView
    private val notesList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        notesRecyclerView = findViewById(R.id.notesRecyclerView)
        notesRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = NoteAdapter(notesList)
        notesRecyclerView.adapter = adapter

        notesRecyclerView.addOnItemTouchListener(
            RecyclerItemClickListener(
                this,
                notesRecyclerView,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        showContextMenu(view, position)
                    }

                    override fun onLongItemClick(view: View, position: Int) {
                        showContextMenu(view, position)
                    }
                }
            )
        )
        findViewById<ImageButton>(R.id.addNoteButton).setOnClickListener {
            showPopupMenu(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menus, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                showToast("Clicked on Settings")
                true
            }
            R.id.action_share -> {
                showToast("Clicked on Share")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showContextMenu(view: View, position: Int) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.context_menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.context_edit -> {
                    showEditNoteDialog(position)
                    true
                }
                R.id.context_delete -> {
                    deleteNoteAt(position)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.context_edit -> {
                val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
                val position = info.position
                showEditNoteDialog(position)
                true
            }
            R.id.context_delete -> {
                val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
                val position = info.position
                deleteNoteAt(position)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun showEditNoteDialog(position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Note")

        val input = EditText(this)
        input.setText(notesList[position])
        builder.setView(input)

        builder.setPositiveButton("Save") { dialog, _ ->
            val editedNote = input.text.toString().trim()
            if (editedNote.isNotEmpty()) {
                editNoteAt(position, editedNote)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun editNoteAt(position: Int, editedNote: String) {
        if (position in notesList.indices) {
            notesList[position] = editedNote
            notesRecyclerView.adapter?.notifyItemChanged(position)
        }
    }

    private fun deleteNoteAt(position: Int) {
        if (position in notesList.indices) {
            notesList.removeAt(position)
            notesRecyclerView.adapter?.notifyItemRemoved(position)
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.popup_add_text_note -> {
                    showAddNoteDialog()
                    true
                }
                R.id.popup_add_image_note -> {
                    showToast("Clicked on Add Image Note")
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showAddNoteDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Note")

        // Set up the input
        val input = EditText(this)
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("OK") { dialog, _ ->
            val noteText = input.text.toString().trim()
            if (noteText.isNotEmpty()) {
                addNoteToList(noteText)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun addNoteToList(note: String) {
        notesList.add(note)
        notesRecyclerView.adapter?.notifyItemInserted(notesList.size - 1)
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

class NoteAdapter(private val notesList: List<String>) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notesList[position]
        holder.bind(note)
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val noteTextView: TextView = itemView.findViewById(R.id.noteTextView)

        fun bind(note: String) {
            noteTextView.text = note
        }
    }
}




class RecyclerItemClickListener(
    context: Context,
    recyclerView: RecyclerView,
    private val listener: OnItemClickListener?
) : RecyclerView.OnItemTouchListener {

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
        fun onLongItemClick(view: View, position: Int)
    }

    private val gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val child = recyclerView.findChildViewUnder(e.x, e.y)
                if (child != null && listener != null) {
                    listener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child))
                }
            }
        })
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        val child = rv.findChildViewUnder(e.x, e.y)
        if (child != null && listener != null && gestureDetector.onTouchEvent(e)) {
            listener.onItemClick(child, rv.getChildAdapterPosition(child))
            return true
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
}
