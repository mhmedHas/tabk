# خطوة أخيرة لازم تعملها بنفسك: ملف google-services.json

عشان أمان حسابك، محدش غيرك يقدر يجيب المفاتيح دي من Firebase Console.
خطوات بسيطة (5 دقايق):

## 1) افتح نفس مشروع Firebase المستخدم في تطبيق الفلاتر
- روح على https://console.firebase.google.com
- افتح المشروع اللي اسمه **kaki-f9832** (ده نفس المشروع اللي شغال عليه تطبيق TRAC-GOLD
  الفلاتر، عشان تفضل كل القطع وبيانات المستخدمين مشتركة بين التطبيقين).

## 2) ضيف تطبيق أندرويد جديد للمشروع
- من صفحة Project Overview، دوس على أيقونة أندرويد "Add app".
- في خانة **Android package name** اكتب بالظبط:
  ```
  com.example.module_android_demo
  ```
  (ده الـ applicationId بتاع الديمو - موجود في app/build.gradle).
- اسم التطبيق (اختياري): TRAC-GOLD Demo
- SHA-1 مش لازم دلوقتي (بس لو هتستخدم Google Sign-In لاحقاً هتحتاجه).

## 3) نزّل ملف google-services.json
- بعد ما تضيف التطبيق، Firebase هيدّيك ملف اسمه `google-services.json`.
- حط الملف ده جوه مجلد:
  ```
  app/google-services.json
  ```
  (يعني جنب app/build.gradle بالظبط)

## 4) تأكد إن الـ Authentication مفعّل
- من قائمة Firebase على اليسار: Authentication > Sign-in method
- تأكد إن **Email/Password** مفعّلة (مفروض تكون مفعّلة بالفعل عشان تطبيق الفلاتر
  شغال بيها).

## 5) ابني المشروع
افتح المشروع في Android Studio واعمل Sync + Run، أو من التيرمنال:
```
./gradlew assembleDebug
```

---

### ملحوظات مهمة
- المستخدمين (accounts) والقطع (items) بتاعت الفلاتر هي **نفس** البيانات اللي
  هتظهر هنا، لأننا بنستخدم نفس مسارات Firestore:
  `users/{uid}/items` و `users/{uid}/balances`، ونفس مسار الصور في Storage:
  `images/users/{uid}/{EPC}`.
- لو ظهرت رسالة "PERMISSION_DENIED" وانت بتجرب تجيب القطع، يبقى قواعد الأمان
  (Firestore Rules) محتاجة تسمح بالقراءة للمستخدم المسجل دخول، بنفس الطريقة
  المفعّلة بالفعل لتطبيق الفلاتر.
