type Variant = 'default' | 'list' | 'article' | 'shop';

function Bar({ w }: { w: string }) {
  return <span className="skel-bar" style={{ width: w }} />;
}

export function PageSkeleton({ variant = 'default' }: { variant?: Variant }) {
  if (variant === 'list') {
    return (
      <div className="skel-root" aria-busy aria-label="加载中">
        {[0, 1, 2].map((i) => (
          <div key={i} className="skel-card">
            <div className="skel-row">
              <span className="skel-avatar" />
              <div className="skel-col" style={{ flex: 1 }}>
                <Bar w="72%" />
                <Bar w="40%" />
              </div>
              <span className="skel-thumb" />
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (variant === 'article') {
    return (
      <div className="skel-root" aria-busy aria-label="加载中">
        <div className="skel-card skel-article">
          <div className="skel-row" style={{ marginBottom: 16 }}>
            <span className="skel-avatar" />
            <div className="skel-col" style={{ flex: 1 }}>
              <Bar w="50%" />
              <Bar w="30%" />
            </div>
          </div>
          <Bar w="88%" />
          <Bar w="76%" />
          <Bar w="64%" />
          <div className="skel-block" style={{ marginTop: 18 }} />
        </div>
      </div>
    );
  }

  if (variant === 'shop') {
    return (
      <div className="skel-root" aria-busy aria-label="加载中">
        <div className="skel-card">
          <Bar w="60%" />
          <Bar w="90%" />
          <div className="skel-row" style={{ marginTop: 14, gap: 8 }}>
            <span className="skel-pill" />
            <span className="skel-pill" />
          </div>
        </div>
        <div className="skel-hero" />
        <div className="skel-card">
          <Bar w="40%" />
          <Bar w="70%" />
        </div>
      </div>
    );
  }

  return (
    <div className="skel-root" aria-busy aria-label="加载中">
      <div className="skel-card">
        <Bar w="55%" />
        <Bar w="80%" />
      </div>
    </div>
  );
}
