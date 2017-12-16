package ch.ethz.inf.vs.a4.minker.einz.UI;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ch.ethz.inf.vs.a4.minker.einz.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectorFragment extends Fragment {

    public static final String CHOICE_LIST_KEY = "choices";
    public static final String TITLE_KEY = "description";

    private SelectorCallbackInterface caller;
    private ArrayAdapter<String> choiceAdapter;

    private String result = null;

    public SelectorFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_selector, container, false);

        LinearLayout choiceList = inflatedView.findViewById(R.id.selector_list);

        Bundle arguments =  getArguments();
        if(arguments != null) {

            TextView title = inflatedView.findViewById(R.id.selector_title);
            String titleText = arguments.getString(TITLE_KEY);
            if(titleText != null) {
                title.setText(titleText);
            }

            ArrayList<String> choices = arguments.getStringArrayList(CHOICE_LIST_KEY);
            if (choices != null) {
                for(String choice : choices){
                    final View element  = inflater.inflate(R.layout.fragment_selector_item, container, false);

                    Button text = element.findViewById(R.id.selector_item);
                    text.setText(choice);

                    text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Button text = view.findViewById(R.id.selector_item);
                            result = (String) text.getText();
                            caller.onItemSelected(result);
                        }
                    });
                    choiceList.addView(element);
                }
            }
        }


        return inflatedView;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof SelectorCallbackInterface){
            caller = (SelectorCallbackInterface) context;
        }
    }

    public interface SelectorCallbackInterface{
        void onItemSelected(String selection);
    }
}


