INSERT INTO categories (name) VALUES
    ('Livros'),
    ('Eletrônicos'),
    ('Papelaria');

INSERT INTO products (name, description, price, stock, category_id) VALUES
    ('Código Limpo', 'Livro sobre boas práticas de programação.', 89.90, 20,
        (SELECT id FROM categories WHERE name = 'Livros')),
    ('Entendendo Algoritmos', 'Guia ilustrado de algoritmos.', 74.50, 15,
        (SELECT id FROM categories WHERE name = 'Livros')),
    ('Teclado Mecânico', 'Teclado mecânico ABNT2 com switches marrons.', 249.00, 8,
        (SELECT id FROM categories WHERE name = 'Eletrônicos')),
    ('Mouse sem fio', 'Mouse sem fio com 1600 DPI.', 79.90, 30,
        (SELECT id FROM categories WHERE name = 'Eletrônicos')),
    ('Fone de ouvido', 'Fone over-ear com cancelamento de ruído.', 399.00, 5,
        (SELECT id FROM categories WHERE name = 'Eletrônicos')),
    ('Caderno pontilhado', 'Caderno A5 com 160 folhas pontilhadas.', 39.90, 50,
        (SELECT id FROM categories WHERE name = 'Papelaria')),
    ('Kit canetas', 'Kit com 10 canetas coloridas.', 29.90, 40,
        (SELECT id FROM categories WHERE name = 'Papelaria'));
