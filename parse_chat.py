import re
import json
from collections import defaultdict
from datetime import datetime

# Define file path
CHAT_FILE = "app/src/main/res/raw/chat.txt"

# Regex patterns
DATE_PATTERN = r"^(\d{4}/\d{2}/\d{2}), (\d{1,2}:\d{2})\s?([ap]m) - (.*?): (.*)"
MEDIA_OMITTED = "<Media omitted>"

# Keywords
NAUGHTY_WORDS = ["cum", "vagina", "dick", "hoohaa", "sugar cane", "sex", "the deed", "make love", "kissing", "kiss", "sleeping together"]
ANNOYANCE_WORDS = ["k", "whatever", "nvm", "fine"]
FIRST_SIGNS = ["i like you", "gaining feelings", "love you partially", "love you a little bit"]
LOVE_WORDS = ["i love you", "love u", "lov u", "luv u"]
ROUTINE_WORDS = {
    "poop": ["poop"],
    "cook": ["cook", "cooking"],
    "eat": ["eat", "eating", "food"],
    "pee": ["pee"],
    "sleep": ["sleep", "sleeping", "bed"],
    "heading_out": ["heading out", "going out", "leaving"],
    "going_somewhere": ["going somewhere"]
}
COMPARISON_PHRASES = {
    "how_are_you": ["how are you", "how r u", "how are u"],
    "sorry": ["sorry", "apologies"],
    "good_morning": ["good morning", "morning"],
    "good_night": ["good night", "night"]
}

# Stats containers
stats = {
    "lefa": {"msgs": 0, "words": 0, "media": 0, "love_count": 0, "naughty": 0, "annoyed": 0, "consecutive_max": 0},
    "owami": {"msgs": 0, "words": 0, "media": 0, "love_count": 0, "naughty": 0, "annoyed": 0, "consecutive_max": 0},
    "routine": {k: {"lefa": 0, "owami": 0} for k in COMPARISON_PHRASES.keys()}, # Re-using routine structure for comparisons
    "actions": {k: {"lefa": 0, "owami": 0} for k in ROUTINE_WORDS.keys()},
    "first_signs": [],
    "total_emojis": 0
}

def count_emojis(text):
    # Simple emoji counting (non-ASCII characters in certain ranges)
    # This is an approximation
    count = 0
    for char in text:
        if ord(char) > 0xFFFF:
            count += 1
    return count

def parse_chat():
    current_sender = None
    consecutive_count = 0

    with open(CHAT_FILE, "r", encoding="utf-8") as f:
        lines = f.readlines()

    for line in lines:
        match = re.match(DATE_PATTERN, line)
        if match:
            date_str, time, ampm, sender_raw, message = match.groups()

            # Normalize sender
            if "lefa" in sender_raw.lower():
                sender = "lefa"
            elif "owami" in sender_raw.lower():
                sender = "owami"
            else:
                continue # Skip system messages

            # Update consecutive
            if sender == current_sender:
                consecutive_count += 1
            else:
                if current_sender:
                    stats[current_sender]["consecutive_max"] = max(stats[current_sender]["consecutive_max"], consecutive_count)
                current_sender = sender
                consecutive_count = 1

            # Basic Stats
            stats[sender]["msgs"] += 1
            stats[sender]["words"] += len(message.split())
            if MEDIA_OMITTED in message:
                stats[sender]["media"] += 1

            stats["total_emojis"] += count_emojis(message)

            msg_lower = message.lower()

            # Naughty
            if any(w in msg_lower for w in NAUGHTY_WORDS):
                stats[sender]["naughty"] += 1

            # Annoyance
            # Check for exact matches for short words like "k"
            words = msg_lower.split()
            if "k" in words or any(w in msg_lower for w in ANNOYANCE_WORDS if w != "k"):
                 stats[sender]["annoyed"] += 1

            # Love
            if any(w in msg_lower for w in LOVE_WORDS):
                stats[sender]["love_count"] += 1

            # First Signs (Store date and message)
            for sign in FIRST_SIGNS:
                if sign in msg_lower:
                    stats["first_signs"].append({"date": date_str, "sender": sender, "phrase": sign, "message": message[:50] + "..."})

            # Comparison Phrases (How are you, etc)
            for key, phrases in COMPARISON_PHRASES.items():
                if any(p in msg_lower for p in phrases):
                    stats["routine"][key][sender] += 1

            # Routine Actions
            for key, phrases in ROUTINE_WORDS.items():
                if any(p in msg_lower for p in phrases):
                    stats["actions"][key][sender] += 1

        else:
            # Handle multiline messages (attributed to previous sender)
            if current_sender:
                stats[current_sender]["words"] += len(line.split())
                # Re-check keywords if needed, but keeping it simple for now

    # Final consecutive check
    if current_sender:
        stats[current_sender]["consecutive_max"] = max(stats[current_sender]["consecutive_max"], consecutive_count)

    print(json.dumps(stats, indent=4))

if __name__ == "__main__":
    parse_chat()
