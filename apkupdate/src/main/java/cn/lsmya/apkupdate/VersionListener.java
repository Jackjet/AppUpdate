package cn.lsmya.apkupdate;

public interface VersionListener {

    abstract void onUpdate(VersionBeen been, String response);

    abstract void onNotUpdate();
}
