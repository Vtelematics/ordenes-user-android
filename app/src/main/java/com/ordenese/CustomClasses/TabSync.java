package com.ordenese.CustomClasses;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.ordenese.DataSets.Category;

import java.util.ArrayList;

public class TabSync extends RecyclerView.OnScrollListener implements TabLayout.OnTabSelectedListener {

    TabLayout tabLayout;
    RecyclerView recyclerView;
    boolean userScroll = true;
    //    boolean userSelect = false;
    LinearLayoutManager linearLayoutManager;
    ArrayList<Category> arrayList;

    public TabSync(TabLayout tabLayout, RecyclerView recyclerView, LinearLayoutManager linearLayoutManager, ArrayList<Category> arrayList) {
        this.tabLayout = tabLayout;
        this.recyclerView = recyclerView;
        this.linearLayoutManager = linearLayoutManager;
        this.arrayList = arrayList;

    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (userScroll) {
            int position = tab.getPosition();
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            if (linearLayoutManager != null) {
                linearLayoutManager.setSmoothScrollbarEnabled(true);
                linearLayoutManager.scrollToPositionWithOffset(position, 0);
            }
//            recyclerView.scrollToPosition(position);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        int firstVisiblePosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        if (userScroll && tabLayout.getSelectedTabPosition() != firstVisiblePosition) {
            tabLayout.getTabAt(firstVisiblePosition).select();
        }
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
//            userSelect = false;
//        }
    }
}
