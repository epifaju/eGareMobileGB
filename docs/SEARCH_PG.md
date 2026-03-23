# Recherche PostgreSQL avancée (optionnel)

En complément de la variante Java (`SearchSynonyms` : accents, typos courantes), vous pouvez activer côté base :

## Extensions

```sql
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS unaccent;
```

## Index trigram (libellés de ligne)

```sql
CREATE INDEX IF NOT EXISTS idx_vehicles_route_label_trgm
  ON vehicles USING gin (route_label gin_trgm_ops);
```

## Requêtes typiques

- **Similarité** : `WHERE route_label % :q` (opérateur `%` pg_trgm) avec `ORDER BY similarity(route_label, :q) DESC`.
- **Sans accents** : `WHERE unaccent(lower(route_label)) LIKE unaccent(lower('%' || :q || '%'))`.

Brancher ces requêtes via `@Query(nativeQuery = true)` ou `JdbcTemplate` en remplaçant progressivement les appels JPQL actuels, après validation des perfs en staging.
