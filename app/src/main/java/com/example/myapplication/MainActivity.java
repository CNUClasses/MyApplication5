package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final int NUMBER_UPDATES = 400;
    private static final int ONE_SECOND = 100;

    private static final String RUNNING_CALC = "Running Calculation on seperate thread ";
    private static final String DONE = "Done with calculations";
    private static final String USER_CANCELED = "User chose to cancel";

    private TextView tv;
    private ProgressDialog myProgressDialog;
    private MyTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView)findViewById(R.id.tv_result);
        task = (MyTask)getLastCustomNonConfigurationInstance();
        if (task!=null) {
            progressDialog_start();
            task.attach(this);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        if (task != null) {
            task.detach();
            progressDialog_stop();
            return task;
        }
        else
            return null;
    }

    public void doStartThread(View view) {
        doThreadedCalculation();
        progressDialog_start();
    }

    private void progressDialog_start() {
        myProgressDialog = new ProgressDialog(this);
        myProgressDialog.setTitle("Please wait");
        myProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (MainActivity.this.task != null)
                            MainActivity.this.task.cancel(true);
                    }
                });
        myProgressDialog.setMessage("Notice user cannot interact with rest of UI\nincluding starting additional threads");
                myProgressDialog.setCancelable(false);
        myProgressDialog.show();
    }
    private void progressDialog_stop(){
        if (myProgressDialog != null)
            myProgressDialog.dismiss();
    }
    private void doThreadedCalculation() {
        task = new MyTask(this);
        task.execute(NUMBER_UPDATES);
    }


    void runCalcs(Integer numb_seconds)
    {
        for (int i = 0; i <= NUMBER_UPDATES; i++) {
            try {
                Thread.sleep(ONE_SECOND);
                if (task.isCancelled())
                    return;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private static class MyTask extends AsyncTask<Integer,Void,Void> {
        private MainActivity act;
        private static int numberInstances=0;

        public void detach(){
            if (act != null)
                act = null;
        }
        public void attach(MainActivity act){
            this.act = act;
        }

        //how many threads are running
        public MyTask(MainActivity act){
            //want to be able to modify UI
            //in parent so save parent
            this.act = act;
        }
        @Override
        protected Void doInBackground(Integer... params) {
            act.runCalcs(params[0]);
            return (null);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            act.bStart.setEnabled(false);
            act.tv.setText(RUNNING_CALC);
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            act.bStart.setEnabled(true);
            resetUI(DONE);
        }

        private void resetUI(String talk) {
            act.tv.setText(talk);
            act.progressDialog_stop();
        }

        /**
         * <p>Runs on the UI thread after {@link #cancel(boolean)} is invoked and
         * {@link #doInBackground(Object[])} has finished.</p>
         *
         * <p>The default implementation simply invokes {@link #onCancelled()} and
         * ignores the result. If you write your own implementation, do not call
         * <code>super.onCancelled(result)</code>.</p>
         *
         * @param aVoid The result, if any, computed in
         *              {@link #doInBackground(Object[])}, can be null
         * @see #cancel(boolean)
         * @see #isCancelled()
         */
        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
            resetUI(USER_CANCELED);
        }
    }
}
