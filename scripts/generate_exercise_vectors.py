#!/usr/bin/env python3
"""
Generate theme-aware 2D volumetric anatomical vector SVG illustrations
for all 80 bundled exercises (Style 1: Modern Anatomical Silhouette + Muscle Heatmap).

Each exercise produces 2 frames:
- Frame 0: Setup / Eccentric Start Position
- Frame 1: Peak Contraction / Concentric Execution

Semantic Color Token Contract (for ThemeAwareSvgDecoder):
- #111111: Mannequin anatomical body silhouette (neutral fill with opacity + contour stroke)
- #FF0000: Primary active working muscle group (MaterialTheme.primary)
- #0000FF: Secondary stabilizer / supporting muscle group (MaterialTheme.tertiary)
- #888888: Apparatus, equipment, bars, benches, and floor lines (MaterialTheme.outline)
"""

import os
import sqlite3
import xml.etree.ElementTree as ET

PROJECT_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
DB_PATH = os.path.join(PROJECT_ROOT, "exercise/src/main/assets/database/exercises.db")
OUTPUT_DIR = os.path.join(PROJECT_ROOT, "exercise/src/main/assets/exercise_images")

def svg_wrap(content):
    return f"""<svg viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <marker id="arrow" viewBox="0 0 10 10" refX="5" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse">
      <path d="M 0 1.5 L 8 5 L 0 8.5 z" fill="#FF0000" />
    </marker>
    <marker id="arrow-neutral" viewBox="0 0 10 10" refX="5" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse">
      <path d="M 0 1.5 L 8 5 L 0 8.5 z" fill="#888888" />
    </marker>
  </defs>
  <!-- Background Badge / Stage -->
  <circle cx="256" cy="256" r="230" fill="#111111" fill-opacity="0.035" stroke="#111111" stroke-width="1.5" stroke-opacity="0.08" />
{content}
</svg>"""

# ----------------- REUSABLE APPARATUS ASSETS ----------------- #

def get_pull_up_bar(y=70):
    return f"""
    <!-- Ceiling / Wall mount fixtures -->
    <rect x="70" y="{y-30}" width="24" height="30" rx="4" fill="#888888" fill-opacity="0.4" stroke="#888888" stroke-width="3" />
    <rect x="418" y="{y-30}" width="24" height="30" rx="4" fill="#888888" fill-opacity="0.4" stroke="#888888" stroke-width="3" />
    <!-- Main Crossbar with grips -->
    <rect x="50" y="{y-8}" width="412" height="16" rx="8" fill="#888888" fill-opacity="0.6" stroke="#888888" stroke-width="3" />
    <rect x="180" y="{y-10}" width="45" height="20" rx="4" fill="#888888" fill-opacity="0.9" />
    <rect x="287" y="{y-10}" width="45" height="20" rx="4" fill="#888888" fill-opacity="0.9" />
    """

def get_floor(y=440):
    return f"""
    <!-- Ground plane line with beveled ends and soft ground shadow -->
    <ellipse cx="256" cy="{y+6}" rx="180" ry="12" fill="#888888" fill-opacity="0.15" />
    <rect x="40" y="{y-4}" width="432" height="8" rx="4" fill="#888888" fill-opacity="0.6" stroke="#888888" stroke-width="2" />
    """

def get_parallel_bars(y=260):
    return f"""
    <!-- Parallel Dip Rails -->
    <path d="M 140,{y+190} L 140,{y} Q 140,{y-15} 155,{y-15} L 220,{y-15}" fill="none" stroke="#888888" stroke-width="8" stroke-linecap="round" />
    <path d="M 372,{y+190} L 372,{y} Q 372,{y-15} 357,{y-15} L 292,{y-15}" fill="none" stroke="#888888" stroke-width="8" stroke-linecap="round" />
    <rect x="150" y="{y-20}" width="65" height="10" rx="5" fill="#888888" fill-opacity="0.9" />
    <rect x="297" y="{y-20}" width="65" height="10" rx="5" fill="#888888" fill-opacity="0.9" />
    <rect x="110" y="{y+185}" width="60" height="8" rx="4" fill="#888888" fill-opacity="0.6" />
    <rect x="342" y="{y+185}" width="60" height="8" rx="4" fill="#888888" fill-opacity="0.6" />
    """

def get_bench(x=290, y=280, w=150, h=140):
    return f"""
    <!-- Cushioned Bench Top -->
    <rect x="{x}" y="{y}" width="{w}" height="26" rx="10" fill="#888888" fill-opacity="0.75" stroke="#888888" stroke-width="3" />
    <!-- Bench Steel Legs -->
    <rect x="{x+15}" y="{y+26}" width="14" height="{h-26}" rx="3" fill="#888888" fill-opacity="0.5" stroke="#888888" stroke-width="2" />
    <rect x="{x+w-29}" y="{y+26}" width="14" height="{h-26}" rx="3" fill="#888888" fill-opacity="0.5" stroke="#888888" stroke-width="2" />
    <rect x="{x+5}" y="{y+h-6}" width="34" height="8" rx="4" fill="#888888" fill-opacity="0.7" />
    <rect x="{x+w-39}" y="{y+h-6}" width="34" height="8" rx="4" fill="#888888" fill-opacity="0.7" />
    """

def get_rings(y=190):
    return f"""
    <!-- Ring Straps -->
    <line x1="190" y1="30" x2="190" y2="{y-30}" stroke="#888888" stroke-width="6" stroke-dasharray="8,4" />
    <line x1="322" y1="30" x2="322" y2="{y-30}" stroke="#888888" stroke-width="6" stroke-dasharray="8,4" />
    <!-- Gymnastic Rings -->
    <circle cx="190" cy="{y}" r="26" fill="none" stroke="#888888" stroke-width="8" />
    <circle cx="322" cy="{y}" r="26" fill="none" stroke="#888888" stroke-width="8" />
    """

# ----------------- BIOMECHANICAL VOLUMETRIC GENERATORS ----------------- #

def get_pull_up_vectors(name):
    bar = get_pull_up_bar(70)
    
    # Target muscle definition
    if "Chin" in name or "Commando" in name:
        target_name = "biceps"
    elif "Dead Hang" in name:
        target_name = "forearms"
    elif "Scapular" in name:
        target_name = "traps"
    else:
        target_name = "lats"

    # Frame 0: Hanging (dead hang / setup)
    f0 = f"""{bar}
    <!-- Mannequin Head (Anatomical oval with jaw contour) -->
    <path d="M 242,125 C 242,112 248,102 256,102 C 264,102 270,112 270,125 C 270,138 264,148 256,148 C 248,148 242,138 242,125 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Left & Right Contoured Arms Reaching Upwards -->
    <path d="M 200,68 C 204,68 208,72 208,78 L 234,140 C 230,144 225,142 222,138 L 194,74 C 194,70 196,68 200,68 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <path d="M 312,68 C 308,68 304,72 304,78 L 278,140 C 282,144 287,142 290,138 L 318,74 C 318,70 316,68 312,68 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Athletic V-Taper Torso (Back view) -->
    <path d="M 226,148 C 218,175 224,225 238,275 C 246,280 266,280 274,275 C 288,225 294,175 286,148 C 274,152 238,152 226,148 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Volumetric Legs Straight Down (Quads & Calves) -->
    <path d="M 238,275 C 236,310 234,350 236,380 C 234,405 236,440 240,460 L 252,460 C 250,440 248,405 248,380 C 248,350 248,310 252,275 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <path d="M 274,275 C 276,310 278,350 276,380 C 278,405 276,440 272,460 L 260,460 C 262,440 264,405 264,380 C 264,350 264,310 260,275 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />

    <!-- Active Muscle Highlight: Latissimus Wings or Forearms -->
    """
    if target_name == "forearms":
        f0 += """
        <path d="M 197,72 L 208,98 L 200,102 L 193,75 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
        <path d="M 315,72 L 304,98 L 312,102 L 319,75 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
        """
    else:
        f0 += """
        <path d="M 226,152 C 220,180 226,220 238,245 C 240,225 236,185 240,158 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
        <path d="M 286,152 C 292,180 286,220 274,245 C 272,225 276,185 272,158 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
        """

    # Frame 1: Concentric Peak (Chin over bar)
    f1 = f"""{bar}
    <!-- Motion Trajectory Arrow -->
    <path d="M 256,190 L 256,120" stroke="#FF0000" stroke-width="3" stroke-dasharray="6,4" marker-end="url(#arrow)" />

    <!-- Mannequin Head (Chin elevated above bar) -->
    <path d="M 242,42 C 242,29 248,19 256,19 C 264,19 270,29 270,42 C 270,55 264,65 256,65 C 248,65 242,55 242,42 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Volumetric Bent Arms (Forearm to hand gripping bar, Bicep/Tricep flexed down) -->
    <path d="M 200,68 C 205,68 208,72 206,78 L 180,135 C 176,140 170,140 166,134 L 160,126 C 158,120 160,115 166,110 L 194,70 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <path d="M 312,68 C 307,68 304,72 306,78 L 332,135 C 336,140 342,140 346,134 L 352,126 C 354,120 352,115 346,110 L 318,70 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <path d="M 166,130 C 172,135 180,132 188,124 L 232,80 C 235,76 232,70 228,68 L 222,68 C 218,68 215,72 210,76 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <path d="M 346,130 C 340,135 332,132 324,124 L 280,80 C 277,76 280,70 284,68 L 290,68 C 294,68 297,72 302,76 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Elevated Torso -->
    <path d="M 226,68 C 218,95 224,145 238,195 C 246,200 266,200 274,195 C 288,145 294,95 286,68 C 274,72 238,72 226,68 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Legs Hanging Slightly Bent at Knees -->
    <path d="M 238,195 C 236,230 234,270 238,300 C 240,325 244,360 248,390 L 260,390 C 256,360 252,325 250,300 C 248,270 248,230 252,195 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <path d="M 274,195 C 276,230 278,270 274,300 C 272,325 268,360 264,390 L 252,390 C 256,360 260,325 262,300 C 264,270 264,230 260,195 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />

    <!-- Peak Active Muscle Highlights -->
    <path d="M 225,72 C 214,100 220,140 236,170 C 238,150 234,115 238,82 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
    <path d="M 287,72 C 298,100 292,140 276,170 C 274,150 278,115 274,82 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
    <!-- Secondary Bicep Contraction -->
    <path d="M 170,126 C 178,135 190,125 186,115 L 206,85 L 194,80 Z" fill="#0000FF" fill-opacity="0.8" stroke="#0000FF" stroke-width="1.5" />
    <path d="M 342,126 C 334,135 322,125 326,115 L 306,85 L 318,80 Z" fill="#0000FF" fill-opacity="0.8" stroke="#0000FF" stroke-width="1.5" />
    """

    return svg_wrap(f0), svg_wrap(f1)

def get_push_up_vectors(name):
    floor = get_floor(430)
    
    # Adaptations for decline / incline
    is_decline = "Decline" in name
    is_incline = "Incline" in name
    is_diamond = "Diamond" in name
    is_knee = "Knee" in name

    bench_asset = ""
    if is_decline:
        bench_asset = get_bench(x=350, y=340, w=110, h=90)
        feet_y = 340
    elif is_incline:
        bench_asset = get_bench(x=60, y=340, w=110, h=90)
        feet_y = 430
    else:
        feet_y = 430

    # Frame 0: Top lockout position
    f0 = f"""{floor}
    {bench_asset}
    <!-- Mannequin Head (Profile angle facing down-forward) -->
    <path d="M 90,240 C 90,225 100,215 112,215 C 124,215 132,225 132,240 C 132,255 122,265 110,265 C 98,265 90,255 90,240 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Torso Plank Wedge (Chest to Hips) -->
    <path d="M 125,235 C 160,250 220,270 290,285 C 295,295 285,310 270,305 C 200,285 145,265 120,250 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Extended Supporting Arm -->
    <path d="M 150,245 L 175,420 C 175,425 165,428 158,428 L 138,255 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Legs (Thigh to Calf and Foot) -->
    <path d="M 285,285 C 330,320 375,355 425,{feet_y-15} L 435,{feet_y} C 415,{feet_y} 360,360 275,305 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />

    <!-- Active Muscle Highlight: Pectoralis & Anterior Deltoid -->
    <path d="M 130,242 C 150,250 168,255 180,252 C 175,264 155,264 135,254 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
    <!-- Triceps Secondary Highlight -->
    <path d="M 144,260 L 158,340 L 148,340 L 138,265 Z" fill="#0000FF" fill-opacity="0.8" stroke="#0000FF" stroke-width="1.5" />
    """

    # Frame 1: Bottom 90-degree chest touch
    f1 = f"""{floor}
    {bench_asset}
    <!-- Motion Press Arrow -->
    <path d="M 160,370 L 160,290" stroke="#FF0000" stroke-width="3" stroke-dasharray="6,4" marker-end="url(#arrow)" />

    <!-- Mannequin Head (Hovering just above floor) -->
    <path d="M 80,345 C 80,332 90,322 102,322 C 114,322 122,332 122,345 C 122,358 112,368 100,368 C 88,368 80,358 80,345 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Torso Plank Wedge (Lowered horizontal) -->
    <path d="M 115,340 C 160,350 220,360 290,365 C 295,375 285,385 270,380 C 200,375 145,365 110,355 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Bent Arm (Elbow flexed at 90 degrees back/up) -->
    <path d="M 135,345 L 195,300 C 202,305 200,312 195,316 L 168,360 L 168,425 C 162,428 152,428 152,422 L 152,370 L 126,355 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Legs Straight -->
    <path d="M 285,365 C 330,375 375,385 425,{feet_y-10} L 435,{feet_y} C 415,{feet_y} 360,395 275,380 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />

    <!-- Peak Active Muscle Highlight: Pectoralis Major & Triceps Peak Stretch -->
    <path d="M 118,348 C 140,355 160,358 172,352 C 168,366 148,366 125,358 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
    <path d="M 142,340 L 190,305 L 198,312 L 152,348 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
    """

    return svg_wrap(f0), svg_wrap(f1)

def get_dip_vectors(name):
    if "Bench" in name:
        bench = get_bench(x=300, y=270, w=130, h=160)
        floor = get_floor(430)
        
        f0 = f"""{floor}
        {bench}
        <!-- Mannequin Head (Upright) -->
        <path d="M 258,160 C 258,145 266,135 276,135 C 286,135 294,145 294,160 C 294,175 286,185 276,185 C 266,185 258,175 258,160 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        
        <!-- Torso Upright -->
        <path d="M 264,185 C 260,220 262,260 268,295 C 276,300 292,300 298,295 C 296,260 296,220 292,185 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        
        <!-- Arm reaching back onto bench -->
        <path d="M 284,200 L 322,265 C 322,270 315,272 310,272 L 272,208 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        
        <!-- Legs extending forward to floor -->
        <path d="M 270,295 C 250,330 220,365 170,425 L 155,425 C 205,360 235,325 255,295 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        
        <!-- Triceps Contraction -->
        <path d="M 278,206 L 314,260 L 306,265 L 272,212 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
        """
        
        f1 = f"""{floor}
        {bench}
        <!-- Motion Arrow -->
        <path d="M 280,310 L 280,240" stroke="#FF0000" stroke-width="3" stroke-dasharray="6,4" marker-end="url(#arrow)" />

        <!-- Head Lowered -->
        <path d="M 258,230 C 258,215 266,205 276,205 C 286,205 294,215 294,230 C 294,245 286,255 276,255 C 266,255 258,245 258,230 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        
        <!-- Torso Lowered -->
        <path d="M 264,255 C 260,290 262,330 268,365 C 276,370 292,370 298,365 C 296,330 296,290 292,255 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        
        <!-- Arm Bent at 90 deg -->
        <path d="M 284,270 L 310,230 C 315,232 318,238 316,242 L 322,270 L 312,270 L 302,248 L 278,280 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        
        <!-- Legs extending forward -->
        <path d="M 270,365 C 250,385 220,405 170,425 L 155,425 C 205,400 235,380 255,365 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        
        <!-- Peak Triceps Stretch & Load -->
        <path d="M 282,272 L 308,234 L 316,240 L 290,280 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
        """
        return svg_wrap(f0), svg_wrap(f1)
    else:
        # Parallel bars / rings
        bars = get_rings(190) if "Ring" in name else get_parallel_bars(260)
        
        f0 = f"""{bars}
        <!-- Head Upright -->
        <path d="M 242,100 C 242,85 250,75 256,75 C 262,75 270,85 270,100 C 270,115 262,125 256,125 C 250,125 242,115 242,100 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        
        <!-- Torso Locked Out Upright -->
        <path d="M 230,128 C 224,165 228,215 238,260 C 246,265 266,265 274,260 C 284,215 288,165 282,128 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        
        <!-- Arms Locked Out Supporting Weight -->
        <path d="M 230,135 L 180,245 C 180,250 188,252 194,250 L 244,140 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <path d="M 282,135 L 332,245 C 332,250 324,252 318,250 L 268,140 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        
        <!-- Knees Bent Behind Body -->
        <path d="M 238,260 C 235,290 230,320 240,350 C 245,360 265,390 280,390 L 285,380 C 272,375 258,350 252,320 C 248,290 248,260 250,260 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <path d="M 274,260 C 271,290 266,320 276,350 C 281,360 301,390 316,390 L 321,380 C 308,375 294,350 288,320 C 284,290 284,260 286,260 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />

        <!-- Triceps Active Highlight -->
        <path d="M 232,145 L 185,240 L 195,244 L 242,150 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
        <path d="M 280,145 L 327,240 L 317,244 L 270,150 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
        """
        
        f1 = f"""{bars}
        <!-- Motion Arrow Driving Upward -->
        <path d="M 256,220 L 256,150" stroke="#FF0000" stroke-width="3" stroke-dasharray="6,4" marker-end="url(#arrow)" />

        <!-- Head Lowered Forward -->
        <path d="M 242,170 C 242,155 250,145 256,145 C 262,145 270,155 270,170 C 270,185 262,195 256,195 C 250,195 242,185 242,170 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        
        <!-- Torso Forward Lean at Bottom of Dip -->
        <path d="M 230,198 C 224,235 228,285 238,330 C 246,335 266,335 274,330 C 284,285 288,235 282,198 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        
        <!-- Arms Bent at 90 deg (Elbows flared back) -->
        <path d="M 234,200 L 155,190 C 150,192 148,198 152,204 L 185,250 C 190,252 195,250 195,244 L 168,206 L 244,212 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <path d="M 278,200 L 357,190 C 362,192 364,198 360,204 L 327,250 C 322,252 317,250 317,244 L 344,206 L 268,212 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        
        <!-- Knees Bent -->
        <path d="M 238,330 C 235,355 230,380 240,405 C 245,415 265,435 280,435 L 285,425 C 272,420 258,400 252,380 C 248,355 248,330 250,330 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <path d="M 274,330 C 271,355 266,380 276,405 C 281,415 301,435 316,435 L 321,425 C 308,420 294,400 288,380 C 284,355 284,330 286,330 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />

        <!-- Peak Muscle Activation: Chest & Triceps -->
        <path d="M 238,205 C 248,220 264,220 274,205 C 265,225 247,225 238,205 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
        <path d="M 230,202 L 158,194 L 165,202 L 235,210 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
        <path d="M 282,202 L 354,194 L 347,202 L 277,210 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
        """
        return svg_wrap(f0), svg_wrap(f1)

def get_squat_vectors(name):
    floor = get_floor(440)
    
    is_pistol = "Pistol" in name
    is_bulgarian = "Bulgarian" in name
    is_nordic = "Nordic" in name
    is_calf = "Calf" in name

    if is_nordic:
        # Kneeling on floor pad, ankles held
        f0 = f"""{floor}
        <!-- Ankle anchor strap -->
        <rect x="360" y="420" width="30" height="15" rx="4" fill="#888888" fill-opacity="0.8" />
        <!-- Head -->
        <path d="M 200,170 C 200,155 208,145 218,145 C 228,145 236,155 236,170 C 236,185 228,195 218,195 C 208,195 200,185 200,170 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <!-- Torso Upright Kneeling -->
        <path d="M 206,195 C 210,240 215,290 220,340 C 232,345 250,345 260,340 C 255,290 250,240 246,195 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <!-- Thighs Kneeling Vertical -->
        <path d="M 220,340 L 230,430 L 255,430 L 260,340 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <!-- Lower Legs on Floor -->
        <path d="M 230,430 L 380,430 L 380,415 L 245,415 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <!-- Hamstring Highlight -->
        <path d="M 245,345 L 248,425 L 258,425 L 256,345 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
        """
        
        f1 = f"""{floor}
        <rect x="360" y="420" width="30" height="15" rx="4" fill="#888888" fill-opacity="0.8" />
        <!-- Lowered Forward Torso (Eccentric descent) -->
        <path d="M 110,290 C 110,275 118,265 128,265 C 138,265 146,275 146,290 C 146,305 138,315 128,315 C 118,315 110,305 110,290 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <path d="M 136,295 C 170,320 205,340 240,360 C 248,370 260,365 265,355 C 230,335 195,310 160,285 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <path d="M 240,360 L 255,430 L 275,430 L 265,355 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <path d="M 255,430 L 380,430 L 380,415 L 265,415 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <!-- Peak Hamstring Tension -->
        <path d="M 245,362 L 262,425 L 272,425 L 260,358 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
        """
        return svg_wrap(f0), svg_wrap(f1)

    # Standard / Pistol / Lunge Squat
    f0 = f"""{floor}
    <!-- Mannequin Head (Standing Upright) -->
    <path d="M 242,85 C 242,70 250,60 256,60 C 262,60 270,70 270,85 C 270,100 262,110 256,110 C 250,110 242,100 242,85 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Torso Athletic Posture -->
    <path d="M 226,115 C 220,150 224,195 236,240 C 244,245 268,245 276,240 C 288,195 292,150 286,115 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Arms Relaxed or Crossed at Chest -->
    <path d="M 226,120 L 200,195 L 210,198 L 234,130 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <path d="M 286,120 L 312,195 L 302,198 L 278,130 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Muscular Standing Legs (Quads & Calves) -->
    <path d="M 236,240 C 230,285 225,335 228,375 C 226,400 228,425 230,438 L 246,438 C 244,425 242,400 242,375 C 244,335 248,285 252,240 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <path d="M 276,240 C 282,285 287,335 284,375 C 286,400 284,425 282,438 L 266,438 C 268,425 270,400 270,375 C 268,335 264,285 260,240 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />

    <!-- Quads Contour Highlight -->
    <path d="M 232,250 C 226,290 228,330 236,365 C 242,330 242,290 246,250 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
    <path d="M 280,250 C 286,290 284,330 276,365 C 270,330 270,290 266,250 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
    """

    # Frame 1: Deep Parallel Squat / Pistol
    f1 = f"""{floor}
    <!-- Motion Arrow (Upward drive from hole) -->
    <path d="M 210,310 L 210,230" stroke="#FF0000" stroke-width="3" stroke-dasharray="6,4" marker-end="url(#arrow)" />

    <!-- Head -->
    <path d="M 198,190 C 198,175 206,165 212,165 C 218,165 226,175 226,190 C 226,205 218,215 212,215 C 206,215 198,205 198,190 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Torso Angled Forward 45 degrees -->
    <path d="M 210,210 C 230,235 260,270 285,310 C 290,320 280,330 268,328 C 240,295 215,260 195,225 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Arms Extended Forward for Balance -->
    <path d="M 215,225 L 130,225 C 125,225 125,215 130,215 L 215,215 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Thigh Parallel to Floor & Knee Flexed at 90+ deg -->
    <path d="M 285,310 C 260,310 220,310 185,315 C 175,320 178,335 188,335 L 280,335 C 290,335 295,320 285,310 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Lower Shin & Foot -->
    <path d="M 185,325 L 205,438 C 205,442 195,442 188,438 L 170,335 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />

    <!-- Peak Glute & Quad Muscle Highlight -->
    <path d="M 275,310 C 255,310 220,310 195,315 C 205,325 240,325 270,325 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
    <path d="M 275,312 C 292,320 292,340 278,345 C 270,335 272,322 275,312 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
    """

    return svg_wrap(f0), svg_wrap(f1)

def get_handstand_vectors(name):
    floor = get_floor(440)
    
    is_wall = "Wall" in name
    wall_asset = '<line x1="360" y1="50" x2="360" y2="440" stroke="#888888" stroke-width="6" stroke-dasharray="10,6" />' if is_wall else ""

    # Frame 0: Handstand Lockout Inverted
    f0 = f"""{floor}
    {wall_asset}
    <!-- Hands planted on floor -->
    <rect x="200" y="432" width="35" height="8" rx="4" fill="#111111" fill-opacity="0.5" />
    <rect x="277" y="432" width="35" height="8" rx="4" fill="#111111" fill-opacity="0.5" />
    
    <!-- Straight Supporting Arms Lockout -->
    <path d="M 215,432 L 235,310 C 238,305 244,305 246,310 L 228,432 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <path d="M 297,432 L 277,310 C 274,305 268,305 266,310 L 284,432 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Head Between Shoulders -->
    <path d="M 242,330 C 242,315 250,305 256,305 C 262,305 270,315 270,330 C 270,345 262,355 256,355 C 250,355 242,345 242,330 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Inverted Torso -->
    <path d="M 230,305 C 224,265 228,215 238,165 C 246,160 266,160 274,165 C 284,215 288,265 282,305 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Inverted Extended Legs Straight Up -->
    <path d="M 238,165 C 236,130 234,90 238,60 L 250,60 C 248,90 248,130 252,165 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <path d="M 274,165 C 276,130 278,90 274,60 L 262,60 C 264,90 264,130 260,165 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />

    <!-- Deltoids / Traps Highlight -->
    <path d="M 230,315 C 225,295 235,285 245,295 C 240,315 235,320 230,315 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
    <path d="M 282,315 C 287,295 277,285 267,295 C 272,315 277,320 282,315 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
    """

    # Frame 1: HSPU Bottom / Tripod
    f1 = f"""{floor}
    {wall_asset}
    <!-- Motion Arrow Upward -->
    <path d="M 256,360 L 256,290" stroke="#FF0000" stroke-width="3" stroke-dasharray="6,4" marker-end="url(#arrow)" />

    <rect x="190" y="432" width="35" height="8" rx="4" fill="#111111" fill-opacity="0.5" />
    <rect x="287" y="432" width="35" height="8" rx="4" fill="#111111" fill-opacity="0.5" />
    
    <!-- Bent Arms in Tripod (Elbows flexed at 90 deg) -->
    <path d="M 205,432 L 160,375 C 158,370 162,365 168,365 L 230,360 C 235,362 235,368 230,372 L 180,380 L 218,432 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <path d="M 307,432 L 352,375 C 354,370 350,365 344,365 L 282,360 C 277,362 277,368 282,372 L 332,380 L 294,432 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Head Hovering 2 inches off floor -->
    <path d="M 242,405 C 242,390 250,380 256,380 C 262,380 270,390 270,405 C 270,420 262,430 256,430 C 250,430 242,420 242,405 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Inverted Torso Lowered -->
    <path d="M 230,360 C 224,320 228,270 238,220 C 246,215 266,215 274,220 C 284,270 288,320 282,360 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    
    <!-- Legs Straight Up -->
    <path d="M 238,220 C 236,185 234,145 238,115 L 250,115 C 248,145 248,185 252,220 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <path d="M 274,220 C 276,185 278,145 274,115 L 262,115 C 264,145 264,185 260,220 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />

    <!-- Peak Deltoid & Tricep Activation -->
    <path d="M 230,365 C 220,345 232,335 242,345 C 238,365 234,370 230,365 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
    <path d="M 282,365 C 292,345 280,335 270,345 C 274,365 278,370 282,365 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
    <path d="M 165,372 L 205,420 L 215,415 L 175,368 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
    <path d="M 347,372 L 307,420 L 297,415 L 337,368 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
    """

    return svg_wrap(f0), svg_wrap(f1)

def get_core_plank_vectors(name):
    floor = get_floor(430)
    
    if "Side" in name:
        f0 = f"""{floor}
        <!-- Head Profile -->
        <path d="M 120,265 C 120,250 130,240 142,240 C 154,240 162,250 162,265 C 162,280 154,290 142,290 C 130,290 120,280 120,265 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <!-- Torso in Straight Side Line -->
        <path d="M 155,270 C 205,295 255,320 310,345 C 315,355 305,370 290,365 C 235,340 185,315 145,285 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <!-- Supporting Forearm on Ground -->
        <path d="M 175,285 L 175,425 L 210,425 L 210,415 L 188,415 L 188,290 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <!-- Legs Stacked Together -->
        <path d="M 310,345 C 350,365 390,390 435,425 L 420,425 C 380,395 340,370 290,365 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <!-- Oblique Muscle Highlight -->
        <path d="M 195,310 C 240,335 270,350 285,355 C 275,365 240,350 190,325 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
        """
        return svg_wrap(f0), svg_wrap(f0)
    elif "L-Sit" in name:
        bars = get_parallel_bars(290)
        f0 = f"""{bars}
        <!-- Head -->
        <path d="M 200,120 C 200,105 208,95 218,95 C 228,95 236,105 236,120 C 236,135 228,145 218,145 C 208,145 200,135 200,120 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <!-- Torso Upright -->
        <path d="M 206,145 C 200,185 205,235 210,285 C 220,290 236,290 242,285 C 245,235 248,185 240,145 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <!-- Supporting Arms Locked Out -->
        <path d="M 220,165 L 185,275 C 185,280 192,282 198,280 L 232,170 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <!-- Legs Locked at 90 degrees -->
        <path d="M 210,285 L 390,285 C 395,285 395,295 390,298 L 220,310 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <!-- Core Compression Highlight (Abs & Hip Flexors) -->
        <path d="M 215,190 C 210,230 215,265 222,285 C 230,280 232,240 230,195 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
        <path d="M 222,285 L 290,285 L 285,302 L 222,305 Z" fill="#0000FF" fill-opacity="0.8" stroke="#0000FF" stroke-width="1.5" />
        """
        return svg_wrap(f0), svg_wrap(f0)
    elif "Hollow" in name:
        f0 = f"""{floor}
        <!-- Head -->
        <path d="M 85,340 C 85,325 95,315 105,315 C 115,315 125,325 125,340 C 125,355 115,365 105,365 C 95,365 85,355 85,340 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <!-- Banana Curved Torso & Pelvis -->
        <path d="M 120,345 C 165,370 235,385 305,370 C 375,355 415,335 435,320 L 440,332 C 415,350 375,375 305,392 C 235,402 165,385 115,355 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <!-- Arms Reaching Overhead -->
        <path d="M 115,348 L 50,330 C 45,330 45,320 50,318 L 115,335 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <!-- Abdominal Wall Full Compression Highlight -->
        <path d="M 180,360 C 235,375 285,370 320,358 C 300,378 240,385 180,372 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
        """
        return svg_wrap(f0), svg_wrap(f0)
    else:
        # Standard Forearm Plank
        f0 = f"""{floor}
        <!-- Head -->
        <path d="M 100,270 C 100,255 110,245 122,245 C 134,245 142,255 142,270 C 142,285 134,295 122,295 C 110,295 100,285 100,270 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <!-- Torso Line -->
        <path d="M 135,265 C 185,285 240,305 300,320 C 305,330 295,345 280,340 C 220,325 170,300 130,280 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <!-- Forearms on Floor -->
        <path d="M 160,275 L 160,425 L 205,425 L 205,415 L 175,415 L 175,280 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <!-- Legs to Toes -->
        <path d="M 295,320 C 340,350 385,385 435,425 L 420,425 C 375,390 330,360 280,340 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
        <!-- Rectus Abdominis Muscle Highlight -->
        <path d="M 185,285 C 225,302 265,315 285,320 C 275,332 235,320 185,300 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
        """
        return svg_wrap(f0), svg_wrap(f0)

def get_biceps_mobility_vectors(name, categories):
    floor = get_floor(440)
    
    # Standing Mannequin with Barbells/Dumbbells or Stretches
    f0 = f"""{floor}
    <!-- Head -->
    <path d="M 242,100 C 242,85 250,75 256,75 C 262,75 270,85 270,100 C 270,115 262,125 256,125 C 250,125 242,115 242,100 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <!-- Torso -->
    <path d="M 226,130 C 220,165 224,210 236,255 C 244,260 268,260 276,255 C 288,210 292,165 286,130 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <!-- Arms Extended Holding Dumbbell/Barbell at Thighs -->
    <path d="M 226,135 L 205,250 C 205,255 212,258 218,255 L 238,140 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <path d="M 286,135 L 307,250 C 307,255 300,258 294,255 L 274,140 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <!-- Barbell / Weight -->
    <rect x="180" y="248" width="152" height="8" rx="4" fill="#888888" stroke="#888888" stroke-width="2" />
    <circle cx="180" cy="252" r="16" fill="#888888" fill-opacity="0.75" />
    <circle cx="332" cy="252" r="16" fill="#888888" fill-opacity="0.75" />
    <!-- Legs -->
    <path d="M 236,255 L 232,438 L 246,438 L 252,255 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <path d="M 276,255 L 280,438 L 266,438 L 260,255 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <!-- Biceps Highlight -->
    <path d="M 224,145 L 210,210 L 220,212 L 234,150 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
    <path d="M 288,145 L 302,210 L 292,212 L 278,150 Z" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
    """
    
    f1 = f"""{floor}
    <!-- Motion Curl Arc Arrow -->
    <path d="M 195,230 Q 180,180 205,165" fill="none" stroke="#FF0000" stroke-width="3" stroke-dasharray="6,4" marker-end="url(#arrow)" />

    <!-- Head -->
    <path d="M 242,100 C 242,85 250,75 256,75 C 262,75 270,85 270,100 C 270,115 262,125 256,125 C 250,125 242,115 242,100 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <!-- Torso -->
    <path d="M 226,130 C 220,165 224,210 236,255 C 244,260 268,260 276,255 C 288,210 292,165 286,130 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <!-- Arms Curled Upwards at Chest -->
    <path d="M 226,135 L 210,195 L 230,155 L 238,140 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <path d="M 286,135 L 302,195 L 282,155 L 274,140 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <!-- Barbell Curled Up -->
    <rect x="180" y="152" width="152" height="8" rx="4" fill="#888888" stroke="#888888" stroke-width="2" />
    <circle cx="180" cy="156" r="16" fill="#888888" fill-opacity="0.75" />
    <circle cx="332" cy="156" r="16" fill="#888888" fill-opacity="0.75" />
    <!-- Legs -->
    <path d="M 236,255 L 232,438 L 246,438 L 252,255 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <path d="M 276,255 L 280,438 L 266,438 L 260,255 Z" fill="#111111" fill-opacity="0.22" stroke="#111111" stroke-width="2" stroke-opacity="0.6" />
    <!-- Peak Bicep Contraction Ball -->
    <circle cx="218" cy="175" r="14" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
    <circle cx="294" cy="175" r="14" fill="#FF0000" fill-opacity="0.9" stroke="#FF0000" stroke-width="1.5" />
    """

    return svg_wrap(f0), svg_wrap(f1)

def dispatch_vector(name, categories):
    lower = name.lower()
    if "dip" in lower:
        return get_dip_vectors(name)
    elif "push-up" in lower or "push up" in lower or "press" in lower and "shoulder" not in lower:
        return get_push_up_vectors(name)
    elif "pull-up" in lower or "pull up" in lower or "chin-up" in lower or "chin up" in lower or "hang" in lower or "lever" in lower:
        return get_pull_up_vectors(name)
    elif "squat" in lower or "lunge" in lower or "calf" in lower or "deadlift" in lower or "bridge" in lower or "nordic" in lower:
        return get_squat_vectors(name)
    elif "handstand" in lower or "shoulder" in lower or "pike" in lower:
        return get_handstand_vectors(name)
    elif "plank" in lower or "l-sit" in lower or "hollow" in lower or "crunch" in lower or "twist" in lower or "climber" in lower or "taps" in lower or "crawl" in lower:
        return get_core_plank_vectors(name)
    else:
        return get_biceps_mobility_vectors(name, categories)

def main():
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    cursor.execute("""
        SELECT e.exerciseId, e.name, GROUP_CONCAT(c.categoryName, ',')
        FROM Exercise e
        LEFT JOIN ExerciseCategoryCrossRef c ON e.exerciseId = c.exerciseId
        GROUP BY e.exerciseId
        ORDER BY e.name
    """)
    exercises = cursor.fetchall()
    print(f"Loaded {len(exercises)} exercises from database.")
    
    count = 0
    for eid, name, cats_str in exercises:
        cats = [c.strip() for c in (cats_str or "").split(",") if c.strip()]
        
        cursor.execute("SELECT imageId, orderIndex FROM ExerciseImage WHERE exerciseId = ? ORDER BY orderIndex", (eid,))
        images = cursor.fetchall()
        if not images:
            continue
            
        f0_svg, f1_svg = dispatch_vector(name, cats)
        
        # Validate XML structure before writing
        ET.fromstring(f0_svg)
        ET.fromstring(f1_svg)
        
        img0_id = images[0][0]
        f0_path = os.path.join(OUTPUT_DIR, f"{img0_id}.svg")
        with open(f0_path, "w", encoding="utf-8") as f:
            f.write(f0_svg)
            
        if len(images) > 1:
            img1_id = images[1][0]
            f1_path = os.path.join(OUTPUT_DIR, f"{img1_id}.svg")
            with open(f1_path, "w", encoding="utf-8") as f:
                f.write(f1_svg)
            
        count += len(images)
        
    conn.close()
    print(f"Successfully generated {count} Style 1 (Modern Anatomical Silhouette + Muscle Heatmap) SVGs in {OUTPUT_DIR}!")

if __name__ == "__main__":
    main()
