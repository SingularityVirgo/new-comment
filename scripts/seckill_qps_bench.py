"""
Local QPS probe for POST /voucher-order/seckill/{voucherId}.
Requires: Redis (application.yaml host/password), app on 8081, pip install redis httpx.
Uses a synthetic voucher id so only Redis Lua + id worker run; DB async path may log errors.
"""
from __future__ import annotations

import asyncio
import json
import os
import random
import time

import httpx
import redis

VOUCHER_ID = int(os.environ.get("BENCH_VOUCHER_ID", "888001"))
REDIS_URL = os.environ.get("BENCH_REDIS_URL", "redis://:123456@localhost:6379/0")
BASE = os.environ.get("BENCH_BASE", "http://127.0.0.1:8081")
DURATION = float(os.environ.get("BENCH_SECONDS", "15"))
CONCURRENCY = int(os.environ.get("BENCH_CONCURRENCY", "256"))
USER_COUNT = int(os.environ.get("BENCH_USERS", "8000"))


def prepare_redis(r: redis.Redis) -> None:
    stock = int(os.environ.get("BENCH_STOCK", "2000000"))
    r.set(f"voucher:stock:{VOUCHER_ID}", str(stock))
    r.delete(f"voucher:order:{VOUCHER_ID}")
    pipe = r.pipeline(transaction=False)
    base_id = 300_000
    for i in range(USER_COUNT):
        tok = f"bench{i}"
        key = f"login:token:{tok}"
        uid = str(base_id + i)
        pipe.hset(
            key,
            mapping={"id": uid, "phone": "1", "nickName": "b", "icon": ""},
        )
        pipe.expire(key, 86400)
    pipe.execute()


async def run() -> None:
    r = redis.Redis.from_url(REDIS_URL, decode_responses=True)
    print(f"Seeding {USER_COUNT} login sessions + stock for voucher {VOUCHER_ID} ...")
    t0 = time.perf_counter()
    prepare_redis(r)
    print(f"Redis seed done in {time.perf_counter() - t0:.2f}s")

    url = f"{BASE.rstrip('/')}/voucher-order/seckill/{VOUCHER_ID}"
    tokens = [f"bench{i}" for i in range(USER_COUNT)]

    ok = fail = 0
    lock = asyncio.Lock()

    async def worker(_idx: int, client: httpx.AsyncClient, stop: float) -> None:
        nonlocal ok, fail
        while time.perf_counter() < stop:
            tok = tokens[random.randrange(USER_COUNT)]
            try:
                resp = await client.post(url, headers={"authorization": tok})
                body = resp.text
                if resp.status_code == 200:
                    data = json.loads(body)
                    if data.get("success") is True:
                        async with lock:
                            ok += 1
                    else:
                        async with lock:
                            fail += 1
                else:
                    async with lock:
                        fail += 1
            except Exception:
                async with lock:
                    fail += 1

    stop = time.perf_counter() + DURATION
    limits = httpx.Limits(max_connections=CONCURRENCY + 50, max_keepalive_connections=CONCURRENCY + 50)
    async with httpx.AsyncClient(limits=limits, timeout=httpx.Timeout(60.0)) as client:
        tasks = [asyncio.create_task(worker(i, client, stop)) for i in range(CONCURRENCY)]
        wall0 = time.perf_counter()
        await asyncio.gather(*tasks)
        wall = time.perf_counter() - wall0

    total = ok + fail
    print(f"Duration (wall): {wall:.2f}s  concurrency={CONCURRENCY}  users={USER_COUNT}")
    print(f"HTTP attempts (approx): {total}  success={ok}  non_success={fail}")
    print(f"Successful QPS (success only): {ok / wall:.1f}")
    print(f"Throughput (all completed): {total / wall:.1f} req/s")


if __name__ == "__main__":
    asyncio.run(run())
