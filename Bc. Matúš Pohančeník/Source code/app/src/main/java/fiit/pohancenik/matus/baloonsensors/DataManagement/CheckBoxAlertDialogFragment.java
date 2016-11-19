package fiit.pohancenik.matus.baloonsensors.DataManagement;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import fiit.pohancenik.matus.baloonsensors.R;

/**
 * Created by matus on 11. 4. 2016.
 */
public class CheckBoxAlertDialogFragment extends DialogFragment {
    public static CheckBoxAlertDialogFragment newInstance(boolean[] checkedItems) {
        CheckBoxAlertDialogFragment frag = new CheckBoxAlertDialogFragment();
        Bundle args = new Bundle();
        args.putBooleanArray("checkedItems", checkedItems);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final boolean[] checkedItems = getArguments().getBooleanArray("checkedItems");


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),  R.style.AlertDialogCustom);
        // Set the dialog title
        builder.setTitle("Data selection")
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(new String[]{"Temperature","Humidity","Barometric pressure"}, checkedItems,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                checkedItems[which] = isChecked;
                            }
                        })
                        // Set the action buttons

                .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so checkedItems data are sent back to the activity
                        MyDialogFragmentListener activity = (MyDialogFragmentListener) getActivity();
                        activity.onReturnValue(checkedItems);

                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });



        return builder.create();
    }
}
