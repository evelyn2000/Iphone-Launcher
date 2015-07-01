
package com.android.iphonelauncher;

import com.android.iphonelauncher.R;
import com.android.iphonelauncher.CellLayout.LayoutParams;
import com.android.ui.IphoneShortcutCallback;
import com.android.util.AnimManager;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class IphoneInstallAppShortCut extends RelativeLayout implements DropTarget,
        View.OnClickListener, IphoneShortcutCallback {

    private static final String TAG = "IphoneInstallAppShortCut";

    private BubbleTextView mContent;

    private Launcher mLauncher;

    private View mUinstallBtn;

    private boolean mIsFolderIconMode = false;

    public IphoneInstallAppShortCut(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDrawingCacheEnabled(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContent = (BubbleTextView) findViewById(R.id.content);
        // mContent.setOnClickListener(this);
        // mContent.setOnLongClickListener(this);
        mUinstallBtn = findViewById(R.id.uninstall_btn);
        mUinstallBtn.setVisibility(View.INVISIBLE);
        mUinstallBtn.setOnClickListener(this);
    }

    @Override
    protected void drawableStateChanged() {
        Drawable d = mContent.getCompoundDrawables()[1];
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }
        super.drawableStateChanged();
    }

    public void setIphoneIcon(Bitmap icon) {
        mContent.setIphoneIcon(icon);
    }

    public void setText(CharSequence c) {
        mContent.setText(c);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.uninstall_btn:
                mLauncher.doUninstall((ShortcutInfo) getTag());
                break;
            case R.id.content:
                break;
            default:
                break;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // if (getAnimation() != null) {
                // ItemInfo info = (ItemInfo) getTag();
                // if (info.container > 0) {
                // // this view is in a folder.
                // Folder folder = mLauncher.getOpenIphoneFolder();
                // if (folder != null) {
                // folder.startDrag(this);
                // return true;
                // }
                // } else {
                // CellLayout.CellInfo cellInfo = (CellLayout.CellInfo) ((View)
                // getParent())
                // .getTag();
                // if (cellInfo == null) {
                // Log.d(TAG, "cellinfo is null");
                // return true;
                // }
                // final View parent = (View) getParent().getParent();
                // if (parent instanceof Workspace) {
                // ((Workspace) parent).startDrag(cellInfo);
                // return true;
                // }
                // }
                // }
                mContent.setIphonePressed(true);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mContent.setIphonePressed(false);
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    public void showUninstallBtn(boolean show) {
        mUinstallBtn.setVisibility(show ? VISIBLE : INVISIBLE);
    }

    public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        // get workspace
        ViewParent parent = getParent();
        if (parent == null) {
            return;
        }

        Workspace mTargetWorkspace = (Workspace) parent.getParent();

        final int workspaceType = mTargetWorkspace.getType();
        int container = 0;
        switch (workspaceType) {
            case Workspace.WORKSPACE_NAVIGATEBAR:
                container = LauncherSettings.Favorites.CONTAINER_NAVIGATEBAR;
                break;
            case Workspace.WORKSPACE_WORKSPACE:
                container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
                break;
            default:
                return;
        }

        final CellLayout.LayoutParams currentLp = (CellLayout.LayoutParams) getLayoutParams();
        // CellLayout targetCelllayout = (CellLayout) getParent();

        // remove my in parent
        removeSelfInParent(mLauncher.getDragController());
        // clearAnimation();
        // targetCelllayout.removeView(this);

        // make the folder info.

        final UserFolderInfo folderInfo = new UserFolderInfo();
        folderInfo.title = getResources().getText(R.string.folder_name);
        // add folder into db
        LauncherModel.addItemToDatabase(getContext(), folderInfo, container,
                mTargetWorkspace.getCurrentScreen(), currentLp.cellX, currentLp.cellY, true);

        // add into new folder
        // CellLayout.CellInfo cellInfo = mSourceWorkspace.getCurrentDragInfo();

        final ShortcutInfo thisTag = (ShortcutInfo) getTag();
        final ShortcutInfo sourceTag = (ShortcutInfo) dragInfo;
        final int oldSourceScreen = sourceTag.screen;
        folderInfo.add(thisTag);
        folderInfo.add(sourceTag);

        // add folder items into db
        LauncherModel.addOrMoveItemInDatabase(mLauncher, thisTag, folderInfo.id, 0, 0, 0);
        LauncherModel.addOrMoveItemInDatabase(mLauncher, sourceTag, folderInfo.id, 0, 0, 0);

        // Create the view
        final FolderIcon newFolder = FolderIcon.fromXml(R.layout.folder_icon, mLauncher,
                (ViewGroup) mTargetWorkspace.getChildAt(mTargetWorkspace.getCurrentScreen()),
                folderInfo);

        // add into target cell
        mTargetWorkspace.addInCurrentScreen(newFolder, currentLp.cellX, currentLp.cellY, 1, 1,
                false);

        // start anim
        AnimManager.getInstance().startSingle(newFolder);

        // range cell
        Workspace sourceWorkspace = (Workspace) source;
        CellLayout sourceCellLayout = (CellLayout) sourceWorkspace.getChildAt(oldSourceScreen);
        sourceCellLayout.rangeChilds(false);

        // open folder
        mTargetWorkspace.getHandler().postDelayed(new Runnable() {
            public void run() {
                mLauncher.handleIphoneFolderClick(folderInfo, newFolder);
            }
        }, 200);
    }

    public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
    }

    public boolean onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {

        Log.d(TAG, "onDragOver " + x + "," + y + "," + xOffset + "," + yOffset);

        // if (!mContent.mOverLeftRect.contains(x, y)) {
        // return false;
        // }

        CellLayout targetCelllayout = (CellLayout) getParent();
        if (targetCelllayout == null) {
            return false;
        }

        CellLayout.LayoutParams targetLp = (CellLayout.LayoutParams) getLayoutParams();
        final int tempTargetCellX = targetLp.cellX;
        final int tempTargetCellY = targetLp.cellY;

        boolean result = false;

        boolean needToShowAsFolderIcon = false;

        if (mContent.mOverLeftRect.contains(x, y)) {
            result = targetCelllayout.movePos(tempTargetCellX, tempTargetCellY, true);
        } else if (mContent.mOverRightrRect.contains(x, y)) {
            result = targetCelllayout.movePos(tempTargetCellX, tempTargetCellY, false);
        } else if (mContent.mCenterRect.contains(x, y)) {
            needToShowAsFolderIcon = true;
        }
        if (needToShowAsFolderIcon) {
            setFolderIcon(dragInfo);
        } else {
            setOrilIcon(dragInfo);
        }
        return result;

        // if (!targetCelllayout.movePos(tempTargetCellX, tempTargetCellY,
        // false)) {
        // return false;
        // }
        // final ShortcutInfo info = (ShortcutInfo) getTag();
        // final Bitmap mFolderIcon = info.getIcon(mLauncher.getIconCache());
        // mContent.setIphoneIcon(mFolderIcon);
        // if (getAnimation() != null && getAnimation().hasStarted())
        // showUninstallBtn(true);

        // return true;
    }

    public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        setOrilIcon(dragInfo);
    }

    public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {

        ViewParent parent = getParent();
        if (parent == null) {
            return false;
        }

        if (!mContent.mCenterRect.contains(x, y)) {
            return false;
        }

        if (dragInfo instanceof UserFolderInfo) {
            // it is a foler icon return
            return false;
        }
        final ItemInfo myInfo = (ItemInfo) getTag();
        if (myInfo.container >= 0) {
            // i am in a folder return
            return false;
        }
        // if (mContent.mOverRect.contains(x, y)) {
        // return false;
        // }
        final ItemInfo item = (ItemInfo) dragInfo;
        final int itemType = item.itemType;
        return (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION || itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT);
    }

    public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo, Rect recycle) {
        return null;
    }

    public void setReflectionEffect(boolean has) {
        mContent.setReflectionEffect(has);
    }

    public void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }

    public void removeSelfInParent(DragController dragController) {
        clearAnimation();
        AnimManager.getInstance().removeControllers(this);
        dragController.removeDropTarget((DropTarget) this);
        ((CellLayout) getParent()).removeView(this);
    }

    private void setFolderIcon(Object dragInfo) {

        if (mIsFolderIconMode) {
            return;
        }

        if (dragInfo instanceof UserFolderInfo) {
            // it is a foler icon return
            return;
        }
        final ShortcutInfo info = (ShortcutInfo) getTag();
        if (info.container >= 0) {
            // i am in a folder return
            return;
        }
        final Bitmap mFolderIcon = info.getIcon(mLauncher.getIconCache());
        mContent.setCompoundDrawablesWithIntrinsicBounds(null,
                new FastBitmapDrawable(Utilities.makeGridFolderIcons(new Bitmap[] {
                    mFolderIcon
                }, getContext())), null, null);
        showUninstallBtn(false);

        mIsFolderIconMode = true;
    }

    private void setOrilIcon(Object dragInfo) {
        if (!mIsFolderIconMode) {
            return;
        }
        if (dragInfo instanceof UserFolderInfo) {
            // it is a foler icon return
            return;
        }
        final ShortcutInfo info = (ShortcutInfo) getTag();
        if (info.container >= 0) {
            // i am in a folder return
            return;
        }
        final Bitmap mFolderIcon = info.getIcon(mLauncher.getIconCache());
        mContent.setIphoneIcon(mFolderIcon);
        if (AnimManager.getInstance().isAnim())
            showUninstallBtn(true);

        mIsFolderIconMode = false;
    }

}
