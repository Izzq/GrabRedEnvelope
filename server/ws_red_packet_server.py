#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import asyncio
import websockets
import json
import subprocess

CLIENTS = set()

async def handler(ws):
    CLIENTS.add(ws)
    addr = ws.remote_address
    print(f"📱 Android 已连接: {addr}")
    try:
        async for msg in ws:
            data = json.loads(msg)
            if data.get("action") == "red_packet":
                info = data.get("info")
                print(f"💰 红包提醒: {info}")
                # 可选择自动抢
                await broadcast({"action":"tap_text","text":"红包"})
    except websockets.ConnectionClosed:
        pass
    finally:
        CLIENTS.remove(ws)
        print(f"❌ Android 断开: {addr}")

async def broadcast(data: dict):
    if not CLIENTS:
        print("⚠️ 没有 Android 客户端连接")
        return
    msg = json.dumps(data, ensure_ascii=False)
    await asyncio.gather(*[ws.send(msg) for ws in CLIENTS])
    print(f"➡️ 广播指令: {msg}")

async def console_input():
    print("💡 可输入指令:")
    print("tap_text 红包")
    print("back")
    print("swipe 500 1200 500 400")
    print("exit")
    loop = asyncio.get_event_loop()
    while True:
        line = await loop.run_in_executor(None, input, ">>> ")
        parts = line.strip().split()
        if not parts:
            continue
        cmd = parts[0].lower()
        if cmd == "exit":
            print("退出服务器")
            for ws in list(CLIENTS):
                await ws.close()
            break
        elif cmd == "tap_text" and len(parts) > 1:
            text = " ".join(parts[1:])
            await broadcast({"action":"tap_text","text":text})
        elif cmd == "back":
            await broadcast({"action":"back"})
        elif cmd == "swipe" and len(parts) == 5:
            try:
                x1, y1, x2, y2 = map(int, parts[1:])
                await broadcast({"action":"swipe","from":[x1,y1],"to":[x2,y2]})
            except ValueError:
                print("❌ swipe 参数必须是整数")
        else:
            print("❌ 无效指令")

async def main():
    async with websockets.serve(handler, "0.0.0.0", 8765):
        print("✅ WebSocket Server 已启动: ws://0.0.0.0:8765")
        await console_input()  # 主线程阻塞

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\n❌ 服务器已停止")
