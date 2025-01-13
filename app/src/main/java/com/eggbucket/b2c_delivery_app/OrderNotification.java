package com.eggbucket.b2c_delivery_app;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrderNotification extends Fragment {

    private RelativeLayout slidingPill;
    private RelativeLayout pickUpButton;

    private TextView orderId,pickupLocation,dropLocation,orderValue,quantity6,quantity12,quantity30;
    private ScrollView scrollview;
    private String phone="0987654321";

    private static final float END_POSITION_THRESHOLD = 0.8f; // 80% of the width as the threshold for the "end position"

    public OrderNotification() {
    }

    @SuppressLint("ClickableViewAccessibility")

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_notification, container, false);

        // Initialize the scrollview
        scrollview = view.findViewById(R.id.new_order_scroll); // Ensure the ID matches the one in your XML layout

        // Retrieve arguments
        Bundle args = getArguments();
        String orderIdText = args != null ? args.getString("ORDER_ID") : "N/A";
        String pickupText = args != null ? args.getString("PICKUP") : "N/A";
        String deliveryText = args != null ? args.getString("DELIVERY") : "N/A";
        String orderValueText = args != null ? args.getString("ORDER_VALUE") : "N/A";
        String quantity6Text = args != null ? args.getString("E6") : "N/A";
        String quantity12Text = args != null ? args.getString("E12") : "N/A";
        String quantity30Text = args != null ? args.getString("E30") : "N/A";

        orderId = view.findViewById(R.id.order_id);
        pickupLocation = view.findViewById(R.id.pickup_location);
        dropLocation = view.findViewById(R.id.drop_location);
        orderValue = view.findViewById(R.id.order_value);
        quantity6 = view.findViewById(R.id.Egg6Quantity);
        quantity12 = view.findViewById(R.id.Egg12Quantity);
        quantity30 = view.findViewById(R.id.Egg30Quantity);

        orderId.setText("Order id: " + orderIdText);
        pickupLocation.setText(pickupText);
        dropLocation.setText(deliveryText);
        orderValue.setText("Order value: " + orderValueText);
        quantity6.setText(quantity6Text);
        quantity12.setText(quantity12Text);
        quantity30.setText(quantity30Text);

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
            float initialY = 0;
            float deltaX = 0;
            float deltaY = 0;
            final float verticalThreshold = 10; // Define a threshold for vertical movement

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Record the initial touch position
                        initialX = event.getRawX();
                        initialY = event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // Ensure scrollview is not null before setting the listener
                        if (scrollview != null) {
                            scrollview.setOnTouchListener((va, events) -> true);
                        }

                        // Calculate the movement distance
                        deltaX = event.getRawX() - initialX;
                        deltaY = event.getRawY() - initialY;

                        // If vertical movement exceeds the threshold, reset the pill
                        if (Math.abs(deltaY) > verticalThreshold) {
                            resetPillPosition();
                            if (scrollview != null) {
                                scrollview.setOnTouchListener(null);
                            }
                            return true;
                        }

                        float newX = slidingPill.getTranslationX() + deltaX;

                        // Ensure the pill stays within bounds
                        if (newX >= 0 && newX <= pickUpButton.getWidth() - slidingPill.getWidth()) {
                            slidingPill.setTranslationX(newX);
                            initialX = event.getRawX();
                            initialY = event.getRawY(); // Update initialY to allow smooth horizontal movement
                        }
                        if (scrollview != null) {
                            scrollview.setOnTouchListener(null);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        if (scrollview != null) {
                            scrollview.setOnTouchListener((va, events) -> true);
                        }

                        // Calculate the end position threshold
                        float endPosition = pickUpButton.getWidth() - slidingPill.getWidth();
                        float thresholdPosition = endPosition * END_POSITION_THRESHOLD;

                        if (slidingPill.getTranslationX() >= thresholdPosition) {
                            // Trigger a popup or any other action when the pill reaches the threshold
                            Toast.makeText(getContext(), "Pill Reached the End! Action Triggered.", Toast.LENGTH_SHORT).show();
                            acceptOrder(phone, orderIdText);
                            // Navigate to the next screen when the pill reaches the threshold
                            NavHostFragment.findNavController(OrderNotification.this)
                                    .navigate(R.id.action_newOrder_to_pickupMap);
                        } else {
                            // Animate the pill to 85% position if dragged close enough
                            if (slidingPill.getTranslationX() >= thresholdPosition * 0.85f) {
                                ObjectAnimator animator = ObjectAnimator.ofFloat(slidingPill, "translationX", slidingPill.getTranslationX(), thresholdPosition);
                                animator.setDuration(300); // Adjust duration for smooth animation
                                animator.start();
                            } else {
                                // Reset pill position if not dragged close enough
                                resetPillPosition();
                            }
                        }
                        if (scrollview != null) {
                            scrollview.setOnTouchListener(null);
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
    private void acceptOrder(String phone, String orderId) {
        // Build the URL dynamically
        String url = "https://b2c-backend-1.onrender.com/api/v1/deliveryPartner/acceptOrder/"+phone+"/"+orderId;

        // Create OkHttpClient
        OkHttpClient client = new OkHttpClient();

        // Create the request body (if required, replace with actual data)
        RequestBody requestBody = RequestBody.create(
                MediaType.get("application/json"), "{}" // Empty JSON body
        );

        // Build the PATCH request
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        // Make the API call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle failure
                System.out.println("API call failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Handle success
                if (response.isSuccessful()) {
                    System.out.println("API call successful: " + response.body().string());
                } else {
                    System.out.println("API call failed with code: " + response.code());
                }
                response.close(); // Close the response body to avoid resource leaks
            }
        });
    }
}
