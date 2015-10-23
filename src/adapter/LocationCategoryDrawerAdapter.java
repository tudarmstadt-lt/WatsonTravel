package adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;

import java.util.List;
import com.example.TravelCorpus_App.R;
import model.LocationLst;

public class LocationCategoryDrawerAdapter extends RecyclerView.Adapter<LocationCategoryDrawerAdapter.MyViewHolder> {
    List<String> subcategories;
    private LayoutInflater inflater;
    private Context context;

    public LocationCategoryDrawerAdapter(Context context, List<String> subcategories) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.subcategories = subcategories;
        System.out.println("KATEGORIEN:"+subcategories.size());
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View category = inflater.inflate(R.layout.answer_category_row, parent, false);
        return new MyViewHolder(category);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String category = subcategories.get(position);
        holder.title.setText(category);
    }

    @Override
    public int getItemCount() {
        return subcategories.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title_category);
        }
    }
}
