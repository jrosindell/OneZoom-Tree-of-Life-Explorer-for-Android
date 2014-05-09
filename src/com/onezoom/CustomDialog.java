package com.onezoom;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

/**
 * This is a custom dialog class that will hold a tab view with 2 tabs.
 * Tab 1 will be a list view. Tab 2 will be a list view.
 * 
 */
public class CustomDialog extends Dialog
{
    /**
     * Our custom list view adapter for tab2 listView (listView02).
     */
    ListView02Adapter listView02Adapter = null;

    /**
     * Default constructor.
     * 
     * @param context
     */
    public CustomDialog(Context context)
    {
        super(context);

        // get this window's layout parameters so we can change the position
        WindowManager.LayoutParams params = getWindow().getAttributes(); 

        // change the position. 0,0 is center
        params.x = 0;
        params.y = -120;
        this.getWindow().setAttributes(params); 

        // no title on this dialog
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.custom_dialog_layout);

        // instantiate our list views for each tab
        TextView textView01 = (TextView)findViewById(R.id.textView01);
        ListView listView02 = (ListView)findViewById(R.id.listView02);

        // register a context menu for all our listView02 items
        registerForContextMenu(listView02);

        textView01.setText(Information.guide);

        listView02Adapter = new ListView02Adapter(context);
        listView02.setAdapter(listView02Adapter);

        // bind a click listener to the listView02 list
        listView02.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parentView, View childView, int position, long id)
            {                   
                // will dismiss the dialog
                dismiss();          
            }           
        });

        // get our tabHost from the xml
        TabHost tabs = (TabHost)findViewById(R.id.TabHost01);
        tabs.setup();

        // create tab 1
        TabHost.TabSpec tab1 = tabs.newTabSpec("tab1");
        tab1.setContent(R.id.textView01);
        tab1.setIndicator("Introduction");
        tabs.addTab(tab1);

        // create tab 2
        TabHost.TabSpec tab2 = tabs.newTabSpec("tab2");
        tab2.setContent(R.id.listView02);
        tab2.setIndicator("Colors Meaning");
        tabs.addTab(tab2);
    }

    
    /**
     * A custom list adapter for listView02
     */
    private class ListView02Adapter extends ArrayAdapter
    {        
    	private final Context context;
    	
        public ListView02Adapter(Context context)
        {
        	super(context, R.layout.list_view_02_row);
        	this.context = context;
        }

        /**
         * This is used to return how many rows are in the list view
         */
        public int getCount()
        {
            // add code here to determine how many results we have, hard coded for now
            return 4;
        }

        /**
         * Should return whatever object represents one row in the
         * list.
         */
        public Object getItem(int position)
        {
            return position;
        }

        /**
         * Used to return the id of any custom data object.
         */
        public long getItemId(int position)
        {
            return position;
        }

        /**
         * This is used to define each row in the list view.
         */
        public View getView(int position, View convertView, ViewGroup parent)
        {            
        	LayoutInflater inflater = 
        			(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	View rowView = inflater.inflate(R.layout.list_view_02_row, parent, false);
        	TextView textView = (TextView) rowView.findViewById(R.id.list_view_02_row_text_view);
        	textView.setTextColor(Information.Colors[position]);
        	textView.setText(Information.meanning[position]);
            return rowView;
        }
    }
}
