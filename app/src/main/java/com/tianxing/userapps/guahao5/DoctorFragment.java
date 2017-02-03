package com.tianxing.userapps.guahao5;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.tianxing.userapps.guahao5.dummy.DoctorList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnDoctorFragmentListener}
 * interface.
 */
public class DoctorFragment extends Fragment implements AbsListView.OnItemClickListener {
    public class DoctorListAdapter extends ArrayAdapter<DoctorList.DoctorItem> {
        final class ViewHolder {
            EditText name;
            TextView title;
            TextView skill;
            TextView remainNum;
            Button selectRatioButton;
            MyTextWatcher textWatcher;
        }
        class MyTextWatcher implements TextWatcher {
            Integer mPosition;
            public void updatePos(Integer pos) {
                mPosition = pos;
            }
            public MyTextWatcher(Integer position) {
                this.mPosition = position;
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count,int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                mModifiedNameMap.put(mPosition,s.toString());
            }
        }
        private Map<Integer, String> mModifiedNameMap = new HashMap<Integer, String>();
        private int mResourceId;
        private int mTouchItemPosition = -1;
        private LayoutInflater mLayoutInflater;
        public DoctorListAdapter(Context context, int textViewResourceId, List<DoctorList.DoctorItem> objects) {
            super(context, textViewResourceId, objects);
            this.mResourceId = textViewResourceId;
            this.mLayoutInflater = LayoutInflater.from(context);
        }

        public String getKeyword(Integer position) {
            String keyword = mModifiedNameMap.get(position);
            return keyword != null ? keyword : getItem(position).doctorName;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            DoctorList.DoctorItem doctor = getItem(position);
            if (doctor == null) {
                return null;
            }
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(mResourceId, null);
                viewHolder = new ViewHolder();
                viewHolder.name = (EditText) convertView.findViewById(R.id.doctor_list_item_name);
                viewHolder.title = (TextView) convertView.findViewById(R.id.doctor_list_item_title);
                viewHolder.skill = (TextView) convertView.findViewById(R.id.doctor_list_item_skill);
                viewHolder.remainNum = (TextView) convertView.findViewById(R.id.doctor_list_item_remain_num);

                viewHolder.name.setTag(position);
                View.OnTouchListener onTouchListener = new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        mTouchItemPosition = (Integer) view.getTag();
                        return false;
                    }
                };
                viewHolder.name.setOnTouchListener(onTouchListener);

                viewHolder.textWatcher = new MyTextWatcher(position);
                viewHolder.name.addTextChangedListener(viewHolder.textWatcher);

                viewHolder.selectRatioButton = (Button)convertView.findViewById(R.id.doctor_list_item_select_rb);
                viewHolder.selectRatioButton.setTag(position);
                viewHolder.selectRatioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClick(null, null, (Integer) v.getTag(), 0);
                    }
                });
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.textWatcher.updatePos(position);

            if(mModifiedNameMap.get(position) != null){
                viewHolder.name.setText(mModifiedNameMap.get(position));
            } else {
                viewHolder.name.setText(doctor.doctorName);
            }
            viewHolder.title.setText(doctor.doctorTitleName);
            viewHolder.skill.setText(doctor.skill);
            viewHolder.remainNum.setText(doctor.remainAvailableNumber);
            if (doctor.remainAvailableNumber.equals("0")) {
                viewHolder.selectRatioButton.setEnabled(false);
            }

            if (mTouchItemPosition == position) {
                viewHolder.name.requestFocus();
                viewHolder.name.setSelection(viewHolder.name.getText().length());
            } else {
                viewHolder.name.clearFocus();
            }
            return convertView;
        }
    }
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    MainActivity mActivity = null;
    String mHpid;
    String mKeid;
    String mDate4GetDL;
    String mDutyCode;
    String mWeekDay;

    DoctorGetter mDoctorGetter=null;

    /*
    Map<String, Map<String, List<DoctorList.DoctorItem>>> mockDoctorList = new HashMap<String, Map<String, List<DoctorList.DoctorItem>>>()
    {
        {
            put("1400116", new HashMap<String, List<DoctorList.DoctorItem>>(){
                {
                    put("星期1",new ArrayList<DoctorList.DoctorItem>(){
                        {
                            add(new DoctorList.DoctorItem("", "刘欣燕"));
                        }
                    });
                    put("星期2",new ArrayList<DoctorList.DoctorItem>(){
                        {
                            add(new DoctorList.DoctorItem("", "朱兰"));
                            add(new DoctorList.DoctorItem("", "陈飞"));
                        }
                    });
                    put("星期3",new ArrayList<DoctorList.DoctorItem>(){
                        {
                            add(new DoctorList.DoctorItem("", "金力"));
                        }
                    });
                    put("星期4",new ArrayList<DoctorList.DoctorItem>(){
                        {
                            add(new DoctorList.DoctorItem("", "李艳"));
                        }
                    });
                    put("星期5",new ArrayList<DoctorList.DoctorItem>(){
                        {
                            add(new DoctorList.DoctorItem("", "陈蔚琳"));
                            add(new DoctorList.DoctorItem("", "冷金花"));
                        }
                    });
                }
            });
            put("1220010", new HashMap<String, List<DoctorList.DoctorItem>>() {
                {
                    put("星期1", new ArrayList<DoctorList.DoctorItem>() {
                        {
                            add(new DoctorList.DoctorItem("", "医师"));
                        }
                    });
                    put("星期2", new ArrayList<DoctorList.DoctorItem>() {
                        {
                            add(new DoctorList.DoctorItem("", "医师"));
                        }
                    });
                    put("星期3", new ArrayList<DoctorList.DoctorItem>() {
                        {
                            add(new DoctorList.DoctorItem("", "医师"));
                        }
                    });
                    put("星期4", new ArrayList<DoctorList.DoctorItem>() {
                        {
                            add(new DoctorList.DoctorItem("", "医师"));
                        }
                    });
                    put("星期5", new ArrayList<DoctorList.DoctorItem>() {
                        {
                            add(new DoctorList.DoctorItem("", "教授"));
                        }
                    });
                }
            });
        }
    };
    */
    private Handler mMessageHandler = null;
    private OnDoctorFragmentListener mListener = null;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    public DoctorList mDoctorList = null;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private DoctorListAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static DoctorFragment newInstance(String param1, String param2) {
        DoctorFragment fragment = new DoctorFragment();
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
    public DoctorFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mActivity = (MainActivity)getActivity();
        mHpid = mActivity.mCurHospitalID;
        mKeid = mActivity.mCurDepID;
        mDate4GetDL = mActivity.mDateItem.date;
        mDutyCode = mActivity.mDateItem.dutyCode;
        mWeekDay = mActivity.mDateItem.weekDay;

        //getDoctorList();Mock doctor list

        View view = inflater.inflate(R.layout.fragment_doctor, container, false);
        mListView = (AbsListView) view.findViewById(android.R.id.list);

        // Set the adapter
        mDoctorList = new DoctorList();
        mAdapter = new DoctorListAdapter(getActivity(),
                R.layout.array_adapter_doctor_list, mDoctorList.ITEMS);
        ((AdapterView<ListAdapter>)mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        // use ratio button click to replace listview ItemClick listener
        // mListView.setOnItemClickListener(this);

        Looper looper = Looper.myLooper();
        mMessageHandler = new MessageHandler(looper);
        mDoctorGetter = new DoctorGetter(mDate4GetDL,mDutyCode,mHpid,mKeid,mMessageHandler);
        //DoctorList.clear();
        //mAdapter.notifyDataSetChanged();
        getDoctorListByHTTP();
        return view;
    }

    @Override
    public void onDestroyView()
    {
        mDoctorGetter.stop();
        super.onDestroyView();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDoctorFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDoctorFragmentListener");
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
            String keyword = mAdapter.getKeyword(position);
            mListener.onDoctorItemClick(mDoctorList.ITEMS.get(position), keyword);
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

    /*
    private void getDoctorList()
    {
        Map<String, List<DoctorList.DoctorItem>> date2DoctorList = mockDoctorList.get(mKeid);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d = null;
        try {
            d = sdf.parse(mDate1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.setTime(d);
        String weekDay = "星期"+(c.get(Calendar.DAY_OF_WEEK)-1);
        List<DoctorList.DoctorItem> doctorList = date2DoctorList.get(weekDay);
        DoctorList.clear();
        for (Integer i=0; i < doctorList.size(); ++i)
        {
            DoctorList.addItem(doctorList.get(i));
        }
    }
    */

    private void getDoctorListByHTTP()
    {
        mDoctorGetter.start();
    }
    class MessageHandler extends Handler {
        public MessageHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            String canSubscribeRet = (String) msg.obj;
            Log.d("DoctorMessage", canSubscribeRet);

            //add common regex pattern as one doctoritem
            String pattern = ".+";
            mDoctorList.addItem(new DoctorList.DoctorItem(pattern));

            boolean ret = getDoctorListFromJson(canSubscribeRet);
            if (ret)
            {
                mAdapter.notifyDataSetChanged();
                return;
            }
            String k = mHpid + "_" + mKeid + "_" + mWeekDay + "_" + mDutyCode;
            Set<String> doctorSet = mActivity.mDoctorListSP.getStringSet(k,null);
            if (doctorSet != null)
            {
                for(String doctorName : doctorSet)
                {
                    mDoctorList.addItem(new DoctorList.DoctorItem(doctorName));
                }
                mAdapter.notifyDataSetChanged();
            }
        }

        private boolean getDoctorListFromJson(String jsonStr)
        {
            if (jsonStr.isEmpty())
            {
                return false;
            }
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                boolean hasError = jsonObj.getBoolean("hasError");
                if (hasError) {
                    String msg = jsonObj.getString("msg");
                    ((MainActivity)getActivity()).showModelDialog(msg);
                    return false;
                }
                JSONArray dataItems = jsonObj.getJSONArray("data");
                for (int i = 0; i < dataItems.length(); i++) {
                    JSONObject dataItem = dataItems.getJSONObject(i);
                    String dutySourceId = dataItem.getString("dutySourceId");
                    String doctorId = dataItem.getString("doctorId");
                    String doctorName = dataItem.getString("doctorName");
                    String doctorTitleName = dataItem.getString("doctorTitleName");
                    String totalFee = dataItem.getString("totalFee");
                    String skill = dataItem.getString("skill");
                    String remainAvailableNumber = dataItem.getString("remainAvailableNumber");
                    mDoctorList.addItem(new DoctorList.DoctorItem(
                            dutySourceId,
                            doctorId,
                            doctorName,
                            doctorTitleName,
                            totalFee,
                            skill,
                            remainAvailableNumber));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            if (mDoctorList.ITEMS.size() > 0) {
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
    public interface OnDoctorFragmentListener {
        // TODO: Update argument type and name
        public void onDoctorItemClick(DoctorList.DoctorItem doctorItem, String keyword);
    }

}
