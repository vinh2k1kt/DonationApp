package ie.app.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import androidx.navigation.ui.AppBarConfiguration;

import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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

        amountTotal.setText("$"+app.totalDonated);
        progressBar.setProgress(app.totalDonated);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetAllTask(this).execute();
    }

    public void donateButtonPressed(View view){
        int amount = amountPicker.getValue();
        int radioId = paymentMethod.getCheckedRadioButtonId();
        String method = radioId == R.id.Paypal? "Paypal" : "Direct";
        if(amount == 0){
            String text = amountText.getText().toString();
            if(!text.equals("")){
                amount = Integer.parseInt(text);
            }
        }
        if(amount > 0){
            app.newDonation(new Donation(amount, method,1));
            Log.v("Donation", ""+amount+ " "+ app.totalDonated);
            progressBar.setProgress(app.totalDonated);
            amountTotal.setText("$" + app.totalDonated);
        }
    }



    @Override
    public void reset(MenuItem item) {
       app.totalDonated=0;
       progressBar.setProgress(app.totalDonated);
       amountTotal.setText("$"+app.totalDonated);
    }

    private class GetAllTask extends AsyncTask<Void, Void, Void> {

        protected ProgressDialog dialog;
        protected Context context;

        public GetAllTask(Context context)
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

                            int sum = 0;
                            for (int i = 0; i < app.donations.size(); i++) {
                                sum += app.donations.get(i).amount;
                            }
                            amountTotal.setText("$"+sum);
                            progressBar.setProgress(sum);
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