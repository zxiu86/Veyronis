# دليل إعداد GitHub Actions وCI/CD لتطبيق Veyronis

تم تجهيز وإعداد مسارات عمل أتمتة GitHub Actions (Workflows) لتطبيق Veyronis بأعلى معايير الأمان والاستقرار وفقًا لإصدارات Gradle 9.3.1 وAndroid Gradle Plugin 9.1.1 وJava 21.

---

## 1. مسارات العمل المنفذة (Workflows)

### أ. مسار التكامل المستمر: `.github/workflows/ci.yml` (CI Workflow)
- **المحفزات**:
  - يتم تشغيله تلقائيًا عند أي `push` إلى الفروع: `main`, `master`, `develop`.
  - يتم تشغيله عند فتح أي `pull_request` موجه إلى `main` أو `master`.
  - إمكانية التشغيل اليدوي (`workflow_dispatch`).
- **المميزات**:
  - تفعيل تسريع التخزين المؤقت **GitHub Actions Cache** عبر `gradle/actions/setup-gradle@v4` و`actions/cache@v4` لتخزين Gradle wrapper ومكتبات التبعيات وConfiguration Cache.
  - فحص الكود وتشغيل الـLint واختبارات الوحدة (`testDebugUnitTest`).
  - بناء APK تجريبي للتحقق من سلامة البناء (`assembleDebug`).
  - رفع تقارير الاختبارات وملف الـAPK كـArtifacts.

### ب. مسار الإصدار والأتمتة: `.github/workflows/release.yml` (Release Workflow)
- **المحفزات**:
  - عند دفع Tag يبدأ بالحرف `v` ومتبوع برقم الإصدار (مثل `v1.0.0` أو `v1.2.3`).
  - إمكانية التشغيل اليدوي من تبويب Actions في GitHub مع إدخال رقم الـTag.
- **الحماية والأمان**:
  - **حماية مطلقة**: تشغيل جميع الاختبارات الآلية أولاً، وإذا فشل أي اختبار أو فشل فحص مفتاح التوقيع يتم إيقاف العملية فورًا ومنع نشر الإصدار.
  - **حذف آمن وفوري**: يتم حذف ملف `release.keystore` من قرص البيئة الافتراضية بمجرد انتهاء البناء (`Secure Keystore Cleanup`).
  - **إدارة الإصدارات الذكية**:
    - استخراج `versionName` تلقائيًا من الـTag (مثال: `v1.2.3` ➔ `1.2.3`).
    - حساب `versionCode` ديناميكيًا بصيغة تصاعدية صارمة تضمن عدم تكراره أو خفضه:
      `VERSION_CODE = (MAJOR * 100000) + (MINOR * 1000) + (PATCH * 10) + COMMIT_COUNT`
  - **بناء ملفات الإنتاج**:
    - بناء ملف APK جاهز للتثبيت المباشر (`assembleRelease`).
    - بناء حزمة Android App Bundle (AAB) لمتجر Google Play (`bundleRelease`).
    - إنشاء بصمات التحقق الرقمية `SHA256SUMS.txt`.
  - **إنشاء GitHub Release تلقائيًا**:
    - رفع ملفات `Veyronis-vX.Y.Z.apk` و`Veyronis-vX.Y.Z.aab` وملف البصمات.
    - توليد ملاحظات الإصدار (Release Notes / Changelog) تلقائيًا.

---

## 2. المفاتيح والبيانات السرية المطلوبة في GitHub (GitHub Secrets)

يجب إعداد الأسرار التالية في مستودع GitHub عبر:
**Settings** ➔ **Secrets and variables** ➔ **Actions** ➔ **New repository secret**:

| اسم السر (Secret Name) | الوصف | مثال |
| :--- | :--- | :--- |
| `KEYSTORE_BASE64` | محتوى ملف `release.keystore` مشفرًا بصيغة Base64 | `MIIKPQIBAzCCCgcGCSqGSIb3DQEHAaCCCfgE...` |
| `KEYSTORE_PASSWORD` | كلمة مرور مخزن المفاتيح (Keystore Password) | `YourSecureKeystorePassword123!` |
| `KEY_ALIAS` | الاسم التعريفي للمفتاح داخل المخزن (Key Alias) | `veyronis` |
| `KEY_PASSWORD` | كلمة مرور المفتاح الخاص (Key Password) | `YourSecureKeyPassword123!` |

*(ملاحظة: الرمز الافتراضي `GITHUB_TOKEN` متوفر تلقائيًا ومدمج داخل بيئة GitHub Actions)*

---

## 3. كيفية إنشاء مفتاح التوقيع الدائم الثابت (One-Time Generation)

> **قاعدة ذهبية هامة**: يُنشأ هذا المفتاح **مرة واحدة فقط** ويُحتفظ به في مكان آمن ومشفر مدى الحياة. تغيير هذا المفتاح مستقبلاً يمنع المستخدمين من تحديث التطبيق على أجهزتهم.

### الخطوة 1: توليد ملف `release.keystore` محليًا
قم بتشغيل الأمر التالي في سطر الأوامر (Terminal) على جهازك:

```bash
keytool -genkeypair -v \
  -keystore release.keystore \
  -alias veyronis \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storetype JKS
```

### الخطوة 2: تحويل المفتاح إلى Base64 لإضافته في GitHub Secrets

- **على أنظمة Linux / macOS**:
  ```bash
  base64 -w 0 release.keystore > release_keystore_base64.txt
  # أو على macOS:
  # base64 -i release.keystore -o release_keystore_base64.txt
  ```

- **على أنظمة Windows (PowerShell)**:
  ```powershell
  [Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore")) | Out-File -Encoding ASCII release_keystore_base64.txt
  ```

انسخ النص الناتج وضعه داخل السر `KEYSTORE_BASE64` في GitHub Secrets.

---

## 4. كيفية إطلاق إصدار جديد (Triggering a Release)

كل ما عليك فعله لإطلاق إصدار جديد هو إنشاء Tag ودفعه إلى GitHub:

```bash
git tag v1.0.0
git push origin v1.0.0
```

سيقوم GitHub Actions فورًا بالتقاط الحدث، تشغيل الاختبارات، بناء وتوقيع الـAPK والـAAB، وإنشاء GitHub Release ورفع الملفات إليه تلقائيًا.
