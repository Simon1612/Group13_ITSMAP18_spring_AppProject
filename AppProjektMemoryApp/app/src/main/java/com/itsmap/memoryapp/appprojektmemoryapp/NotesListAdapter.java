package com.itsmap.memoryapp.appprojektmemoryapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.itsmap.memoryapp.appprojektmemoryapp.Activities.EditNotesActivity;
import com.itsmap.memoryapp.appprojektmemoryapp.Models.NoteDataModel;
import java.util.List;

public class NotesListAdapter extends ArrayAdapter<NoteDataModel> implements AdapterView.OnItemClickListener {
    private Context context;
    private List<NoteDataModel> noteList;
    TextView noteNameView, noteLocationView, noteDateView;
    ListView listView;
    NoteDataModel currentNoteDataModel;
    Intent editNoteIntent;

    public NotesListAdapter(Context _context, List<NoteDataModel> _cityList){
        super(_context, R.layout.listitem_note, _cityList);
        this.context = _context;
        noteList = _cityList;
    }

    @Override
    public int getCount()
    {
        return noteList.size();
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent)
    {
        View view = convertView;
        if (view == null)
            view = LayoutInflater.from(context).inflate(R.layout.listitem_note, parent, false);

        noteNameView = view.findViewById(R.id.noteNameText);
        noteLocationView = view.findViewById(R.id.noteLocationText);
        noteDateView = view.findViewById(R.id.noteDateText);
        listView = view.findViewById(R.id.homescreenNotesList);

        currentNoteDataModel = noteList.get(index);

        noteNameView.setText(currentNoteDataModel.getName());
        noteDateView.setText(currentNoteDataModel.getTimeStamp());
        noteLocationView.setText(currentNoteDataModel.getLocation());

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        editNoteIntent = new Intent(context, EditNotesActivity.class)
                .putExtra("noteData", getItem(i));

        context.startActivity(editNoteIntent);
    }
}
