package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;
import java.util.Collections;
import java.util.List;
import com.example.TravelCorpus_App.R;
import model.Question;
import model.TableItem;

public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.MyViewHolder> {
    private List<TableItem> data = Collections.emptyList();
    private LayoutInflater inflater;
    private Context context;

    public NavigationDrawerAdapter(Context context, List<TableItem> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    public void delete(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    public void insert(TableItem tableItem) {
        data.add(tableItem);
        notifyItemInserted(data.lastIndexOf(tableItem));
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.nav_drawer_row, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        System.out.println("HIER");
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Question question = (Question) data.get(position);
        holder.title.setText(question.getTitle());
        System.out.println("Title:" + question.getTitle());
        System.out.println("Icon:" + question.getIconId());
        holder.src.setImageResource(question.getIconId());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView src;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            src = (ImageView) itemView.findViewById(R.id.image);
        }
    }
}