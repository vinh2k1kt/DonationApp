package ie.app.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.IOException;
import java.util.List;

import ie.app.adapter.DonationAdapter;
import ie.app.api.ApiService;
import ie.app.models.Donation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Report extends Base {
    ListView listView;
    ImageButton deleteBtn;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_report);
        listView = findViewById(R.id.reportList);
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            new GetTask(view.getContext()).execute(i+1);
            view.findViewById(R.id.imgDelete).setOnClickListener(view1 -> {
                onDonationDelete(app.donations.get(i));
            });
        });

        swipeRefreshLayout = findViewById(R.id.report_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> new CallApi(this).execute());

        new CallApi(this).execute();
    }

    private class GetTask extends AsyncTask<Integer, Void, Donation> {
        protected ProgressDialog dialog;
        protected Context context;
        protected Donation donation;

        public GetTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Retrieving Donation Details");
            this.dialog.show();
        }

        @Override
        protected Donation doInBackground(Integer... params) {
            ApiService.apiService.getDonation(params[0]).enqueue(new Callback<List<Donation>>() {
                @Override
                public void onResponse(Call<List<Donation>> call, Response<List<Donation>> response) {
                    if (response != null) {
                        donation = response.body().get(0);
                        Toast.makeText(Report.this, "Donation Data [ " + donation.upvotes +
                                "]\n " +
                                "With ID of [" + donation.id + "]", Toast.LENGTH_SHORT).show();
                    }
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<List<Donation>> call, Throwable t) {
                    Toast.makeText(context, "Failed to call for API", Toast.LENGTH_SHORT).show();
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            });
            return donation;
        }
    }


    private class CallApi extends AsyncTask<Void, Void, Void> {

        protected ProgressDialog dialog;
        protected Context context;

        public CallApi(Context context) {
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
        protected Void doInBackground(Void... voids) {
            ApiService.apiService.getAnswers().enqueue(
                    new Callback<List<Donation>>() {

                        @Override
                        public void onResponse(Call<List<Donation>> call, Response<List<Donation>> response) {
                            if (response != null) {
                                Toast.makeText(context, "Called Successfully", Toast.LENGTH_SHORT).show();
                                app.donations = response.body();
                            }
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Donation>> call, Throwable t) {
                            Toast.makeText(context, "Failed to call for API", Toast.LENGTH_SHORT).show();
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                        }
                    }
            );
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            swipeRefreshLayout.setRefreshing(false);
            if (app.donations != null) {
                DonationAdapter adapter = new DonationAdapter(context, app.donations);
                listView.setAdapter(adapter);
            }
        }
    }

    private class DeleteTask extends AsyncTask<Integer, Void, Donation> {
        protected ProgressDialog dialog;
        protected Context context;
        protected Donation donation;

        public DeleteTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Deleting Donation");
            this.dialog.show();
        }

        @Override
        protected Donation doInBackground(Integer... params) {

            ApiService.apiService.deleteDonation(params[0]).enqueue(new Callback<Donation>() {
                @Override
                public void onResponse(Call<Donation> call, Response<Donation> response) {
                    Toast.makeText(Report.this, "Donation with ID of: " + params[0] +
                            " Has been Deleted", Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < app.donations.size(); i++) {
                        if (app.donations.get(i).id == params[0]) {
                            app.donations.remove(i);
                            new CallApi(Report.this).execute();
                        }
                    }
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<Donation> call, Throwable t) {
                    Toast.makeText(context, "Failed to call for API to Delete", Toast.LENGTH_SHORT).show();
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            });
            return donation;
        }
    }

    public void onDonationDelete(final Donation donation) {
        int Id = donation.id;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Donation?");
        builder.setIcon(android.R.drawable.ic_delete);
        builder.setMessage("Are you sure you want to Delete the \'Donation with ID \' \n [ "
                + Id + " ] ?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                new DeleteTask(Report.this).execute(donation.id);
            }
        }).setNegativeButton("No", (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }
}