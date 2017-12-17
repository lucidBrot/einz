package ch.ethz.inf.vs.a4.minker.einz.UI;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import ch.ethz.inf.vs.a4.minker.einz.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectorFragment extends Fragment {

    public static final String CHOICE_LIST_TEXT = "choices_text";
    public static final String TITLE_KEY = "description";
    public static final String RULE_NAME = "ruleName";
    public static final String CHOICE_LIST_VALUES = "choices_values";

    private SelectorCallbackInterface caller;
    private ArrayAdapter<String> choiceAdapter;

    private String result = null;
    private String ruleName = null;

    public SelectorFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_selector, container, false);

        final LinearLayout choiceList = inflatedView.findViewById(R.id.selector_list);

        Bundle arguments =  getArguments();
        if(arguments != null) {

            TextView title = inflatedView.findViewById(R.id.selector_title);
            String titleText = arguments.getString(TITLE_KEY);
            if(titleText != null) {
                title.setText(titleText);
            }

            ruleName = arguments.getString(RULE_NAME);

            ArrayList<String> choicesText = arguments.getStringArrayList(CHOICE_LIST_TEXT);
            ArrayList<String> choicesValues = arguments.getStringArrayList(CHOICE_LIST_VALUES);
            if (choicesText != null && choicesValues != null) {
                for(int i = 0; i < choicesText.size(); i++){
                    final View element  = inflater.inflate(R.layout.fragment_selector_item, container, false);

                    Button text = element.findViewById(R.id.selector_item);
                    text.setText(choicesText.get(i));
                    text.setTag(choicesValues.get(i));

                    text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Button text = view.findViewById(R.id.selector_item);
                            result = (String) text.getTag();
                            if(caller != null) {
                                caller.onItemSelected(result, ruleName);
                            }
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
        void onItemSelected(String selection, String ruleName);
    }
}


