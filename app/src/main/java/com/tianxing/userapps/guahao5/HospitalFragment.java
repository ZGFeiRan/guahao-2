package com.tianxing.userapps.guahao5;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;


import com.tianxing.userapps.guahao5.dummy.HospitalList;

import java.util.Collections;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnHospitalFragmentListener}
 * interface.
 */
public class HospitalFragment extends Fragment implements AbsListView.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private SharedPreferences mSP;
    private SharedPreferences.Editor mEditor;
    private OnHospitalFragmentListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static HospitalFragment newInstance(String param1, String param2) {
        HospitalFragment fragment = new HospitalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HospitalFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        MainActivity activity = (MainActivity)getActivity();
        mSP = activity.getSharedPreferences("hospital_list", 0);
        mEditor = mSP.edit();

        //TODO:set all the list items by http return or mock by static data member
        if (!getHospitalListBySP())//no file in data dir, so save the default hospital list in the static member to data dir
        {
            saveHospitalListBySP();
        }

        // TODO: Change Adapter to display your content
        mAdapter = new ArrayAdapter<HospitalList.HospitalItem>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, HospitalList.ITEMS);
    }
    private void saveHospitalListBySP()
    {
        if (HospitalList.ITEMS.size() < 1)
        {
            return;
        }
        for(HospitalList.HospitalItem item : HospitalList.ITEMS)
        {
            mEditor.putString(item.id, item.content);
        }
        mEditor.commit();
        Log.d("saveHospitalListBySP", "" + HospitalList.ITEMS.size());
    }
    private boolean getHospitalListBySP()
    {
        Map<String, ?> hospitalMap = mSP.getAll();
        if (hospitalMap.size() > 0)
        {
            for(Map.Entry<String, ?> entry : hospitalMap.entrySet()) {
                String k = entry.getKey();
                String v = (String)entry.getValue();
                HospitalList.addItem(new HospitalList.HospitalItem(k, v));
            }
            Log.d("getHospitalListBySP", "" + hospitalMap.size());
            return true;
        }
        return false;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getHospitalList();
        View view = inflater.inflate(R.layout.fragment_hospital, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnHospitalFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHospitalFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            HospitalList.HospitalItem item = HospitalList.ITEMS.get(position);
            mListener.onHospitalClick(item.id, item.content);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    private void getHospitalList()
    {
        //get hospital list by network

    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnHospitalFragmentListener {
        // TODO: Update argument type and name
        public void onHospitalClick(String id, String name);
    }

}
