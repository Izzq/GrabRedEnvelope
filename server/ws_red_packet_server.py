#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import asyncio
import websockets
import json
import subprocess
import time
from datetime import datetime

CLIENTS = set()
HEARTBEAT_INTERVAL = 10  # 心跳间隔，单位：秒
LAST_HEARTBEAT = {}  # 存储每个客户端的最后心跳时间

async def handler(ws):
    CLIENTS.add(ws)
    addr = ws.remote_address
    print(f"📱 Android 已连接: {addr}")
    try:
        # 每次连接时初始化该客户端的心跳时间
        LAST_HEARTBEAT[ws] = time.time()

        async for msg in ws:
            data = json.loads(msg)
            if data.get("action") == "red_packet":
                info = data.get("info")
                print(f"💰 红包提醒: {info}")
                # 可选择自动抢
                await broadcast({"action":"tap_text","text":"红包"})
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
async def broadcast(data: dict):
    if not CLIENTS:
        print("⚠️ 没有 Android 客户端连接")
        return
    msg = json.dumps(data, ensure_ascii=False)
    try:
        await asyncio.gather(*[ws.send(msg) for ws in CLIENTS])
        print(f"➡️ 广播指令: {msg}")
    except websockets.ConnectionClosed as e:
        print(f"❌ 广播失败，连接已关闭: {e}")
    except Exception as e:
        print(f"❌ 广播失败: {e}")

# 处理命令行输入
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
