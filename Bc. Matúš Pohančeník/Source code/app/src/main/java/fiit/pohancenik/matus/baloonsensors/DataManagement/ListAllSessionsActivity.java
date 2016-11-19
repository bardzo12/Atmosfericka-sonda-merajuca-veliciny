package fiit.pohancenik.matus.baloonsensors.DataManagement;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;


import fiit.pohancenik.matus.baloonsensors.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matus on 3. 4. 2016.
 */
public class ListAllSessionsActivity extends ListActivity {

    private FileManager FM;
    private SessionsListAdapter sessionsListAdapter;
    private DatabaseHandler db;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list_all_sessions);

        FM = new FileManager(getApplicationContext());
        db = new DatabaseHandler(this);

    }

    @Override
    protected void onResume(){
        super.onResume();
        sessionsListAdapter = new SessionsListAdapter();
        setListAdapter(sessionsListAdapter);
        sessionsListAdapter.addList(db.getAllSessions());
        sessionsListAdapter.notifyDataSetChanged();

    }

    private void showDialog(final SessionInfo sessionInfo) {//dialog, where aap is asking user if he wants to delete file
        new AlertDialog.Builder(this,  R.style.AlertDialogCustom)
                .setTitle("Delete session file")
                .setMessage("Are you sure you want to delete this file?")
                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("Sprava", "totot je cesta" + sessionInfo.getFilePath());
                        if(FM.deleteFile(sessionInfo.getFilePath())){
                            db.deleteSession(sessionInfo);
                            sessionsListAdapter.addList(db.getAllSessions());
                            sessionsListAdapter.notifyDataSetChanged();
                        }

                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }


    public void showSessionData(SessionInfo sessionInfo){

        Intent intent = new Intent(this,ShowSessionDataActivity.class);
        intent.putExtra("filepath",sessionInfo.getFilePath());
        intent.putExtra("name",sessionInfo.getName());
        intent.putExtra("date",sessionInfo.getDate());
        //Start ShowSessionDataActivity A
        startActivity(intent);

    }


    private class SessionsListAdapter extends BaseAdapter {// adapter for ListView showing all sessions
        private ArrayList<SessionInfo> arrayListSessions;
        private LayoutInflater mInflator;


        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        public SessionsListAdapter() {
            super();
            arrayListSessions = new ArrayList<SessionInfo>();
            mInflator = ListAllSessionsActivity.this.getLayoutInflater();
        }

        public void addSession(SessionInfo sessionInfo) {
            if(!arrayListSessions.contains(sessionInfo)) {
                arrayListSessions.add(sessionInfo);

            }
        }

        public void deleteSession(String Name, String date){
            int size = arrayListSessions.size();

            for(int i = 0 ; i < size ; i++){
                if(arrayListSessions.get(i).getName().equals(Name)
                        && arrayListSessions.get(i).getDate().equals(date)){

                    arrayListSessions.remove(i);
                    return;
                }
            }
        }
        public void addList(List<SessionInfo> cars){
            arrayListSessions.clear();
            arrayListSessions = (ArrayList<SessionInfo>) cars;

        }

        public SessionInfo getSessionInfo(int position) {
            return arrayListSessions.get(position);
        }

        public void clear() {
            arrayListSessions.clear();
        }

        @Override
        public int getCount() {
            return arrayListSessions.size();
        }

        @Override
        public Object getItem(int i) {
            return arrayListSessions.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_session_info, null);
                viewHolder = new ViewHolder();
                viewHolder.session_Date = (TextView) view.findViewById(R.id.session_date_text_listitem_session_info);
                viewHolder.session_Name = (TextView) view.findViewById(R.id.session_name_text_listitem_session_info);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            final SessionInfo sessionInfo = arrayListSessions.get(i);
            final String deviceName = sessionInfo.getName();
            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.session_Date.setText(sessionInfo.getDate());
                viewHolder.session_Name.setText(sessionInfo.getName());
            }


            RelativeLayout startActivity = (RelativeLayout) view.findViewById(R.id.celyObsah_listitem_session_info);
            startActivity.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    showSessionData(sessionInfo);
                }
            });

            ImageButton deleteImageView = (ImageButton) view.findViewById(R.id.deleteButton_listitem_session_info);
            deleteImageView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    showDialog(sessionInfo);
                }
            });



            return view;
        }


    }
    static class ViewHolder {// class used for creating view of one Car
        TextView session_Name;
        TextView session_Date;

    }



}
