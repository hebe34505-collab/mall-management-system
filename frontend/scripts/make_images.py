from pathlib import Path

root = Path(r"C:\Users\dell\mall-management-system\frontend\public\images\products")
root.mkdir(parents=True, exist_ok=True)
colors = [
    "#15803D", "#0369A1", "#0F766E", "#B45309", "#7C2D12", "#1D4ED8",
    "#166534", "#9A3412", "#0E7490", "#3F6212", "#155E75", "#4D7C0F",
    "#92400E", "#1E3A8A", "#047857", "#0369A1", "#64748B", "#15803D",
]
names = ["耳机", "键盘", "支架", "衬衫", "背包", "休闲鞋", "咖啡", "坚果", "燕麦", "手册", "手账", "马克杯", "抱枕", "蜡烛", "瑜伽垫", "水杯", "跑鞋", "加湿器"]
for index, (color, name) in enumerate(zip(colors, names), start=1):
    svg = f'''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 480" role="img" aria-label="{name}">
  <rect width="640" height="480" fill="#F0FDF4"/>
  <rect x="70" y="50" width="500" height="300" rx="28" fill="{color}"/>
  <circle cx="190" cy="180" r="46" fill="#F8FAFC"/>
  <rect x="270" y="145" width="210" height="18" rx="9" fill="#F8FAFC"/>
  <rect x="270" y="185" width="160" height="12" rx="6" fill="#DCFCE7"/>
  <text x="320" y="420" text-anchor="middle" font-family="Microsoft YaHei, Segoe UI, sans-serif" font-size="36" fill="#14532D">{name}</text>
</svg>'''
    (root / f"p{index:02d}.svg").write_text(svg, encoding="utf-8")
print(len(list(root.glob("*.svg"))))
