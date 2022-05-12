package ie.app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import ie.app.adapter.DonationAdapter;
import ie.app.api.ApiService;
import ie.app.models.Donation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Report extends Base {
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new CallApi(this).execute();

        setContentView(R.layout.activity_report);
        listView = (ListView) findViewById(R.id.reportList);
    }

    private class CallApi extends AsyncTask<Void, Void, Void> {

        protected ProgressDialog dialog;
        protected Context context;

        public CallApi(Context context)
        {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Retrieving Donations List");
            this.dialog.show();
        }
        @Override
        protected Void doInBackground(Void ... voids) {
            ApiService.apiService.getAnswers().enqueue(
                    new Callback<List<Donation>>() {

                        @Override
                        public void onResponse(Call<List<Donation>> call, Response<List<Donation>> response) {
                            Toast.makeText(context,"Called Successfully",Toast.LENGTH_SHORT).show();
                            app.donations = response.body();
                            DonationAdapter adapter = new DonationAdapter(context, app.donations);
                            listView.setAdapter(adapter);
                            if (dialog.isShowing()) {
                                Log.v("Donate", "Is Showing");
                                dialog.dismiss();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Donation>> call, Throwable t) {
                            Toast.makeText(context,"Failed to call for API",Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            return null;
        }
    }
}