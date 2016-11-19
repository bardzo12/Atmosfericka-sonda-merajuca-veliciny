package fiit.pohancenik.matus.baloonsensors.DataManagement;

/**
 * Created by matus on 11. 4. 2016.
 * Interface used to send data from CheckBoxAlertDialogFragment into ShowSessionDataActivity
 */
public interface MyDialogFragmentListener {
    public void onReturnValue(boolean[] checkedItems);
}