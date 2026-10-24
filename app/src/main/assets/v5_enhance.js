/* V5 Enhancer v2 — 零侵入式增强层（默认不改变宿主任何样式） */
(function () {
  'use strict';
  if (window.__V5E__) return;
  window.__V5E__ = true;
  var V = '5.0.0';
  var LS = window.localStorage || {};
  var LS_THEME = 'v5e_theme', LS_FONT = 'v5e_font', LS_TAB = 'v5e_tab';

  function mk(tag, cls, txt) {
    var e = document.createElement(tag);
    if (cls) e.className = cls;
    if (txt != null) e.textContent = txt;
    return e;
  }
  function store(k, v) { try { LS.setItem(k, v); } catch (e) {} }
  function load(k, d) { try { var v = LS.getItem(k); return v == null ? d : v; } catch (e) { return d; } }

  /* ================= 设计系统：只作用于自身 UI ================= */
  var CSS = ''
    + '.v5e-fab{position:fixed;right:16px;bottom:calc(18px + env(safe-area-inset-bottom,0px));width:46px;height:46px;border-radius:16px;'
    + 'display:flex;align-items:center;justify-content:center;font:600 13px/1 -apple-system,system-ui,"PingFang SC",sans-serif;'
    + 'color:#fff;background:linear-gradient(135deg,#5b8cff,#8b5cf6);box-shadow:0 8px 24px rgba(60,90,220,.32),0 2px 6px rgba(0,0,0,.18);'
    + 'z-index:2147483000;cursor:pointer;user-select:none;-webkit-tap-highlight-color:transparent;transition:transform .18s ease,box-shadow .18s ease,opacity .2s}'
    + '.v5e-fab:active{transform:scale(.92)}.v5e-fab.v5e-hidden{opacity:0;pointer-events:none;transform:scale(.6)}'
    + '.v5e-mask{position:fixed;inset:0;background:rgba(8,10,16,.42);backdrop-filter:blur(2px);-webkit-backdrop-filter:blur(2px);'
    + 'z-index:2147483001;opacity:0;transition:opacity .22s ease}'
    + '.v5e-mask.v5e-in{opacity:1}'
    + '.v5e-panel{position:fixed;left:10px;right:10px;bottom:calc(10px + env(safe-area-inset-bottom,0px));z-index:2147483002;'
    + 'max-height:78vh;display:flex;flex-direction:column;border-radius:22px;overflow:hidden;'
    + 'background:rgba(22,24,30,.92);backdrop-filter:blur(22px) saturate(160%);-webkit-backdrop-filter:blur(22px) saturate(160%);'
    + 'border:1px solid rgba(255,255,255,.10);box-shadow:0 -8px 40px rgba(0,0,0,.42),0 0 0 .5px rgba(255,255,255,.05) inset;'
    + 'color:#eef1f7;font:14px/1.5 -apple-system,system-ui,"PingFang SC",sans-serif;'
    + 'transform:translateY(112%);transition:transform .30s cubic-bezier(.22,1,.36,1)}'
    + '.v5e-panel.v5e-in{transform:translateY(0)}'
    + '.v5e-hd{display:flex;align-items:center;gap:8px;padding:14px 16px 10px}'
    + '.v5e-title{font-size:15px;font-weight:650;letter-spacing:.2px}'
    + '.v5e-badge{font-size:10px;padding:2px 7px;border-radius:999px;background:linear-gradient(135deg,#5b8cff,#8b5cf6);color:#fff;font-weight:600}'
    + '.v5e-x{margin-left:auto;width:30px;height:30px;border-radius:10px;display:flex;align-items:center;justify-content:center;'
    + 'background:rgba(255,255,255,.08);color:#dfe4ee;cursor:pointer;font-size:15px}'
    + '.v5e-x:active{background:rgba(255,255,255,.16)}'
    + '.v5e-tabs{display:flex;gap:6px;padding:0 12px 10px}'
    + '.v5e-tab{flex:1;text-align:center;padding:7px 0;border-radius:11px;font-size:12.5px;color:#aab2c2;'
    + 'background:rgba(255,255,255,.05);cursor:pointer;transition:all .18s}'
    + '.v5e-tab.v5e-on{color:#fff;background:linear-gradient(135deg,rgba(91,140,255,.92),rgba(139,92,246,.92));box-shadow:0 4px 14px rgba(91,140,255,.28)}'
    + '.v5e-body{padding:2px 14px 16px;overflow-y:auto;-webkit-overflow-scrolling:touch}'
    + '.v5e-sec{padding:12px 0 4px}'
    + '.v5e-lab{font-size:11.5px;color:#8d95a6;letter-spacing:.4px;margin-bottom:8px;text-transform:uppercase}'
    + '.v5e-seg{display:flex;gap:6px}'
    + '.v5e-seg>div{flex:1;text-align:center;padding:9px 0;border-radius:12px;font-size:12.5px;color:#cfd6e4;'
    + 'background:rgba(255,255,255,.06);border:1px solid transparent;cursor:pointer;transition:all .16s}'
    + '.v5e-seg>div.v5e-on{color:#fff;border-color:rgba(122,158,255,.65);background:rgba(91,140,255,.20)}'
    + '.v5e-range{width:100%;-webkit-appearance:none;height:4px;border-radius:4px;background:rgba(255,255,255,.14);outline:none}'
    + '.v5e-range::-webkit-slider-thumb{-webkit-appearance:none;width:20px;height:20px;border-radius:50%;'
    + 'background:linear-gradient(135deg,#5b8cff,#8b5cf6);box-shadow:0 2px 8px rgba(0,0,0,.4);cursor:pointer}'
    + '.v5e-grid{display:grid;grid-template-columns:repeat(2,1fr);gap:8px}'
    + '.v5e-card{padding:11px 12px;border-radius:14px;background:rgba(255,255,255,.06);border:1px solid rgba(255,255,255,.07);'
    + 'cursor:pointer;transition:transform .14s,background .18s}'
    + '.v5e-card:active{transform:scale(.97);background:rgba(255,255,255,.11)}'
    + '.v5e-card b{display:block;font-size:13px;font-weight:600;color:#fff;margin-bottom:2px}'
    + '.v5e-card span{font-size:11px;color:#98a1b3}'
    + '.v5e-row{display:flex;align-items:center;gap:10px;padding:11px 12px;border-radius:14px;background:rgba(255,255,255,.06);'
    + 'border:1px solid rgba(255,255,255,.07)}'
    + '.v5e-input{flex:1;min-width:0;background:transparent;border:0;outline:0;color:#eef1f7;'
    + 'font:14px/1.4 -apple-system,system-ui,"PingFang SC",sans-serif}'
    + '.v5e-input::placeholder{color:#78808f}'
    + '.v5e-mini{flex-shrink:0;padding:6px 11px;border-radius:9px;font-size:12px;color:#fff;cursor:pointer;'
    + 'background:linear-gradient(135deg,rgba(91,140,255,.95),rgba(139,92,246,.95))}'
    + '.v5e-mini.v5e-ghost{background:rgba(255,255,255,.10);color:#cfd6e4}'
    + '.v5e-hint{font-size:11.5px;color:#8d95a6;margin-top:8px;line-height:1.6}'
    + '.v5e-toast{position:fixed;left:50%;bottom:calc(88px + env(safe-area-inset-bottom,0px));transform:translate(-50%,12px);'
    + 'z-index:2147483003;padding:9px 16px;border-radius:12px;background:rgba(24,26,32,.94);color:#fff;font:13px/1.4 -apple-system,system-ui,sans-serif;'
    + 'box-shadow:0 6px 24px rgba(0,0,0,.35);opacity:0;transition:opacity .2s,transform .2s;pointer-events:none;white-space:nowrap;max-width:86vw;overflow:hidden;text-overflow:ellipsis}'
    + '.v5e-toast.v5e-in{opacity:1;transform:translate(-50%,0)}'
    + 'mark.v5e-hit{background:rgba(255,213,79,.85)!important;color:#1b1b1f!important;border-radius:3px;padding:0 1px}'
    + '@media (min-width:720px){.v5e-panel{left:auto;right:18px;width:390px}}';

  function boot() {
    var st = mk('style');
    st.id = 'v5e-style';
    st.textContent = CSS;
    document.head.appendChild(st);

    /* ---------- 主题：auto / light / dark / eye（auto=完全不干扰宿主） ---------- */
    var themeCss = mk('style');
    themeCss.id = 'v5e-theme';
    document.head.appendChild(themeCss);
    var THEMES = {
      auto: null,
      light: 'html{background:#fff!important}body{background:#fff!important;color:#1b1b1f!important}',
      dark: 'html{background:#14161a!important}body{background:#14161a!important;color:#e9ecf2!important}',
      eye: 'html{background:#c9edd0!important}body{background:#c9edd0!important;color:#1f3327!important}'
    };
    function applyTheme(name) {
      if (!(name in THEMES)) name = 'auto';
      themeCss.textContent = THEMES[name] || '';
      document.documentElement.setAttribute('data-v5e-theme', name);
      store(LS_THEME, name);
      return name;
    }

    /* ---------- 字号：仅 html 根字号，不使用 !important ---------- */
    var fontSt = mk('style');
    fontSt.id = 'v5e-font';
    document.head.appendChild(fontSt);
    function applyFont(pct) {
      pct = Math.min(160, Math.max(80, Math.round(pct)));
      fontSt.textContent = 'html{font-size:' + pct + '%}';
      store(LS_FONT, String(pct));
      return pct;
    }

    /* ---------- 对话采集 / 导出 ---------- */
    function nodes() {
      var sels = ['.message', '.msg', '.chat-item', '.bubble', '[class*="message-"]', '[class*="chat-item"]'];
      var out = [];
      for (var i = 0; i < sels.length; i++) {
        var n = document.querySelectorAll(sels[i]);
        if (n.length > out.length) out = [].slice.call(n);
      }
      return out;
    }
    function texts() {
      var ns = nodes(), arr = [];
      for (var i = 0; i < ns.length; i++) {
        var t = (ns[i].innerText || '').trim();
        if (t) arr.push({ role: /user|我|right/i.test(ns[i].className) ? 'user' : 'ai', text: t });
      }
      if (!arr.length) {
        var main = document.querySelector('main,[class*="content"],[class*="chat"]') || document.body;
        var all = (main.innerText || '').trim();
        if (all) arr.push({ role: 'doc', text: all });
      }
      return arr;
    }
    function download(name, content, mime) {
      try {
        var blob = new Blob([content], { type: mime + ';charset=utf-8' });
        var a = mk('a');
        a.href = URL.createObjectURL(blob);
        a.download = name;
        a.style.display = 'none';
        document.body.appendChild(a);
        a.click();
        setTimeout(function () { URL.revokeObjectURL(a.href); a.remove(); }, 1200);
        toast('已导出 ' + name);
      } catch (e) { toast('导出失败：' + e.message); }
    }
    function stamp() {
      var d = new Date(), p = function (n) { return (n < 10 ? '0' : '') + n; };
      return d.getFullYear() + p(d.getMonth() + 1) + p(d.getDate()) + '-' + p(d.getHours()) + p(d.getMinutes());
    }

    /* ---------- 全文搜索 ---------- */
    var hits = [], hitIdx = -1, lastQ = '';
    function clearHits() {
      var m = document.querySelectorAll('mark.v5e-hit');
      for (var i = 0; i < m.length; i++) {
        var p = m[i].parentNode;
        if (!p) continue;
        p.replaceChild(document.createTextNode(m[i].textContent), m[i]);
        p.normalize();
      }
      hits = []; hitIdx = -1;
    }
    function doSearch(q) {
      clearHits();
      lastQ = q;
      if (!q) return 0;
      var root = document.body, tw = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, {
        acceptNode: function (n) {
          if (!n.nodeValue || !n.nodeValue.trim()) return NodeFilter.FILTER_REJECT;
          var p = n.parentNode;
          if (!p) return NodeFilter.FILTER_REJECT;
          var tag = p.nodeName;
          if (tag === 'SCRIPT' || tag === 'STYLE' || tag === 'TEXTAREA' || tag === 'INPUT') return NodeFilter.FILTER_REJECT;
          if (p.closest && p.closest('.v5e-panel,.v5e-fab,.v5e-toast')) return NodeFilter.FILTER_REJECT;
          return NodeFilter.FILTER_ACCEPT;
        }
      });
      var list = [], n;
      while ((n = tw.nextNode()) && list.length < 300) list.push(n);
      for (var i = 0; i < list.length && hits.length < 300; i++) {
        var tx = list[i].nodeValue, low = tx.toLowerCase(), ql = q.toLowerCase(), from = 0, at;
        var frag = null;
        while ((at = low.indexOf(ql, from)) >= 0) {
          if (!frag) frag = document.createDocumentFragment();
          frag.appendChild(document.createTextNode(tx.slice(from, at)));
          var mkEl = mk('mark', 'v5e-hit', tx.substr(at, q.length));
          frag.appendChild(mkEl);
          hits.push(mkEl);
          from = at + q.length;
        }
        if (frag) {
          frag.appendChild(document.createTextNode(tx.slice(from)));
          list[i].parentNode.replaceChild(frag, list[i]);
        }
      }
      return hits.length;
    }
    function jump(step) {
      if (!hits.length) return;
      hitIdx = (hitIdx + step + hits.length) % hits.length;
      var h = hits[hitIdx];
      if (h.scrollIntoView) h.scrollIntoView({ block: 'center', behavior: 'smooth' });
      var old = document.querySelectorAll('mark.v5e-hit.v5e-cur');
      for (var i = 0; i < old.length; i++) old[i].classList.remove('v5e-cur');
      h.classList.add('v5e-cur');
      h.style.outline = '2px solid #5b8cff';
      return hitIdx + 1;
    }

    /* ---------- 快捷指令 ---------- */
    var CMDS = [
      { t: '总结要点', d: '提炼上文核心', p: '请把上面的内容总结成要点，分条列出，不超过 5 条。' },
      { t: '润色文字', d: '让表达更自然', p: '请润色下面这段文字，保持原意，让它更自然通顺：' },
      { t: '翻译成英文', d: '中英互译', p: '请把下面内容翻译成地道的英文：' },
      { t: '深入分析', d: '多角度拆解', p: '请从原理、利弊、适用场景三个角度深入分析：' },
      { t: '写代码', d: '附注释', p: '请用简洁可靠的实现给出代码，并加必要注释：' },
      { t: '解释概念', d: '讲给新手听', p: '请用通俗易懂的方式解释这个概念，并举一个生活化的例子：' }
    ];
    function insertText(s) {
      var cands = [document.activeElement, document.querySelector('textarea'), document.querySelector('[contenteditable="true"]')];
      for (var i = 0; i < cands.length; i++) {
        var t = cands[i];
        if (!t) continue;
        if (t.tagName === 'TEXTAREA' || t.tagName === 'INPUT') {
          try {
            var set = Object.getOwnPropertyDescriptor(t.__proto__, 'value').set;
            set.call(t, s);
            t.dispatchEvent(new Event('input', { bubbles: true }));
            t.focus();
            return true;
          } catch (e) {
            t.value = s; t.dispatchEvent(new Event('input', { bubbles: true })); t.focus(); return true;
          }
        }
        if (t.isContentEditable) {
          t.focus();
          try { document.execCommand('insertText', false, s); } catch (e) { t.textContent += s; }
          t.dispatchEvent(new Event('input', { bubbles: true }));
          return true;
        }
      }
      return false;
    }

    /* ---------- Toast ---------- */
    var toastEl = null, toastTimer = null;
    function toast(msg) {
      if (!toastEl) { toastEl = mk('div', 'v5e-toast'); document.body.appendChild(toastEl); }
      toastEl.textContent = msg;
      requestAnimationFrame(function () { toastEl.classList.add('v5e-in'); });
      clearTimeout(toastTimer);
      toastTimer = setTimeout(function () { toastEl.classList.remove('v5e-in'); }, 1900);
    }

    /* ---------- UI ---------- */
    var curTheme = applyTheme(load(LS_THEME, 'auto'));
    var curFont = applyFont(parseInt(load(LS_FONT, '100'), 10) || 100);

    var fab = mk('div', 'v5e-fab', 'V5');
    fab.setAttribute('role', 'button');
    fab.setAttribute('aria-label', 'V5 增强面板');
    document.body.appendChild(fab);

    var mask = mk('div', 'v5e-mask');
    var panel = mk('div', 'v5e-panel');
    var hd = mk('div', 'v5e-hd');
    hd.appendChild(mk('div', 'v5e-title', '增强面板'));
    hd.appendChild(mk('div', 'v5e-badge', 'v' + V));
    var xBtn = mk('div', 'v5e-x', '✕');
    hd.appendChild(xBtn);
    var tabs = mk('div', 'v5e-tabs');
    var body = mk('div', 'v5e-body');
    panel.appendChild(hd); panel.appendChild(tabs); panel.appendChild(body);

    var TAB_DEFS = [['look', '外观'], ['tool', '工具'], ['cmd', '指令']];
    var tabEls = {}, curTab = load(LS_TAB, 'look');

    function seg(items, onPick, current) {
      var wrap = mk('div', 'v5e-seg');
      items.forEach(function (it) {
        var d = mk('div', it[2] === current ? 'v5e-on' : '', it[1]);
        d.onclick = function () {
          [].forEach.call(wrap.children, function (c) { c.classList.remove('v5e-on'); });
          d.classList.add('v5e-on');
          onPick(it[2]);
        };
        wrap.appendChild(d);
      });
      return wrap;
    }
    function lab(t) { return mk('div', 'v5e-lab', t); }

    function render() {
      body.innerHTML = '';
      for (var k in tabEls) tabEls[k].classList.toggle('v5e-on', k === curTab);
      if (curTab === 'look') {
        body.appendChild(lab('主题'));
        body.appendChild(seg([['auto', '跟随应用', 'auto'], ['light', '浅色', 'light'], ['dark', '深色', 'dark'], ['eye', '护眼', 'eye']], function (v) {
          curTheme = applyTheme(v); toast('主题：' + v);
        }, curTheme));
        body.appendChild(mk('div', 'v5e-hint', '默认「跟随应用」不会改动原界面配色。'));
        var s = mk('div', 'v5e-sec');
        s.appendChild(lab('字号  ' + curFont + '%'));
        var r = mk('input', 'v5e-range');
        r.type = 'range'; r.min = '80'; r.max = '160'; r.step = '5'; r.value = String(curFont);
        r.oninput = function () { curFont = applyFont(parseInt(r.value, 10)); r.previousSibling.textContent = '字号  ' + curFont + '%'; };
        s.appendChild(r);
        body.appendChild(s);
        var g = mk('div', 'v5e-grid');
        [['回到顶部', '滚到最上面', function () { window.scrollTo({ top: 0, behavior: 'smooth' }); }],
         ['回到底部', '滚到最新消息', function () { window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' }); }],
         ['字数统计', '统计当前对话', function () {
           var a = texts(), n = a.reduce(function (x, y) { return x + y.text.length; }, 0);
           toast(a.length + ' 段 / ' + n + ' 字');
         }],
         ['刷新界面', '重新加载页面', function () { location.reload(); }]].forEach(function (it) {
          var c = mk('div', 'v5e-card');
          c.appendChild(mk('b', '', it[0]));
          c.appendChild(mk('span', '', it[1]));
          c.onclick = it[2];
          g.appendChild(c);
        });
        body.appendChild(g);
      } else if (curTab === 'tool') {
        body.appendChild(lab('导出对话'));
        var g2 = mk('div', 'v5e-grid');
        [['导出 Markdown', '.md', function () {
          var a = texts();
          if (!a.length) return toast('没有找到可导出的内容');
          var md = '# 对话记录 ' + stamp() + '\n\n' + a.map(function (m) {
            return '**' + (m.role === 'user' ? '我' : 'AI') + '**\n\n' + m.text;
          }).join('\n\n---\n\n');
          download('chat-' + stamp() + '.md', md, 'text/markdown');
        }],
         ['导出纯文本', '.txt', function () {
           var a = texts();
           if (!a.length) return toast('没有找到可导出的内容');
           download('chat-' + stamp() + '.txt', a.map(function (m) {
             return (m.role === 'user' ? '我: ' : 'AI: ') + m.text;
           }).join('\n\n'), 'text/plain');
         }],
         ['导出 JSON', '.json', function () {
           var a = texts();
           if (!a.length) return toast('没有找到可导出的内容');
           download('chat-' + stamp() + '.json', JSON.stringify({ app: document.title, version: V, time: new Date().toISOString(), messages: a }, null, 2), 'application/json');
         }],
         ['复制全文', '到剪贴板', function () {
           var a = texts(), t = a.map(function (m) { return m.text; }).join('\n\n');
           if (!t) return toast('没有可复制的内容');
           if (navigator.clipboard) navigator.clipboard.writeText(t).then(function () { toast('已复制'); }, function () { toast('复制失败'); });
           else toast('当前环境不支持复制');
         }]].forEach(function (it) {
          var c = mk('div', 'v5e-card');
          c.appendChild(mk('b', '', it[0]));
          c.appendChild(mk('span', '', it[1]));
          c.onclick = it[2];
          g2.appendChild(c);
        });
        body.appendChild(g2);

        body.appendChild(lab('全文搜索'));
        var row = mk('div', 'v5e-row');
        var inp = mk('input', 'v5e-input');
        inp.placeholder = '输入关键词后回车';
        inp.value = lastQ;
        var cnt = mk('span', 'v5e-hint', '');
        cnt.style.margin = '0';
        var prev = mk('div', 'v5e-mini v5e-ghost', '↑');
        var next = mk('div', 'v5e-mini', '↓');
        var go = function () {
          var n = doSearch(inp.value.trim());
          cnt.textContent = n ? ('1/' + n) : '无结果';
          if (n) jump(0);
        };
        inp.onkeydown = function (e) { if (e.key === 'Enter') go(); };
        prev.onclick = function () { var p = jump(-1); if (p) cnt.textContent = p + '/' + hits.length; };
        next.onclick = function () { var p = jump(1); if (p) cnt.textContent = p + '/' + hits.length; };
        row.appendChild(inp); row.appendChild(prev); row.appendChild(next);
        body.appendChild(row);
        body.appendChild(cnt);

        body.appendChild(lab('快捷操作'));
        var row2 = mk('div', 'v5e-row');
        row2.appendChild(mk('div', 'v5e-input', '清理搜索高亮'));
        var clr = mk('div', 'v5e-mini v5e-ghost', '清除');
        clr.onclick = function () { clearHits(); toast('已清除'); render(); };
        row2.appendChild(clr);
        body.appendChild(row2);
      } else {
        body.appendChild(lab('快捷指令（点击写入输入框）'));
        var g3 = mk('div', 'v5e-grid');
        CMDS.forEach(function (c) {
          var d = mk('div', 'v5e-card');
          d.appendChild(mk('b', '', c.t));
          d.appendChild(mk('span', '', c.d));
          d.onclick = function () {
            if (insertText(c.p)) { toast('已填入：' + c.t); close(); }
            else toast('未找到输入框，请先点一下输入框');
          };
          g3.appendChild(d);
        });
        body.appendChild(g3);
      }
    }

    TAB_DEFS.forEach(function (t) {
      var b = mk('div', 'v5e-tab', t[1]);
      b.onclick = function () { curTab = t[0]; store(LS_TAB, curTab); render(); };
      tabEls[t[0]] = b;
      tabs.appendChild(b);
    });

    var opened = false;
    function open() {
      if (!panel.parentNode) { document.body.appendChild(mask); document.body.appendChild(panel); }
      render();
      opened = true;
      mask.style.display = 'block';
      requestAnimationFrame(function () { mask.classList.add('v5e-in'); panel.classList.add('v5e-in'); });
      fab.classList.add('v5e-hidden');
    }
    function close() {
      opened = false;
      mask.classList.remove('v5e-in');
      panel.classList.remove('v5e-in');
      setTimeout(function () { if (!opened) { mask.style.display = 'none'; } }, 300);
      fab.classList.remove('v5e-hidden');
    }
    fab.onclick = open;
    xBtn.onclick = close;
    mask.onclick = close;
    document.addEventListener('keydown', function (e) { if (e.key === 'Escape' && opened) close(); });
    render();
    window.__V5E_API__ = { open: open, close: close, theme: applyTheme, font: applyFont, export: texts };
  }

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', boot);
  else boot();
})();