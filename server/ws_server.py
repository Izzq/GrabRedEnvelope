#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import asyncio
import websockets
import json

CLIENTS = set()  # 保存已连接客户端

async def handler(ws):
    """处理客户端连接"""
    CLIENTS.add(ws)
    addr = ws.remote_address
    print(f"📱 Android 已连接: {addr}")
    try:
        async for msg in ws:
            print(f"⬅️ [{addr}] 收到消息: {msg}")
    except websockets.ConnectionClosed:
        pass
    finally:
        CLIENTS.remove(ws)
        print(f"❌ Android 断开: {addr}")

async def broadcast(data: dict):
    """广播指令到所有客户端"""
    if not CLIENTS:
        print("⚠️ 没有 Android 客户端连接")
        return
    msg = json.dumps(data, ensure_ascii=False)
    await asyncio.gather(*[ws.send(msg) for ws in CLIENTS])
    print(f"➡️ 广播指令: {msg}")

async def console_input():
    """异步命令行输入控制客户端"""
    print("💡 可输入指令格式:")
    print("tap_text 测试按钮")
    print("back")
    print("swipe 500 1200 500 400")
    print("exit")
    loop = asyncio.get_event_loop()
    while True:
        line = await loop.run_in_executor(None, input, ">>> ")
        parts = line.strip().split()
        if not parts:
            continue
        cmd_type = parts[0]
        if cmd_type == "exit":
            print("退出服务器")
            for ws in list(CLIENTS):
                await ws.close()
            break
        elif cmd_type == "tap_text" and len(parts) > 1:
            text = " ".join(parts[1:])
            await broadcast({"action": "tap_text", "text": text})
        elif cmd_type == "back":
            await broadcast({"action": "back"})
        elif cmd_type == "swipe" and len(parts) == 5:
            try:
                from_x, from_y, to_x, to_y = map(int, parts[1:])
                await broadcast({
                    "action": "swipe",
                    "from": [from_x, from_y],
                    "to": [to_x, to_y]
                })
            except ValueError:
                print("❌ swipe 参数必须是整数: swipe x1 y1 x2 y2")
        else:
            print("❌ 无效指令")

async def main():
    # 启动 WebSocket Server
    server = await websockets.serve(handler, "0.0.0.0", 8765)
    print("✅ WebSocket Server 已启动: ws://0.0.0.0:8765")
    # 并行执行 server 和 console 输入
    await asyncio.gather(
        server.wait_closed(),
        console_input()
    )

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\n❌ 服务器已停止")
