#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import asyncio
import websockets

async def hello():
    async with websockets.serve(handler, "0.0.0.0", 8765):
        print("✅ WebSocket Server 已启动: ws://localhost:8765")
        await asyncio.Future()  # run forever

async def handler(ws):
    async for msg in ws:
        print("⬅️ 收到:", msg)
        await ws.send("pong")

asyncio.run(hello())
