# Simplify Role System and Navigation

The goal is to streamline the app to only two functional roles: Admin and Kasir. Admin will focus on managing menu items and prices, while Kasir will handle order inputs.

## Proposed Changes

### [Android App]

#### [RegisterActivity.java](file:///C:/Users/SMART PC/AndroidStudioProjects/android_app/app/src/main/java/com/example/pesananmakanan/RegisterActivity.java)
- Remove "pengguna" from the role selection.
- Keep the secret code requirement for both remaining roles.

#### [LoginActivity.java](file:///C:/Users/SMART PC/AndroidStudioProjects/android_app/app/src/main/java/com/example/pesananmakanan/LoginActivity.java)
- Update redirection logic:
    - Admin -> `AdminMainActivity`
    - Kasir -> `KasirActivity`
- Remove any redirection to `PenggunaActivity` or `DashboardActivity` by default.

#### [AdminMainActivity.java](file:///C:/Users/SMART PC/AndroidStudioProjects/android_app/app/src/main/java/com/example/pesananmakanan/AdminMainActivity.java)
- Ensure the UI focus is on managing menu items.
- Optionally remove or hide the "Dashboard" button if it's confusing, or keep it as a secondary tool for Admin.

### [Backend API]

#### [register_firebase.php](file:///C:/Users/SMART PC/AndroidStudioProjects/android_app/backend_api/register_firebase.php)
- Reject any registration that isn't Admin or Kasir.

## Verification Plan

### Manual Verification
1. Open the Register screen and verify only "Admin" and "Kasir" options are available.
2. Register an Admin using `ADMIN123` and a Kasir using `KASIR123`.
3. Login as Admin and verify it opens the product management screen.
4. Login as Kasir and verify it opens the order input screen.
