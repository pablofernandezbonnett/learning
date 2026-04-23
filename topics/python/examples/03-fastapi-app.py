"""
FastAPI — Lightweight REST API
The Python equivalent of Spring Boot for quick, well-documented APIs.

Run:  uvicorn examples.03-fastapi-app:app --reload
Docs: http://localhost:8000/docs  (auto-generated OpenAPI UI — like Springdoc)

Requires: pip install fastapi uvicorn
"""

from fastapi import FastAPI, HTTPException, Depends, Query, status
from pydantic import BaseModel, Field, field_validator
from typing import Annotated
from datetime import datetime
from uuid import UUID, uuid4
from enum import Enum

# ─── APP SETUP ────────────────────────────────────────────────────────────────
# Like @SpringBootApplication — creates the app instance

app = FastAPI(
    title="Product Catalogue API",
    description="Retail product API — FastAPI demo",
    version="1.0.0",
)

# ─── MODELS (PYDANTIC) ────────────────────────────────────────────────────────
# Like Kotlin data classes with @field: validation.
# Pydantic validates on construction — no need for @Valid annotation.

class Category(str, Enum):
    TOPS = "tops"
    BOTTOMS = "bottoms"
    OUTERWEAR = "outerwear"
    ACCESSORIES = "accessories"


class ProductCreate(BaseModel):
    """Request body for creating a product. Like a Kotlin DTO."""
    sku: str = Field(..., min_length=3, max_length=50, example="UT-WHITE-M")
    name: str = Field(..., min_length=1, max_length=200)
    price: float = Field(..., gt=0, description="Price in JPY")
    category: Category

    @field_validator("sku")
    @classmethod
    def sku_must_be_uppercase(cls, v: str) -> str:
        if v != v.upper():
            raise ValueError("SKU must be uppercase")
        return v


class ProductResponse(BaseModel):
    """Response body. Like a Kotlin ResponseDTO."""
    id: UUID
    sku: str
    name: str
    price: float
    category: Category
    active: bool
    created_at: datetime

    model_config = {"from_attributes": True}   # like @JsonProperty in Spring


class PatchProduct(BaseModel):
    """Partial update. All fields optional — like a PATCH DTO."""
    name: str | None = None
    price: float | None = Field(default=None, gt=0)
    active: bool | None = None


# ─── IN-MEMORY STORE ──────────────────────────────────────────────────────────
# Replaces a DB for this demo. In production: inject a DB session via Depends().

class ProductRecord(BaseModel):
    id: UUID
    sku: str
    name: str
    price: float
    category: Category
    active: bool = True
    created_at: datetime = Field(default_factory=datetime.utcnow)

_db: dict[UUID, ProductRecord] = {}

# Seed data
for sku, name, price, cat in [
    ("UT-WHITE-M",    "Uniqlo T-Shirt White M",  2990.0, Category.TOPS),
    ("UT-BLACK-L",    "Uniqlo T-Shirt Black L",   2990.0, Category.TOPS),
    ("FLEECE-GREY-M", "Fleece Jacket Grey M",     4990.0, Category.OUTERWEAR),
]:
    rec = ProductRecord(id=uuid4(), sku=sku, name=name, price=price, category=cat)
    _db[rec.id] = rec


# ─── DEPENDENCY INJECTION ─────────────────────────────────────────────────────
# Like Spring's @Autowired / constructor injection.
# Depends() is evaluated per-request; FastAPI resolves the dependency graph.

def get_db() -> dict[UUID, ProductRecord]:
    """In production: yield a DB session, then close it in finally block."""
    return _db


# Type alias for injected dependency — cleaner signatures
DB = Annotated[dict[UUID, ProductRecord], Depends(get_db)]


def require_product(product_id: UUID, db: DB) -> ProductRecord:
    """Shared dependency — like a @Service method returning 404 on miss."""
    product = db.get(product_id)
    if not product:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Product {product_id} not found",
        )
    return product


# ─── ENDPOINTS ────────────────────────────────────────────────────────────────
# @app.get / @app.post → like @GetMapping / @PostMapping
# Return type annotation → FastAPI serialises automatically (like @ResponseBody)

@app.get("/products", response_model=list[ProductResponse])
def list_products(
    db: DB,
    category: Category | None = Query(default=None, description="Filter by category"),
    active_only: bool = Query(default=True),
    limit: int = Query(default=20, ge=1, le=100),
):
    """
    List products with optional filtering.
    Like: GET /api/products?category=tops&active_only=true&limit=20
    """
    results = list(db.values())

    if active_only:
        results = [p for p in results if p.active]
    if category:
        results = [p for p in results if p.category == category]

    return results[:limit]


@app.get("/products/{product_id}", response_model=ProductResponse)
def get_product(product: Annotated[ProductRecord, Depends(require_product)]):
    """Get a single product by ID. 404 handled in the shared dependency."""
    return product


@app.post("/products", response_model=ProductResponse, status_code=status.HTTP_201_CREATED)
def create_product(body: ProductCreate, db: DB):
    """
    Create a product.
    Pydantic validates the request body automatically — no @Valid needed.
    Returns 422 Unprocessable Entity on validation failure (not 400).
    """
    # Check for duplicate SKU
    if any(p.sku == body.sku for p in db.values()):
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=f"SKU {body.sku!r} already exists",
        )

    record = ProductRecord(id=uuid4(), **body.model_dump())
    db[record.id] = record
    return record


@app.patch("/products/{product_id}", response_model=ProductResponse)
def patch_product(
    body: PatchProduct,
    product: Annotated[ProductRecord, Depends(require_product)],
):
    """Partial update — only provided fields are changed."""
    update_data = body.model_dump(exclude_unset=True)   # only fields the client sent
    updated = product.model_copy(update=update_data)
    _db[product.id] = updated
    return updated


@app.delete("/products/{product_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_product(product: Annotated[ProductRecord, Depends(require_product)]):
    """Soft delete — sets active=False. Hard delete: del _db[product.id]"""
    _db[product.id] = product.model_copy(update={"active": False})


# ─── FASTAPI vs SPRING BOOT COMPARISON ────────────────────────────────────────
"""
Concept               Spring Boot (Kotlin)              FastAPI (Python)
──────────────────────────────────────────────────────────────────────────
Route annotation      @GetMapping("/products")          @app.get("/products")
Request body          @RequestBody @Valid ProductDTO     body: ProductCreate (Pydantic)
Path variable         @PathVariable id: UUID            product_id: UUID (path param)
Query param           @RequestParam category: String?   category: str | None = Query()
Dependency injection  @Autowired / constructor          Depends(get_db)
Response status       @ResponseStatus(CREATED)          status_code=201
404 handling          throw ResponseStatusException      raise HTTPException(404)
Validation            @NotBlank, @Min, @field:          Field(min_length=1, gt=0)
OpenAPI docs          springdoc-openapi (manual setup)  Built-in at /docs — no config
Async support         @Async, coroutines                async def (native asyncio)
Middleware            @Component WebFilter              @app.middleware("http")
"""

# ─── ASYNC ENDPOINT EXAMPLE ───────────────────────────────────────────────────
import asyncio

@app.get("/products/{product_id}/stock", response_model=dict)
async def get_stock(product: Annotated[ProductRecord, Depends(require_product)]):
    """
    Async endpoint — non-blocking I/O.
    Like a suspend fun in Kotlin. Use when calling DB/HTTP services.
    """
    # Simulate async DB call
    await asyncio.sleep(0.001)
    return {"sku": product.sku, "stock": 42, "reserved": 5, "available": 37}


# ─── STARTUP EVENT ────────────────────────────────────────────────────────────
# Like @PostConstruct in Spring — runs once when the app starts.

@app.on_event("startup")
async def startup():
    print(f"🚀 Catalogue API started — {len(_db)} products loaded")
    print("📖 Docs: http://localhost:8000/docs")
