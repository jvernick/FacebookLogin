package com.example.justin12.firstgame;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;

/**
 * Created by Justin12 on 1/17/2015.
 */
public abstract class BaseListElement {
    private Drawable icon;
    private String text1;
    private String text2;
    private int requestCode;
    private BaseAdapter adapter;
    protected abstract View.OnClickListener getOnClickListener();

    public BaseListElement(Drawable icon, String text1, String text2, int requestCode) {
        super();
        this.icon = icon;
        this.text1 = text1;
        this.text2 = text2;
        this.requestCode = requestCode;
    }

    public String getText1() {
        return text1;
    }

    public String getText2() {
        return text2;
    }

    public void setText1(String text) {
        text1 = text;
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void setText2(String text) {
        text2 = text;
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public Drawable getIcon() {
        return icon;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setAdapter(BaseAdapter adapter) {
        this.adapter = adapter;
    }

    protected void onActivityResult(Intent data) {}

    protected void onSaveInstanceState(Bundle bundle) {}

    protected boolean restoreState(Bundle savedState) {
        return false;
    }

    protected void notifyDataChanged() {
        adapter.notifyDataSetChanged();
    }

}
