package ie.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;
import ie.app.activities.R;
import ie.app.models.Donation;

public class DonationAdapter extends ArrayAdapter<Donation> {
    private Context context;
    private List<Donation> donations;

    public DonationAdapter(Context context, List<Donation> donations){
        super(context, R.layout.row_donate, donations);
        this.context = context;
        this.donations = donations;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.row_donate, parent, false);
        Donation donation = donations.get(position);
        TextView amountView = view.findViewById(R.id.row_amount);
        TextView methodView = view.findViewById(R.id.row_method);
        TextView upVotesView = view.findViewById(R.id.row_upvotes);

        upVotesView.setText(String.valueOf(donation.upvotes));
        amountView.setText("$"+donation.amount);
        methodView.setText(donation.method);

        return view;
    }


}
