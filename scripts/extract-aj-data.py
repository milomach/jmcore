import os
import re
import shutil

# **THIS SCRIPT IS NOT INTENDED TO BE A LONG-TERM SOLUTION AND WILL BE REPLACED WITH THE .json EXPORT AS SOON AS IT IS SUPPORTED**
# Because of this, there has not been particular care in making sure it is written consistently, concisely, etc.

# Main output folder
OUTPUT_DIR = "aj_data"

# Subfolders for rig data and indexes
RIG_DIR = os.path.join(OUTPUT_DIR, "rig")
INDEX_DIR = os.path.join(OUTPUT_DIR, "index")

# Root of Animated Java functions
AJ_FUNC_ROOT = os.path.join("data", "animated_java", "function")

FRAME_FILE_RE = re.compile(r"^(\d+)\.mcfunction$")

DATA_MERGE_RE = re.compile(
    r"\$?data merge entity ([^\s]+) \{transformation: \[([^\]]+)\],start_interpolation: ([^,]+),interpolation_duration: ([^\}]+)\}"
)

# Regex to match the root summon line (first item_display summon with root tags)
ROOT_SUMMON_RE = re.compile(
    r"summon\s+minecraft:item_display[^\{]*\{\s*Tags:\s*\[([^\]]+)\]", re.IGNORECASE
)

def clear_output_dir():
    if os.path.exists(OUTPUT_DIR):
        shutil.rmtree(OUTPUT_DIR)
    os.makedirs(RIG_DIR)
    os.makedirs(INDEX_DIR)

def find_export_namespaces():
    return [
        name for name in os.listdir(AJ_FUNC_ROOT)
        if os.path.isdir(os.path.join(AJ_FUNC_ROOT, name)) and name != "global"
    ]

def find_animations(namespace):
    anim_dir = os.path.join(AJ_FUNC_ROOT, namespace, "animations")
    if not os.path.isdir(anim_dir):
        return []
    return [
        name for name in os.listdir(anim_dir)
        if os.path.isdir(os.path.join(anim_dir, name))
    ]

def clean_bone_name(raw_name):
    if raw_name.startswith("$(") and raw_name.endswith(")"):
        raw_name = raw_name[2:-1]
    elif raw_name.startswith("$"):
        raw_name = raw_name[1:]
    if raw_name.startswith("bone_"):
        raw_name = raw_name[len("bone_"):]
    return raw_name

# --- Animation and default pose extraction logic ---

def process_animation(namespace, animation):
    """
    Extracts animation frames for bones, vanilla display types, and locators.
    Output format:
      <frame> bone <name> <matrix>
      <frame> item_display <name> <matrix>
      <frame> block_display <name> <matrix>
      <frame> text_display <name> <matrix>
      <frame> locator <name> <pos> <rot>
    """
    frames_dir = os.path.join(
        AJ_FUNC_ROOT, namespace, "animations", animation, "zzz", "frames"
    )
    if not os.path.isdir(frames_dir):
        return []

    lines = []
    for fname in sorted(os.listdir(frames_dir), key=lambda x: int(x.split('.')[0]) if x.split('.')[0].isdigit() else 0):
        if not FRAME_FILE_RE.match(fname):
            continue
        frame_num = fname.split('.')[0]
        frame_path = os.path.join(frames_dir, fname)
        with open(frame_path, "r") as f:
            frame_lines = f.readlines()

        # Handle bones and vanilla displays
        for line in frame_lines:
            m = DATA_MERGE_RE.search(line)
            if m:
                raw_bone_name = m.group(1)
                matrix = m.group(2).replace("f", "")
                # Determine type and name
                bone_name = clean_bone_name(raw_bone_name)
                # Check for vanilla display types by prefix
                if bone_name.startswith("item_display_"):
                    display_name = bone_name[len("item_display_"):]
                    lines.append(f"{frame_num} item_display {display_name} {matrix}")
                elif bone_name.startswith("block_display_"):
                    display_name = bone_name[len("block_display_"):]
                    lines.append(f"{frame_num} block_display {display_name} {matrix}")
                elif bone_name.startswith("text_display_"):
                    display_name = bone_name[len("text_display_"):]
                    lines.append(f"{frame_num} text_display {display_name} {matrix}")
                else:
                    lines.append(f"{frame_num} bone {bone_name} {matrix}")

        # Handle locators
        for line in frame_lines:
            if "data modify entity @s data merge value" in line and '"locators":{' in line:
                # Extract locator data
                locator_data_match = re.search(r'"locators":\{(.*)\}', line)
                if locator_data_match:
                    locator_data_str = locator_data_match.group(1)
                    # Parse each locator entry
                    locators = re.findall(
                        r'"([^"]+)":\{"px":([-\d.eE]+),"py":([-\d.eE]+),"pz":([-\d.eE]+),"ry":([-\d.eE]+),"rx":([-\d.eE]+)\}',
                        locator_data_str
                    )
                    for locator in locators:
                        locator_name, px, py, pz, ry, rx = locator
                        pos_str = f"{px},{py},{pz}"
                        rot_str = f"{ry},{rx}"
                        lines.append(f"{frame_num} locator {locator_name} {pos_str} {rot_str}")

    return lines

def extract_locator_default_poses(namespace):
    """
    Extracts the default pose for locators from the modern data pack format.

    The default pose for locators is now stored in separate files in the
    `data/animated_java/function/<namespace>/zzz/zzz/set_default_pose` directory.
    Each file is named `as_locator_<locator_name>.mcfunction` and contains a line
    of the format:
        data modify entity @s data.locators.<locator_name> merge value { px: <float>, py: <float>, pz: <float>, ry: <float>, rx: <float> }

    This function reads these files and extracts the position and rotation values
    for each locator.

    Args:
        namespace (str): The export namespace.

    Returns:
        A list of strings in the format:
        `0 locator <locator_name> <px>,<py>,<pz> <ry>,<rx>`
    """
    locators_dir = os.path.join(AJ_FUNC_ROOT, namespace, "zzz", "zzz", "set_default_pose")
    if not os.path.isdir(locators_dir):
        return []

    locator_lines = []

    # Iterate over all files in the locators directory
    for filename in os.listdir(locators_dir):
        if filename.startswith("as_locator_") and filename.endswith(".mcfunction"):
            locator_name = filename[len("as_locator_"):-len(".mcfunction")]
            file_path = os.path.join(locators_dir, filename)

            # Read the file and extract the relevant line
            with open(file_path, "r") as f:
                for line in f:
                    if "data modify entity @s data.locators." in line and "merge value" in line:
                        # Extract the position and rotation values
                        match = re.search(
                            r"data\.locators\." + re.escape(locator_name) + r"\s*merge\s*value\s*\{\s*px:\s*([-\d.eE]+),\s*py:\s*([-\d.eE]+),\s*pz:\s*([-\d.eE]+),\s*ry:\s*([-\d.eE]+),\s*rx:\s*([-\d.eE]+)\s*\}",
                            line
                        )
                        if match:
                            px, py, pz = match.group(1), match.group(2), match.group(3)
                            ry, rx = match.group(4), match.group(5)
                            pos_str = f"{px},{py},{pz}"
                            rot_str = f"{ry},{rx}"
                            locator_lines.append(f"0 locator {locator_name} {pos_str} {rot_str}")
                        break  # Only one relevant line per file

    return locator_lines

def process_default_pose(namespace):
    """
    Extracts the default pose for bones, vanilla display types, and locators.
    The output format does not include a frame number.

    The type of each entity is determined by comparing the tags in
    `apply_default_pose.mcfunction` with the tags of bones, item_displays,
    block_displays, and text_displays.

    Output format:
      bone <name> <matrix>
      item_display <name> <matrix>
      block_display <name> <matrix>
      text_display <name> <matrix>
      locator <name> <pos> <rot>
    """
    pose_file = os.path.join(AJ_FUNC_ROOT, namespace, "apply_default_pose.mcfunction")
    lines = []

    # Load references for bones and vanilla displays
    bones_dir = os.path.join(RIG_DIR, namespace, "bones")
    item_display_dir = os.path.join(RIG_DIR, namespace, "item_displays")
    block_display_dir = os.path.join(RIG_DIR, namespace, "block_displays")
    text_display_dir = os.path.join(RIG_DIR, namespace, "text_displays")

    # Create sets of tags for each type
    bone_tags = {f"aj.{namespace}.node.{os.path.splitext(f)[0]}" for f in os.listdir(bones_dir) if f.endswith(".txt")}
    item_display_tags = {f"aj.{namespace}.node.{os.path.splitext(f)[0]}" for f in os.listdir(item_display_dir) if f.endswith(".txt")}
    block_display_tags = {f"aj.{namespace}.node.{os.path.splitext(f)[0]}" for f in os.listdir(block_display_dir) if f.endswith(".txt")}
    text_display_tags = {f"aj.{namespace}.node.{os.path.splitext(f)[0]}" for f in os.listdir(text_display_dir) if f.endswith(".txt")}

    # Extract bone and vanilla display default pose
    if os.path.isfile(pose_file):
        with open(pose_file, "r") as f:
            for line in f:
                # Match lines with the transformation data
                match = re.search(
                    r"execute on passengers if entity @s\[tag=(aj\.[^.]+\.node\.[^\]]+)\] run data merge entity @s \{ transformation: \[([^\]]+)\], start_interpolation: [^\}]+ \}",
                    line
                )
                if match:
                    tag = match.group(1)  # e.g., aj.<namespace>.node.<name>
                    matrix = match.group(2).replace("f", "")  # Transformation matrix

                    # Determine the type by comparing the tag
                    if tag in bone_tags:
                        name = tag.split(".")[-1]
                        lines.append(f"bone {name} {matrix}")
                    elif tag in item_display_tags:
                        name = tag.split(".")[-1]
                        lines.append(f"item_display {name} {matrix}")
                    elif tag in block_display_tags:
                        name = tag.split(".")[-1]
                        lines.append(f"block_display {name} {matrix}")
                    elif tag in text_display_tags:
                        name = tag.split(".")[-1]
                        lines.append(f"text_display {name} {matrix}")
                    else:
                        # Default to bone if the tag is not found in any set
                        name = tag.split(".")[-1]
                        lines.append(f"bone {name} {matrix}")

    # Extract locator default pose from the modern data pack format
    locator_lines = extract_locator_default_poses(namespace)
    for l in locator_lines:
        # Remove the leading "0 " from locator lines for default_pose output.
        if l.startswith("0 "):
            lines.append(l[2:])
        else:
            lines.append(l)

    return lines

# --- End of animation/default pose logic ---

def extract_passengers_from_summon(summon_file):
    """
    Parses the Passengers array from the root summon.mcfunction file.
    Returns a list of entity dicts (raw strings parsed to dicts).
    """
    with open(summon_file, "r") as f:
        content = f.read()

    # Find Passengers:[
    passengers_start = content.find("Passengers:[")
    if passengers_start == -1:
        return []

    # Find the matching closing bracket for the Passengers array
    i = passengers_start + len("Passengers:[")
    depth = 1
    passengers_end = i
    while i < len(content):
        if content[i] == '[':
            depth += 1
        elif content[i] == ']':
            depth -= 1
            if depth == 0:
                passengers_end = i
                break
        i += 1
    passengers_str = content[passengers_start + len("Passengers:["):passengers_end]

    # Extract each entity block { ... } from the Passengers array
    entities = []
    i = 0
    while i < len(passengers_str):
        if passengers_str[i] == '{':
            start = i
            depth = 1
            i += 1
            while i < len(passengers_str) and depth > 0:
                if passengers_str[i] == '{':
                    depth += 1
                elif passengers_str[i] == '}':
                    depth -= 1
                i += 1
            entities.append(passengers_str[start:i])
        else:
            i += 1
    return entities

def extract_tags(entity_str):
    """
    Extracts tags from an entity string.
    Returns a list of tags.
    """
    tags_match = re.search(r'Tags:\[([^\]]+)\]', entity_str)
    if not tags_match:
        return []
    tags = [t.strip(" '\"") for t in tags_match.group(1).split(",") if t.strip(" '\"")]
    return tags

def extract_name_from_tags(tags, namespace, display_type):
    """
    Extracts the display name from tags of the format aj.<namespace>.<type>_display.<name>
    """
    prefix = f"aj.{namespace}.{display_type}_display."
    for tag in tags:
        if tag.startswith(prefix):
            return tag[len(prefix):]
    return None

def process_bones_and_displays(namespace, bones_dir, item_display_dir, block_display_dir, text_display_dir):
    """
    Processes bones and vanilla display entities from summon.mcfunction Passengers.
    Returns lists of relative file paths for each type.
    """
    summon_path = os.path.join(AJ_FUNC_ROOT, namespace, "summon.mcfunction")
    if not os.path.isfile(summon_path):
        return [], [], [], []

    entities = extract_passengers_from_summon(summon_path)
    bone_files = []
    item_display_files = []
    block_display_files = []
    text_display_files = []

    for entity_str in entities:
        tags = extract_tags(entity_str)
        # Ignore the data entity (has aj.global.data tag)
        if "aj.global.data" in tags:
            continue

        # Remove aj.new from tags for output
        tags = [tag for tag in tags if tag != "aj.new"]

        # Get entity type
        id_match = re.search(r'id:"([^"]+)"', entity_str)
        entity_type = id_match.group(1) if id_match else "minecraft:item_display"

        # Extract bounding box height and width
        height_match = re.search(r'height:([0-9.]+)f', entity_str)
        width_match = re.search(r'width:([0-9.]+)f', entity_str)
        height = height_match.group(1) if height_match else ""
        width = width_match.group(1) if width_match else ""

        # Bone: item_display with item_model
        item_model_match = re.search(r'"minecraft:item_model":"([^"]+)"', entity_str)
        if entity_type == "minecraft:item_display" and item_model_match:
            item_model_path = item_model_match.group(1)
            bone_name = item_model_path.split("/")[-1]
            # Extract item_id
            item_id_match = re.search(r'item:\{id:"([^"]+)"', entity_str)
            item_id = item_id_match.group(1) if item_id_match else ""
            # Write bone file
            bone_file = os.path.join(bones_dir, f"{bone_name}.txt")
            with open(bone_file, "w") as bf:
                bf.write(f"Bone Name: {bone_name}\n")
                bf.write(f"Item: {item_id}\n")
                bf.write(f"Item Model Path: {item_model_path}\n")
                bf.write(f"Bounding Box Height: {height}\n")
                bf.write(f"Bounding Box Width: {width}\n")
                bf.write("Tags:\n")
                for tag in tags:
                    bf.write(f"  - {tag}\n")
            rel_path = os.path.relpath(bone_file, os.path.join(RIG_DIR, namespace)).replace("\\", "/")
            bone_files.append(rel_path)
            continue

        # Vanilla item_display (not a bone)
        if entity_type == "minecraft:item_display":
            name = extract_name_from_tags(tags, namespace, "item")
            item_id_match = re.search(r'item:\{id:"([^"]+)"', entity_str)
            item_id = item_id_match.group(1) if item_id_match else ""
            item_display_file = os.path.join(item_display_dir, f"{name or 'unnamed'}.txt")
            with open(item_display_file, "w") as f:
                f.write(f"Item Display Name: {name or 'unnamed'}\n")
                if item_id:
                    f.write(f"Item: {item_id}\n")
                f.write(f"Bounding Box Height: {height}\n")
                f.write(f"Bounding Box Width: {width}\n")
                f.write("Tags:\n")
                for tag in tags:
                    f.write(f"  - {tag}\n")
            rel_path = os.path.relpath(item_display_file, os.path.join(RIG_DIR, namespace)).replace("\\", "/")
            item_display_files.append(rel_path)
            continue

        # Vanilla block_display
        if entity_type == "minecraft:block_display":
            name = extract_name_from_tags(tags, namespace, "block")
            block_name_match = re.search(r'block_state:\{[^}]*Name:"([^"]+)"', entity_str)
            block_name = block_name_match.group(1) if block_name_match else ""
            block_display_file = os.path.join(block_display_dir, f"{name or 'unnamed'}.txt")
            with open(block_display_file, "w") as f:
                f.write(f"Block Display Name: {name or 'unnamed'}\n")
                if block_name:
                    f.write(f"Block: {block_name}\n")
                f.write(f"Bounding Box Height: {height}\n")
                f.write(f"Bounding Box Width: {width}\n")
                f.write("Tags:\n")
                for tag in tags:
                    f.write(f"  - {tag}\n")
            rel_path = os.path.relpath(block_display_file, os.path.join(RIG_DIR, namespace)).replace("\\", "/")
            block_display_files.append(rel_path)
            continue

        # Vanilla text_display
        if entity_type == "minecraft:text_display":
            name = extract_name_from_tags(tags, namespace, "text")
            text_match = re.search(r'text:"([^"]+)"', entity_str)
            text_val = text_match.group(1) if text_match else ""
            text_display_file = os.path.join(text_display_dir, f"{name or 'unnamed'}.txt")
            with open(text_display_file, "w") as f:
                f.write(f"Text Display Name: {name or 'unnamed'}\n")
                if text_val:
                    f.write(f"Text: {text_val}\n")
                f.write(f"Bounding Box Height: {height}\n")
                f.write(f"Bounding Box Width: {width}\n")
                f.write("Tags:\n")
                for tag in tags:
                    f.write(f"  - {tag}\n")
            rel_path = os.path.relpath(text_display_file, os.path.join(RIG_DIR, namespace)).replace("\\", "/")
            text_display_files.append(rel_path)
            continue

    return bone_files, item_display_files, block_display_files, text_display_files

def process_variants(namespace, variants_dir):
    """
    Extracts variant data for a rig. For each variant, creates a file listing the variant name
    and all bones it applies to. Returns a list of relative paths to the generated files.

    The modern data pack format stores each variant in a separate folder in the
    `data/animated_java/function/<namespace>/variants` directory. Each folder contains
    an `apply.mcfunction` file that specifies the bones for the variant.

    Args:
        namespace (str): The export namespace.
        variants_dir (str): The directory where the variant files will be generated.

    Returns:
        list: A list of relative paths to the generated variant files.
    """
    variant_root = os.path.join(AJ_FUNC_ROOT, namespace, "variants")
    if not os.path.isdir(variant_root):
        return []

    variant_files = []

    # Iterate over each variant folder
    for variant_name in os.listdir(variant_root):
        variant_path = os.path.join(variant_root, variant_name)
        apply_file = os.path.join(variant_path, "apply.mcfunction")

        # Skip if the folder does not contain an apply.mcfunction file
        if not os.path.isfile(apply_file):
            continue

        # Extract the bones from the apply.mcfunction file
        bones = []
        with open(apply_file, "r") as f:
            for line in f:
                # Match lines of the format:
                # execute on passengers if entity @s[tag=aj.<namespace>.node.<name>] at @s run function ...
                match = re.search(
                    rf"execute on passengers if entity @s\[tag=aj\.{re.escape(namespace)}\.node\.([^\]]+)\]",
                    line
                )
                if match:
                    bone_name = match.group(1)
                    bones.append(bone_name)

        # Write the variant file if any bones were found
        if bones:
            variant_file = os.path.join(variants_dir, f"{variant_name}.txt")
            with open(variant_file, "w") as vf:
                vf.write(f"Variant: {variant_name}\n")
                vf.write("Bones:\n")
                for bone in sorted(bones):
                    vf.write(f"  - {bone}\n")

            # Add the relative path to the list of variant files
            rel_path = os.path.relpath(variant_file, os.path.join(RIG_DIR, namespace)).replace("\\", "/")
            variant_files.append(rel_path)

    return variant_files

def process_root(namespace, root_dir):
    """
    Extracts root tags from the root summon line in summon.mcfunction and writes them to root.txt.
    The header is 'Tags:' to match the bone files.
    """
    summon_path = os.path.join(AJ_FUNC_ROOT, namespace, "summon.mcfunction")
    if not os.path.isfile(summon_path):
        return

    with open(summon_path, "r") as f:
        for line in f:
            m = ROOT_SUMMON_RE.search(line)
            if m:
                tags_str = m.group(1)
                tags = [t.strip(" '\"") for t in tags_str.split(",") if t.strip(" '\"") and t.strip(" '\"") != "aj.new"]
                root_file = os.path.join(root_dir, "root.txt")
                with open(root_file, "w") as rf:
                    rf.write("Tags:\n")
                    for tag in tags:
                        rf.write(f"  - {tag}\n")
                break

def extract_locator_names_from_namespace(namespace):
    """
    Extracts all unique locator names for a namespace by parsing the summon.mcfunction file.
    Uses the same logic as extract_locators_from_summon, but only collects locator names.
    """
    locator_names = set()
    summon_file = os.path.join(AJ_FUNC_ROOT, namespace, "summon.mcfunction")
    if not os.path.isfile(summon_file):
        return locator_names

    with open(summon_file, "r") as f:
        content = f.read()

    # Find the start of the locators section
    start = content.find("locators:{")
    if start == -1:
        return locator_names

    # Find the full locators:{...} block (with nested braces)
    brace_start = content.find("{", start)
    depth = 1
    i = brace_start + 1
    while i < len(content) and depth > 0:
        if content[i] == '{':
            depth += 1
        elif content[i] == '}':
            depth -= 1
        i += 1
    locators_block = content[brace_start + 1 : i - 1]  # exclude outermost braces

    # Parse locator entries: locator_name:{...}, handling nested braces
    idx = 0
    while idx < len(locators_block):
        # Find locator name
        m = re.match(r'\s*([a-zA-Z0-9_]+)\s*:\s*\{', locators_block[idx:])
        if not m:
            break
        locator_name = m.group(1)
        locator_names.add(locator_name)
        idx += m.end()  # Move past locator_name:{
        # Find the matching closing brace for this locator
        brace_depth = 1
        while idx < len(locators_block) and brace_depth > 0:
            if locators_block[idx] == '{':
                brace_depth += 1
            elif locators_block[idx] == '}':
                brace_depth -= 1
            idx += 1
        # Skip comma if present
        while idx < len(locators_block) and locators_block[idx] in ', ':
            idx += 1

    return locator_names

def process_locators(namespace, locators_dir):
    """
    For the given namespace, creates a text file for each unique locator in locators_dir.
    Each file contains just the locator name.

    The names of all locators are extracted in the same way as for the locators
    in the generated default pose files.

    Args:
        namespace (str): The export namespace.
        locators_dir (str): The directory where the locator files will be generated.

    Returns:
        list: A list of relative paths to the generated locator files.
    """
    # Extract locator names using the same logic as for default pose files
    locators_dir_path = os.path.join(AJ_FUNC_ROOT, namespace, "zzz", "zzz", "set_default_pose")
    if not os.path.isdir(locators_dir_path):
        return []

    locator_files = []

    # Iterate over all files in the set_default_pose directory
    for filename in os.listdir(locators_dir_path):
        if filename.startswith("as_locator_") and filename.endswith(".mcfunction"):
            locator_name = filename[len("as_locator_"):-len(".mcfunction")]

            # Generate the locator file
            locator_file = os.path.join(locators_dir, f"{locator_name}.txt")
            with open(locator_file, "w") as lf:
                lf.write(f"Locator Name: {locator_name}\n")

            # Add the relative path to the list of locator files
            rel_path = os.path.relpath(locator_file, os.path.join(RIG_DIR, namespace)).replace("\\", "/")
            locator_files.append(rel_path)

    return locator_files

def main():
    clear_output_dir()
    namespaces = find_export_namespaces()
    animation_index_paths = []
    bone_index_paths = []
    variant_index_paths = []
    root_index_paths = []
    locator_index_paths = []
    item_display_index_paths = []
    block_display_index_paths = []
    text_display_index_paths = []
    default_pose_index_paths = []
    animated_offset_index_paths = []

    for ns in namespaces:
        ns_dir = os.path.join(RIG_DIR, ns)
        animations_dir = os.path.join(ns_dir, "animations")
        bones_dir = os.path.join(ns_dir, "bones")
        root_dir = os.path.join(ns_dir, "root")
        variants_dir = os.path.join(ns_dir, "variants")
        locators_dir = os.path.join(ns_dir, "locators")
        item_display_dir = os.path.join(ns_dir, "item_displays")
        block_display_dir = os.path.join(ns_dir, "block_displays")
        text_display_dir = os.path.join(ns_dir, "text_displays")
        default_pose_dir = os.path.join(ns_dir, "default_pose")

        os.makedirs(animations_dir, exist_ok=True)
        os.makedirs(bones_dir, exist_ok=True)
        os.makedirs(root_dir, exist_ok=True)
        os.makedirs(variants_dir, exist_ok=True)
        os.makedirs(locators_dir, exist_ok=True)
        os.makedirs(item_display_dir, exist_ok=True)
        os.makedirs(block_display_dir, exist_ok=True)
        os.makedirs(text_display_dir, exist_ok=True)
        os.makedirs(default_pose_dir, exist_ok=True)

        # Process bones and vanilla displays first to ensure their files are generated
        bone_files, item_display_files, block_display_files, text_display_files = process_bones_and_displays(
            ns, bones_dir, item_display_dir, block_display_dir, text_display_dir
        )
        for path in bone_files:
            bone_index_paths.append(f"{ns}/" + path)
        for path in item_display_files:
            item_display_index_paths.append(f"{ns}/" + path)
        for path in block_display_files:
            block_display_index_paths.append(f"{ns}/" + path)
        for path in text_display_files:
            text_display_index_paths.append(f"{ns}/" + path)

        # Process animations
        animations = find_animations(ns)
        for anim in animations:
            # process_animation(...) returns a list of textual lines for the animation
            anim_lines = process_animation(ns, anim)

            # write the animation file to aj_data/rig/<ns>/animations/<anim>.txt
            anim_file = os.path.join(animations_dir, f"{anim}.txt")
            rel_path = os.path.relpath(anim_file, RIG_DIR).replace("\\", "/")

            # Always add to the normal animation index (existing behaviour)
            animation_index_paths.append(rel_path)

            # Write file contents
            with open(anim_file, "w") as f:
                for line in anim_lines:
                    f.write(line + "\n")

            # Determine whether this animation qualifies for animated_offset_index
            # - Find all lines that are for frame 0 (start with "0 ")
            # - If there is exactly one such line and it represents a locator ("0 locator ..."),
            #   then add this animation file path to animated_offset_index_paths.
            zero_frame_lines = [L for L in anim_lines if L.startswith("0 ")]
            if len(zero_frame_lines) == 1:
                single_zero = zero_frame_lines[0]
                # Normalize whitespace at start, then check token 2 == 'locator'
                # format is "<frame> <type> <name> ..."
                parts = single_zero.strip().split()
                if len(parts) >= 3 and parts[1] == "locator":
                    # Add the same relative path used for animation index
                    animated_offset_index_paths.append(rel_path)

        # Write default_pose.txt for this namespace in the default_pose folder
        default_pose_lines = process_default_pose(ns)  # uses same logic as before
        default_pose_file = os.path.join(default_pose_dir, "default_pose.txt")
        if default_pose_lines:
            # Add path to the default_pose_index
            rel_default_pose = os.path.relpath(default_pose_file, RIG_DIR).replace("\\", "/")
            default_pose_index_paths.append(rel_default_pose)
            with open(default_pose_file, "w") as f:
                for line in default_pose_lines:
                    f.write(line + "\n")

        # Process variants and collect variant file paths for the index
        variant_files = process_variants(ns, variants_dir)
        if variant_files:
            for path in variant_files:
                variant_index_paths.append(f"{ns}/" + path)

        # Process root tags
        process_root(ns, root_dir)
        rel_root_path = os.path.relpath(os.path.join(root_dir, "root.txt"), RIG_DIR).replace("\\", "/")
        root_index_paths.append(rel_root_path)

        # Process locators and collect locator file paths for the index
        locator_files = process_locators(ns, locators_dir)
        if locator_files:
            for path in locator_files:
                locator_index_paths.append(f"{ns}/" + path)
    
    # Write index files
    with open(os.path.join(INDEX_DIR, "variant_index.txt"), "w") as vf:
        for path in variant_index_paths:
            vf.write(path + "\n")
    with open(os.path.join(INDEX_DIR, "animation_index.txt"), "w") as f:
        for path in animation_index_paths:
            f.write(path + "\n")
    with open(os.path.join(INDEX_DIR, "bone_index.txt"), "w") as bf:
        for path in bone_index_paths:
            bf.write(path + "\n")
    with open(os.path.join(INDEX_DIR, "root_index.txt"), "w") as rf:
        for path in root_index_paths:
            rf.write(path + "\n")
    with open(os.path.join(INDEX_DIR, "locator_index.txt"), "w") as lf:
        for path in locator_index_paths:
            lf.write(path + "\n")
    with open(os.path.join(INDEX_DIR, "item_display_index.txt"), "w") as idf:
        for path in item_display_index_paths:
            idf.write(path + "\n")
    with open(os.path.join(INDEX_DIR, "block_display_index.txt"), "w") as bdf:
        for path in block_display_index_paths:
            bdf.write(path + "\n")
    with open(os.path.join(INDEX_DIR, "text_display_index.txt"), "w") as tdf:
        for path in text_display_index_paths:
            tdf.write(path + "\n")
    with open(os.path.join(INDEX_DIR, "default_pose_index.txt"), "w") as dpf:
        for path in default_pose_index_paths:
            dpf.write(path + "\n")
    with open(os.path.join(INDEX_DIR, "animated_offset_index.txt"), "w") as aof:
        for path in animated_offset_index_paths:
            aof.write(path + "\n")

if __name__ == "__main__":
    main()