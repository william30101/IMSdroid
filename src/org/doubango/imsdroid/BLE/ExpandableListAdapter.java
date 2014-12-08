package org.doubango.imsdroid.BLE;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
	 
    private Activity context;
    private BLEDeviceControlActivity DevCon;
    private ArrayList<ArrayList<HashMap<String, String>>>  laptopCollections;
    //private List<String> laptops;
    ArrayList<HashMap<String, String>> laptops;
    
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    
    public static final String TAG = "william";
 
    public ExpandableListAdapter(Activity context, BLEDeviceControlActivity DevCon ,ArrayList<HashMap<String, String>> laptops,
    		ArrayList<ArrayList<HashMap<String, String>>>  laptopCollections) {
        this.context = context;
        this.DevCon = DevCon;
        this.laptopCollections = laptopCollections;
        this.laptops = laptops;
    }
 
    public Object getChild(int groupPosition, int childPosition) {
        return laptopCollections.get(groupPosition).get(childPosition);
    }
    
    public Object getChildName(int groupPosition, int childPosition) {
        return laptopCollections.get(groupPosition).get(childPosition).get("Name");
    }
    
    public Object getChildUUID(int groupPosition, int childPosition) {
        return laptopCollections.get(groupPosition).get(childPosition).get("UUID");
    }
 
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
     
     
    public View getChildView(final int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
       // final String laptop = (String) getChild(groupPosition, childPosition);
    	
    	final String ChildUUID = (String) getChildUUID(groupPosition, childPosition);
    	final String ChildName = (String) getChildName(groupPosition, childPosition);
    	
        LayoutInflater inflater = context.getLayoutInflater();
         
        if (convertView == null) {
            //convertView = inflater.inflate(R.layout.child_item, null);
        }
         
       //TextView cname = (TextView) convertView.findViewById(R.id.cname);
        //cname.setText(ChildName);
        
        //TextView cuuid = (TextView) convertView.findViewById(R.id.cuuid);
        //cuuid.setText(ChildUUID);
        
        
        //ImageView downarrow = (ImageView) convertView.findViewById(R.id.downarrow);
       // downarrow.setOnClickListener(new OnClickListener() {
           /*  
        	public void onClick(View v) {
        		Log.i(TAG,"downarrow click here");
        		DevCon.CharacteristicWRN(groupPosition, childPosition, 0);
        	}
        });
        */
        /*
        ImageView uparrow = (ImageView) convertView.findViewById(R.id.uparrow);
        uparrow.setOnClickListener(new OnClickListener() {
	
        	public void onClick(View v) {
        		Log.i(TAG,"uparrow click here value = "+ChildUUID);
        		DevCon.CharacteristicWRN(groupPosition, childPosition, 1);
            }
             
    	 });
                 
        ImageView noti = (ImageView) convertView.findViewById(R.id.notification);
        noti.setOnClickListener(new OnClickListener() {
	
        	public void onClick(View v) {
        		Log.i(TAG,"noti click here value = "+ChildUUID);
        		DevCon.CharacteristicWRN(groupPosition, childPosition, 2);
            }
             
    	 });

        
        */
        return convertView;
    }
 
    public int getChildrenCount(int groupPosition) {
        return laptopCollections.get(groupPosition).size();
    }
    
    
	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return null;
	}
    
    public Object getGroupUUID(int groupPosition) {
        return laptops.get(groupPosition).get("UUID");
    }
    
    public Object getGroupName(int groupPosition) {
        return laptops.get(groupPosition).get("NAME");
    }
    
    public int getGroupCount() {
        return laptops.size();
    }
 
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {


        String GroupName = (String) getGroupName(groupPosition);
    	//HashMap laptopmap = getGroup(groupPosition);
        String GroupUUID = (String) getGroupUUID(groupPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //convertView = infalInflater.inflate(R.layout.group_item,
       //             null);
        }

        //TextView gname = (TextView) convertView.findViewById(R.id.gname);
        //gname.setTypeface(null, Typeface.BOLD);
        //gname.setText(GroupName);
        
       // TextView guuid = (TextView) convertView.findViewById(R.id.guuid);
       // guuid.setTypeface(null, Typeface.BOLD);
       // guuid.setText(GroupUUID);
        
        return convertView;
    }
 
    public boolean hasStableIds() {
        return true;
    }
 
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }



	
}
