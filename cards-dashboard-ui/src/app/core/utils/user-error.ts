/** Turns API / HTTP failures into short messages suitable for customers (not engineers). */
export function toUserMessage(err: unknown, fallback: string): string {
  const raw =
    (err as { error?: { message?: string; code?: string }; message?: string })?.error?.message ||
    (err as { message?: string })?.message ||
    '';

  const text = String(raw).toLowerCase();
  if (!text) {
    return fallback;
  }
  if (text.includes('x-channel-id') || text.includes('x-client-id') || text.includes('bff_00')) {
    return 'We could not reach your account safely. Please sign out, sign back in, and try again.';
  }
  if (text.includes('unauthorized') || text.includes('401') || text.includes('jwt') || text.includes('token')) {
    return 'Your session expired. Please sign in again.';
  }
  if (text.includes('forbidden') || text.includes('403')) {
    return 'You do not have access to this information.';
  }
  if (text.includes('network') || text.includes('failed to fetch') || text.includes('0 unknown')) {
    return 'Connection problem. Check that you are online and try again.';
  }
  // Keep short, non-technical responses only — never surface raw header / stack jargon.
  if (text.includes('header') || text.includes('correlation') || text.includes('flyway') || text.includes('sql')) {
    return fallback;
  }
  if (raw.length <= 120 && !/[_{}]/.test(raw) && !raw.includes('Exception')) {
    return raw;
  }
  return fallback;
}
