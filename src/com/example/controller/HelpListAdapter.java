package com.example.controller;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HelpListAdapter extends BaseExpandableListAdapter
{
	public ArrayList<String> headerItems = new ArrayList<String>();
	public ArrayList<String> subItems = new ArrayList<String>();
	public LayoutInflater inflater;
	
	public HelpListAdapter(ArrayList<String> _headerItems, ArrayList<String> _subItems, LayoutInflater _inflater)
	{
		headerItems = _headerItems;
		subItems = _subItems;
		inflater = _inflater;
	}
	
	@Override
	public Object getChild(int arg0, int arg1) 
	{
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) 
	{
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) 
	{
		if (convertView == null)
			convertView = inflater.inflate(R.layout.help_list_sub_item, null);
		
		((TextView) convertView.findViewById(R.id.txt_help_list_sub_text)).setText(subItems.get(groupPosition));
		
		convertView.setPadding(40, 10, 40, 10);
		
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) 
	{
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) 
	{
		return null;
	}

	@Override
	public int getGroupCount()
	{
		return headerItems.size();
	}

	@Override
	public long getGroupId(int groupPosition) 
	{
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) 
	{
		if (convertView == null)
			convertView = inflater.inflate(R.layout.help_list_header, null);

		((TextView) convertView.findViewById(R.id.txt_help_list_header_text)).setText(headerItems.get(groupPosition));
		
		if (isExpanded)
			((ImageView) convertView.findViewById(R.id.img_help_list_header_icon)).setImageResource(R.drawable.ic_up_arrow);
		else
			((ImageView) convertView.findViewById(R.id.img_help_list_header_icon)).setImageResource(R.drawable.ic_down_arrow);
		
		return convertView;
	}

	@Override
	public boolean hasStableIds() 
	{
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) 
	{
		return true;
	}
}