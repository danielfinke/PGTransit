package com.finke.pgtransit;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PagerContainerFragment extends Fragment {
    private Fragment mReplacementFragment;

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

    /**
     *
     */
    public void setReplacementFragment(Fragment replacementFragment) {
        mReplacementFragment = replacementFragment;
    }
}
