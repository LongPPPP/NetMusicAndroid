/*
  Warnings:

  - Made the column `email` on table `User` required. This step will fail if there are existing NULL values in that column.

*/
-- RedefineTables
PRAGMA
defer_foreign_keys=ON;
PRAGMA
foreign_keys=OFF;
CREATE TABLE "new_User"
(
    "id"        INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT,
    "username"  TEXT     NOT NULL,
    "password"  TEXT     NOT NULL,
    "nickname"  TEXT,
    "email"     TEXT     NOT NULL,
    "avatar"    TEXT,
    "gender"    INTEGER  NOT NULL DEFAULT 0,
    "signature" TEXT,
    "createdAt" DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" DATETIME NOT NULL
);
-- 先给已有的 NULL email 设置一个占位值，避免 NOT NULL 冲突
UPDATE "User"
SET "email" = 'deleted-' || "id" || '@placeholder.local'
WHERE "email" IS NULL;
INSERT INTO "new_User" ("avatar", "createdAt", "email", "gender", "id", "nickname", "password", "signature",
                        "updatedAt", "username")
SELECT "avatar",
       "createdAt",
       "email",
       "gender",
       "id",
       "nickname",
       "password",
       "signature",
       "updatedAt",
       "username"
FROM "User";
DROP TABLE "User";
ALTER TABLE "new_User" RENAME TO "User";
CREATE UNIQUE INDEX "User_username_key" ON "User" ("username");
CREATE UNIQUE INDEX "User_email_key" ON "User" ("email");
PRAGMA
foreign_keys=ON;
PRAGMA
defer_foreign_keys=OFF;
