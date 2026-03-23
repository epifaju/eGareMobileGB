package com.garemobilegb.search.support;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Variantes de requête (fautes courantes, accents) sans index plein texte côté DB.
 * Option PostgreSQL avancée : {@code docs/SEARCH_PG.md} (pg_trgm, unaccent).
 */
public final class SearchSynonyms {

  private static final Map<String, String> TYPOS =
      Map.ofEntries(
          Map.entry("bissao", "bissau"),
          Map.entry("bisau", "bissau"),
          Map.entry("gabu", "gabú"),
          Map.entry("bafata", "bafatá"));

  private SearchSynonyms() {}

  /** Normalise pour comparaison (minuscules, sans accents). */
  public static String stripAccents(String s) {
    if (s == null) {
      return "";
    }
    String n = Normalizer.normalize(s.trim(), Normalizer.Form.NFD);
    return n.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
  }

  /**
   * Variantes pour la sous-chaîne « destination » (recherche véhicules).
   */
  public static List<String> expandDestinationTerms(String raw) {
    if (raw == null || raw.isBlank()) {
      return List.of();
    }
    String trimmed = raw.trim();
    LinkedHashSet<String> out = new LinkedHashSet<>();
    out.add(trimmed);
    String lower = trimmed.toLowerCase(Locale.ROOT);
    if (!lower.equals(trimmed)) {
      out.add(lower);
    }
    String folded = stripAccents(trimmed);
    if (!folded.isEmpty()) {
      out.add(folded);
    }
    for (var e : TYPOS.entrySet()) {
      if (lower.contains(e.getKey())) {
        out.add(lower.replace(e.getKey(), e.getValue()));
      }
    }
    return new ArrayList<>(out);
  }

  /**
   * Variantes pour une requête déjà normalisée (minuscules, sans % / _) — autocomplétion.
   */
  public static List<String> expandNormalizedQuery(String normalizedQuery) {
    if (normalizedQuery == null || normalizedQuery.isBlank()) {
      return List.of();
    }
    LinkedHashSet<String> out = new LinkedHashSet<>();
    out.add(normalizedQuery);
    String folded = stripAccents(normalizedQuery);
    if (!folded.isEmpty() && !folded.equals(normalizedQuery)) {
      out.add(folded);
    }
    for (var e : TYPOS.entrySet()) {
      if (normalizedQuery.contains(e.getKey())) {
        out.add(normalizedQuery.replace(e.getKey(), e.getValue()));
      }
    }
    return new ArrayList<>(out);
  }

  public static boolean routeLabelMatchesFolded(String routeLabel, String queryFolded) {
    if (routeLabel == null || queryFolded == null || queryFolded.isEmpty()) {
      return false;
    }
    return stripAccents(routeLabel).contains(queryFolded);
  }
}
