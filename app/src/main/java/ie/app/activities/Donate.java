package ie.app.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.navigation.ui.AppBarConfiguration;

import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ie.app.activities.databinding.ActivityMainBinding;
import ie.app.api.ApiService;
import ie.app.models.Donation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Donate extends Base {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private Button donateButton;
    private RadioGroup paymentMethod;
    private NumberPicker amountPicker;
    private ProgressBar progressBar;
    private EditText amountText;
    private TextView amountTotal;
    private ProgressDialog resetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        donateButton = (Button) findViewById(R.id.donateButton);
        paymentMethod = (RadioGroup) findViewById(R.id.paymentMethod);
        amountPicker = (NumberPicker) findViewById(R.id.amountPicker);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        amountText = (EditText) findViewById(R.id.paymentAmount);
        amountTotal = (TextView) findViewById(R.id.totalSoFar);
        progressBar.setMax(10000);
        amountPicker.setMinValue(0);
        amountPicker.setMaxValue(1000);

        amountTotal.setText("$" + app.totalDonated);
        progressBar.setProgress(app.totalDonated);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetAllTask(this).execute();
    }

    public void donateButtonPressed(View view) {
        int amount = amountPicker.getValue();
        int radioId = paymentMethod.getCheckedRadioButtonId();
        String method = radioId == R.id.Paypal ? "Paypal" : "Direct";
        if (amount == 0) {
            String text = amountText.getText().toString();
            if (!text.equals("")) {
                amount = Integer.parseInt(text);
            }
        }
        if (amount > 0) {
            if (app.donations != null) {
                new InsertTask(this).execute(new Donation(amount, method, app.donations.size(), 69));
                app.newDonation(new Donation(amount, method, app.donations.size(), 69));
            } else {
                new InsertTask(this).execute(new Donation(amount, method, 1, 69));
                app.newDonation(new Donation(amount, method, 1, 69));
            }
            progressBar.setProgress(app.totalDonated);
            amountTotal.setText("$" + app.totalDonated);
        }
    }


    private class GetAllTask extends AsyncTask<Void, Void, Void> {

        protected ProgressDialog dialog;
        protected Context context;

        public GetAllTask(Context context) {
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
                            Toast.makeText(context, "Called Successfully", Toast.LENGTH_SHORT).show();
                            if (response.body() != null) {
                                app.donations = response.body();

                                Log.v("Donate", String.valueOf(response.body()));
                                int sum = 0;
                                for (int i = 0; i < app.donations.size(); i++) {
                                    sum += app.donations.get(i).amount;
                                }
                                amountTotal.setText("$" + sum);
                                progressBar.setProgress(sum);
                                app.totalDonated = sum;
                            } else {
                                app.donations = new ArrayList<>();
                            }
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Donation>> call, Throwable t) {
                            Toast.makeText(context, "Failed to call for API", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            return null;
        }
    }

    private class InsertTask extends AsyncTask<Donation, Void, Void> {
        protected ProgressDialog dialog;
        protected Context context;

        public InsertTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Saving Donation....");
            this.dialog.show();
        }

        @Override
        protected Void doInBackground(Donation... params) {
            ApiService.apiService.addDonation(params[0]).enqueue(new Callback<Donation>() {
                @Override
                public void onResponse(Call<Donation> call, Response<Donation> response) {
                    Log.v("Donate", "Donation App Inserting");
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<Donation> call, Throwable t) {

                }
            });
            return null;
        }
    }

    @Override
    public void reset(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete All Donation?");
        builder.setIcon(android.R.drawable.ic_delete);
        builder.setMessage("Are you sure you want to Delete all Donations?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                app.totalDonated = 0;
                progressBar.setProgress(app.totalDonated);
                amountTotal.setText("$" + app.totalDonated);
                resetDialog = new ProgressDialog(Donate.this, 1);
                resetDialog.setMessage("Deleting Donations....");
                resetDialog.show();
                deleteAll();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteAll() {
        List<Donation> temp = new ArrayList<>();
        temp.addAll(app.donations);
        Log.d("Donate", String.valueOf(temp.size()));
        for (int i = 0; i <= 99; i++) {
            ApiService.apiService.deleteDonation(i).enqueue(new Callback<Donation>() {
                @Override
                public void onResponse(Call<Donation> call, Response<Donation> response) {
                    Donation donation = response.body();
//                    Log.d("donationList", app.donations.size() + " " + resetDialog.isShowing());
                        if (resetDialog.isShowing())
                            resetDialog.dismiss();
                }

                @Override
                public void onFailure(Call<Donation> call, Throwable t) {
                    Log.d("Donate", "Error");
                }
            });
        }
        app.donations = new ArrayList<>();
//        new GetAllTask(this).execute();
    }
}