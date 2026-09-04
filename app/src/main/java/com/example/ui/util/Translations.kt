package com.example.ui.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Translations {

    val classOptions = listOf(
        ClassOption("Play", "Play"),
        ClassOption("Nursery", "Nursery"),
        ClassOption("One", "1"),
        ClassOption("Two", "2"),
        ClassOption("Three", "3"),
        ClassOption("Four", "4"),
        ClassOption("Five", "5"),
        ClassOption("Six", "6"),
        ClassOption("Seven", "7"),
        ClassOption("Eight", "8"),
        ClassOption("Nine", "9"),
        ClassOption("Ten", "10")
    )

    val sectionClasses = setOf("Play", "Nursery", "One", "Two", "Three", "Four", "Five")
    val groupClasses = setOf("Nine", "Ten")

    val paymentCategories = listOf(
        "Tuition Fee",
        "Admission Fee",
        "Monthly Fee",
        "Exam Fee",
        "Registration Fee",
        "Session Fee",
        "Development Fee",
        "Library Fee",
        "Computer Fee",
        "Sports Fee",
        "ID Card Fee",
        "Certificate Fee",
        "Late Fee",
        "Other"
    )

    val expenseCategories = listOf(
        "Salary",
        "Electricity Bill",
        "Water Bill",
        "Internet Bill",
        "Stationery",
        "Maintenance",
        "Cleaning",
        "Equipment",
        "Events",
        "Rent",
        "Other"
    )

    private val categoryMapBn = mapOf(
        "Tuition Fee" to "টিউশন ফি",
        "Admission Fee" to "ভর্তি ফি",
        "Monthly Fee" to "মাসিক ফি",
        "Exam Fee" to "পরীক্ষা ফি",
        "Registration Fee" to "রেজিস্ট্রেশন ফি",
        "Session Fee" to "সেশন ফি",
        "Development Fee" to "উন্নয়ন ফি",
        "Library Fee" to "লাইব্রেরি ফি",
        "Computer Fee" to "কম্পিউটার ফি",
        "Sports Fee" to "ক্রীড়া ফি",
        "ID Card Fee" to "আইডি কার্ড ফি",
        "Certificate Fee" to "সনদ ফি",
        "Late Fee" to "বিলম্ব ফি",
        "Salary" to "বেতন",
        "Electricity Bill" to "বিদ্যুৎ বিল",
        "Water Bill" to "পানি বিল",
        "Internet Bill" to "ইন্টারনেট বিল",
        "Stationery" to "স্টেশনারি",
        "Maintenance" to "রক্ষণাবেক্ষণ",
        "Cleaning" to "পরিষ্কার-পরিচ্ছন্নতা",
        "Equipment" to "সরঞ্জাম",
        "Events" to "অনুষ্ঠান",
        "Rent" to "ভাড়া",
        "Other" to "অন্যান্য"
    )

    fun getCategoryLabel(category: String, lang: String): String {
        return if (lang == "bn") {
            categoryMapBn[category] ?: category
        } else {
            category
        }
    }

    fun getGroupLabel(group: String?, lang: String): String {
        if (group == null) return ""
        return if (lang == "bn") {
            when (group) {
                "Science" -> "সায়েন্স"
                "Commerce" -> "কমার্স"
                "Arts" -> "আর্টস"
                else -> group
            }
        } else {
            group
        }
    }

    fun formatMoney(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "৳ " + formatter.format(amount)
    }

    fun formatDate(timestamp: Long, lang: String): String {
        val pattern = "dd MMM yyyy, hh:mm a"
        val locale = if (lang == "bn") Locale("bn", "BD") else Locale.US
        return SimpleDateFormat(pattern, locale).format(Date(timestamp))
    }

    fun formatMonth(yearMonth: String, lang: String): String {
        return try {
            val date = SimpleDateFormat("yyyy-MM", Locale.US).parse(yearMonth) ?: Date()
            val locale = if (lang == "bn") Locale("bn", "BD") else Locale.US
            SimpleDateFormat("MMMM yyyy", locale).format(date)
        } catch (e: Exception) {
            yearMonth
        }
    }

    fun getString(key: String, lang: String): String {
        val bnMap = mapOf(
            "app_title" to "স্কুল ফাইন্যান্স",
            "login_sub" to "স্কুলের আর্থিক ব্যবস্থাপনা সিস্টেম",
            "email_phone" to "ইমেইল অথবা ফোন",
            "password" to "পাসওয়ার্ড",
            "login" to "লগইন",
            "quick_demo" to "ডেমো মোডে প্রবেশ করুন",
            "dashboard" to "ড্যাশবোর্ড",
            "payment" to "পেমেন্ট",
            "expense" to "এক্সপেন্স",
            "history" to "হিস্টোরি",
            "settings" to "সেটিংস",
            "logout" to "লগআউট",
            "welcome" to "স্বাগতম",
            "dashboard_desc" to "স্কুলের আর্থিক কার্যক্রম সহজে পরিচালনা করুন।",
            "today_payments" to "আজকের পেমেন্ট",
            "today_expenses" to "আজকের খরচ",
            "month_income" to "এই মাসের আয়",
            "month_balance" to "এই মাসের ব্যালেন্স",
            "quick_payment" to "নতুন পেমেন্ট যোগ করুন",
            "quick_expense" to "স্কুলের খরচ রেকর্ড করুন",
            "recent_transactions" to "সাম্প্রতিক ট্রানজেকশন",
            "no_transactions" to "কোনো ট্রানজেকশন পাওয়া যায়নি।",
            "student_name" to "শিক্ষার্থীর নাম",
            "roll" to "রোল",
            "class" to "ক্লাস",
            "section" to "শাখা",
            "group" to "গ্রুপ",
            "category" to "ক্যাটাগরি",
            "amount" to "টাকার পরিমাণ (৳)",
            "date_time" to "তারিখ ও সময়",
            "description" to "বিবরণ (যদি থাকে)",
            "select_class" to "ক্লাস নির্বাচন করুন",
            "select_section" to "শাখা নির্বাচন করুন",
            "select_group" to "গ্রুপ নির্বাচন করুন",
            "select_category" to "ক্যাটাগরি নির্বাচন করুন",
            "save_payment" to "পেমেন্ট সেভ করুন",
            "save_expense" to "খরচ সেভ করুন",
            "cancel" to "বাতিল",
            "payment_title" to "নতুন পেমেন্ট",
            "payment_desc" to "শিক্ষার্থীর ফি বা পেমেন্ট রেকর্ড করুন।",
            "expense_title" to "নতুন খরচ",
            "expense_desc" to "স্কুলের দৈনন্দিন খরচ রেকর্ড করুন।",
            "history_title" to "মাসিক হিস্টোরি",
            "history_desc" to "প্রতি মাসের মোট আয়, ব্যয় ও ট্রানজেকশন বিবরণী।",
            "total_payments" to "মোট পেমেন্ট",
            "total_expenses" to "মোট খরচ",
            "total_transactions" to "মোট ট্রানজেকশন",
            "net_balance" to "নিট ব্যালেন্স",
            "settings_title" to "সেটিংস",
            "settings_desc" to "স্কুলের তথ্য ও অ্যাপের পছন্দসমূহ নিয়ন্ত্রণ করুন।",
            "school_info" to "স্কুলের তথ্য",
            "school_name" to "স্কুলের নাম",
            "school_logo" to "স্কুলের লোগো",
            "save_settings" to "সেটিংস সেভ করুন",
            "preferences" to "পছন্দসমূহ",
            "dark_mode" to "ডার্ক মোড",
            "dark_mode_desc" to "রাতের জন্য ডার্ক থিম সক্রিয় করুন।",
            "language" to "ভাষা",
            "language_desc" to "বাংলা ও ইংরেজির মধ্যে পরিবর্তন করুন।",
            "bangla" to "বাংলা",
            "english" to "English",
            "transaction_details" to "ট্রানজেকশনের বিস্তারিত",
            "receipt_number" to "রিসিট নম্বর",
            "transaction_type" to "ট্রানজেকশনের ধরন",
            "transaction_date" to "তারিখ ও সময়",
            "saved_payment" to "পেমেন্ট সফলভাবে সেভ হয়েছে",
            "saved_expense" to "খরচ সফলভাবে সেভ হয়েছে",
            "done" to "সম্পন্ন",
            "search_hint" to "নাম, রোল, ক্যাটাগরি বা রিসিট নং দিয়ে খুঁজুন...",
            "all_months" to "মাস নির্বাচন করুন",
            "reports" to "রিপোর্ট",
            "reports_title" to "আর্থিক রিপোর্ট ও চার্ট",
            "reports_desc" to "চলতি মাসের মোট আয় বনাম ব্যয়ের তুলনামূলক গ্রাফ ও পরিসংখ্যান।",
            "income_vs_expense" to "আয় বনাম ব্যয় চার্ট",
            "weekly_trend" to "সাপ্তাহিক ট্রেন্ড বিশ্লেষণ",
            "category_breakdown" to "খাতভিত্তিক আয় ও ব্যয়",
            "net_surplus" to "নিট উদ্বৃত্ত",
            "net_deficit" to "নিট ঘাটতি",
            "signup_tab" to "নতুন একাউন্ট",
            "login_tab" to "লগইন",
            "full_name" to "পুরো নাম",
            "phone_optional" to "মোবাইল নম্বর (ঐচ্ছিক)",
            "signup_btn" to "একাউন্ট তৈরি করুন",
            "resend_email_btn" to "পুনরায় ভেরিফিকেশন ইমেইল পাঠান",
            "server_settings" to "সার্ভার কনফিগারেশন",
            "supabase_cloud_connected" to "Supabase ক্লাউড কানেক্টেড",
            "supabase_cloud_error" to "সার্ভার সংযোগে ত্রুটি",
            "create_account_desc" to "নতুন স্কুল একাউন্ট তৈরি করুন"
        )

        val enMap = mapOf(
            "app_title" to "School Finance",
            "login_sub" to "School financial management system",
            "email_phone" to "Email or Phone",
            "password" to "Password",
            "login" to "Login",
            "quick_demo" to "Enter Demo / Local Mode",
            "dashboard" to "Dashboard",
            "payment" to "Payment",
            "expense" to "Expense",
            "history" to "History",
            "settings" to "Settings",
            "logout" to "Logout",
            "welcome" to "Welcome",
            "dashboard_desc" to "Manage your school's finances with ease.",
            "today_payments" to "Today's Payments",
            "today_expenses" to "Today's Expenses",
            "month_income" to "This Month Income",
            "month_balance" to "This Month Balance",
            "quick_payment" to "Add a new payment",
            "quick_expense" to "Record school expense",
            "recent_transactions" to "Recent Transactions",
            "no_transactions" to "No transactions found.",
            "student_name" to "Student Name",
            "roll" to "Roll",
            "class" to "Class",
            "section" to "Section",
            "group" to "Group",
            "category" to "Category",
            "amount" to "Amount (৳)",
            "date_time" to "Date & Time",
            "description" to "Description (Optional)",
            "select_class" to "Select Class",
            "select_section" to "Select Section",
            "select_group" to "Select Group",
            "select_category" to "Select Category",
            "save_payment" to "Save Payment",
            "save_expense" to "Save Expense",
            "cancel" to "Cancel",
            "payment_title" to "Add Payment",
            "payment_desc" to "Record a student fee or payment.",
            "expense_title" to "Add Expense",
            "expense_desc" to "Record a school expense.",
            "history_title" to "Monthly History",
            "history_desc" to "View comprehensive statements by month.",
            "total_payments" to "Total Payments",
            "total_expenses" to "Total Expenses",
            "total_transactions" to "Total Transactions",
            "net_balance" to "Net Balance",
            "settings_title" to "Settings",
            "settings_desc" to "Manage school details and app preferences.",
            "school_info" to "School Information",
            "school_name" to "School Name",
            "school_logo" to "School Logo",
            "save_settings" to "Save Settings",
            "preferences" to "Preferences",
            "dark_mode" to "Dark Mode",
            "dark_mode_desc" to "Use a darker visual interface.",
            "language" to "Language",
            "language_desc" to "Switch between Bengali and English.",
            "bangla" to "বাংলা",
            "english" to "English",
            "transaction_details" to "Transaction Details",
            "receipt_number" to "Receipt Number",
            "transaction_type" to "Transaction Type",
            "transaction_date" to "Date & Time",
            "saved_payment" to "Payment Saved Successfully",
            "saved_expense" to "Expense Saved Successfully",
            "done" to "Done",
            "search_hint" to "Search by name, roll, category, or receipt...",
            "all_months" to "Select Month",
            "reports" to "Reports",
            "reports_title" to "Financial Reports & Charts",
            "reports_desc" to "Income vs Expenses overview & graphical breakdown for current month.",
            "income_vs_expense" to "Income vs Expense Chart",
            "weekly_trend" to "Weekly Trend Breakdown",
            "category_breakdown" to "Category-wise Breakdown",
            "net_surplus" to "Net Surplus",
            "net_deficit" to "Net Deficit",
            "signup_tab" to "Sign Up",
            "login_tab" to "Login",
            "full_name" to "Full Name",
            "phone_optional" to "Phone Number (Optional)",
            "signup_btn" to "Create Account",
            "resend_email_btn" to "Resend Verification Email",
            "server_settings" to "Server Configuration",
            "supabase_cloud_connected" to "Supabase Cloud Connected",
            "supabase_cloud_error" to "Server Connection Error",
            "create_account_desc" to "Create a new school account"
        )

        return if (lang == "bn") {
            bnMap[key] ?: enMap[key] ?: key
        } else {
            enMap[key] ?: bnMap[key] ?: key
        }
    }
}

data class ClassOption(val value: String, val label: String)
