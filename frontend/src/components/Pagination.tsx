interface Props {
  page: number;
  totalPages: number;
  onChange: (page: number) => void;
}

export default function Pagination({ page, totalPages, onChange }: Props) {
  if (totalPages <= 1) return null;
  return (
    <div className="pagination">
      <button disabled={page === 0} onClick={() => onChange(page - 1)}>Anterior</button>
      <span>Página {page + 1} de {totalPages}</span>
      <button disabled={page + 1 >= totalPages} onClick={() => onChange(page + 1)}>Próxima</button>
    </div>
  );
}
