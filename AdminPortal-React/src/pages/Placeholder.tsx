/**
 * Stub for routes owned by other parallel migration sessions
 * (userAccount / appointment / transactions). Replaced as those slices land.
 */
export function Placeholder({ title }: { title: string }) {
  return (
    <div style={{ padding: '20px 0' }}>
      <h2 className="clean-font">{title}</h2>
      <p className="clean-font">This page is being migrated in another session.</p>
    </div>
  );
}
