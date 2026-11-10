// Compares the JavaScript version with the output of the Java version.
// Run with: node --test
const test = require("node:test");
const assert = require("node:assert");
const fs = require("fs");
const path = require("path");
const { run } = require("../docs/tagalog.js");

const expectedDir = path.join(__dirname, "expected");

function result(source) {
  const r = run(source);
  const lines = [...r.output];
  if (r.error) lines.push("Error: " + r.error);
  return lines.join("\n");
}

for (const file of fs.readdirSync(expectedDir).filter((f) => f.endsWith(".txt"))) {
  const name = file.replace(".txt", "");
  test(`gives the same output as the Java version for ${name}.tgl`, () => {
    const source = fs.readFileSync(path.join(__dirname, "..", "examples", name + ".tgl"), "utf8");
    const expected = fs.readFileSync(path.join(expectedDir, file), "utf8").replace(/\r\n/g, "\n").trimEnd();
    assert.strictEqual(result(source), expected);
  });
}

test("stops an endless loop instead of freezing the page", () => {
  const r = run("habang totoo\ntapos\n");
  assert.match(r.error, /masyadong mahaba/);
});

test("stops endless output", () => {
  const r = run("habang totoo\n    sabihin 1\ntapos\n");
  assert.match(r.error, /masyadong mahaba ang output/);
});

test("variable names such as constructor are safe", () => {
  assert.strictEqual(result("itakda constructor = 5\nsabihin constructor"), "5");
});

test("shows decimals and text joins", () => {
  assert.strictEqual(result('sabihin 7 / 2\nsabihin "a" + 1'), "3.5\na1");
});
