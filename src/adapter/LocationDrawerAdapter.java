package adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.TravelCorpus_App.R;
import model.Location;
import model.LocationLst;

import java.util.List;

public class LocationDrawerAdapter extends RecyclerView.Adapter<LocationDrawerAdapter.MyViewHolder> {
    List<Location> locations;
    private LayoutInflater inflater;
    private Context context;

    public LocationDrawerAdapter(Context context, List<Location> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.locations = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View category = inflater.inflate(R.layout.answer_category_row, parent, false);
        return new MyViewHolder(category);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Location current = locations.get(position);
        holder.title.setText(current.getName());
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title_category);
        }
    }
}
