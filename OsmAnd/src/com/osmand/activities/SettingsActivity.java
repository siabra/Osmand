package com.osmand.activities;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;

import com.osmand.OsmandSettings;
import com.osmand.R;
import com.osmand.map.TileSourceManager;
import com.osmand.map.TileSourceManager.TileSourceTemplate;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener {
	
	private CheckBoxPreference showPoiOnMap;
	private CheckBoxPreference useInternetToDownloadTiles;
	private ListPreference tileSourcePreference;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_pref);
		PreferenceScreen screen = getPreferenceScreen();
		useInternetToDownloadTiles = (CheckBoxPreference) screen.findPreference(OsmandSettings.USE_INTERNET_TO_DOWNLOAD_TILES);
		useInternetToDownloadTiles.setOnPreferenceChangeListener(this);
		showPoiOnMap =(CheckBoxPreference) screen.findPreference(OsmandSettings.SHOW_POI_OVER_MAP);
		showPoiOnMap.setOnPreferenceChangeListener(this);
		
		tileSourcePreference =(ListPreference) screen.findPreference(OsmandSettings.MAP_TILE_SOURCES);
		tileSourcePreference.setOnPreferenceChangeListener(this);
		
		
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	useInternetToDownloadTiles.setChecked(OsmandSettings.isUsingInternetToDownloadTiles(this));
    	showPoiOnMap.setChecked(OsmandSettings.isShowingPoiOverMap(this));
    	
    	List<TileSourceTemplate> list = TileSourceManager.getKnownSourceTemplates();
    	String[] entries = new String[list.size()];
    	for(int i=0; i<list.size(); i++){
    		entries[i] = list.get(i).getName();
    	}
    	
    	tileSourcePreference.setEntries(entries);
    	tileSourcePreference.setEntryValues(entries);
    	tileSourcePreference.setValue(OsmandSettings.getMapTileSourceName(this));
    	tileSourcePreference.setSummary(tileSourcePreference.getSummary() + "\t\t[" + OsmandSettings.getMapTileSourceName(this)+"]");
    	
    }
    

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		SharedPreferences prefs = getSharedPreferences(OsmandSettings.SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
		Editor edit = prefs.edit();
		if(preference == showPoiOnMap){
			edit.putBoolean(OsmandSettings.SHOW_POI_OVER_MAP, (Boolean) newValue);
			edit.commit();
		} else if(preference == useInternetToDownloadTiles){
			edit.putBoolean(OsmandSettings.USE_INTERNET_TO_DOWNLOAD_TILES, (Boolean) newValue);
			edit.commit();
		} else if (preference == tileSourcePreference) {
			edit.putString(OsmandSettings.MAP_TILE_SOURCES, (String) newValue);
			edit.commit();
		}
		return true;
	}

}