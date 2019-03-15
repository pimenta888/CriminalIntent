package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.util.Date;
import java.util.UUID;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String HAS_CRIME_CHANGED = "com.bignerdranch.android.criminalintent.has_crime_changed";
    private static final String DELETED_CRIME = "com.bignerdranch.android.criminalintent.deleted_crime";
    private static final String DIALOG_DATE = "DialogDate";
    private static final int REQUEST_DATE = 0;

    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private CheckBox mPoliceCheckBox;
    private boolean mHasCrimeChanged = false;
    private boolean mItemRemoved = false;

    public static CrimeFragment newInstance(UUID crimeId){
        Bundle args =new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static boolean hasCrimeChanged(Intent result) {
        return result.getBooleanExtra(HAS_CRIME_CHANGED, false);
    }

    public static UUID getCrimeId(Intent result) {
        return (UUID) result.getSerializableExtra(ARG_CRIME_ID);
    }

    public static boolean itemRemoved(Intent result) {
        return result.getBooleanExtra(DELETED_CRIME, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //this space is intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                mHasCrimeChanged = true;
                returnResult();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //this space is intentionally left blank
            }
        });

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
                mHasCrimeChanged = true;
                returnResult();
            }
        });

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                mHasCrimeChanged = true;
                returnResult();
            }
        });

        mPoliceCheckBox = (CheckBox) v.findViewById(R.id.crime_call_police);
        mPoliceCheckBox.setChecked(mCrime.isRequiresPolice());
        mPoliceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setRequiresPolice(isChecked);
                mHasCrimeChanged = true;
                returnResult();
            }
        });

        return v;
    }

    public void returnResult(){
        Intent data = new Intent();
        data.putExtra(HAS_CRIME_CHANGED, mHasCrimeChanged);
        data.putExtra(ARG_CRIME_ID, mCrime.getId());
        data.putExtra(DELETED_CRIME, mItemRemoved);
        getActivity().setResult(Activity.RESULT_OK, data);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        }
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getDate().toString());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.delete_crime:
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                mItemRemoved = true;
                returnResult();
                getActivity().finish();
                return true;
            case android.R.id.home:
                getActivity().finish();
                mHasCrimeChanged = true;
                returnResult();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

