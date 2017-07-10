/*
* Copyright 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.appusagestatistics.fragments;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.appusagestatistics.R;
import com.example.android.appusagestatistics.adapters.UsageListAdapter;
import com.example.android.appusagestatistics.models.DisplayUsageEvent;
import com.example.android.appusagestatistics.utils.FormatCustomUsageEvents;

import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AppUsageStatisticsFragment extends LifecycleFragment {

    private static final String TAG = AppUsageStatisticsFragment.class.getSimpleName();
    @BindView(R.id.recyclerview_app_usage)
    protected RecyclerView mRecyclerView;
    @BindView(R.id.button_open_usage_setting)
    protected Button mOpenUsageSettingButton;
    @BindArray(R.array.exclude_packages)
    protected String[] excludePackages;

    private UsageStatsManager mUsageStatsManager;
    private Unbinder unbinder;
    private FormatCustomUsageEvents formatCustomUsageEvents;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment {@link AppUsageStatisticsFragment}.
     */
    public static AppUsageStatisticsFragment newInstance() {
        return new AppUsageStatisticsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT > 21)
            mUsageStatsManager = (UsageStatsManager) getActivity()
                    .getSystemService(Context.USAGE_STATS_SERVICE);
        else
            mUsageStatsManager = (UsageStatsManager) getActivity()
                    .getSystemService("usagestats");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_usage_statistics, container, false);
        unbinder = ButterKnife.bind(this, view);
        excludePackages[excludePackages.length - 1] = findLauncher();
        return view;
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        UsageListAdapter mUsageListAdapter = new UsageListAdapter(this);
        mRecyclerView.scrollToPosition(0);
        mRecyclerView.setAdapter(mUsageListAdapter);
        formatCustomUsageEvents = ViewModelProviders
                .of(this)
                .get(FormatCustomUsageEvents.class);

        formatCustomUsageEvents
                .getDisplayUsageEventsList()
                .observe(this, events -> {
                    if (events == null) {
                        Log.i(TAG, "The user may not have allowed access to apps usage.");
                        Toast.makeText(getActivity(),
                                getString(R.string.explanation_access_to_appusage_is_not_enabled),
                                Toast.LENGTH_LONG).show();
                        mOpenUsageSettingButton.setVisibility(View.VISIBLE);
                        mOpenUsageSettingButton.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
                    } else {
                        mOpenUsageSettingButton.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        formatCustomUsageEvents
                .setDisplayUsageEventsList(mUsageStatsManager, excludePackages);
    }

    private String findLauncher() {
        PackageManager localPackageManager = getContext().getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        return localPackageManager.resolveActivity(intent,
                PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
    }
}
