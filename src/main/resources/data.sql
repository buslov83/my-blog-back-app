INSERT INTO posts (title, text, image, image_content_type, likes_count, tags)
VALUES (
    'Введение в Spring Framework',
    'Spring Framework — это мощный Java-фреймворк для создания enterprise-приложений. Он предоставляет инверсию управления (IoC) и внедрение зависимостей (DI), что упрощает разработку и тестирование кода.',
    NULL,
    NULL,
    3,
    NULL
);

INSERT INTO posts (title, text, image, image_content_type, likes_count, tags)
VALUES (
    'Работа с Hibernate ORM',
    'Hibernate — популярный ORM-фреймворк для Java, который позволяет работать с реляционными базами данных через объектно-ориентированную модель. Основные концепции: Session, Transaction и HQL.',
    NULL,
    NULL,
    1,
    ' hibernate java orm '
);

INSERT INTO posts (title, text, image, image_content_type, likes_count, tags)
VALUES (
    'Паттерны проектирования в Java',
    'Паттерны проектирования — это типовые решения часто встречающихся проблем при проектировании программ. В Java широко применяются паттерны: Singleton, Factory, Builder, Strategy и Observer.',
    NULL,
    NULL,
    7,
    ' design java oop patterns '
);

INSERT INTO comments (text, post_id) VALUES ('Отличная статья, очень доступно объяснено!', 1);
INSERT INTO comments (text, post_id) VALUES ('Спасибо, давно искал хорошее введение в Spring.', 1);
INSERT INTO comments (text, post_id) VALUES ('Полезный материал про Hibernate, жду продолжения.', 2);
