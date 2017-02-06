package com.tianxing.userapps.guahao5;

import android.app.Activity;
import android.os.AsyncTask;
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


import com.tianxing.userapps.guahao5.dummy.DateItem;
import com.tianxing.userapps.guahao5.dummy.DateList;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnDateFragmentListener}
 * interface.
 */
public class DateFragment extends Fragment implements AbsListView.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static ExecutorService SINGLE_TASK_EXECUTOR = (ExecutorService) Executors.newSingleThreadExecutor();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private GetDateListTask mGetDateListTask = null;
    private String mHpid;
    private String mKeid;

    private OnDateFragmentListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;
    private DateList mDateList = null;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ArrayAdapter<DateItem> mAdapter;

    // TODO: Rename and change types of parameters
    public static DateFragment newInstance(String param1, String param2) {
        DateFragment fragment = new DateFragment();
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
    public DateFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
        {
            return;
        }
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private void getDateList()
    {
        MainActivity activity = (MainActivity) getActivity();
        if (!mDateList.ITEMS.isEmpty() && mHpid.equals(activity.mCurHospitalID)
                && mKeid.equals(activity.mCurDepID))
        {
            return;
        }
        mDateList.clear();
        mAdapter.notifyDataSetChanged();
        mHpid = activity.mCurHospitalID;
        mKeid = activity.mCurDepID;

        mGetDateListTask = new GetDateListTask();
        //mGetDateListTask.execute((Void) null);
        mGetDateListTask.executeOnExecutor(SINGLE_TASK_EXECUTOR);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_date, container, false);

        mDateList = new DateList();
        mAdapter = new ArrayAdapter<DateItem>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, mDateList.ITEMS);
        // Set the adapter
        mListView = (AbsListView)view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        getDateList();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDateFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDateFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onHiddenChanged(boolean isHidden)
    {
        if (!isHidden) {
            getDateList();
        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onDateClick(mDateList.ITEMS.get(position));
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
    public interface OnDateFragmentListener {
        // TODO: Update argument type and name
        public void onDateClick(DateItem dataItem);
    }

    public class GetDateListTask extends AsyncTask<Void, Void, Boolean> {
        //String patternFullDateStr = "<input type=\"hidden\" id=\"fullDate\" value=\"(.*?)\" />";
        //Pattern patternFullDate = Pattern.compile(patternFullDateStr);
        //String patternRemainDateStr = "<input type=\"hidden\" id=\"remainDate\" value=\"(.*?)\" />";
        String patternRemainDateStr =
                "class=\"ksorder_kyy.*\">[\\s\\S]*?<br>剩余:([0-9]+)<input .*? value=\"(.*?)\".*?>";
        Pattern patternRemainDate = Pattern.compile(patternRemainDateStr);
        String patternFullDateStr =
                "class=\"ksorder_ym.*\">[\\s\\S]*?<input .*? value=\"(.*?)\".*?>";
        Pattern patternFullDate = Pattern.compile(patternFullDateStr);
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                return getDateList();
            } catch (InterruptedException e) {
                return false;
            }
        }
        protected  boolean getDateList() throws InterruptedException
        {
            Map headers = new HashMap();
            headers.put("Referer", HTTPSessionStatus.URL_INDEX_MOBILE);

            String url = String.format(HTTPSessionStatus.URL_DATELIST_MOBILE, mHpid, mKeid);
            return getDateListByHTTPClientWrapper(url, headers);
        }

        protected void parseFull(String html) {
            Matcher matcher;
            matcher = patternFullDate.matcher(html);
            Map<String, Integer> date2cnt = new HashMap();
            while (matcher.find())
            {
                String date = matcher.group(1);
                String[] dateSplit = date.split("_");
                String dutyCodeDate = dateSplit[1] + "_" + dateSplit[2];
                if (!date2cnt.containsKey(dutyCodeDate))
                {
                    date2cnt.put(dutyCodeDate, 0);
                }
                Log.d("dutyCodeDate:%s", dutyCodeDate);
            }
            mDateList.addItem(date2cnt, "已满");
        }
        protected void parseRemain(String html) {
            Matcher matcher;
            matcher = patternRemainDate.matcher(html);
            Map<String, Integer> date2cnt = new HashMap();
            while (matcher.find())
            {
                String remainCnt = matcher.group(1);
                String date = matcher.group(2);
                String[] dateSplit = date.split("_");
                String dutyCodeDate = dateSplit[1] + "_" + dateSplit[2];
                Integer rCnt = Integer.parseInt(remainCnt);
                if (date2cnt.containsKey(dutyCodeDate))
                {
                    rCnt += date2cnt.get(dutyCodeDate);
                }
                date2cnt.put(dutyCodeDate, rCnt);
                Log.d("dutyCodeDate:%s", dutyCodeDate);
            }
            mDateList.addItem(date2cnt, "可挂");
        }
        protected boolean ParseDataList(String html)
        {
            mDateList.clear();
            parseFull(html);
            parseRemain(html);
            mDateList.addGrabItem();
            return true;
        }
        protected boolean getDateListByHTTPClientWrapper(String url,Map headers) throws InterruptedException{
            HttpEntity entity = HTTPClientWrapper.getInstance().doGet(url, null, headers);
            try {
                if (entity == null)
                {
                    Log.e("login", "return entity null");
                    return false;
                }
                String result = EntityUtils.toString(entity, "GBK");
                Log.d("getDataList http result", result);
                return ParseDataList(result);
            }
            catch (IOException e) {
                //strResult = e.getMessage().toString();
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mGetDateListTask = null;
            if (success) {
                Log.d("GetDateList", "success");
                mAdapter.notifyDataSetChanged();
            } else {
                Log.d("GetDateList", "fail");
            }
        }

        @Override
        protected void onCancelled() {
            mGetDateListTask = null;
        }
    }
}
