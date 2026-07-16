package com.example.smartfinancialmanagement;

import java.util.ArrayList;

public class DataBridge {
    // 💡 වෙනත් ඕනෑම Activity එකක සිට ආරක්ෂිතව දත්ත රඳවා තැබීමට භාවිතා කරන පොදු Static ලැයිස්තුව
    public static ArrayList<BillReportItem> stagedReportItems = new ArrayList<>();
}