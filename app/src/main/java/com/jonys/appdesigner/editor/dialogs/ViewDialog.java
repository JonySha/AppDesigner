package com.jonys.appdesigner.editor.dialogs;

import android.content.Context;

import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;

import com.jonys.appdesigner.managers.IdManager;

public class ViewDialog extends AttributeDialog {
	
	private ArrayList<String> ids;
	private ListView listview;
	
	public ViewDialog(Context context, String savedValue) {
		super(context);
		
		ids = IdManager.getIds();
		
		listview = new ListView(context);
		listview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		listview.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_single_choice, ids));
		listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listview.setDivider(null);

		if(!savedValue.equals("")) {
			listview.setItemChecked(ids.indexOf(savedValue.replace("@id/", "")), true);
		}
		
		setView(listview, 0, 20, 0, 0);
	}
	
	@Override
	protected void onClickSave() {
	    super.onClickSave();
		
		if(listview.getCheckedItemPosition() == -1) {
			listener.onSave("-1");
		}
		else {
			listener.onSave("@id/" + ids.get(listview.getCheckedItemPosition()));
		}
	}
}