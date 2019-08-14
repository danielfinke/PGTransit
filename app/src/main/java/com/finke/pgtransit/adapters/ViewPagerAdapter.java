package com.finke.pgtransit.adapters;

import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.finke.pgtransit.MapFragment;
import com.finke.pgtransit.PagerContainerFragment;
import com.finke.pgtransit.RoutesFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private static final int COUNT = 2;

    private PagerContainerFragment[] mContainers = new PagerContainerFragment[2];

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                if(mContainers[0] == null) {
                    mContainers[0] = new PagerContainerFragment();
                    mContainers[0].setReplacementFragment(new RoutesFragment());
                }
                return mContainers[0];
            default: // case 1
                if(mContainers[1] == null) {
                    mContainers[1] = new PagerContainerFragment();
                    mContainers[1].setReplacementFragment(new MapFragment());
                }
                return mContainers[1];
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PagerContainerFragment createdFragment = (PagerContainerFragment)super.instantiateItem(container, position);
        mContainers[position] = createdFragment;
        return createdFragment;
    }

    @Override
    public int getCount() {
        return COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch(position) {
            case 0:
                return "Routes";
            default: // case 1
                return "Map";
        }
    }
}
