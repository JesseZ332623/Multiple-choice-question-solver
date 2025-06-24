package com.jesse.examination.redis.keys;

import org.jetbrains.annotations.NotNull;

public enum ProjectRedisKey implements CharSequence
{
    /**
     * <p>某普通用户登录状态确认 Redis 键。</p>
     * <p>
     *     格式为：
     *     <pre>
     *         K: LOGIN_STATUS_OF_USER_[USER_NAME]
     *         V: String
     *     </pre>
     * </p>
     */
    USER_LOGIN_STATUS_KEY("LOGIN_STATUS_OF_USER_"),

    /**
     * <p>某管理员登录状态确认 Redis 键。</p>
     * <p>
     *     格式为：
     *     <pre>
     *         K: LOGIN_STATUS_OF_ADMIN_[ADMIN_NAME]
     *         V: String
     *     </pre>
     * </p>
     */
    ADMIN_LOGIN_STATUS_KEY("LOGIN_STATUS_OF_ADMIN_"),

    /**
     * <p>用户所有问题答对次数列表的 Redis 键。</p>
     * <p>
     *     格式为：
     *     <pre>
     *         K: CORRECT_TIMES_LIST_OF_[USER_NAME]
     *         V: List（准确来说是 List{@literal <List>}）
     *     </pre>
     * </p>
     */
    CORRECT_TIMES_LIST_KEY("CORRECT_TIMES_LIST_OF_"),

    /**
     * <p>验证码发起者邮箱 Redis 键。</p>
     * <p>
     *     格式为：
     *     <pre>
     *         K: ENTERPRISE_EMAIL_ADDRESS
     *         V: String
     *     </pre>
     * </p>
     */
    ENTERPRISE_EMAIL_ADDRESS("ENTERPRISE_EMAIL_ADDRESS"),

    /**
     * <p>来自邮箱服务提供的授权码键。</p>
     * <p>
     *     格式为：
     *     <pre>
     *         K: SERVICE_AUTH_CODE
     *         V: String
     *     </pre>
     * </p>
     */
    SERVICE_AUTH_CODE("SERVICE_AUTH_CODE"),

    /**
     * <p>某用户请求的验证码键。</p>
     * <p>
     *     格式为：
     *     <pre>
     *         K: VERIFY_CODE_FOR_[USER_NAME]
     *         V: String
     *     </pre>
     * </p>
     */
    USER_VERIFYCODE_KEY("VERIFY_CODE_FOR_");

    final String keyName;

    ProjectRedisKey(String name) { this.keyName = name; }

    /**
     * Returns the length of this character sequence.  The length is the number
     * of 16-bit {@code char}s in the sequence.
     *
     * @return the number of {@code char}s in this sequence
     */
    @Override
    public int length() { return this.keyName.length(); }

    /**
     * Returns the {@code char} value at the specified index.  An index ranges from zero
     * to {@code length() - 1}.  The first {@code char} value of the sequence is at
     * index zero, the next at index one, and so on, as for array
     * indexing.
     *
     * <p>If the {@code char} value specified by the index is a
     * {@linkplain Character##unicode surrogate}, the surrogate value
     * is returned.
     *
     * @param index the index of the {@code char} value to be returned
     * @return the specified {@code char} value
     * @throws IndexOutOfBoundsException if the {@code index} argument is negative or not less than
     *                                   {@code length()}
     */
    @Override
    public char charAt(int index) {
        return this.keyName.charAt(index);
    }

    /**
     * Returns a {@code CharSequence} that is a subsequence of this sequence.
     * The subsequence starts with the {@code char} value at the specified index and
     * ends with the {@code char} value at index {@code end - 1}.  The length
     * (in {@code char}s) of the
     * returned sequence is {@code end - start}, so if {@code start == end}
     * then an empty sequence is returned.
     *
     * @param start the start index, inclusive
     * @param end   the end index, exclusive
     * @return the specified subsequence
     * @throws IndexOutOfBoundsException if {@code start} or {@code end} are negative,
     *                                   if {@code end} is greater than {@code length()},
     *                                   or if {@code start} is greater than {@code end}
     */
    @Override
    public @NotNull CharSequence subSequence(int start, int end) {
        return this.keyName.subSequence(start, end);
    }

    @Override
    public @NotNull String toString() {
        return this.keyName;
    }
}
