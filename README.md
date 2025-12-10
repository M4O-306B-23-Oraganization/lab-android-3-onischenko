# Android Compose Template

Этот репозиторий — минимальный шаблон пустого Android‑приложения на **Kotlin + Jetpack Compose**. Он предназначен для быстрого старта новых проектов: вы копируете шаблон, переименовываете пакет и приложение под себя — и сразу можете писать код.

## Что внутри

- **Язык:** Kotlin.
- **UI:** Jetpack Compose, Material 3.
- **Сборка:** Gradle Kotlin DSL (`build.gradle.kts`, `settings.gradle.kts`).

Текущее базовое приложение использует:

- `applicationId` / namespace: `ru.lavafrai.study.template`.
- Пакеты кода: `ru.lavafrai.study.template`, `ru.lavafrai.study.template.ui.theme`, `ru.lavafrai.study.template.viewmodels`.
- Имя приложения: строковый ресурс `@string/app_name` со значением `android app`.

---

## Что нужно переименовать под своё приложение

Когда вы создаёте новое приложение на основе этого шаблона, вам, как минимум, нужно изменить:

1. **Идентификатор пакета / applicationId / namespace**

   - В файле `app/build.gradle.kts`:
     - `namespace = "ru.lavafrai.study.template"`
     - `defaultConfig { applicationId = "ru.lavafrai.study.template" }`
   - В коде Kotlin (`app/src/main/java/**`):
     - Директории: `app/src/main/java/ru/lavafrai/study/template/` и подпапки (`ui/theme`, `viewmodels`).
     - Объявления пакетов:
       - `package ru.lavafrai.study.template`
       - `package ru.lavafrai.study.template.ui.theme`
       - `package ru.lavafrai.study.template.viewmodels`
     - Импорты внутри проекта, например:
       - `import ru.lavafrai.study.template.ui.theme.AndroidAppTheme`
       - `import ru.lavafrai.study.template.viewmodels.MainViewModel`

   Новый `applicationId` должен быть в формате reverse DNS, например: `com.example.myapp`.

2. **Отображаемое имя приложения (app name)**

   - В `app/src/main/res/values/strings.xml`:
     - Строка:
       ```xml
       <string name="app_name">android app</string>
       ```
       Замените `android app` на нужное имя, например `My Cool App`.
   - В `app/src/main/AndroidManifest.xml`:
     - Атрибут приложения использует строковый ресурс:
       ```xml
       <application
           android:label="@string/app_name"
           ... />
       ```
       Обычно менять тут ничего не нужно, достаточно обновить строку `app_name`.

3. **MainActivity (опционально)**

   В шаблоне главная Activity называется `MainActivity` и лежит в пакете `ru.lavafrai.study.template`.

   - Файл: `app/src/main/java/ru/lavafrai/study/template/MainActivity.kt`.
   - В манифесте:
     ```xml
     <activity
         android:name=".MainActivity"
         ...>
         <intent-filter>
             <action android:name="android.intent.action.MAIN" />
             <category android:name="android.intent.category.LAUNCHER" />
         </intent-filter>
     </activity>
     ```

   Если вы хотите переименовать `MainActivity` или перенести её в другой пакет:

   - Переименуйте класс и файл в IDE.
   - Обновите `android:name` в манифесте, если класс переместился в другой пакет (например, `android:name=".ui.MainActivity"` или полное имя пакета).

## Ручное переименование под новый package/applicationId

Если вы не хотите использовать скрипт, можно переименоваться вручную.

1. **Выберите новый applicationId / пакет**

   Примеры:

   - Было: `ru.lavafrai.study.template`
   - Стало: `com.example.myapp`

2. **Переименуйте пакет в исходниках**

   В Android Studio или другой IDE используйте стандартный рефакторинг пакета:

   - Найдите пакет `ru.lavafrai.study.template` в дереве `app/src/main/java`.
   - Запустите Refactor → Rename/Move и задайте новый пакет `com.example.myapp`.
   - IDE автоматически обновит объявления `package` и импорты.

3. **Обновите Gradle‑конфигурацию**

   В `app/build.gradle.kts` замените значения на новый пакет:

   ```kotlin
   android {
       namespace = "com.example.myapp" // было "ru.lavafrai.study.template"

       defaultConfig {
           applicationId = "com.example.myapp" // было "ru.lavafrai.study.template"
           // ... остальное без изменений
       }
   }
   ```

4. **Обновите имя приложения**

   В `app/src/main/res/values/strings.xml` поменяйте `app_name`:

   ```xml
   <string name="app_name">My App</string>
   ```

5. **Проверьте и соберите проект**

   В корне проекта выполните:

   ```cmd
   cd E:\PROJECTS\MAI\android-template
   .\gradlew.bat assembleDebug
   ```

   Убедитесь, что:

   - приложение собирается без ошибок,
   - устанавливается с новым `applicationId`,
   - на устройстве отображается новое имя.

6. **(Опционально) Обновите README и другие тексты**

   Если вы храните этот README в вашем реальном проекте, обновите в нём примеры пакетов и имена приложения.

---

## Автоматическое переименование через Python‑скрипт

В репозитории есть скрипт `scripts/rename_app.py`, который автоматизирует описанные выше шаги.

### Возможности скрипта

Скрипт:

- меняет `namespace` и `applicationId` в `app/build.gradle.kts`;
- обновляет объявления пакетов и импорты в исходниках Kotlin внутри `app/src/main/java`;
- переименовывает директории пакетов (`ru/lavafrai/study/template` → `com/example/myapp`);
- по желанию меняет имя приложения в `app/src/main/res/values/strings.xml` (`app_name`);
- опционально обновляет упоминания старого `applicationId` и app name в `README.md`.

> ⚠️ Скрипт ориентирован на структуру именно этого шаблона. Если вы сильно меняли структуру каталогов или Gradle‑конфигурацию, сначала проверьте работу скрипта на отдельной ветке или после коммита.

### Аргументы командной строки

Скрипт принимает следующие параметры:

- Обязательные:
  - `-o, --old-id` — старый `applicationId` / пакет, например: `ru.lavafrai.study.template`.
  - `-n, --new-id` — новый `applicationId` / пакет, например: `com.example.myapp`.

- Опциональные:
  - `-N, --new-name` — новое отображаемое имя приложения (`app_name`), например: `"My App"`.
  - `-r, --project-root` — путь к корню проекта. По умолчанию — каталог, содержащий скрипт, или его родитель.
  - `--update-readme` — если указан, скрипт также попытается обновить упоминания старого `applicationId` и имени приложения в `README.md`.

### Примеры использования

1. Переименовать только пакет:

   ```cmd
   cd E:\PROJECTS\MAI\android-template
   python scripts\rename_app.py -o ru.lavafrai.study.template -n com.example.myapp
   ```

2. Переименовать пакет и имя приложения:

   ```cmd
   cd E:\PROJECTS\MAI\android-template
   python scripts\rename_app.py -o ru.lavafrai.study.template -n com.example.myapp -N "My App"
   ```

3. Дополнительно обновить README:

   ```cmd
   cd E:\PROJECTS\MAI\android-template
   python scripts\rename_app.py -o ru.lavafrai.study.template -n com.example.myapp -N "My App" --update-readme
   ```

### Рекомендации по использованию

- **Не запускайте скрипт параллельно с IDE‑рефакторингами.** Лучше закрыть Android Studio или хотя бы не делать одновременных изменений пакетов.
- **После запуска обязательно соберите проект**, чтобы убедиться, что всё переименовалось корректно:

  ```cmd
  cd E:\PROJECTS\MAI\android-template
  .\gradlew.bat assembleDebug
  ```

---

## Быстрый старт с шаблоном

1. Склонируйте репозиторий или создайте репозиторий из шаблона.
2. Запустите скрипт `scripts/rename_app.py` или выполните ручное переименование.
3. Откройте проект в Android Studio.
4. Запустите приложение на устройстве или эмуляторе.

Теперь у вас есть готовый каркас приложения на Compose под ваш пакет и названием.
