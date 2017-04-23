package com.tianxing.userapps.guahao5;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnLoginFragmentListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {
    private UserLoginTask mAuthTask = null;
    private GetCodeTask mGetCodeTask = null;
    private Handler mMessageHandler = null;
    // UI references.
    private AutoCompleteTextView mEditTextName;
    private AutoCompleteTextView mEditTextPassword;
    private AutoCompleteTextView mEditTextPatient;
    private AutoCompleteTextView mEditTextPatientMedicareIDCard;
    private ImageView mImageViewLoginCode;
    private TextView mTextViewErrorMsg;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnLoginFragmentListener mListener;

    private Set<String> mName2passwordSet;
    private Map<String, String> mName2password = new HashMap<String, String>();
    private String [] mNames = null;

    private Set<String> mPatient2MedicareIDCardSet;
    private Map<String, String> mPatient2MedicareIDCard = new HashMap<String, String>();
    private String [] mPatients = null;

    private String mName;
    private String mPassword;
    private String mPatient;
    private String mPatientMedicareIDCard;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        //mListener.onLoginSuccess();

        // Reset errors.
        mEditTextName.setError(null);
        mEditTextPassword.setError(null);

        // Store values at the time of the login attempt.
        mName = mEditTextName.getText().toString();
        mPassword = mEditTextPassword.getText().toString();
        mPatient = mEditTextPatient.getText().toString();
        mPatientMedicareIDCard = mEditTextPatientMedicareIDCard.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(mName)) {
            mEditTextName.setError(getString(R.string.require_prompt));
            focusView = mEditTextName;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mPassword)) {
            mEditTextPassword.setError(getString(R.string.require_prompt));
            focusView = mEditTextPassword;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTask = new UserLoginTask(mName, mPassword, "");
            mAuthTask.execute((Void) null);
        }
    }
    private void fillDataBySP() {
        mName2passwordSet = ((MainActivity)getActivity()).mLoginInfoSP.getStringSet("login_info", null);
        if (mName2passwordSet != null) {
            for(String name : mName2passwordSet) {
                String [] splited = name.split(",");
                mName2password.put(splited[0], splited[1]);
            }
        }
        if (!mName2password.isEmpty())  {
            mNames = new String[mName2password.keySet().size()];
            mName2password.keySet().toArray(mNames);
        } else {
            mNames = new String[0];
        }

        mPatient2MedicareIDCardSet = ((MainActivity)getActivity()).mLoginInfoSP.getStringSet("patient2MedicareIDCard", null);
        if (mPatient2MedicareIDCardSet != null) {
            for(String name : mPatient2MedicareIDCardSet) {
                String [] splited = name.split(",");
                mPatient2MedicareIDCard.put(splited[0], splited[1]);
            }
        }
        if (!mPatient2MedicareIDCard.isEmpty())  {
            mPatients = new String[mPatient2MedicareIDCard.keySet().size()];
            mPatient2MedicareIDCard.keySet().toArray(mPatients);
        } else {
            mPatients = new String[0];
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mEditTextName = (AutoCompleteTextView)view.findViewById(R.id.editTextName);

        fillDataBySP();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, mNames);
        mEditTextName.setAdapter(arrayAdapter);
        mEditTextName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mEditTextName.showDropDown();//显示下拉列表
                return false;
            }
        });
        mEditTextName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String username = mEditTextName.getText().toString();
                mEditTextPassword.setText(mName2password.get(username));
            }
        });

        mEditTextPassword = (AutoCompleteTextView)view.findViewById(R.id.editTextPassword);
        mEditTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

        mEditTextPatient = (AutoCompleteTextView)view.findViewById(R.id.editTextPatient);
        ArrayAdapter<String> patientsAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1,mPatients);
        mEditTextPatient.setAdapter(patientsAdapter);
        mEditTextPatient.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mEditTextPatient.showDropDown();//显示下拉列表
                return false;
            }
        });
        mEditTextPatientMedicareIDCard = (AutoCompleteTextView)view.findViewById(R.id.editTextPatientMedicareIDCard);
        mEditTextPatient.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String patient = mEditTextPatient.getText().toString();
                mEditTextPatientMedicareIDCard.setText(mPatient2MedicareIDCard.get(patient));
            }
        });


        mTextViewErrorMsg = (TextView)view.findViewById(R.id.textViewErrorMsg);
        //set getLoginCode()' result to imageview;

        /*
        mImageViewLoginCode = (ImageView)view.findViewById(R.id.imageViewLoginCode);
        mImageViewLoginCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetCodeTask = new GetCodeTask();
                mGetCodeTask.execute((Void) null);
            }
        });
        //mGetCodeTask = new GetCodeTask();;
        //mGetCodeTask.execute((Void) null);
        mEditTextLoginCode = (EditText)view.findViewById(R.id.editTextLoginCode);
        */

        final Button mLoginButton = (Button) view.findViewById(R.id.buttonLogin);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mLoginButton set non-click
                attemptLogin();
            }
        });
        Looper looper = Looper.myLooper();
        mMessageHandler = new MessageHandler(looper);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onLoginSuccess(mName, mPassword, mPatient, mPatientMedicareIDCard);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnLoginFragmentListener) activity;
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
    public interface OnLoginFragmentListener {
        // TODO: Update argument type and name
        public void onLoginSuccess(String name, String password, String patient, String patientIDCard);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mName;
        private final String mPassword;
        private final String mCode;
        public String mErrorMsg;

        UserLoginTask(String name, String password, String code) {
            mName = name;
            mPassword = password;
            mCode = code;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //attempt authentication against a network service.
            try {
                return login();
            } catch (InterruptedException e) {
                return false;
            }
        }

        protected  boolean loginMobile(String[] msg) throws InterruptedException
        {
            Map headers = new HashMap();
            headers.put("Referer", HTTPSessionStatus.URL_INDEX_MOBILE);
            headers.put("Accept-Encoding", "gzip, deflate");
            headers.put("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("mobileNo", mName));
            formparams.add(new BasicNameValuePair("password", mPassword));
            formparams.add(new BasicNameValuePair("yzm", mCode));
            formparams.add(new BasicNameValuePair("isAjax", "true"));
            String url = HTTPSessionStatus.URL_LOGIN_MOBILE;
            return loginByHTTPClientWrapper(url, headers, formparams, msg);
        }
        protected void preLogin(){
            String preUrl = HTTPSessionStatus.URL_MOBILE_BASE + "IdentityServlet";
            Map headers = new HashMap();
            headers.put("Referer", HTTPSessionStatus.URL_INDEX_MOBILE);
            headers.put("Accept-Encoding", "gzip, deflate");
            headers.put("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
            headers.put("Accept", "image/webp,image/*,*/*;q=0.8");

            Map param = new HashMap();
            param.put("ts", System.currentTimeMillis());
            param.put("channel", "yzdl");

            HttpEntity entity = HTTPClientWrapper.getInstance().doGet(preUrl,param,headers);
        }
        protected boolean login() throws InterruptedException{
            //preLogin();
            String[] msg = new String[1];
            msg[0] = "login error";
            boolean ret = loginMobile(msg);
            if (!ret) {
                mErrorMsg = msg[0];
            }
            return ret;
        }
        protected boolean loginByHTTPClientWrapper(String url,Map headers,List<NameValuePair> formparams, String[] msg) throws InterruptedException{
            HttpEntity entity = HTTPClientWrapper.getInstance().doPost(url,formparams,headers, HTTP.UTF_8);
            try {
                if (entity == null)
                {
                    Log.e("login", "return entity null");
                    return false;
                }
                String result = EntityUtils.toString(entity, "GBK");
                Log.d("login", result);
                JSONObject jsonObj = new JSONObject(result);
                Boolean hasError = jsonObj.getBoolean("hasError");
                Integer code = jsonObj.getInt("code");
                if (hasError || code != 200) {
                    msg[0] = jsonObj.getString("msg");
                    return false;
                }
                return true;
            }
            catch (Exception e) {
                //strResult = e.getMessage().toString();
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            if (success) {
                //save cookie and switch to hospital layout
                mListener.onLoginSuccess(mName, mPassword, mPatient, mPatientMedicareIDCard);
            } else {
                mTextViewErrorMsg.setText(mErrorMsg);
                mTextViewErrorMsg.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    public class GetCodeTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            InputStream getCodeStream = getLoginCode();
            if (getCodeStream != null) {
                Bitmap verifyIcon = BitmapFactory.decodeStream(getCodeStream);
                Message message = Message.obtain();
                message.obj = verifyIcon;
                mMessageHandler.sendMessage(message);
                return true;
            }
            return false;
        }
        protected InputStream getLoginCodeByMobile()
        {
            Map headers = new HashMap();
            headers.put("Referer", HTTPSessionStatus.URL_INDEX_MOBILE);
            headers.put("X-Requested-With","com.android.browser");
            String url = HTTPSessionStatus.URL_GETLOGINCODE_MOBILE + "?id=" + Math.random();
            return getLoginCodeByHTTPClientWrapper(headers, url);
        }
        protected InputStream getLoginCodePC()
        {
            Map headers = new HashMap();
            headers.put("Referer", HTTPSessionStatus.URL_INDEX);
            String url = HTTPSessionStatus.URL_GETLOGINCODE + "?id=" + Math.random();
            return getLoginCodeByHTTPClientWrapper(headers, url);
        }
        protected InputStream getLoginCodeByHTTPClientWrapper(Map headers, String url) {
            HttpEntity entity = HTTPClientWrapper.getInstance().doGet(url, new HashMap(), headers);
            if (entity == null)
            {
                return null;
            }
            try {
                return entity.getContent();
            } catch (IOException e) {
                //strResult = e.getMessage().toString();
                e.printStackTrace();
            }
            return null;
        }
        protected InputStream getLoginCode() {
            return getLoginCodeByMobile();
        }
    }

    class MessageHandler extends Handler {
        public MessageHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Bitmap verifyIcon = (Bitmap)msg.obj;
            mImageViewLoginCode.setImageBitmap(verifyIcon);
            mGetCodeTask = null;
        }
    }
}
