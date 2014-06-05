package com.onezoom;

import com.fscz.util.TextViewEx;

import android.app.Dialog;
import android.text.method.ScrollingMovementMethod;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

/**
 * This is a custom dialog class that will hold a tab view with 3 tabs.
 * The first tab tells user different meanings of colors in the tree.
 * The second tab tells the authors of the app
 * The third tab lead user to a tutorial of how to use the app.
 * 
 */
public class CustomDialog extends Dialog
{
	private CustomDialog self;
    /**
     * Our custom list view adapter for tab2 listView (listView02).
     */

    /**
     * Default constructor.
     * 
     * @param context
     */
    public CustomDialog(final CanvasActivity context)
    {
        super(context);
        self = this;
        // get this window's layout parameters so we can change the position
        WindowManager.LayoutParams params = getWindow().getAttributes(); 

        // change the position. 0,0 is center
        params.x = 0;
        params.y = -30;//-context.getScreenHeight() / 24;
        this.getWindow().setAttributes(params); 

        // no title on this dialog
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.custom_dialog_layout);

        // instantiate our list views for each tab
        TextViewEx textView01 = (TextViewEx)findViewById(R.id.textView01);
        TextViewEx textView02 = (TextViewEx)findViewById(R.id.textView02);

        //set text for tab1 and enable scrolling
        textView01.setText(Information.authorAndCredit, true);
        textView01.setMovementMethod(new ScrollingMovementMethod());

        
        //set text for tab2 and enable scrolling
        textView02.setText(Information.guide, true);
        textView02.setMovementMethod(new ScrollingMovementMethod());
        
        // get our tabHost from the xml
        TabHost tabs = (TabHost)findViewById(R.id.TabHost01);
        tabs.setup();


        // create tab 1
        // use textview01
        TabHost.TabSpec tab1 = tabs.newTabSpec("tab1");
        tab1.setContent(R.id.textView01);
        tab1.setIndicator(Information.tabTitle[0]);
        tabs.addTab(tab1);
        
        // create tab 2
        // use textview03
        TabHost.TabSpec tab2 = tabs.newTabSpec("tab2");
        tab2.setContent(R.id.textView02);
        tab2.setIndicator(Information.tabTitle[1]);
        tabs.addTab(tab2);
        
        tabs.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				if (tabId.equals("tab2")) {
					context.showIntroductionSlide();
					self.cancel();
				}
			}
        	
        });
    }
}
