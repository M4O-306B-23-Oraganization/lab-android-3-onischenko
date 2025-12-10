#!/usr/bin/env python

import argparse
import re
import shutil
from pathlib import Path


def parse_args():
    parser = argparse.ArgumentParser(description="Rename Android Compose template package and app name.")
    parser.add_argument("-o", "--old-id", required=True, help="Old applicationId / package (e.g. ru.lavafrai.study.template)")
    parser.add_argument("-n", "--new-id", required=True, help="New applicationId / package (e.g. com.example.myapp)")
    parser.add_argument("-N", "--new-name", required=False, help="New app name (string resource app_name)")
    parser.add_argument(
        "-r",
        "--project-root",
        required=False,
        help="Project root path (defaults to parent of this script)",
    )
    parser.add_argument(
        "--update-readme",
        action="store_true",
        help="Also update occurrences in README.md",
    )
    return parser.parse_args()


def split_package(pkg: str):
    return [p for p in pkg.split(".") if p]


def update_gradle_files(project_root: Path, old_id: str, new_id: str):
    app_build = project_root / "app" / "build.gradle.kts"
    if not app_build.exists():
        print(f"[WARN] app/build.gradle.kts not found at {app_build}")
        return

    text = app_build.read_text(encoding="utf-8")
    if old_id not in text:
        print(f"[WARN] Old id '{old_id}' not found in app/build.gradle.kts")

    text_new = text.replace(f'namespace = "{old_id}"', f'namespace = "{new_id}"')
    text_new = text_new.replace(f'applicationId = "{old_id}"', f'applicationId = "{new_id}"')

    app_build.write_text(text_new, encoding="utf-8")
    print("[OK] Updated namespace and applicationId in app/build.gradle.kts")


def update_kotlin_sources(project_root: Path, old_id: str, new_id: str):
    src_root = project_root / "app" / "src" / "main" / "java"
    if not src_root.exists():
        print(f"[WARN] Kotlin source root not found at {src_root}")
        return

    for path in src_root.rglob("*.kt"):
        text = path.read_text(encoding="utf-8")
        if old_id not in text:
            continue
        new_text = text.replace(old_id, new_id)
        if new_text != text:
            path.write_text(new_text, encoding="utf-8")
            rel = path.relative_to(project_root)
            print(f"[OK] Updated package/imports in {rel}")


def rename_package_directories(project_root: Path, old_id: str, new_id: str):
    src_root = project_root / "app" / "src" / "main" / "java"
    if not src_root.exists():
        print(f"[WARN] Kotlin source root not found at {src_root}")
        return

    old_parts = split_package(old_id)
    new_parts = split_package(new_id)

    old_dir = src_root.joinpath(*old_parts)
    new_dir = src_root.joinpath(*new_parts)

    if not old_dir.exists():
        print(f"[WARN] Old package directory not found: {old_dir}")
        return

    new_dir.parent.mkdir(parents=True, exist_ok=True)

    # Move all contents from old_dir to new_dir
    new_dir.mkdir(exist_ok=True)
    for item in old_dir.iterdir():
        target = new_dir / item.name
        if target.exists():
            if item.is_dir():
                # merge directories
                for sub in item.rglob("*"):
                    rel_sub = sub.relative_to(item)
                    target_sub = target / rel_sub
                    if sub.is_dir():
                        target_sub.mkdir(parents=True, exist_ok=True)
                    else:
                        target_sub.parent.mkdir(parents=True, exist_ok=True)
                        shutil.move(str(sub), str(target_sub))
            else:
                # overwrite file
                shutil.move(str(item), str(target))
        else:
            shutil.move(str(item), str(target))

    # Remove old empty directories upwards
    current = old_dir
    while current != src_root and current.exists():
        try:
            current.rmdir()
            current = current.parent
        except OSError:
            # not empty
            break

    print(f"[OK] Renamed package directory {old_dir} -> {new_dir}")


def update_app_name(project_root: Path, new_name: str | None):
    if not new_name:
        return

    strings_xml = project_root / "app" / "src" / "main" / "res" / "values" / "strings.xml"
    if not strings_xml.exists():
        print(f"[WARN] strings.xml not found at {strings_xml}")
        return

    text = strings_xml.read_text(encoding="utf-8")

    # Пробуем заменить через регулярное выражение тег app_name
    pattern = r"(<string\\s+name=\\"app_name\\">)(.*?)(</string>)"

    def repl(match: re.Match):
        return match.group(1) + new_name + match.group(3)

    new_text, count = re.subn(pattern, repl, text, flags=re.DOTALL)
    if count == 0:
        print("[WARN] <string name=\"app_name\"> not found in strings.xml")
        return

    strings_xml.write_text(new_text, encoding="utf-8")
    print(f"[OK] Updated app_name in {strings_xml}")


def update_readme(project_root: Path, old_id: str, new_id: str, new_name: str | None):
    readme = project_root / "README.md"
    if not readme.exists():
        print(f"[WARN] README.md not found at {readme}")
        return

    text = readme.read_text(encoding="utf-8")
    changed = False

    if old_id in text:
        text = text.replace(old_id, new_id)
        changed = True
        print("[OK] Updated package occurrences in README.md")

    # Предполагаем, что старое имя по умолчанию — "android app"
    if new_name:
        old_name = "android app"
        if old_name in text:
            text = text.replace(old_name, new_name)
            changed = True
            print("[OK] Updated app name occurrences in README.md")

    if changed:
        readme.write_text(text, encoding="utf-8")
    else:
        print("[INFO] No README.md changes were necessary")


def main():
    args = parse_args()

    if args.project_root:
        project_root = Path(args.project_root).resolve()
    else:
        # По умолчанию считаем, что скрипт лежит в scripts/ внутри корня
        script_path = Path(__file__).resolve()
        project_root = script_path.parent.parent

    print(f"[INFO] Project root: {project_root}")

    old_id = args.old_id.strip()
    new_id = args.new_id.strip()

    if old_id == new_id:
        print("[ERROR] old-id and new-id are the same; nothing to do.")
        return

    print(f"[INFO] Renaming package/applicationId: {old_id} -> {new_id}")

    update_gradle_files(project_root, old_id, new_id)
    update_kotlin_sources(project_root, old_id, new_id)
    rename_package_directories(project_root, old_id, new_id)

    if args.new_name:
        update_app_name(project_root, args.new_name)

    if args.update_readme:
        update_readme(project_root, old_id, new_id, args.new_name)

    print("[DONE] Rename completed. Please sync and rebuild the project in Android Studio.")


if __name__ == "__main__":
    main()

