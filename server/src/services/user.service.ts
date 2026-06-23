import prisma from '../config/database';
import { NotFoundError } from '../errors/AppError';
import { AuthErrorMessage } from '../constants/errorString';
import { sanitize } from '../utils/sanitize';
import type { UpdateUserInput } from '../validators/user.validator';

// 获取用户公开信息
export async function getUserById(userId: number) {
  const user = await prisma.user.findUnique({
    where: { id: userId },
    select: {
      id: true,
      username: true,
      nickname: true,
      email: true,
      avatar: true,
      gender: true,
      signature: true,
      createdAt: true,
    },
  });

  if (!user) {
    throw new NotFoundError(AuthErrorMessage.USER_NOT_FOUND);
  }

  return user;
}

// 更新用户资料（仅允许更新非敏感字段）
export async function updateUser(userId: number, data: UpdateUserInput) {
  const user = await prisma.user.findUnique({ where: { id: userId } });
  if (!user) {
    throw new NotFoundError(AuthErrorMessage.USER_NOT_FOUND);
  }

  // XSS 过滤字符串字段
  const updateData: Record<string, any> = {};
  if (data.nickname !== undefined) updateData.nickname = sanitize(data.nickname);
  if (data.avatar !== undefined) updateData.avatar = data.avatar;
  if (data.gender !== undefined) updateData.gender = data.gender;
  if (data.signature !== undefined) updateData.signature = sanitize(data.signature);

  const updated = await prisma.user.update({
    where: { id: userId },
    data: updateData,
    select: {
      id: true,
      username: true,
      nickname: true,
      email: true,
      avatar: true,
      gender: true,
      signature: true,
    },
  });

  return updated;
}
