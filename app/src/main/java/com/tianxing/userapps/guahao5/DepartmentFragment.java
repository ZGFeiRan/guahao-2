package com.tianxing.userapps.guahao5;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;


import com.tianxing.userapps.guahao5.dummy.DepartmentList;
import com.tianxing.userapps.guahao5.dummy.DoctorList;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnDepFragmentListener}
 * interface.
 */
public class DepartmentFragment extends Fragment implements AbsListView.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String mHpid;
    private DepGetter mDepGetter;
    private MainActivity mActivity;
    private OnDepFragmentListener mListener;
    private SharedPreferences mSP;
    private SharedPreferences.Editor mEditor;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ArrayAdapter<DepartmentList.DepartmentItem> mAdapter;

    // TODO: Rename and change types of parameters
    public static DepartmentFragment newInstance(String param1, String param2) {
        DepartmentFragment fragment = new DepartmentFragment();
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
    public DepartmentFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // TODO: Change Adapter to display your content
        mAdapter = new ArrayAdapter<DepartmentList.DepartmentItem>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, DepartmentList.ITEMS);

        mActivity = (MainActivity)getActivity();
        mSP = mActivity.getSharedPreferences("dep_list", 0);
        mEditor = mSP.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mHpid = mActivity.mCurHospitalID;
        View view = inflater.inflate(R.layout.dep_fragment_item, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        Looper looper = Looper.myLooper();
        mDepGetter = new DepGetter(mHpid, new MessageHandler(looper));
        DepartmentList.clear();
        mAdapter.notifyDataSetChanged();
        getDepListByHTTP();
        /*
        if (!getDepListBySP())
        {
            getDepListByHTTP();
        }
        */
        return view;
    }
    private boolean getDepListBySP()
    {
        Set<String> depSet = mSP.getStringSet(mHpid,null);
        if (depSet != null)
        {
            for(String str : depSet) {
                String [] s = str.split("\0x01");
                DepartmentList.addItem(new DepartmentList.DepartmentItem(s[0], s[1]));
            }
            Log.d("getDepListBySP", ""+depSet.size());
            Collections.sort(DepartmentList.ITEMS);
            return true;
        }
        return false;
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDepFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDepFragmentListener");
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
            DepartmentList.DepartmentItem item = DepartmentList.ITEMS.get(position);
            mListener.onDepClick(item.id, item.content);
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
    private void getDepListByHTTP()
    {
        mDepGetter.start();
    }
    class MessageHandler extends Handler {
        private int cnt = 0;
        //String mPatternStr = "onclick=\"location.href='/Home/Guahao/index/hpid/[0-9]+/ksid/([0-9]+)'.*?\">(.*?)</a>";
        //String mPatternStr = "href=[\"']\\./content\\.php\\?hpid=[0-9]+&keid=(.+?)['\"]>(.*?)</a>";
        String mPatternStr = "href=\"/dpt/appoint/(.+?)\\.htm\">(.*?)</a>";
        Pattern mPattern = Pattern.compile(mPatternStr);

        public MessageHandler(Looper looper) {
            super(looper);
        }

        private void saveDepList()
        {
            Set<String> depSet = new HashSet<String>();
            for(DepartmentList.DepartmentItem item : DepartmentList.ITEMS)
            {
                depSet.add(item.id + "\0x01" + item.content);
            }
            if (!depSet.isEmpty()) {
                mEditor.putStringSet(mHpid, depSet);
                mEditor.commit();
                Log.d("saveDepList", ""+depSet.size());
            }
        }
        @Override
        public void handleMessage(Message msg) {
            String msgStr = (String) msg.obj;
            Log.d("DepMessage", msgStr);
            boolean ret = getListFromMsg(msgStr);
            if (ret)
            {
                mDepGetter.stop();
                Collections.sort(DepartmentList.ITEMS);
                mAdapter.notifyDataSetChanged();
                saveDepList();
                return;
            }
            if (++cnt > 3)
            {
                mDepGetter.stop();
                Log.d("DepGet", "num >" + cnt);
                cnt = 0;
            }
        }

        private boolean getListFromMsg(String html)
        {
            if (html.isEmpty())
            {
                return false;
            }
            Matcher matcher = mPattern.matcher(html);
            while (matcher.find())
            {
                String ksid = matcher.group(1);
                ksid = ksid.split("-")[1];
                String ksName = matcher.group(2);
                DepartmentList.addItem(new DepartmentList.DepartmentItem(ksid, ksName));
            }
            if ( DepartmentList.ITEMS.size() > 0) {
                return true;
            }
            return false;
        }
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
    public interface OnDepFragmentListener {
        // TODO: Update argument type and name
        public void onDepClick(String id, String name);
    }

}
