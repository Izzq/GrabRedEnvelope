# -*- coding: utf-8 -*-

import subprocess
import shutil
import sys

ADB_PATH = shutil.which("adb")  # 自动找 adb

if not ADB_PATH:
    print("❌ adb 未找到，请确认已安装 Android SDK 并加入 PATH")
    sys.exit(1)


def adb(cmd: str) -> str:
    """
    执行 adb 命令
    """
    full_cmd = [ADB_PATH] + cmd.split()

    result = subprocess.run(
        full_cmd,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        encoding="utf-8"
    )

    if result.returncode != 0:
        raise RuntimeError(f"ADB 执行失败:\n{result.stderr}")

    return result.stdout.strip()


if __name__ == "__main__":
    print("📱 当前连接设备：")
    print(adb("devices"))

#     print("\n🏠 发送 HOME 键")
#     adb("shell input keyevent 3")

    print("\n🏠 发送 BACK 键")
    adb("shell input keyevent 4")

#     print(adb("logcat"))
    print(adb("shell getprop ro.build.version.release"))
