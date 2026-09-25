#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PACKAGE_NAME="sk.ziacik.nearly"

usage() {
	echo "Usage: $0 <mobile|wear> [adb-serial]" >&2
	exit 2
}

MODULE="${1:-}"
[[ "$MODULE" == "mobile" || "$MODULE" == "wear" ]] || usage
shift

REQUESTED_SERIAL="${1:-}"
APK="$ROOT_DIR/$MODULE/build/outputs/apk/debug/$MODULE-debug.apk"

command -v adb >/dev/null 2>&1 || {
	echo "adb not found in PATH" >&2
	exit 1
}

ALL_DEVICES=()
while IFS= read -r line; do
	[[ -z "$line" || "$line" == "List of devices attached" ]] && continue
	if [[ "$line" =~ ^(.*[^[:space:]])[[:space:]]+device([[:space:]].*)?$ ]]; then
		ALL_DEVICES+=("${BASH_REMATCH[1]}")
	fi
done < <(adb devices -l)

if (( ${#ALL_DEVICES[@]} == 0 )); then
	echo "No ADB devices connected." >&2
	exit 1
fi

CANDIDATES=()
for serial in "${ALL_DEVICES[@]}"; do
	characteristics="$(adb -s "$serial" shell getprop ro.build.characteristics 2>/dev/null | tr -d '\r' || true)"
	if [[ "$MODULE" == "wear" ]]; then
		[[ "$characteristics" == *watch* ]] && CANDIDATES+=("$serial")
	else
		[[ "$characteristics" != *watch* ]] && CANDIDATES+=("$serial")
	fi
done

if (( ${#CANDIDATES[@]} == 0 )); then
	echo "No suitable $MODULE device found." >&2
	echo "Connected devices:" >&2
	adb devices -l >&2
	exit 1
fi

if [[ -n "$REQUESTED_SERIAL" ]]; then
	SERIAL=""
	for candidate in "${CANDIDATES[@]}"; do
		if [[ "$candidate" == "$REQUESTED_SERIAL" ]]; then
			SERIAL="$candidate"
			break
		fi
	done
	if [[ -z "$SERIAL" ]]; then
		echo "Device '$REQUESTED_SERIAL' is not a suitable $MODULE target." >&2
		exit 1
	fi
elif (( ${#CANDIDATES[@]} == 1 )); then
	SERIAL="${CANDIDATES[0]}"
else
	LABELS=()
	for serial in "${CANDIDATES[@]}"; do
		model="$(adb -s "$serial" shell getprop ro.product.model 2>/dev/null | tr -d '\r' || true)"
		product="$(adb -s "$serial" shell getprop ro.product.name 2>/dev/null | tr -d '\r' || true)"
		label=""
		[[ -n "$model" ]] && label+="model:$model"
		if [[ -n "$product" ]]; then
			[[ -n "$label" ]] && label+="  "
			label+="product:$product"
		fi
		[[ -n "$label" ]] && label+="  "
		label+="$serial"
		LABELS+=("$label")
	done

	echo "Choose $MODULE target:"
	PS3="Select target: "
	select label in "${LABELS[@]}"; do
		if [[ -n "$label" ]]; then
			SERIAL="${CANDIDATES[REPLY - 1]}"
			break
		fi
		echo "Invalid selection." >&2
	done
fi

echo "Building $MODULE..."
(
	cd "$ROOT_DIR"
	bash ./gradlew ":$MODULE:assembleDebug"
)

[[ -f "$APK" ]] || {
	echo "APK not found: $APK" >&2
	exit 1
}

echo "Installing on $SERIAL..."
adb -s "$SERIAL" install -r "$APK"

echo "Launching $PACKAGE_NAME..."
adb -s "$SERIAL" shell am start -n "$PACKAGE_NAME/.MainActivity" >/dev/null

echo "Done."
