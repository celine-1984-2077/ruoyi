#!/usr/bin/env python3

from __future__ import annotations

import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DOCS_ROOT = ROOT / "docs" / "iocoder-docs"
MANIFEST_PATH = DOCS_ROOT / "manifest.json"
OUTPUT_PATH = ROOT / "mkdocs.yml"


SECTION_LABELS = {
    "pages": "全部文档",
    "ai": "AI 手册",
    "admin-uniapp": "Admin Uniapp",
    "bpm": "工作流",
    "changelog": "更新日志",
    "crm": "CRM",
    "erp": "ERP",
    "iot": "IoT",
    "mall": "商城",
    "member": "会员",
    "message-queue": "消息队列",
    "mp": "公众号",
    "new-feature": "代码生成",
    "pay": "支付",
    "report": "大屏",
    "saas-tenant": "SaaS",
    "sale": "销售",
    "vben5": "Vben5 前端",
    "vue2": "Vue2 前端",
    "vue3": "Vue3 前端",
}


def label_for_segment(segment: str) -> str:
    return SECTION_LABELS.get(segment, segment.replace("-", " ").replace("_", " ").title())


def build_tree(manifest: list[dict[str, str]]) -> dict:
    root: dict[str, dict] = {}
    for item in manifest:
        path = item["path"]
        if path == "pages/home/index.md":
            continue
        parts = path.split("/")
        node = root
        for segment in parts[:-1]:
            node = node.setdefault(segment, {})
        node["__page__"] = {"title": item["title"], "path": path}
    return root


def sort_key(value: tuple[str, dict]) -> tuple[int, str]:
    name = value[0]
    if name == "__page__":
        return (0, name)
    return (1, name)


def render_nav(node: dict, level: int = 0) -> list[str]:
    lines: list[str] = []
    for name, child in sorted(node.items(), key=sort_key):
        if name == "__page__":
            page = child
            lines.append(f'{"  " * level}- "{page["title"]}": {page["path"]}')
            continue

        nested = child.copy()
        page = nested.pop("__page__", None)
        section_label = label_for_segment(name)
        indent = "  " * level
        if page and not nested:
            lines.append(f'{indent}- "{section_label}": {page["path"]}')
            continue

        lines.append(f'{indent}- "{section_label}":')
        if page:
            lines.append(f'{"  " * (level + 1)}- "概览": {page["path"]}')
        lines.extend(render_nav(nested, level + 1))
    return lines


def main() -> int:
    manifest = json.loads(MANIFEST_PATH.read_text(encoding="utf-8"))
    tree = build_tree(manifest)

    lines = [
        "site_name: RuoYi Docs Mirror",
        "site_description: Mirror of doc.iocoder.cn for local search and GitHub Pages hosting",
        "site_url: https://tony1120.github.io/ruoyi/",
        "repo_url: https://github.com/Tony1120/ruoyi",
        "repo_name: Tony1120/ruoyi",
        "docs_dir: docs/iocoder-docs",
        "site_dir: site",
        "use_directory_urls: true",
        "",
        "theme:",
        "  name: material",
        "  language: zh",
        "  features:",
        "    - navigation.instant",
        "    - navigation.sections",
        "    - navigation.expand",
        "    - navigation.indexes",
        "    - toc.follow",
        "    - search.suggest",
        "    - search.highlight",
        "  palette:",
        "    - scheme: default",
        '      primary: blue grey',
        '      accent: indigo',
        "",
        "plugins:",
        "  - search",
        "",
        "markdown_extensions:",
        "  - admonition",
        "  - attr_list",
        "  - def_list",
        "  - footnotes",
        "  - md_in_html",
        "  - tables",
        "  - toc:",
        "      permalink: true",
        "  - pymdownx.details",
        "  - pymdownx.highlight:",
        "      anchor_linenums: true",
        "  - pymdownx.inlinehilite",
        "  - pymdownx.snippets",
        "  - pymdownx.superfences",
        "  - pymdownx.tabbed:",
        "      alternate_style: true",
        "  - pymdownx.tasklist:",
        "      custom_checkbox: true",
        "",
        "extra:",
        "  social: []",
        "",
        "extra_css:",
        "  - stylesheets/docs.css",
        "",
        "nav:",
        '  - "首页": index.md',
        '  - "完整索引": all-pages.md',
    ]
    lines.extend(render_nav(tree, level=1))
    OUTPUT_PATH.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Wrote {OUTPUT_PATH}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
