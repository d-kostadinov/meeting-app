package kinect.pro.meetingapp.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import kinect.pro.meetingapp.R;
import kinect.pro.meetingapp.activity.BaseActivity;
import kinect.pro.meetingapp.adapter.MeetingsAdapter;
import kinect.pro.meetingapp.model.MeetingModels;

/**
 * Created by dobrikostadinov on 11/24/17.
 */

public class FraMeetingsList extends Fragment {

    public static final String TAG = FraMeetingsList.class.getSimpleName();
    private List<MeetingModels> data;

    public static FraMeetingsList newInstance(List<MeetingModels> data){

        FraMeetingsList fraMeetingsList = new FraMeetingsList();

        fraMeetingsList.data = data;

        return fraMeetingsList;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fra_meetings_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RecyclerView mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler);


        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);


        MeetingsAdapter mAdapter = new MeetingsAdapter(data);
        mRecyclerView.setAdapter(mAdapter);
    }
}
