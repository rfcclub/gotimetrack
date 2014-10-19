package com.gotako.gotimetrack.fragment;

public interface IFragment {
    public void callback(Object obj);

    public void actionMenu(int actionId);

    public void refresh();

    public void fragmentSelected();
}
