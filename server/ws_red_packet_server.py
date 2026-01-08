#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import asyncio
import websockets
import json
import subprocess
import shutil
import sys
import time
import xml.etree.ElementTree as ET
from datetime import datetime

CLIENTS = set()
HEARTBEAT_INTERVAL = 15  # 心跳间隔，单位：秒
LAST_HEARTBEAT = {}  # 存储每个客户端的最后心跳时间


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


async def handler(ws):
    CLIENTS.add(ws)
    addr = ws.remote_address
    print(f"📱 Android 已连接: {addr}")
    try:
        # 每次连接时初始化该客户端的心跳时间
        LAST_HEARTBEAT[ws] = time.time()

        async for msg in ws:
            data = json.loads(msg)
            if data.get("action") == "command":
                cmd = data.get("info")
                print(f"➡️  执行命令: {cmd}")
                # 可选择自动抢
                await broadcast(cmd)
            # elif data.get("action") == "heartbeat_response":
            elif data.get("action") == "heartbeat":
                # 更新客户端的最后心跳时间
                LAST_HEARTBEAT[ws] = time.time()
                print(f"💓 收到客户端心跳")
    except websockets.ConnectionClosed as e:
        print(f"❌ 连接关闭: {e}")
    except Exception as e:
        print(f"❌ 发生了一个未知错误: {e}")
    finally:
        # 清理断开连接的客户端
        CLIENTS.remove(ws)
        del LAST_HEARTBEAT[ws]
        print(f"❌ Android 断开: {addr}")

# 定时广播心跳消息给所有客户端
async def send_heartbeat():
    while True:
        if not CLIENTS:
            # 没有客户端，直接休眠，不打印时间
            await asyncio.sleep(HEARTBEAT_INTERVAL)
            continue

        current_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        print(f"🕒 当前时间: {current_time}")  # 打印当前时间
        for ws in list(CLIENTS):
            try:
                # 检查客户端是否超时（假设超时为 60 秒）
                last_heartbeat = LAST_HEARTBEAT.get(ws, 0)
                if time.time() - last_heartbeat > 60:
                    print(f"❌ 客户端 {ws.remote_address} 超时，关闭连接")
                    await ws.close()
                    CLIENTS.remove(ws)
                    del LAST_HEARTBEAT[ws]
                else:
                    # 向客户端发送心跳消息
                    await ws.send(json.dumps({"action": "heartbeat"}))
                    print(f"➡️ 发送心跳消息到 {ws.remote_address}")
            except websockets.ConnectionClosed as e:
                print(f"❌ 发送心跳失败，连接已关闭: {e}")
                CLIENTS.remove(ws)
                del LAST_HEARTBEAT[ws]
            except Exception as e:
                print(f"❌ 发送心跳失败: {e}")
        await asyncio.sleep(HEARTBEAT_INTERVAL)


# 广播指令给所有连接的客户端
async def broadcast(data: str):
    if not CLIENTS:
        print("⚠️ 没有 Android 客户端连接")
        return

    cmd = data.lower()

    try:
        # 原本是 websocket 广播，这里改为直接 adb 执行
        adb(cmd)
        print(f"➡️ 广播指令: {cmd}")

    except websockets.ConnectionClosed as e:
        print(f"❌ 广播失败，连接已关闭: {e}")

    except Exception as e:
        print(f"❌ 广播失败: {e}")


# 处理命令行输入
async def console_input():
    print("💡 可输入指令:")
    print("tap x y")
    print("tap_text 红包")
    print("back")
    print("swipe x1 y1 x2 y2")
    print("exit")

    loop = asyncio.get_event_loop()

    while True:
        line = await loop.run_in_executor(None, input, ">>> ")
        parts = line.strip().split()

        if not parts:
            continue

        cmd = parts[0].lower()

        print(f"🔍 parts len = {len(parts)}")

        try:
            # 退出
            if cmd == "exit":
                print("👋 退出服务器")
                break

            # 返回键
            elif cmd == "back":
                adb("shell input keyevent 4")
                print("⬅️ back")

            # 点击坐标
            elif cmd == "tap" and len(parts) == 3:
                x, y = map(int, parts[1:])
                adb(f"shell input tap {x} {y}")
                print(f"👆 tap {x},{y}")

            # 根据文本点击（需要 uiautomator / Accessibility）
            elif cmd == "tap_text" and len(parts) > 1:
                text = " ".join(parts[1:])
                print(f"🔍 查找并点击文本: {text}")
                adb(f'shell uiautomator dump /sdcard/ui.xml')
                adb(f'pull /sdcard/ui.xml .')
                click_text_from_xml(text)

            # 滑动
            elif cmd == "swipe" and len(parts) == 5:
                x1, y1, x2, y2 = map(int, parts[1:])
                adb(f"shell input swipe {x1} {y1} {x2} {y2}")
                print(f"👉 swipe {x1},{y1} -> {x2},{y2}")

            else:
                print("❌ 无效指令")

        except Exception as e:
            print(f"❌ 执行失败: {e}")



def click_text_from_xml(target_text: str):
    tree = ET.parse("ui.xml")
    root = tree.getroot()

    for node in root.iter("node"):
        text = node.attrib.get("text", "")
        if target_text in text:
            bounds = node.attrib.get("bounds")
            # bounds="[x1,y1][x2,y2]"
            x1, y1, x2, y2 = map(
                int,
                bounds.replace("[", "").replace("]", ",").split(",")[:4]
            )
            x = (x1 + x2) // 2
            y = (y1 + y2) // 2
            adb(f"shell input tap {x} {y}")
            print(f"🎯 点击文本 [{target_text}] at {x},{y}")
            return

    print(f"⚠️ 未找到文本: {target_text}")


# 启动 WebSocket 服务器并定时发送心跳
async def main():
    try:
        async with websockets.serve(handler, "0.0.0.0", 8765):
            print("✅ WebSocket Server 已启动: ws://0.0.0.0:8765")
            # 启动心跳任务
            asyncio.create_task(send_heartbeat())
            await console_input()  # 主线程阻塞
    except Exception as e:
        print(f"❌ 服务器启动失败: {e}")

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\n❌ 服务器已停止")
    except Exception as e:
        print(f"❌ 发生了一个未捕获的异常: {e}")
