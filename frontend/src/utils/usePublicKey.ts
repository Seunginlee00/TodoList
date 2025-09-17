// src/composables/usePublicKey.ts
import { ref, computed, onMounted, onUnmounted, Ref, ComputedRef } from "vue";
import axios, { AxiosError } from "axios";
import { API_SERVER_HOST } from "@/api/hostApi";

interface JwtPubKeyResponse {
    publicKey: string;
    token: string;
    expiresIn?: number; // 서버에서 제공할 수도 있고 안 줄 수도 있음
}

const publicKey: Ref<string> = ref("");
const jwtToken: Ref<string> = ref("");
const expiresAt: Ref<number> = ref(0); // epoch(ms)
const loading: Ref<boolean> = ref(false);
const error: Ref<AxiosError | Error | null> = ref(null);

const nowTs: Ref<number> = ref(Date.now());
let tickTimer: number | null = null;
let refreshTimer: number | null = null;
let inflight: Promise<{ publicKey: string; jwtToken: string }> | null = null;
let lastUserId: string = "";

/** JWT exp(sec) → epoch(ms) */
function expMsFromJwt(token: string): number | null {
    try {
        const payload = JSON.parse(atob(token.split(".")[1]));
        return typeof payload.exp === "number" ? payload.exp * 1000 : null;
    } catch {
        return null;
    }
}

function startTick(): void {
    if (tickTimer) clearInterval(tickTimer);
    tickTimer = window.setInterval(() => {
        nowTs.value = Date.now();
    }, 1000);
}

function scheduleRefresh(): void {
    if (refreshTimer) clearTimeout(refreshTimer);
    const prefetchMs = Math.max(0, expiresAt.value - Date.now() - 30_000); // 만료 30초 전
    refreshTimer = window.setTimeout(() => {
        // 조용히 재발급 시도 (에러는 다음 ensureFresh에서 처리)
        fetchKey(lastUserId).catch(() => {});
    }, prefetchMs);
}

/** 실제 키/토큰 발급 호출 (중복 호출 자동 병합) */
async function fetchKey(
    userId?: string
): Promise<{ publicKey: string; jwtToken: string }> {
    if (inflight) return inflight;

    loading.value = true;
    error.value = null;
    lastUserId = userId ?? lastUserId ?? "";

    const params = lastUserId ? { userId: lastUserId } : undefined;

    inflight = axios
        .get<JwtPubKeyResponse>(`${API_SERVER_HOST}/api/jwt-pub-key`, { params })
        .then(({ data }) => {
            publicKey.value = data.publicKey;
            jwtToken.value = data.token;

            const jwtExpMs = expMsFromJwt(jwtToken.value);
            if (jwtExpMs) {
                expiresAt.value = jwtExpMs;
            } else if (typeof data.expiresIn === "number") {
                expiresAt.value = Date.now() + data.expiresIn * 1000;
            } else {
                // 서버가 expiresIn을 안 주는 경우 기본 5분
                expiresAt.value = Date.now() + 300_000;
            }

            scheduleRefresh();
            return { publicKey: publicKey.value, jwtToken: jwtToken.value };
        })
        .catch((e: AxiosError | Error) => {
            error.value = e;
            throw e;
        })
        .finally(() => {
            loading.value = false;
            inflight = null;
        });

    return inflight;
}

/** 제출 직전 신선도 보장: 5초 이하 남았거나 토큰 없음 → 즉시 재발급 */
async function ensureFresh(
    userId?: string
): Promise<{ publicKey: string; jwtToken: string }> {
    if (!jwtToken.value || Date.now() + 5_000 >= expiresAt.value) {
        return fetchKey(userId);
    }
    return { publicKey: publicKey.value, jwtToken: jwtToken.value };
}

/** 수동 재발급 버튼 등에서 사용 */
function refreshNow(userId?: string) {
    return fetchKey(userId);
}

/** 첫 마운트 시 자동 시작 */
function startAuto(userId?: string): void {
    startTick();
    if (!jwtToken.value) fetchKey(userId);
    else scheduleRefresh();
}

function stopAuto(): void {
    if (tickTimer) clearInterval(tickTimer);
    if (refreshTimer) clearTimeout(refreshTimer);
    tickTimer = null;
    refreshTimer = null;
}

const remainingSec: ComputedRef<number> = computed(() =>
    Math.max(0, Math.floor((expiresAt.value - nowTs.value) / 1000))
);
const isExpired: ComputedRef<boolean> = computed(
    () => remainingSec.value <= 0
);

export function usePublicKey() {
    onMounted(() => startAuto());
    onUnmounted(() => stopAuto());

    return {
        publicKey,
        jwtToken,
        remainingSec,
        isExpired,
        loading,
        error,
        expiresAt,
        ensureFresh,
        refreshNow,
        startAuto,
        stopAuto,
    };
}

export default usePublicKey;
