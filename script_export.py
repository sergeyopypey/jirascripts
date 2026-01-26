import json
import os
import re

# Пусть файл JSON лежит рядом с этим скриптом
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
JSON_FILE = os.path.join(SCRIPT_DIR, "json.json")
EXPORT_DIR = os.path.join(SCRIPT_DIR, "export")

def safe_name(name: str) -> str:
    if not name:
        return "unnamed"
    name = name.strip()
    # Заменяем запрещённые символы Windows
    name = re.sub(r'[<>:"/\\|?*]', '_', name)
    # Убираем повторяющиеся _
    name = re.sub(r'_+', '_', name)
    return name


def write_script(dir_name, file_name, content):
    """Записывает скрипт в файл, убирая лишние пустые строки"""
    os.makedirs(dir_name, exist_ok=True)
    path = os.path.join(dir_name, file_name)
    
    # Чистим лишние пробелы в конце и пустые строки
    lines = [line.rstrip() for line in content.splitlines()]
    cleaned_content = "\n".join(lines) + "\n"  # завершаем файлом одной пустой строкой

    with open(path, "w", encoding="utf-8") as f:
        f.write(cleaned_content)
    print(f"✔ Saved: {path}")

def export_scripts(category_name, items, base_dir):
    for item in items:
        raw_item_name = item.get("name") or f"{category_name}_{item.get('id')}"
        item_name = safe_name(raw_item_name)
        item_dir = os.path.join(base_dir, category_name, item_name)

        if category_name == "workflows":
            for transition in item.get("transitions", []):
                transition_name = safe_name(transition.get("name") or f"transition_{transition.get('id')}")
                transition_dir = os.path.join(item_dir, transition_name)

                for cfg in transition.get("configuredItems", []):
                    cfg_name = safe_name(cfg.get("name") or "configured_item")
                    cfg_dir = os.path.join(transition_dir, cfg_name)

                    for script in cfg.get("scripts", []):
                        content = script.get("inlineScript")
                        if not content:
                            continue
                        script_name = safe_name(script.get("name") or "script") + ".groovy"
                        write_script(cfg_dir, script_name, content)

        else:
            for script in item.get("scripts", []):
                content = script.get("inlineScript")
                if not content:
                    continue

                if script.get("scriptFile"):
                    script_name = safe_name(os.path.basename(script["scriptFile"]))
                else:
                    script_name = safe_name(script.get("name") or "script") + ".groovy"

                write_script(item_dir, script_name, content)


def main():
    print(f"📄 Using JSON file: {JSON_FILE}")
    with open(JSON_FILE, "r", encoding="utf-8") as f:
        data = json.load(f)

    output = data.get("output", {})

    print("Starting export...")

    # Обрабатываем все категории
    categories = ["workflows", "listeners", "fields", "endpoints", "behaviours", "fragments", "jobs"]
    for category in categories:
        items = output.get(category, [])
        if items:
            print(f"Exporting {category} ({len(items)})...")
            export_scripts(category, items, EXPORT_DIR)

    print("\n🎉 Export complete!")

if __name__ == "__main__":
    main()
