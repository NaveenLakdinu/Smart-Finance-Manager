import os
import re

FILES = [
    "/Users/bosethrathnayake/LNBTI SEM4/financial manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/DashboardActivity.java",
    "/Users/bosethrathnayake/LNBTI SEM4/financial manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/WorkerDashboardActivity.java",
    "/Users/bosethrathnayake/LNBTI SEM4/financial manager/Smart-Finance-Manager/app/src/main/java/com/example/smartfinancialmanagement/MultiAccountDashboardActivity.java"
]

IMPORTS_TO_ADD = """import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import android.widget.ScrollView;
import android.widget.ProgressBar;
import android.app.AlertDialog;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
"""

METHOD_TO_ADD = """
    private void showNotificationPanelDialog() {
        View panelView = LayoutInflater.from(this).inflate(R.layout.dialog_notifications, null);
        LinearLayout container = panelView.findViewById(R.id.layoutNotificationsContainer);
        Button btnClose = panelView.findViewById(R.id.btnDismissNotifications);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_SmartFinance_Dialog)
                .setView(panelView).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Show loading state
        container.removeAllViews();
        TextView loadingView = new TextView(this);
        loadingView.setText("Loading messages...");
        loadingView.setTextColor(Color.parseColor("#94A3B8"));
        loadingView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        loadingView.setPadding(0, 24, 0, 24);
        container.addView(loadingView);

        // Query Firestore for admin reply messages targeted at this user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            loadingView.setText("Not signed in.");
            btnClose.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
            return;
        }

        FirebaseFirestore.getInstance().collection("notifications")
            .whereEqualTo("uid", currentUser.getUid())
            .whereEqualTo("isUserTargeted", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                container.removeAllViews();
                if (querySnapshot.isEmpty()) {
                    // No messages yet
                    TextView emptyView = new TextView(this);
                    emptyView.setText("📭  No messages from support yet.");
                    emptyView.setTextColor(Color.parseColor("#94A3B8"));
                    emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
                    emptyView.setPadding(0, 24, 0, 24);
                    container.addView(emptyView);
                } else {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String title   = doc.getString("title");
                        String body    = doc.getString("body");
                        String type    = doc.getString("type");
                        Boolean isRead = doc.getBoolean("read");

                        // Card wrapper
                        LinearLayout card = new LinearLayout(this);
                        card.setOrientation(LinearLayout.VERTICAL);
                        int dp8  = (int)(8  * getResources().getDisplayMetrics().density);
                        int dp12 = (int)(12 * getResources().getDisplayMetrics().density);
                        int dp16 = (int)(16 * getResources().getDisplayMetrics().density);
                        card.setPadding(dp12, dp12, dp12, dp12);
                        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        cardParams.setMargins(0, 0, 0, dp8);
                        card.setLayoutParams(cardParams);
                        card.setBackgroundColor(Color.parseColor(
                            (isRead != null && isRead) ? "#1A2535" : "#1E2D42"));

                        // Title
                        TextView titleView = new TextView(this);
                        titleView.setText(title != null ? title : "Support Message");
                        titleView.setTextColor(Color.parseColor("#2DD4BF"));
                        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
                        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
                        titleView.setPadding(0, 0, 0, dp8);

                        // Body
                        TextView bodyView = new TextView(this);
                        bodyView.setText(body != null ? body : "");
                        bodyView.setTextColor(Color.parseColor("#CBD5E1"));
                        bodyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
                        bodyView.setLineSpacing(0, 1.4f);

                        // Unread dot label
                        if (isRead == null || !isRead) {
                            TextView unreadTag = new TextView(this);
                            unreadTag.setText("● NEW");
                            unreadTag.setTextColor(Color.parseColor("#2DD4BF"));
                            unreadTag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
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
                TextView errView = new TextView(this);
                errView.setText("⚠️ Could not load messages: " + e.getMessage());
                errView.setTextColor(Color.parseColor("#F87171"));
                errView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
                errView.setPadding(0, 16, 0, 16);
                container.addView(errView);
            });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
"""

for filepath in FILES:
    if not os.path.exists(filepath):
        continue
    
    with open(filepath, 'r') as f:
        content = f.read()

    # Add imports
    if "import com.google.firebase.firestore.Query;" not in content:
        content = content.replace("public class", IMPORTS_TO_ADD + "\npublic class", 1)

    # Wire up button
    if "btnNotifications = findViewById" not in content:
        # Some classes have initViews, some have initializeViews, etc.
        # Just find the end of onCreate and put it there.
        on_create_match = re.search(r'(protected void onCreate\(Bundle savedInstanceState\) \{.*?)(?=\n\s+(?:protected|public|private|@))', content, re.DOTALL)
        if on_create_match:
            on_create_body = on_create_match.group(1)
            new_on_create = on_create_body + "\n        View btnNotifications = findViewById(R.id.btnNotifications);\n        if (btnNotifications != null) {\n            btnNotifications.setOnClickListener(v -> showNotificationPanelDialog());\n        }\n"
            content = content.replace(on_create_body, new_on_create)

    # Add method
    if "showNotificationPanelDialog()" not in content:
        last_brace = content.rfind("}")
        content = content[:last_brace] + METHOD_TO_ADD + content[last_brace:]

    with open(filepath, 'w') as f:
        f.write(content)
    print(f"Updated {filepath}")
