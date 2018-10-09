
CREATE TABLE "account" (
  "id" BIGSERIAL NOT NULL PRIMARY KEY,
  "username" VARCHAR NOT NULL,
  "domain" VARCHAR,
  "display_name" VARCHAR NOT NULL,
  "locked" BOOLEAN NOT NULL DEFAULT FALSE,
  "created_at" TIMESTAMP WITH TIME ZONE NOT NULL,
  "followers_count" INTEGER  NOT NULL DEFAULT 0,
  "following_count" INTEGER  NOT NULL DEFAULT 0,
  "statuses_count" INTEGER  NOT NULL DEFAULT 0,
  "note" TEXT NOT NULL,
  "url" VARCHAR NOT NULL,
  "avatar" VARCHAR NOT NULL,
  "avatar_static" VARCHAR NOT NULL,
  "header" VARCHAR NOT NULL,
  "header_static" VARCHAR NOT NULL,
  "moved_to_account_id" BIGINT REFERENCES "account"("id"),
  -- Service or Person - correlates to "bot" in the REST entity
  "actor_type" VARCHAR NOT NULL
)