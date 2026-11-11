#!/usr/bin/env python3
"""
Generate theme-aware vector SVG illustrations for all 80 bundled exercises.
Each exercise produces 2 frames (Frame 0: Setup, Frame 1: Peak/Execution).

Semantic Color Token Contract:
- #888888: Apparatus (Bar, Floor, Bench, Rings) -> outlineVariant
- #111111: Mannequin body lines -> onSurface
- #FF0000: Active working muscle -> primary
- #0000FF: Secondary stabilizer muscle -> tertiary
"""

import os
import sqlite3
import xml.etree.ElementTree as ET

PROJECT_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
DB_PATH = os.path.join(PROJECT_ROOT, "exercise/src/main/assets/database/exercises.db")
OUTPUT_DIR = os.path.join(PROJECT_ROOT, "exercise/src/main/assets/exercise_images")

def svg_wrap(content):
    return f"""<svg viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg">
{content}
</svg>"""

# ----------------- BIOMECHANICAL VECTOR GENERATORS ----------------- #

def get_pull_up_vectors(name):
    # Bar at top
    bar = '<path stroke="#888888" stroke-width="8" stroke-linecap="round" d="M 60,70 L 452,70" />'
    
    if "Chin" in name:
        grip_accent_0 = '<circle cx="210" cy="70" r="10" stroke="#FF0000" stroke-width="4" fill="#FF0000" /><circle cx="302" cy="70" r="10" stroke="#FF0000" stroke-width="4" fill="#FF0000" />'
        muscle_0 = '<path stroke="#FF0000" stroke-width="6" stroke-linecap="round" d="M 215,115 L 215,180" /><path stroke="#FF0000" stroke-width="6" stroke-linecap="round" d="M 297,115 L 297,180" />'
        muscle_1 = '<path stroke="#FF0000" stroke-width="8" stroke-linecap="round" fill="#FF0000" d="M 205,85 Q 215,120 225,95 Z" /><path stroke="#FF0000" stroke-width="8" stroke-linecap="round" fill="#FF0000" d="M 307,85 Q 297,120 287,95 Z" />'
    elif "Dead Hang" in name:
        grip_accent_0 = ''
        muscle_0 = '<path stroke="#FF0000" stroke-width="6" stroke-linecap="round" d="M 220,180 L 235,230" /><path stroke="#FF0000" stroke-width="6" stroke-linecap="round" d="M 292,180 L 277,230" />'
        muscle_1 = muscle_0
    else:
        grip_accent_0 = ''
        muscle_0 = '<path stroke="#FF0000" stroke-width="6" stroke-linecap="round" d="M 215,140 L 235,220" /><path stroke="#FF0000" stroke-width="6" stroke-linecap="round" d="M 297,140 L 277,220" />'
        muscle_1 = '<path stroke="#FF0000" stroke-width="8" stroke-linecap="round" fill="#FF0000" d="M 220,95 Q 235,160 250,110 Z" /><path stroke="#FF0000" stroke-width="8" stroke-linecap="round" fill="#FF0000" d="M 292,95 Q 277,160 262,110 Z" />'

    # Frame 0: Dead hang from bar
    f0 = f"""{bar}
    {grip_accent_0}
    <!-- Head -->
    <circle cx="256" cy="140" r="24" stroke="#111111" stroke-width="8" fill="none" />
    <!-- Arms reaching overhead to bar -->
    <path stroke="#111111" stroke-width="12" stroke-linecap="round" d="M 210,70 L 236,150" />
    <path stroke="#111111" stroke-width="12" stroke-linecap="round" d="M 302,70 L 276,150" />
    <!-- Torso -->
    <path stroke="#111111" stroke-width="16" stroke-linecap="round" d="M 256,160 L 256,290" />
    <!-- Legs straight down -->
    <path stroke="#111111" stroke-width="14" stroke-linecap="round" d="M 256,290 L 250,440" />
    <path stroke="#111111" stroke-width="14" stroke-linecap="round" d="M 256,290 L 262,440" />
    {muscle_0}"""

    # Frame 1: Pulled up, chin over bar
    f1 = f"""{bar}
    <!-- Head above bar -->
    <circle cx="256" cy="45" r="24" stroke="#111111" stroke-width="8" fill="none" />
    <!-- Arms bent, elbows down and tucked -->
    <path stroke="#111111" stroke-width="12" stroke-linecap="round" stroke-linejoin="round" fill="none" d="M 210,70 L 190,140 L 236,80" />
    <path stroke="#111111" stroke-width="12" stroke-linecap="round" stroke-linejoin="round" fill="none" d="M 302,70 L 322,140 L 276,80" />
    <!-- Torso elevated -->
    <path stroke="#111111" stroke-width="16" stroke-linecap="round" d="M 256,70 L 256,210" />
    <!-- Legs hanging or knees slightly bent -->
    <path stroke="#111111" stroke-width="14" stroke-linecap="round" d="M 256,210 L 250,370" />
    <path stroke="#111111" stroke-width="14" stroke-linecap="round" d="M 256,210 L 262,370" />
    {muscle_1}"""

    return svg_wrap(f0), svg_wrap(f1)

def get_push_up_vectors(name):
    floor = '<path stroke="#888888" stroke-width="6" stroke-linecap="round" d="M 40,420 L 472,420" />'
    
    # Diamond / Wide adaptations
    hand_offset = 0
    if "Diamond" in name:
        hand_offset = -20
    elif "Wide" in name:
        hand_offset = 30
        
    f0 = f"""{floor}
    <circle cx="110" cy="225" r="26" stroke="#111111" stroke-width="8" fill="none" />
    <path stroke="#111111" stroke-width="16" stroke-linecap="round" d="M 134,240 L 290,290" />
    <path stroke="#111111" stroke-width="12" stroke-linecap="round" d="M 160,250 L {160 + hand_offset},420" />
    <path stroke="#111111" stroke-width="8" stroke-linecap="round" d="M {150 + hand_offset},420 L {175 + hand_offset},420" />
    <path stroke="#111111" stroke-width="14" stroke-linecap="round" d="M 290,290 L 420,380" />
    <path stroke="#111111" stroke-width="10" stroke-linecap="round" d="M 420,380 L 440,420" />
    <path stroke="#FF0000" stroke-width="6" stroke-linecap="round" d="M 165,260 L 165,340" />
    <path stroke="#FF0000" stroke-width="5" stroke-linecap="round" fill="none" d="M 145,265 Q 165,280 175,260" />"""

    f1 = f"""{floor}
    <circle cx="90" cy="340" r="26" stroke="#111111" stroke-width="8" fill="none" />
    <path stroke="#111111" stroke-width="16" stroke-linecap="round" d="M 114,355 L 290,370" />
    <path stroke="#111111" stroke-width="12" stroke-linecap="round" stroke-linejoin="round" fill="none" d="M 140,360 L {195 + hand_offset},310 L {170 + hand_offset},420" />
    <path stroke="#111111" stroke-width="8" stroke-linecap="round" d="M {155 + hand_offset},420 L {180 + hand_offset},420" />
    <path stroke="#111111" stroke-width="14" stroke-linecap="round" d="M 290,370 L 420,395" />
    <path stroke="#111111" stroke-width="10" stroke-linecap="round" d="M 420,395 L 435,420" />
    <path stroke="#FF0000" stroke-width="8" stroke-linecap="round" d="M 145,355 L {190 + hand_offset},315" />
    <path stroke="#FF0000" stroke-width="6" stroke-linecap="round" fill="#FF0000" d="M 125,370 Q 148,385 160,370 Z" />"""

    return svg_wrap(f0), svg_wrap(f1)

def get_dip_vectors(name):
    # Parallel bars or bench
    if "Bench" in name:
        app = '<rect x="300" y="270" width="130" height="150" stroke="#888888" stroke-width="6" fill="none" /><path stroke="#888888" stroke-width="6" d="M 40,420 L 472,420" />'
        f0 = f"""{app}
        <circle cx="270" cy="180" r="24" stroke="#111111" stroke-width="8" fill="none" />
        <path stroke="#111111" stroke-width="16" stroke-linecap="round" d="M 270,205 L 270,320" />
        <path stroke="#111111" stroke-width="12" stroke-linecap="round" d="M 270,230 L 310,270" />
        <path stroke="#111111" stroke-width="14" stroke-linecap="round" d="M 270,320 L 220,360 L 160,420" />
        <path stroke="#FF0000" stroke-width="6" stroke-linecap="round" d="M 285,240 L 305,265" />"""
        
        f1 = f"""{app}
        <circle cx="270" cy="250" r="24" stroke="#111111" stroke-width="8" fill="none" />
        <path stroke="#111111" stroke-width="16" stroke-linecap="round" d="M 270,275 L 270,370" />
        <path stroke="#111111" stroke-width="12" stroke-linecap="round" stroke-linejoin="round" fill="none" d="M 270,285 L 295,235 L 310,270" />
        <path stroke="#111111" stroke-width="14" stroke-linecap="round" d="M 270,370 L 210,390 L 160,420" />
        <path stroke="#FF0000" stroke-width="8" stroke-linecap="round" d="M 272,285 L 292,240" />"""
    else:
        # Parallel bar / ring dip
        bars = '<path stroke="#888888" stroke-width="8" stroke-linecap="round" d="M 160,250 L 160,450" /><path stroke="#888888" stroke-width="8" stroke-linecap="round" d="M 352,250 L 352,450" /><path stroke="#888888" stroke-width="8" stroke-linecap="round" d="M 120,250 L 392,250" />'
        f0 = f"""{bars}
        <!-- Head -->
        <circle cx="256" cy="110" r="24" stroke="#111111" stroke-width="8" fill="none" />
        <!-- Arms locked out in support -->
        <path stroke="#111111" stroke-width="12" stroke-linecap="round" d="M 230,135 L 200,250" />
        <path stroke="#111111" stroke-width="12" stroke-linecap="round" d="M 282,135 L 312,250" />
        <!-- Torso upright -->
        <path stroke="#111111" stroke-width="16" stroke-linecap="round" d="M 256,135 L 256,280" />
        <!-- Legs bent at knees -->
        <path stroke="#111111" stroke-width="14" stroke-linecap="round" stroke-linejoin="round" fill="none" d="M 256,280 L 246,360 L 290,400" />
        <path stroke="#FF0000" stroke-width="6" stroke-linecap="round" d="M 215,170 L 205,230" /><path stroke="#FF0000" stroke-width="6" stroke-linecap="round" d="M 297,170 L 307,230" />"""

        f1 = f"""{bars}
        <!-- Head lowered, chest leaned forward -->
        <circle cx="256" cy="180" r="24" stroke="#111111" stroke-width="8" fill="none" />
        <!-- Arms bent at 90 deg dip -->
        <path stroke="#111111" stroke-width="12" stroke-linecap="round" stroke-linejoin="round" fill="none" d="M 235,195 L 175,180 L 200,250" />
        <path stroke="#111111" stroke-width="12" stroke-linecap="round" stroke-linejoin="round" fill="none" d="M 277,195 L 337,180 L 312,250" />
        <!-- Torso lowered -->
        <path stroke="#111111" stroke-width="16" stroke-linecap="round" d="M 256,195 L 256,330" />
        <path stroke="#111111" stroke-width="14" stroke-linecap="round" stroke-linejoin="round" fill="none" d="M 256,330 L 246,400 L 290,430" />
        <!-- Chest and triceps peak activation -->
        <path stroke="#FF0000" stroke-width="8" stroke-linecap="round" fill="#FF0000" d="M 240,210 Q 256,225 272,210 Z" />
        <path stroke="#FF0000" stroke-width="8" stroke-linecap="round" d="M 225,195 L 180,185" />
        <path stroke="#FF0000" stroke-width="8" stroke-linecap="round" d="M 287,195 L 332,185" />"""

    return svg_wrap(f0), svg_wrap(f1)

def get_squat_vectors(name):
    floor = '<path stroke="#888888" stroke-width="6" stroke-linecap="round" d="M 60,450 L 452,450" />'
    
    # Frame 0: Standing upright
    f0 = f"""{floor}
    <circle cx="256" cy="100" r="24" stroke="#111111" stroke-width="8" fill="none" />
    <path stroke="#111111" stroke-width="16" stroke-linecap="round" d="M 256,125 L 256,260" />
    <path stroke="#111111" stroke-width="12" stroke-linecap="round" d="M 256,150 L 220,230" /><path stroke="#111111" stroke-width="12" stroke-linecap="round" d="M 256,150 L 292,230" />
    <path stroke="#111111" stroke-width="14" stroke-linecap="round" d="M 256,260 L 235,445" />
    <path stroke="#111111" stroke-width="14" stroke-linecap="round" d="M 256,260 L 277,445" />
    <path stroke="#FF0000" stroke-width="6" stroke-linecap="round" d="M 238,280 L 236,360" /><path stroke="#FF0000" stroke-width="6" stroke-linecap="round" d="M 274,280 L 276,360" />"""

    # Frame 1: Deep parallel squat
    f1 = f"""{floor}
    <circle cx="210" cy="210" r="24" stroke="#111111" stroke-width="8" fill="none" />
    <!-- Torso angled forward -->
    <path stroke="#111111" stroke-width="16" stroke-linecap="round" d="M 225,230 L 285,320" />
    <!-- Arms reaching forward for balance -->
    <path stroke="#111111" stroke-width="12" stroke-linecap="round" d="M 230,240 L 140,240" />
    <!-- Thigh parallel to floor, knee flexed -->
    <path stroke="#111111" stroke-width="14" stroke-linecap="round" stroke-linejoin="round" fill="none" d="M 285,320 L 190,320 L 210,445" />
    <!-- Quads and glutes peak activation -->
    <path stroke="#FF0000" stroke-width="8" stroke-linecap="round" d="M 280,315 L 195,315" />
    <path stroke="#FF0000" stroke-width="8" stroke-linecap="round" fill="#FF0000" d="M 275,320 Q 295,340 280,350 Z" />"""

    return svg_wrap(f0), svg_wrap(f1)

def get_handstand_vectors(name):
    floor = '<path stroke="#888888" stroke-width="6" stroke-linecap="round" d="M 60,450 L 452,450" />'
    
    # Frame 0: Handstand lockout
    f0 = f"""{floor}
    <!-- Hands on floor -->
    <path stroke="#111111" stroke-width="8" stroke-linecap="round" d="M 200,450 L 230,450" />
    <path stroke="#111111" stroke-width="8" stroke-linecap="round" d="M 282,450 L 312,450" />
    <!-- Arms straight vertical -->
    <path stroke="#111111" stroke-width="12" stroke-linecap="round" d="M 215,450 L 235,320" />
    <path stroke="#111111" stroke-width="12" stroke-linecap="round" d="M 297,450 L 277,320" />
    <!-- Head between shoulders -->
    <circle cx="256" cy="345" r="24" stroke="#111111" stroke-width="8" fill="none" />
    <!-- Torso inverted straight -->
    <path stroke="#111111" stroke-width="16" stroke-linecap="round" d="M 256,320 L 256,180" />
    <!-- Legs vertical straight up -->
    <path stroke="#111111" stroke-width="14" stroke-linecap="round" d="M 256,180 L 256,60" />
    <path stroke="#FF0000" stroke-width="6" stroke-linecap="round" d="M 230,340 L 220,400" /><path stroke="#FF0000" stroke-width="6" stroke-linecap="round" d="M 282,340 L 292,400" />"""

    # Frame 1: HSPU bottom / bent arms (head hovering above floor)
    f1 = f"""{floor}
    <path stroke="#111111" stroke-width="8" stroke-linecap="round" d="M 190,450 L 220,450" />
    <path stroke="#111111" stroke-width="8" stroke-linecap="round" d="M 292,450 L 322,450" />
    <!-- Arms bent at elbows in tripod -->
    <path stroke="#111111" stroke-width="12" stroke-linecap="round" stroke-linejoin="round" fill="none" d="M 205,450 L 165,370 L 230,370" />
    <path stroke="#111111" stroke-width="12" stroke-linecap="round" stroke-linejoin="round" fill="none" d="M 307,450 L 347,370 L 282,370" />
    <!-- Head hovering at floor line -->
    <circle cx="256" cy="415" r="24" stroke="#111111" stroke-width="8" fill="none" />
    <!-- Torso inverted -->
    <path stroke="#111111" stroke-width="16" stroke-linecap="round" d="M 256,370 L 256,230" />
    <!-- Legs vertical straight up -->
    <path stroke="#111111" stroke-width="14" stroke-linecap="round" d="M 256,230 L 256,90" />
    <path stroke="#FF0000" stroke-width="8" stroke-linecap="round" d="M 225,370 L 175,375" /><path stroke="#FF0000" stroke-width="8" stroke-linecap="round" d="M 287,370 L 337,375" />
    <path stroke="#FF0000" stroke-width="8" stroke-linecap="round" d="M 180,380 L 200,430" /><path stroke="#FF0000" stroke-width="8" stroke-linecap="round" d="M 332,380 L 312,430" />"""

    return svg_wrap(f0), svg_wrap(f1)

def get_core_plank_vectors(name):
    floor = '<path stroke="#888888" stroke-width="6" stroke-linecap="round" d="M 40,420 L 472,420" />'
    
    if "Side" in name:
        f0 = f"""{floor}
        <circle cx="140" cy="270" r="24" stroke="#111111" stroke-width="8" fill="none" />
        <path stroke="#111111" stroke-width="16" stroke-linecap="round" d="M 160,285 L 310,340" />
        <path stroke="#111111" stroke-width="12" stroke-linecap="round" d="M 180,290 L 180,420" />
        <path stroke="#111111" stroke-width="14" stroke-linecap="round" d="M 310,340 L 430,420" />
        <path stroke="#FF0000" stroke-width="6" stroke-linecap="round" d="M 200,320 L 280,350" />"""
        f1 = f0 # Isometric hold
    elif "Reverse" in name:
        f0 = f"""{floor}
        <circle cx="400" cy="270" r="24" stroke="#111111" stroke-width="8" fill="none" />
        <!-- Torso facing upwards towards ceiling -->
        <path stroke="#111111" stroke-width="16" stroke-linecap="round" d="M 380,290 L 240,335" />
        <path stroke="#111111" stroke-width="12" stroke-linecap="round" d="M 360,295 L 360,420" />
        <path stroke="#111111" stroke-width="14" stroke-linecap="round" d="M 240,335 L 120,420" />
        <path stroke="#FF0000" stroke-width="8" stroke-linecap="round" d="M 250,330 L 370,290" />"""
        f1 = f0
    elif "L-Sit" in name:
        bars = '<path stroke="#888888" stroke-width="8" stroke-linecap="round" d="M 170,260 L 170,450" /><path stroke="#888888" stroke-width="8" stroke-linecap="round" d="M 130,260 L 230,260" />'
        f0 = f"""{bars}
        <circle cx="210" cy="140" r="24" stroke="#111111" stroke-width="8" fill="none" />
        <!-- Torso upright -->
        <path stroke="#111111" stroke-width="16" stroke-linecap="round" d="M 210,165 L 210,290" />
        <path stroke="#111111" stroke-width="12" stroke-linecap="round" d="M 210,190 L 180,260" />
        <!-- Tuck L-Sit setup -->
        <path stroke="#111111" stroke-width="14" stroke-linecap="round" stroke-linejoin="round" fill="none" d="M 210,290 L 260,290 L 260,360" />
        <path stroke="#FF0000" stroke-width="6" stroke-linecap="round" d="M 215,220 L 215,280" />"""
        
        f1 = f"""{bars}
        <circle cx="210" cy="140" r="24" stroke="#111111" stroke-width="8" fill="none" />
        <path stroke="#111111" stroke-width="16" stroke-linecap="round" d="M 210,165 L 210,290" />
        <path stroke="#111111" stroke-width="12" stroke-linecap="round" d="M 210,190 L 180,260" />
        <!-- Full L-Sit locked at 90 degrees -->
        <path stroke="#111111" stroke-width="14" stroke-linecap="round" d="M 210,290 L 390,290" />
        <path stroke="#FF0000" stroke-width="8" stroke-linecap="round" d="M 218,210 L 218,285" />
        <path stroke="#FF0000" stroke-width="8" stroke-linecap="round" d="M 220,285 L 300,285" />"""
    elif "Hollow" in name:
        f0 = f"""{floor}
        <circle cx="100" cy="360" r="22" stroke="#111111" stroke-width="8" fill="none" />
        <path stroke="#111111" stroke-width="16" stroke-linecap="round" d="M 120,365 L 250,380 L 380,365" />
        <path stroke="#111111" stroke-width="12" stroke-linecap="round" d="M 130,365 L 70,350" />
        <path stroke="#111111" stroke-width="14" stroke-linecap="round" d="M 380,365 L 440,350" />
        <path stroke="#FF0000" stroke-width="8" stroke-linecap="round" d="M 180,375 L 300,375" />"""
        f1 = f0
    else:
        # Standard Plank
        f0 = f"""{floor}
        <circle cx="110" cy="285" r="24" stroke="#111111" stroke-width="8" fill="none" />
        <path stroke="#111111" stroke-width="16" stroke-linecap="round" d="M 134,300 L 290,340" />
        <!-- Forearms on floor -->
        <path stroke="#111111" stroke-width="12" stroke-linecap="round" stroke-linejoin="round" fill="none" d="M 160,310 L 160,420 L 195,420" />
        <path stroke="#111111" stroke-width="14" stroke-linecap="round" d="M 290,340 L 420,400" />
        <path stroke="#111111" stroke-width="10" stroke-linecap="round" d="M 420,400 L 440,420" />
        <path stroke="#FF0000" stroke-width="8" stroke-linecap="round" d="M 180,325 L 270,345" />"""
        f1 = f0

    return svg_wrap(f0), svg_wrap(f1)

def get_generic_calisthenics_vectors(name, categories):
    floor = '<path stroke="#888888" stroke-width="6" stroke-linecap="round" d="M 50,430 L 462,430" />'
    
    # Primary working muscle based on category
    target_muscle = '<path stroke="#FF0000" stroke-width="8" stroke-linecap="round" d="M 240,210 L 270,210" />'
    if "LEGS" in categories:
        return get_squat_vectors(name)
    elif "BACK" in categories:
        return get_pull_up_vectors(name)
    elif "CHEST" in categories:
        return get_push_up_vectors(name)
    elif "ABS" in categories:
        return get_core_plank_vectors(name)
    elif "SHOULDERS" in categories:
        return get_handstand_vectors(name)
    else:
        # Balanced athletic stance
        f0 = f"""{floor}
        <circle cx="256" cy="140" r="26" stroke="#111111" stroke-width="8" fill="none" />
        <path stroke="#111111" stroke-width="16" stroke-linecap="round" d="M 256,166 L 256,290" />
        <path stroke="#111111" stroke-width="12" stroke-linecap="round" d="M 256,180 L 190,260" /><path stroke="#111111" stroke-width="12" stroke-linecap="round" d="M 256,180 L 322,260" />
        <path stroke="#111111" stroke-width="14" stroke-linecap="round" d="M 256,290 L 220,425" /><path stroke="#111111" stroke-width="14" stroke-linecap="round" d="M 256,290 L 292,425" />
        {target_muscle}"""
        
        f1 = f"""{floor}
        <circle cx="256" cy="180" r="26" stroke="#111111" stroke-width="8" fill="none" />
        <path stroke="#111111" stroke-width="16" stroke-linecap="round" d="M 256,206 L 256,320" />
        <path stroke="#111111" stroke-width="12" stroke-linecap="round" stroke-linejoin="round" fill="none" d="M 256,215 L 180,215 L 180,280" />
        <path stroke="#111111" stroke-width="12" stroke-linecap="round" stroke-linejoin="round" fill="none" d="M 256,215 L 332,215 L 332,280" />
        <path stroke="#111111" stroke-width="14" stroke-linecap="round" stroke-linejoin="round" fill="none" d="M 256,320 L 210,360 L 210,425" />
        <path stroke="#111111" stroke-width="14" stroke-linecap="round" stroke-linejoin="round" fill="none" d="M 256,320 L 302,360 L 302,425" />
        <path stroke="#FF0000" stroke-width="10" stroke-linecap="round" fill="#FF0000" d="M 240,240 Q 256,260 272,240 Z" />"""
        return svg_wrap(f0), svg_wrap(f1)

def dispatch_vector(name, categories):
    lower = name.lower()
    if "dip" in lower:
        return get_dip_vectors(name)
    elif "push-up" in lower or "push up" in lower:
        return get_push_up_vectors(name)
    elif "pull-up" in lower or "pull up" in lower or "chin-up" in lower or "chin up" in lower or "hang" in lower:
        return get_pull_up_vectors(name)
    elif "squat" in lower or "lunge" in lower or "calf" in lower or "deadlift" in lower:
        return get_squat_vectors(name)
    elif "handstand" in lower or "press" in lower:
        return get_handstand_vectors(name)
    elif "plank" in lower or "l-sit" in lower or "hollow" in lower or "lever" in lower:
        return get_core_plank_vectors(name)
    else:
        return get_generic_calisthenics_vectors(name, categories)

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
        
        # Get image IDs
        cursor.execute("SELECT imageId, orderIndex FROM ExerciseImage WHERE exerciseId = ? ORDER BY orderIndex", (eid,))
        images = cursor.fetchall()
        if not images:
            continue
            
        f0_svg, f1_svg = dispatch_vector(name, cats)
        
        img0_id = images[0][0]
        f0_path = os.path.join(OUTPUT_DIR, f"{img0_id}.svg")
        with open(f0_path, "w", encoding="utf-8") as f:
            f.write(f0_svg)
            
        if len(images) > 1:
            img1_id = images[1][0]
            f1_path = os.path.join(OUTPUT_DIR, f"{img1_id}.svg")
            with open(f1_path, "w", encoding="utf-8") as f:
                f.write(f1_svg)
        else:
            img1_id = None
            
        count += 2
        
    conn.close()
    print(f"Successfully generated {count} theme-aware SVG illustrations in {OUTPUT_DIR}!")

if __name__ == "__main__":
    main()
