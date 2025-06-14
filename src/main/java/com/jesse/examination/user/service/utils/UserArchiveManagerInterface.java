package com.jesse.examination.user.service.utils;

import com.jesse.examination.file.exceptions.DirectoryRenameException;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;

/**
 * 用户存档管理工具类接口（用户和管理员都会用到）。
*/
public interface UserArchiveManagerInterface
{
    /**
     * 获取用户默认头像。
     */
    byte[] getDefaultAvatarImage();

    /**
     * 获取指定用户头像数据。
     */
    byte[] getUserAvatarImage(String userName) throws IOException;

    /**
     * 设置指定用户头像数据。
     */
    void setUserAvatarImage(String userName, byte[] imageDataBytes) throws IOException;

    /**
     * 将已经存在的旧路径名 oldUserName 修改成新路径名 newUserName。
     */
    void renameUserArchiveDir(
            String oldUserName, String newUserName
    ) throws DirectoryRenameException;

    /**
     * 为新用户创建存档数据，数据描述如下所示：
     *
     * <ol>
     *      <li>创建用户所有问题答对次数记录文件</li>
     *      <li>将默认的用户所有问题答对次数列表写入 Redis 数据库</li>
     * </ol>
     *
     * @param userName 指定用户名
     */
    void createNewArchiveForUser(String userName) throws IOException;

    /**
     * 用户登录时，读取用户的存档信息，具体操作如下所示。
     *
     * <ol>
     *     <li>读取用户所有问题答对次数记录文件</li>
     *     <li>
     *         将该列表整体存入 Redis 数据库中，</br>
     *         键值对是这样的：
     *         <pre>
     *         [key = userName, value = questionCorrectTimesDTOS]
     *         </pre>
     *          在 Redis 服务器中使用 LRANGE userName 0 -1 命令可以查看这个列表。</br>
     *         （这大概不是最好的写法，但是目前能用就先予以保留吧。）
     *     </li>
     * </ol>
     *
     * @param userName 指定用户名
     */
     void readUserArchive(String userName);

    /**
     * 用户登出时，保存用户的存档信息，具体操作如下所示：
     *
     * <ol>
     *     <li>将用户所有问题答对次数列表从 Redis 中读出，写入指定文件中</li>
     *     <li>删除 Redis 数据库中 userName 键对应的整个列表</li>
     * </ol>
     */
     void saveUserArchive(String userName);

    /**
     * 删除用户时，对应的存档、数据库记录也应该一并删除，
     * 具体要删除这几类数据：
     *
     * <ol>
     *     <li>删除用户存档数据</li>
     *     <li>删除 Redis 数据库中 userName 键对应的整个列表</li>
     *     <li>删除 score_record 表中所有用户名为 userName 的数据行</li>
     * </ol>
     */
     void deleteUserArchive(String userName);

     /**
      * 获取内部的 RedisTemplate 进行一些独立的操作，
      * 有点破坏封装性但是可控且值得。
      */
     RedisTemplate<String, Object> getRedisTemplate();
}
