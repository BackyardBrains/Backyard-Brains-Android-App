package com.backyardbrains.drawing;

import android.content.Context;
import android.opengl.GLSurfaceView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.backyardbrains.analysis.AnalysisManager;
import com.backyardbrains.dsp.ProcessingService;
import com.backyardbrains.ui.BaseFragment;
import java.lang.ref.WeakReference;

import static com.backyardbrains.utils.LogUtils.LOGD;
import static com.backyardbrains.utils.LogUtils.makeLogTag;

/**
 * @author Tihomir Leka <tihomir at backyardbrains.com>
 */
abstract class BaseRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = makeLogTag(BaseRenderer.class);

    private WeakReference<BaseFragment> fragmentRef;

    BaseRenderer(@NonNull BaseFragment fragment) {
        fragmentRef = new WeakReference<>(fragment);
    }

    @Nullable ProcessingService getProcessingService() {
        final BaseFragment fragment = getFragment("getProcessingService()");
        if (fragment == null) return null;

        return fragment.getProvider() != null ? fragment.getProvider().processingService() : null;
    }

    @Nullable protected AnalysisManager getAnalysisManager() {
        final BaseFragment fragment = getFragment("getAnalysisManager()");
        if (fragment == null) return null;

        return fragment.getProvider() != null ? fragment.getProvider().analysisManager() : null;
    }

    @Nullable protected Context getContext() {
        final BaseFragment fragment = getFragment("getAnalysisManager()");
        if (fragment == null) return null;

        return fragment.getContext();
    }

    // Returns the fragment reference and if reference is lost, logs the calling method.
    @Nullable private BaseFragment getFragment(@NonNull String methodName) {
        final BaseFragment fragment = fragmentRef.get();
        if (fragment == null) LOGD(TAG, "Renderer lost Fragment reference, ignoring (" + methodName + ")");

        return fragment;
    }
}
