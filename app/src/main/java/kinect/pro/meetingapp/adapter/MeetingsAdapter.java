package kinect.pro.meetingapp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kinect.pro.meetingapp.R;
import kinect.pro.meetingapp.model.MeetingModels;

/**
 * Created by dobrikostadinov on 11/24/17.
 */

public class MeetingsAdapter extends RecyclerView.Adapter<MeetingsAdapter.ViewHolder> {

    public List<MeetingModels> data;

    public MeetingsAdapter(List<MeetingModels> data) {
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meeting, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.tvText.setText(data.get(position).getTopic());
        holder.tvDate.setText(new Date(data.get(position).getDate()).toString());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvText;
        TextView tvDate;

        public ViewHolder(View itemView) {
            super(itemView);

            tvText = itemView.findViewById(R.id.item_meeting_text);

            tvDate = itemView.findViewById(R.id.item_meeting_date);
        }
    }
}
