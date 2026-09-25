-- 초기 데이터
-- defer-datasource-initialization=true 설정에 의해 Hibernate 스키마 반영(ddl-auto) 이후에 실행됨.
-- ddl-auto=update 이므로 재기동해도 테이블/데이터가 유지될 수 있어, 재실행해도 중복 삽입되지 않도록
-- WHERE NOT EXISTS 가드를 둠.

-- 1) users (email 은 UNIQUE, password 는 현재 평문 저장 정책이라 시드도 평문)
INSERT INTO users (name, email, password, created_at, updated_at)
SELECT '홍길동', 'hong@example.com', 'password1234', NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'hong@example.com');

INSERT INTO users (name, email, password, created_at, updated_at)
SELECT '김철수', 'kim@example.com', 'password1234', NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'kim@example.com');

INSERT INTO users (name, email, password, created_at, updated_at)
SELECT '이영희', 'lee@example.com', 'password1234', NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'lee@example.com');

-- 2) menu
INSERT INTO menu (name, price, status, created_at, updated_at)
SELECT '아메리카노', 4000, 'SALE', NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE name = '아메리카노');

INSERT INTO menu (name, price, status, created_at, updated_at)
SELECT '카페라떼', 4500, 'SALE', NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE name = '카페라떼');

INSERT INTO menu (name, price, status, created_at, updated_at)
SELECT '카푸치노', 4500, 'SALE', NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE name = '카푸치노');

INSERT INTO menu (name, price, status, created_at, updated_at)
SELECT '바닐라라떼', 5000, 'SALE', NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE name = '바닐라라떼');

INSERT INTO menu (name, price, status, created_at, updated_at)
SELECT '콜드브루', 4800, 'SOLDOUT', NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE name = '콜드브루');

-- 3) user_point (users와 1:1)
INSERT INTO user_point (user_id, balance, created_at, updated_at)
SELECT u.id, 10000, NOW(), NOW()
FROM users u
WHERE u.name = '홍길동'
  AND NOT EXISTS (SELECT 1 FROM user_point up WHERE up.user_id = u.id);

INSERT INTO user_point (user_id, balance, created_at, updated_at)
SELECT u.id, 5000, NOW(), NOW()
FROM users u
WHERE u.name = '김철수'
  AND NOT EXISTS (SELECT 1 FROM user_point up WHERE up.user_id = u.id);

INSERT INTO user_point (user_id, balance, created_at, updated_at)
SELECT u.id, 0, NOW(), NOW()
FROM users u
WHERE u.name = '이영희'
  AND NOT EXISTS (SELECT 1 FROM user_point up WHERE up.user_id = u.id);
