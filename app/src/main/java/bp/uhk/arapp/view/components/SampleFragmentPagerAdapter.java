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
    static HomeTab homeTab;
    static MapTab mapTab;
    final int PAGE_COUNT = 2;
    private Context context;

    public SampleFragmentPagerAdapter(FragmentManager fm, Context context)
    {
        super(fm);
        this.context = context;

        if (homeTab == null)
        {
            homeTab = new HomeTab();
            homeTab.setRetainInstance(true);
        }
        if (mapTab == null)
        {
            mapTab = new MapTab();
            mapTab.setRetainInstance(true); //potřebuji, aby se nevytvořil nový fragment po recreate(), jinak se na něj přes SampleFragmentPagerAdapter nedostanu
        }
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
            return homeTab;
        }
        else
        {
            return mapTab;
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
