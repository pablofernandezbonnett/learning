"""
pandas for Retail Data — ETL and Analysis Scripts
Use case: the kind of data work you do when you need to process product catalogues,
sales reports, or inventory exports that come as CSV/JSON from ERP/SAP systems.

Run: python examples/02-data-scripts.py
Requires: pip install pandas
"""

import pandas as pd
import json
from io import StringIO
from datetime import datetime, timedelta
import random

# ─── SAMPLE DATA ──────────────────────────────────────────────────────────────
# In real life this comes from: pd.read_csv("export.csv") or pd.read_json("data.json")
# or pd.read_sql("SELECT ...", connection)

SALES_CSV = """order_id,sku,store_id,quantity,unit_price,currency,order_date,status
ORD-001,UT-WHITE-M,STORE-TOKYO,2,2990,JPY,2024-01-10,DELIVERED
ORD-002,UT-BLACK-L,STORE-OSAKA,1,2990,JPY,2024-01-11,DELIVERED
ORD-003,UT-WHITE-M,STORE-TOKYO,3,2990,JPY,2024-01-12,DELIVERED
ORD-004,FLEECE-GREY-M,STORE-NAGOYA,1,4990,JPY,2024-01-12,CANCELLED
ORD-005,UT-BLACK-L,STORE-TOKYO,2,2990,JPY,2024-01-13,DELIVERED
ORD-006,FLEECE-GREY-M,STORE-OSAKA,2,4990,JPY,2024-01-14,DELIVERED
ORD-007,UT-WHITE-M,STORE-NAGOYA,1,2990,JPY,2024-01-15,DELIVERED
ORD-008,JEANS-SLIM-32,STORE-TOKYO,1,6990,JPY,2024-01-15,PROCESSING
ORD-009,UT-BLACK-L,STORE-NAGOYA,3,2990,JPY,2024-01-16,DELIVERED
ORD-010,JEANS-SLIM-32,STORE-OSAKA,2,6990,JPY,2024-01-16,DELIVERED
"""

PRODUCTS_CSV = """sku,name,category,cost_price
UT-WHITE-M,Uniqlo T-Shirt White M,tops,890
UT-BLACK-L,Uniqlo T-Shirt Black L,tops,890
FLEECE-GREY-M,Fleece Jacket Grey M,outerwear,1990
JEANS-SLIM-32,Slim Jeans 32,bottoms,2490
"""

# ─── 1. LOADING DATA ──────────────────────────────────────────────────────────

sales = pd.read_csv(StringIO(SALES_CSV), parse_dates=["order_date"])
products = pd.read_csv(StringIO(PRODUCTS_CSV))

print("=== Raw Sales Data ===")
print(sales.head())
print(f"\nShape: {sales.shape}")   # (rows, columns)
print(f"Dtypes:\n{sales.dtypes}")  # shows inferred types


# ─── 2. FILTERING & SELECTION ─────────────────────────────────────────────────
# Like SQL WHERE — boolean indexing

delivered = sales[sales["status"] == "DELIVERED"]
print(f"\nDelivered orders: {len(delivered)}/{len(sales)}")

# Multiple conditions — & (and), | (or), ~ (not)
tokyo_delivered = sales[
    (sales["store_id"] == "STORE-TOKYO") &
    (sales["status"] == "DELIVERED")
]
print(f"Tokyo delivered: {len(tokyo_delivered)}")

# Select specific columns
summary_cols = sales[["order_id", "sku", "unit_price", "quantity"]]

# Add computed column
sales["revenue"] = sales["unit_price"] * sales["quantity"]
print(f"\nRevenue column added. Total: ¥{sales['revenue'].sum():,}")


# ─── 3. GROUPBY & AGGREGATION ─────────────────────────────────────────────────
# Like SQL GROUP BY. Returns a new DataFrame.

print("\n=== Revenue by Store (delivered orders only) ===")
store_revenue = (
    delivered
    .groupby("store_id")["revenue"]
    .agg(["sum", "count", "mean"])
    .rename(columns={"sum": "total_revenue", "count": "orders", "mean": "avg_order"})
    .sort_values("total_revenue", ascending=False)
)
print(store_revenue)

print("\n=== Top SKUs by Units Sold ===")
sku_volume = (
    delivered
    .groupby("sku")
    .agg(
        units_sold=("quantity", "sum"),
        order_count=("order_id", "count"),
        revenue=("revenue", "sum")
    )
    .sort_values("units_sold", ascending=False)
)
print(sku_volume)


# ─── 4. JOIN (MERGE) ──────────────────────────────────────────────────────────
# Like SQL JOIN. pd.merge() is the main tool.

print("\n=== Sales Enriched with Product Info ===")
enriched = pd.merge(
    delivered,
    products,
    on="sku",
    how="left"   # LEFT JOIN — keep all sales even if no product match
)

# Calculate margin
enriched["margin"] = enriched["unit_price"] - enriched["cost_price"]
enriched["margin_pct"] = (enriched["margin"] / enriched["unit_price"] * 100).round(1)

print(enriched[["order_id", "sku", "name", "unit_price", "cost_price", "margin_pct"]].head())


# ─── 5. PIVOT TABLE ───────────────────────────────────────────────────────────
# Cross-tab — units sold per SKU per store. Classic retail report.

print("\n=== Units Sold by SKU × Store ===")
pivot = delivered.pivot_table(
    values="quantity",
    index="sku",
    columns="store_id",
    aggfunc="sum",
    fill_value=0     # NaN → 0 for missing combos
)
print(pivot)


# ─── 6. DATE-BASED ANALYSIS ───────────────────────────────────────────────────
# Common in retail: daily/weekly revenue trends

print("\n=== Daily Revenue Trend ===")
daily = (
    delivered
    .groupby(delivered["order_date"].dt.date)["revenue"]
    .sum()
    .reset_index()
    .rename(columns={"order_date": "date"})
)
print(daily)

# Rolling average (3-day window)
daily["revenue_3d_avg"] = daily["revenue"].rolling(window=3, min_periods=1).mean()


# ─── 7. APPLY — ROW-LEVEL TRANSFORMATIONS ─────────────────────────────────────
# For logic that doesn't fit vectorized operations

def classify_order_value(revenue: float) -> str:
    if revenue >= 10_000:
        return "high"
    elif revenue >= 5_000:
        return "medium"
    return "low"

delivered = delivered.copy()  # avoid SettingWithCopyWarning
delivered["value_tier"] = delivered["revenue"].apply(classify_order_value)

print("\n=== Orders by Value Tier ===")
print(delivered["value_tier"].value_counts())


# ─── 8. EXPORT ────────────────────────────────────────────────────────────────
# Write results back to CSV, JSON, or Excel

import tempfile, os

# CSV export
with tempfile.NamedTemporaryFile(suffix=".csv", delete=False, mode="w") as f:
    sku_volume.to_csv(f)
    csv_path = f.name
print(f"\nExported to CSV: {csv_path}")
os.unlink(csv_path)

# JSON export (for API payloads or further processing)
report = store_revenue.reset_index().to_dict(orient="records")
print("\nJSON-serialisable report:")
print(json.dumps(report[:2], indent=2, default=str))


# ─── 9. REAL USE CASE — INVENTORY DISCREPANCY SCRIPT ─────────────────────────
# Classic ops task: compare what the system says vs what the warehouse counted.
# This is the kind of script you write once and run weekly.

SYSTEM_STOCK = """sku,system_qty
UT-WHITE-M,150
UT-BLACK-L,89
FLEECE-GREY-M,42
JEANS-SLIM-32,0
"""

PHYSICAL_COUNT = """sku,counted_qty,count_date
UT-WHITE-M,147,2024-01-17
UT-BLACK-L,89,2024-01-17
FLEECE-GREY-M,38,2024-01-17
JEANS-SLIM-32,5,2024-01-17
"""

system = pd.read_csv(StringIO(SYSTEM_STOCK))
physical = pd.read_csv(StringIO(PHYSICAL_COUNT))

discrepancy = pd.merge(system, physical, on="sku")
discrepancy["diff"] = discrepancy["counted_qty"] - discrepancy["system_qty"]
discrepancy["action"] = discrepancy["diff"].apply(
    lambda d: "INVESTIGATE" if abs(d) > 2 else "OK"
)

print("\n=== Inventory Discrepancy Report ===")
print(discrepancy[["sku", "system_qty", "counted_qty", "diff", "action"]])

issues = discrepancy[discrepancy["action"] == "INVESTIGATE"]
if not issues.empty:
    print(f"\n⚠ {len(issues)} SKU(s) need investigation:")
    print(issues[["sku", "diff"]].to_string(index=False))

print("\n✓ Data scripts completed")
