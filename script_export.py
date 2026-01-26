import json
import os
import re

# Папка экспорта (создаётся рядом с этим скриптом)
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
EXPORT_ROOT = os.path.join(SCRIPT_DIR, "export")


def safe_name(name: str) -> str:
    """Конвертирует имя в безопасное имя файла."""
    if not name:
        return "unnamed"
    name = str(name).strip()
    name = re.sub(r"\s+", "_", name)  # пробелы → подчёркивания
    return re.sub(r"[^a-zA-Z0-9._-]", "_", name)


def write_script(dir_name, file_name, content):
    """Записывает скрипт в файл, очищая лишние пробелы и пустые строки."""
    os.makedirs(dir_name, exist_ok=True)
    path = os.path.join(dir_name, file_name)

    # Убираем \r, лишние пробелы в конце строк, множественные пустые строки
    lines = content.replace("\r\n", "\n").splitlines()
    cleaned_lines = []
    blank_count = 0
    for line in lines:
        stripped = line.rstrip()
        if stripped:
            cleaned_lines.append(stripped)
            blank_count = 0
        else:
            blank_count += 1
            if blank_count == 1:  # оставляем только одну пустую строку между блоками
                cleaned_lines.append("")

    cleaned_content = "\n".join(cleaned_lines).rstrip() + "\n"

    with open(path, "w", encoding="utf-8") as f:
        f.write(cleaned_content)
    print(f"✔ Saved: {os.path.relpath(path, SCRIPT_DIR)}")


# ====================== ОБРАБОТЧИКИ ======================

def process_listeners(items):
    outdir = os.path.join(EXPORT_ROOT, "listeners")
    for item in items:
        name = item.get("name") + "__" + item.get("id")
        for script_item in item.get("scripts", []):
            script = script_item.get("inlineScript")
            if not script:
                continue
            fname = f"{safe_name(name)}.groovy"
            write_script(outdir, fname, script)


def process_endpoints(items):
    outdir = os.path.join(EXPORT_ROOT, "endpoints")
    for item in items:
        name = item.get("name") + "__" + item.get("id")
        for script_item in item.get("scripts", []):
            script = script_item.get("inlineScript")
            if not script:
                continue
            fname = f"{safe_name(name)}.groovy"
            write_script(outdir, fname, script)


def process_behaviours(items):
    outdir = os.path.join(EXPORT_ROOT, "behaviours")
    for item in items:
        name = item.get("name") or item.get("id") or "unknown_behaviour"
        # Behaviours могут иметь несколько скриптов (Initialiser, Server-side и т.д.)
        for script_item in item.get("scripts", []):
            script_name = script_item.get("name") or "script"
            script = script_item.get("inlineScript")
            if not script:
                continue
            fname = f"{safe_name(name)}__{safe_name(script_name)}.groovy"
            write_script(outdir, fname, script)


def process_fragments(items):
    outdir = os.path.join(EXPORT_ROOT, "fragments")
    for item in items:
        name = item.get("name") or item.get("id") or "unknown_fragment"
        for script_item in item.get("scripts", []):
            script_name = script_item.get("name") or "script"
            script = script_item.get("inlineScript")
            if not script:
                continue
            fname = f"{safe_name(name)}__{safe_name(script_name)}.groovy"
            write_script(outdir, fname, script)


def process_fields(items):
    outdir = os.path.join(EXPORT_ROOT, "fields")
    for item in items:
        name = item.get("name") or item.get("id") or "unknown_field"
        for script_item in item.get("scripts", []):
            script = script_item.get("inlineScript")
            if not script:
                continue
            fname = f"{safe_name(name)}.groovy"
            write_script(outdir, fname, script)


def process_jobs(items):
    outdir = os.path.join(EXPORT_ROOT, "jobs")
    for item in items:
        name = item.get("name") + "__" + item.get("id")
        for script_item in item.get("scripts", []):
            script = script_item.get("inlineScript")
            if not script:
                continue
            fname = f"{safe_name(name)}.groovy"
            write_script(outdir, fname, script)


def process_workflows(workflows):
    outdir = os.path.join(EXPORT_ROOT, "workflows")
    for wf in workflows:
        wf_name = wf.get("name") or "unknown_workflow"
        wf_dir = os.path.join(outdir, safe_name(wf_name))
        
        transitions = wf.get("transitions", [])
        for trans in transitions:
            trans_name = trans.get("name") or f"transition_{trans.get('id', 'unknown')}"
            trans_dir = os.path.join(wf_dir, safe_name(trans_name))
            
            configured_items = trans.get("configuredItems", [])
            for item in configured_items:
                item_name = item.get("name") or "unknown_item"
                for script_item in item.get("scripts", []):
                    script = script_item.get("inlineScript")
                    if not script:
                        continue
                    script_label = script_item.get("name") or "script"
                    # Пример имени: In_Progress__Custom_script_validator.groovy
                    fname = f"{safe_name(item_name)}__{safe_name(script_label)}.groovy"
                    write_script(trans_dir, fname, script)


def find_json_file(script_dir):
    """Находит первый JSON-файл в папке скрипта."""
    for fname in os.listdir(script_dir):
        if fname.lower().endswith(".json"):
            return os.path.join(script_dir, fname)
    return None


def main():
    json_path = find_json_file(SCRIPT_DIR)
    if not json_path:
        print("ERROR: JSON file not found next to the script.")
        return

    print(f"Using JSON file: {json_path}")

    with open(json_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    output = data.get("output", {})

    print(f"Starting export...")
    print(f"Export root: {EXPORT_ROOT}\n")

    process_listeners(output.get("listeners", []))
    process_endpoints(output.get("endpoints", []))
    process_behaviours(output.get("behaviours", []))
    process_fragments(output.get("fragments", []))
    process_fields(output.get("fields", []))
    process_jobs(output.get("jobs", []))
    process_workflows(output.get("workflows", []))

    print("\nExport complete!")


if __name__ == "__main__":
    main()
