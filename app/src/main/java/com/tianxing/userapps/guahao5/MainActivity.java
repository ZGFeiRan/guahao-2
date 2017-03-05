package com.tianxing.userapps.guahao5;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;

import com.tianxing.userapps.guahao5.dummy.DateItem;
import com.tianxing.userapps.guahao5.dummy.DateList;
import com.tianxing.userapps.guahao5.dummy.DoctorList;
import com.tianxing.userapps.guahao5.dummy.FavorateList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity implements LoginFragment.OnLoginFragmentListener,HospitalFragment.OnHospitalFragmentListener,
        DepartmentFragment.OnDepFragmentListener,DateFragment.OnDateFragmentListener,
        DoctorFragment.OnDoctorFragmentListener,SubscribeMobileFragment.OnSubscribeFragmentListener,
        FavorateFragment.OnFavorateFragmentListener
{
    private LoginFragment mLoginFragment;
    private HospitalFragment mHospitalFragment;
    private DepartmentFragment mDepFragment;
    private DateFragment mDateFragment;
    private DoctorFragment mDoctorFragment;
    private SubscribeMobileFragment mSubscribeFragment;
    private FavorateFragment mFavorateFragment;

    private List<Fragment> mFragmentList = new ArrayList<Fragment>();

    private Fragment mCurFragment = null;
    private FragmentManager fm = getFragmentManager();

    public String mCurDepID;
    public String mCurHospitalID;
    public String mCurDate;
    public String mCurHospitalName;
    public String mCurDepName;
    public String mDoctorKeyword;
    public String mPatient;

    public DateItem mDateItem;
    public DoctorList.DoctorItem mDoctorItem;

    public SharedPreferences mDoctorListSP;
    public SharedPreferences.Editor mDoctorListEditor;
    public SharedPreferences mFavHpKsSP;
    public SharedPreferences.Editor  mFavHpKsEditor;
    public SharedPreferences mLoginInfoSP;
    public SharedPreferences.Editor  mLoginInfoEditor;

    PowerManager.WakeLock wakeLock = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDoctorListSP = this.getSharedPreferences("doctor_list", MODE_PRIVATE);
        mDoctorListEditor = mDoctorListSP.edit();
        mFavHpKsSP = this.getSharedPreferences("fav_hpks_list", MODE_PRIVATE);
        mFavHpKsEditor = mFavHpKsSP.edit();
        mLoginInfoSP = this.getSharedPreferences("login_info_list", MODE_PRIVATE);
        mLoginInfoEditor = mLoginInfoSP.edit();

        if (savedInstanceState == null) {
            if (mLoginFragment == null) {
                mLoginFragment = new LoginFragment();
                mFragmentList.add(mLoginFragment);
            }
            setFragment(false, mLoginFragment);
        }
        acquireWakeLock();
    }

    @Override
    protected void onDestroy()
    {
        releaseWakeLock();
        super.onDestroy();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        MenuItem favDoctorlistItem=menu.findItem(R.id.action_fav_doctorlist);
        MenuItem favHpidksidItem=menu.findItem(R.id.action_fav_hpid_ksid);
        MenuItem openFavHpidksidItem=menu.findItem(R.id.action_open_fav_hpid_ksid);
        favDoctorlistItem.setEnabled(false);
        favHpidksidItem.setEnabled(false);
        openFavHpidksidItem.setEnabled(false);
        setCurFragment();
        if (mCurFragment instanceof DoctorFragment)
        {
            favDoctorlistItem.setEnabled(true);
        }
        else if (mCurFragment instanceof DateFragment)
        {
            favHpidksidItem.setEnabled(true);
        }

        if (!(mCurFragment instanceof LoginFragment))
        {
            openFavHpidksidItem.setEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_fav_doctorlist) {
            //if current fragment is doctor fragment
            String k = mCurHospitalID + "_" + mCurDepID + "_" +
                    mDateItem.weekDay + "_" + mDateItem.dutyCode;
            Set<String> doctorSet = new HashSet<String>();
            for(DoctorList.DoctorItem doctorItem : mDoctorFragment.mDoctorList.ITEMS)
            {
                doctorSet.add(doctorItem.doctorName);
            }
            if (!doctorSet.isEmpty()) {
                mDoctorListEditor.putStringSet(k, doctorSet);
                mDoctorListEditor.commit();
                ALogger.getLogger(MainActivity.class).debug("commit doctorSet " + doctorSet.size());
            }
        }
        else if (id == R.id.action_fav_hpid_ksid)
        {
            boolean ret = FavorateList.addItem(new FavorateList.Item(mCurHospitalID, mCurHospitalName, mCurDepID, mCurDepName));
            if (ret)
            {
                String k = mCurHospitalID + "_" + mCurDepID;
                String v = mCurHospitalName + "_" + mCurDepName;
                mFavHpKsEditor.putString(k, v);
                mFavHpKsEditor.commit();
                ALogger.getLogger(MainActivity.class).debug("commit FavHpKs k:" + k + "\nv:" + v);
            }
        }
        else if (id == R.id.action_open_fav_hpid_ksid)
        {
            if (mFavorateFragment == null)
            {
                mFavorateFragment = new FavorateFragment();
                mFragmentList.add(mFavorateFragment);
            }
            setFragment(true, mFavorateFragment);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoginSuccess(String name, String IDCard, String patient)
    {
        // save login info to SP
        String name2IDCard = name + "," + IDCard;
        boolean isExist = false;
        Set<String> name2IDCardSet = mLoginInfoSP.getStringSet("login_info", new HashSet<String>());
        for (String str : name2IDCardSet) {
            if (name.equals(str.split(",")[0])) {
                isExist = true;
            }
        }
        if (!isExist) {
            name2IDCardSet.add(name2IDCard);
            mLoginInfoEditor.putStringSet("login_info", name2IDCardSet);
        }

        Set<String> patientsSet = mLoginInfoSP.getStringSet("patients", new HashSet<String>());
        if (!patientsSet.contains(patient)) {
            patientsSet.add(patient);
            mLoginInfoEditor.putStringSet("patients", patientsSet);
        }
        mLoginInfoEditor.commit();
        mPatient = patient;

        if (mHospitalFragment == null)
        {
            mHospitalFragment = new HospitalFragment();
            mFragmentList.add(mHospitalFragment);
        }
        setFragment(false, mHospitalFragment);
    }

    @Override
    public void onHospitalClick(String id, String name)
    {
        mCurHospitalID = id;
        mCurHospitalName = name;
        if (mDepFragment == null)
        {
            mDepFragment = new DepartmentFragment();
            mFragmentList.add(mDepFragment);
        }
        setFragment(true, mDepFragment);
    }

    @Override
    public void onDepClick(String id, String name)
    {
        mCurDepID = id;
        mCurDepName = name;
        if (mDateFragment == null)
        {
            mDateFragment = new DateFragment();
            mFragmentList.add(mDateFragment);
        }
        setFragment(true, mDateFragment);
    }

    @Override
    public void onDateClick(DateItem dateItem)
    {
        if (mDoctorFragment == null)
        {
            mDoctorFragment = new DoctorFragment();
            mFragmentList.add(mDoctorFragment);
        }
        setFragment(true, mDoctorFragment);
        mCurDate = dateItem.date;
        mDateItem = dateItem;
    }
    @Override
    public void onDoctorItemClick(DoctorList.DoctorItem doctorItem, String keyword)
    {
        mDoctorKeyword = keyword;
        if (mSubscribeFragment == null)
        {
            //mSubscribeFragment = new SubscribeFragment();
            mSubscribeFragment = new SubscribeMobileFragment();
            mFragmentList.add(mSubscribeFragment);
        }
        mDoctorItem = doctorItem;
        setFragment(true, mSubscribeFragment);
    }
    @Override
    public void onFavorateClick(String hpid, String ksid)
    {
        mCurHospitalID = hpid;
        mCurDepID = ksid;
        if (mDateFragment == null)
        {
            mDateFragment = new DateFragment();
            mFragmentList.add(mDateFragment);
        }
        setFragment(true, mDateFragment);
    }
    @Override
    public void onSubscribeSuccess()
    {
    }

    private void setCurFragment()
    {
        mCurFragment = null;
        for (Fragment f : mFragmentList)
        {
            if (f != null && f.isVisible() && !f.isHidden())
            {
                mCurFragment = f;
                break;
            }
        }
    }
    private void setFragment(boolean isToBackStack, Fragment newFrag)
    {
        FragmentTransaction transaction = fm.beginTransaction();
        setCurFragment();

        if(mCurFragment != null) {
            transaction.hide(mCurFragment);
        }
        if (!newFrag.isAdded()) {
            transaction.add(R.id.id_content, newFrag);
        }
        else
        {
            transaction.show(newFrag);
        }

        if (isToBackStack)
        {
            transaction.addToBackStack(null);
        }
        transaction.commit();
        mCurFragment = newFrag;
    }

    //获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
    private void acquireWakeLock()
    {
        if (null == wakeLock)
        {
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "Guahao5Service");
            if (null != wakeLock)
            {
                wakeLock.acquire();
            }
        }
    }

    //释放设备电源锁
    private void releaseWakeLock()
    {
        if (null != wakeLock)
        {
            wakeLock.release();
            wakeLock = null;
        }
    }

    public void showModelDialog(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setMessage(msg)
                .setPositiveButton("cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                })
                .setNegativeButton("关闭", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });
        builder.setCancelable(false);
        builder.show();
    }
}