#!/usr/bin/env python3

from __future__ import annotations

import argparse
import concurrent.futures
import json
import mimetypes
import posixpath
import re
import sys
import time
from pathlib import Path
from typing import Iterable
from urllib.parse import urljoin, urlparse, urlunparse

from bs4 import BeautifulSoup
from markdownify import markdownify as html_to_markdown
from urllib.request import Request, urlopen


BASE_URL = "https://doc.iocoder.cn"
USER_AGENT = "Mozilla/5.0 (compatible; CodexDocMirror/1.0)"
SKIP_PREFIXES = (
    "/assets/",
    "/img/",
    "/favicon",
)
SKIP_SUFFIXES = (
    ".css",
    ".js",
    ".json",
    ".xml",
    ".ico",
    ".png",
    ".jpg",
    ".jpeg",
    ".gif",
    ".svg",
    ".webp",
    ".pdf",
    ".zip",
)


def fetch(url: str, *, binary: bool = False) -> bytes | str:
    req = Request(url, headers={"User-Agent": USER_AGENT})
    with urlopen(req, timeout=30) as resp:
        body = resp.read()
    return body if binary else body.decode("utf-8", errors="replace")


def canonicalize(url: str) -> str | None:
    absolute = urljoin(BASE_URL, url)
    parsed = urlparse(absolute)
    if parsed.scheme not in {"http", "https"}:
        return None
    if parsed.netloc != urlparse(BASE_URL).netloc:
        return None
    path = parsed.path or "/"
    if path != "/" and path.endswith("/index.html"):
        path = path[: -len("index.html")]
    if path != "/" and not path.endswith("/") and "." not in path.rsplit("/", 1)[-1]:
        path = path + "/"
    if path != "/" and path.endswith("/"):
        path = path.rstrip("/") + "/"
    cleaned = parsed._replace(query="", fragment="", path=path)
    return urlunparse(cleaned)


def is_doc_url(url: str) -> bool:
    parsed = urlparse(url)
    path = parsed.path or "/"
    if any(path.startswith(prefix) for prefix in SKIP_PREFIXES):
        return False
    if any(path.lower().endswith(suffix) for suffix in SKIP_SUFFIXES):
        return False
    last_segment = path.rsplit("/", 1)[-1]
    if "." in last_segment:
        return False
    return True


def output_path_for_url(output_root: Path, url: str) -> Path:
    path = urlparse(url).path or "/"
    if path == "/":
        return output_root / "pages" / "home" / "index.md"
    clean = path.lstrip("/")
    if clean.endswith("/"):
        clean = f"{clean}index"
    return output_root / "pages" / f"{clean}.md"


def image_output_path(output_root: Path, url: str, content_type: str | None = None) -> Path:
    parsed = urlparse(url)
    path = parsed.path.lstrip("/") or "asset"
    ext = Path(path).suffix
    if not ext:
        guessed = mimetypes.guess_extension(content_type or "") or ".bin"
        path += guessed
    return output_root / "assets" / parsed.netloc / path


def clean_content(content: BeautifulSoup) -> None:
    for selector in [
        ".header-anchor",
        ".copy-code-button",
        ".page-edit",
        ".page-nav-wapper",
        ".page-nav",
        ".wwads-cn",
        "script",
        "style",
        "iframe",
    ]:
        for node in content.select(selector):
            node.decompose()

    for node in content.find_all(["svg"]):
        node.decompose()

    for node in content.find_all(class_=re.compile(r"icon|outbound|sr-only")):
        node.decompose()


def rel_link(from_md: Path, to_md: Path) -> str:
    return posixpath.relpath(to_md.as_posix(), start=from_md.parent.as_posix())


def normalize_space(text: str) -> str:
    return re.sub(r"\n{3,}", "\n\n", text).strip() + "\n"


def extract_page(
    html: str,
    page_url: str,
    output_root: Path,
    all_doc_urls: set[str],
    downloaded_assets: dict[str, str],
) -> tuple[str, str]:
    soup = BeautifulSoup(html, "html.parser")
    content = soup.select_one("div.theme-default-content") or soup.select_one("div.content__default")
    if content is None:
        raise RuntimeError(f"Could not find main content in {page_url}")

    clean_content(content)

    current_md = output_path_for_url(output_root, page_url)

    for img in content.find_all("img"):
        src = img.get("src")
        if not src:
            continue
        asset_url = canonicalize(src)
        if not asset_url:
            continue
        try:
            asset_path = downloaded_assets.get(asset_url)
            if asset_path is None:
                data = fetch(asset_url, binary=True)
                out_path = image_output_path(output_root, asset_url)
                out_path.parent.mkdir(parents=True, exist_ok=True)
                out_path.write_bytes(data)
                asset_path = rel_link(current_md, out_path)
                downloaded_assets[asset_url] = out_path.as_posix()
            else:
                asset_path = rel_link(current_md, Path(asset_path))
            img["src"] = asset_path
        except Exception:
            img["src"] = asset_url

    for link in content.find_all("a"):
        href = link.get("href")
        if not href:
            continue
        full = canonicalize(urljoin(page_url, href))
        if full and full in all_doc_urls and is_doc_url(full):
            target_md = output_path_for_url(output_root, full)
            link["href"] = rel_link(current_md, target_md)
        else:
            absolute = urljoin(page_url, href)
            link["href"] = absolute

    title = ""
    first_h1 = content.find("h1")
    if first_h1:
        title = first_h1.get_text(" ", strip=True)
    if not title:
        title = soup.title.get_text(" ", strip=True).split("|", 1)[0].strip()

    markdown = html_to_markdown(
        str(content),
        heading_style="ATX",
        bullets="-",
        strong_em_symbol="*",
    )
    markdown = normalize_space(markdown)
    markdown = f"# {title}\n\nSource: {page_url}\n\n{markdown}"
    return title, markdown


def collect_links_from_html(page_url: str, html: str) -> set[str]:
    soup = BeautifulSoup(html, "html.parser")
    found: set[str] = set()
    for link in soup.find_all("a", href=True):
        full = canonicalize(urljoin(page_url, link["href"]))
        if full and is_doc_url(full):
            found.add(full)
    return found


def discover(start_urls: Iterable[str], limit: int | None = None) -> list[str]:
    seeds = [canonicalize(url) for url in start_urls]
    seeds = [url for url in seeds if url and is_doc_url(url)]
    discovered: set[str] = set(seeds)

    print(f"Discovering from {len(seeds)} seed pages...", flush=True)
    seed_html: dict[str, str] = {}
    for seed in seeds:
        html = fetch(seed)
        seed_html[seed] = html
        discovered.update(collect_links_from_html(seed, html))
        print(f"Seed scanned: {seed} ({len(discovered)} pages so far)", flush=True)

    if limit:
        return sorted(discovered)[:limit]

    candidates = sorted(discovered)
    print(f"Expanding discovery across {len(candidates)} candidate pages...", flush=True)

    def worker(url: str) -> set[str]:
        try:
            html = seed_html.get(url) or fetch(url)
            return collect_links_from_html(url, html)
        except Exception:
            return set()

    with concurrent.futures.ThreadPoolExecutor(max_workers=8) as executor:
        for index, extra_links in enumerate(executor.map(worker, candidates), start=1):
            discovered.update(extra_links)
            if index % 25 == 0 or index == len(candidates):
                print(
                    f"Discovery progress: {index}/{len(candidates)} scanned, {len(discovered)} unique pages",
                    flush=True,
                )

    return sorted(discovered)


def write_index(output_root: Path, pages: list[dict[str, str]]) -> None:
    lines = [
        "# iocoder docs mirror",
        "",
        f"Total pages: {len(pages)}",
        "",
    ]
    for page in pages:
        lines.append(f"- [{page['title']}]({page['path']})")
        lines.append(f"  - Source: {page['url']}")
    lines.append("")
    (output_root / "README.md").write_text("\n".join(lines), encoding="utf-8")
    (output_root / "manifest.json").write_text(
        json.dumps(pages, ensure_ascii=False, indent=2), encoding="utf-8"
    )


def main() -> int:
    parser = argparse.ArgumentParser(description="Mirror doc.iocoder.cn pages to markdown.")
    parser.add_argument(
        "--output",
        default="docs/iocoder-docs",
        help="Output directory for markdown files.",
    )
    parser.add_argument(
        "--limit",
        type=int,
        default=None,
        help="Optional page limit for debugging.",
    )
    args = parser.parse_args()

    output_root = Path(args.output).resolve()
    output_root.mkdir(parents=True, exist_ok=True)

    start_urls = [
        f"{BASE_URL}/intro/",
        f"{BASE_URL}/bpm/model-designer-bpmn/",
    ]
    all_doc_urls = set(discover(start_urls, limit=args.limit))

    pages: list[dict[str, str]] = []
    downloaded_assets: dict[str, str] = {}
    for index, url in enumerate(sorted(all_doc_urls), start=1):
        try:
            html = fetch(url)
        except Exception as exc:
            print(f"Skipping {url}: fetch failed ({exc})", flush=True)
            continue
        try:
            title, markdown = extract_page(
                html=html,
                page_url=url,
                output_root=output_root,
                all_doc_urls=all_doc_urls,
                downloaded_assets=downloaded_assets,
            )
        except RuntimeError as exc:
            print(f"Skipping {url}: {exc}", flush=True)
            continue
        out_path = output_path_for_url(output_root, url)
        out_path.parent.mkdir(parents=True, exist_ok=True)
        out_path.write_text(markdown, encoding="utf-8")
        pages.append(
            {
                "title": title,
                "url": url,
                "path": out_path.relative_to(output_root).as_posix(),
            }
        )
        print(
            f"[{index}/{len(all_doc_urls)}] {url} -> {out_path.relative_to(output_root)}",
            flush=True,
        )
        time.sleep(0.05)

    write_index(output_root, pages)
    print(f"Wrote {len(pages)} markdown files to {output_root}", flush=True)
    return 0


if __name__ == "__main__":
    sys.exit(main())
