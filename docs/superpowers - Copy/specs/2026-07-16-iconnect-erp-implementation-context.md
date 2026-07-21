# iConnect ERP — Implementation Context (anti-hallucination handoff)

**Date:** 2026-07-16
**Read this with:** `2026-07-16-iconnect-erp-spec-reconciliation-design.md` (the WHAT/decisions). This file is the WHERE/HOW (code map + concrete edit targets). Verify line numbers before editing — they drift as the file changes.

## Ground truth files

- **Active mockup:** `ui-mockup/iconnect-erp.html` — single self-contained file, ~1170 lines, vanilla JS, no build step, no dependencies. Open directly in a browser.
- **Older versions (do NOT edit):** `ui-mockup/iconnect-erp-20260715.html`, `ui-mockup/iconnect-erp_20260701.html`.
- **Requirements:** `docs/puregold/Puregold JDA ERP_1.pdf` (9 pages, primary spec), `docs/puregold/Puregold System.docx` (feature checklist), `docs/puregold/Puregold System Feature Mapping.xlsx` (not yet read).

## Environment gotchas (verified this session)

- **Not a git repo** — cannot commit; do not assume git.
- **GateGuard hooks are active:** before the first Bash command and before any Write/Edit, a "Fact-Forcing Gate" requires stating facts (request summary + what the command does / who calls the file + Glob check + verbatim instruction). Comply, then retry the same call.
- **PDF/DOCX tooling:** `pdftotext`/`pdftoppm`/`libreoffice` are NOT installed; the `Read` tool cannot render this PDF. `python3` with `python-docx` + `pdfplumber` IS installed (pip-installed this session). Extract via a short Python script, not the Read tool.
- Scratchpad: `/tmp/claude-1000/-mnt-d-claude-workspace-iconnect-erp/.../scratchpad`.

## Mockup code architecture (so you don't re-read 1170 lines)

Single `<script>`. Everything is string-template rendering into `#main`. Approx anchors:

- `NAV` (~L203–240): array of `{group, items:[{id, n, label, icon}]}`. `n` = spec module number shown as a badge; empty for non-spec pages. **Sidebar groups:** Overview, Master Data, Inventory & Pricing, Procurement, Store Operations, Sales, User Management.
- `ICONS` (~L241–264): id→SVG-path map used by `renderSidebar`.
- `renderSidebar(active)` (~L265): rebuilds nav, binds click→`route`.
- **Render helpers** (~L277–309): `head(no,title,desc,actions)`, `badge(type,text)`, `stat(lbl,val,extra)`, `ph(title,sub,right)`, `btn(label,cls,icon)`, icon consts `iAdd/iExport/iFilter`, `table(cols,rows)` (cols=`{t,k,num}`), `filterbar(fields,actions)`, `tabs(id,items)` (items=`{label,cnt,body}`), `pager(showing,total)`, `noteBox(html,color)` (color: cyan/indigo/green).
- `PAGES` object (~L311+): one arrow-fn per page returning an HTML string. Keys: `home, company, branch, vendor, allowance, hierarchy, item, inventory, price, cost, po, replen, receiving, transfer, rtv, adjust, makeup, salesaudit, users, roles, branchaccess, audit`.
- **Create/new-record forms** (~L800+): data constants `BR` (branch list ~L801), `VEN` (vendor list ~L802), input helper `ci(v,w)`. Functions `ff(field)`, `lineTable(l)`, `createForm(cfg)`. `CREATE` object (~L829) keyed: `user, po, item, vendor, company, branch, allowance, price, cost, transfer, rtv, adjust, makeup, node, receipt, salesbatch, replenrun`. `CREATE_BTN` (~L875) maps button label→CREATE key. `openCreate(key)`, `bindCreate()`.
- **POS batch detail** (~L888+): `BATCH` object, `posBatchView(id,b)`, `viewBatch(id)`, `bindBatch()` (binds `SB-####` row-links).
- **RBAC/admin** (~L965+): `rBadge`, `scopeCell(kind,txt)` (kind: chain/zone/store), `ck(on)`, `PAGES.users`, `ROLES` array, `PAGES.roles`, `PERM_MODULES`, `ROLE_PERMS`, `openRoleEditor(name)`, `accCell`, `BA_SCOPES` (Chain/Zone/Branch), `bindBranchAccess()`, `PAGES.branchaccess`, `aBadge`/`aRow`, `PAGES.audit`, `bindUM()`.
- `route(id)` (~L1142): `main.innerHTML=PAGES[id](); renderSidebar; bindTabs; bindCreate; bindBatch; bindUM; bindBranchAccess`. `bindTabs()` (~L1153). Boot: `route('home')` (~L1167).

**Editing pattern:** to change a page, edit its `PAGES.<key>` template and/or the matching `CREATE.<key>` config. Keep the helper-function contracts. Removing "Zone" must also touch: top-bar context `<select>` (~L184–187), `filterbar` zone fields, `CREATE.user` Branch-Access section, `BA_SCOPES`, `scopeCell`.

## Concrete edit targets per decision (map to spec sections)

| Decision (spec §) | Where in mockup |
|---|---|
| Levels → Department/Sub-Dept/Class/Sub-Class (§2.1) | `PAGES.hierarchy` tree + Node Detail; `PAGES.item` Classification selects; `CREATE.item` Classification; `CREATE.node` Level/Parent opts |
| Global hierarchy + per-item-vendor supply profile, 2-level vendor category (§2.2) | `PAGES.item` "Suppliers" tab (add vendor category L1/L2, MOQ, order increment, schedule, lead time, vendor cost); `CREATE.item`; possibly new fields on `CREATE.vendor` |
| Sub-Class default 101/102/103, variable (§2.3) | `PAGES.hierarchy` tree; `CREATE.node` |
| Item listing-fee + store-range tags (§3); buying/selling UOM | `PAGES.item` General/Attributes + Pack&UOM tab; `CREATE.item` |
| Store primary/alt vendor + cross-dock served (§4.1) | `PAGES.branch` columns; `CREATE.branch` Setup section |
| Vendor RTV-allowed flag (§4.2) | `PAGES.vendor`; `CREATE.vendor` |
| Buyer master entity (§4.3) | new maintenance page OR company sub-tab; referenced in `CREATE.po` Buyer select |
| Pricing/cost level = Chain→Banner→Store; drop geo Zone (§5.1–5.2) | `PAGES.price`/`PAGES.cost` Level badges + Scope selects; `CREATE.price`/`CREATE.cost` Scope opts; top-bar context; `PAGES.replen` filters; RBAC scope (`CREATE.user`, `BA_SCOPES`, `scopeCell`, `PAGES.branchaccess`) |
| Regular vs Promo PCA + auto-revert; latest-event-wins; daily report (§5.3) | `PAGES.price` (add PCA type, start/end dates); `CREATE.price` header |
| Margin guardrail = warning + approval routing (§5.4) | `PAGES.price`, `PAGES.cost` foot/status; `noteBox` text |
| PO margin guard / active-SKU / auto-drop log (§6.1) | `PAGES.po`, `CREATE.po` line validation notes |
| Per-vendor replen schedule; formula params; drop Confidence (§6.2) | `PAGES.replen` (remove Confidence column; add schedule tab); `CREATE.replenrun` |
| OTB budget carryover (§6.3) | `PAGES.po` OTB tab |
| Merchandise Issuance doc; bulk-upload adj; wtd-avg cost; damaged→RTV (§7.2–7.5) | `PAGES.inventory` (issuance doc on transfer), `PAGES.adjust`/`CREATE.adjust` (upload), `PAGES.rtv` |
| RBAC scope Chain-wide/By Store (§8/§9) | `CREATE.user` Branch Access; `BA_SCOPES` (drop Zone); `scopeCell` |

## Decision quick-reference (do not re-derive — see spec for full text)

Full replacement of JDA (no internal CSV). Global merch hierarchy, spec level names, sub-class variable. Vendor supply data on item↔vendor link (2-level vendor category, MOQ, increment, schedule, lead time, cost). Pricing = Chain→Banner(Price Club/Extra/Minimart)→Store; geographic Zone dropped now (future). Store classification (physical format) parked. Add: store primary/alt vendor + cross-dock, vendor RTV flag, buyer master, item listing-fee + store-range + buy/sell UOM. Price: Regular/Promo+auto-revert, latest-event-wins, daily compare report, margin warning+approval. PO: margin guard, active-SKU-only, auto-drop restricted (logged). Replen: per-vendor schedule, exposed formula params, OTB carryover, remove Confidence. Inventory: merch issuance doc, bulk-upload adj, weighted-avg costing, damaged-on-issue→RTV. Keep User Management (scope Chain-wide/By Store) + Dashboard. Global filter Company/Branch (Banner later).

## Next step

Not yet started implementation. Recommended: invoke the **writing-plans** skill to turn the spec into a step-by-step plan for revising `ui-mockup/iconnect-erp.html`, then implement page-by-page.
