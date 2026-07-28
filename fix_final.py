import os

FILES = [
    "/Users/bosethrathnayake/LNBTI SEM4/financial manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/DashboardActivity.java",
    "/Users/bosethrathnayake/LNBTI SEM4/financial manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/WorkerDashboardActivity.java",
    "/Users/bosethrathnayake/LNBTI SEM4/financial manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/MultiAccountDashboardActivity.java"
]

METHOD_TO_ADD = """
    private void showNotificationPanelDialog() {
        android.view.View panelView = android.view.LayoutInflater.from(this).inflate(com.example.smartfinancialmanagement.R.layout.dialog_notifications, null);
        android.widget.LinearLayout container = panelView.findViewById(com.example.smartfinancialmanagement.R.id.layoutNotificationsContainer);
        android.widget.Button btnClose = panelView.findViewById(com.example.smartfinancialmanagement.R.id.btnDismissNotifications);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this, com.example.smartfinancialmanagement.R.style.Theme_SmartFinance_Dialog)
                .setView(panelView).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Show loading state
        container.removeAllViews();
        android.widget.TextView loadingView = new android.widget.TextView(this);
        loadingView.setText("Loading messages...");
        loadingView.setTextColor(android.graphics.Color.parseColor("#94A3B8"));
        loadingView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 13f);
        loadingView.setPadding(0, 24, 0, 24);
        container.addView(loadingView);

        // Query Firestore for admin reply messages targeted at this user
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            loadingView.setText("Not signed in.");
            btnClose.setOnClickListener(view -> dialog.dismiss());
            dialog.show();
            return;
        }

        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("notifications")
            .whereEqualTo("uid", currentUser.getUid())
            .whereEqualTo("isUserTargeted", true)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                container.removeAllViews();
                if (querySnapshot.isEmpty()) {
                    // No messages yet
                    android.widget.TextView emptyView = new android.widget.TextView(this);
                    emptyView.setText("📭  No messages from support yet.");
                    emptyView.setTextColor(android.graphics.Color.parseColor("#94A3B8"));
                    emptyView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 13f);
                    emptyView.setPadding(0, 24, 0, 24);
                    container.addView(emptyView);
                } else {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String title   = doc.getString("title");
                        String body    = doc.getString("body");
                        String type    = doc.getString("type");
                        Boolean isRead = doc.getBoolean("read");

                        // Card wrapper
                        android.widget.LinearLayout card = new android.widget.LinearLayout(this);
                        card.setOrientation(android.widget.LinearLayout.VERTICAL);
                        int dp8  = (int)(8  * getResources().getDisplayMetrics().density);
                        int dp12 = (int)(12 * getResources().getDisplayMetrics().density);
                        int dp16 = (int)(16 * getResources().getDisplayMetrics().density);
                        card.setPadding(dp12, dp12, dp12, dp12);
                        android.widget.LinearLayout.LayoutParams cardParams = new android.widget.LinearLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                        cardParams.setMargins(0, 0, 0, dp8);
                        card.setLayoutParams(cardParams);
                        card.setBackgroundColor(android.graphics.Color.parseColor(
                            (isRead != null && isRead) ? "#1A2535" : "#1E2D42"));

                        // Title
                        android.widget.TextView titleView = new android.widget.TextView(this);
                        titleView.setText(title != null ? title : "Support Message");
                        titleView.setTextColor(android.graphics.Color.parseColor("#2DD4BF"));
                        titleView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 13f);
                        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
                        titleView.setPadding(0, 0, 0, dp8);

                        // Body
                        android.widget.TextView bodyView = new android.widget.TextView(this);
                        bodyView.setText(body != null ? body : "");
                        bodyView.setTextColor(android.graphics.Color.parseColor("#CBD5E1"));
                        bodyView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12.5f);
                        bodyView.setLineSpacing(0, 1.4f);

                        // Unread dot label
                        if (isRead == null || !isRead) {
                            android.widget.TextView unreadTag = new android.widget.TextView(this);
                            unreadTag.setText("● NEW");
                            unreadTag.setTextColor(android.graphics.Color.parseColor("#2DD4BF"));
                            unreadTag.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 10f);
                            unreadTag.setPadding(0, dp8, 0, 0);
                            card.addView(unreadTag);

                            // Mark as read in Firestore
                            doc.getReference().update("read", true);
                        }

                        card.addView(titleView);
                        card.addView(bodyView);
                        container.addView(card);
                    }
                }
            })
            .addOnFailureListener(e -> {
                container.removeAllViews();
                android.widget.TextView errView = new android.widget.TextView(this);
                errView.setText("⚠️ Could not load messages: " + e.getMessage());
                errView.setTextColor(android.graphics.Color.parseColor("#F87171"));
                errView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12f);
                errView.setPadding(0, 16, 0, 16);
                container.addView(errView);
            });

        btnClose.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }
}
"""

for filepath in FILES:
    if not os.path.exists(filepath):
        continue
    with open(filepath, 'r') as f:
        content = f.read()

    content = content.replace("import android.app.AlertDialog;", "")
    content = content.replace("v -> showNotificationPanelDialog()", "notifBtn -> showNotificationPanelDialog()")

    if "showNotificationPanelDialog()" not in content:
        # append right before the last closing brace
        last_brace = content.rfind("}")
        if last_brace != -1:
            content = content[:last_brace] + METHOD_TO_ADD
        
    with open(filepath, 'w') as f:
        f.write(content)
    print(f"Fixed {filepath}")
