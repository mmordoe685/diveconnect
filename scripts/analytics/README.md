# Scripts de Analytics

Pipeline ETL en Python que extrae datos de la BD MySQL de DiveConnect, los transforma con `pandas` y genera gráficas + CSVs consolidados para la memoria y dashboards externos.

## Instalación

Requiere Python 3.10+. La forma más limpia es un virtualenv:

```bash
cd scripts/analytics
python -m venv .venv

# Activar
source .venv/bin/activate           # macOS / Linux
.venv\Scripts\activate              # Windows PowerShell

pip install -r requirements.txt
```

## Uso

```bash
# Con valores por defecto (mismos que docker-compose / local)
python analytics.py

# Personalizando conexión
python analytics.py \
  --db-host localhost --db-port 3306 \
  --db-user diveconnect_user --db-pass 'DiveConnect2025!' \
  --db-name diveconnect_db

# O via variables de entorno
DB_HOST=mysql DB_PORT=3306 \
DB_USERNAME=diveconnect_user DB_PASSWORD='DiveConnect2025!' \
DB_NAME=diveconnect_db \
python analytics.py
```

## Salida

| Fichero | Contenido |
|---|---|
| `docs/screenshots/analytics/01-publicaciones-mes.png` | Barras: publicaciones agrupadas por mes |
| `docs/screenshots/analytics/02-reservas-estado.png`   | Pie: distribución de reservas por estado |
| `docs/screenshots/analytics/03-top-inmersiones.png`   | Barras horizontales: top 5 inmersiones por reservas |
| `scripts/analytics/dashboard.csv`                     | Snapshot consolidado de métricas globales |
| `scripts/analytics/publicaciones_mes.csv`             | Datos brutos de la primera gráfica |
| `scripts/analytics/reservas_estado.csv`               | Datos brutos de la segunda gráfica |
| `scripts/analytics/top_inmersiones.csv`               | Datos brutos de la tercera gráfica |
| `scripts/analytics/especies_top.csv`                  | Top 10 especies mencionadas en publicaciones |

Las gráficas usan la paleta corporativa de DiveConnect (turquoise/coral/gold/indigo) para integrarse con el resto del proyecto.

## Cómo se incrusta en la web

La idea original era servir el `dashboard.csv` desde un endpoint Spring Boot opcional, pero queda como deuda en el `ROADMAP` (futuro `AnalyticsController` que sirva los CSV/PNG generados al frontend para una pestaña de admin).

## Decisiones de diseño

- **Sin SQLAlchemy**: PyMySQL directo. La pequeña cantidad de queries no justifica el overhead de un ORM.
- **`matplotlib.use("Agg")`**: backend sin display, así corre en CI / Docker sin pantalla.
- **Idempotente**: ejecutar dos veces produce los mismos ficheros.
- **CSV + PNG**: ambos se generan. El CSV es para que la memoria importe los números reales; el PNG para inserción en docs y diapositivas.
- **Estilo coherente con la paleta del frontend**: tokens RGB tomados de `style.css` para que los gráficos no parezcan "ajenos" al producto.
