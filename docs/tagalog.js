/* Tagalog Script in JavaScript: lexer, parser, and interpreter.
   This is a port of the Java version. It runs in the browser and in Node. */
(function () {
  "use strict";

  const MAX_STEPS = 1000000;
  const MAX_LINES = 5000;

  class TagalogError extends Error {}
  class ReturnSignal { constructor(value) { this.value = value; } }
  class Fn { constructor(decl, closure) { this.decl = decl; this.closure = closure; } }

  class Env {
    constructor(parent) { this.vars = new Map(); this.parent = parent; }
    define(name, value) { this.vars.set(name, value); }
    has(name) { return this.vars.has(name) || (this.parent !== null && this.parent.has(name)); }
    get(name) { return this.vars.has(name) ? this.vars.get(name) : this.parent.get(name); }
  }

  // Lexer

  const KEYWORDS = {
    itakda: "ITAKDA", sabihin: "SABIHIN", kung: "KUNG", kundi: "KUNDI", habang: "HABANG",
    tapos: "TAPOS", totoo: "TOTOO", mali: "MALI", at: "AT", o: "O", hindi: "HINDI",
    gawin: "GAWIN", ibalik: "IBALIK",
  };
  const isDigit = (c) => c >= "0" && c <= "9";
  const isAlpha = (c) => /[A-Za-z_]/.test(c);

  function lex(src) {
    const tokens = [];
    let i = 0, line = 1;
    const push = (type, lexeme) => tokens.push({ type, lexeme, line });
    const fail = (message) => new TagalogError(`Linya ${line}: ${message}`);
    const two = (next, yes, no, text) => {
      if (src[i] === next) { i++; push(yes, text + next); } else push(no, text);
    };

    while (i < src.length) {
      const c = src[i++];
      switch (c) {
        case "(": push("LEFT_PAREN", c); break;
        case ")": push("RIGHT_PAREN", c); break;
        case ",": push("COMMA", c); break;
        case "+": push("PLUS", c); break;
        case "-": push("MINUS", c); break;
        case "*": push("STAR", c); break;
        case "/": push("SLASH", c); break;
        case "=": two("=", "EQUAL_EQUAL", "EQUAL", c); break;
        case "<": two("=", "LESS_EQUAL", "LESS", c); break;
        case ">": two("=", "GREATER_EQUAL", "GREATER", c); break;
        case "!":
          if (src[i] === "=") { i++; push("BANG_EQUAL", "!="); }
          else throw fail("hindi kilalang karakter '!'");
          break;
        case "#": while (i < src.length && src[i] !== "\n") i++; break;
        case " ": case "\r": case "\t": break;
        case "\n": push("NEWLINE", "\\n"); line++; break;
        case '"': {
          const start = i;
          while (i < src.length && src[i] !== '"') { if (src[i] === "\n") line++; i++; }
          if (i >= src.length) throw fail("hindi natapos ang string");
          push("STRING", src.slice(start, i));
          i++;
          break;
        }
        default:
          if (isDigit(c)) {
            const start = i - 1;
            while (isDigit(src[i] || "")) i++;
            if (src[i] === "." && isDigit(src[i + 1] || "")) { i++; while (isDigit(src[i] || "")) i++; }
            push("NUMBER", src.slice(start, i));
          } else if (isAlpha(c)) {
            const start = i - 1;
            while (i < src.length && /[A-Za-z0-9_]/.test(src[i])) i++;
            const word = src.slice(start, i);
            push(Object.hasOwn(KEYWORDS, word) ? KEYWORDS[word] : "IDENTIFIER", word);
          } else {
            throw fail(`hindi kilalang karakter '${c}'`);
          }
      }
    }
    push("EOF", "");
    return tokens;
  }

  // Parser

  function parse(tokens) {
    let cur = 0, fnDepth = 0;
    const peek = () => tokens[cur];
    const prev = () => tokens[cur - 1];
    const atEnd = () => peek().type === "EOF";
    const check = (type) => peek().type === type;
    const advance = () => { if (!atEnd()) cur++; return prev(); };
    const match = (...types) => {
      for (const type of types) if (check(type)) { advance(); return true; }
      return false;
    };
    const describe = (t) => t.type === "EOF" ? "dulo ng file" : t.type === "NEWLINE" ? "dulo ng linya" : `'${t.lexeme}'`;
    const error = (t, message) => new TagalogError(`Linya ${t.line}: ${message} (nakita: ${describe(t)})`);
    const consume = (type, message) => { if (check(type)) return advance(); throw error(peek(), message); };
    const skipNewlines = () => { while (match("NEWLINE")); };
    const endStatement = () => {
      if (match("NEWLINE") || atEnd()) return;
      throw error(peek(), "inaasahan ang bagong linya pagkatapos ng pahayag");
    };

    function program() {
      const list = [];
      skipNewlines();
      while (!atEnd()) { list.push(statement()); skipNewlines(); }
      return list;
    }

    function block() {
      const list = [];
      skipNewlines();
      while (!check("TAPOS") && !check("KUNDI") && !atEnd()) { list.push(statement()); skipNewlines(); }
      return list;
    }

    function statement() {
      if (match("ITAKDA")) {
        const name = consume("IDENTIFIER", "inaasahan ang pangalan ng variable pagkatapos ng 'itakda'");
        consume("EQUAL", "inaasahan ang '=' pagkatapos ng pangalan ng variable");
        const value = expression();
        endStatement();
        return { t: "Assign", name, value };
      }
      if (match("SABIHIN")) {
        const value = expression();
        endStatement();
        return { t: "Print", value };
      }
      if (match("KUNG")) {
        const cond = expression();
        consume("NEWLINE", "inaasahan ang bagong linya pagkatapos ng kondisyon");
        const then = block();
        let otherwise = [];
        if (match("KUNDI")) {
          consume("NEWLINE", "inaasahan ang bagong linya pagkatapos ng 'kundi'");
          otherwise = block();
        }
        consume("TAPOS", "inaasahan ang 'tapos' sa dulo ng 'kung'");
        endStatement();
        return { t: "If", cond, then, otherwise };
      }
      if (match("HABANG")) {
        const cond = expression();
        consume("NEWLINE", "inaasahan ang bagong linya pagkatapos ng kondisyon");
        const body = block();
        consume("TAPOS", "inaasahan ang 'tapos' sa dulo ng 'habang'");
        endStatement();
        return { t: "While", cond, body };
      }
      if (match("GAWIN")) {
        const name = consume("IDENTIFIER", "inaasahan ang pangalan ng function pagkatapos ng 'gawin'");
        consume("LEFT_PAREN", "inaasahan ang '(' pagkatapos ng pangalan ng function");
        const params = [];
        if (!check("RIGHT_PAREN")) {
          do { params.push(consume("IDENTIFIER", "inaasahan ang pangalan ng parameter")); } while (match("COMMA"));
        }
        consume("RIGHT_PAREN", "inaasahan ang ')' pagkatapos ng mga parameter");
        consume("NEWLINE", "inaasahan ang bagong linya pagkatapos ng 'gawin'");
        fnDepth++;
        const body = block();
        fnDepth--;
        consume("TAPOS", "inaasahan ang 'tapos' sa dulo ng 'gawin'");
        endStatement();
        return { t: "Function", name, params, body };
      }
      if (match("IBALIK")) {
        const keyword = prev();
        if (fnDepth === 0) throw error(keyword, "ang 'ibalik' ay para sa loob ng function lamang");
        let value = null;
        if (!check("NEWLINE") && !atEnd()) value = expression();
        endStatement();
        return { t: "Return", keyword, value };
      }
      if (check("IDENTIFIER")) {
        const start = peek();
        const e = expression();
        if (check("EQUAL")) throw error(start, "gamitin ang 'itakda' para magtakda ng variable");
        if (e.t !== "Call") throw error(start, "inaasahan ang pahayag o ang pagtawag ng function");
        endStatement();
        return { t: "Expr", e };
      }
      throw error(peek(), "inaasahan ang 'itakda', 'sabihin', 'kung', 'habang', 'gawin', o 'ibalik'");
    }

    function expression() { return or(); }

    const binary = (next, ...types) => () => {
      let left = next();
      while (match(...types)) {
        const op = prev();
        left = { t: "Binary", left, op, right: next() };
      }
      return left;
    };

    function primary() {
      if (match("NUMBER")) return { t: "Literal", value: parseFloat(prev().lexeme) };
      if (match("STRING")) return { t: "Literal", value: prev().lexeme };
      if (match("TOTOO")) return { t: "Literal", value: true };
      if (match("MALI")) return { t: "Literal", value: false };
      if (match("IDENTIFIER")) return { t: "Variable", name: prev() };
      if (match("LEFT_PAREN")) {
        const e = expression();
        consume("RIGHT_PAREN", "inaasahan ang ')' pagkatapos ng expression");
        return { t: "Grouping", e };
      }
      throw error(peek(), "inaasahan ang expression");
    }

    function call() {
      let e = primary();
      while (match("LEFT_PAREN")) {
        const args = [];
        if (!check("RIGHT_PAREN")) { do { args.push(expression()); } while (match("COMMA")); }
        const paren = consume("RIGHT_PAREN", "inaasahan ang ')' pagkatapos ng mga argumento");
        e = { t: "Call", callee: e, paren, args };
      }
      return e;
    }

    function unary() {
      if (match("HINDI", "MINUS")) { const op = prev(); return { t: "Unary", op, right: unary() }; }
      return call();
    }

    const factor = binary(unary, "STAR", "SLASH");
    const term = binary(factor, "PLUS", "MINUS");
    const comparison = binary(term, "LESS", "LESS_EQUAL", "GREATER", "GREATER_EQUAL");
    const equality = binary(comparison, "EQUAL_EQUAL", "BANG_EQUAL");
    const and = binary(equality, "AT");
    const or = binary(and, "O");

    return program();
  }

  // Interpreter

  function stringify(v) {
    if (v === null) return "wala";
    if (typeof v === "boolean") return v ? "totoo" : "mali";
    if (v instanceof Fn) return `<function ${v.decl.name.lexeme}>`;
    return String(v);
  }

  function interpret(program, out) {
    let steps = 0;
    const globals = new Env(null);
    let env = globals;

    const fail = (line, message) => new TagalogError(line > 0 ? `Linya ${line}: ${message}` : message);
    const tick = () => {
      if (++steps > MAX_STEPS) {
        throw new TagalogError("masyadong mahaba ang takbo ng programa (higit sa 1,000,000 hakbang)");
      }
    };
    const lineOf = (e) => {
      switch (e.t) {
        case "Variable": return e.name.line;
        case "Unary": case "Binary": return e.op.line;
        case "Call": return e.paren.line;
        case "Grouping": return lineOf(e.e);
        default: return -1;
      }
    };
    const isTrue = (v, line) => {
      if (typeof v === "boolean") return v;
      throw fail(line, "ang kondisyon ay dapat totoo o mali");
    };
    const needNumbers = (op, l, r) => {
      if (typeof l !== "number" || typeof r !== "number") {
        throw fail(op.line, `ang '${op.lexeme}' ay para sa numero lamang`);
      }
    };

    function evaluate(e) {
      switch (e.t) {
        case "Literal": return e.value;
        case "Grouping": return evaluate(e.e);
        case "Variable": {
          const name = e.name.lexeme;
          if (!env.has(name)) throw fail(e.name.line, `walang variable na '${name}'`);
          return env.get(name);
        }
        case "Unary": {
          const right = evaluate(e.right);
          if (e.op.type === "MINUS") {
            if (typeof right !== "number") throw fail(e.op.line, "ang '-' ay para sa numero lamang");
            return -right;
          }
          if (typeof right !== "boolean") throw fail(e.op.line, "ang 'hindi' ay para sa totoo o mali lamang");
          return !right;
        }
        case "Binary": return binaryOp(e);
        case "Call": {
          const callee = evaluate(e.callee);
          const args = e.args.map(evaluate);
          if (!(callee instanceof Fn)) throw fail(e.paren.line, "hindi function ang tinawag");
          const expected = callee.decl.params.length;
          if (args.length !== expected) {
            throw fail(e.paren.line, `inaasahan ang ${expected} argumento pero ${args.length} ang ibinigay`);
          }
          return callFunction(callee, args);
        }
        default: throw new Error("Hindi kilalang expression");
      }
    }

    function binaryOp(e) {
      const op = e.op;
      if (op.type === "AT" || op.type === "O") {
        const left = evaluate(e.left);
        if (typeof left !== "boolean") throw fail(op.line, `ang '${op.lexeme}' ay para sa totoo o mali lamang`);
        if (op.type === "O" && left) return true;
        if (op.type === "AT" && !left) return false;
        const right = evaluate(e.right);
        if (typeof right !== "boolean") throw fail(op.line, `ang '${op.lexeme}' ay para sa totoo o mali lamang`);
        return right;
      }
      const l = evaluate(e.left);
      const r = evaluate(e.right);
      switch (op.type) {
        case "PLUS":
          if (typeof l === "number" && typeof r === "number") return l + r;
          if (typeof l === "string" || typeof r === "string") return stringify(l) + stringify(r);
          throw fail(op.line, "ang '+' ay para sa numero o text lamang");
        case "MINUS": needNumbers(op, l, r); return l - r;
        case "STAR": needNumbers(op, l, r); return l * r;
        case "SLASH":
          needNumbers(op, l, r);
          if (r === 0) throw fail(op.line, "hindi maaaring hatiin sa zero");
          return l / r;
        case "LESS": needNumbers(op, l, r); return l < r;
        case "LESS_EQUAL": needNumbers(op, l, r); return l <= r;
        case "GREATER": needNumbers(op, l, r); return l > r;
        case "GREATER_EQUAL": needNumbers(op, l, r); return l >= r;
        case "EQUAL_EQUAL": return l === r;
        case "BANG_EQUAL": return l !== r;
        default: throw new Error("Hindi kilalang operator");
      }
    }

    function callFunction(fn, args) {
      tick();
      const previous = env;
      env = new Env(fn.closure);
      try {
        fn.decl.params.forEach((p, i) => env.define(p.lexeme, args[i]));
        execList(fn.decl.body);
        return null;
      } catch (signal) {
        if (signal instanceof ReturnSignal) return signal.value;
        throw signal;
      } finally {
        env = previous;
      }
    }

    function execList(list) { for (const s of list) exec(s); }

    function exec(s) {
      tick();
      switch (s.t) {
        case "Assign": env.define(s.name.lexeme, evaluate(s.value)); break;
        case "Print": {
          const text = stringify(evaluate(s.value));
          if (out.length >= MAX_LINES) throw new TagalogError("masyadong mahaba ang output");
          out.push(text);
          break;
        }
        case "If":
          if (isTrue(evaluate(s.cond), lineOf(s.cond))) execList(s.then); else execList(s.otherwise);
          break;
        case "While":
          while (isTrue(evaluate(s.cond), lineOf(s.cond))) { tick(); execList(s.body); }
          break;
        case "Function": env.define(s.name.lexeme, new Fn(s, env)); break;
        case "Return": throw new ReturnSignal(s.value === null ? null : evaluate(s.value));
        case "Expr": evaluate(s.e); break;
        default: throw new Error("Hindi kilalang pahayag");
      }
    }

    execList(program);
  }

  // Public function: returns the printed lines and an error message (or null).
  function run(source) {
    const output = [];
    try {
      interpret(parse(lex(source)), output);
      return { output, error: null };
    } catch (e) {
      if (e instanceof TagalogError) return { output, error: e.message };
      if (e instanceof RangeError) return { output, error: "masyadong malalim ang pagtawag ng function" };
      throw e;
    }
  }

  globalThis.TagalogScript = { run };
  if (typeof module !== "undefined") module.exports = { run };
})();
