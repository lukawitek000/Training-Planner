#!/usr/bin/env python3
"""
Deterministically generate exercise/src/main/assets/database/exercises.db
and ensure bundled exercise images exist.
"""

import os
import sqlite3
import uuid
import shutil
import json

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, ".."))
ROOM_IDENTITY_HASH = "0c5756d49f1f59c6c2f103c95ac70a10"
DB_PATH = os.path.join(PROJECT_ROOT, "exercise/src/main/assets/database/exercises.db")
IMAGE_DIR = os.path.join(PROJECT_ROOT, "exercise/src/main/assets/exercise_images")
SEED_PATH = os.path.join(SCRIPT_DIR, "bundled_exercises_seed.json")

CATEGORIES = [
    "ABS", "BACK", "BICEPS", "CARDIO", "CHEST", "LEGS", "SHOULDERS", "STRETCHING", "TRICEPS"
]

NEW_EXERCISES = [
    {
        "name": "Parallel Bar Dip",
        "description": "Lower your body by bending your arms until your shoulders are below your elbows on parallel bars, then press back up.",
        "categories": ["CHEST", "SHOULDERS", "TRICEPS"],
        "recommendations": {
            "BEGINNER": {"sets": 3, "reps": 4, "restTime": 90, "weight": None, "time": None},
            "INTERMEDIATE": {"sets": 4, "reps": 8, "restTime": 120, "weight": None, "time": None},
            "ADVANCED": {"sets": 5, "reps": 15, "restTime": 150, "weight": None, "time": None},
        }
    },
    {
        "name": "Bench Dip",
        "description": "With hands placed behind you on a bench or chair and feet on the floor, lower your hips by bending your elbows and push back up.",
        "categories": ["CHEST", "TRICEPS"],
        "recommendations": {
            "BEGINNER": {"sets": 3, "reps": 8, "restTime": 60, "weight": None, "time": None},
            "INTERMEDIATE": {"sets": 3, "reps": 12, "restTime": 75, "weight": None, "time": None},
            "ADVANCED": {"sets": 4, "reps": 15, "restTime": 90, "weight": None, "time": None},
        }
    },
    {
        "name": "Straight Bar Dip",
        "description": "Support yourself above a single horizontal straight bar, lean slightly forward, lower your chest toward the bar, and push back up.",
        "categories": ["CHEST", "SHOULDERS", "TRICEPS"],
        "recommendations": {
            "BEGINNER": {"sets": 3, "reps": 3, "restTime": 90, "weight": None, "time": None},
            "INTERMEDIATE": {"sets": 4, "reps": 6, "restTime": 120, "weight": None, "time": None},
            "ADVANCED": {"sets": 4, "reps": 10, "restTime": 150, "weight": None, "time": None},
        }
    },
    {
        "name": "Ring Dip",
        "description": "Perform a dip on gymnastic rings, actively stabilizing the rings close to your body and turning rings out at the top of the movement.",
        "categories": ["CHEST", "SHOULDERS", "TRICEPS"],
        "recommendations": {
            "BEGINNER": {"sets": 3, "reps": 3, "restTime": 120, "weight": None, "time": None},
            "INTERMEDIATE": {"sets": 4, "reps": 6, "restTime": 150, "weight": None, "time": None},
            "ADVANCED": {"sets": 4, "reps": 10, "restTime": 180, "weight": None, "time": None},
        }
    },
    {
        "name": "Knee Push-Up",
        "description": "A regressed push-up performed with knees on the ground, maintaining a straight line from knees to shoulders.",
        "categories": ["CHEST", "TRICEPS"],
        "recommendations": {
            "BEGINNER": {"sets": 3, "reps": 6, "restTime": 60, "weight": None, "time": None},
            "INTERMEDIATE": {"sets": 3, "reps": 10, "restTime": 60, "weight": None, "time": None},
            "ADVANCED": {"sets": 4, "reps": 15, "restTime": 75, "weight": None, "time": None},
        }
    },
    {
        "name": "Active Dead Hang",
        "description": "Hang from a pull-up bar while depressing and retracting your scapulae to actively engage back, shoulder, and grip strength.",
        "categories": ["BACK", "SHOULDERS"],
        "recommendations": {
            "BEGINNER": {"sets": 3, "reps": 1, "restTime": 60, "weight": None, "time": 20},
            "INTERMEDIATE": {"sets": 3, "reps": 1, "restTime": 75, "weight": None, "time": 40},
            "ADVANCED": {"sets": 4, "reps": 1, "restTime": 90, "weight": None, "time": 60},
        }
    },
    {
        "name": "Tuck Front Lever Hold",
        "description": "Hang from a bar and pull your body horizontal to the ground with knees tucked tightly to your chest and arms straight.",
        "categories": ["ABS", "BACK"],
        "recommendations": {
            "BEGINNER": {"sets": 3, "reps": 1, "restTime": 90, "weight": None, "time": 8},
            "INTERMEDIATE": {"sets": 4, "reps": 1, "restTime": 120, "weight": None, "time": 15},
            "ADVANCED": {"sets": 4, "reps": 1, "restTime": 150, "weight": None, "time": 25},
        }
    },
    {
        "name": "Tuck Planche Hold",
        "description": "Support your body on straight arms with knees tucked tightly to chest, leaning forward to balance your weight on your hands.",
        "categories": ["ABS", "CHEST", "SHOULDERS"],
        "recommendations": {
            "BEGINNER": {"sets": 3, "reps": 1, "restTime": 90, "weight": None, "time": 6},
            "INTERMEDIATE": {"sets": 4, "reps": 1, "restTime": 120, "weight": None, "time": 12},
            "ADVANCED": {"sets": 4, "reps": 1, "restTime": 150, "weight": None, "time": 20},
        }
    },
    {
        "name": "Nordic Hamstring Curl",
        "description": "Kneel with ankles anchored and lower your torso toward the ground under eccentric control using your hamstrings.",
        "categories": ["LEGS"],
        "recommendations": {
            "BEGINNER": {"sets": 3, "reps": 3, "restTime": 90, "weight": None, "time": None},
            "INTERMEDIATE": {"sets": 4, "reps": 6, "restTime": 120, "weight": None, "time": None},
            "ADVANCED": {"sets": 4, "reps": 10, "restTime": 150, "weight": None, "time": None},
        }
    }
]

# Exercises that legitimately keep added weights
LEGIT_WEIGHTED_EXERCISES = {
    "Weighted Pull-Up",
    "Dumbbell Chest Press",
    "Dumbbell Row",
    "Dumbbell Shoulder Press",
    "Biceps Curl",
    "Heavy Barbell Biceps Curl",
    "Romanian Deadlift",
    "Face Pull",
    "Russian Twist"
}

def clean_recommendations(name, recs):
    cleaned = {}
    for level, r in recs.items():
        entry = dict(r)
        # Strip artificial weight from bodyweight exercises
        if name not in LEGIT_WEIGHTED_EXERCISES:
            entry["weight"] = None
        
        # Adjust elite exercises for realistic progression
        if name == "90° Push-Up":
            if level == "BEGINNER":
                entry["reps"] = 1
                entry["restTime"] = 150
            elif level == "INTERMEDIATE":
                entry["reps"] = 3
                entry["restTime"] = 180
            elif level == "ADVANCED":
                entry["reps"] = 5
                entry["restTime"] = 210
        elif name == "Muscle-Up":
            if level == "BEGINNER":
                entry["reps"] = 1
                entry["restTime"] = 150
            elif level == "INTERMEDIATE":
                entry["reps"] = 3
                entry["restTime"] = 180
            elif level == "ADVANCED":
                entry["reps"] = 5
                entry["restTime"] = 210
        elif name == "Handstand Push-Up":
            if level == "BEGINNER":
                entry["reps"] = 1
                entry["restTime"] = 150
            elif level == "INTERMEDIATE":
                entry["reps"] = 3
                entry["restTime"] = 180
            elif level == "ADVANCED":
                entry["reps"] = 6
                entry["restTime"] = 210
        elif name == "Planche Lean":
            if level == "BEGINNER":
                entry["time"] = 10
                entry["restTime"] = 75
            elif level == "INTERMEDIATE":
                entry["time"] = 15
                entry["restTime"] = 90
            elif level == "ADVANCED":
                entry["time"] = 20
                entry["restTime"] = 120
        elif name == "L-Sit":
            if level == "BEGINNER":
                entry["time"] = 10
                entry["restTime"] = 60
            elif level == "INTERMEDIATE":
                entry["time"] = 15
                entry["restTime"] = 75
            elif level == "ADVANCED":
                entry["time"] = 25
                entry["restTime"] = 90
                
        cleaned[level] = entry
    return cleaned

def generate():
    # 1. Load existing dump
    with open(SEED_PATH, "r") as f:
        existing_exercises = json.load(f)
        
    all_exercises = []
    
    # Process existing
    for ex in existing_exercises:
        name = ex["name"]
        cleaned_recs = clean_recommendations(name, ex["recommendations"])
        all_exercises.append({
            "id": ex["id"],
            "name": name,
            "description": ex["description"],
            "categories": ex["categories"],
            "recommendations": cleaned_recs,
            "images": ex["images"]
        })
        
    # Process new exercises
    # Pick fallback svg from existing images as placeholder for new exercises
    fallback_img_path = os.path.join(IMAGE_DIR, "c4044131-c319-50ad-a1b4-fe990f060e91.svg")
    fallback_img_path_2 = os.path.join(IMAGE_DIR, "62a05104-c4eb-5786-b360-0526f6949bac.svg")
    
    for new_ex in NEW_EXERCISES:
        name = new_ex["name"]
        ex_id = str(uuid.uuid5(uuid.NAMESPACE_DNS, f"tp:exercise:{name}"))
        img1_id = str(uuid.uuid5(uuid.NAMESPACE_DNS, f"tp:image:{name}:0"))
        img2_id = str(uuid.uuid5(uuid.NAMESPACE_DNS, f"tp:image:{name}:1"))
        
        # Ensure image files exist
        target_img1 = os.path.join(IMAGE_DIR, f"{img1_id}.svg")
        target_img2 = os.path.join(IMAGE_DIR, f"{img2_id}.svg")
        if not os.path.exists(target_img1):
            shutil.copyfile(fallback_img_path, target_img1)
        if not os.path.exists(target_img2):
            shutil.copyfile(fallback_img_path_2, target_img2)
            
        all_exercises.append({
            "id": ex_id,
            "name": name,
            "description": new_ex["description"],
            "categories": new_ex["categories"],
            "recommendations": new_ex["recommendations"],
            "images": [img1_id, img2_id]
        })
        
    print(f"Total exercises to insert: {len(all_exercises)}")
    
    # Recreate SQLite database
    if os.path.exists(DB_PATH):
        os.remove(DB_PATH)
        
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    # Schema creation
    cursor.executescript("""
    CREATE TABLE `Exercise` (
        `exerciseId` TEXT NOT NULL,
        `name` TEXT NOT NULL,
        `description` TEXT NOT NULL,
        `imageId` TEXT,
        `sourceType` TEXT NOT NULL,
        `sharing` TEXT NOT NULL,
        `revision` INTEGER NOT NULL,
        `syncState` TEXT NOT NULL,
        `ownerId` TEXT,
        `ownerName` TEXT,
        PRIMARY KEY(`exerciseId`)
    );
    CREATE TABLE `DbExerciseCategory` (
        `categoryName` TEXT NOT NULL,
        PRIMARY KEY(`categoryName`)
    );
    CREATE TABLE `ExerciseCategoryCrossRef` (
        `exerciseId` TEXT NOT NULL,
        `categoryName` TEXT NOT NULL,
        PRIMARY KEY(`exerciseId`, `categoryName`),
        FOREIGN KEY(`exerciseId`) REFERENCES `Exercise`(`exerciseId`) ON UPDATE NO ACTION ON DELETE CASCADE,
        FOREIGN KEY(`categoryName`) REFERENCES `DbExerciseCategory`(`categoryName`) ON UPDATE NO ACTION ON DELETE CASCADE
    );
    CREATE TABLE `exercise_recommendation` (
        `exerciseId` TEXT NOT NULL,
        `level` TEXT NOT NULL,
        `sets` INTEGER NOT NULL,
        `reps` INTEGER NOT NULL,
        `restTimeInSeconds` INTEGER NOT NULL,
        `weightInKg` INTEGER,
        `timeInSeconds` INTEGER,
        PRIMARY KEY(`exerciseId`, `level`),
        FOREIGN KEY(`exerciseId`) REFERENCES `Exercise`(`exerciseId`) ON UPDATE NO ACTION ON DELETE CASCADE
    );
    CREATE TABLE `SyncMetadata` (
        `key` TEXT NOT NULL,
        `value` INTEGER NOT NULL,
        PRIMARY KEY(`key`)
    );
    CREATE TABLE `ExerciseMutation` (
        `id` TEXT NOT NULL,
        `exerciseId` TEXT NOT NULL,
        `idempotencyKey` TEXT NOT NULL,
        `expectedVersion` INTEGER NOT NULL,
        `name` TEXT NOT NULL,
        `description` TEXT NOT NULL,
        `categoryNames` TEXT NOT NULL,
        `state` TEXT NOT NULL,
        `type` TEXT NOT NULL,
        `beginnerSets` INTEGER,
        `beginnerReps` INTEGER,
        `beginnerRestTime` INTEGER,
        `beginnerWeight` INTEGER,
        `beginnerTime` INTEGER,
        `intermediateSets` INTEGER,
        `intermediateReps` INTEGER,
        `intermediateRestTime` INTEGER,
        `intermediateWeight` INTEGER,
        `intermediateTime` INTEGER,
        `advancedSets` INTEGER,
        `advancedReps` INTEGER,
        `advancedRestTime` INTEGER,
        `advancedWeight` INTEGER,
        `advancedTime` INTEGER,
        `conflictServerJson` TEXT,
        `createdAt` INTEGER NOT NULL,
        PRIMARY KEY(`id`)
    );
    CREATE TABLE `ExerciseImage` (
        `exerciseId` TEXT NOT NULL,
        `imageId` TEXT NOT NULL,
        `orderIndex` INTEGER NOT NULL,
        PRIMARY KEY(`exerciseId`, `imageId`),
        FOREIGN KEY(`exerciseId`) REFERENCES `Exercise`(`exerciseId`) ON UPDATE NO ACTION ON DELETE CASCADE
    );
    CREATE TABLE room_master_table (
        id INTEGER PRIMARY KEY,
        identity_hash TEXT
    );
    """)
    
    # Insert categories
    for cat in CATEGORIES:
        cursor.execute("INSERT INTO DbExerciseCategory (categoryName) VALUES (?)", (cat,))
        
    # Insert exercises
    for ex in all_exercises:
        primary_image_id = ex["images"][0] if ex["images"] else None
        cursor.execute("""
            INSERT INTO Exercise (
                exerciseId, name, description, imageId,
                sourceType, sharing, revision, syncState, ownerId, ownerName
            ) VALUES (?, ?, ?, ?, 'BUNDLED', 'COMMUNITY', 1, 'SYNCED', NULL, NULL)
        """, (ex["id"], ex["name"], ex["description"], primary_image_id))
        
        # Categories
        for cat in ex["categories"]:
            cursor.execute("""
                INSERT INTO ExerciseCategoryCrossRef (exerciseId, categoryName)
                VALUES (?, ?)
            """, (ex["id"], cat))
            
        # Recommendations
        for lvl, r in ex["recommendations"].items():
            cursor.execute("""
                INSERT INTO exercise_recommendation (
                    exerciseId, level, sets, reps, restTimeInSeconds, weightInKg, timeInSeconds
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """, (ex["id"], lvl, r["sets"], r["reps"], r["restTime"], r["weight"], r["time"]))
            
        # Images
        for idx, img_id in enumerate(ex["images"]):
            cursor.execute("""
                INSERT INTO ExerciseImage (exerciseId, imageId, orderIndex)
                VALUES (?, ?, ?)
            """, (ex["id"], img_id, idx))
            
    # Insert room master table identity hash
    cursor.execute("INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES (42, ?)", (ROOM_IDENTITY_HASH,))
    
    conn.commit()
    conn.close()
    print("Database successfully generated at", DB_PATH)

if __name__ == "__main__":
    generate()
