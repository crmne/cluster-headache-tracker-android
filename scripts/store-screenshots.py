#!/usr/bin/env python3
"""Captures the Google Play screenshots from a running emulator.

The app must be a debug build pointed at a local Rails app that holds the
synthetic `screenshots` account (never real data):

    # in the Rails app
    DEMO_USERNAME=screenshots DEMO_ONGOING_ATTACK=1 DEMO_TIME_ZONE=Europe/Berlin \\
      DEMO_PASSWORD=... bin/rails demo:reset
    bin/rails server -p 3078

    # here
    ./gradlew assembleDebug -PbaseUrl=http://localhost:3078
    adb -s emulator-5554 reverse tcp:3078 tcp:3078
    adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk
    DEMO_PASSWORD=... scripts/store-screenshots.py emulator-5554 phoneScreenshots

Screenshots land in fastlane/metadata/android/en-US/images/<kind>/. The status
bar is put in demo mode (full battery, no notifications) first, its clock set to
the device's time so it agrees with the times on screen.
"""

import os
import re
import subprocess
import sys
import time

PACKAGE = "me.paolino.clusterheadachetracker"
OPEN_PATH = f"{PACKAGE}.action.OPEN_PATH"


class Device:
    def __init__(self, serial):
        self.serial = serial

    def adb(self, *args, capture=False):
        command = ["adb", "-s", self.serial, *args]
        if capture:
            return subprocess.run(command, check=True, capture_output=True).stdout
        subprocess.run(command, check=True, stdout=subprocess.DEVNULL)

    def shell(self, *args):
        self.adb("shell", *args)

    def nodes(self):
        self.shell("uiautomator", "dump", "/sdcard/ui.xml")
        xml = self.adb("exec-out", "cat", "/sdcard/ui.xml", capture=True).decode()
        for node in re.findall(r"<node [^>]*>", xml):
            text = re.search(r' text="([^"]*)"', node).group(1)
            description = re.search(r' content-desc="([^"]*)"', node).group(1)
            bounds = [int(n) for n in re.findall(r"\d+", re.search(r'bounds="([^"]*)"', node).group(1))]
            kind = re.search(r' class="([^"]*)"', node).group(1)
            yield text or description, bounds, kind

    def tap_label(self, label):
        for text, (left, top, right, bottom), _ in self.nodes():
            if text == label:
                self.shell("input", "tap", str((left + right) // 2), str((top + bottom) // 2))
                return
        raise RuntimeError(f"Nothing labelled {label!r} on screen")

    def has_label(self, label):
        return any(text == label for text, _, _ in self.nodes())

    def wait_for(self, *labels, timeout=90):
        deadline = time.time() + timeout
        while time.time() < deadline:
            texts = {text for text, _, _ in self.nodes()}
            for label in labels:
                if label in texts:
                    return label
            time.sleep(2)
        raise RuntimeError(f"None of {labels} appeared")

    def sign_in(self, username, password):
        fields = [bounds for _, bounds, kind in self.nodes() if kind == "android.widget.EditText"]
        for (left, top, right, bottom), value in zip(fields, (username, password)):
            self.shell("input", "tap", str((left + right) // 2), str((top + bottom) // 2))
            time.sleep(1.5)
            self.shell("input", "text", value)
            time.sleep(0.5)
        self.shell("input", "keyevent", "66")  # Enter submits the form

    def open_path(self, path):
        self.shell("am", "start", "-a", OPEN_PATH, "-n", f"{PACKAGE}/.MainActivity", "--es", "path", path)

    def back(self):
        self.shell("input", "keyevent", "4")

    def demo_status_bar(self):
        self.shell("settings", "put", "global", "sysui_demo_allowed", "1")
        for command in (
            ["enter"],
            ["clock", "-e", "hhmm", self.adb("shell", "date", "+%H%M", capture=True).decode().strip()],
            ["battery", "-e", "level", "100", "-e", "plugged", "false"],
            ["network", "-e", "wifi", "show", "-e", "level", "4", "-e", "fully", "true"],
            ["network", "-e", "mobile", "hide"],
            ["notifications", "-e", "visible", "false"],
        ):
            self.shell("am", "broadcast", "-a", "com.android.systemui.demo", "-e", "command", *command)

    def screen_size(self):
        size = self.adb("shell", "wm", "size", capture=True).decode().split(":")[-1].strip()
        return tuple(int(n) for n in size.split("x"))

    def scroll(self, times):
        """Scrolls the page by whole steps from the same spot, so every run frames the same content."""
        width, height = self.screen_size()
        for _ in range(times):
            self.shell("input", "swipe", str(width // 2), str(height * 78 // 100), str(width // 2), str(height * 29 // 100), "700")
            time.sleep(2)

    def night_mode(self, on):
        self.shell("cmd", "uimode", "night", "yes" if on else "no")

    def capture(self, path):
        # The attack timer rolls its digits on every second; shoot between two ticks.
        time.sleep(1.55 - time.time() % 1)
        with open(path, "wb") as file:
            file.write(self.adb("exec-out", "screencap", "-p", capture=True))
        print(path)


def settle(seconds=6):
    time.sleep(seconds)


def main():
    serial, kind = sys.argv[1], sys.argv[2]
    password = os.environ["DEMO_PASSWORD"]
    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    out = os.path.join(root, "fastlane", "metadata", "android", "en-US", "images", kind)
    os.makedirs(out, exist_ok=True)

    device = Device(serial)
    device.demo_status_bar()
    device.night_mode(False)
    device.shell("am", "force-stop", PACKAGE)
    device.shell("am", "start", "-n", f"{PACKAGE}/.MainActivity")
    if device.wait_for("Sign in", "Headache Logs") == "Sign in":
        device.sign_in(os.environ.get("DEMO_USERNAME", "screenshots"), password)
        device.wait_for("Headache Logs")
    settle()

    device.open_path("/current_attack")
    settle(8)
    device.capture(os.path.join(out, "1_current_attack.png"))
    device.back()
    settle(3)

    device.tap_label("Logs")
    settle()
    device.scroll(2)
    device.capture(os.path.join(out, "2_logs.png"))

    device.tap_label("Charts")
    settle(10)
    device.scroll(4)
    device.capture(os.path.join(out, "3_charts.png"))
    device.scroll(3)
    device.capture(os.path.join(out, "4_treatment.png"))

    device.tap_label("New")
    settle(8)
    device.capture(os.path.join(out, "5_new_log.png"))
    device.back()
    settle(3)

    device.tap_label("Logs")
    device.open_path("/timeline")
    settle(8)
    device.capture(os.path.join(out, "6_timeline.png"))
    device.back()
    settle(3)

    device.night_mode(True)
    settle(8)
    device.capture(os.path.join(out, "7_logs_dark.png"))
    device.night_mode(False)


if __name__ == "__main__":
    main()
