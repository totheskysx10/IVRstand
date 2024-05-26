CREATE TABLE categories (
                            category_id SERIAL PRIMARY KEY,
                            category_title TEXT NOT NULL,
                            parent_category_id BIGINT,
                            cat_gif_preview VARCHAR,
                            cat_gif_link VARCHAR,
                            cat_main_icon_link VARCHAR,
                            FOREIGN KEY (parent_category_id) REFERENCES categories(category_id)
);

CREATE TABLE items (
                       item_id SERIAL PRIMARY KEY,
                       item_title TEXT NOT NULL,
                       item_description TEXT NOT NULL,
                       gif_preview VARCHAR,
                       gif_link VARCHAR,
                       main_icon_link VARCHAR,
                       category_id BIGINT REFERENCES categories(category_id)
);

CREATE TABLE additions (
                           addition_id SERIAL PRIMARY KEY,
                           addition_title TEXT NOT NULL,
                           addition_description TEXT NOT NULL,
                           addition_gif_preview VARCHAR,
                           addition_gif_link VARCHAR,
                           addition_main_icon_link VARCHAR,
                           item_id BIGINT NOT NULL REFERENCES items(item_id)
);

CREATE TABLE notification_chats (
                                    id SERIAL PRIMARY KEY,
                                    notification_chat_id VARCHAR NOT NULL,
                                    notification_category VARCHAR NOT NULL
);

CREATE TABLE item_icons (
                            id SERIAL PRIMARY KEY,
                            item_id BIGINT REFERENCES items (item_id),
                            icon_link TEXT NOT NULL
);

CREATE TABLE addition_icons (
                                id SERIAL PRIMARY KEY,
                                addition_id BIGINT REFERENCES additions (addition_id),
                                icon_link TEXT NOT NULL
);
