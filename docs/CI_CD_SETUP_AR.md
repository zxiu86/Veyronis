# دليل إعداد GitHub Actions وCI/CD الموحد لتطبيق Veyronis

تم دمج وتوحيد مسار البناء والاختبار والإصدار التلقائي داخل ملف مسار عمل واحد متكامل:
`.github/workflows/ci.yml`

---

## 1. مسار العمل الموحد (`.github/workflows/ci.yml`)

يحتوي المسار على مرحلتين متتاليتين مترابطتين:

### المرحلة الأولى: الفحص والاختبارات (`validate-and-test`)
- **المحفزات**:
  - يتم تشغيله تلقائيًا عند أي `push` إلى الفروع (`main`, `master`, `develop`) أو عند دفع `Tag` يبدأ بـ `v*`.
  - يعمل عند فتح أي `pull_request` موجه إلى `main` أو `master`.
  - إمكانية التشغيل اليدوي (`workflow_dispatch`).
- **المهام المنفذة**:
  - تفعيل تسريع التخزين المؤقت **GitHub Actions Cache** عبر `gradle/actions/setup-gradle@v4` و`actions/cache@v4` لتخزين التبعيات وConfiguration Cache.
  - فحص الكود وتشغيل الـLint واختبارات الوحدة (`testDebugUnitTest`).
  - بناء APK تجريبي والتأكد من سلامة التجميع وخلوه من أخطاء KSP أو التوقيع (`assembleDebug`).
  - رفع تقارير الفحص والـAPK كـArtifacts.

### المرحلة الثانية: الإصدار والإنتاج التلقائي (`build-and-release`)
- **المحفزات التلقائية**:
  - تعمل هذه المرحلة **تلقائيًا** بعد نجاح المرحلة الأولى عند الرفع (`push`) إلى فرع الإنتاج `main` أو `master`، أو عند دفع `Tag` بإصدار جديد.
- **إدارة الإصدارات الذكية من `version.json`**:
  - يتم قراءة رقم الإصدار مباشرة وتلقائيًا من ملف `version.json`:
    ```json
    {
      "versionName": "1.0.0",
      "versionCode": 1,
      "releaseNotes": "وصف التحديث..."
    }
    ```
  - إذا تم تحديث رقم الإصدار في `version.json` أو تم دفع `Tag`، يقوم الـWorkflow بإنشاء الإصدار ومطابقة رقم `versionCode` بحيث يكون تصاعديًا بشكل صارم ويستحيل تكراره أو خفضه.
- **بناء حزم الإنتاج الموقعة**:
  - بناء APK إنتاج كامل (`assembleRelease`).
  - بناء حزمة Google Play Bundle (`bundleRelease`).
  - إنشاء ملف بصمات التحقق الرقمي `SHA256SUMS.txt`.
- **نشر GitHub Release رسمي**:
  - إنشاء Release تلقائي برقم الإصدار ورفع ملفات `Veyronis-vX.Y.Z.apk` و`Veyronis-vX.Y.Z.aab` وملف البصمات.
  - توليد Release Notes تلقائيًا من سجل التغييرات في Git.

---

## 2. إعداد مفتاح التوقيع الثابت `release.keystore`

> تم تعديل إعدادات التوقيع في المشروع لتقبل **`release.keystore`** بشكل أساسي في كل من البناء التجريبي وبناء الإنتاج، مع توفير بديل آمن يمنع توقف البناء في حال عدم توفر المفتاح أثناء مرحلة الـCI الأولى.

### الأسرار المطلوبة في GitHub Secrets:
في إعدادات المستودع على GitHub:
**Settings** ➔ **Secrets and variables** ➔ **Actions** ➔ **New repository secret**:

| اسم السر (Secret Name) | الوصف | مثال |
| :--- | :--- | :--- |
| `KEYSTORE_BASE64` | محتوى ملف `release.keystore` مشفرًا بنص Base64 | `MIIKPQIBAzCCCgcGCSqGSIb3DQEHAaCCCfgE...` |
| `KEYSTORE_PASSWORD` | كلمة مرور مخزن المفاتيح (Keystore Password) | `YourKeystorePassword123!` |
| `KEY_ALIAS` | الاسم التعريفي للمفتاح داخل المخزن (Key Alias) | `veyronis` |
| `KEY_PASSWORD` | كلمة مرور المفتاح الخاص (Key Password) | `YourKeyPassword123!` |

---

## 3. كيفية توليد ملف `release.keystore` الثابت (مرة واحدة فقط)

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

ثم تحويله إلى نص Base64:
- **على Linux / macOS**:
  ```bash
  base64 -w 0 release.keystore > release_keystore_base64.txt
  ```
- **على Windows (PowerShell)**:
  ```powershell
  [Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore")) | Out-File -Encoding ASCII release_keystore_base64.txt
  ```

انسخ محتوى الملف وضعه في السر `KEYSTORE_BASE64` على GitHub.

---

## 4. كيفية إطلاق إصدار جديد تلقائيًا

كل ما عليك فعله لإطلاق إصدار جديد هو أحد أمرين:

1. **تحديث ملف `version.json` في المستودع ودفعه إلى `main`**:
   ```json
   {
     "versionName": "1.0.1",
     "versionCode": 2,
     "releaseNotes": "تحسينات جديدة ودعم مزامنة البيانات"
   }
   ```
   بمجرد عمل `git commit` و `git push origin main`، سيعمل الـ CI/CD تلقائيًا، ينفذ الفحص، ويبني الإصدار ويرفعه إلى GitHub Releases.

2. **أو دفع Tag من خلال Git**:
   ```bash
   git tag v1.0.1
   git push origin v1.0.1
   ```
