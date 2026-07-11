import fs from "node:fs";
import path from "node:path";
import process from "node:process";

const root = process.cwd();
const markdownFiles = [];
const failures = [];

function collectMarkdownFiles(directory) {
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    if (entry.name === ".git" || entry.name === "node_modules") {
      continue;
    }

    const entryPath = path.join(directory, entry.name);
    if (entry.isDirectory()) {
      collectMarkdownFiles(entryPath);
    } else if (entry.isFile() && entry.name.endsWith(".md")) {
      markdownFiles.push(entryPath);
    }
  }
}

function lineNumber(content, index) {
  return content.slice(0, index).split("\n").length;
}

function slugify(heading) {
  return heading
    .replace(/`/g, "")
    .replace(/<[^>]*>/g, "")
    .trim()
    .toLowerCase()
    .replace(/[^\p{L}\p{N}\s-]/gu, "")
    .replace(/\s+/g, "-");
}

function anchorsFor(filePath) {
  const seen = new Map();
  const anchors = new Set();
  const content = fs.readFileSync(filePath, "utf8");

  for (const match of content.matchAll(/^#{1,6}\s+(.+?)\s*#*\s*$/gm)) {
    const base = slugify(match[1]);
    const count = seen.get(base) ?? 0;
    seen.set(base, count + 1);
    anchors.add(count === 0 ? base : `${base}-${count}`);
  }

  return anchors;
}

function isExternal(target) {
  return /^(?:https?:|mailto:|tel:|data:)/i.test(target);
}

collectMarkdownFiles(root);

for (const sourceFile of markdownFiles) {
  const content = fs.readFileSync(sourceFile, "utf8");

  for (const match of content.matchAll(/(?<!!)(?:\[[^\]]*\])\(([^)\s]+)(?:\s+"[^"]*")?\)/g)) {
    let target = match[1];
    if (target.startsWith("<") && target.endsWith(">")) {
      target = target.slice(1, -1);
    }

    if (isExternal(target)) {
      continue;
    }

    const [rawPath, rawAnchor] = target.split("#", 2);
    const targetFile = rawPath
      ? path.resolve(path.dirname(sourceFile), decodeURIComponent(rawPath))
      : sourceFile;
    const location = `${path.relative(root, sourceFile)}:${lineNumber(content, match.index)}`;

    if (!fs.existsSync(targetFile)) {
      failures.push(`${location} -> missing target: ${target}`);
      continue;
    }

    if (rawAnchor) {
      const anchors = anchorsFor(targetFile);
      const anchor = slugify(decodeURIComponent(rawAnchor));
      if (!anchors.has(anchor)) {
        failures.push(`${location} -> missing anchor: ${target}`);
      }
    }
  }
}

if (failures.length > 0) {
  console.error("Markdown verification failed:");
  for (const failure of failures) {
    console.error(`- ${failure}`);
  }
  process.exit(1);
}

console.log(`Markdown verification passed for ${markdownFiles.length} files.`);
