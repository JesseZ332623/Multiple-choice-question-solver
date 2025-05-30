async function doAvatarModify() {
    // 文件大小不得超过 8 MB
    const MAX_FILE_SIZE = 8 * 1024 * 1024;

    // 支持的文件类型
    const ALLOWED_FILE_TYPES = ['image/png', 'image/jpeg', 'image/webp'];

    const avatarModifyButton = document.getElementById('avatar-input');

    avatarModifyButton.addEventListener('change',
        async (event) => {
            const file = event.target.files[0];  // 获取上传的第一个文件？
            const statusElement = document.getElementById('upload-status');

            if (!file) { return; }

            // 检查文件格式
            if (!ALLOWED_FILE_TYPES.includes(file.type)) {
                statusElement.textContent = '仅支持 PNG/JPED/WEBP 格式的图片。';
                statusElement.style.color = '#F47067';

                return;
            }

            // 检查文件大小
            if (file.size >= MAX_FILE_SIZE) {
                statusElement.textContent = `文件大小应小于 ${MAX_FILE_SIZE / 1024 / 1024} MB!`;
                statusElement.style.color = '#F47067';

                return;
            }

            try {
                statusElement.textContent = '头像上传中';
                statusElement.style.color = 'inherit';

                const fileByteArray = new Uint8Array(await file.arrayBuffer());

                const csrfToken = document.querySelector('meta[name="_csrf"]').content;
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

                // 发送请求
                const response
                    = await fetch('/api/user_info/set_user_overview_avatar',
                        {
                            method: 'POST',
                            headers:
                            {
                                'Content-Type': 'application/octet-stream',
                                [csrfHeader]: csrfToken
                            },
                            body: fileByteArray
                        }
                    );

                if (!response.ok) {
                    throw new Error(await response.text());
                }

                statusElement.textContent = '头像更新成功！';
                statusElement.style.color = '#57ab5a';

                // 1.5 秒后清除状态
                setTimeout(
                    () => { statusElement.textContent = ''; location.reload(); },
                    1500
                );
            }
            catch (error) {
                console.error('Upload failed:', error);
                statusElement.textContent = `上传失败: ${error.message}`;
                statusElement.style.color = '#f47067';
            }
        }
    );
}