-- Включаем расширение для генерации UUID, если еще не включено
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==========================================
-- 1. ENUM TYPES (Перечисления)
-- ==========================================

-- Категории вещей
CREATE TYPE category_enum AS ENUM (
    'HEADWEAR',   -- Головной убор
    'TOP',        -- Верх
    'BOTTOM',     -- Низ
    'SHOES',      -- Обувь
    'OUTERWEAR',  -- Верхняя одежда
    'ACCESSORIES' -- Аксессуары
);

-- Сезонность
CREATE TYPE season_enum AS ENUM (
    'WINTER',
    'SPRING',
    'SUMMER',
    'AUTUMN',
    'ALL_SEASONS'
);

-- ==========================================
-- 2. TABLES (Таблицы)
-- ==========================================

-- Таблица пользователей
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    avatar_url VARCHAR(2048),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Таблица настроек пользователя
CREATE TABLE user_settings (
    user_id UUID PRIMARY KEY, -- PK является также FK
    theme VARCHAR(20) DEFAULT 'LIGHT',
    language VARCHAR(10) DEFAULT 'RU',
    notifications_enabled BOOLEAN DEFAULT TRUE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_settings_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE -- Если удаляется юзер, удаляются и настройки
);

-- Таблица вещей
CREATE TABLE items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(2048),
    category category_enum NOT NULL,
    season season_enum,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_items_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE -- При удалении юзера удаляются все его вещи
);

-- Таблица образов (Outfits)
CREATE TABLE outfits (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    season season_enum,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_outfits_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE -- При удалении юзера удаляются все его образы
);

-- Таблица слотов образа (Outfit Slots)
CREATE TABLE outfit_slots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    outfit_id UUID NOT NULL,
    item_id UUID, -- Слот может быть пустым (шаблон образа)
    slot_type category_enum NOT NULL,

    CONSTRAINT fk_slots_outfit 
        FOREIGN KEY (outfit_id) 
        REFERENCES outfits(id) 
        ON DELETE CASCADE, -- При удалении образа удаляются его слоты
        
    CONSTRAINT fk_slots_item 
        FOREIGN KEY (item_id) 
        REFERENCES items(id) 
        ON DELETE SET NULL -- При удалении вещи слот не удаляется, а обнуляется.
                           -- Образ сохраняется как структура, но без конкретной вещи.
);

-- ==========================================
-- 3. INDEXES (Индексы)
-- ==========================================

CREATE INDEX idx_items_user_id ON items(user_id);
CREATE INDEX idx_outfits_user_id ON outfits(user_id);
CREATE INDEX idx_slots_outfit_id ON outfit_slots(outfit_id);
CREATE INDEX idx_slots_item_id ON outfit_slots(item_id);

-- ==========================================
-- 4. TRIGGERS (Триггеры для updated_at)
-- ==========================================

-- Функция для обновления timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = NOW();
   RETURN NEW;
END;
$$ language 'plpgsql';

-- Функция проверки на совпадение категорий вещи и слота
CREATE OR REPLACE FUNCTION check_slot_item_category()
RETURNS TRIGGER AS $$
DECLARE
    item_cat category_enum;
BEGIN
    -- Если item_id не NULL, проверяем его категорию
    IF NEW.item_id IS NOT NULL THEN
        SELECT category INTO item_cat FROM items WHERE id = NEW.item_id;
        
        -- Если категория вещи не совпадает с типом слота — ошибка
        IF item_cat != NEW.slot_type THEN
            RAISE EXCEPTION 'Item category (%) does not match slot type (%)', item_cat, NEW.slot_type;
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Функция запрета смены категории, если вещь используется
CREATE OR REPLACE FUNCTION check_item_category_change()
RETURNS TRIGGER AS $$
BEGIN
    -- Если категория меняется
    IF OLD.category != NEW.category THEN
        -- Проверяем, используется ли эта вещь где-то
        IF EXISTS (SELECT 1 FROM outfit_slots WHERE item_id = OLD.id) THEN
            RAISE EXCEPTION 'Cannot change category of item "%". It is currently used in an outfit.', OLD.name;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Применяем триггеры ко всем таблицам, где есть updated_at
CREATE TRIGGER update_users_modtime BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_settings_modtime BEFORE UPDATE ON user_settings FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_items_modtime BEFORE UPDATE ON items FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_outfits_modtime BEFORE UPDATE ON outfits FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Применяем триггер к таблице слотов
CREATE TRIGGER ensure_slot_category_match
    BEFORE INSERT OR UPDATE ON outfit_slots
    FOR EACH ROW
    EXECUTE FUNCTION check_slot_item_category();

-- Применяем триггер к таблице items
CREATE TRIGGER prevent_category_change_if_used
    BEFORE UPDATE ON items
    FOR EACH ROW
    EXECUTE FUNCTION check_item_category_change();