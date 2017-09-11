package com.finke.pgtransit;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.finke.pgtransit.extensions.OnBackPressedListener;

public class PagerContainerFragment extends Fragment implements
        OnBackPressedListener {

    private static final String CHILD_FRAGMENT_KEY = "CHILD_FRAGMENT";

    private Fragment mReplacementFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            mReplacementFragment = getChildFragmentManager().getFragment(savedInstanceState, CHILD_FRAGMENT_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.blank_pager_view, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(mReplacementFragment != null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.container, mReplacementFragment)
                    .commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        getChildFragmentManager().putFragment(outState, CHILD_FRAGMENT_KEY, mReplacementFragment);
    }

    /**
     *
     */
    public void setReplacementFragment(Fragment replacementFragment) {
        mReplacementFragment = replacementFragment;
    }

    @Override
    public boolean onBackPressed() {
        if(mReplacementFragment instanceof OnBackPressedListener) {
            return ((OnBackPressedListener) mReplacementFragment).onBackPressed();
        }
        return false;
    }

}
