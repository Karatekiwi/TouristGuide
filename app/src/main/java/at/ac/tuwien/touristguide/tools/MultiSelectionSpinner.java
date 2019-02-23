package at.ac.tuwien.touristguide.tools;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import at.ac.tuwien.touristguide.R;
import at.ac.tuwien.touristguide.db.DatabaseHandler;


/**
 * http://v4all123.blogspot.in/2013/09/spinner-with-multiple-selection-in.html
 */
public class MultiSelectionSpinner extends Spinner implements OnMultiChoiceClickListener {
    private String[] mitems = null;
    private boolean[] mSelection = null;
    private ArrayAdapter<String> simple_adapter;
    private Context context;


    public MultiSelectionSpinner(Context context) {
        super(context);
        simple_adapter = new ArrayAdapter<>(context, R.layout.spinner_textview);
        super.setAdapter(simple_adapter);
    }

    public MultiSelectionSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        simple_adapter = new ArrayAdapter<>(context, R.layout.spinner_textview);
        super.setAdapter(simple_adapter);
    }

    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        mSelection[which] = isChecked;

        //Thunderdome :)
        if (getSelectedItemsCount() == 0) {
            Toast.makeText(context, context.getString(R.string.ms1), Toast.LENGTH_LONG).show();

            for (int i = 0; i < mitems.length; ++i) {
                mSelection[i] = true;
            }

            simple_adapter.add(buildSelectedItemString());

        }

        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());

        DatabaseHandler.getInstance(context).saveCategories(getSelectedIndiciesAsString());
    }

    private int getSelectedItemsCount() {
        List<Integer> selectedInts = new LinkedList<>();

        for (int i = 0; i < mitems.length; ++i) {
            if (mSelection[i]) {
                selectedInts.add(i);
            }
        }

        return selectedInts.size();
    }

    @Override
    public boolean performClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMultiChoiceItems(mitems, mSelection, this);
        builder.show();

        return true;
    }


    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        throw new RuntimeException("setAdapter is not supported by MultiSelectSpinner.");
    }


    public void setItems(List<String> items) {
        mitems = items.toArray(new String[items.size()]);
        mSelection = new boolean[mitems.length];
        simple_adapter.clear();
        simple_adapter.add(mitems[0]);
        Arrays.fill(mSelection, false);
    }


    public void setSelection(int[] selectedIndicies) {
        for (int i = 0; i < mSelection.length; i++) {
            mSelection[i] = false;
        }

        for (int index : selectedIndicies) {
            mSelection[index] = true;
        }

        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
    }


    public String getSelectedIndiciesAsString() {
        String selection = "";

        List<Integer> selectedInts = new LinkedList<>();
        for (int i = 0; i < mitems.length; ++i) {
            if (mSelection[i]) {
                selectedInts.add(i);
            }
        }

        for (int i : selectedInts) {
            selection += i + ",";
        }

        return selection;
    }

    private String buildSelectedItemString() {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < mitems.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(", ");
                }

                foundOne = true;
                sb.append(mitems[i]);
            }
        }

        return sb.toString();
    }


    public void setContext(Context context) {
        this.context = context;
    }


}

