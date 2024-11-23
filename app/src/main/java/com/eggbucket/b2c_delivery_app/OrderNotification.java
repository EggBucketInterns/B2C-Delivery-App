package com.eggbucket.b2c_delivery_app;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class OrderNotification extends Fragment {

    private RelativeLayout slidingPill;
    private RelativeLayout pickUpButton;

    private static final float END_POSITION_THRESHOLD = 0.8f; // 80% of the width as the threshold for the "end position"

    public OrderNotification() {
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_notification, container, false);

        pickUpButton = view.findViewById(R.id.pickUpButton);
        slidingPill = view.findViewById(R.id.sliding_pill);

        // Handle the button click to navigate
        pickUpButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(OrderNotification.this)
                    .navigate(R.id.action_newOrder_to_pickupMap);
        });

        // Implement the drag behavior for the sliding pill
        slidingPill.setOnTouchListener(new View.OnTouchListener() {
            float initialX = 0;
            float deltaX = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Record the initial touch position
                        initialX = event.getRawX();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // Calculate the movement distance
                        deltaX = event.getRawX() - initialX;
                        float newX = slidingPill.getTranslationX() + deltaX; // Use translationX instead of setX

                        // Ensure the pill stays within bounds
                        if (newX >= 0 && newX <= pickUpButton.getWidth() - slidingPill.getWidth()) {
                            slidingPill.setTranslationX(newX); // Smooth translation update
                            initialX = event.getRawX(); // Update initial position for the next move
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        // Calculate the end position threshold
                        float endPosition = pickUpButton.getWidth() - slidingPill.getWidth();
                        if (slidingPill.getTranslationX() >= endPosition * END_POSITION_THRESHOLD) {
                            // Trigger a popup (Toast) or any other action when the pill reaches the end
                            Toast.makeText(getContext(), "Pill Reached the End! Action Triggered.", Toast.LENGTH_SHORT).show();

                            // Navigate to the next screen when the pill reaches the end
                            NavHostFragment.findNavController(OrderNotification.this)
                                    .navigate(R.id.action_newOrder_to_pickupMap);
                        } else {
                            // Reset pill position immediately if it's not at the end
                            resetPillPosition();
                        }
                        break;
                }
                return true; // Return true to indicate that the event has been handled
            }
        });

        return view;
    }

    private void resetPillPosition() {
        // Animate the pill back to its original position if not dragged to the end
        ObjectAnimator animator = ObjectAnimator.ofFloat(slidingPill, "translationX", slidingPill.getTranslationX(), 0f);
        animator.setDuration(300); // Adjust duration for smooth animation
        animator.start();
    }
}
