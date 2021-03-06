package com.backyardbrains.drawing.gl;

import androidx.annotation.Nullable;
import android.util.SparseArray;
import android.view.MotionEvent;

import static com.backyardbrains.utils.LogUtils.makeLogTag;

/**
 * @author Tihomir Leka <tihomir at backyardbrains.com>
 */
public class GlHandleDragHelper {

    private static final String TAG = makeLogTag(GlHandleDragHelper.class);

    private static final int NONE = -1;
    private static final float OFFSET_PERCENT = 1.2f;

    private final SparseArray<Rect> draggableAreas = new SparseArray<>();

    private int activePointerId = MotionEvent.INVALID_POINTER_ID;
    private float lastTouchY;
    private int height;
    private int selectedDraggableArea;

    /**
     * Interface definition for a callbacks to be invoked when one of the registered drag area start or ends being
     * dragged or while dragging.
     */
    public interface OnDragListener {
        /**
         * Listener that is invoked when drag area registered at specified {@code index} start being dragged.
         *
         * @param index Index at which drag area started being dragged is registered.
         */
        void onDragStart(int index);

        /**
         * Listener that is invoked while drag area registered at specified {@code index} is being dragged.
         *
         * @param index Index at which drag area started being dragged is registered.
         * @param dy Difference between current and previous drag position.
         */
        void onDrag(int index, float dy);

        /**
         * Listener that is invoked when drag area registered at specified {@code index} stops being dragged.
         *
         * @param index Index at which drag area stopped being dragged is registered.
         */
        void onDragStop(int index);
    }

    private OnDragListener listener;

    public GlHandleDragHelper(@Nullable OnDragListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the height of the drawable surface which is necessary for helper to calculate position correctly.
     */
    public void setSurfaceHeight(int height) {
        this.height = height;
    }

    /**
     */
    /**
     * Registers a single draggable area under specified {@code index}.
     * <p>
     * Renderers should register all touchable thumbs through this method passing instance the of the {@link
     * com.backyardbrains.drawing.gl.Rect} as the thumb representation.
     * </p>
     */
    public void registerDraggableArea(int index, float x, float y, float width, float height) {
        Rect r = draggableAreas.get(index);
        if (r != null) {
            r.set(x, y, width * OFFSET_PERCENT, height * OFFSET_PERCENT);
        } else {
            draggableAreas.put(index, new Rect(x, y, width * OFFSET_PERCENT, height * OFFSET_PERCENT));
        }
    }

    /**
     * Resets registered thumbs.
     */
    public void resetDraggableAreas() {
        draggableAreas.clear();
    }

    /**
     * Handles specified {@code event} by checking whether any of the registered graph thumbs has been clicked and
     * returns {@code true} if it was, {@code false} otherwise.
     */
    public boolean onTouch(MotionEvent event) {
        float x, y, dy;
        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final int pointerIndex = event.getActionIndex();
                x = event.getX(pointerIndex);
                y = event.getY(pointerIndex);

                // remember where we started dragging
                lastTouchY = y;
                // save the ID of this pointer
                activePointerId = event.getPointerId(0);

                selectedDraggableArea = getSelectedDraggableArea(x, y);

                // trigger OnDragListener.onDragStart() callback if listener has been set
                if (selectedDraggableArea != NONE && listener != null) listener.onDragStart(selectedDraggableArea);

                return selectedDraggableArea != NONE;
            }
            case MotionEvent.ACTION_MOVE: {
                if (selectedDraggableArea != NONE) {
                    // find the index of the active pointer and fetch its position
                    final int pointerIndex = event.findPointerIndex(activePointerId);

                    y = event.getY(pointerIndex);

                    // calculate the distance moved
                    dy = y - lastTouchY;

                    // remember this touch position for the next move event
                    lastTouchY = y;

                    // trigger OnDragListener.onDrag() callback if listener has been set
                    if (listener != null) listener.onDrag(selectedDraggableArea, dy);

                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_UP: {
                activePointerId = MotionEvent.INVALID_POINTER_ID;

                if (selectedDraggableArea != NONE) {
                    // trigger OnDragListener.onDragStop() callback if listener has been set
                    if (listener != null) listener.onDragStop(selectedDraggableArea);

                    selectedDraggableArea = NONE;

                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = event.getActionIndex();
                final int pointerId = event.getPointerId(pointerIndex);

                if (pointerId == activePointerId) {
                    activePointerId = MotionEvent.INVALID_POINTER_ID;

                    if (selectedDraggableArea != NONE) {
                        // trigger OnDragListener.onDragStop() callback if listener has been set
                        if (listener != null) listener.onDragStop(selectedDraggableArea);

                        selectedDraggableArea = NONE;

                        return true;
                    }
                }
                break;
            }
        }

        return false;
    }

    // If one of the graph thumbs is touched thumb index is returned, -1 is returned otherwise.
    private int getSelectedDraggableArea(float x, float y) {
        for (int i = 0; i < draggableAreas.size(); i++) {
            Rect rect = draggableAreas.valueAt(i);
            if (rect != null && rect.inside(x, height - y)) return i;
        }

        return NONE;
    }
}
