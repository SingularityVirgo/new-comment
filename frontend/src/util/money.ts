/** 后端金额单位为分 */
export function fenToYuan(fen: number): string {
  return (fen / 100).toFixed(2);
}
