
package com.android.ui;

import com.android.iphonelauncher.DragController;
import com.android.iphonelauncher.Launcher;

public interface IphoneShortcutCallback {

    public void setReflectionEffect(boolean has);

    public void setLauncher(Launcher launcher);

    public void removeSelfInParent(DragController dragController);
}
