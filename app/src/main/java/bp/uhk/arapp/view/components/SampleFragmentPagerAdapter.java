package bp.uhk.arapp.view.components;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import bp.uhk.arapp.R;
import bp.uhk.arapp.view.tabs.HomeTab;
import bp.uhk.arapp.view.tabs.MapTab;

public class SampleFragmentPagerAdapter extends FragmentPagerAdapter
{
    private final int PAGE_COUNT = 2;
    private HomeTab homeTab;
    private MapTab mapTab;
    private Context context;

    public SampleFragmentPagerAdapter(FragmentManager fm, Context context)
    {
        super(fm);
        this.context = context;

        homeTab = new HomeTab();
        mapTab = new MapTab();
    }

    private Fragment getHomeTabInstance()
    {
        if (homeTab == null)
        {
            homeTab = new HomeTab();
        }
        return homeTab;
    }

    private Fragment getMapTabInstance()
    {
        if (mapTab == null)
        {
            mapTab = new MapTab();
        }
        return mapTab;
    }

    @Override
    public int getCount()
    {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position)
    {
        if (position == 0)
        {
            return getHomeTabInstance();
        }
        else
        {
            return getMapTabInstance();
        }
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        // Generate title based on item position
        if (position == 0)
        {
            return context.getString(R.string.tab_home);
        }
        else
        {
            return context.getString(R.string.tab_map);
        }
    }
}
