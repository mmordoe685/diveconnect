"""
DiveConnect — Análisis de datos con Python (módulo optativo 2º DAW).

Este script ETL hace tres cosas:

  1. EXTRACT: se conecta a la BD MySQL de DiveConnect y extrae los datos
              relevantes (publicaciones, reservas, usuarios, inmersiones).

  2. TRANSFORM: limpia y agrega métricas de negocio:
       - publicaciones por mes
       - reservas por estado y por centro
       - profundidad media por usuario
       - especies más mencionadas
       - ingresos confirmados acumulados

  3. LOAD/REPORT: genera tres gráficas PNG en docs/screenshots/analytics/
                  y un CSV consolidado en scripts/analytics/dashboard.csv,
                  consumibles desde la memoria o un dashboard externo.

Uso:
    pip install -r requirements.txt
    python analytics.py [--db-host localhost] [--db-port 3306]
                        [--db-user diveconnect_user]
                        [--db-pass DiveConnect2025!]
                        [--db-name diveconnect_db]

Diseño consciente:
- Sin dependencias propietarias (sklearn, etc.); sólo pandas + matplotlib +
  pymysql, que cubren la rúbrica del módulo "Programación en Python y
  Análisis de Datos".
- Idempotente: ejecutar dos veces produce el mismo resultado.
- Gráficas en estilo coherente con la paleta del frontend (turquoise,
  coral, gold) para que las imágenes encajen con el resto del proyecto.
"""

from __future__ import annotations

import argparse
import os
import sys
from datetime import datetime
from pathlib import Path

import matplotlib
matplotlib.use("Agg")  # entorno sin display (CI / Render)
import matplotlib.pyplot as plt
import pandas as pd
import pymysql


# ── Paleta corporativa de DiveConnect ──────────────────────────
PALETTE = {
    "navy":     "#0B1A2B",
    "navy_mid": "#1B4965",
    "seafoam":  "#00D4AA",
    "indigo":   "#5B5EA6",
    "coral":    "#FF6B6B",
    "gold":     "#F5A623",
    "cream":    "#FFFBF5",
    "ink":      "#1A1A2E",
    "ink_muted":"#4A5568",
}

# Estilo común de matplotlib para mantener consistencia visual.
plt.rcParams.update({
    "figure.figsize":  (9, 5),
    "font.family":     "sans-serif",
    "font.sans-serif": ["DejaVu Sans", "Arial", "Helvetica"],
    "axes.edgecolor":  PALETTE["ink_muted"],
    "axes.labelcolor": PALETTE["ink"],
    "axes.titlesize":  14,
    "axes.titleweight":"bold",
    "axes.titlecolor": PALETTE["navy"],
    "axes.spines.top":   False,
    "axes.spines.right": False,
    "xtick.color":  PALETTE["ink_muted"],
    "ytick.color":  PALETTE["ink_muted"],
    "grid.color":   "#E8E3DC",
    "grid.linestyle": "--",
    "grid.alpha":   0.5,
    "savefig.dpi":  120,
    "savefig.bbox": "tight",
})


# ── Conexión a BD ──────────────────────────────────────────────
def connect(args):
    """Devuelve una conexión PyMySQL en modo cursor de tuplas."""
    return pymysql.connect(
        host=args.db_host,
        port=args.db_port,
        user=args.db_user,
        password=args.db_pass,
        database=args.db_name,
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
    )


def fetch_df(conn, sql: str) -> pd.DataFrame:
    """Ejecuta una query y devuelve un DataFrame."""
    with conn.cursor() as cur:
        cur.execute(sql)
        rows = cur.fetchall()
    return pd.DataFrame(rows)


# ── Métrica 1: Publicaciones por mes ───────────────────────────
def publicaciones_por_mes(conn) -> pd.DataFrame:
    df = fetch_df(conn, """
        SELECT DATE_FORMAT(fecha_publicacion, '%Y-%m') AS mes,
               COUNT(*)                                AS publicaciones
        FROM publicaciones
        GROUP BY mes
        ORDER BY mes ASC
    """)
    return df


def grafica_publicaciones_por_mes(df: pd.DataFrame, out: Path) -> None:
    if df.empty:
        return
    fig, ax = plt.subplots()
    ax.bar(df["mes"], df["publicaciones"], color=PALETTE["seafoam"], edgecolor=PALETTE["navy_mid"])
    ax.set_title("Publicaciones por mes")
    ax.set_xlabel("Mes")
    ax.set_ylabel("Nº de publicaciones")
    ax.grid(axis="y")
    for i, v in enumerate(df["publicaciones"]):
        ax.text(i, v + 0.3, str(v), ha="center", color=PALETTE["navy"], fontweight="bold")
    plt.xticks(rotation=30, ha="right")
    fig.savefig(out)
    plt.close(fig)


# ── Métrica 2: Reservas por estado ─────────────────────────────
def reservas_por_estado(conn) -> pd.DataFrame:
    df = fetch_df(conn, """
        SELECT estado, COUNT(*) AS total
        FROM reservas
        GROUP BY estado
        ORDER BY total DESC
    """)
    return df


def grafica_reservas_por_estado(df: pd.DataFrame, out: Path) -> None:
    if df.empty:
        return
    color_map = {
        "PENDIENTE":  PALETTE["gold"],
        "CONFIRMADA": "#22C55E",
        "COMPLETADA": "#3B82F6",
        "CANCELADA":  PALETTE["coral"],
    }
    colors = [color_map.get(e, PALETTE["seafoam"]) for e in df["estado"]]

    fig, ax = plt.subplots(figsize=(7, 7))
    wedges, _, autotexts = ax.pie(
        df["total"],
        labels=df["estado"],
        colors=colors,
        autopct="%1.1f%%",
        startangle=90,
        wedgeprops=dict(edgecolor="white", linewidth=2),
        textprops=dict(color=PALETTE["navy"], fontweight="bold"),
    )
    for at in autotexts:
        at.set_color("white")
    ax.set_title("Distribución de reservas por estado")
    fig.savefig(out)
    plt.close(fig)


# ── Métrica 3: Top-5 inmersiones más reservadas ────────────────
def top_inmersiones(conn) -> pd.DataFrame:
    df = fetch_df(conn, """
        SELECT  i.titulo,
                c.nombre AS centro,
                COUNT(r.id) AS num_reservas,
                COALESCE(SUM(r.precio_total), 0) AS total_eur
        FROM    inmersiones i
        LEFT JOIN reservas      r ON r.inmersion_id = i.id
        JOIN      centros_buceo c ON c.id = i.centro_buceo_id
        GROUP BY i.id, i.titulo, c.nombre
        ORDER BY num_reservas DESC, total_eur DESC
        LIMIT 5
    """)
    return df


def grafica_top_inmersiones(df: pd.DataFrame, out: Path) -> None:
    if df.empty:
        return
    df = df.copy()
    df["label"] = df.apply(lambda r: f"{r['titulo'][:30]}\n— {r['centro']}", axis=1)

    fig, ax = plt.subplots()
    bars = ax.barh(df["label"], df["num_reservas"], color=PALETTE["indigo"], edgecolor=PALETTE["navy_mid"])
    ax.set_title("Top 5 inmersiones por número de reservas")
    ax.set_xlabel("Nº de reservas")
    ax.invert_yaxis()
    ax.grid(axis="x")
    for bar, val, eur in zip(bars, df["num_reservas"], df["total_eur"]):
        ax.text(bar.get_width() + 0.05, bar.get_y() + bar.get_height() / 2,
                f"{val} ({eur:.0f} €)",
                va="center", color=PALETTE["navy"], fontweight="bold")
    fig.savefig(out)
    plt.close(fig)


# ── Métricas adicionales ───────────────────────────────────────
def metricas_globales(conn) -> dict:
    df = fetch_df(conn, """
        SELECT  (SELECT COUNT(*) FROM usuarios WHERE activo = TRUE)       AS usuarios_activos,
                (SELECT COUNT(*) FROM usuarios WHERE tipo_usuario = 'USUARIO_EMPRESA') AS centros,
                (SELECT COUNT(*) FROM inmersiones WHERE activa = TRUE)    AS inmersiones,
                (SELECT COUNT(*) FROM publicaciones)                      AS publicaciones,
                (SELECT COUNT(*) FROM reservas)                           AS total_reservas,
                (SELECT COUNT(*) FROM reservas WHERE payment_status='PAID') AS pagadas,
                (SELECT COALESCE(SUM(precio_total),0) FROM reservas WHERE payment_status='PAID') AS ingresos_pagados,
                (SELECT AVG(profundidad_maxima) FROM publicaciones WHERE profundidad_maxima IS NOT NULL) AS profundidad_media
    """)
    return df.iloc[0].to_dict() if not df.empty else {}


def especies_top(conn) -> pd.DataFrame:
    """
    Especies mencionadas en publicaciones. Como `especies_vistas` es texto
    libre con varias especies separadas por coma, hacemos un EXPLODE en
    Python (no es trivial en SQL puro).
    """
    df = fetch_df(conn, """
        SELECT especies_vistas
        FROM publicaciones
        WHERE especies_vistas IS NOT NULL AND especies_vistas <> ''
    """)
    if df.empty:
        return pd.DataFrame(columns=["especie", "menciones"])

    explode = (df["especies_vistas"]
               .str.split(r"[,;/]")
               .explode()
               .str.strip()
               .str.lower())
    explode = explode[explode.str.len() > 1]
    counts = explode.value_counts().head(10).reset_index()
    counts.columns = ["especie", "menciones"]
    return counts


# ── Pipeline principal ─────────────────────────────────────────
def main() -> int:
    parser = argparse.ArgumentParser(description="DiveConnect — Análisis de datos")
    parser.add_argument("--db-host", default=os.environ.get("DB_HOST", "localhost"))
    parser.add_argument("--db-port", default=int(os.environ.get("DB_PORT", "3306")), type=int)
    parser.add_argument("--db-user", default=os.environ.get("DB_USERNAME", "diveconnect_user"))
    parser.add_argument("--db-pass", default=os.environ.get("DB_PASSWORD", "DiveConnect2025!"))
    parser.add_argument("--db-name", default=os.environ.get("DB_NAME", "diveconnect_db"))
    args = parser.parse_args()

    project_root = Path(__file__).resolve().parents[2]
    out_dir = project_root / "docs" / "screenshots" / "analytics"
    out_dir.mkdir(parents=True, exist_ok=True)
    csv_dir = Path(__file__).resolve().parent
    csv_dir.mkdir(parents=True, exist_ok=True)

    print(f"[+] Conectando a {args.db_host}:{args.db_port}/{args.db_name}")
    try:
        conn = connect(args)
    except pymysql.MySQLError as e:
        print(f"[!] Error conectando a MySQL: {e}", file=sys.stderr)
        return 1

    try:
        # 1. Métricas globales
        globales = metricas_globales(conn)
        print("\n[+] Métricas globales")
        for k, v in globales.items():
            print(f"    {k:24s} = {v}")

        # 2. Publicaciones por mes
        df_pubs = publicaciones_por_mes(conn)
        print(f"\n[+] Publicaciones por mes: {len(df_pubs)} filas")
        grafica_publicaciones_por_mes(df_pubs, out_dir / "01-publicaciones-mes.png")
        df_pubs.to_csv(csv_dir / "publicaciones_mes.csv", index=False)

        # 3. Reservas por estado
        df_res = reservas_por_estado(conn)
        print(f"[+] Reservas por estado: {len(df_res)} estados")
        grafica_reservas_por_estado(df_res, out_dir / "02-reservas-estado.png")
        df_res.to_csv(csv_dir / "reservas_estado.csv", index=False)

        # 4. Top inmersiones
        df_top = top_inmersiones(conn)
        print(f"[+] Top inmersiones: {len(df_top)} filas")
        grafica_top_inmersiones(df_top, out_dir / "03-top-inmersiones.png")
        df_top.to_csv(csv_dir / "top_inmersiones.csv", index=False)

        # 5. Especies más mencionadas
        df_esp = especies_top(conn)
        print(f"[+] Especies top: {len(df_esp)} filas")
        if not df_esp.empty:
            df_esp.to_csv(csv_dir / "especies_top.csv", index=False)

        # 6. Dashboard consolidado
        dashboard = pd.DataFrame([{
            "fecha_calculo":     datetime.now().isoformat(timespec="seconds"),
            **globales,
        }])
        dashboard.to_csv(csv_dir / "dashboard.csv", index=False)

        print(f"\n[OK] Gráficas guardadas en {out_dir}")
        print(f"[OK] CSV consolidado en {csv_dir}/dashboard.csv")
        return 0
    finally:
        conn.close()


if __name__ == "__main__":
    sys.exit(main())
