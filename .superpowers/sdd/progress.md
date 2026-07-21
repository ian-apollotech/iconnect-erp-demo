# SDD Progress — iConnect ERP Master Data Backend

Plan: docs/superpowers/plans/2026-07-21-iconnect-erp-master-data-prototype-backend.md

Task 1: complete (commits e84e7cc..21d13d2, review clean)
Task 2: complete (commits 21d13d2..ef96123, review clean)
Task 3: complete (commits ef96123..1409bcd, review clean)
  MINOR (defer to final review): ApiExceptionHandler Map.of(message, ex.getMessage()) NPEs->500 if a future exception has null message. Low risk now.
  MINOR: Task 3 test does not exercise the handler HTTP contract; first real exercise is Task 4 Company MockMvc tests.
Task 4: complete (commits 1409bcd..7873f4c, review clean)
  MINOR (coverage, defer): PUT/update path (id-guard on edit) untested; code correct by construction (base update does findOne then validate). Brief scoped only 3 create tests.
Task 5: complete (commits 7873f4c..430893b, review clean; + security fix d6d70bd)
  SECURITY: stored-XSS in list render fixed by esc() helper. CONVENTION: Tasks 8 & 13 MUST wrap all DB-sourced string values in esc() when interpolating into innerHTML.
  Two deviations adjudicated OK: PAGES.company simplified per brief snippet; route() keeps page-fallback for unknown ids.
Task 6a: complete (commit ad57215, review clean)
  MINOR (perf, defer): Company list does 1 countByCompanyId query per row (N+1). Brief-specified; fine for prototype.
Task 6b-6e: complete (commits a4e6854,43a5eb2,f8d0759,cf9c298, review clean)
  MINOR (defer): explicit "" code/ref bypasses blank-skip -> DB unique violation -> unhandled 500. Fix candidate: add @ExceptionHandler(DataIntegrityViolationException)->400 (protects all entities) OR normalize ""->null.
  MINOR (cosmetic): unused JsonNode import in VendorCategoryApiTest + VendorScheduleApiTest.
Task 6f-6j: complete (commits 9a17355,7daf074,695b91e,ebc6416,afc9d1a, review clean)
  MINOR (defer): StoreGrouping.code unique but not @NotBlank (per brief; asymmetric vs Company/Branch). Consider @NotBlank for consistency.
TASK 6 COMPLETE: all 9 generic masters + Branch amendment. Suite 27/27.
Task 7: complete (commit cfc980f, review clean). Suite 28/28. All generic masters seeded.
Task 8: complete (commit 3163832, salvaged from interrupted run; esc() audited clean, 56 esc calls, JS parses). Full browser check deferred to final pass.
Task 9+10: complete (commits 076689d MerchNode, d382769 Product+PackUom). Suite 31/31, margin=19.2, scaffold 101/102/103 OK.
Task 13 (partial): Product + Hierarchy pages wired (commit aed242e). Full classification cascade. Product-focused slice DEPLOYABLE.
Task 11: complete (commit e40a9ec). VendorProduct single-Primary + CCM read-only cost; Product.preferredVendor derived. Suite 33.
Task 12: complete (commit f61963b). AssortmentPlan + lines, skuCount. Suite 35/35.
  Seed: 2 vendor-product links (Nescafe->Nestle, Alaska->Alaska Milk, Primary). Skipped links w/ no seeded product/vendor.
Task 13 (rest): vencatalog + Suppliers tab + Assortment wizard/list wired (commit cd90247). DONE_WITH_CONCERNS: item-detail mostly static; wizard cluster-level only; wizard SKU picker mock codes best-effort matched. Suite 35/35.
Task 14: complete (commit 3d579a6). Item Maintenance->Product Maintenance, Item Hierarchy->Category Hierarchy. JS parses.

=== FINAL REVIEW (Task 15) ===
Backend review (opus): all 8 signature rules OK. 2 IMPORTANT (reproduced live):
  BUG#1 AssortmentPlanService.validate does not FK-check companyId -> dangling company accepted 201 (should 400). Fix: inject CompanyRepository, guard if companyId!=null.
  BUG#2 single-Primary bypassed on PUT: apply copies sourcePriority, base update path skips createFromClient demotion -> two Primaries -> GET /api/products 500 (NonUniqueResultException). Fix: enforce single-Primary demotion on update path too.
  MINORS: auto-code 100000+id could collide w/ seeded literal codes (500); plan lines productId not FK-validated (by design); vendorCategoryId/allowanceId not validated (ok); scaffold child name="101/102/103" cosmetic.
  Triage: ApiExceptionHandler null-message NPE not merge-blocking; ""->500 not reachable for these entities; StoreGrouping out of scope.
FINAL FIXES complete (commit 073f487). Suite 40/40 (+5 TDD). Fixed BUG#1 AssortmentPlan.companyId FK->400; BUG#2 single-Primary on PUT->products 200 one Primary; +DataIntegrity->400 handler+null-safe msgs; SAVE.vendor Contact + SAVE.branch Setup/Address fields; allowance ref fallback; naming stragglers. DB reset + verified clean. PLAN COMPLETE Tasks 1-15.
