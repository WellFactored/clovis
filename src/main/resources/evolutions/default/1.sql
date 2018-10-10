DROP TABLE IF EXISTS "follow";
DROP TABLE IF EXISTS "account";

CREATE TABLE "account" (
  "id" BIGSERIAL NOT NULL PRIMARY KEY,
  "username" VARCHAR NOT NULL,
  "domain" VARCHAR,
  "display_name" VARCHAR NOT NULL,
  "locked" BOOLEAN NOT NULL DEFAULT FALSE,
  "created_at" TIMESTAMP WITH TIME ZONE NOT NULL,
  "note" TEXT NOT NULL,
  "url" VARCHAR NOT NULL,
  "avatar" VARCHAR NOT NULL,
  "avatar_static" VARCHAR NOT NULL,
  "header" VARCHAR NOT NULL,
  "header_static" VARCHAR NOT NULL,
  "moved_to_account_id" BIGINT REFERENCES "account"("id"),
  -- Service or Person - correlates to "bot" in the REST entity
  "actor_type" VARCHAR NOT NULL
);



CREATE TABLE "follow" (
  "follower_id" BIGINT NOT NULL REFERENCES "account"("id"),
  "followed_id" BIGINT NOT NULL REFERENCES "account"("id"),
  "show_reblogs" BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE "follow" ADD PRIMARY KEY ("follower_id", "followed_id");
CREATE INDEX "follower_idx" on "follow" ( "follower_id" );
CREATE INDEX "followed_idx" on "follow" ( "followed_id" );

