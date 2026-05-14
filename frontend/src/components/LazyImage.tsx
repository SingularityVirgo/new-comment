import { useCallback, useState, type CSSProperties } from 'react';

type Props = {
  src: string;
  alt: string;
  className?: string;
  style?: CSSProperties;
  width?: number;
  height?: number;
};

/**
 * loading="lazy" + 模糊占位过渡（LQIP 思路），优化 LCP/感知性能。
 */
export function LazyImage({ src, alt, className, style, width, height }: Props) {
  const [loaded, setLoaded] = useState(false);
  const onLoad = useCallback(() => setLoaded(true), []);

  if (!src) return null;

  return (
    <img
      src={src}
      alt={alt}
      width={width}
      height={height}
      className={`lazy-img${loaded ? ' lazy-img--loaded' : ''}${className ? ` ${className}` : ''}`}
      style={style}
      loading="lazy"
      decoding="async"
      draggable={false}
      onLoad={onLoad}
    />
  );
}
