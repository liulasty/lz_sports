import argparse
import datetime
import os
import sys

def generate_migration_file(purpose, output_dir="."):
    timestamp = datetime.datetime.now().strftime("%Y%m%d%H%M%S")
    # Sanitize purpose for filename
    safe_purpose = "".join(c if c.isalnum() else "_" for c in purpose).strip("_")
    filename = f"{timestamp}_{safe_purpose}.sql"

    # Ensure output directory exists
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    filepath = os.path.join(output_dir, filename)

    content = f"""/*
 * Migration: {timestamp}_{safe_purpose}
 * Date: {datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")}
 * Purpose: {purpose}
 *
 * Operations:
 * [ ] Add
 * [ ] Delete
 * [ ] Adjust
 *
 * Description:
 * (Please describe the specific changes here)
 */

-- Write your SQL commands below this line
-- ---------------------------------------

"""

    with open(filepath, "w", encoding="utf-8") as f:
        f.write(content)

    print(f"Generated migration file: {filepath}")
    return filepath

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate a standardized SQL migration file.")
    parser.add_argument("--purpose", required=True, help="Short description of the database change")
    parser.add_argument("--dir", default=".", help="Directory to save the file")

    args = parser.parse_args()

    generate_migration_file(args.purpose, args.dir)
