package com.imagine.go.adapter;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.imagine.go.view.WeatherFragment;

public class WeatherPagerAdapter extends FragmentPagerAdapter {
	private List<WeatherFragment> fragments;

	public WeatherPagerAdapter(FragmentManager fm,
			List<WeatherFragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}

	@Override
	public Fragment getItem(int position) {
		return fragments.get(position);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}

}
